package mylaund.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import mylaund.model.Kasir;
import mylaund.model.Owner;
import mylaund.model.Pekerja;
import mylaund.model.Pelanggan;
import mylaund.model.Pengguna;

public class LoginFrame extends JFrame {

    private final PesananController controller;
    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JLabel lblPesan = Theme.label(" ", 12, false, Theme.DANGER);
    private final JButton btnLogin = Theme.tombolUtama("Masuk");

    /** Menyiapkan tema lalu menampilkan layar login di thread Swing. */
    public static void tampilkan(PesananController controller) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new LoginFrame(controller).setVisible(true);
        });
    }

    public LoginFrame(PesananController controller) {
        this.controller = controller;
        setTitle("MyLaund - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 520);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel latar = new JPanel(new GridBagLayout());
        latar.setBackground(Theme.BG);
        latar.add(buatKartu());
        setContentPane(latar);

        getRootPane().setDefaultButton(btnLogin);
    }

    private JPanel buatKartu() {
        JPanel kartu = Theme.kartu(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel judul = Theme.label("MyLaund", 32, true, Theme.PRIMARY);
        judul.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 0;
        c.insets = new Insets(16, 24, 0, 24);
        kartu.add(judul, c);

        JLabel sub = Theme.label("Layanan laundry, tanpa ribet", 13, false, Theme.MUTED);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 1;
        c.insets = new Insets(2, 24, 22, 24);
        kartu.add(sub, c);

        c.gridy = 2;
        c.insets = new Insets(0, 24, 4, 24);
        kartu.add(Theme.label("Username", 12, true, Theme.TEXT), c);

        Theme.gayaField(txtUsername);
        c.gridy = 3;
        c.insets = new Insets(0, 24, 12, 24);
        kartu.add(txtUsername, c);

        c.gridy = 4;
        c.insets = new Insets(0, 24, 4, 24);
        kartu.add(Theme.label("Password", 12, true, Theme.TEXT), c);

        Theme.gayaField(txtPassword);
        c.gridy = 5;
        c.insets = new Insets(0, 24, 6, 24);
        kartu.add(txtPassword, c);

        c.gridy = 6;
        c.insets = new Insets(0, 24, 10, 24);
        kartu.add(lblPesan, c);

        btnLogin.addActionListener(e -> prosesLogin());
        c.gridy = 7;
        c.insets = new Insets(0, 24, 14, 24);
        kartu.add(btnLogin, c);

        JLabel petunjuk = Theme.label("Akun demo: wati, kasir, pekerja, owner (sandi 1234)",
                11, false, Theme.MUTED);
        petunjuk.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 8;
        c.insets = new Insets(0, 24, 16, 24);
        kartu.add(petunjuk, c);
        return kartu;
    }

    private void prosesLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblPesan.setText("Username dan password wajib diisi.");
            return;
        }

        Pengguna user = controller.handleLogin(username, password);
        if (user == null) {
            lblPesan.setText("Username atau password salah.");
            txtPassword.setText("");
            txtPassword.requestFocusInWindow();
            return;
        }

        DashboardFrame dashboard = buatDashboard(user);
        if (dashboard == null) {
            lblPesan.setText("Peran pengguna tidak dikenali.");
            return;
        }
        dashboard.setVisible(true);
        dispose();
    }

    private DashboardFrame buatDashboard(Pengguna user) {
        if (user instanceof Pelanggan) {
            return new PelangganView((Pelanggan) user, controller);
        } else if (user instanceof Kasir) {
            return new KasirView((Kasir) user, controller);
        } else if (user instanceof Pekerja) {
            return new PekerjaView((Pekerja) user, controller);
        } else if (user instanceof Owner) {
            return new OwnerView((Owner) user, controller);
        }
        return null;
    }
}
