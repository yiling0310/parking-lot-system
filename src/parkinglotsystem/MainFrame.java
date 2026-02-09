package parkinglotsystem;

import javax.swing.*;
import parkinglotsystem.admin.AdminService;
import parkinglotsystem.core.EntryExitService;
import parkinglotsystem.core.ParkingLot;
import parkinglotsystem.core.PaymentService;
import parkinglotsystem.ui.AdminPanel;
import parkinglotsystem.ui.ExitPanel;

public class MainFrame extends JFrame {

    public MainFrame(ParkingLot lot, AdminService admin,
                     EntryExitService entry, PaymentService payment) {

        super("Parking Lot Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(900, 600));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Admin Dashboard", new AdminPanel(admin));
        tabs.addTab("Vehicle Exit & Payment", new ExitPanel(lot, payment));

        setContentPane(tabs);
    }

    public static void main(String[] args) {

        // Create your services correctly
        ParkingLot lot = ParkingLot.getInstance("University Parking");
        PaymentService payment = new PaymentService(); // no-arg constructor
        EntryExitService entry = new EntryExitService(lot);
        AdminService admin = new AdminService(lot, payment);

        SwingUtilities.invokeLater(() ->
            new MainFrame(lot, admin, entry, payment).setVisible(true)
        );
    }
}
