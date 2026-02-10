package parkinglotsystem.ui;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import parkinglotsystem.DatabaseHandler;
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
            LocalDateTime entryTime = vehicle.getEntryTime();
            LocalDateTime exitTime = LocalDateTime.now(); 
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            double unpaidFines = getPreviousFinesFromDB(plate);

            Ticket tempTicket = new Ticket(vehicle, currentSpot, vehicle.getEntryTime());

            currentBill = paymentService.calculateBill(tempTicket, currentSpot);

            double grandTotal = currentBill.totalAmount + unpaidFines;
           
            StringBuilder sb = new StringBuilder();
            sb.append("=== PAYMENT SUMMARY ===\n\n");
            sb.append(String.format("Plate: %s (%s)\n", currentBill.plate, vehicle.getClass().getSimpleName()));
            sb.append("-------------------------------\n");
            sb.append(String.format("Entry Time  : %s\n", entryTime.format(fmt)));
            sb.append(String.format("Exit Time   : %s\n", exitTime.format(fmt)));
            sb.append("-------------------------------\n");
            sb.append(String.format("Total Hours: %d hrs\n", currentBill.hours));
            sb.append(String.format("Hourly Rate: RM %.2f\n", currentSpot.getHourlyRate()));
            sb.append("-----------------------\n");
            sb.append(String.format("Parking Fee: RM %.2f\n", currentBill.parkingFee));
            
            if (currentBill.currentFine > 0) {
                sb.append(String.format("Current Fine: RM %.2f\n", currentBill.currentFine));
            }
            if (currentBill.previousFines > 0) {
                sb.append(String.format("Unpaid Fines:  RM %.2f\n", currentBill.previousFines));
            }
            
            sb.append("-----------------------\n");
            sb.append(String.format("TOTAL DUE:   RM %.2f\n", currentBill.totalAmount));
            
            billArea.setText(sb.toString());
            
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
            
            String input = JOptionPane.showInputDialog(this, 
                "Total Amount Due: RM " + String.format("%.2f", currentBill.totalAmount) + 
                "\nEnter Amount Paid:");

            if (input == null) return; // User cancelled
            double amountPaid = Double.parseDouble(input);

            currentBill.amountPaid = amountPaid;
            currentBill.method = method;
            double remaining = Math.max(0, currentBill.totalAmount - amountPaid);
                
            syncPaymentToDB(currentBill.plate, currentSpot.getSpotId(), amountPaid, currentBill.totalAmount);   

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            StringBuilder receipt = new StringBuilder();

            receipt.append("===============================\n");
            receipt.append("       OFFICIAL RECEIPT        \n");
            receipt.append("===============================\n");
            receipt.append(String.format("Plate No    : %s\n", currentBill.plate));
            receipt.append(String.format("Entry Time  : %s\n", currentSpot.getCurrentVehicle().getEntryTime().format(fmt)));
            receipt.append(String.format("Exit Time   : %s\n", LocalDateTime.now().format(fmt)));
            receipt.append(String.format("Duration    : %d hours\n", currentBill.hours));
            receipt.append("-------------------------------\n");
            receipt.append(String.format("Fee Breakdown: %d hrs x RM %.2f\n", currentBill.hours, currentSpot.getHourlyRate()));
            receipt.append(String.format("Parking Fee : RM %.2f\n", currentBill.parkingFee));
            receipt.append(String.format("Fines Due   : RM %.2f\n", currentBill.currentFine + currentBill.previousFines));
            receipt.append("-------------------------------\n");
            receipt.append(String.format("TOTAL DUE   : RM %.2f\n", currentBill.totalAmount));
            receipt.append(String.format("PAY METHOD  : %s\n", currentBill.method));
            receipt.append(String.format("AMOUNT PAID : RM %.2f\n", currentBill.amountPaid));
            receipt.append(String.format("REMAINING   : RM %.2f\n", remaining));
            receipt.append("===============================\n");

            billArea.setText(receipt.toString()); 

            parkingLot.releaseSpotByPlate(currentBill.plate);
            JOptionPane.showMessageDialog(this, "Transaction Complete. Remaining Balance: RM " + String.format("%.2f", remaining));

            payButton.setEnabled(false);
            currentBill = null;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Payment Failed: " + ex.getMessage());
        }
    }

    public double getPreviousFinesFromDB(String plate) {
        double totalFine = 0;
        String sql = "SELECT SUM(amount) FROM parking_fines WHERE plate_number = ? AND status = 'Unpaid'";
        try (Connection conn = DatabaseHandler.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalFine = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return totalFine;
    }

    private void syncPaymentToDB(String plate, String spotId, double paid, double due) {
        try (Connection conn = DatabaseHandler.connect()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement ps1 = conn.prepareStatement("UPDATE parking_spots SET status = 'Available' WHERE spot_id = ?")) {
                ps1.setString(1, spotId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement("UPDATE parking_tickets SET exit_time = CURRENT_TIMESTAMP, status = 'Paid' WHERE plate_number = ? AND status = 'Active'")) {
                ps2.setString(1, plate);
                ps2.executeUpdate();
            }

            if (paid < due) {
                try (PreparedStatement ps3 = conn.prepareStatement("INSERT INTO parking_fines (plate_number, amount, status) VALUES (?, ?, 'Unpaid')")) {
                    ps3.setString(1, plate);
                    ps3.setDouble(2, due - paid);
                    ps3.executeUpdate();
                }
            }
            
            try (PreparedStatement ps4 = conn.prepareStatement("UPDATE parking_fines SET status = 'Paid' WHERE plate_number = ? AND status = 'Unpaid' AND amount <= ?")) {
                ps4.setString(1, plate);
                ps4.setDouble(2, paid); 
                ps4.executeUpdate();
            }

            conn.commit(); 
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}