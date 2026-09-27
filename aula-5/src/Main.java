import java.util.Scanner;
import model.Projeto;
import service.ProjetoService;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        ProjetoService service = new ProjetoService();
        service.carregar();

        int opcao = -1;

        while (opcao != 0) {
            System.out.println("\n========================================");
            System.out.println("SISTEMA DE PROJETOS (Total: " + service.listar().size() + ")");
            System.out.println("========================================");
            System.out.println("1 - Listar projetos");
            System.out.println("2 - Buscar projeto por ID");
            System.out.println("3 - Cadastrar projeto");
            System.out.println("4 - Alterar projeto");
            System.out.println("5 - Excluir projeto");
            System.out.println("6 - Buscar por categoria");
            System.out.println("7 - Buscar por status");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");

            opcao = scanner.nextInt();
            scanner.nextLine(); // Limpa o buffer

            switch (opcao) {
                case 1:
                    System.out.println("\n--- LISTA DE PROJETOS ---");
                    if (service.listar().isEmpty()) {
                        System.out.println("Nenhum projeto cadastrado.");
                    } else {
                        for (Projeto projeto : service.listar()) {
                            projeto.exibirDados();
                        }
                    }
                    break;

                case 2:
                    System.out.print("Digite o ID: ");
                    int idBusca = scanner.nextInt();
                    scanner.nextLine();
                    Projeto encontrado = service.buscarPorId(idBusca);
                    if (encontrado != null) {
                        System.out.println("\nProjeto encontrado:");
                        encontrado.exibirDados();
                    } else {
                        System.out.println("Projeto não encontrado.");
                    }
                    break;

                case 3:
                    System.out.println("\n--- CADASTRO DE PROJETO ---");
                    System.out.print("ID: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();

                    System.out.print("Nome: ");
                    String nome = scanner.nextLine();
                    if (nome == null || nome.isBlank()) {
                        System.out.println("O nome é obrigatório.");
                        break;
                    }

                    System.out.print("Descrição: ");
                    String descricao = scanner.nextLine();

                    System.out.print("Categoria: ");
                    String categoria = scanner.nextLine();
                    if (categoria.isBlank()) {
                        System.out.println("A categoria é obrigatória.");
                        break;
                    }

                    System.out.print("Status: ");
                    String status = scanner.nextLine();

                    Projeto projeto = new Projeto(id, nome, descricao, categoria, status);
                    boolean cadastrado = service.adicionar(projeto);

                    if (cadastrado) {
                        service.salvar();
                        System.out.println("Projeto cadastrado com sucesso.");
                    } else {
                        System.out.println("Não foi possível cadastrar (ID já existente).");
                    }
                    break;

                case 4:
                    System.out.println("\n--- ALTERAÇÃO DE PROJETO ---");
                    System.out.print("ID do projeto: ");
                    int idAlterar = scanner.nextInt();
                    scanner.nextLine();

                    Projeto existente = service.buscarPorId(idAlterar);
                    if (existente == null) {
                        System.out.println("Projeto não encontrado.");
                        break;
                    }

                    System.out.println("\nProjeto atual:");
                    existente.exibirDados();

                    System.out.print("Novo nome: ");
                    String novoNome = scanner.nextLine();
                    System.out.print("Nova descrição: ");
                    String novaDescricao = scanner.nextLine();
                    System.out.print("Nova categoria: ");
                    String novaCategoria = scanner.nextLine();
                    System.out.print("Novo status: ");
                    String novoStatus = scanner.nextLine();

                    Projeto atualizado = new Projeto(idAlterar, novoNome, novaDescricao, novaCategoria, novoStatus);
                    boolean alterado = service.alterar(atualizado);

                    if (alterado) {
                        service.salvar();
                        System.out.println("Projeto alterado com sucesso.");
                    } else {
                        System.out.println("Erro ao alterar.");
                    }
                    break;

                case 5:
                    System.out.println("\n--- EXCLUSÃO DE PROJETO ---");
                    System.out.print("ID do projeto: ");
                    int idExcluir = scanner.nextInt();
                    scanner.nextLine();

                    Projeto projetoExcluir = service.buscarPorId(idExcluir);
                    if (projetoExcluir == null) {
                        System.out.println("Projeto não encontrado.");
                        break;
                    }

                    System.out.println("\nProjeto que será excluído:");
                    projetoExcluir.exibirDados();

                    System.out.print("Confirma exclusão? (S/N): ");
                    String confirmacao = scanner.nextLine();

                    if (confirmacao.equalsIgnoreCase("S")) {
                        boolean removido = service.removerPorId(idExcluir);
                        if (removido) {
                            service.salvar();
                            System.out.println("Projeto excluído com sucesso.");
                        }
                    } else {
                        System.out.println("Exclusão cancelada.");
                    }
                    break;

                case 6:
                    System.out.print("Digite a categoria: ");
                    String catBusca = scanner.nextLine();
                    var listaCat = service.buscarPorCategoria(catBusca);
                    if (listaCat.isEmpty()) {
                        System.out.println("Nenhum projeto encontrado nesta categoria.");
                    } else {
                        for (var p : listaCat) {
                            p.exibirDados();
                        }
                    }
                    break;

                case 7:
                    System.out.print("Digite o status: ");
                    String statusBusca = scanner.nextLine();
                    var listaStatus = service.buscarPorStatus(statusBusca);
                    if (listaStatus.isEmpty()) {
                        System.out.println("Nenhum projeto encontrado com este status.");
                    } else {
                        for (var p : listaStatus) {
                            p.exibirDados();
                        }
                    }
                    break;

                case 0:
                    System.out.println("Encerrando o sistema...");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        }
        scanner.close();
    }
}