package api;

import java.util.List;
import io.javalin.Javalin;
import model.Projeto;
import service.ProjetoService;

public class Api {
    public static void main(String[] args) {
        ProjetoService service = new ProjetoService();
        service.carregar(); // Carrega os dados salvos no CSV ao iniciar

        var app = Javalin.create().start(7070);

        // ROTA INICIAL
        app.get("/", ctx -> ctx.result("API Sistema de Projetos"));

        // GET - LISTAR
        app.get("/api/projetos", ctx -> {
            List<Projeto> projetos = service.listar();
            ctx.json(projetos);
        });

        // GET - BUSCAR POR ID
        app.get("/api/projetos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Projeto projeto = service.buscarPorId(id);
            if (projeto == null) {
                ctx.status(404);
                return;
            }
            ctx.json(projeto);
        });

        // POST - CADASTRAR
        app.post("/api/projetos", ctx -> {
            Projeto projeto = ctx.bodyAsClass(Projeto.class);
            
            if (projeto.getNome() == null || projeto.getNome().isBlank()) {
                ctx.status(400);
                ctx.json(new ErroResponse("Nome é obrigatório"));
                return;
            }

            boolean adicionado = service.adicionar(projeto);
            if (!adicionado) {
                ctx.status(400);
                ctx.json(new ErroResponse("ID já existente ou erro ao cadastrar"));
                return;
            }

            service.salvar();
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
                ctx.status(404);
                return;
            }

            service.salvar();
            ctx.json(projeto);
        });

        // DELETE - EXCLUIR
        app.delete("/api/projetos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean removido = service.removerPorId(id);
            if (!removido) {
                ctx.status(404);
                return;
            }

            service.salvar();
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