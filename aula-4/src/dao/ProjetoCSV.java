package dao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import model.Projeto;

public class ProjetoCSV {
    private Path caminho;

    public ProjetoCSV() {
        this.caminho = Path.of("dados/projetos.csv");
    }

    public void salvar(List<Projeto> projetos) throws Exception {
        // Cria a pasta 'dados' se ela ainda não existir para evitar erros
        if (caminho.getParent() != null && !Files.exists(caminho.getParent())) {
            Files.createDirectories(caminho.getParent());
        }

        List<String> linhas = new ArrayList<>();
        linhas.add("id;nome;descricao;categoria;status"); // Cabeçalho

        for (Projeto projeto : projetos) {
            String linha = projeto.getId() + ";" +
                           projeto.getNome() + ";" +
                           projeto.getDescricao() + ";" +
                           projeto.getCategoria() + ";" +
                           projeto.getStatus();
            linhas.add(linha);
        }

        Files.write(caminho, linhas);
    }

    public List<Projeto> listar() throws Exception {
        List<Projeto> projetos = new ArrayList<>();

        if (!Files.exists(caminho)) {
            return projetos; // Se o arquivo não existe, retorna lista vazia
        }

        List<String> linhas = Files.readAllLines(caminho);

        // Começa em i = 1 para pular o cabeçalho (linha 0)
        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank()) continue; // Pula linhas em branco

            String[] dados = linha.split(";");

            int id = Integer.parseInt(dados[0].trim());
            String nome = dados[1].trim();
            String descricao = dados[2].trim();
            String categoria = dados[3].trim();
            String status = dados[4].trim();

            Projeto projeto = new Projeto(id, nome, descricao, categoria, status);
            projetos.add(projeto);
        }

        return projetos;
    }
}