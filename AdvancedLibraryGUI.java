import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

// ===== BOOK CLASS =====
class Book {
    int id;
    String title;
    String author;
    boolean issued;
    String borrower;
    String issueDate; // format: YYYY-MM-DD

    public Book(int id, String title, String author, boolean issued, String borrower, String issueDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.issued = issued;
        this.borrower = borrower;
        this.issueDate = issueDate;
    }

    @Override
    public String toString() {
        return id + "," + title + "," + author + "," + issued + "," +
                (borrower == null ? "" : borrower) + "," + (issueDate == null ? "" : issueDate);
    }

    public static Book fromString(String s) {
        String[] p = s.split(",", -1);
        return new Book(Integer.parseInt(p[0]), p[1], p[2], Boolean.parseBoolean(p[3]),
                p[4].isEmpty() ? null : p[4], p[5].isEmpty() ? null : p[5]);
    }
}

// ===== MAIN GUI =====
public class AdvancedLibraryGUI extends JFrame {

    private ArrayList<Book> books = new ArrayList<>();
    private JTable table;
    private DefaultTableModel tableModel;

    public AdvancedLibraryGUI() {
        setTitle("Advanced Library System");
        setSize(800, 500);
        setLayout(new BorderLayout());

        // ===== TOP PANEL INPUTS =====
        JPanel inputPanel = new JPanel(new FlowLayout());
        JTextField idField = new JTextField(5);
        JTextField titleField = new JTextField(10);
        JTextField authorField = new JTextField(10);

        inputPanel.add(new JLabel("ID")); inputPanel.add(idField);
        inputPanel.add(new JLabel("Title")); inputPanel.add(titleField);
        inputPanel.add(new JLabel("Author")); inputPanel.add(authorField);

        add(inputPanel, BorderLayout.NORTH);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add");
        JButton showBtn = new JButton("Show All");
        JButton searchBtn = new JButton("Search");
        JButton issueBtn = new JButton("Issue");
        JButton returnBtn = new JButton("Return");
        JButton sortTitleBtn = new JButton("Sort by Title");
        JButton sortAuthorBtn = new JButton("Sort by Author");
        JButton filterBtn = new JButton("Show Available");
        JButton deleteBtn = new JButton("Delete");
        buttonPanel.add(addBtn); buttonPanel.add(showBtn); buttonPanel.add(searchBtn);
        buttonPanel.add(issueBtn); buttonPanel.add(returnBtn);
        buttonPanel.add(sortTitleBtn); buttonPanel.add(sortAuthorBtn); buttonPanel.add(filterBtn);
        buttonPanel.add(deleteBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // ===== TABLE FOR DISPLAY =====
        String[] columns = {"ID", "Title", "Author", "Status", "Borrower", "Issue Date", "Overdue"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadBooks();
        refreshTable();

        // ===== BUTTON ACTIONS =====

        // Add Book
        addBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();

                if (title.isEmpty() || author.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title and Author cannot be empty!");
                    return;
                }

                boolean exists = books.stream().anyMatch(b -> b.id == id);
                if (exists) {
                    JOptionPane.showMessageDialog(this, "Book with this ID already exists!");
                    return;
                }

                books.add(new Book(id, title, author, false, null, null));
                saveBooks();
                refreshTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID! Enter a number.");
            }
        });

        // Show All
        showBtn.addActionListener(e -> refreshTable());

        // Search
        searchBtn.addActionListener(e -> {
            String keyword = titleField.getText().trim().toLowerCase();
            tableModel.setRowCount(0);
            for (Book b : books) {
                if (String.valueOf(b.id).equals(keyword) ||
                    b.title.toLowerCase().contains(keyword) ||
                    b.author.toLowerCase().contains(keyword)) {
                    tableModel.addRow(bookRow(b));
                }
            }
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No matching books found.");
            }
        });

        // Issue Book
        issueBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                for (Book b : books) {
                    if (b.id == id) {
                        if (!b.issued) {
                            b.issued = true;
                            b.borrower = JOptionPane.showInputDialog(this, "Enter Borrower Name:");
                            b.issueDate = LocalDate.now().toString();
                            saveBooks();
                            refreshTable();
                            return;
                        } else {
                            JOptionPane.showMessageDialog(this, "Book already issued.");
                            return;
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Book ID not found.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID!");
            }
        });

        // Return Book
        returnBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                for (Book b : books) {
                    if (b.id == id) {
                        if (b.issued) {
                            b.issued = false;
                            b.borrower = null;
                            b.issueDate = null;
                            saveBooks();
                            refreshTable();
                            return;
                        } else {
                            JOptionPane.showMessageDialog(this, "Book was not issued.");
                            return;
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Book ID not found.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID!");
            }
        });

        // Sort by Title
        sortTitleBtn.addActionListener(e -> {
            books.sort(Comparator.comparing(b -> b.title.toLowerCase()));
            refreshTable();
        });

        // Sort by Author
        sortAuthorBtn.addActionListener(e -> {
            books.sort(Comparator.comparing(b -> b.author.toLowerCase()));
            refreshTable();
        });

        // Show Only Available
        filterBtn.addActionListener(e -> {
            tableModel.setRowCount(0);
            for (Book b : books) {
                if (!b.issued) tableModel.addRow(bookRow(b));
            }
        });

        // Delete Book
        deleteBtn.addActionListener(e -> {
             try {
                 int id = Integer.parseInt(idField.getText().trim());

                     for (int i = 0; i < books.size(); i++) {
                        if (books.get(i).id == id) { // Confirmation popup
                            int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to delete this book?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION );
                            if (confirm == JOptionPane.YES_OPTION) {
                                books.remove(i);
                                saveBooks();
                                refreshTable();
                                JOptionPane.showMessageDialog(this, "Book deleted successfully.");
                            }
                            return;
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Book ID not found.");
                } catch (NumberFormatException ex) {
                 JOptionPane.showMessageDialog(this, "Invalid ID!");
            }
        });
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Convert Book to Table Row
    private Object[] bookRow(Book b) {
        String overdue = "";
        if (b.issued && b.issueDate != null) {
            LocalDate issue = LocalDate.parse(b.issueDate);
            long days = ChronoUnit.DAYS.between(issue, LocalDate.now());
            if (days > 2) overdue = "Yes";
        }
        return new Object[]{b.id, b.title, b.author, b.issued ? "Issued" : "Available",
                b.borrower == null ? "" : b.borrower,
                b.issueDate == null ? "" : b.issueDate, overdue};
    }

    // Refresh Table
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Book b : books) tableModel.addRow(bookRow(b));
    }

    // Save Books to CSV
    private void saveBooks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("books.txt"))) {
            for (Book b : books) {
                bw.write(b.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving books: " + e.getMessage());
        }
    }

    // Load Books from CSV
    private void loadBooks() {
        File file = new File("books.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                books.add(Book.fromString(line));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdvancedLibraryGUI::new);
    }
}