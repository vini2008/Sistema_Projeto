package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
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
    private JButton botaoSalvar;
    private JButton botaoModificar;
    private JButton botaoExcluir;
    private JButton botaoLimpar;

    // Labels para os cartões de estatísticas do topo
    private JLabel lblTotalCount = new JLabel("0");
    private JLabel lblConcluidosCount = new JLabel("0");
    private JLabel lblAndamentoCount = new JLabel("0");
    private JLabel lblPlanejadosCount = new JLabel("0");

    private ApiClient apiClient;
    private int idSelecionado = -1;

    public TelaProjetos() {
        setTitle("Sistema de Projetos - Dashboard Corporativo");
        setSize(1150, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        apiClient = new ApiClient();

        initComponents();
        initEvents();
        carregarDados();

        // ⏱️ Temporizador para atualizar a tabela e os cartões automaticamente a cada 5 segundos
        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> carregarDados());
        timer.start();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. MENU LATERAL ESQUERDO (Sidebar)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(24, 43, 73)); // Azul escuro corporativo
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblLogo = new JLabel("<html><b>Sistema</b><br>de Projetos</html>");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblLogo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        String[] menus = {" Dashboard", " Projetos", " Categorias", " Relatórios", " Configurações"};
        for (String menu : menus) {
            JButton btnMenu = new JButton(menu);
            btnMenu.setMaximumSize(new Dimension(190, 40));
            btnMenu.setHorizontalAlignment(SwingConstants.LEFT);
            btnMenu.setForeground(menu.contains("Projetos") ? Color.WHITE : new Color(180, 195, 215));
            btnMenu.setBackground(menu.contains("Projetos") ? new Color(41, 98, 175) : new Color(24, 43, 73));
            btnMenu.setFocusPainted(false);
            btnMenu.setBorderPainted(false);
            btnMenu.setFont(new Font("Segoe UI", menu.contains("Projetos") ? Font.BOLD : Font.PLAIN, 14));
            sidebar.add(btnMenu);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        add(sidebar, BorderLayout.WEST);

        // 2. PAINEL PRINCIPAL DIREITO
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainContent.setBackground(new Color(245, 247, 250));

        // 2.1 PAINEL DE CARTÕES DE ESTATÍSTICAS (Topo)
        JPanel panelCards = new JPanel(new GridLayout(1, 4, 15, 0));
        panelCards.setBackground(new Color(245, 247, 250));
        panelCards.setPreferredSize(new Dimension(getWidth(), 85));

        panelCards.add(criarCardEstatistica("Total de Projetos", lblTotalCount, new Color(41, 98, 175)));
        panelCards.add(criarCardEstatistica("Concluídos", lblConcluidosCount, new Color(40, 167, 69)));
        panelCards.add(criarCardEstatistica("Em Andamento", lblAndamentoCount, new Color(255, 193, 7)));
        panelCards.add(criarCardEstatistica("Planejados", lblPlanejadosCount, new Color(111, 66, 193)));

        mainContent.add(panelCards, BorderLayout.NORTH);

        // 2.2 PAINEL CENTRAL (Formulário à Esquerda + Tabela à Direita)
        JPanel panelWorkspace = new JPanel(new GridLayout(1, 2, 15, 0));
        panelWorkspace.setBackground(new Color(245, 247, 250));

        // Formulário de Cadastro / Edição
        JPanel panelFormCard = new JPanel(new BorderLayout());
        panelFormCard.setBackground(Color.WHITE);
        panelFormCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblFormTitle = new JLabel("Cadastro de Projeto");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(new Color(30, 30, 30));
        panelFormCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel panelCampos = new JPanel(new GridLayout(8, 1, 5, 5));
        panelCampos.setBackground(Color.WHITE);
        panelCampos.setBorder(new EmptyBorder(15, 0, 15, 0));

        campoNome = new JTextField();
        campoDescricao = new JTextField();
        comboCategoria = new JComboBox<>(new String[]{"Web", "Software", "Mobile", "Outro"});
        comboStatus = new JComboBox<>(new String[]{"Planejado", "Em desenvolvimento", "Concluído"});

        panelCampos.add(new JLabel("Nome do Projeto *"));
        panelCampos.add(campoNome);
        panelCampos.add(new JLabel("Descrição"));
        panelCampos.add(campoDescricao);
        panelCampos.add(new JLabel("Categoria"));
        panelCampos.add(comboCategoria);
        panelCampos.add(new JLabel("Status"));
        panelCampos.add(comboStatus);

        panelFormCard.add(panelCampos, BorderLayout.CENTER);

        // Botões de Ação do Formulário
        JPanel panelBotoes = new JPanel(new GridLayout(1, 3, 8, 0));
        panelBotoes.setBackground(Color.WHITE);

        botaoSalvar = new JButton("Salvar");
        botaoSalvar.setBackground(new Color(0, 123, 255));
        botaoSalvar.setForeground(Color.WHITE);
        botaoSalvar.setFocusPainted(false);

        botaoModificar = new JButton("Modificar");
        botaoModificar.setBackground(new Color(23, 162, 184));
        botaoModificar.setForeground(Color.WHITE);
        botaoModificar.setFocusPainted(false);

        botaoExcluir = new JButton("Excluir");
        botaoExcluir.setBackground(new Color(220, 53, 69));
        botaoExcluir.setForeground(Color.WHITE);
        botaoExcluir.setFocusPainted(false);

        panelBotoes.add(botaoSalvar);
        panelBotoes.add(botaoModificar);
        panelBotoes.add(botaoExcluir);

        panelFormCard.add(panelBotoes, BorderLayout.SOUTH);
        panelWorkspace.add(panelFormCard);

        // Tabela de Listagem
        JPanel panelTableCard = new JPanel(new BorderLayout());
        panelTableCard.setBackground(Color.WHITE);
        panelTableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblTableTitle = new JLabel("Lista de Projetos");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTableTitle.setForeground(new Color(30, 30, 30));
        panelTableCard.add(lblTableTitle, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new Object[]{"ID", "Nome do Projeto", "Categoria", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tabela);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 240)));
        panelTableCard.add(scrollPane, BorderLayout.CENTER);

        // Botão Limpar em baixo da tabela
        botaoLimpar = new JButton("Limpar Seleção / Formulário");
        botaoLimpar.setFocusPainted(false);
        panelTableCard.add(botaoLimpar, BorderLayout.SOUTH);

        panelWorkspace.add(panelTableCard);
        mainContent.add(panelWorkspace, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel criarCardEstatistica(String titulo, JLabel lblValor, Color corBorda) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, corBorda),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(100, 110, 120));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValor.setForeground(new Color(40, 40, 40));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private void initEvents() {
        botaoSalvar.addActionListener(e -> cadastrar());
        botaoModificar.addActionListener(e -> modificar());
        botaoExcluir.addActionListener(e -> excluir());
        botaoLimpar.addActionListener(e -> limparFormulario());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linha = tabela.getSelectedRow();
                if (linha != -1) {
                    idSelecionado = Integer.parseInt(modelo.getValueAt(linha, 0).toString());
                    campoNome.setText(modelo.getValueAt(linha, 1).toString());
                    comboCategoria.setSelectedItem(modelo.getValueAt(linha, 2).toString());
                    comboStatus.setSelectedItem(modelo.getValueAt(linha, 3).toString());
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
        String nome = campoNome.getText().trim();
        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do projeto.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Projeto p = new Projeto(0, nome, campoDescricao.getText(), comboCategoria.getSelectedItem().toString(), comboStatus.getSelectedItem().toString());
            apiClient.cadastrarProjeto(p);
            JOptionPane.showMessageDialog(this, "Projeto cadastrado com sucesso!");
            limparFormulario();
            carregarDados();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificar() {
        if (idSelecionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um projeto na tabela para modificar.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nome = campoNome.getText().trim();
        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "O nome não pode estar vazio.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Projeto p = new Projeto(idSelecionado, nome, campoDescricao.getText(), comboCategoria.getSelectedItem().toString(), comboStatus.getSelectedItem().toString());
            apiClient.atualizarProjeto(p);
            JOptionPane.showMessageDialog(this, "Projeto modificado com sucesso!");
            limparFormulario();
            carregarDados();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao modificar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir() {
        if (idSelecionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um projeto na tabela para excluir.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int op = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir este projeto?", "Confirmação", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            try {
                apiClient.deletarProjeto(idSelecionado);
                JOptionPane.showMessageDialog(this, "Projeto excluído com sucesso!");
                limparFormulario();
                carregarDados();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarDados() {
        new SwingWorker<List<Projeto>, Void>() {
            @Override
            protected List<Projeto> doInBackground() throws Exception {
                return apiClient.listarProjetos();
            }
            @Override
            protected void done() {
                try {
                    List<Projeto> lista = get();
                    modelo.setRowCount(0);
                    
                    int concluidos = 0;
                    int andamento = 0;
                    int planejados = 0;

                    for (Projeto p : lista) {
                        modelo.addRow(new Object[]{p.getId(), p.getNome(), p.getCategoria(), p.getStatus()});
                        
                        if ("Concluído".equalsIgnoreCase(p.getStatus())) concluidos++;
                        else if ("Em desenvolvimento".equalsIgnoreCase(p.getStatus())) andamento++;
                        else if ("Planejado".equalsIgnoreCase(p.getStatus())) planejados++;
                    }

                    lblTotalCount.setText(String.valueOf(lista.size()));
                    lblConcluidosCount.setText(String.valueOf(concluidos));
                    lblAndamentoCount.setText(String.valueOf(andamento));
                    lblPlanejadosCount.setText(String.valueOf(planejados));

                } catch (Exception ex) {
                    // Ignora erros silenciados de conexão periódica do timer para não pipocar pop-ups se a API cair um segundo
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Erro ao carregar o FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            new TelaProjetos().setVisible(true);
        });
    }
}