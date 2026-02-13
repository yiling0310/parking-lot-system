package parkinglotsystem.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import parkinglotsystem.admin.AdminService;

public class ReportingPanel extends JPanel {

    private final AdminService adminService;
    private final JLabel revenueLabel = new JLabel("RM 0.00");
    private final JLabel occupancyLabel = new JLabel("0.0%");

    private final DefaultTableModel parkedModel = new DefaultTableModel(
            new Object[]{"Plate", "Vehicle Type", "Spot ID", "Entry Time"}, 0
    );
    private final DefaultTableModel occupancyModel = new DefaultTableModel(
            new Object[]{"Spot Type", "Total", "Occupied", "Occupancy %"}, 0
    );
    private final DefaultTableModel fineModel = new DefaultTableModel(
            new Object[]{"Plate", "Outstanding Fine (RM)"}, 0
    );

    public ReportingPanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new GridLayout(1, 3, 8, 8));
        top.setBorder(BorderFactory.createTitledBorder("Report Summary"));
        top.add(new JLabel("Revenue:"));
        top.add(revenueLabel);
        top.add(occupancyLabel);

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        JTable parkedTable = new JTable(parkedModel);
        JTable occupancyTable = new JTable(occupancyModel);
        JTable fineTable = new JTable(fineModel);

        JScrollPane parkedScroll = new JScrollPane(parkedTable);
        parkedScroll.setBorder(BorderFactory.createTitledBorder("Vehicles Currently in Lot"));
        JScrollPane occupancyScroll = new JScrollPane(occupancyTable);
        occupancyScroll.setBorder(BorderFactory.createTitledBorder("Occupancy Report"));
        JScrollPane fineScroll = new JScrollPane(fineTable);
        fineScroll.setBorder(BorderFactory.createTitledBorder("Fine Report (Outstanding)"));

        center.add(parkedScroll);
        center.add(occupancyScroll);
        center.add(fineScroll);

        JButton refreshBtn = new JButton("Refresh Reports");
        refreshBtn.addActionListener(e -> refresh());
        JPanel bottom = new JPanel();
        bottom.add(refreshBtn);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    public final void refresh() {
        revenueLabel.setText("Revenue: " + adminService.getTotalRevenueString());
        occupancyLabel.setText("Overall Occupancy: "
                + String.format("%.1f%%", adminService.getOverallOccupancyRate() * 100.0));

        parkedModel.setRowCount(0);
        List<AdminService.ParkedVehicleInfo> parkedVehicles = adminService.getCurrentlyParkedVehicles();
        for (AdminService.ParkedVehicleInfo info : parkedVehicles) {
            parkedModel.addRow(new Object[]{info.plate, info.vehicleType, info.spotId, info.entryTime});
        }

        occupancyModel.setRowCount(0);
        Map<String, AdminService.TypeStat> occupancy = adminService.getOccupancyBySpotType();
        for (AdminService.TypeStat stat : occupancy.values()) {
            occupancyModel.addRow(new Object[]{
                    stat.typeName,
                    stat.total,
                    stat.occupied,
                    String.format("%.1f%%", stat.getRate() * 100.0)
            });
        }

        fineModel.setRowCount(0);
        List<AdminService.FineInfo> fines = adminService.getOutstandingFinesByPlate();
        for (AdminService.FineInfo fine : fines) {
            fineModel.addRow(new Object[]{fine.plate, String.format("%.2f", fine.unpaidAmount)});
        }
    }
}
