package parkinglotsystem;

import javax.swing.*;
import parkinglotsystem.admin.AdminService;
import parkinglotsystem.core.*; 
import parkinglotsystem.ui.AdminPanel;
import parkinglotsystem.ui.EntryPanel;
import parkinglotsystem.ui.ExitPanel;
import parkinglotsystem.ui.ReportingPanel;

public class MainFrame extends JFrame {

    public MainFrame(ParkingLot lot, AdminService admin,
                     EntryExitService entry, PaymentService payment) {

        super("Parking Lot Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(900, 600));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Admin Dashboard", new AdminPanel(admin));
   
        tabs.addTab("Vehicle Entry", new EntryPanel(entry));
        
        tabs.addTab("Vehicle Exit & Payment", new ExitPanel(lot, payment));
        tabs.addTab("Reporting", new ReportingPanel(admin));

        setContentPane(tabs);
    }

  public static void main(String[] args) {
        DatabaseHandler.createNewTable();

        //1. Initialize Lot & Pattern
        SpotType[] pattern = new SpotType[]{
            SpotType.REGULAR, SpotType.REGULAR, SpotType.REGULAR,
            SpotType.COMPACT,
            SpotType.HANDICAPPED,
            SpotType.RESERVED
        };
        ParkingLot lot = ParkingLotInitializer.createDefaultLot(
            "University Parking", 5, 3, 10, pattern
        );

        //2. Load Data from Database
        DatabaseHandler.initializeSpots(lot);   
        DatabaseHandler.loadSpotsFromDB(lot);     
        DatabaseHandler.loadActiveTickets(lot);

        //3. Create Services
        PaymentService payment = new PaymentService(); 
        EntryExitService entry = new EntryExitService(lot, payment);
        AdminService admin = new AdminService(lot, payment);

        //4. Launch GUI
        SwingUtilities.invokeLater(() ->
            new MainFrame(lot, admin, entry, payment).setVisible(true)
        );
    }
}
