package api;

import java.util.List;

import io.javalin.Javalin;
import model.Projeto;
import service.ProjetoService;

public class Api {
    public static void main(String[] args) {
        ProjetoService service = new ProjetoService();

        var app = Javalin.create(config -> {
            
            // ROTA INICIAL
            config.routes.get("/", ctx -> {
                ctx.result("API Sistema de Projetos");
            });

            // GET - LISTAR
            config.routes.get("/api/projetos", ctx -> {
                List<Projeto> projetos = service.listar();
                ctx.json(projetos);
            });

            // GET - BUSCAR POR ID
            config.routes.get("/api/projetos/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Projeto projeto = service.buscarPorId(id);
                if (projeto == null) {
                    ctx.status(404);
                    return;
                }
                ctx.json(projeto);
            });

            // POST - CADASTRAR
            config.routes.post("/api/projetos", ctx -> {
                Projeto projeto = ctx.bodyAsClass(Projeto.class);
                
                if (projeto.getNome() == null || projeto.getNome().isBlank()) {
                    ctx.status(400);
                    ctx.json(new ErroResponse("Nome é obrigatório"));
                    return;
                }

                service.adicionar(projeto);
                service.salvar();
                ctx.status(201);
                ctx.json(projeto);
            });

            // PUT - ALTERAR
            config.routes.put("/api/projetos/{id}", ctx -> {
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
            config.routes.delete("/api/projetos/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Projeto projeto = service.buscarPorId(id);
                if (projeto == null) {
                    ctx.status(404);
                    return;
                }

                service.removerPorId(id);
                service.salvar();
                ctx.status(204);
            });

        }).start(7070);

        System.out.println("Servidor Javalin iniciado na porta 7070...");
    }
}

// Classe auxiliar de erro mantida no mesmo ficheiro
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