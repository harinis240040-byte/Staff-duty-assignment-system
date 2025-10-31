package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;


public class  ViewAllDuties extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton refreshButton;

    public ViewAllDuties() {
        setTitle("Hospital - All Staff Shift Assignments");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);
        initUI();
        loadData();
    }

    private void initUI() {
        // Table columns
        String[] columns = {"Staff_ID", "Name", "Department", "Shift", "Duty_Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            // Make table non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true); // allow sorting by clicking headers

        JScrollPane scrollPane = new JScrollPane(table);

        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadData());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(refreshButton);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);

        String sql = "SELECT s.id AS Staff_ID, s.name AS Staff_Name, s.department AS Department, " +
                     "a.shift_type AS Shift, a.duty_date AS Duty_Date " +
                     "FROM staff s " +
                     "JOIN shift_assignment a ON s.id = a.staff_id " +
                     "ORDER BY a.duty_date, a.shift_type;";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int staffId = rs.getInt("Staff_ID");
                String name = rs.getString("Staff_Name");
                String dept = rs.getString("Department");
                String shift = rs.getString("Shift");
                Date dutyDate = rs.getDate("Duty_Date");

                // Add row to table model
                tableModel.addRow(new Object[]{
                    staffId,
                    name,
                    dept,
                    shift,
                    dutyDate != null ? dutyDate.toString() : null
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                    "No assignment records found.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading data from database:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ViewAllDuties viewer = new ViewAllDuties();
            viewer.setVisible(true);
        });
    }
}
