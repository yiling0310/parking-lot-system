package parkinglotsystem.ui;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import parkinglotsystem.core.*;

public class ExitPanel extends JPanel {

    private final ParkingLot parkingLot;
    private final PaymentService paymentService;
 
    private final JTextField plateField = new JTextField(15);
    private final JTextArea billArea = new JTextArea(10, 30);
    private final JComboBox<String> paymentMethodCombo = new JComboBox<>(new String[]{
        "Cash", "Card"
    });
    private final JCheckBox includePreviousFinesCheck = new JCheckBox("Pay all fines now (current + previous)", true);
    private final JTextField amountPaidField = new JTextField("0.00", 10);
    private final JButton payButton = new JButton("Pay & Exit");
    
    private Bill currentBill = null; 

    public ExitPanel(ParkingLot parkingLot, PaymentService paymentService) {
        this.parkingLot = parkingLot;
        this.paymentService = paymentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        //1. Top Panel: Input
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Exit"));
        topPanel.add(new JLabel("License Plate:"));
        topPanel.add(plateField);
        JButton calcBtn = new JButton("Calculate Bill");
        calcBtn.addActionListener(e -> calculateBill());
        topPanel.add(calcBtn);

        //2. Center Panel: Bill Display
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Bill Details"));
        billArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        billArea.setEditable(false);
        centerPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);

        //3. Bottom Panel: Payment Selection & Action
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        bottomPanel.add(new JLabel("Payment Method:"));
        bottomPanel.add(paymentMethodCombo);
        bottomPanel.add(includePreviousFinesCheck);
        bottomPanel.add(new JLabel("Amount Paid (RM):"));
        bottomPanel.add(amountPaidField);
        
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

        //1. Find the vehicle in RAM
        var spotOpt = parkingLot.findSpotByPlate(plate);
        if (spotOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vehicle not found inside the lot.");
            return;
        }

        ParkingSpot spot = spotOpt.get();
        Vehicle vehicle = spot.getVehicle(); 
        
        //2. Create temporary ticket wrapper
        Ticket tempTicket = new Ticket(vehicle, spot); 

        try {
            //3. Generate Bill (This fetches the REAL Ticket ID from DB)
            currentBill = paymentService.generateBill(tempTicket);
            displayBill(currentBill);
            payButton.setEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error calculating bill: " + ex.getMessage());
        }
    }

    private void displayBill(Bill bill) {
        long hours = bill.billedHours();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append("============= FINAL BILL =============\n");
        sb.append("Ticket ID     : ").append(bill.ticketId()).append("\n");
        sb.append("Plate No      : ").append(bill.plateNumber()).append("\n");
        sb.append("Spot ID       : ").append(bill.spotId()).append("\n");
        sb.append("Entry Time    : ").append(bill.entryTime().format(dtf)).append("\n");
        sb.append("Exit Time     : ").append(bill.exitTime().format(dtf)).append("\n");
        sb.append("Duration      : ").append(hours).append(" hour(s)\n");
        sb.append("Fine Scheme   : ").append(bill.fineScheme()).append("\n");
        sb.append("Hourly Rate   : RM ").append(String.format("%.2f", bill.hourlyRate())).append("\n");
        sb.append("Fee Formula   : ").append(hours).append(" x RM ")
          .append(String.format("%.2f", bill.hourlyRate())).append("\n");
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
        sb.append("Mandatory Due : RM ").append(String.format("%.2f", bill.mandatoryAmount())).append("\n");
        sb.append("TOTAL DUE     : RM ").append(String.format("%.2f", bill.totalAmount())).append("\n");
        sb.append("======================================\n");
        
        billArea.setText(sb.toString());
        amountPaidField.setText(String.format("%.2f", bill.totalAmount()));
    }

    private void processExit() {
        if (currentBill == null) return;
        
        String method = (String) paymentMethodCombo.getSelectedItem();
        boolean includePreviousFines = includePreviousFinesCheck.isSelected();
        double amountPaid;
        try {
            amountPaid = Double.parseDouble(amountPaidField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount paid.");
            return;
        }
        if (amountPaid < 0) {
            JOptionPane.showMessageDialog(this, "Amount paid cannot be negative.");
            return;
        }

        double totalFines = currentBill.overstayFine() + currentBill.misuseFine() + currentBill.previousFines();
        double selectedDue = currentBill.mandatoryAmount() + (includePreviousFines ? totalFines : 0.0);
        double remainingBalance = Math.max(0.0, selectedDue - amountPaid);
        double change = Math.max(0.0, amountPaid - selectedDue);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Confirm payment:\n"
                + "Amount Due: RM " + String.format("%.2f", selectedDue) + "\n"
                + "Amount Paid: RM " + String.format("%.2f", amountPaid) + "\n"
                + "Remaining Balance: RM " + String.format("%.2f", remainingBalance) + "\n"
                + "Change: RM " + String.format("%.2f", change) + "\n"
                + "via " + method + "?", 
            "Payment Confirmation", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                paymentService.processPayment(currentBill, parkingLot, method, amountPaid, includePreviousFines);
                double allFines = currentBill.overstayFine() + currentBill.misuseFine() + currentBill.previousFines();
                double appliedToFines = includePreviousFines
                        ? Math.min(Math.max(0.0, amountPaid - currentBill.mandatoryAmount()), allFines)
                        : 0.0;
                double outstandingFines = Math.max(0.0, allFines - appliedToFines);
                
                JOptionPane.showMessageDialog(this, "Payment Successful via " + method + "! Gate Opening...");
                StringBuilder receipt = new StringBuilder();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                receipt.append("=========== EXIT RECEIPT ===========\n");
                receipt.append("Ticket ID       : ").append(currentBill.ticketId()).append("\n");
                receipt.append("Plate No        : ").append(currentBill.plateNumber()).append("\n");
                receipt.append("Entry Time      : ").append(currentBill.entryTime().format(dtf)).append("\n");
                receipt.append("Exit Time       : ").append(currentBill.exitTime().format(dtf)).append("\n");
                receipt.append("Duration        : ").append(currentBill.billedHours()).append(" hour(s)\n");
                receipt.append("Breakdown       : ").append(currentBill.billedHours())
                        .append(" x RM ").append(String.format("%.2f", currentBill.hourlyRate())).append("\n");
                receipt.append("Parking Fee     : RM ").append(String.format("%.2f", currentBill.parkingFee())).append("\n");
                receipt.append("Current Fines   : RM ").append(String.format("%.2f", currentBill.overstayFine() + currentBill.misuseFine())).append("\n");
                receipt.append("Previous Fines  : RM ").append(String.format("%.2f", currentBill.previousFines())).append("\n");
                receipt.append("Payment Method  : ").append(method).append("\n");
                receipt.append("Total Amount Paid: RM ").append(String.format("%.2f", amountPaid)).append("\n");
                receipt.append("Remaining Balance: RM ").append(String.format("%.2f", outstandingFines)).append("\n");
                receipt.append("====================================\n");
                billArea.setText(receipt.toString());
                plateField.setText("");
                payButton.setEnabled(false);
                currentBill = null;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Payment Failed: " + ex.getMessage());
            }
        }
    }
}
