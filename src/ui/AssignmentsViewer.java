package ui;
import database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
public class AssignmentsViewer extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton filterButton;
    private JButton showAllButton;
    private JTextField dateField; 
    public AssignmentsViewer() {
        setTitle("Hospital - Staff Shift Assignments");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 450);
        setLocationRelativeTo(null);
        initUI();
        loadData(null); 
    }
    private void initUI() {
        String[] columns = {"Staff_ID", "Name", "Department", "Shift", "Duty_Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true); 
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controlPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField(10);
        controlPanel.add(dateField);
        filterButton = new JButton("Filter");
        controlPanel.add(filterButton);
        showAllButton = new JButton("Show All");
        controlPanel.add(showAllButton);
        refreshButton = new JButton("Refresh");
        controlPanel.add(refreshButton);
        filterButton.addActionListener(e -> {
            String dateText = dateField.getText().trim();
            if (dateText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a date in format YYYY-MM-DD, or click Show All.",
                        "Input Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                LocalDate.parse(dateText); 
                loadData(dateText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid date format. Use YYYY-MM-DD (example: 2025-10-29).",
                        "Invalid Date",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        showAllButton.addActionListener(e -> {
            dateField.setText("");
            loadData(null);
        });
        refreshButton.addActionListener(e -> {
            String dateText = dateField.getText().trim();
            if (dateText.isEmpty()) loadData(null);
            else loadData(dateText);
        });
        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(controlPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }
    private void loadData(String dateFilter) {
        tableModel.setRowCount(0);
        String sqlAll = "SELECT s.id AS Staff_ID, s.name AS Staff_Name, s.department AS Department, " +
                "a.shift_type AS Shift, a.duty_date AS Duty_Date " +
                "FROM staff s JOIN shift_assignment a ON s.id = a.staff_id " +
                "ORDER BY a.duty_date, a.shift_type;";
        String sqlByDate = "SELECT s.id AS Staff_ID, s.name AS Staff_Name, s.department AS Department, " +
                "a.shift_type AS Shift, a.duty_date AS Duty_Date " +
                "FROM staff s JOIN shift_assignment a ON s.id = a.staff_id " +
                "WHERE a.duty_date = ? " +
                "ORDER BY a.shift_type;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = (dateFilter == null) ? conn.prepareStatement(sqlAll) : conn.prepareStatement(sqlByDate)) {
            if (dateFilter != null) {
                ps.setDate(1, Date.valueOf(LocalDate.parse(dateFilter)));
            }
            try (ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    int staffId = rs.getInt("Staff_ID");
                    String name = rs.getString("Staff_Name");
                    String dept = rs.getString("Department");
                    String shift = rs.getString("Shift");
                    Date dutyDate = rs.getDate("Duty_Date");
                    tableModel.addRow(new Object[]{
                            staffId,
                            name,
                            dept,
                            shift,
                            dutyDate != null ? dutyDate.toString() : ""
                    });
                }
                if (!any) {
                    if (dateFilter == null) {
                        JOptionPane.showMessageDialog(this,
                                "No assignment records found in database.",
                                "No Data",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No assignments found for date: " + dateFilter,
                                "No Data",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AssignmentsViewer v = new AssignmentsViewer();
            v.setVisible(true);
        });
    }
}
