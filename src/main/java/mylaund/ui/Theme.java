package mylaund.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import mylaund.model.DetailPesanan;
import mylaund.model.Pesanan;
import mylaund.model.StatusPesanan;

/**
 * Tema tampilan bersama untuk semua layar MyLaund: warna, font, dan
 * pembuat komponen (tombol, kartu, tabel) agar tampilan seragam.
 */
final class Theme {

    static final Color PRIMARY = new Color(0x0E7C86);
    static final Color PRIMARY_DARK = new Color(0x0A5A62);
    static final Color PRIMARY_LIGHT = new Color(0xD9F0F2);
    static final Color BG = new Color(0xF3F6F8);
    static final Color CARD = Color.WHITE;
    static final Color BORDER = new Color(0xDDE3E8);
    static final Color TEXT = new Color(0x1F2933);
    static final Color MUTED = new Color(0x6B7280);
    static final Color SUCCESS = new Color(0x1B8F5A);
    static final Color WARNING = new Color(0xC77700);
    static final Color DANGER = new Color(0xC0392B);
    static final Color INFO = new Color(0x1F6FEB);
    private static final Color ZEBRA = new Color(0xF7FAFB);

    private static final Locale ID = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", ID);

    private static boolean terpasang = false;

    private Theme() {
    }

    // ------------------------------------------------------------------ setup

    static void install() {
        if (terpasang) {
            return;
        }
        terpasang = true;
        try {
            UIManager.put("nimbusBase", PRIMARY_DARK);
            UIManager.put("nimbusSelectionBackground", PRIMARY);
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // tampilan bawaan Java tetap dipakai bila Nimbus tidak tersedia
        }
        UIManager.put("defaultFont", new FontUIResource(new Font("Segoe UI", Font.PLAIN, 13)));
    }

    // ------------------------------------------------------------- formatting

    static String rupiah(double nilai) {
        return "Rp " + NumberFormat.getInstance(ID).format(Math.round(nilai));
    }

    static String tanggal(LocalDateTime waktu) {
        return waktu == null ? "-" : TANGGAL.format(waktu);
    }

    static String jumlah(double nilai) {
        if (nilai == Math.floor(nilai)) {
            return String.valueOf((long) nilai);
        }
        return String.valueOf(nilai);
    }

    static String judulKata(String teks) {
        String[] kata = teks.toLowerCase().split("[_ ]+");
        StringBuilder sb = new StringBuilder();
        for (String k : kata) {
            if (k.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(k.charAt(0))).append(k.substring(1));
        }
        return sb.toString();
    }

    static String namaStatus(StatusPesanan status) {
        return judulKata(status.name());
    }

    static Color warnaStatus(StatusPesanan status) {
        StatusPesanan[] semua = StatusPesanan.values();
        if (status == semua[semua.length - 1]) {
            return SUCCESS;
        }
        if (status == semua[0]) {
            return WARNING;
        }
        return INFO;
    }

    static boolean selesai(StatusPesanan status) {
        StatusPesanan[] semua = StatusPesanan.values();
        return status == semua[semua.length - 1];
    }

    static String ringkasLayanan(Pesanan pesanan) {
        StringBuilder sb = new StringBuilder();
        for (DetailPesanan d : pesanan.getDaftarDetail()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(d.getLayanan().getNamaLayanan());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    // -------------------------------------------------------------- komponen

    static JLabel label(String teks, int ukuran, boolean tebal, Color warna) {
        JLabel l = new JLabel(teks);
        l.setFont(l.getFont().deriveFont(tebal ? Font.BOLD : Font.PLAIN, (float) ukuran));
        l.setForeground(warna);
        return l;
    }

    static JPanel halaman(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        return p;
    }

    static JPanel kartu(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    static JScrollPane gulir(Component isi) {
        JScrollPane s = new JScrollPane(isi);
        s.setBorder(new LineBorder(BORDER));
        s.getViewport().setBackground(Color.WHITE);
        return s;
    }

    static void gayaField(JTextField f) {
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 10, 7, 10)));
    }

    static JButton tombolUtama(String teks) {
        return new AppButton(teks, PRIMARY, Color.WHITE, false);
    }

    static JButton tombolSekunder(String teks) {
        return new AppButton(teks, Color.WHITE, TEXT, true);
    }

    static JButton tombolBahaya(String teks) {
        return new AppButton(teks, DANGER, Color.WHITE, false);
    }

    // ---------------------------------------------------------------- tabel

