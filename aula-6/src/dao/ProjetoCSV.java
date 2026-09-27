package dao;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import model.Projeto;

public class ProjetoCSV {
    private String caminhoArquivo = "dados/projetos.csv";

    public ArrayList<Projeto> carregar() {
        ArrayList<Projeto> projetos = new ArrayList<>();
        File arquivo = new File(caminhoArquivo);
        
        if (!arquivo.exists()) {
            return projetos;
        }

        try (Scanner scanner = new Scanner(arquivo)) {
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();
                String[] partes = linha.split(";");
                if (partes.length == 5) {
                    int id = Integer.parseInt(partes[0]);
                    Projeto p = new Projeto(id, partes[1], partes[2], partes[3], partes[4]);
                    projetos.add(p);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar o arquivo CSV: " + e.getMessage());
        }

        return projetos;
    }

    public void salvar(ArrayList<Projeto> projetos) {
        File diretorio = new File("dados");
        if (!diretorio.exists()) {
            diretorio.mkdir();
        }

        try (PrintWriter pw = new PrintWriter(new File(caminhoArquivo))) {
            for (Projeto p : projetos) {
                pw.println(p.toCsv());
            }
        } catch (Exception e) {
            System.out.println("Erro ao salvar o arquivo CSV: " + e.getMessage());
        }
    }
}