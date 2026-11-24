package mylaund.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import mylaund.model.ITransaksi;
import mylaund.model.Kasir;
import mylaund.model.LayananLaundry;
import mylaund.model.Pembayaran;
import mylaund.model.Pesanan;

public class KasirView extends DashboardFrame {

    private final Kasir kasir;
    private final List<Pesanan> dataPesanan = new ArrayList<>();

    private final DefaultTableModel modelPesanan = Theme.modelTabel(
            "ID Pesanan", "Pelanggan", "Tanggal Masuk", "Layanan", "Total", "Pembayaran", "Kembalian", "Status");
    private final DefaultTableModel modelLayanan = Theme.modelTabel("Layanan", "Jenis", "Tarif", "Durasi", "Keterangan");
    private final DefaultTableModel modelRiwayat = Theme.modelTabel("Tanggal", "Keterangan", "Dibayar", "Kembalian");

    private final JTable tabelPesanan = Theme.tabel(modelPesanan);
    private final JTable tabelLayanan = Theme.tabel(modelLayanan);
    private final JTable tabelRiwayat = Theme.tabel(modelRiwayat);

    private final JLabel lblInfo = Theme.label(" ", 14, true, Theme.TEXT);
    private final JCheckBox chkBelumBayar = new JCheckBox("Hanya yang belum dibayar");
    private final JButton btnBayar = Theme.tombolUtama("Catat Pembayaran");

    public KasirView(Kasir kasir, PesananController controller) {
        super("Dashboard Kasir", kasir, controller, 1000, 660);
        this.kasir = kasir;

        tabs.addTab("Pesanan & Pembayaran", buatTabPesanan());
        tabs.addTab("Daftar Layanan", buatTabTabel("Layanan yang tersedia", tabelLayanan));
        tabs.addTab("Riwayat Transaksi", buatTabTabel("Pembayaran yang sudah tercatat", tabelRiwayat));

        refresh();
    }

    private JPanel buatTabPesanan() {
        JPanel halaman = Theme.halaman(new BorderLayout(0, 12));

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(lblInfo, BorderLayout.WEST);
        chkBelumBayar.setOpaque(false);
        chkBelumBayar.addActionListener(e -> refresh());
        atas.add(chkBelumBayar, BorderLayout.EAST);
        halaman.add(atas, BorderLayout.NORTH);

        JPanel kartu = Theme.kartu(new BorderLayout());
        kartu.add(Theme.gulir(tabelPesanan), BorderLayout.CENTER);
        Theme.ratakanKanan(tabelPesanan, 4, 6);
        Theme.lencana(tabelPesanan, 5, 7);
        Theme.lebarKolom(tabelPesanan, 3, 220);
        halaman.add(kartu, BorderLayout.CENTER);

        btnBayar.addActionListener(e -> catatPembayaran());
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bawah.setOpaque(false);
        bawah.add(btnBayar);
        halaman.add(bawah, BorderLayout.SOUTH);
        return halaman;
    }

    private JPanel buatTabTabel(String judul, JTable tabel) {
        JPanel halaman = Theme.halaman(new BorderLayout());
        JPanel kartu = Theme.kartu(new BorderLayout(0, 10));
        kartu.add(Theme.label(judul, 15, true, Theme.TEXT), BorderLayout.NORTH);
        kartu.add(Theme.gulir(tabel), BorderLayout.CENTER);
        halaman.add(kartu, BorderLayout.CENTER);
        return halaman;
    }

    private Pesanan pesananTerpilih() {
        int baris = tabelPesanan.getSelectedRow();
        if (baris < 0 || baris >= dataPesanan.size()) {
            return null;
        }
        return dataPesanan.get(baris);
    }