    static DefaultTableModel modelTabel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int baris, int kolom) {
                return false;
            }
        };
    }

    static JTable tabel(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int baris, int kolom) {
                Component c = super.prepareRenderer(renderer, baris, kolom);
                if (isRowSelected(baris)) {
                    c.setBackground(PRIMARY_LIGHT);
                    c.setForeground(TEXT);
                } else {
                    c.setBackground(baris % 2 == 0 ? Color.WHITE : ZEBRA);
                }
                return c;
            }
        };
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        t.setForeground(TEXT);
        t.setDefaultRenderer(Object.class, new PaddedRenderer(SwingConstants.LEADING));

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(10, 34));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean sel,
                    boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, false, false, row, col);
                setOpaque(true);
                setBackground(PRIMARY_DARK);
                setForeground(Color.WHITE);
                setFont(getFont().deriveFont(Font.BOLD));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
        return t;
    }

    static void ratakanKanan(JTable t, int... kolom) {
        for (int k : kolom) {
            t.getColumnModel().getColumn(k).setCellRenderer(new PaddedRenderer(SwingConstants.RIGHT));
        }
    }

    static void lencana(JTable t, int... kolom) {
        for (int k : kolom) {
            t.getColumnModel().getColumn(k).setCellRenderer(new BadgeRenderer());
        }
    }

    static void lebarKolom(JTable t, int kolom, int lebar) {
        t.getColumnModel().getColumn(kolom).setPreferredWidth(lebar);
    }

    /** Memilih kembali baris yang sebelumnya terpilih setelah tabel dimuat ulang. */
    static void pilihKembali(JTable t, List<?> data, Object item) {
        if (item == null) {
            return;
        }
        int idx = data.indexOf(item);
        if (idx >= 0 && idx < t.getRowCount()) {
            t.setRowSelectionInterval(idx, idx);
        }
    }

    // ------------------------------------------------------- kelas pendukung

    private static class PaddedRenderer extends DefaultTableCellRenderer {
        private final int rata;

        PaddedRenderer(int rata) {
            this.rata = rata;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean sel,
                boolean focus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, sel, false, row, col);
            setHorizontalAlignment(rata);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    /** Menampilkan status pesanan atau status pembayaran sebagai teks berwarna. */
    private static class BadgeRenderer extends PaddedRenderer {
        BadgeRenderer() {
            super(SwingConstants.LEADING);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean sel,
                boolean focus, int row, int col) {
            super.getTableCellRendererComponent(tbl, value, sel, focus, row, col);
            Color warna = null;
            if (value instanceof StatusPesanan) {
                StatusPesanan s = (StatusPesanan) value;
                setText(namaStatus(s));
                warna = warnaStatus(s);
            } else if ("Lunas".equals(value)) {
                warna = SUCCESS;
            } else if ("Belum dibayar".equals(value)) {
                warna = WARNING;
            }
            if (warna != null) {
                setFont(getFont().deriveFont(Font.BOLD));
                if (!sel) {
                    setForeground(warna);
                }
            }
            return this;
        }
    }

    /** Tombol bulat yang menggambar dirinya sendiri sehingga warnanya sama di semua Look and Feel. */
    private static final class AppButton extends JButton {
        private final Color warnaDasar;
        private final Color warnaTeks;
        private final boolean garisTepi;

        AppButton(String teks, Color warnaDasar, Color warnaTeks, boolean garisTepi) {
            super(teks);
            this.warnaDasar = warnaDasar;
            this.warnaTeks = warnaTeks;
            this.garisTepi = garisTepi;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(getFont().deriveFont(Font.BOLD));
            setBorder(new EmptyBorder(9, 20, 9, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color isi = warnaDasar;
            if (!isEnabled()) {
                isi = new Color(0xE3E8EB);
            } else if (getModel().isPressed()) {
                isi = warnaDasar.darker();
            } else if (getModel().isRollover()) {
                isi = garisTepi ? ZEBRA : terang(warnaDasar, 22);
            }
            g2.setColor(isi);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            if (garisTepi) {
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }

            g2.setFont(getFont());
            g2.setColor(isEnabled() ? warnaTeks : new Color(0x8A949B));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }

        private static Color terang(Color c, int selisih) {
            return new Color(Math.min(255, c.getRed() + selisih),
                    Math.min(255, c.getGreen() + selisih),
                    Math.min(255, c.getBlue() + selisih));
        }
    }
}
