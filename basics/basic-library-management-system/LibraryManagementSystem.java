import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.List;  // Explicitly import java.util.List only
import java.util.stream.Collectors;

// Book class to represent book data
class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String title;
    private String author;
    private String genre;
    private boolean isBorrowed;

    public Book(String id, String title, String author, String genre) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isBorrowed = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public boolean isBorrowed() { return isBorrowed; }
    public void setBorrowed(boolean borrowed) { this.isBorrowed = borrowed; }

    @Override
    public String toString() {
        return String.format("ID: %s | Title: %s | Author: %s | Genre: %s | %s",
            id, title, author, genre, isBorrowed ? "Borrowed" : "Available");
    }
}

// Library class to manage book collection
class Library implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Book> books;

    public Library() {
        books = new HashMap<>();
    }

    public void addBook(Book book) {
        books.put(book.getId(), book);
    }

    public void removeBook(String id) {
        books.remove(id);
    }

    public void borrowBook(String id) {
        Book book = books.get(id);
        if (book != null && !book.isBorrowed()) {
            book.setBorrowed(true);
        }
    }

    public void returnBook(String id) {
        Book book = books.get(id);
        if (book != null && book.isBorrowed()) {
            book.setBorrowed(false);
        }
    }

    public List<Book> searchBooks(String query, String searchType) {
        return books.values().stream()
            .filter(book -> {
                String field = switch (searchType) {
                    case "Title" -> book.getTitle();
                    case "Author" -> book.getAuthor();
                    case "Genre" -> book.getGenre();
                    default -> "";
                };
                return field.toLowerCase().contains(query.toLowerCase());
            })
            .collect(Collectors.toList());
    }

    public Collection<Book> getAllBooks() {
        return books.values();
    }

    public Book getBook(String id) {
        return books.get(id);
    }
}

// Main GUI class
public class LibraryManagementSystem extends JFrame {
    private Library library;
    private JTextArea displayArea;
    private JTextField idField, titleField, authorField, genreField, searchField;
    private JComboBox<String> searchTypeCombo;

    public LibraryManagementSystem() {
        library = loadLibrary();
        initUI();
    }

    private void initUI() {
        setTitle("Library Management System");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(6, 2));
        inputPanel.add(new JLabel("Book ID:"));
        idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        inputPanel.add(authorField);

        inputPanel.add(new JLabel("Genre:"));
        genreField = new JTextField();
        inputPanel.add(genreField);

        // Buttons
        JButton addButton = new JButton("Add Book");
        JButton editButton = new JButton("Edit Book");
        JButton deleteButton = new JButton("Delete Book");
        JButton borrowButton = new JButton("Borrow Book");
        JButton returnButton = new JButton("Return Book");

        inputPanel.add(addButton);
        inputPanel.add(editButton);
        inputPanel.add(deleteButton);
        inputPanel.add(borrowButton);
        inputPanel.add(returnButton);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(15);
        searchTypeCombo = new JComboBox<>(new String[]{"Title", "Author", "Genre"});
        JButton searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchTypeCombo);
        searchPanel.add(searchButton);

        // Display Area
        displayArea = new JTextArea(10, 50);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        // Add components to frame
        add(inputPanel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Event Listeners
        addButton.addActionListener(e -> addBook());
        editButton.addActionListener(e -> editBook());
        deleteButton.addActionListener(e -> deleteBook());
        borrowButton.addActionListener(e -> borrowBook());
        returnButton.addActionListener(e -> returnBook());
        searchButton.addActionListener(e -> searchBooks());

        // Save on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveLibrary();
            }
        });

        updateDisplay();
    }

    private void addBook() {
        String id = idField.getText();
        if (library.getBook(id) != null) {
            JOptionPane.showMessageDialog(this, "Book ID already exists!");
            return;
        }
        Book book = new Book(id, titleField.getText(), authorField.getText(), genreField.getText());
        library.addBook(book);
        clearFields();
        updateDisplay();
    }

    private void editBook() {
        String id = idField.getText();
        Book existingBook = library.getBook(id);
        if (existingBook == null) {
            JOptionPane.showMessageDialog(this, "Book not found!");
            return;
        }
        library.removeBook(id);
        Book updatedBook = new Book(id, titleField.getText(), authorField.getText(), genreField.getText());
        updatedBook.setBorrowed(existingBook.isBorrowed());
        library.addBook(updatedBook);
        clearFields();
        updateDisplay();
    }

    private void deleteBook() {
        String id = idField.getText();
        if (library.getBook(id) == null) {
            JOptionPane.showMessageDialog(this, "Book not found!");
            return;
        }
        library.removeBook(id);
        clearFields();
        updateDisplay();
    }

    private void borrowBook() {
        String id = idField.getText();
        Book book = library.getBook(id);
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Book not found!");
        } else if (book.isBorrowed()) {
            JOptionPane.showMessageDialog(this, "Book is already borrowed!");
        } else {
            library.borrowBook(id);
            updateDisplay();
        }
    }

    private void returnBook() {
        String id = idField.getText();
        Book book = library.getBook(id);
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Book not found!");
        } else if (!book.isBorrowed()) {
            JOptionPane.showMessageDialog(this, "Book wasn't borrowed!");
        } else {
            library.returnBook(id);
            updateDisplay();
        }
    }

    private void searchBooks() {
        String query = searchField.getText();
        String searchType = (String) searchTypeCombo.getSelectedItem();
        List<Book> results = library.searchBooks(query, searchType);
        displayArea.setText("Search Results:\n");
        results.forEach(book -> displayArea.append(book.toString() + "\n"));
    }

    private void updateDisplay() {
        displayArea.setText("All Books:\n");
        library.getAllBooks().forEach(book -> displayArea.append(book.toString() + "\n"));
    }

    private void clearFields() {
        idField.setText("");
        titleField.setText("");
        authorField.setText("");
        genreField.setText("");
    }

    private void saveLibrary() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("library.dat"))) {
            oos.writeObject(library);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Library loadLibrary() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("library.dat"))) {
            return (Library) ois.readObject();
        } catch (FileNotFoundException e) {
            return new Library();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new Library();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LibraryManagementSystem().setVisible(true);
        });
    }
}
