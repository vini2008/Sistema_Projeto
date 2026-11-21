package api;

import java.util.List;

import io.javalin.Javalin;
import log.LoggerUtil;
import model.Projeto;
import service.ProjetoService;

public class Api {
    public static void main(String[] args) {
        LoggerUtil.application("INFO", "Iniciando sistema e carregando dados...");

        ProjetoService service = new ProjetoService();
        service.carregar(); // Carrega os dados salvos no CSV ao iniciar

        var app = Javalin.create(config -> {
            config.staticFiles.add("/public", io.javalin.http.staticfiles.Location.CLASSPATH);
        }).start(7070);

        LoggerUtil.application("INFO", "Servidor Javalin iniciado na porta 7070");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LoggerUtil.application("INFO", "Servidor Javalin encerrado");
        }));

        // Registra acesso ao sistema (chamado pela Web ou Desktop ao abrir)
        app.get("/api/acesso", ctx -> {
            String origem = ctx.queryParam("origem");
            if (origem == null || origem.isBlank()) {
                origem = "web";
            }
            LoggerUtil.access(origem, "ACESSO_SISTEMA ip=" + ctx.ip());
            ctx.status(200);
            ctx.result("OK");
        });

        // Registra toda chamada da API após processar a rota
        app.after(ctx -> {
            if (ctx.path().startsWith("/api/")) {
                LoggerUtil.api(ctx.method().name(), ctx.path(), ctx.statusCode());
            }
        });

        // Captura erros não tratados da aplicação
        app.exception(Exception.class, (e, ctx) -> {
            LoggerUtil.error("Erro inesperado em " + ctx.method() + " " + ctx.path() + ": " + e.getMessage());
            LoggerUtil.application("ERROR", "Falha não tratada: " + e.getMessage());
            ctx.status(500);
            ctx.json(new ErroResponse("Erro interno no servidor"));
        });

        // GET - LISTAR APENAS OS ATIVOS (Para a tabela de projetos - esconde os cancelados)
        app.get("/api/projetos", ctx -> {
            List<Projeto> projetosAtivos = service.listarAtivos();
            ctx.json(projetosAtivos);
        });

        // GET - LISTAR TODOS (Ativos e Cancelados - ideal para o Dashboard / Estatísticas)
        app.get("/api/projetos/todos", ctx -> {
            List<Projeto> todosProjetos = service.listar();
            ctx.json(todosProjetos);
        });

        // GET - BUSCAR POR ID
        app.get("/api/projetos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Projeto projeto = service.buscarPorId(id);
            if (projeto == null) {
                LoggerUtil.error("GET /api/projetos/" + id + " -> Projeto não encontrado (404)");
                ctx.status(404);
                return;
            }
            ctx.json(projeto);
        });

        // POST - CADASTRAR
        app.post("/api/projetos", ctx -> {
            Projeto projeto = ctx.bodyAsClass(Projeto.class);

            if (projeto.getNome() == null || projeto.getNome().isBlank()) {
                LoggerUtil.error("POST /api/projetos -> Tentativa de cadastro sem nome (400)");
                ctx.status(400);
                ctx.json(new ErroResponse("Nome é obrigatório"));
                return;
            }

            boolean adicionado = service.adicionar(projeto);
            if (!adicionado) {
                LoggerUtil.error("POST /api/projetos -> Falha ao adicionar projeto '" + projeto.getNome() + "' (400)");
                ctx.status(400);
                ctx.json(new ErroResponse("Erro ao cadastrar"));
                return;
            }

            LoggerUtil.audit("PROJETO " + projeto.getId() + " CRIADO (" + projeto.getNome() + ")");
            ctx.status(201);
            ctx.json(projeto);
        });

        // PUT - ALTERAR
        app.put("/api/projetos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Projeto projeto = ctx.bodyAsClass(Projeto.class);
            projeto.setId(id);

            boolean alterou = service.alterar(projeto);
            if (!alterou) {
                LoggerUtil.error("PUT /api/projetos/" + id + " -> Projeto não encontrado para alteração (404)");
                ctx.status(404);
                return;
            }

            LoggerUtil.audit("PROJETO " + projeto.getId() + " ALTERADO (" + projeto.getNome() + ")");
            ctx.json(projeto);
        });

        // DELETE - EXCLUIR (Exclusão lógica: muda o status para "Cancelado")
        app.delete("/api/projetos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean removido = service.removerPorId(id);
            if (!removido) {
                LoggerUtil.error("DELETE /api/projetos/" + id + " -> Projeto não encontrado para exclusão (404)");
                ctx.status(404);
                return;
            }

            LoggerUtil.audit("PROJETO " + id + " EXCLUIDO");
            ctx.status(204);
        });

        System.out.println("Servidor Javalin iniciado na porta 7070...");
    }
}

class ErroResponse {
    private String erro;

    public ErroResponse(String erro) {
        this.erro = erro;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }
}