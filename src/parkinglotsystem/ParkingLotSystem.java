package parkinglotsystem;

import javax.swing.*;
import parkinglotsystem.admin.AdminService;
import parkinglotsystem.core.ParkingLot;
import parkinglotsystem.core.ParkingLotInitializer;
import parkinglotsystem.core.SpotType;

// Main entry point of the Parking Lot Management System
// This class initializes the system and starts the GUI
public class ParkingLotSystem {

    // Main method where the program starts
    public static void main(String[] args) {

        // Define parking spot type pattern for initialization
        // This pattern will be repeated when creating spots
        SpotType[] pattern = new SpotType[]{
                SpotType.REGULAR,
                SpotType.REGULAR,
                SpotType.REGULAR,
                SpotType.COMPACT,
                SpotType.HANDICAPPED,
                SpotType.RESERVED
        };

        // Create a default parking lot using initializer
        // Parameters:
        // name, number of floors, rows per floor, spots per row, spot type pattern
        ParkingLot lot = ParkingLotInitializer.createDefaultLot(
                "University Parking",
                5,   // Number of floors
                3,   // Number of rows per floor
                10,  // Number of spots per row
                pattern
        );

        // Create admin service to manage parking data
        AdminService adminService = new AdminService(lot);

        // Start GUI in Event Dispatch Thread (Swing best practice)
        SwingUtilities.invokeLater(() -> {

            // Create main application window
            MainFrame frame = new MainFrame(adminService);

            // Display the window
            frame.setVisible(true);
        });
    }
}
