package mylaund;

import java.awt.GraphicsEnvironment;
import mylaund.model.ITransaksi;
import mylaund.model.LayananLaundry;
import mylaund.model.Pelanggan;
import mylaund.model.Pembayaran;
import mylaund.model.Pesanan;
import mylaund.model.StatusPesanan;
import mylaund.repository.LayananRepository;
import mylaund.repository.NotifikasiRepository;
import mylaund.repository.PenggunaRepository;
import mylaund.repository.PesananRepository;
import mylaund.repository.TransaksiRepository;
import mylaund.repository.sqlite.DatabaseConnection;
import mylaund.repository.sqlite.SqliteLayananRepository;
import mylaund.repository.sqlite.SqliteNotifikasiRepository;
import mylaund.repository.sqlite.SqlitePenggunaRepository;
import mylaund.repository.sqlite.SqlitePesananRepository;
import mylaund.repository.sqlite.SqliteTransaksiRepository;
import mylaund.service.AuthService;
import mylaund.service.NotifikasiService;
import mylaund.service.PembayaranService;
import mylaund.service.PesananService;
import mylaund.ui.LoginFrame;
import mylaund.ui.PesananController;

public class App {
    public static void main(String[] args) {
        // 1. Composition Root: membuat DatabaseConnection & implementasi Repository SQLite.
        //    Akun demo dan daftar layanan awal dibuat oleh inisialisasiSkema().
        DatabaseConnection db = new DatabaseConnection("mylaund.db");
        db.inisialisasiSkema();

        PenggunaRepository penggunaRepo = new SqlitePenggunaRepository(db);
        LayananRepository layananRepo = new SqliteLayananRepository(db);
        PesananRepository pesananRepo = new SqlitePesananRepository(db, penggunaRepo, layananRepo);
        TransaksiRepository transaksiRepo = new SqliteTransaksiRepository(db, pesananRepo);
        NotifikasiRepository notifikasiRepo = new SqliteNotifikasiRepository(db, penggunaRepo);

        // 2. Menyuntikkan interface Repository ke lapisan Service (Constructor Injection)
        AuthService authService = new AuthService(penggunaRepo);
        NotifikasiService notifikasiService = new NotifikasiService(notifikasiRepo);
        PesananService pesananService = new PesananService(pesananRepo, layananRepo, notifikasiService, transaksiRepo);
        PembayaranService pembayaranService = new PembayaranService(transaksiRepo);

        // 3. Menghubungkan Service ke lapisan Presentation (Controller & View)
        PesananController controller = new PesananController(
                authService, pesananService, pembayaranService, notifikasiService);

        // 4. Contoh pesanan hanya dibuat saat database masih kosong, supaya tidak terulang
        //    setiap aplikasi dijalankan. Hapus bagian ini jika tidak diperlukan.
        if (controller.getSemuaPesanan().isEmpty()) {
            buatDataContoh(controller, penggunaRepo, layananRepo);
        }

        // 5. Menampilkan LoginFrame jika berjalan di lingkungan desktop
        if (!GraphicsEnvironment.isHeadless()) {
            LoginFrame.tampilkan(controller);
        }
    }

    private static void buatDataContoh(PesananController controller, PenggunaRepository penggunaRepo,
            LayananRepository layananRepo) {
        Pelanggan wati = (Pelanggan) penggunaRepo.cariByUsername("wati");
        LayananLaundry kiloan = layananRepo.cariById("L01");
        LayananLaundry satuan = layananRepo.cariById("L02");

        Pesanan pesanan = controller.handleBuatPesanan(controller.buatIdPesanan(), wati);
        controller.handleTambahItem(pesanan, kiloan, 3.0);
        controller.handleTambahItem(pesanan, satuan, 2.0);

        System.out.println("Total Biaya Pesanan: Rp" + pesanan.getTotalBiaya());
        System.out.println("Estimasi Selesai   : " + pesanan.getEstimasiSelesai());

        Pembayaran bayar = controller.handlePembayaran(controller.buatIdPembayaran(), pesanan, 50000, "TUNAI");
        System.out.println("Status Pembayaran  : " + (bayar.isLunas() ? "LUNAS" : "BELUM LUNAS"));
        System.out.println("Kembalian          : Rp" + bayar.getKembalian());

        controller.handlePerbaruiStatus(pesanan, StatusPesanan.DIPROSES);
        controller.handlePerbaruiStatus(pesanan, StatusPesanan.SIAP_DIAMBIL);

        System.out.println("=== Riwayat Transaksi ===");
        for (ITransaksi trx : controller.getRiwayatTransaksi()) {
            System.out.println("- " + trx.getDeskripsiTransaksi() + " | Nominal: Rp" + trx.getNominal());
        }
    }
}
