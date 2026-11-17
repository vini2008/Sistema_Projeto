package service;

import java.util.ArrayList;

import dao.ProjetoCSV;
import model.Projeto;

public class ProjetoService {
    private ArrayList<Projeto> projetos;
    private ProjetoCSV projetoCSV;

    public ProjetoService() {
        this.projetos = new ArrayList<>();
        this.projetoCSV = new ProjetoCSV();
        carregar();
    }

    public void carregar() {
        this.projetos = projetoCSV.carregar();
    }

    public void salvar() {
        projetoCSV.salvar(projetos);
    }

    public boolean adicionar(Projeto projeto) {
        // Se o ID for 0, inválido ou já existir, gera automaticamente o próximo ID disponível
        if (projeto.getId() <= 0 || buscarPorId(projeto.getId()) != null) {
            int maiorId = 0;
            for (Projeto p : projetos) {
                if (p.getId() > maiorId) {
                    maiorId = p.getId();
                }
            }
            projeto.setId(maiorId + 1);
        }
        
        projetos.add(projeto);
        salvar(); // Grava no CSV imediatamente
        return true;
    }

    public ArrayList<Projeto> listar() {
        return projetos;
    }

    public Projeto buscarPorId(int id) {
        for (Projeto p : projetos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Projeto> buscarPorCategoria(String categoria) {
        ArrayList<Projeto> resultado = new ArrayList<>();
        for (Projeto p : projetos) {
            if (p.getCategoria().equalsIgnoreCase(categoria)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public ArrayList<Projeto> buscarPorStatus(String status) {
        ArrayList<Projeto> resultado = new ArrayList<>();
        for (Projeto p : projetos) {
            if (p.getStatus().equalsIgnoreCase(status)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public boolean alterar(Projeto projetoAtualizado) {
        Projeto projeto = buscarPorId(projetoAtualizado.getId());
        if (projeto == null) {
            return false;
        }
        projeto.setNome(projetoAtualizado.getNome());
        projeto.setDescricao(projetoAtualizado.getDescricao());
        projeto.setCategoria(projetoAtualizado.getCategoria());
        projeto.setStatus(projetoAtualizado.getStatus());
        salvar(); // Atualiza e grava no CSV
        return true;
    }

    public boolean removerPorId(int id) {
        Projeto projeto = buscarPorId(id);
        if (projeto != null) {
            projetos.remove(projeto);
            salvar(); // Grava no CSV após remover
            return true;
        }
        return false;
    }
}