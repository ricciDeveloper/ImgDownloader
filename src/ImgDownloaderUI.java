package src;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    // Conjunto de extensões de imagem válidas
    private static final Set<String> VALID_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    ));

    public ImgDownloaderUI() {
        setTitle("DOWNLOADER DE IMAGENS");
        try {
            ImageIcon icon = new ImageIcon("imagens/logo1.png");
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + e.getMessage());
        }

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
            ImageIcon icon = new ImageIcon("imagens/logo.png");
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

        int totalUrls = (int) Arrays.stream(urls).filter(url -> !url.trim().isBlank()).count();
        if (totalUrls == 0) {
            JOptionPane.showMessageDialog(this, "Nenhuma URL válida inserida.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        progressBar.setMaximum(totalUrls);
        progressBar.setValue(0);
        cardLayout.show(bottomPanel, "progress");
        bottomPanel.revalidate();
        bottomPanel.repaint();

        int totalThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        count.set(0);

        for (int i = 0; i < urls.length; i++) {
            String url = urls[i].trim();
            if (!url.isBlank()) {
                final int taskIndex = i;
                executor.submit(() -> {
                    try {
                        java.nio.file.Path path;
                        try {
                            URL imageUrl = new URL(url);
                            String path1 = imageUrl.getPath();
                            String[] parts = path1.split("/");

                            String rawName = (parts.length >= 2)
                                    ? parts[parts.length - 2] + "-" + parts[parts.length - 1]
                                    : "imagem_" + (taskIndex + 1);

                            // Remove query parameters
                            rawName = rawName.split("\\?")[0];

                            // Extrai a extensão do nome do arquivo
                            String fileExtension = "";
                            int lastDotIndex = rawName.lastIndexOf(".");
                            if (lastDotIndex != -1 && lastDotIndex < rawName.length() - 1) {
                                fileExtension = rawName.substring(lastDotIndex + 1).toLowerCase();
                            }

                            // Verifica se a extensão é válida; se não for, usa .jpg como padrão
                            if (!VALID_IMAGE_EXTENSIONS.contains(fileExtension)) {
                                fileExtension = "jpg";
                                rawName = rawName.substring(0, lastDotIndex == -1 ? rawName.length() : lastDotIndex) + ".jpg";
                            }

                            String fileName = rawName.replaceAll("[\\\\/:*?\"<>|]", "-");

                            if (fileName.trim().isEmpty() || fileName.length() < 3) {
                                fileName = "imagem_" + (taskIndex + 1) + "." + fileExtension;
                            }

                            path = java.nio.file.Paths.get(dir.getAbsolutePath(), fileName);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }

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

                    int option = JOptionPane.showConfirmDialog(this,
                            "Download concluído: " + count.get() + " imagens baixadas.\n\nDeseja abrir a pasta de destino?",
                            "Sucesso", JOptionPane.YES_NO_OPTION);

                    if (option == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(new File(directoryPath));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                    "Erro ao abrir a pasta: " + ex.getMessage(),
                                    "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
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