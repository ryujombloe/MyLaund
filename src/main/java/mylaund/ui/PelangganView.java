package mylaund.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import mylaund.model.DetailPesanan;
import mylaund.model.LayananLaundry;
import mylaund.model.Notifikasi;
import mylaund.model.Pelanggan;
import mylaund.model.Pesanan;

public class PelangganView extends DashboardFrame {

    private static final int TAB_BUAT = 0;
    private static final int TAB_PESANAN = 1;
    private static final int TAB_NOTIF = 2;

    /** Satu baris keranjang sebelum pesanan benar-benar dibuat. */
    private static final class ItemKeranjang {
        private final LayananLaundry layanan;
        private double jumlah;

        ItemKeranjang(LayananLaundry layanan, double jumlah) {
            this.layanan = layanan;
            this.jumlah = jumlah;
        }

        double perkiraanHarga() {
            return layanan.hitungHarga(jumlah);
        }
    }

    private final Pelanggan pelanggan;

    private final List<LayananLaundry> dataLayanan = new ArrayList<>();
    private final List<ItemKeranjang> keranjang = new ArrayList<>();
    private final List<Pesanan> dataPesanan = new ArrayList<>();

    private final DefaultTableModel modelLayanan = Theme.modelTabel("Layanan", "Jenis", "Tarif", "Durasi", "Keterangan");
    private final DefaultTableModel modelKeranjang = Theme.modelTabel("Layanan", "Jumlah", "Perkiraan Harga");
    private final DefaultTableModel modelPesanan = Theme.modelTabel(
            "ID Pesanan", "Tanggal Masuk", "Estimasi Selesai", "Total", "Pembayaran", "Status");
    private final DefaultListModel<String> modelNotif = new DefaultListModel<>();

    private final JTable tabelLayanan = Theme.tabel(modelLayanan);
    private final JTable tabelKeranjang = Theme.tabel(modelKeranjang);
    private final JTable tabelPesanan = Theme.tabel(modelPesanan);

    private final SpinnerNumberModel modelJumlah = new SpinnerNumberModel(1.0, 0.5, 500.0, 0.5);
    private final JLabel lblSatuan = Theme.label("Jumlah (kg)", 13, true, Theme.TEXT);
    private final JLabel lblTotal = Theme.label("Total perkiraan: Rp 0", 15, true, Theme.PRIMARY_DARK);
    private final JTextArea txtRincian = new JTextArea();

    public PelangganView(Pelanggan pelanggan, PesananController controller) {
        super("Dashboard Pelanggan", pelanggan, controller, 980, 680);
        this.pelanggan = pelanggan;

        tabs.addTab("Buat Pesanan", buatTabBuatPesanan());
        tabs.addTab("Pesanan Saya", buatTabPesananSaya());
        tabs.addTab("Notifikasi", buatTabNotifikasi());

        refresh();
    }

    // ------------------------------------------------------------ tab 1

