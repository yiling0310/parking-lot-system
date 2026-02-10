package parkinglotsystem.ui;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import parkinglotsystem.DatabaseHandler;
import parkinglotsystem.core.*;

public class EntryPanel extends JPanel {

    private final EntryExitService entryService;

    // UI Components
    private final JTextField plateField = new JTextField(15);
    private final JComboBox<VehicleType> typeCombo = new JComboBox<>(VehicleType.values());
    private final JCheckBox reservedOnly = new JCheckBox("Show Reserved Spots Only");
    private final JComboBox<String> spotCombo = new JComboBox<>(); 
    private final JTextArea ticketArea = new JTextArea(6, 30);
    private final JButton parkButton = new JButton("Park & Generate Ticket");
    private final JCheckBox handicappedCardCheck = new JCheckBox("Has Handicapped Card");

    public EntryPanel(EntryExitService entryService) {
        this.entryService = entryService;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Top Panel: Vehicle Details
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Entry"));

        inputPanel.add(new JLabel("License Plate:"));
        inputPanel.add(plateField);

        inputPanel.add(new JLabel("Vehicle Type:"));
        inputPanel.add(typeCombo);

        inputPanel.add(new JLabel("Reserved:"));
        inputPanel.add(reservedOnly);

        inputPanel.add(new JLabel("Handicapped Card:"));
        inputPanel.add(handicappedCardCheck);

        JButton findBtn = new JButton("Find Available Spots");
        findBtn.addActionListener(e -> findSpots());
        inputPanel.add(new JLabel("Action:"));
        inputPanel.add(findBtn);

        // 2. Center Panel: Select Spot
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Select Parking Spot"));
        selectionPanel.add(new JLabel("Available Spots:"));
        selectionPanel.add(spotCombo);
        
        parkButton.setEnabled(false); 
        parkButton.addActionListener(e -> parkVehicle());
        selectionPanel.add(parkButton);

        // 3. Bottom Panel: Ticket Output
        JPanel ticketPanel = new JPanel(new BorderLayout());
        ticketPanel.setBorder(BorderFactory.createTitledBorder("Parking Ticket"));
        ticketArea.setEditable(false);
        ticketArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        ticketPanel.add(new JScrollPane(ticketArea), BorderLayout.CENTER);

        // Assemble
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(inputPanel, BorderLayout.NORTH);
        topContainer.add(selectionPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(ticketPanel, BorderLayout.CENTER);
    }

    private void findSpots() {
        try {
            String plate = plateField.getText().trim();
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a license plate.");
                return;
            }

            boolean hasCard = handicappedCardCheck.isSelected();
            VehicleType type = (VehicleType) typeCombo.getSelectedItem();
            Vehicle tempVehicle;

            switch (type) {
                case CAR -> tempVehicle = new Car(plate, false);
                case MOTORCYCLE -> tempVehicle = new Motorcycle(plate, false);
                case SUV_TRUCK -> tempVehicle = new Suv(plate, false);
                case HANDICAPPED -> tempVehicle = new HandicappedVehicle(plate, hasCard);                
                default -> throw new IllegalStateException("Unknown type");
            }

            // Get suitable spots from service [cite: 71]
            List<ParkingSpot> spots = entryService.findAvailableSpotsFor(tempVehicle);
            
            spotCombo.removeAllItems();
            boolean isReservedSelected = reservedOnly.isSelected();
            int displayedCount = 0;

            for (ParkingSpot s : spots) {
                String typeLabel = " [" + s.getSpotType().toString() + "]";

                String displayText = s.getSpotId() + typeLabel;
                
                if (isReservedSelected) {
                    // Filter: Only show Reserved spots
                    if (s.getSpotType() == SpotType.RESERVED) {
                        spotCombo.addItem(displayText);
                        displayedCount++;
                    }
                } else {
                    // Filter: Show everything EXCEPT Reserved spots
                    if (s.getSpotType() != SpotType.RESERVED) {
                        spotCombo.addItem(displayText);
                        displayedCount++;
                    }
                }
            }

            if (displayedCount == 0) {
                String msg = isReservedSelected ? "No Reserved spots available." : "No standard spots available.";
                JOptionPane.showMessageDialog(this, msg);
                parkButton.setEnabled(false);
            } else {
                parkButton.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Found " + displayedCount + " matching spots!");
            }    
        } 
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void parkVehicle() {
        try {
            String plate = plateField.getText().trim();
            VehicleType type = (VehicleType) typeCombo.getSelectedItem();
            String selectedText = (String) spotCombo.getSelectedItem();
            boolean hasCard = handicappedCardCheck.isSelected();

            if (selectedText == null) return;

            String spotId = selectedText.split(" ")[0];

            Vehicle vehicle;
            switch (type) {
                case CAR -> vehicle = new Car(plate, false);
                case MOTORCYCLE -> vehicle = new Motorcycle(plate, false);
                case SUV_TRUCK -> vehicle = new Suv(plate, false);
                case HANDICAPPED -> vehicle = new HandicappedVehicle(plate, hasCard);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }

            boolean isReservedSpot = selectedText.contains("[RESERVED]");
            if (type == VehicleType.HANDICAPPED && isReservedSpot) {
                JOptionPane.showMessageDialog(this, 
                    "Error: Handicapped vehicles are NOT allowed to park in Reserved spots!", 
                    "Access Denied", 
                    JOptionPane.ERROR_MESSAGE);
                return; 
            }

            Ticket ticket = entryService.parkVehicle(spotId, vehicle);

            DateTimeFormatter entryTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = ticket.getEntryTime().format(entryTimeFormat);

            // Display Ticket output [cite: 75, 76]
            ticketArea.setText("********************************\n");
            ticketArea.append("       PARKING TICKET       \n");
            ticketArea.append("********************************\n");
            ticketArea.append("Ticket ID : " + ticket.getTicketId() + "\n");
            ticketArea.append("Plate No  : " + ticket.getLicensePlate() + "\n");
            ticketArea.append("Vehicle   : " + vehicle.getVehicleType() + "\n");
            ticketArea.append("Spot ID   : " + ticket.getSpotId() + "\n");
            ticketArea.append("Entry Time: " + formattedTime + "\n");
            ticketArea.append("********************************\n");
            ticketArea.append("  PLEASE KEEP TICKET SAFE  \n");

            // Reset UI for next entry
            plateField.setText("");
            spotCombo.removeAllItems();
            parkButton.setEnabled(false);

            syncToDatabase(ticket, vehicle, spotId);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Parking Failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void syncToDatabase(Ticket ticket, Vehicle vehicle, String spotId) {
        try (Connection conn = DatabaseHandler.connect()) {
            if (conn == null) return;
            
            // Start Transaction
            conn.setAutoCommit(false);

            try {
                // Update Spot Status
                String updateSpot = "UPDATE parking_spots SET status = 'Occupied' WHERE spot_id = ?";
                try (PreparedStatement ps1 = conn.prepareStatement(updateSpot)) {
                    ps1.setString(1, spotId);
                    ps1.executeUpdate();
                }

                // Insert Ticket Record
                String insertTicket = "INSERT INTO parking_tickets (ticket_id, plate_number, spot_id, status) VALUES (?, ?, ?, 'Active')";
                try (PreparedStatement ps2 = conn.prepareStatement(insertTicket)) {
                    ps2.setString(1, ticket.getTicketId());
                    ps2.setString(2, vehicle.getLicensePlate());
                    ps2.setString(3, spotId);
                    ps2.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Database Sync Error: " + e.getMessage());
        }
    }
}
