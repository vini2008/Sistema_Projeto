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
    private JButton botaoModificar;
    private JButton botaoExcluir;
    private JButton botaoLimpar;
    
    private ApiClient apiClient;
    private int idSelecionado = -1;

    public TelaProjetos() {
        setTitle("Sistema de Projetos - Interface Gráfica (API)");
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        apiClient = new ApiClient();

        criarComponentes();
        criarEventos();
        carregarTabela();
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
        botaoModificar = new JButton("Modificar");
        botaoExcluir = new JButton("Excluir");
        botaoLimpar = new JButton("Limpar");

        modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nome");
        modelo.addColumn("Categoria");
        modelo.addColumn("Status");
        tabela = new JTable(modelo);

        JPanel painelFormulario = new JPanel(new GridLayout(4, 2, 5, 5));
        painelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        painelFormulario.add(new JLabel("Nome:"));
        painelFormulario.add(campoNome);
        
        painelFormulario.add(new JLabel("Descrição:"));
        painelFormulario.add(campoDescricao);
        
        painelFormulario.add(new JLabel("Categoria:"));
        painelFormulario.add(comboCategoria);
        
        painelFormulario.add(new JLabel("Status:"));
        painelFormulario.add(comboStatus);

        // Painel com 4 botões lado a lado
        JPanel painelBotoes = new JPanel(new GridLayout(1, 4, 5, 5));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        painelBotoes.add(botaoCadastrar);
        painelBotoes.add(botaoModificar);
        painelBotoes.add(botaoExcluir);
        painelBotoes.add(botaoLimpar);

        JPanel painelNorte = new JPanel(new BorderLayout());
        painelNorte.add(painelFormulario, BorderLayout.CENTER);
        painelNorte.add(painelBotoes, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(painelNorte, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    private void criarEventos() {
        botaoLimpar.addActionListener(e -> limparFormulario());
        botaoCadastrar.addActionListener(e -> cadastrar());
        botaoModificar.addActionListener(e -> modificar());
        botaoExcluir.addActionListener(e -> excluir());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linhaSelecionada = tabela.getSelectedRow();
                if (linhaSelecionada != -1) {
                    idSelecionado = Integer.parseInt(tabela.getValueAt(linhaSelecionada, 0).toString());
                    campoNome.setText(tabela.getValueAt(linhaSelecionada, 1).toString());
                    comboCategoria.setSelectedItem(tabela.getValueAt(linhaSelecionada, 2).toString());
                    comboStatus.setSelectedItem(tabela.getValueAt(linhaSelecionada, 3).toString());
                }
            }
        });
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        idSelecionado = -1;
        tabela.clearSelection();
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
            Projeto projeto = new Projeto(0, nome, descricao, categoria, status);
            apiClient.cadastrarProjeto(projeto);
            JOptionPane.showMessageDialog(this, "Projeto cadastrado com sucesso!");
            limparFormulario();
            carregarTabela();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar projeto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificar() {
        if (idSelecionado == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto na tabela para modificar.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String categoria = comboCategoria.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do projeto.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Projeto projeto = new Projeto(idSelecionado, nome, descricao, categoria, status);
            apiClient.atualizarProjeto(projeto);
            JOptionPane.showMessageDialog(this, "Projeto modificado com sucesso!");
            limparFormulario();
            carregarTabela();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao modificar projeto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir() {
        if (idSelecionado == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto na tabela para excluir.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir o projeto selecionado?", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                apiClient.deletarProjeto(idSelecionado);
                JOptionPane.showMessageDialog(this, "Projeto excluído com sucesso!");
                limparFormulario();
                carregarTabela();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir projeto: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarTabela() {
        new SwingWorker<List<Projeto>, Void>() {
            @Override
            protected List<Projeto> doInBackground() throws Exception {
                return apiClient.listarProjetos();
            }

            @Override
            protected void done() {
                try {
                    List<Projeto> projetos = get();
                    modelo.setRowCount(0);
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