import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TodoListApp extends JFrame {
  private List<Task> tasks = new ArrayList<>();
  private DefaultListModel<String> listModel = new DefaultListModel<>();
  private JList<String> taskList = new JList<>(listModel);
  private JTextField taskInput = new JTextField(20);
  private JComboBox<String> filterCombo = new JComboBox<>(new String[]{"All", "Pending", "Completed"});
  private static final String FILE_NAME = "tasks.txt";

  static class Task {
    String name;
    boolean completed;

    Task(String name) {
      this.name = name;
      this.completed = false;
    }

    @Override
    public String toString(){
      return (completed ? "[X] " : "[ ] ") + name;
    }
  }

  public TodoListApp(){
    // Window setup
    setTitle("To-Do List App");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    setSize(400, 500);

    // Load tasks from file
    loadTasks();

    // Input panel
    JPanel inputPanel = new JPanel();
    JButton addButton = new JButton("Add");
    inputPanel.add(new JLabel("Task:"));
    inputPanel.add(taskInput);
    inputPanel.add(addButton);
    add(inputPanel, BorderLayout.NORTH);

    // Task list panel
    JScrollPane scrollPane = new JScrollPane(taskList);
    add(scrollPane, BorderLayout.CENTER);

    // Control panel
    JPanel controlPanel = new JPanel();
    JButton completeButton = new JButton("Complete");
    JButton editButton = new JButton("Edit");
    JButton deleteButton = new JButton("Delete");
    controlPanel.add(filterCombo);
    controlPanel.add(completeButton);
    controlPanel.add(editButton);
    controlPanel.add(deleteButton);
    add(controlPanel, BorderLayout.SOUTH);

    // Event listeners
    addButton.addActionListener(e -> addTask());
    completeButton.addActionListener(e -> toggleComplete());
    editButton.addActionListener(e -> editTask());
    deleteButton.addActionListener(e -> deleteTask());
    filterCombo.addActionListener(e -> updateList());
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        saveTask();
      }
    });

    updateList();
    setVisible(true);
  }

  private void addTask() {
    String taskName = taskInput.getText().trim();
    if(!taskName.isEmpty()) {
      tasks.add(new Task(taskName));
      taskInput.setText("");
      updateList();
    }
  }

  private void toggleComplete() {
    int index = taskList.getSelectedIndex();
    if (index != -1) {
      int taskIndex = getTaskIndexFromFiltered(index);
      Task task = tasks.get(taskIndex);
      task.completed = !task.completed;
      updateList();
    }
  }

  private void editTask() {
    int index = taskList.getSelectedIndex();
    if(index != -1) {
      int taskIndex = getTaskIndexFromFiltered(index);
      Task task = tasks.get(taskIndex);
      String newName = JOptionPane.showInputDialog(this, "Edit Task:", task.name);
      if(newName != null && !newName.trim().isEmpty()) {
        task.name = newName.trim();
        updateList();
      }
    }
  }

  private void deleteTask() {
    int index = taskList.getSelectedIndex();
    if(index != -1) {
      int taskIndex = getTaskIndexFromFiltered(index);
      tasks.remove(taskIndex);
      updateList();
    }
  }

  private int getTaskIndexFromFiltered(int filteredIndex) {
    String filter = (String) filterCombo.getSelectedItem();
    int count = -1;
    for(int i = 0; i < tasks.size(); i++) {
      Task task = tasks.get(i);
      if(filter.equals("All") || (filter.equals("Pending") && !task.completed) || (filter.equals("Completed") && task.completed)) {
        count++;
        if (count == filteredIndex) return i;
      }
    }
    return -1;
  }

  private void updateList() {
    listModel.clear();
    String filter = (String) filterCombo.getSelectedItem();
    for(Task task : tasks) {
      if(filter.equals("All") || (filter.equals("Pending") && !task.completed) || (filter.equals("Completed") && task.completed)) {
        listModel.addElement(task.toString());
      }
    }
  }

  private void saveTask() {
    try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
      for(Task task : tasks) {
        writer.println(task.name + "," + task.completed);
      }
    }catch(IOException e){
      JOptionPane.showMessageDialog(this, "Error saving tasks: " + e.getMessage());
    }
  }

  private void loadTasks() {
    File file = new File(FILE_NAME);

    if(file.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(file))){
        String line;
        while((line = reader.readLine()) != null) {
          String[] parts = line.split(",", 2);
          if(parts.length == 2) {
            Task task = new Task(parts[0]);
            task.completed = Boolean.parseBoolean(parts[1]);
            tasks.add(task);
          }
        }
      }catch(IOException e) {
        JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage());
      }
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(TodoListApp::new);
  }
}
