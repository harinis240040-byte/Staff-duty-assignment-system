package ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class AdminUI extends JFrame {
    private JTextField nameField, deptField, dateField;
    private JComboBox<String> shiftBox;
    private JButton assignButton;

    public AdminUI() {
        setTitle("Hospital Duty Assignment System");
        setSize(400, 300);
        setLayout(new GridLayout(5, 2, 10, 10));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JLabel("Staff Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Department:"));
        deptField = new JTextField();
        add(deptField);

        add(new JLabel("Shift:"));
        shiftBox = new JComboBox<>(new String[]{"Morning", "Night"});
        add(shiftBox);

        add(new JLabel("Duty Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        add(dateField);

        assignButton = new JButton("Assign Duty");
        add(assignButton);

        assignButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Socket socket = new Socket("localhost", 6000);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    String data = nameField.getText() + "," +
                                  deptField.getText() + "," +
                                  shiftBox.getSelectedItem() + "," +
                                  dateField.getText();
                    out.println(data);
                    JOptionPane.showMessageDialog(null, "✅ Duty Assigned Successfully!");
                    socket.close();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "⚠️ Error connecting to server.");
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new AdminUI();
    }
}
