package service;

import dao.ProjetoCSV;
import java.util.ArrayList;
import model.Projeto;

public class ProjetoService {
    private ArrayList<Projeto> projetos;
    private ProjetoCSV projetoCSV;

    public ProjetoService() {
        this.projetos = new ArrayList<>();
        this.projetoCSV = new ProjetoCSV();
    }

    public void carregar() {
        this.projetos = projetoCSV.carregar();
    }

    public void salvar() {
        projetoCSV.salvar(projetos);
    }

    public boolean adicionar(Projeto projeto) {
        if (buscarPorId(projeto.getId()) != null) {
            return false; // ID já existe
        }
        projetos.add(projeto);
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
        return true;
    }

    public boolean removerPorId(int id) {
        Projeto projeto = buscarPorId(id);
        if (projeto != null) {
            projetos.remove(projeto);
            return true;
        }
        return false;
    }
}