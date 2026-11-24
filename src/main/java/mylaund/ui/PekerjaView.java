package mylaund.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import mylaund.model.Pekerja;
import mylaund.model.Pesanan;
import mylaund.model.StatusPesanan;

public class PekerjaView extends DashboardFrame {

    private final Pekerja pekerja;
    private final List<Pesanan> dataPesanan = new ArrayList<>();

    private final DefaultTableModel modelPesanan = Theme.modelTabel(
            "ID Pesanan", "Pelanggan", "Layanan", "Estimasi Selesai", "Status");
    private final JTable tabelPesanan = Theme.tabel(modelPesanan);

    private final JLabel lblInfo = Theme.label(" ", 14, true, Theme.TEXT);
    private final JCheckBox chkSembunyikan = new JCheckBox("Sembunyikan pesanan yang sudah selesai", true);
    private final JComboBox<StatusPesanan> cmbStatus = new JComboBox<>();
    private final JButton btnPerbarui = Theme.tombolUtama("Perbarui Status");

    public PekerjaView(Pekerja pekerja, PesananController controller) {
        super("Dashboard Pekerja", pekerja, controller, 960, 620);
        this.pekerja = pekerja;

        JPanel halaman = Theme.halaman(new BorderLayout(0, 12));

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(lblInfo, BorderLayout.WEST);
        chkSembunyikan.setOpaque(false);
        chkSembunyikan.addActionListener(e -> refresh());
        atas.add(chkSembunyikan, BorderLayout.EAST);
        halaman.add(atas, BorderLayout.NORTH);

        JPanel kartu = Theme.kartu(new BorderLayout());
        kartu.add(Theme.gulir(tabelPesanan), BorderLayout.CENTER);
        Theme.lencana(tabelPesanan, 4);
        Theme.lebarKolom(tabelPesanan, 2, 240);
        tabelPesanan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                usulkanStatusBerikutnya();
            }
        });
        halaman.add(kartu, BorderLayout.CENTER);

        cmbStatus.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean fokus) {
                super.getListCellRendererComponent(list, value, index, selected, fokus);
                if (value instanceof StatusPesanan) {
                    setText(Theme.namaStatus((StatusPesanan) value));
                }
                return this;
            }
        });
        btnPerbarui.addActionListener(e -> perbaruiStatus());

        JPanel kartuAksi = Theme.kartu(new FlowLayout(FlowLayout.LEFT, 12, 0));
        kartuAksi.add(Theme.label("Ubah status pesanan terpilih ke:", 13, true, Theme.TEXT));
        kartuAksi.add(cmbStatus);
        kartuAksi.add(btnPerbarui);
        halaman.add(kartuAksi, BorderLayout.SOUTH);

        tabs.addTab("Antrian Pekerjaan", halaman);
        refresh();
    }

    private Pesanan pesananTerpilih() {
        int baris = tabelPesanan.getSelectedRow();
        if (baris < 0 || baris >= dataPesanan.size()) {
            return null;
        }
        return dataPesanan.get(baris);
    }

    /** Menyiapkan pilihan status ke tahap berikutnya dari status pesanan saat ini. */
    private void usulkanStatusBerikutnya() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            cmbStatus.removeAllItems();
            btnPerbarui.setEnabled(false);
            return;
        }
        StatusPesanan[] semua = StatusPesanan.values();
        cmbStatus.removeAllItems();
        for (int i = pesanan.getStatus().ordinal() + 1; i < semua.length; i++) {
            cmbStatus.addItem(semua[i]);
        }
        btnPerbarui.setEnabled(cmbStatus.getItemCount() > 0);
    }

    private void perbaruiStatus() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            tampilkanInfo("Pilih pesanan yang statusnya ingin diperbarui.");
            return;
        }
        StatusPesanan baru = (StatusPesanan) cmbStatus.getSelectedItem();
        if (baru == null) {
            tampilkanInfo("Pesanan " + pesanan.getIdPesanan() + " sudah selesai dan statusnya tidak bisa diubah lagi.");
            return;
        }
        if (baru == pesanan.getStatus()) {
            tampilkanInfo("Pesanan " + pesanan.getIdPesanan() + " sudah berstatus "
                    + Theme.namaStatus(baru) + ".");
            return;
        }
        try {
            controller.handlePerbaruiStatus(pesanan, baru);
            refresh();
            tampilkanInfo("Status pesanan " + pesanan.getIdPesanan() + " diperbarui menjadi "
                    + Theme.namaStatus(baru) + ".\nPemberitahuan dikirim ke " + pesanan.getPelanggan().getNama() + ".");
        } catch (RuntimeException ex) {
            tampilkanError(ex.getMessage());
        }
    }

    @Override
    protected void refresh() {
        Pesanan dipilih = pesananTerpilih();
        dataPesanan.clear();
        int aktif = 0;
        for (Pesanan p : controller.getSemuaPesanan()) {
            boolean selesai = Theme.selesai(p.getStatus());
            if (!selesai) {
                aktif++;
            }
            if (!chkSembunyikan.isSelected() || !selesai) {
                dataPesanan.add(p);
            }
        }
        modelPesanan.setRowCount(0);
        for (Pesanan p : dataPesanan) {
            modelPesanan.addRow(new Object[] {
                p.getIdPesanan(),
                p.getPelanggan().getNama(),
                Theme.ringkasLayanan(p),
                Theme.tanggal(p.getEstimasiSelesai()),
                p.getStatus()
            });
        }
        Theme.pilihKembali(tabelPesanan, dataPesanan, dipilih);
        lblInfo.setText(aktif == 0 ? "Tidak ada pekerjaan yang sedang berjalan"
                : aktif + " pesanan sedang dikerjakan");
        usulkanStatusBerikutnya();
    }
}
