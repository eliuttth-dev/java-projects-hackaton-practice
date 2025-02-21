import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NotesApp extends JFrame {
    private List<Note> notes = new ArrayList<>();
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> noteList = new JList<>(listModel);
    private JTextField titleField = new JTextField(20);
    private JTextPane contentPane = new JTextPane();
    private JTextField searchField = new JTextField(20);
    private static final String FILE_NAME = "notes.dat";

    // Define style constants
    private static final int STYLE_BOLD = 1;
    private static final int STYLE_ITALIC = 2;
    private static final int STYLE_UNDERLINE = 3;

    static class Note implements Serializable {
        String title;
        StyledDocument doc;

        Note(String title, StyledDocument doc) {
            this.title = title;
            this.doc = createDocumentCopy(doc); // Now calls static method
        }
    }

    public NotesApp() {
        // Window setup
        setTitle("Notes App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(600, 400);

        // Load notes from file
        loadNotes();

        // Top panel (title input and formatting)
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        titlePanel.add(titleField);
        JButton boldButton = new JButton("B");
        boldButton.setFont(new Font("Serif", Font.BOLD, 12));
        JButton italicButton = new JButton("I");
        italicButton.setFont(new Font("Serif", Font.ITALIC, 12));
        JButton underlineButton = new JButton("U");
        underlineButton.setFont(new Font("Serif", Font.PLAIN, 12));
        underlineButton.setText("<html><u>U</u></html>");
        JPanel formatPanel = new JPanel();
        formatPanel.add(boldButton);
        formatPanel.add(italicButton);
        formatPanel.add(underlineButton);
        topPanel.add(titlePanel, BorderLayout.CENTER);
        topPanel.add(formatPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center panel (list and content)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(noteList), new JScrollPane(contentPane));
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

        // Bottom panel (controls and search)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        JButton newButton = new JButton("New");
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        buttonPanel.add(newButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        bottomPanel.add(buttonPanel, BorderLayout.WEST);
        bottomPanel.add(searchPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH); // Corrected to bottomPanel

        // Event listeners
        noteList.addListSelectionListener(e -> displaySelectedNote());
        newButton.addActionListener(e -> newNote());
        saveButton.addActionListener(e -> saveNote());
        deleteButton.addActionListener(e -> deleteNote()); // Fixed from saveNote() to deleteNote()
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterNotes();
            }
        });
        boldButton.addActionListener(e -> applyStyle(STYLE_BOLD));
        italicButton.addActionListener(e -> applyStyle(STYLE_ITALIC));
        underlineButton.addActionListener(e -> applyStyle(STYLE_UNDERLINE));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveNotesToFile();
            }
        });

        updateList();
        setVisible(true);
    }

    private void newNote() {
        titleField.setText("");
        contentPane.setText("");
        contentPane.setStyledDocument(new DefaultStyledDocument());
        noteList.clearSelection();
    }

    private void saveNote() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty!");
            return;
        }
        int selectedIndex = noteList.getSelectedIndex();
        if (selectedIndex == -1) {
            notes.add(new Note(title, contentPane.getStyledDocument()));
        } else {
            int noteIndex = getNoteIndexFromFiltered(selectedIndex);
            notes.set(noteIndex, new Note(title, contentPane.getStyledDocument()));
        }
        updateList();
        noteList.clearSelection();
    }

    private void deleteNote() {
        int selectedIndex = noteList.getSelectedIndex();
        if (selectedIndex != -1) {
            int noteIndex = getNoteIndexFromFiltered(selectedIndex);
            notes.remove(noteIndex);
            updateList();
            newNote();
        }
    }

    private void displaySelectedNote() {
        int selectedIndex = noteList.getSelectedIndex();
        if (selectedIndex != -1) {
            int noteIndex = getNoteIndexFromFiltered(selectedIndex);
            Note note = notes.get(noteIndex);
            titleField.setText(note.title);
            contentPane.setStyledDocument(createDocumentCopy(note.doc));
        }
    }

    private int getNoteIndexFromFiltered(int filteredIndex) {
        String query = searchField.getText().trim().toLowerCase();
        int count = -1;
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            String content = getTextFromDoc(note.doc).toLowerCase();
            if (query.isEmpty() || note.title.toLowerCase().contains(query) || content.contains(query)) {
                count++;
                if (count == filteredIndex) return i;
            }
        }
        return -1;
    }

    private void filterNotes() {
        updateList();
    }

    private void updateList() {
        listModel.clear();
        String query = searchField.getText().trim().toLowerCase();
        for (Note note : notes) {
            String content = getTextFromDoc(note.doc).toLowerCase();
            if (query.isEmpty() || note.title.toLowerCase().contains(query) || content.contains(query)) {
                listModel.addElement(note.title);
            }
        }
    }

    private String getTextFromDoc(StyledDocument doc) {
        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return "";
        }
    }

    private void applyStyle(int style) {
        StyledDocument doc = contentPane.getStyledDocument();
        int start = contentPane.getSelectionStart();
        int end = contentPane.getSelectionEnd();
        if (start != end) {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            AttributeSet currentAttrs = doc.getCharacterElement(start).getAttributes();
            switch (style) {
                case STYLE_BOLD:
                    StyleConstants.setBold(attrs, !StyleConstants.isBold(currentAttrs));
                    break;
                case STYLE_ITALIC:
                    StyleConstants.setItalic(attrs, !StyleConstants.isItalic(currentAttrs));
                    break;
                case STYLE_UNDERLINE:
                    StyleConstants.setUnderline(attrs, !StyleConstants.isUnderline(currentAttrs));
                    break;
            }
            doc.setCharacterAttributes(start, end - start, attrs, false);
        }
    }

    private static StyledDocument createDocumentCopy(StyledDocument original) {
        DefaultStyledDocument copy = new DefaultStyledDocument();
        try {
            String text = original.getText(0, original.getLength());
            copy.insertString(0, text, null);
            for (int i = 0; i < original.getLength(); i++) {
                AttributeSet attrs = original.getCharacterElement(i).getAttributes();
                copy.setCharacterAttributes(i, 1, attrs, false);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return copy;
    }

    private void saveNotesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(notes);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving notes: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadNotes() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                notes = (List<Note>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Error loading notes: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NotesApp::new);
    }
}
