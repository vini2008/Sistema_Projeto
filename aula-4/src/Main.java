import model.Projeto;
import service.ProjetoService;

public class Main {
    public static void main(String[] args) {
        try {
            ProjetoService service = new ProjetoService();
            
            // 1. Carrega os dados existentes do CSV
            service.carregar();
            System.out.println("Projetos carregados: " + service.listar().size());

            // 2. Cria e adiciona um novo projeto de teste
            Projeto p1 = new Projeto(
                1,
                "Sistema Acadêmico",
                "Gerenciamento de alunos",
                "Software",
                "Em desenvolvimento"
            );

            if (service.adicionar(p1)) {
                // 3. Salva a lista atualizada no arquivo CSV
                service.salvar();
                System.out.println("Projeto adicionado e salvo com sucesso!");
            } else {
                System.out.println("Projeto já existe ou dados inválidos.");
            }

            // 4. Imprime os projetos na tela
            for (Projeto p : service.listar()) {
                p.exibirDados();
            }

        } catch (Exception e) {
            System.err.println("Erro durante a execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
}