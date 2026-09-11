import model.Projeto;
public class Main{
public static void main(String[] args) {
    Projeto projeto1 = new Projeto(
            1,
            "Sistema Acadêmico",
            "Sistema para gerenciamento acadêmico",
            "Software",
            "Em desenvolvimento"
        );

        Projeto projeto2 = new Projeto(
            2,
            "Site Institucional",
            "Website de uma instituição",
            "Web",
            "Concluído"
        );
        Projeto projeto3 = new Projeto(
            3,
            " Aplicativo Mobile",
            "Web",
            "Software",
            "Planejado"
        );

        System.out.println("PROJETO 1");
        System.out.println(projeto1.getNome());
        System.out.println(projeto1.getCategoria());
        System.out.println(projeto1.getStatus());
        projeto1.exibirDados();

        System.out.println();

        System.out.println("PROJETO 2");
        System.out.println(projeto2.getNome());
        System.out.println(projeto2.getCategoria());
        System.out.println(projeto2.getStatus());
        projeto1.exibirDados();

        System.out.println("PROJETO 3");
        System.out.println(projeto3.getNome());
        System.out.println(projeto3.getCategoria());
        System.out.println(projeto3.getStatus());
        projeto1.exibirDados();
}
}