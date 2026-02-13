package parkinglotsystem.ui;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
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

            // FIX: Pass 'hasCard' to ALL constructors, not just HandicappedVehicle
            switch (type) {
                case CAR -> tempVehicle = new Car(plate, hasCard);
                case MOTORCYCLE -> tempVehicle = new Motorcycle(plate, hasCard);
                case SUV_TRUCK -> tempVehicle = new Suv(plate, hasCard);
                case HANDICAPPED -> tempVehicle = new HandicappedVehicle(plate, hasCard);                
                default -> throw new IllegalStateException("Unknown type");
            }

            List<ParkingSpot> spots = entryService.findAvailableSpotsFor(tempVehicle);
            
            spotCombo.removeAllItems();
            boolean isReservedSelected = reservedOnly.isSelected();
            int displayedCount = 0;

            for (ParkingSpot s : spots) {
                String typeLabel = " [" + s.getSpotType().toString() + "]";
                String displayText = s.getSpotId() + typeLabel;
                
                if (isReservedSelected) {
                    if (s.getSpotType() == SpotType.RESERVED) {
                        spotCombo.addItem(displayText);
                        displayedCount++;
                    }
                } else {
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
            // FIX: Pass 'hasCard' to ALL constructors here as well
            switch (type) {
                case CAR -> vehicle = new Car(plate, hasCard);
                case MOTORCYCLE -> vehicle = new Motorcycle(plate, hasCard);
                case SUV_TRUCK -> vehicle = new Suv(plate, hasCard);
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

            // ATTEMPT TO PARK with specific error handling
            try {
                // This call now handles both the internal logic and Database saving
                Ticket ticket = entryService.parkVehicle(spotId, vehicle);

                // If successful, display the Ticket details
                DateTimeFormatter entryTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = ticket.getEntryTime().format(entryTimeFormat);

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

                JOptionPane.showMessageDialog(this, "Vehicle successfully parked in spot: " + spotId);

                // Reset UI for next entry
                plateField.setText("");
                spotCombo.removeAllItems();
                parkButton.setEnabled(false);

            } catch (IllegalArgumentException ex) {
                // Catches "Vehicle already parked!" or spot mismatch errors
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Entry Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                // Catch any other unexpected system errors
                JOptionPane.showMessageDialog(this, "System Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Application Error: " + ex.getMessage());
        }
    }
}