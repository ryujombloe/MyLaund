package mylaund.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import mylaund.model.Pengguna;

/**
 * Kerangka bersama semua dashboard: header (nama aplikasi, pengguna, logout),
 * tab konten, dan pemuatan ulang data saat jendela diaktifkan.
 */
abstract class DashboardFrame extends JFrame {

    protected final PesananController controller;
    protected final JTabbedPane tabs = new JTabbedPane();

    protected DashboardFrame(String judul, Pengguna pengguna, PesananController controller,
            int lebar, int tinggi) {
        super("MyLaund - " + judul);
        this.controller = controller;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                refresh();
            }
        });

        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        add(buatHeader(pengguna), BorderLayout.NORTH);

        JPanel isi = new JPanel(new BorderLayout());
        isi.setBackground(Theme.BG);
        isi.setBorder(new EmptyBorder(10, 12, 12, 12));
        isi.add(tabs, BorderLayout.CENTER);
        add(isi, BorderLayout.CENTER);

        setSize(lebar, tinggi);
        setMinimumSize(new Dimension(780, 540));
        setLocationRelativeTo(null);
    }

    /** Memuat ulang data di semua tab. */
    protected abstract void refresh();

    private JPanel buatHeader(Pengguna pengguna) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel kiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        kiri.setOpaque(false);
        kiri.add(Theme.label("MyLaund", 22, true, Color.WHITE));
        kiri.add(Theme.label(Theme.judulKata(pengguna.getPeran()), 13, false, new Color(0xCFEFF2)));
        header.add(kiri, BorderLayout.WEST);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        kanan.setOpaque(false);
        kanan.add(Theme.label("Halo, " + pengguna.getNama(), 14, true, Color.WHITE));
        javax.swing.JButton btnLogout = Theme.tombolSekunder("Logout");
        btnLogout.addActionListener(e -> logout());
        kanan.add(btnLogout);
        header.add(kanan, BorderLayout.EAST);
        return header;
    }

    protected void logout() {
        dispose();
        new LoginFrame(controller).setVisible(true);
    }

    protected void tampilkanInfo(String pesan) {
        javax.swing.JOptionPane.showMessageDialog(this, pesan, "Informasi",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    protected void tampilkanError(String pesan) {
        javax.swing.JOptionPane.showMessageDialog(this, pesan, "Tidak dapat diproses",
                javax.swing.JOptionPane.WARNING_MESSAGE);
    }
}
