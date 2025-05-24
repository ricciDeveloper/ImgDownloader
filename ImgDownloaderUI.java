import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

public class ImgDownloaderUI extends JFrame {
    private JTextArea urlTextArea;
    private JTextField directoryField;
    private JLabel counterLabel;
    private JLabel logoLabel;

    // Cor laranja personalizada (255,154,6)
    public static final Color ORANGE = new Color(255, 154, 6);

    public ImgDownloaderUI() {
        setTitle("DOWNLOADER DE IMAGENS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);
        add(mainPanel, BorderLayout.CENTER);

        // Painel de URLs
        JPanel urlPanel = new JPanel(new BorderLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ORANGE), "INSIRA AS URLs DAS IMAGENS:");
        titledBorder.setTitleColor(ORANGE);
        urlPanel.setBorder(titledBorder);
        urlPanel.setBackground(Color.BLACK);

        urlTextArea = new JTextArea(10, 40);
        urlTextArea.setBackground(Color.DARK_GRAY);
        urlTextArea.setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(urlTextArea);
        urlPanel.add(scrollPane, BorderLayout.CENTER);

        urlTextArea.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                atualizarContadorUrls();
            }
        });

        mainPanel.add(urlPanel, BorderLayout.WEST);

        // Painel direito (diretório + contador + logo)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.BLACK);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0); // Espaçamento vertical
        gbc.anchor = GridBagConstraints.CENTER; // Centraliza os componentes

        JLabel pathLabel = new JLabel("SELECIONE O LOCAL DE DOWNLOAD:");
        pathLabel.setForeground(ORANGE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        rightPanel.add(pathLabel, gbc);

        directoryField = new JTextField(20);
        directoryField.setMaximumSize(new Dimension(250, 25));
        gbc.gridx = 0;
        gbc.gridy = 1;
        rightPanel.add(directoryField, gbc);

        JButton browseButton = createOrangeButton("Selecionar...");
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDir = chooser.getSelectedFile();
                directoryField.setText(selectedDir.getAbsolutePath());
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        rightPanel.add(browseButton, gbc);

        counterLabel = new JLabel("TOTAL: 0 URLs INSERIDAS");
        counterLabel.setForeground(ORANGE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        rightPanel.add(counterLabel, gbc);

        // LOGO (certifique-se que o caminho esteja correto)
        logoLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("imagens/logo.png"); // Substitua por seu caminho
            Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception ex) {
            logoLabel.setText("LOGO AQUI");
            logoLabel.setForeground(Color.GRAY);
        }
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE; // Não estica o componente
        gbc.weightx = 0; // Não ocupa espaço extra horizontalmente
        gbc.weighty = 1; // Ocupa espaço vertical restante para centralizar
        rightPanel.add(logoLabel, gbc);

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Botão inferior
        JButton downloadButton = createOrangeButton("REALIZAR DOWNLOAD");
        downloadButton.setFont(new Font("Arial", Font.BOLD, 14));
        downloadButton.addActionListener(e -> iniciarDownload());
        add(downloadButton, BorderLayout.SOUTH);
    }

    private void atualizarContadorUrls() {
        String text = urlTextArea.getText().trim();
        long count = text.isEmpty() ? 0 : Arrays.stream(text.split("\n"))
                .filter(s -> !s.isBlank())
                .count();
        counterLabel.setText("TOTAL: " + count + " URLs INSERIDAS");
    }

    private void iniciarDownload() {
        String[] urls = urlTextArea.getText().trim().split("\n");
        String directoryPath = directoryField.getText().trim();

        if (urls.length == 0 || directoryPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha os campos de URLs e diretório.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File dir = new File(directoryPath);
        if (!dir.exists()) dir.mkdirs();

        int count = 0;
        for (int i = 0; i < urls.length; i++) {
            String url = urls[i].trim();
            if (!url.isBlank()) {
                try {
                    java.nio.file.Path path = java.nio.file.Paths.get(dir.getAbsolutePath(), "imagem_" + (i + 1) + ".jpg");
                    java.io.InputStream in = new URL(url).openStream();
                    java.nio.file.Files.copy(in, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    in.close();
                    count++;
                } catch (Exception ex) {
                    System.err.println("Erro ao baixar imagem " + url + ": " + ex.getMessage());
                }
            }
        }

        JOptionPane.showMessageDialog(this, "Download concluído: " + count + " imagem(ns) baixada(s).", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createOrangeButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ORANGE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(10));
        return button;
    }

    // Classe auxiliar para borda arredondada
    static class RoundedBorder implements Border {
        private int radius;
        RoundedBorder(int radius) {
            this.radius = radius;
        }
        public Insets getBorderInsets(Component c) {
            return new Insets(radius+1, radius+1, radius+2, radius);
        }
        public boolean isBorderOpaque() {
            return true;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(ORANGE);
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImgDownloaderUI().setVisible(true));
    }
}