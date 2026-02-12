package parkinglotsystem.ui;

import java.awt.*;
import java.util.List; // Import FineType
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import parkinglotsystem.admin.AdminService;
import parkinglotsystem.core.FineType;
import parkinglotsystem.core.SpotType;

public class AdminPanel extends JPanel {

    private final AdminService adminService;

    // UI Components
    private final JLabel summaryLabel = new JLabel("-");
    private final JLabel revenueLabel = new JLabel("RM 0.00"); // NEW: Revenue Display
    private final JLabel finesLabel = new JLabel("Unpaid Fines: RM 0.00");
    private final JComboBox<FineType> fineSchemeCombo = new JComboBox<>(FineType.values()); // NEW: Fine Selector
    
    private final JTextArea floorArea = new JTextArea(6, 30);

    private final DefaultTableModel spotModel = new DefaultTableModel(
            new Object[]{"Floor", "Spot ID", "Type", "Status", "Vehicle Plate", "Rate (RM/hr)"},
            0
    );
    private final JTable spotTable = new JTable(spotModel);

    public AdminPanel(AdminService adminService) {
        if (adminService == null) throw new IllegalArgumentException("adminService cannot be null");
        this.adminService = adminService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        floorArea.setEditable(false);
        spotTable.setFillsViewportHeight(true);

        // Initialize Fine Scheme Selection
        fineSchemeCombo.setSelectedItem(adminService.getCurrentFineScheme());
        fineSchemeCombo.addActionListener(e -> {
            FineType selected = (FineType) fineSchemeCombo.getSelectedItem();
            adminService.setFineScheme(selected);
            JOptionPane.showMessageDialog(this, "Fine Scheme updated to: " + selected);
        });

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        refresh();
    }

    private JComponent buildTop() {
        JPanel top = new JPanel(new GridLayout(1, 3, 10, 0));

        // Box 1: General Summary
        JPanel summaryBox = new JPanel(new BorderLayout(8, 8));
        summaryBox.setBorder(BorderFactory.createTitledBorder("Occupancy Summary"));
        summaryBox.add(summaryLabel, BorderLayout.CENTER);

        // Box 2: Financials (UPDATED)
        JPanel financeBox = new JPanel(new GridLayout(2, 1, 5, 5)); // <--- CHANGE to GridLayout
        financeBox.setBorder(BorderFactory.createTitledBorder("Financials")); // Rename title if you want
        
        // Style the Revenue Label
        revenueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        revenueLabel.setForeground(new Color(0, 100, 0)); // Green
        revenueLabel.setBorder(BorderFactory.createTitledBorder("Total Revenue")); // Optional: Add mini-border
        
        // Style the Fines Label
        finesLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        finesLabel.setForeground(Color.RED); // Red for debt
        finesLabel.setBorder(BorderFactory.createTitledBorder("Unpaid Fines")); // Optional: Add mini-border

        financeBox.add(revenueLabel); // <--- ADD revenue
        financeBox.add(finesLabel);   // <--- ADD fines

        // Box 3: Settings & Actions (UPDATED)
        JPanel settingsBox = new JPanel(new GridLayout(3, 1, 5, 5)); // <--- CHANGE rows to 3
        settingsBox.setBorder(BorderFactory.createTitledBorder("Admin Settings"));
        
        JPanel schemePanel = new JPanel(new BorderLayout());
        schemePanel.add(new JLabel("Fine Scheme: "), BorderLayout.WEST);
        schemePanel.add(fineSchemeCombo, BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.addActionListener(e -> refresh());

        // Feature 1 Fix: Add Manage Button
        JButton manageBtn = new JButton("Manage Structure");  // <--- ADD THIS
        manageBtn.addActionListener(e -> showManagementDialog()); // <--- ADD THIS

        settingsBox.add(schemePanel);
        settingsBox.add(manageBtn); // <--- Add to panel
        settingsBox.add(refreshBtn);

        top.add(summaryBox);
        top.add(financeBox);
        top.add(settingsBox);
        
        return top;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new BorderLayout(10, 10));

        JPanel floorBox = new JPanel(new BorderLayout());
        floorBox.setBorder(BorderFactory.createTitledBorder("Occupancy by Floor"));
        floorBox.add(new JScrollPane(floorArea), BorderLayout.CENTER);

        JPanel spotBox = new JPanel(new BorderLayout());
        spotBox.setBorder(BorderFactory.createTitledBorder("All Parking Spots"));
        spotBox.add(new JScrollPane(spotTable), BorderLayout.CENTER);

        center.add(floorBox, BorderLayout.NORTH);
        center.add(spotBox, BorderLayout.CENTER);
        return center;
    }

    public final void refresh() {
        // 1. Update Summary Text
       summaryLabel.setText(adminService.getSummary());

        // 2. Update Revenue 
        revenueLabel.setText(adminService.getTotalRevenueString());

        // 3. Update Fines 
        finesLabel.setText(adminService.getTotalUnpaidFinesString());

        // 3. Floor occupancy
        List<String> lines = adminService.getFloorOccupancyLines();
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append(line).append("\n");
        floorArea.setText(sb.toString());

        // 4. Spot table
        spotModel.setRowCount(0);
        for (AdminService.SpotInfo info : adminService.getAllSpotInfos()) {
            spotModel.addRow(new Object[]{
                    info.floorNo,
                    info.spotId,
                    info.type,
                    info.status,
                    info.plateOrDash,
                    String.format("%.2f", info.hourlyRate)
            });
        }
    }
    private void showManagementDialog() {
        String[] options = {"Add Floor", "Add Spot"};
        int choice = JOptionPane.showOptionDialog(this, "What would you like to add?", 
                "Manage Structure", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);

        try {
            if (choice == 0) { // Add Floor
                String input = JOptionPane.showInputDialog("Enter New Floor Number:");
                if (input != null) {
                    adminService.createNewFloor(Integer.parseInt(input));
                    refresh();
                    JOptionPane.showMessageDialog(this, "Floor added successfully!");
                }
            } else if (choice == 1) { // Add Spot
                String floorStr = JOptionPane.showInputDialog("Enter Floor Number:");
                String spotId = JOptionPane.showInputDialog("Enter Spot ID (e.g., 1-A):");
                
                // Dropdown for Spot Type
                SpotType type = (SpotType) JOptionPane.showInputDialog(this, "Select Type", "Type", 
                        JOptionPane.QUESTION_MESSAGE, null, SpotType.values(), SpotType.REGULAR);
                
                if (floorStr != null && spotId != null && type != null) {
                     // Default rate 5.0, you can change this if needed
                     adminService.createNewSpot(Integer.parseInt(floorStr), spotId, type, 5.0); 
                     refresh();
                     JOptionPane.showMessageDialog(this, "Spot added successfully!");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
}