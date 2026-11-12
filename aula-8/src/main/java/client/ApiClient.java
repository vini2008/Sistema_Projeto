package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Projeto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ApiClient {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl = "http://localhost:7070";

    public ApiClient() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public List<Projeto> listarProjetos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/projetos"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erro ao listar. Código: " + response.statusCode());
        }

        return mapper.readValue(response.body(), new TypeReference<List<Projeto>>() {});
    }

public Projeto cadastrarProjeto(Projeto projeto) throws Exception {
    String json = mapper.writeValueAsString(projeto);
    System.out.println("JSON enviado: " + json); // Vê o que está a ser enviado

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/projetos"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
    System.out.println("Status da resposta: " + response.statusCode());
    System.out.println("Corpo da resposta: " + response.body());

    if (response.statusCode() != 201 && response.statusCode() != 200) {
        throw new RuntimeException("Erro ao cadastrar. Código: " + response.statusCode() + " - " + response.body());
    }

    return mapper.readValue(response.body(), Projeto.class);
}
}