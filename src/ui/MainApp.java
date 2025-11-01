package ui;
import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
public class MainApp extends JFrame {
    private static final String DEMO_USER = "admin";
    private static final String DEMO_PASS = "admin123";
    private final JButton btnStaffMgmt;
    private final JButton btnViewAssignments;
    private final JButton btnAutoAssign;
    private final JButton btnLogout;
    private final JLabel lblStatus;
    public MainApp() {
        setTitle("Hospital Duty Assignment - Dashboard");
        setSize(480, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        JLabel hdr = new JLabel("Hospital Duty Assignment - Admin Dashboard", SwingConstants.CENTER);
        hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 16f));
        add(hdr, BorderLayout.NORTH);
        JPanel center = new JPanel(new GridLayout(4, 1, 8, 8));
        btnStaffMgmt = new JButton("Open Staff Management (AdminUI)");
        btnViewAssignments = new JButton("Open Assignments Viewer");
        btnAutoAssign = new JButton("Auto Assign Shifts (Date Range)");
        btnLogout = new JButton("Logout / Exit");
        center.add(btnStaffMgmt);
        center.add(btnViewAssignments);
        center.add(btnAutoAssign);
        center.add(btnLogout);
        add(center, BorderLayout.CENTER);
        lblStatus = new JLabel("Logged in as: admin (demo)", SwingConstants.CENTER);
        add(lblStatus, BorderLayout.SOUTH);
        btnStaffMgmt.addActionListener(e -> openAdminUI());
        btnViewAssignments.addActionListener(e -> openAssignmentsViewer());
        btnAutoAssign.addActionListener(e -> openAutoAssignDialog());
        btnLogout.addActionListener(e -> logoutConfirm());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logoutConfirm();
            }
        });
    }
    private void openAdminUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                AdminUI ui = new AdminUI();
                ui.setVisible(true);
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to open Admin UI: " + t.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    private void openAssignmentsViewer() {
        SwingUtilities.invokeLater(() -> {
            try {
                AssignmentsViewer v = new AssignmentsViewer();
                v.setVisible(true);
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to open Assignments Viewer: " + t.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    private void openAutoAssignDialog() {
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        p.add(new JLabel("Start Date (YYYY-MM-DD):"));
        JTextField startField = new JTextField(LocalDate.now().toString());
        p.add(startField);
        p.add(new JLabel("End Date (YYYY-MM-DD):"));
        JTextField endField = new JTextField(LocalDate.now().plusDays(6).toString());
        p.add(endField);
        int ok = JOptionPane.showConfirmDialog(this, p, "Auto Assign Shifts (Round-robin)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        LocalDate start, end;
        try {
            start = LocalDate.parse(startField.getText().trim());
            end = LocalDate.parse(endField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date(s). Use YYYY-MM-DD.", "Date Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this, "End date must be same or after start date.", "Range Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Auto-assign Morning and Night shifts from " + start + " to " + end + "?\n(This will insert rows into database.)",
                "Confirm Auto Assign", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        new Thread(() -> {
            setButtonsEnabled(false);
            try (Connection conn = DBConnection.getConnection()) {
                final String qStaff = "SELECT staff_id FROM staff ORDER BY staff_id";
                final String ins = "INSERT INTO shift_assignment (staff_id, shift_type, duty_date) VALUES (?, ?, ?)";
                final String checkAssigned = "SELECT 1 FROM shift_assignment WHERE staff_id = ? AND duty_date = ?";
                java.util.List<Integer> staffIds = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(qStaff);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) staffIds.add(rs.getInt("staff_id"));
                }
                if (staffIds.isEmpty()) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "No staff available to assign.", "Info", JOptionPane.INFORMATION_MESSAGE));
                    return;
                }
                int index = 0;
                int created = 0;
                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    boolean morningDone = false;
                    for (int attempts = 0; attempts < staffIds.size() && !morningDone; attempts++) {
                        int staffId = staffIds.get(index % staffIds.size());
                        index++;
                        try (PreparedStatement psChk = conn.prepareStatement(checkAssigned)) {
                            psChk.setInt(1, staffId);
                            psChk.setDate(2, Date.valueOf(date));
                            try (ResultSet rsChk = psChk.executeQuery()) {
                                if (rsChk.next()) continue;
                            }
                        }
                        try (PreparedStatement psIns = conn.prepareStatement(ins)) {
                            psIns.setInt(1, staffId);
                            psIns.setString(2, "Morning");
                            psIns.setDate(3, Date.valueOf(date));
                            psIns.executeUpdate();
                            created++;
                            morningDone = true;
                        } catch (SQLException ex) {
                            System.err.println("Insert morning failed for staff " + staffId + " on " + date + ": " + ex.getMessage());
                        }
                    }
                    boolean nightDone = false;
                    for (int attempts = 0; attempts < staffIds.size() && !nightDone; attempts++) {
                        int staffId = staffIds.get(index % staffIds.size());
                        index++;
                        try (PreparedStatement psChk = conn.prepareStatement(checkAssigned)) {
                            psChk.setInt(1, staffId);
                            psChk.setDate(2, Date.valueOf(date));
                            try (ResultSet rsChk = psChk.executeQuery()) {
                                if (rsChk.next()) continue;
                            }
                        }
                        try (PreparedStatement psIns = conn.prepareStatement(ins)) {
                            psIns.setInt(1, staffId);
                            psIns.setString(2, "Night");
                            psIns.setDate(3, Date.valueOf(date));
                            psIns.executeUpdate();
                            created++;
                            nightDone = true;
                        } catch (SQLException ex) {
                            System.err.println("Insert night failed for staff " + staffId + " on " + date + ": " + ex.getMessage());
                        }
                    }
                } 
                final int createdF = created;
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Auto-assign finished. Assignments created: " + createdF, "Done", JOptionPane.INFORMATION_MESSAGE);
                    lblStatus.setText("Last auto-assign created: " + createdF + " entries.");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error during auto-assign:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
            } finally {
                setButtonsEnabled(true);
            }
        }).start();
    }
    private void setButtonsEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            btnStaffMgmt.setEnabled(enabled);
            btnViewAssignments.setEnabled(enabled);
            btnAutoAssign.setEnabled(enabled);
            btnLogout.setEnabled(enabled);
        });
    }
    private void logoutConfirm() {
        int ok = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout/exit?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
    private static boolean showLoginDialog(Frame owner) {
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        p.add(new JLabel("Username:"));
        p.add(userField);
        p.add(new JLabel("Password:"));
        p.add(passField);
        int ok = JOptionPane.showConfirmDialog(owner, p, "Admin Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return false;
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        if (DEMO_USER.equals(user) && DEMO_PASS.equals(pass)) return true;
        JOptionPane.showMessageDialog(owner, "Invalid credentials. Use admin/admin123 for demo.", "Login Failed", JOptionPane.WARNING_MESSAGE);
        return false;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            boolean ok = showLoginDialog(null);
            if (!ok) {
                System.exit(0);
            }
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
}
