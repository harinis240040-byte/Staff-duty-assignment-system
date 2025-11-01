package server;
import java.io.*;
import java.net.*;
import java.sql.*;
import database.DBConnection;
public class DutyServer {
    @SuppressWarnings("ConvertToTryWithResources")
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(6000)) {
            System.out.println("✅ Server started. Waiting for client...");
            Socket socket = serverSocket.accept();
            System.out.println("Client connected.");
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String dutyData = input.readLine(); 
            String[] parts = dutyData.split(",");
            String name = parts[0];
            String dept = parts[1];
            String shift = parts[2];
            String date = parts[3];
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO staff(name, department) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, dept);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int staffId = 0;
            if (rs.next()) staffId = rs.getInt(1);
            PreparedStatement ps2 = conn.prepareStatement(
                "INSERT INTO shift_assignment(staff_id, shift_type, duty_date) VALUES(?, ?, ?)");
            ps2.setInt(1, staffId);
            ps2.setString(2, shift);
            ps2.setString(3, date);
            ps2.executeUpdate();
            System.out.println("✅ Duty assigned successfully: " + name + " (" + shift + " shift)");
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}