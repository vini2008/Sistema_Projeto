package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import client.ApiClient;
import model.Projeto;

public class TelaProjetos extends JFrame {
    private JTextField campoNome;
    private JTextField campoDescricao;
    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;
    private JTable tabela;
    private DefaultTableModel modelo;
    private JButton botaoCadastrar;
    private JButton botaoLimpar;
    
    private ApiClient apiClient; // Substituiu o ProjetoService

    public TelaProjetos() {
        setTitle("Sistema de Projetos - Interface Gráfica (API)");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        apiClient = new ApiClient(); // Inicializa o cliente HTTP

        criarComponentes();
        criarEventos();
        carregarTabela(); // Carrega assincronamente da API
    }

    private void criarComponentes() {
        campoNome = new JTextField(20);
        campoDescricao = new JTextField(20);
        
        comboCategoria = new JComboBox<>();
        comboCategoria.addItem("Web");
        comboCategoria.addItem("Software");
        comboCategoria.addItem("Mobile");
        comboCategoria.addItem("Outro");

        comboStatus = new JComboBox<>();
        comboStatus.addItem("Planejado");
        comboStatus.addItem("Em desenvolvimento");
        comboStatus.addItem("Concluído");

        botaoCadastrar = new JButton("Cadastrar");
        botaoLimpar = new JButton("Limpar");

        modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nome");
        modelo.addColumn("Categoria");
        modelo.addColumn("Status");
        tabela = new JTable(modelo);

        JPanel painelFormulario = new JPanel(new GridLayout(5, 2, 5, 5));
        painelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        painelFormulario.add(new JLabel("Nome:"));
        painelFormulario.add(campoNome);
        
        painelFormulario.add(new JLabel("Descrição:"));
        painelFormulario.add(campoDescricao);
        
        painelFormulario.add(new JLabel("Categoria:"));
        painelFormulario.add(comboCategoria);
        
        painelFormulario.add(new JLabel("Status:"));
        painelFormulario.add(comboStatus);
        
        painelFormulario.add(botaoCadastrar);
        painelFormulario.add(botaoLimpar);

        setLayout(new BorderLayout());
        add(painelFormulario, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    private void criarEventos() {
        botaoLimpar.addActionListener(e -> limparFormulario());
        botaoCadastrar.addActionListener(e -> cadastrar());
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        campoNome.requestFocus();
    }

    private void cadastrar() {
        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String categoria = comboCategoria.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do projeto.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // ID 0: o servidor da API vai gerar e persistir o ID real automaticamente
            Projeto projeto = new Projeto(0, nome, descricao, categoria, status);
            
            apiClient.cadastrarProjeto(projeto);
            JOptionPane.showMessageDialog(this, "Projeto cadastrado com sucesso!");
            limparFormulario();
            carregarTabela();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar projeto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarTabela() {
        // SwingWorker roda a requisição HTTP em segundo plano para não congelar a tela
        new SwingWorker<List<Projeto>, Void>() {
            @Override
            protected List<Projeto> doInBackground() throws Exception {
                return apiClient.listarProjetos();
            }

            @Override
            protected void done() {
                try {
                    List<Projeto> projetos = get();
                    modelo.setRowCount(0); // Limpa a tabela antes de preencher
                    for (Projeto projeto : projetos) {
                        modelo.addRow(new Object[]{
                            projeto.getId(),
                            projeto.getNome(),
                            projeto.getCategoria(),
                            projeto.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TelaProjetos.this, "Erro ao carregar projetos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaProjetos tela = new TelaProjetos();
            tela.setVisible(true);
        });
    }
}