package api;

import io.javalin.Javalin;
import model.Projeto;
import service.ProjetoService;

import java.util.List;

public class Api {
    public static void main(String[] args) {
        ProjetoService service = new ProjetoService();

        var app = Javalin.create(config -> {
            // Rota inicial / raiz
            config.routes.get("/", ctx -> {
                ctx.result("API Sistema de Projetos");
            });

            // Endpoint para listar todos os projetos
            config.routes.get("/api/projetos", ctx -> {
                List<Projeto> projetos = service.listar();
                ctx.json(projetos);
            });

            // Endpoint para buscar projeto por ID
            config.routes.get("/api/projetos/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Projeto projeto = service.buscarPorId(id);
                
                if (projeto == null) {
                    ctx.status(404);
                    ctx.result("Projeto não encontrado");
                    return;
                }
                
                ctx.json(projeto);
            });
        }).start(7070);

        System.out.println("Servidor rodando em http://localhost:7070");
    }
}