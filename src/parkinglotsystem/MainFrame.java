package parkinglotsystem;

import java.awt.*;
import javax.swing.*;
import parkinglotsystem.admin.AdminService;
import parkinglotsystem.core.EntryExitService;
import parkinglotsystem.core.PaymentService;
import parkinglotsystem.core.ParkingLot;
import parkinglotsystem.ui.AdminPanel;
import parkinglotsystem.ui.EntryPanel;
import parkinglotsystem.ui.ExitPanel; // Import new panel

public class MainFrame extends JFrame {

    // Updated Constructor with MORE arguments
    public MainFrame(ParkingLot lot, AdminService admin, EntryExitService entry, PaymentService payment) {

        super("Parking Lot Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        // 1. Admin Tab
        tabs.addTab("Admin Dashboard", new AdminPanel(admin));

        // 2. Entry Tab
        tabs.addTab("Vehicle Entry", new EntryPanel(entry));

        // 3. Exit Tab (NEW)
        tabs.addTab("Vehicle Exit & Payment", new ExitPanel(lot, payment));

        setContentPane(tabs);
    }
}