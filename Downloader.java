import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Downloader {
    public static void downloadImages(List<String> urls, String pastaDestino){
            Path pasta = Paths.get(pastaDestino);
            try {
                if (!Files.exists(pasta)) {
                    Files.createDirectories(pasta);
                }

                for (int i = 0; i < urls.size(); i++) {
                    String urlStr = urls.get(i);
                    try (InputStream in = new URL(urlStr).openStream()) {

                        Path caminhoArquivo = getPath(urlStr, i, pasta);

                        Files.copy(in, caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Imagem salva: " + caminhoArquivo);
                    } catch (IOException e) {
                        System.out.println("Erro ao baixar imagem: " + urlStr);
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                System.out.println("Erro ao criar diretório: " + pastaDestino);
                e.printStackTrace();
            }
        }

    private static Path getPath(String urlStr, int i, Path pasta) {
        String extensao = ".jpg";
        if (urlStr.contains(".")) {
            String[] partes = urlStr.split("\\.");
            extensao = "." + partes[partes.length - 1].split("[?&]")[0];
            if (extensao.length() > 5 || extensao.contains("/"))
                extensao = ".jpg"; // fallback
        }
        String nomeArquivo = "img" + (i + 1) + extensao;
        Path caminhoArquivo = pasta.resolve(nomeArquivo);
        return caminhoArquivo;
    }

    public static void main (String[] args){
        List<String> listaDeUrls = List.of(


                // Adicione até 20 URLs
                "https://www.tempest.com.br/_next/static/media/image-card-01.47ecf425.jpg"
        );
        downloadImages(listaDeUrls, "imagens");
    }
}
