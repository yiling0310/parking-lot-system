package parkinglotsystem.ui;

import java.awt.*;
import javax.swing.*;
import parkinglotsystem.core.*;

public class ExitPanel extends JPanel {

    private final ParkingLot parkingLot;
    private final PaymentService paymentService;
    
    // UI Components
    private final JTextField plateField = new JTextField(15);
    private final JTextArea billArea = new JTextArea(10, 30);
    private final JComboBox<String> paymentMethodCombo = new JComboBox<>(new String[]{
        "Cash", "Credit Card", "Debit Card", "Touch 'n Go eWallet", "QR Pay"
    });
    private final JButton payButton = new JButton("Pay & Exit");
    
    private Bill currentBill = null; 

    public ExitPanel(ParkingLot parkingLot, PaymentService paymentService) {
        this.parkingLot = parkingLot;
        this.paymentService = paymentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Top Panel: Input
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Exit"));
        topPanel.add(new JLabel("License Plate:"));
        topPanel.add(plateField);
        JButton calcBtn = new JButton("Calculate Bill");
        calcBtn.addActionListener(e -> calculateBill());
        topPanel.add(calcBtn);

        // 2. Center Panel: Bill Display
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Bill Details"));
        billArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        billArea.setEditable(false);
        centerPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);

        // 3. Bottom Panel: Payment Selection & Action
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Add the Payment Method Label and Dropdown here
        bottomPanel.add(new JLabel("Payment Method:"));
        bottomPanel.add(paymentMethodCombo);
        
        payButton.setEnabled(false);
        payButton.setBackground(new Color(0, 150, 0));
        payButton.setForeground(Color.WHITE);
        payButton.addActionListener(e -> processExit());
        bottomPanel.add(payButton);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void calculateBill() {
        String plate = plateField.getText().trim();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a license plate.");
            return;
        }

        // 1. Find the vehicle in RAM
        var spotOpt = parkingLot.findSpotByPlate(plate);
        if (spotOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vehicle not found inside the lot.");
            return;
        }

        ParkingSpot spot = spotOpt.get();
        // Ensure ParkingSpot.java has the getVehicle() method we fixed earlier!
        Vehicle vehicle = spot.getVehicle(); 
        
        // 2. Create temporary ticket wrapper
        Ticket tempTicket = new Ticket(vehicle, spot); 

        try {
            // 3. Generate Bill (This fetches the REAL Ticket ID from DB)
            currentBill = paymentService.generateBill(tempTicket);
            displayBill(currentBill);
            payButton.setEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error calculating bill: " + ex.getMessage());
        }
    }

    private void displayBill(Bill bill) {
        long hours = bill.duration().toHours();
        long minutes = bill.duration().toMinutesPart();

        StringBuilder sb = new StringBuilder();
        sb.append("============= FINAL BILL =============\n");
        sb.append("Ticket ID     : ").append(bill.ticketId()).append("\n");
        sb.append("Plate No      : ").append(bill.plateNumber()).append("\n");
        sb.append("Spot ID       : ").append(bill.spotId()).append("\n");
        sb.append("Duration      : ").append(hours).append("h ").append(minutes).append("m\n");
        sb.append("Hourly Rate   : RM ").append(String.format("%.2f", bill.hourlyRate())).append("\n");
        sb.append("--------------------------------------\n");
        sb.append("Parking Fee   : RM ").append(String.format("%.2f", bill.parkingFee())).append("\n");
        
        if (bill.overstayFine() > 0) {
            sb.append("Overstay Fine : RM ").append(String.format("%.2f", bill.overstayFine())).append(" (>24h)\n");
        }
        if (bill.misuseFine() > 0) {
            sb.append("Misuse Fine   : RM ").append(String.format("%.2f", bill.misuseFine())).append(" (Reserved Spot)\n");
        }
        if (bill.previousFines() > 0) {
            sb.append("Unpaid Fines  : RM ").append(String.format("%.2f", bill.previousFines())).append(" (Prev)\n");
        }
        
        sb.append("--------------------------------------\n");
        sb.append("TOTAL DUE     : RM ").append(String.format("%.2f", bill.totalAmount())).append("\n");
        sb.append("======================================\n");
        
        billArea.setText(sb.toString());
    }

    private void processExit() {
        if (currentBill == null) return;
        
        String method = (String) paymentMethodCombo.getSelectedItem();
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Confirm payment of RM " + String.format("%.2f", currentBill.totalAmount()) + "\nvia " + method + "?", 
            "Payment Confirmation", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // We pass the method string here (even if Service doesn't store it yet, it's good for logging)
                paymentService.processPayment(currentBill, parkingLot);
                
                JOptionPane.showMessageDialog(this, "Payment Successful via " + method + "! Gate Opening...");
                billArea.setText("--- TRANSACTION COMPLETE ---\nPaid via: " + method);
                plateField.setText("");
                payButton.setEnabled(false);
                currentBill = null;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Payment Failed: " + ex.getMessage());
            }
        }
    }
}
