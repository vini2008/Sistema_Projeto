package service;

import dao.ProjetoCSV;
import java.util.ArrayList;
import java.util.List;
import model.Projeto;

public class ProjetoService {
    private List<Projeto> projetos;
    private ProjetoCSV dao;

    public ProjetoService() {
        this.projetos = new ArrayList<>();
        this.dao = new ProjetoCSV();
    }

    public void carregar() throws Exception {
        this.projetos = dao.listar();
    }

    public void salvar() throws Exception {
        dao.salvar(this.projetos);
    }

    public boolean adicionar(Projeto projeto) {
        if (projeto.getNome() == null || projeto.getNome().isBlank()) {
            return false;
        }
        if (buscarPorId(projeto.getId()) != null) {
            return false;
        }
        projetos.add(projeto);
        return true;
    }

    public List<Projeto> listar() {
        return projetos;
    }

    public Projeto buscarPorId(int id) {
        for (Projeto projeto : projetos) {
            if (projeto.getId() == id) {
                return projeto;
            }
        }
        return null;
    }
}