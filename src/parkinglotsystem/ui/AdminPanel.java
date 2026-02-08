package parkinglotsystem.ui;

import parkinglotsystem.admin.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {

    private final AdminService adminService;

    private final JLabel summaryLabel = new JLabel("-");
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

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        refresh();
    }

    private JComponent buildTop() {
        JPanel top = new JPanel(new BorderLayout(10, 10));

        JPanel summaryBox = new JPanel(new BorderLayout(8, 8));
        summaryBox.setBorder(BorderFactory.createTitledBorder("Parking Lot Summary"));
        summaryBox.add(summaryLabel, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(refreshBtn);

        top.add(summaryBox, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);
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
        summaryLabel.setText(adminService.getSummary());

        // Floor occupancy
        List<String> lines = adminService.getFloorOccupancyLines();
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append(line).append("\n");
        floorArea.setText(sb.toString());

        // Spot table
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
}
