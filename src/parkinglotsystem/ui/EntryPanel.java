package parkinglotsystem.ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import parkinglotsystem.core.*;

public class EntryPanel extends JPanel {

    private final EntryExitService entryService;

    // UI Components
    private final JTextField plateField = new JTextField(10);
    private final JComboBox<VehicleType> typeCombo = new JComboBox<>(VehicleType.values());
    private final JCheckBox handiCheck = new JCheckBox("Handicapped Holder?");
    
    private final JComboBox<String> spotCombo = new JComboBox<>(); // Populated after search
    private final JTextArea ticketArea = new JTextArea(6, 30);
    private final JButton parkButton = new JButton("Park & Generate Ticket");

    public EntryPanel(EntryExitService entryService) {
        this.entryService = entryService;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Top Panel: Vehicle Details
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Entry"));

        inputPanel.add(new JLabel("License Plate:"));
        inputPanel.add(plateField);

        inputPanel.add(new JLabel("Vehicle Type:"));
        inputPanel.add(typeCombo);

        inputPanel.add(new JLabel("")); // Spacer
        inputPanel.add(handiCheck);

        JButton findBtn = new JButton("Find Available Spots");
        findBtn.addActionListener(e -> findSpots());
        inputPanel.add(new JLabel("Action:"));
        inputPanel.add(findBtn);

        // 2. Center Panel: Select Spot
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Select Parking Spot"));
        selectionPanel.add(new JLabel("Available Spots:"));
        selectionPanel.add(spotCombo);
        
        parkButton.setEnabled(false); // Disabled until spots are found
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
            
            // Factory logic to create correct subclass based on selection
            VehicleType type = (VehicleType) typeCombo.getSelectedItem();
            boolean isHandi = handiCheck.isSelected();
            Vehicle tempVehicle;

            switch (type) {
                case CAR -> tempVehicle = new Car(plate, isHandi);
                case MOTORCYCLE -> tempVehicle = new Motorcycle(plate, isHandi);
                case SUV_TRUCK -> tempVehicle = new Suv(plate, isHandi);
                case HANDICAPPED -> tempVehicle = new HandicappedVehicle(plate);
                default -> throw new IllegalStateException("Unknown type");
            }

            // Ask Service for spots
            List<ParkingSpot> spots = entryService.findAvailableSpotsFor(tempVehicle);
            
            // Update UI
            spotCombo.removeAllItems();
            if (spots.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No suitable spots available for this vehicle type.");
                parkButton.setEnabled(false);
            } else {
                for (ParkingSpot s : spots) {
                    spotCombo.addItem(s.getSpotId());
                }
                parkButton.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Found " + spots.size() + " available spots!");
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void parkVehicle() {
        try {
            String plate = plateField.getText();
            VehicleType type = (VehicleType) typeCombo.getSelectedItem();
            boolean isHandi = handiCheck.isSelected();
            String spotId = (String) spotCombo.getSelectedItem();

            if (spotId == null) return;

            // 1. Create the REAL vehicle object
            Vehicle vehicle;
            switch (type) {
                case CAR -> vehicle = new Car(plate, isHandi);
                case MOTORCYCLE -> vehicle = new Motorcycle(plate, isHandi);
                case SUV_TRUCK -> vehicle = new Suv(plate, isHandi);
                case HANDICAPPED -> vehicle = new HandicappedVehicle(plate);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }

            // 2. Call Service to park
            Ticket ticket = entryService.parkVehicle(spotId, vehicle);

            // 3. Display Ticket
            ticketArea.setText("********************************\n");
            ticketArea.append("       PARKING TICKET       \n");
            ticketArea.append("********************************\n");
            ticketArea.append("Ticket ID : " + ticket.getTicketId() + "\n");
            ticketArea.append("Plate No  : " + ticket.getLicensePlate() + "\n");
            ticketArea.append("Spot ID   : " + ticket.getSpotId() + "\n");
            ticketArea.append("Entry Time: " + ticket.getEntryTime() + "\n");
            ticketArea.append("********************************\n");
            ticketArea.append("  PLEASE KEEP TICKET SAFE  \n");

            // 4. Reset UI
            plateField.setText("");
            spotCombo.removeAllItems();
            parkButton.setEnabled(false);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Parking Failed: " + ex.getMessage());
        }
    }
}