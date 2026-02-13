package parkinglotsystem.ui;

import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import parkinglotsystem.admin.AdminService;

public class ReportDialog extends JDialog {

    public ReportDialog(Frame parent, AdminService adminService) {
        super(parent, "System Performance Report", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // 1. Header: Financial Summary
        JPanel financePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        financePanel.setBorder(BorderFactory.createTitledBorder("Financial Overview"));
        
        JLabel revLabel = new JLabel("Total Revenue:");
        JLabel revValue = new JLabel(adminService.getTotalRevenueString());
        revValue.setForeground(new Color(0, 100, 0));
        revValue.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel fineLabel = new JLabel("Unpaid Fines:");
        JLabel fineValue = new JLabel(adminService.getTotalUnpaidFinesString());
        fineValue.setForeground(Color.RED);
        fineValue.setFont(new Font("Arial", Font.BOLD, 14));

        financePanel.add(revLabel);
        financePanel.add(revValue);
        financePanel.add(fineLabel);
        financePanel.add(fineValue);

        // 2. Center: Occupancy by Spot Type Table
        String[] columns = {"Spot Type", "Total Spots", "Occupied", "Occupancy %"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        
        // Fetch data from our new Service method
        Map<String, AdminService.TypeStat> stats = adminService.getOccupancyBySpotType();
        
        int grandTotal = 0;
        int grandOccupied = 0;

        for (AdminService.TypeStat stat : stats.values()) {
            grandTotal += stat.total;
            grandOccupied += stat.occupied;
            
            model.addRow(new Object[]{
                stat.typeName,
                stat.total,
                stat.occupied,
                String.format("%.1f%%", stat.getRate() * 100)
            });
        }

        // Add a Total Row at the bottom
        double overallRate = (grandTotal == 0) ? 0.0 : (double) grandOccupied / grandTotal;
        model.addRow(new Object[]{
            "TOTAL", 
            grandTotal, 
            grandOccupied, 
            String.format("%.1f%%", overallRate * 100)
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Occupancy by Spot Type"));

        // 3. Close Button
        JButton closeBtn = new JButton("Close Report");
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);

        // Assemble
        add(financePanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
}