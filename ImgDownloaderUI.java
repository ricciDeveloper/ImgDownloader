import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImgDownloaderUI extends JFrame {
    private JTextArea urlTextArea;
    private JTextField directoryField;
    private JLabel counterLabel;
    private JLabel logoLabel;
    private JButton downloadButton;
    private JProgressBar progressBar;
    private AtomicInteger count = new AtomicInteger(0);
    private JPanel bottomPanel;
    private CardLayout cardLayout;

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
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.anchor = GridBagConstraints.CENTER;

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
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 1;
        rightPanel.add(logoLabel, gbc);

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Botão inferior e barra de progresso com CardLayout
        cardLayout = new CardLayout();
        bottomPanel = new JPanel(cardLayout);
        bottomPanel.setBackground(Color.BLACK);

        downloadButton = createOrangeButton("REALIZAR DOWNLOAD");
        downloadButton.setFont(new Font("Arial", Font.BOLD, 14));
        downloadButton.addActionListener(e -> iniciarDownload());

        // Configurações globais para a barra de progresso
        UIManager.put("ProgressBar.selectionForeground", Color.WHITE); // Cor do texto em preto
        UIManager.put("ProgressBar.selectionBackground", Color.WHITE); // Cor do texto no fundo preto

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(ORANGE); // Cor do progresso em laranja
        progressBar.setBackground(Color.BLACK); // Fundo da barra em preto
        progressBar.setPreferredSize(new Dimension(400, 40)); // Tamanho maior para visibilidade
        progressBar.setFont(new Font("Arial", Font.BOLD, 14)); // Fonte em negrito e tamanho 14

        // Adiciona os componentes ao CardLayout com nomes
        bottomPanel.add(downloadButton, "button");
        bottomPanel.add(progressBar, "progress");
        cardLayout.show(bottomPanel, "button"); // Começa com o botão visível
        add(bottomPanel, BorderLayout.SOUTH);
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

        // Contagem de URLs válidas
        int totalUrls = (int) Arrays.stream(urls).filter(url -> !url.trim().isBlank()).count();
        if (totalUrls == 0) {
            JOptionPane.showMessageDialog(this, "Nenhuma URL válida inserida.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Alterna para a barra de progresso
        System.out.println("Iniciando download - Mostrando barra de progresso...");
        progressBar.setMaximum(totalUrls);
        progressBar.setValue(0);
        cardLayout.show(bottomPanel, "progress");
        bottomPanel.revalidate();
        bottomPanel.repaint();
        System.out.println("Barra de progresso visível: " + progressBar.isVisible());

        int totalThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        count.set(0); // Reinicia o contador

        for (int i = 0; i < urls.length; i++) {
            String url = urls[i].trim();
            if (!url.isBlank()) {
                int index = i;
                final int taskIndex = i;
                executor.submit(() -> {
                    try {
                        java.nio.file.Path path = java.nio.file.Paths.get(dir.getAbsolutePath(), "imagem_" + (taskIndex + 1) + ".jpg");
                        try (java.io.InputStream in = new URL(url).openStream();
                             java.io.FileOutputStream out = new java.io.FileOutputStream(path.toFile())) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }
                        int currentCount = count.incrementAndGet();
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(currentCount);
                            progressBar.setString(currentCount + " de " + totalUrls + " imagens");
                            System.out.println("Progresso: " + currentCount + "/" + totalUrls);
                        });
                    } catch (Exception ex) {
                        System.err.println("Erro ao baixar imagem " + url + ": " + ex.getMessage());
                    }
                });
            }
        }

        executor.shutdown();
        new Thread(() -> {
            try {
                while (!executor.isTerminated()) {
                    Thread.sleep(100);
                }
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Download concluído - Restaurando botão...");
                    cardLayout.show(bottomPanel, "button");
                    bottomPanel.revalidate();
                    bottomPanel.repaint();
                    JOptionPane.showMessageDialog(this, "Download concluído: " + count.get() + " imagem(ns) baixada(s).", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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
            return new Insets(radius + 1, radius + 1, radius + 2, radius);
        }
        public boolean isBorderOpaque() {
            return true;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(ORANGE);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImgDownloaderUI().setVisible(true));
    }
}