package parkinglotsystem.ui;

import java.awt.*;
import javax.swing.*;
import parkinglotsystem.core.*;
import parkinglotsystem.core.PaymentService.Bill;

public class ExitPanel extends JPanel {

    private final ParkingLot parkingLot;
    private final PaymentService paymentService;

    // UI Components
    private final JTextField plateField = new JTextField(10);
    private final JTextArea billArea = new JTextArea(8, 30);
    private final JButton payButton = new JButton("Pay & Exit Vehicle");
    private final JLabel statusLabel = new JLabel("Enter license plate to calculate bill.");

    // NEW: Payment Method Selector
    private final JComboBox<String> paymentMethodCombo = new JComboBox<>(new String[]{
        "Cash Payment", 
        "Credit/Debit Card", 
        "E-Wallet / QR"
    });

    // Store current calculation to prevent paying without calculating
    private Bill currentBill = null;
    private ParkingSpot currentSpot = null;

    public ExitPanel(ParkingLot parkingLot, PaymentService paymentService) {
        this.parkingLot = parkingLot;
        this.paymentService = paymentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Top Panel: Search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Exit"));
        
        topPanel.add(new JLabel("License Plate:"));
        topPanel.add(plateField);
        
        JButton calcBtn = new JButton("Calculate Bill");
        calcBtn.addActionListener(e -> calculateBill());
        topPanel.add(calcBtn);

        // 2. Center Panel: Bill Display
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Payment Details"));
        
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        centerPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);

        // 3. Bottom Panel: Action & Payment Method
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        // Payment Method Section
        JPanel payMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        payMethodPanel.add(new JLabel("Payment Method:"));
        payMethodPanel.add(paymentMethodCombo);
        
        payButton.setEnabled(false); // Disabled until bill is calculated
        payButton.setBackground(new Color(200, 255, 200)); // Light green
        payButton.addActionListener(e -> processPayment());
        
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(payMethodPanel, BorderLayout.CENTER); // Add dropdown
        bottomPanel.add(payButton, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void calculateBill() {
        try {
            String plate = plateField.getText().trim();
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a license plate.");
                return;
            }

            // 1. Find Vehicle in the Lot
            var spotOpt = parkingLot.findSpotByPlate(plate);
            
            if (spotOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vehicle not found in the parking lot!");
                return;
            }

            currentSpot = spotOpt.get();
            Vehicle vehicle = currentSpot.getCurrentVehicle();
            
            // 2. Create a temporary 'Ticket' wrapper
            Ticket tempTicket = new Ticket(vehicle, currentSpot, vehicle.getEntryTime());

            // 3. Calculate Bill
            currentBill = paymentService.calculateBill(tempTicket, currentSpot);

            // 4. Display Bill
            StringBuilder sb = new StringBuilder();
            sb.append("=== PARKING RECEIPT ===\n");
            sb.append(String.format("Plate:       %s\n", currentBill.plate));
            sb.append(String.format("Total Hours: %d hrs\n", currentBill.hours));
            sb.append(String.format("Hourly Rate: RM %.2f\n", currentSpot.getHourlyRate()));
            sb.append("-----------------------\n");
            sb.append(String.format("Parking Fee: RM %.2f\n", currentBill.parkingFee));
            
            if (currentBill.currentFine > 0) {
                sb.append(String.format("Overstay Fine: RM %.2f\n", currentBill.currentFine));
            }
            if (currentBill.previousFines > 0) {
                sb.append(String.format("Unpaid Fines:  RM %.2f\n", currentBill.previousFines));
            }
            
            sb.append("-----------------------\n");
            sb.append(String.format("TOTAL DUE:   RM %.2f\n", currentBill.totalAmount));
            
            billArea.setText(sb.toString());
            
            // Enable Pay Button
            payButton.setEnabled(true);
            statusLabel.setText("Please collect payment of RM " + String.format("%.2f", currentBill.totalAmount));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void processPayment() {
        if (currentBill == null || currentSpot == null) return;

        try {
            String method = (String) paymentMethodCombo.getSelectedItem();
            
            // 1. Confirm Payment
            int choice = JOptionPane.showConfirmDialog(this, 
                "Confirm payment of RM " + String.format("%.2f", currentBill.totalAmount) + "\nvia " + method + "?",
                "Payment Processing",
                JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                
                // 2. Process logic
                paymentService.processPayment(currentBill.plate, currentBill.totalAmount);
                
                // 3. Release the spot
                parkingLot.releaseSpotByPlate(currentBill.plate);

                // 4. Success Message
                JOptionPane.showMessageDialog(this, "Payment Successful via " + method + "!\nGate Opening...\nHave a nice day.");

                // 5. Reset UI
                plateField.setText("");
                billArea.setText("");
                payButton.setEnabled(false);
                statusLabel.setText("Enter license plate to calculate bill.");
                currentBill = null;
                currentSpot = null;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Payment Failed: " + ex.getMessage());
        }
    }
}