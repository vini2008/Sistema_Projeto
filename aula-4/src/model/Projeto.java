package model;

public class Projeto {
    private int id;
    private String nome;
    private String descricao;
    private String categoria;
    private String status;

    public Projeto(int id, String nome, String descricao, String categoria, String status) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.status = status;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getCategoria() { return categoria; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public void exibirDados() {
        System.out.println("ID: " + id + " | Nome: " + nome + " | Categoria: " + categoria + " | Status: " + status);
    }
}