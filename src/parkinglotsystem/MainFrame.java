package parkinglotsystem;

import javax.swing.*;
import parkinglotsystem.admin.AdminService;
import parkinglotsystem.core.*; // Import ALL core classes (needed for SpotType/Initializer)
import parkinglotsystem.ui.AdminPanel;
import parkinglotsystem.ui.EntryPanel;
import parkinglotsystem.ui.ExitPanel; // Make sure EntryPanel is imported!

public class MainFrame extends JFrame {

    public MainFrame(ParkingLot lot, AdminService admin,
                     EntryExitService entry, PaymentService payment) {

        super("Parking Lot Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(900, 600));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Admin Dashboard", new AdminPanel(admin));
        
        // Add Entry Tab (This was missing in your uploaded file!)
        tabs.addTab("Vehicle Entry", new EntryPanel(entry));
        
        tabs.addTab("Vehicle Exit & Payment", new ExitPanel(lot, payment));

        setContentPane(tabs);
    }

  public static void main(String[] args) {
        DatabaseHandler.createNewTable();

        // 1. Initialize Lot & Pattern
        SpotType[] pattern = new SpotType[]{
            SpotType.REGULAR, SpotType.REGULAR, SpotType.REGULAR,
            SpotType.COMPACT,
            SpotType.HANDICAPPED,
            SpotType.RESERVED
        };
        ParkingLot lot = ParkingLotInitializer.createDefaultLot(
            "University Parking", 5, 3, 10, pattern
        );

        // 2. Load Data from Database (ORDER MATTERS!)
        DatabaseHandler.initializeSpots(lot);     // A. Create/Save default spots to DB
        DatabaseHandler.loadSpotsFromDB(lot);     // B. Load any custom spots from DB
        
        // --- ADD THIS LINE ---
        DatabaseHandler.loadActiveTickets(lot);   // C. Put the cars back in the spots!
        // ---------------------

        // 3. Create Services
        PaymentService payment = new PaymentService(); 
        EntryExitService entry = new EntryExitService(lot);
        AdminService admin = new AdminService(lot, payment);

        // 4. Launch GUI
        SwingUtilities.invokeLater(() ->
            new MainFrame(lot, admin, entry, payment).setVisible(true)
        );
    }
}