    private void catatPembayaran() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            tampilkanInfo("Pilih pesanan yang akan dibayar terlebih dahulu.");
            return;
        }
        if (controller.sudahDibayar(pesanan)) {
            tampilkanInfo("Pesanan " + pesanan.getIdPesanan() + " sudah lunas.");
            return;
        }
        if (pesanan.getDaftarDetail().isEmpty()) {
            tampilkanError("Pesanan " + pesanan.getIdPesanan() + " belum memiliki item layanan.");
            return;
        }

        JComboBox<String> cmbMetode = new JComboBox<>(new String[] {"TUNAI", "QRIS"});
        JTextField txtJumlah = new JTextField(String.valueOf((long) Math.ceil(pesanan.getTotalBiaya())));
        Theme.gayaField(txtJumlah);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.add(new JLabel("Pesanan"));
        form.add(new JLabel(pesanan.getIdPesanan() + " - " + pesanan.getPelanggan().getNama()));
        form.add(new JLabel("Total tagihan"));
        form.add(Theme.label(Theme.rupiah(pesanan.getTotalBiaya()), 13, true, Theme.PRIMARY_DARK));
        form.add(new JLabel("Metode pembayaran"));
        form.add(cmbMetode);
        form.add(new JLabel("Jumlah dibayar (Rp)"));
        form.add(txtJumlah);
        JLabel lblKembalian = Theme.label("-", 13, true, Theme.SUCCESS);
        form.add(new JLabel("Kembalian"));
        form.add(lblKembalian);
        Runnable hitungKembalian = () -> {
            Double dibayar = bacaJumlah(txtJumlah.getText());
            if (dibayar == null) {
                lblKembalian.setText("-");
            } else if (dibayar < pesanan.getTotalBiaya()) {
                lblKembalian.setText("Uang kurang " + Theme.rupiah(pesanan.getTotalBiaya() - dibayar));
            } else {
                lblKembalian.setText(Theme.rupiah(dibayar - pesanan.getTotalBiaya()));
            }
        };
        txtJumlah.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                hitungKembalian.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                hitungKembalian.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                hitungKembalian.run();
            }
        });
        hitungKembalian.run();

        int pilihan = JOptionPane.showConfirmDialog(this, form, "Catat Pembayaran",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (pilihan != JOptionPane.OK_OPTION) {
            return;
        }

        Double jumlahBaca = bacaJumlah(txtJumlah.getText());
        if (jumlahBaca == null) {
            tampilkanError("Jumlah pembayaran harus berupa angka.");
            return;
        }
        double jumlah = jumlahBaca;

        try {
            Pembayaran pembayaran = controller.handlePembayaran(
                    controller.buatIdPembayaran(), pesanan, jumlah, (String) cmbMetode.getSelectedItem());
            double kembalian = pembayaran.getKembalian();
            String pesan = "Pembayaran " + pembayaran.getIdPembayaran() + " tercatat. Pesanan lunas.";
            if (kembalian > 0) {
                pesan += "\nKembalian: " + Theme.rupiah(kembalian);
            }
            tampilkanInfo(pesan);
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            tampilkanError(ex.getMessage());
        }
    }

    /** Mengubah teks input menjadi angka, atau null bila bukan angka. */
    private static Double bacaJumlah(String teks) {
        try {
            return Double.parseDouble(teks.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    protected void refresh() {
        // pesanan
        Pesanan dipilih = pesananTerpilih();
        dataPesanan.clear();
        int menunggu = 0;
        List<Pembayaran> bayarTampil = new ArrayList<>();
        for (Pesanan p : controller.getSemuaPesanan()) {
            Pembayaran bayar = controller.getPembayaran(p);
            boolean lunas = bayar != null;
            if (!lunas) {
                menunggu++;
            }
            if (!chkBelumBayar.isSelected() || !lunas) {
                dataPesanan.add(p);
                bayarTampil.add(bayar);
            }
        }
        modelPesanan.setRowCount(0);
        for (int i = 0; i < dataPesanan.size(); i++) {
            Pesanan p = dataPesanan.get(i);
            Pembayaran bayar = bayarTampil.get(i);
            modelPesanan.addRow(new Object[] {
                p.getIdPesanan(),
                p.getPelanggan().getNama(),
                Theme.tanggal(p.getTanggalMasuk()),
                Theme.ringkasLayanan(p),
                Theme.rupiah(p.getTotalBiaya()),
                bayar != null ? "Lunas" : "Belum dibayar",
                bayar != null ? Theme.rupiah(bayar.getKembalian()) : "-",
                p.getStatus()
            });
        }
        Theme.pilihKembali(tabelPesanan, dataPesanan, dipilih);
        lblInfo.setText(menunggu == 0 ? "Semua pesanan sudah lunas"
                : menunggu + " pesanan menunggu pembayaran");

        // layanan
        modelLayanan.setRowCount(0);
        for (LayananLaundry l : controller.getDaftarLayanan()) {
            modelLayanan.addRow(new Object[] {
                l.getNamaLayanan(), l.getJenis(),
                Theme.rupiah(l.getTarif()) + " / " + l.getSatuan(),
                l.getDurasiHari() + " hari", l.getDeskripsi()
            });
        }
        Theme.ratakanKanan(tabelLayanan, 2);

        // riwayat
        modelRiwayat.setRowCount(0);
        List<ITransaksi> riwayat = controller.getRiwayatTransaksi();
        for (int i = riwayat.size() - 1; i >= 0; i--) {
            ITransaksi t = riwayat.get(i);
            modelRiwayat.addRow(new Object[] {
                Theme.tanggal(t.getTanggal()), t.getDeskripsiTransaksi(), Theme.rupiah(t.getNominal()),
                t instanceof Pembayaran ? Theme.rupiah(((Pembayaran) t).getKembalian()) : "-"
            });
        }
        Theme.ratakanKanan(tabelRiwayat, 2, 3);
        Theme.lebarKolom(tabelRiwayat, 1, 420);
    }
}
