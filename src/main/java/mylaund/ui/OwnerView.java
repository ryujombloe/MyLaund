package mylaund.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import mylaund.model.ITransaksi;
import mylaund.model.Owner;
import mylaund.model.Pembayaran;
import mylaund.model.Pesanan;
import mylaund.model.StatusPesanan;

public class OwnerView extends DashboardFrame {

    /** Kartu angka ringkasan (judul kecil + nilai besar). */
    private static final class KartuAngka extends JPanel {
        private final JLabel lblNilai = Theme.label("0", 26, true, Theme.PRIMARY_DARK);

        KartuAngka(String judul) {
            super(new BorderLayout(0, 4));
            setBackground(Theme.CARD);
            setBorder(Theme.kartu(new BorderLayout()).getBorder());
            add(Theme.label(judul, 12, false, Theme.MUTED), BorderLayout.NORTH);
            add(lblNilai, BorderLayout.CENTER);
        }

        void setNilai(String nilai) {
            lblNilai.setText(nilai);
        }
    }

    private final Owner owner;

    private final KartuAngka kartuTotal = new KartuAngka("Total pesanan");
    private final KartuAngka kartuAktif = new KartuAngka("Sedang diproses");
    private final KartuAngka kartuMenunggu = new KartuAngka("Menunggu pembayaran");
    private final KartuAngka kartuPendapatan = new KartuAngka("Pendapatan");

    private final DefaultTableModel modelStatus = Theme.modelTabel("Status", "Jumlah Pesanan");
    private final DefaultTableModel modelPesanan = Theme.modelTabel(
            "ID Pesanan", "Pelanggan", "Tanggal Masuk", "Layanan", "Total", "Pembayaran", "Kembalian", "Status");
    private final DefaultTableModel modelRiwayat = Theme.modelTabel("Tanggal", "Keterangan", "Dibayar", "Kembalian");

    private final JTable tabelStatus = Theme.tabel(modelStatus);
    private final JTable tabelPesanan = Theme.tabel(modelPesanan);
    private final JTable tabelRiwayat = Theme.tabel(modelRiwayat);

    public OwnerView(Owner owner, PesananController controller) {
        super("Dashboard Owner", owner, controller, 1000, 660);
        this.owner = owner;

        tabs.addTab("Ringkasan", buatTabRingkasan());
        tabs.addTab("Semua Pesanan", buatTabTabel("Seluruh pesanan masuk", tabelPesanan));
        tabs.addTab("Riwayat Transaksi", buatTabTabel("Pembayaran yang sudah tercatat", tabelRiwayat));

        Theme.ratakanKanan(tabelStatus, 1);
        Theme.ratakanKanan(tabelPesanan, 4, 6);
        Theme.lencana(tabelPesanan, 5, 7);
        Theme.lebarKolom(tabelPesanan, 3, 220);
        Theme.ratakanKanan(tabelRiwayat, 2, 3);
        Theme.lebarKolom(tabelRiwayat, 1, 420);

        refresh();
    }

    private JPanel buatTabRingkasan() {
        JPanel halaman = Theme.halaman(new BorderLayout(0, 14));

        JPanel baris = new JPanel(new GridLayout(1, 4, 12, 0));
        baris.setOpaque(false);
        baris.add(kartuTotal);
        baris.add(kartuAktif);
        baris.add(kartuMenunggu);
        baris.add(kartuPendapatan);
        halaman.add(baris, BorderLayout.NORTH);

        JPanel kartu = Theme.kartu(new BorderLayout(0, 10));
        kartu.add(Theme.label("Jumlah pesanan per status", 15, true, Theme.TEXT), BorderLayout.NORTH);
        kartu.add(Theme.gulir(tabelStatus), BorderLayout.CENTER);
        halaman.add(kartu, BorderLayout.CENTER);
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

    @Override
    protected void refresh() {
        List<Pesanan> semua = controller.getSemuaPesanan();

        int aktif = 0;
        int menunggu = 0;
        double pendapatan = 0;
        int[] perStatus = new int[StatusPesanan.values().length];

        modelPesanan.setRowCount(0);
        for (Pesanan p : semua) {
            Pembayaran bayar = controller.getPembayaran(p);
            boolean lunas = bayar != null;
            if (!Theme.selesai(p.getStatus())) {
                aktif++;
            }
            if (lunas) {
                pendapatan += p.getTotalBiaya();
            } else {
                menunggu++;
            }
            perStatus[p.getStatus().ordinal()]++;
            modelPesanan.addRow(new Object[] {
                p.getIdPesanan(),
                p.getPelanggan().getNama(),
                Theme.tanggal(p.getTanggalMasuk()),
                Theme.ringkasLayanan(p),
                Theme.rupiah(p.getTotalBiaya()),
                lunas ? "Lunas" : "Belum dibayar",
                lunas ? Theme.rupiah(bayar.getKembalian()) : "-",
                p.getStatus()
            });
        }

        kartuTotal.setNilai(String.valueOf(semua.size()));
        kartuAktif.setNilai(String.valueOf(aktif));
        kartuMenunggu.setNilai(String.valueOf(menunggu));
        kartuPendapatan.setNilai(Theme.rupiah(pendapatan));

        modelStatus.setRowCount(0);
        for (StatusPesanan s : StatusPesanan.values()) {
            modelStatus.addRow(new Object[] {Theme.namaStatus(s), perStatus[s.ordinal()]});
        }

        modelRiwayat.setRowCount(0);
        List<ITransaksi> riwayat = controller.getRiwayatTransaksi();
        for (int i = riwayat.size() - 1; i >= 0; i--) {
            ITransaksi t = riwayat.get(i);
            modelRiwayat.addRow(new Object[] {
                Theme.tanggal(t.getTanggal()), t.getDeskripsiTransaksi(), Theme.rupiah(t.getNominal()),
                t instanceof Pembayaran ? Theme.rupiah(((Pembayaran) t).getKembalian()) : "-"
            });
        }
    }
}
