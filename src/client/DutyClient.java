package client;
import java.io.*;
import java.net.*;

public class DutyClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 6000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Example Data
            String dutyInfo = "Dr.Asha,Cardiology,Night,2025-10-29";
            out.println(dutyInfo);
            System.out.println("📤 Duty info sent to server.");

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
