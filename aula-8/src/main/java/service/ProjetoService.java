package service;

import java.util.ArrayList;

import dao.ProjetoCSV;
import log.LoggerUtil;
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
        try {
            ArrayList<Projeto> carregados = projetoCSV.carregar();
            this.projetos = (carregados != null) ? carregados : new ArrayList<>();
        } catch (Exception e) {
            this.projetos = new ArrayList<>();
            LoggerUtil.error("Falha ao carregar projetos do CSV: " + e.getMessage());
            LoggerUtil.application("ERROR", "Erro ao carregar base CSV na inicialização");
        }
    }

    public void salvar() {
        try {
            projetoCSV.salvar(projetos);
        } catch (Exception e) {
            LoggerUtil.error("Falha ao salvar projetos no CSV: " + e.getMessage());
            LoggerUtil.application("ERROR", "Erro ao gravar base CSV");
        }
    }

    public int gerarProximoId() {
        // Olha para TODOS os projetos (ativos e cancelados) para o ID nunca se repetir
        if (projetos == null || projetos.isEmpty()) {
            return 1;
        }
        int maiorId = 0;
        for (Projeto p : projetos) {
            if (p.getId() > maiorId) {
                maiorId = p.getId();
            }
        }
        return maiorId + 1;
    }

    public boolean adicionar(Projeto projeto) {
        if (projeto == null || projeto.getNome() == null || projeto.getNome().isBlank()) {
            LoggerUtil.error("Tentativa de adicionar projeto com nome inválido ou vazio");
            return false;
        }

        // Atribui sempre o próximo ID seguro e sequencial
        projeto.setId(gerarProximoId());

        projetos.add(projeto);
        salvar(); // Grava no CSV imediatamente
        LoggerUtil.audit("PROJETO " + projeto.getId() + " CRIADO (nome='" + projeto.getNome() + "', status='" + projeto.getStatus() + "')");
        return true;
    }

    // Retorna apenas os projetos que NÃO estão cancelados (para a tabela principal)
    public ArrayList<Projeto> listarAtivos() {
        ArrayList<Projeto> ativos = new ArrayList<>();
        for (Projeto p : projetos) {
            if (p.getStatus() == null || !p.getStatus().equalsIgnoreCase("Cancelado")) {
                ativos.add(p);
            }
        }
        return ativos;
    }

    // Retorna TODOS os projetos (útil para o Dashboard, estatísticas e cálculo de ID)
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
        if (categoria == null) return resultado;
        for (Projeto p : projetos) {
            if (p.getCategoria() != null && p.getCategoria().equalsIgnoreCase(categoria)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public ArrayList<Projeto> buscarPorStatus(String status) {
        ArrayList<Projeto> resultado = new ArrayList<>();
        if (status == null) return resultado;
        for (Projeto p : projetos) {
            if (p.getStatus() != null && p.getStatus().equalsIgnoreCase(status)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public boolean alterar(Projeto projetoAtualizado) {
        if (projetoAtualizado == null) {
            LoggerUtil.error("Tentativa de alterar projeto nulo");
            return false;
        }
        Projeto projeto = buscarPorId(projetoAtualizado.getId());
        if (projeto == null) {
            LoggerUtil.error("Tentativa de alterar projeto inexistente ID=" + projetoAtualizado.getId());
            return false;
        }
        if (projetoAtualizado.getNome() == null || projetoAtualizado.getNome().isBlank()) {
            LoggerUtil.error("Tentativa de alterar projeto ID=" + projetoAtualizado.getId() + " com nome vazio");
            return false;
        }

        projeto.setNome(projetoAtualizado.getNome());
        projeto.setDescricao(projetoAtualizado.getDescricao());
        projeto.setCategoria(projetoAtualizado.getCategoria());
        projeto.setStatus(projetoAtualizado.getStatus());
        salvar(); // Atualiza e grava no CSV
        LoggerUtil.audit("PROJETO " + projeto.getId() + " ALTERADO (nome='" + projeto.getNome() + "', status='" + projeto.getStatus() + "')");
        return true;
    }

    // Exclusão Lógica: Em vez de apagar do array, muda o status para "Cancelado"
    public boolean removerPorId(int id) {
        Projeto projeto = buscarPorId(id);
        if (projeto != null) {
            projeto.setStatus("Cancelado"); // Oculta da tabela, mas preserva no CSV e no Dashboard
            salvar(); // Grava a alteração no CSV
            LoggerUtil.audit("PROJETO " + id + " EXCLUIDO (status alterado para Cancelado)");
            return true;
        }
        LoggerUtil.error("Tentativa de excluir projeto inexistente ID=" + id);
        return false;
    }
}