package log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
    private static final Path PASTA_LOGS = Paths.get("logs");
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static synchronized void escrever(String nomeArquivo, String nivel, String mensagem) {
        try {
            Files.createDirectories(PASTA_LOGS);
            String timestamp = LocalDateTime.now().format(FORMATO_DATA);
            String linha = timestamp + " " + nivel + " " + mensagem + System.lineSeparator();
            Path arquivo = PASTA_LOGS.resolve(nomeArquivo);
            Files.writeString(
                    arquivo,
                    linha,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Falha ao escrever no log " + nomeArquivo + ": " + e.getMessage());
        }
    }

    public static void application(String nivel, String mensagem) {
        escrever("application.log", nivel, mensagem);
    }

    public static void access(String usuario, String acao) {
        escrever("access.log", "INFO", "USUARIO=" + usuario + " " + acao);
    }

    public static void audit(String acao) {
        escrever("audit.log", "AUDIT", acao);
    }

    public static void error(String mensagem) {
        escrever("error.log", "ERROR", mensagem);
    }

    public static void api(String metodo, String rota, int status) {
        escrever("api.log", "HTTP", metodo + " " + rota + " -> " + status);
    }
}