    private JPanel buatTabBuatPesanan() {
        JPanel halaman = Theme.halaman(new GridLayout(2, 1, 0, 12));

        // --- kartu pilih layanan
        JPanel kartuLayanan = Theme.kartu(new BorderLayout(0, 10));
        kartuLayanan.add(Theme.label("1. Pilih layanan", 15, true, Theme.TEXT), BorderLayout.NORTH);
        kartuLayanan.add(Theme.gulir(tabelLayanan), BorderLayout.CENTER);
        Theme.ratakanKanan(tabelLayanan, 2);
        tabelLayanan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                sesuaikanInputJumlah();
            }
        });

        JSpinner spinner = new JSpinner(modelJumlah);
        JButton btnTambah = Theme.tombolUtama("Tambah ke keranjang");
        btnTambah.addActionListener(e -> tambahKeKeranjang());
        JPanel barisInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barisInput.setOpaque(false);
        barisInput.add(lblSatuan);
        barisInput.add(spinner);
        barisInput.add(btnTambah);
        kartuLayanan.add(barisInput, BorderLayout.SOUTH);

        // --- kartu keranjang
        JPanel kartuKeranjang = Theme.kartu(new BorderLayout(0, 10));
        kartuKeranjang.add(Theme.label("2. Keranjang", 15, true, Theme.TEXT), BorderLayout.NORTH);
        kartuKeranjang.add(Theme.gulir(tabelKeranjang), BorderLayout.CENTER);
        Theme.ratakanKanan(tabelKeranjang, 1, 2);

        JButton btnHapus = Theme.tombolSekunder("Hapus item");
        btnHapus.addActionListener(e -> hapusDariKeranjang());
        JButton btnBuat = Theme.tombolUtama("Buat Pesanan");
        btnBuat.addActionListener(e -> buatPesanan());

        JPanel barisBawah = new JPanel(new BorderLayout());
        barisBawah.setOpaque(false);
        barisBawah.add(lblTotal, BorderLayout.WEST);
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tombol.setOpaque(false);
        tombol.add(btnHapus);
        tombol.add(btnBuat);
        barisBawah.add(tombol, BorderLayout.EAST);
        kartuKeranjang.add(barisBawah, BorderLayout.SOUTH);

        halaman.add(kartuLayanan);
        halaman.add(kartuKeranjang);
        return halaman;
    }

    private LayananLaundry layananTerpilih() {
        int baris = tabelLayanan.getSelectedRow();
        if (baris < 0 || baris >= dataLayanan.size()) {
            return null;
        }
        return dataLayanan.get(baris);
    }

    /** Satuan dan langkah input mengikuti jenis layanan yang dipilih (kg atau pcs). */
    private void sesuaikanInputJumlah() {
        LayananLaundry layanan = layananTerpilih();
        if (layanan == null) {
            return;
        }
        boolean perPcs = "pcs".equals(layanan.getSatuan());
        double minimum = perPcs ? 1.0 : 0.5;
        modelJumlah.setMinimum(minimum);
        modelJumlah.setStepSize(perPcs ? 1.0 : 0.5);
        modelJumlah.setValue(Math.max(minimum, ((Number) modelJumlah.getValue()).doubleValue()));
        lblSatuan.setText("Jumlah (" + layanan.getSatuan() + ")");
    }

    private void tambahKeKeranjang() {
        LayananLaundry layanan = layananTerpilih();
        if (layanan == null) {
            tampilkanInfo("Pilih layanan terlebih dahulu pada tabel di atas.");
            return;
        }
        double jumlah = ((Number) modelJumlah.getValue()).doubleValue();
        for (ItemKeranjang item : keranjang) {
            if (item.layanan.equals(layanan)) {
                item.jumlah += jumlah;
                muatKeranjang();
                return;
            }
        }
        keranjang.add(new ItemKeranjang(layanan, jumlah));
        muatKeranjang();
    }

    private void hapusDariKeranjang() {
        int baris = tabelKeranjang.getSelectedRow();
        if (baris < 0 || baris >= keranjang.size()) {
            tampilkanInfo("Pilih item di keranjang yang ingin dihapus.");
            return;
        }
        keranjang.remove(baris);
        muatKeranjang();
    }

    private void muatKeranjang() {
        modelKeranjang.setRowCount(0);
        double total = 0;
        for (ItemKeranjang item : keranjang) {
            double harga = item.perkiraanHarga();
            total += harga;
            modelKeranjang.addRow(new Object[] {
                item.layanan.getNamaLayanan(),
                Theme.jumlah(item.jumlah) + " " + item.layanan.getSatuan(),
                Theme.rupiah(harga)
            });
        }
        lblTotal.setText("Total perkiraan: " + Theme.rupiah(total));
    }

    private void buatPesanan() {
        if (keranjang.isEmpty()) {
            tampilkanInfo("Keranjang masih kosong. Tambahkan minimal satu layanan.");
            return;
        }
        try {
            Pesanan pesanan = controller.handleBuatPesanan(controller.buatIdPesanan(), pelanggan);
            for (ItemKeranjang item : keranjang) {
                controller.handleTambahItem(pesanan, item.layanan, item.jumlah);
            }
            keranjang.clear();
            muatKeranjang();
            refresh();
            tabs.setSelectedIndex(TAB_PESANAN);
            Theme.pilihKembali(tabelPesanan, dataPesanan, pesanan);
            tampilkanInfo("Pesanan " + pesanan.getIdPesanan() + " berhasil dibuat.\n"
                    + "Total: " + Theme.rupiah(pesanan.getTotalBiaya()) + "\n"
                    + "Estimasi selesai: " + Theme.tanggal(pesanan.getEstimasiSelesai()) + "\n\n"
                    + "Silakan lakukan pembayaran di kasir.");
        } catch (RuntimeException ex) {
            tampilkanError(ex.getMessage());
        }
    }

    // ------------------------------------------------------------ tab 2

    private JPanel buatTabPesananSaya() {
        JPanel halaman = Theme.halaman(new BorderLayout(0, 12));

        JPanel kartuTabel = Theme.kartu(new BorderLayout(0, 10));
        kartuTabel.add(Theme.label("Riwayat pesanan", 15, true, Theme.TEXT), BorderLayout.NORTH);
        kartuTabel.add(Theme.gulir(tabelPesanan), BorderLayout.CENTER);
        Theme.ratakanKanan(tabelPesanan, 3);
        Theme.lencana(tabelPesanan, 4, 5);
        tabelPesanan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanRincian();
            }
        });

        txtRincian.setEditable(false);
        txtRincian.setLineWrap(true);
        txtRincian.setWrapStyleWord(true);
        txtRincian.setBorder(new EmptyBorder(6, 8, 6, 8));
        JPanel kartuRincian = Theme.kartu(new BorderLayout(0, 8));
        kartuRincian.add(Theme.label("Rincian pesanan", 15, true, Theme.TEXT), BorderLayout.NORTH);
        kartuRincian.add(Theme.gulir(txtRincian), BorderLayout.CENTER);
        kartuRincian.setPreferredSize(new java.awt.Dimension(10, 170));

        halaman.add(kartuTabel, BorderLayout.CENTER);
        halaman.add(kartuRincian, BorderLayout.SOUTH);
        return halaman;
    }

    private void tampilkanRincian() {
        int baris = tabelPesanan.getSelectedRow();
        if (baris < 0 || baris >= dataPesanan.size()) {
            txtRincian.setText("Pilih salah satu pesanan untuk melihat rinciannya.");
            return;
        }
        Pesanan p = dataPesanan.get(baris);
        StringBuilder sb = new StringBuilder();
        for (DetailPesanan d : p.getDaftarDetail()) {
            sb.append("- ").append(d.getLayanan().getNamaLayanan())
              .append("  x ").append(Theme.jumlah(d.getJumlah())).append(' ').append(d.getLayanan().getSatuan())
              .append("  =  ").append(Theme.rupiah(d.getSubtotal())).append('\n');
        }
        sb.append("\nTotal: ").append(Theme.rupiah(p.getTotalBiaya()));
        txtRincian.setText(sb.toString());
        txtRincian.setCaretPosition(0);
    }

    // ------------------------------------------------------------ tab 3

    private JPanel buatTabNotifikasi() {
        JPanel halaman = Theme.halaman(new BorderLayout());
        JPanel kartu = Theme.kartu(new BorderLayout(0, 10));
        kartu.add(Theme.label("Pemberitahuan terbaru", 15, true, Theme.TEXT), BorderLayout.NORTH);

        JList<String> daftar = new JList<>(modelNotif);
        daftar.setFixedCellHeight(34);
        daftar.setBorder(new EmptyBorder(4, 8, 4, 8));
        kartu.add(Theme.gulir(daftar), BorderLayout.CENTER);
        halaman.add(kartu, BorderLayout.CENTER);
        return halaman;
    }

    // ---------------------------------------------------------- refresh

    @Override
    protected void refresh() {
        // layanan
        LayananLaundry layananDipilih = layananTerpilih();
        dataLayanan.clear();
        dataLayanan.addAll(controller.getDaftarLayanan());
        modelLayanan.setRowCount(0);
        for (LayananLaundry l : dataLayanan) {
            modelLayanan.addRow(new Object[] {
                l.getNamaLayanan(),
                l.getJenis(),
                Theme.rupiah(l.getTarif()) + " / " + l.getSatuan(),
                l.getDurasiHari() + " hari",
                l.getDeskripsi()
            });
        }
        Theme.lebarKolom(tabelLayanan, 0, 200);
        Theme.lebarKolom(tabelLayanan, 4, 260);
        if (layananDipilih != null) {
            Theme.pilihKembali(tabelLayanan, dataLayanan, layananDipilih);
        } else if (!dataLayanan.isEmpty()) {
            tabelLayanan.setRowSelectionInterval(0, 0);
        }

        // pesanan
        int barisDipilih = tabelPesanan.getSelectedRow();
        Pesanan pesananDipilih = (barisDipilih >= 0 && barisDipilih < dataPesanan.size())
                ? dataPesanan.get(barisDipilih) : null;
        dataPesanan.clear();
        dataPesanan.addAll(controller.getPesananPelanggan(pelanggan.getIdPengguna()));
        modelPesanan.setRowCount(0);
        for (Pesanan p : dataPesanan) {
            modelPesanan.addRow(new Object[] {
                p.getIdPesanan(),
                Theme.tanggal(p.getTanggalMasuk()),
                Theme.tanggal(p.getEstimasiSelesai()),
                Theme.rupiah(p.getTotalBiaya()),
                controller.sudahDibayar(p) ? "Lunas" : "Belum dibayar",
                p.getStatus()
            });
        }
        Theme.pilihKembali(tabelPesanan, dataPesanan, pesananDipilih);
        tampilkanRincian();

        // notifikasi
        List<Notifikasi> notif = controller.getNotifikasiPelanggan(pelanggan.getIdPengguna());
        modelNotif.clear();
        if (notif.isEmpty()) {
            modelNotif.addElement("Belum ada pemberitahuan.");
        } else {
            for (int i = notif.size() - 1; i >= 0; i--) {
                Notifikasi n = notif.get(i);
                modelNotif.addElement(Theme.tanggal(n.getWaktuKirim()) + "   |   " + n.getPesan());
            }
        }
        tabs.setTitleAt(TAB_NOTIF, notif.isEmpty() ? "Notifikasi" : "Notifikasi (" + notif.size() + ")");
    }
}
