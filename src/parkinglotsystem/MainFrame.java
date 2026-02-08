package parkinglotsystem;

import java.awt.*;
import javax.swing.*;
import parkinglotsystem.admin.AdminService;
import parkinglotsystem.ui.AdminPanel;

// Main application window for the Parking Lot Management System
// This class creates the main frame and holds all system panels
public class MainFrame extends JFrame {

    // Constructor to initialize the main window
    public MainFrame(AdminService adminService) {

        // Set window title
        super("Parking Lot Management System");

        // Close application when window is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set minimum window size
        setMinimumSize(new Dimension(900, 600));

        // Center the window on screen
        setLocationRelativeTo(null);

        // Create tab container for different system modules
        JTabbedPane tabs = new JTabbedPane();

        // Add Admin panel tab
        tabs.addTab("Admin", new AdminPanel(adminService));

        

        // Set the tab panel as main content
        setContentPane(tabs);
    }
}
