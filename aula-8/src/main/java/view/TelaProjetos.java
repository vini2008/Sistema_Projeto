package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private JButton btnMenuDashboard;
    private JButton btnMenuProjetos;

    private CardLayout cardLayoutCentro;
    private JPanel painelCentro;

    private PainelGraficoStatus graficoStatus;
    private PainelGraficoCategoria graficoCategoria;
    private PainelGraficoMensal graficoMensal;

    // 4 indicadores do topo
    private JLabel lblTotalCount = new JLabel("0");
    private JLabel lblConcluidosCount = new JLabel("0");
    private JLabel lblAndamentoCount = new JLabel("0");
    private JLabel lblCanceladosCount = new JLabel("0");

    private ApiClient apiClient;
    private int idSelecionado = -1;
    private List<Projeto> cacheTodosProjetos = new ArrayList<>();

    public TelaProjetos() {
        setTitle("Sistema de Projetos - Dashboard Corporativo");
        setSize(1150, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        apiClient = new ApiClient();
        new Thread(() -> apiClient.registrarAcesso("desktop")).start();

        initComponents();
        initEvents();
        carregarDados();

        // Temporizador para atualizar a cada 10 segundos
        javax.swing.Timer timer = new javax.swing.Timer(10000, e -> carregarDados());
        timer.start();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. MENU LATERAL ESQUERDO (Sidebar)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(24, 43, 73));
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblLogo = new JLabel("<html><b>Sistema</b><br>de Projetos</html>");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblLogo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        btnMenuDashboard = criarBotaoMenu(" Dashboard", false);
        btnMenuProjetos = criarBotaoMenu(" Projetos", true);

        sidebar.add(btnMenuDashboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnMenuProjetos);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] menusExtras = {" Categorias", " Relatórios", " Configurações"};
        for (String menu : menusExtras) {
            sidebar.add(criarBotaoMenu(menu, false));
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        add(sidebar, BorderLayout.WEST);

        // 2. PAINEL PRINCIPAL DIREITO
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainContent.setBackground(new Color(245, 247, 250));

        // 2.1 PAINEL DE CARTÕES DE ESTATÍSTICAS (4 indicadores)
        JPanel panelCards = new JPanel(new GridLayout(1, 4, 15, 0));
        panelCards.setBackground(new Color(245, 247, 250));
        panelCards.setPreferredSize(new Dimension(getWidth(), 85));

        panelCards.add(criarCardEstatistica("Total de Projetos", lblTotalCount, new Color(41, 98, 175)));
        panelCards.add(criarCardEstatistica("Concluídos", lblConcluidosCount, new Color(40, 167, 69)));
        panelCards.add(criarCardEstatistica("Em Andamento", lblAndamentoCount, new Color(255, 193, 7)));
        panelCards.add(criarCardEstatistica("Cancelados", lblCanceladosCount, new Color(220, 53, 69)));

        mainContent.add(panelCards, BorderLayout.NORTH);

        // 2.2 ÁREA CENTRAL COM CARDLAYOUT (Projetos x Dashboard)
        cardLayoutCentro = new CardLayout();
        painelCentro = new JPanel(cardLayoutCentro);
        painelCentro.setBackground(new Color(245, 247, 250));

        painelCentro.add(criarPainelProjetos(), "PROJETOS");
        painelCentro.add(criarPainelDashboard(), "DASHBOARD");

        cardLayoutCentro.show(painelCentro, "PROJETOS");
        mainContent.add(painelCentro, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private JButton criarBotaoMenu(String texto, boolean ativo) {
        JButton btnMenu = new JButton(texto);
        btnMenu.setMaximumSize(new Dimension(190, 40));
        btnMenu.setHorizontalAlignment(SwingConstants.LEFT);
        btnMenu.setFocusPainted(false);
        btnMenu.setBorderPainted(false);
        estilizarBotaoMenu(btnMenu, ativo);
        return btnMenu;
    }

    private void estilizarBotaoMenu(JButton botao, boolean ativo) {
        botao.setForeground(ativo ? Color.WHITE : new Color(180, 195, 215));
        botao.setBackground(ativo ? new Color(41, 98, 175) : new Color(24, 43, 73));
        botao.setFont(new Font("Segoe UI", ativo ? Font.BOLD : Font.PLAIN, 14));
    }

    private void alternarAba(String aba) {
        cardLayoutCentro.show(painelCentro, aba);
        boolean ehDashboard = "DASHBOARD".equals(aba);
        estilizarBotaoMenu(btnMenuDashboard, ehDashboard);
        estilizarBotaoMenu(btnMenuProjetos, !ehDashboard);
        if (ehDashboard) {
            atualizarGraficos(cacheTodosProjetos);
        }
    }

    private JPanel criarPainelProjetos() {
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
        comboCategoria = new JComboBox<>(new String[]{"Web", "Software", "Mobile", "Desktop", "Outro"});
        comboStatus = new JComboBox<>(new String[]{"Planejado", "Em desenvolvimento", "Concluído", "Cancelado"});

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

        // Tabela de Listagem (somente ativos)
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

        botaoLimpar = new JButton("Limpar Seleção / Formulário");
        botaoLimpar.setFocusPainted(false);
        panelTableCard.add(botaoLimpar, BorderLayout.SOUTH);

        panelWorkspace.add(panelTableCard);
        return panelWorkspace;
    }

    private JPanel criarPainelDashboard() {
        JPanel gridGraficos = new JPanel(new GridLayout(1, 3, 15, 0));
        gridGraficos.setBackground(new Color(245, 247, 250));

        graficoStatus = new PainelGraficoStatus();
        graficoCategoria = new PainelGraficoCategoria();
        graficoMensal = new PainelGraficoMensal();

        gridGraficos.add(envolucrarCardGrafico("Status dos Projetos", graficoStatus));
        gridGraficos.add(envolucrarCardGrafico("Projetos por Categoria", graficoCategoria));
        gridGraficos.add(envolucrarCardGrafico("Evolução Mensal", graficoMensal));

        return gridGraficos;
    }

    private JPanel envolucrarCardGrafico(String titulo, JPanel grafico) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(30, 30, 30));

        card.add(lbl, BorderLayout.NORTH);
        card.add(grafico, BorderLayout.CENTER);
        return card;
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
        btnMenuDashboard.addActionListener(e -> alternarAba("DASHBOARD"));
        btnMenuProjetos.addActionListener(e -> alternarAba("PROJETOS"));

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

    private static class ResultadoCarga {
        final List<Projeto> ativos;
        final List<Projeto> todos;

        ResultadoCarga(List<Projeto> ativos, List<Projeto> todos) {
            this.ativos = ativos;
            this.todos = todos;
        }
    }

    private void carregarDados() {
        new SwingWorker<ResultadoCarga, Void>() {
            @Override
            protected ResultadoCarga doInBackground() throws Exception {
                List<Projeto> ativos = apiClient.listarProjetos();
                List<Projeto> todos = apiClient.listarTodosProjetos();
                return new ResultadoCarga(ativos, todos);
            }

            @Override
            protected void done() {
                try {
                    ResultadoCarga res = get();
                    cacheTodosProjetos = res.todos;

                    // Tabela mostra apenas ativos
                    int linhaSelecionadaAnterior = tabela.getSelectedRow();
                    modelo.setRowCount(0);
                    for (Projeto p : res.ativos) {
                        modelo.addRow(new Object[]{p.getId(), p.getNome(), p.getCategoria(), p.getStatus()});
                    }
                    if (linhaSelecionadaAnterior >= 0 && linhaSelecionadaAnterior < modelo.getRowCount()) {
                        tabela.setRowSelectionInterval(linhaSelecionadaAnterior, linhaSelecionadaAnterior);
                    }

                    // 4 cards consideram TODOS os projetos (inclusive cancelados)
                    int concluidos = 0;
                    int andamento = 0;
                    int cancelados = 0;

                    for (Projeto p : res.todos) {
                        String st = p.getStatus() == null ? "" : p.getStatus();
                        if ("Concluído".equalsIgnoreCase(st)) {
                            concluidos++;
                        } else if ("Cancelado".equalsIgnoreCase(st)) {
                            cancelados++;
                        } else {
                            andamento++;
                        }
                    }

                    lblTotalCount.setText(String.valueOf(res.todos.size()));
                    lblConcluidosCount.setText(String.valueOf(concluidos));
                    lblAndamentoCount.setText(String.valueOf(andamento));
                    lblCanceladosCount.setText(String.valueOf(cancelados));

                    atualizarGraficos(res.todos);

                } catch (Exception ex) {
                    // Silencia falhas pontuais de polling para não interromper o uso
                }
            }
        }.execute();
    }

    private void atualizarGraficos(List<Projeto> todos) {
        graficoStatus.setDados(todos);
        graficoCategoria.setDados(todos);
        graficoMensal.setDados(todos);
    }

    // --- GRÁFICO 1: PIZZA DE STATUS ---
    private static class PainelGraficoStatus extends JPanel {
        private Map<String, Integer> contagens = new LinkedHashMap<>();
        private final Color[] cores = {
                new Color(40, 167, 69),
                new Color(255, 193, 7),
                new Color(111, 66, 193),
                new Color(220, 53, 69)
        };

        PainelGraficoStatus() {
            setBackground(Color.WHITE);
            setDados(new ArrayList<>());
        }

        void setDados(List<Projeto> projetos) {
            contagens.clear();
            contagens.put("Concluído", 0);
            contagens.put("Em desenvolvimento", 0);
            contagens.put("Planejado", 0);
            contagens.put("Cancelado", 0);

            for (Projeto p : projetos) {
                String st = p.getStatus() == null ? "" : p.getStatus();
                if ("Em andamento".equalsIgnoreCase(st)) st = "Em desenvolvimento";
                if (contagens.containsKey(st)) {
                    contagens.put(st, contagens.get(st) + 1);
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = contagens.values().stream().mapToInt(Integer::intValue).sum();
            int diametro = Math.min(getWidth() - 40, getHeight() - 100);
            diametro = Math.max(diametro, 80);
            int x = (getWidth() - diametro) / 2;
            int y = 10;

            if (total == 0) {
                g2.setColor(new Color(230, 235, 240));
                g2.fillOval(x, y, diametro, diametro);
                g2.setColor(Color.GRAY);
                g2.drawString("Sem dados", getWidth() / 2 - 28, y + diametro / 2);
                return;
            }

            int anguloInicial = 0;
            int idx = 0;
            for (Map.Entry<String, Integer> entry : contagens.entrySet()) {
                int qtd = entry.getValue();
                if (qtd > 0) {
                    int arco = (int) Math.round(qtd * 360.0 / total);
                    g2.setColor(cores[idx % cores.length]);
                    g2.fillArc(x, y, diametro, diametro, anguloInicial, arco);
                    anguloInicial += arco;
                }
                idx++;
            }

            // Legenda simples na base
            int lx = 10;
            int ly = y + diametro + 18;
            idx = 0;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (Map.Entry<String, Integer> entry : contagens.entrySet()) {
                g2.setColor(cores[idx % cores.length]);
                g2.fillRect(lx, ly - 9, 10, 10);
                g2.setColor(Color.DARK_GRAY);
                String texto = entry.getKey() + ": " + entry.getValue();
                g2.drawString(texto, lx + 14, ly);
                lx += g2.getFontMetrics().stringWidth(texto) + 26;
                if (lx > getWidth() - 70) {
                    lx = 10;
                    ly += 16;
                }
                idx++;
            }
        }
    }

    // --- GRÁFICO 2: BARRAS POR CATEGORIA ---
    private static class PainelGraficoCategoria extends JPanel {
        private final String[] categorias = {"Web", "Software", "Mobile", "Desktop", "Outro"};
        private final int[] valores = new int[categorias.length];

        PainelGraficoCategoria() {
            setBackground(Color.WHITE);
        }

        void setDados(List<Projeto> projetos) {
            for (int i = 0; i < valores.length; i++) valores[i] = 0;
            for (Projeto p : projetos) {
                String cat = p.getCategoria() == null ? "" : p.getCategoria();
                for (int i = 0; i < categorias.length; i++) {
                    if (categorias[i].equalsIgnoreCase(cat)) {
                        valores[i]++;
                        break;
                    }
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int max = 1;
            for (int v : valores) if (v > max) max = v;

            int margemEsq = 20;
            int margemDir = 20;
            int margemTopo = 25;
            int margemBaixo = 35;
            int areaUtilLargura = Math.max(100, getWidth() - margemEsq - margemDir);
            int areaUtilAltura = Math.max(80, getHeight() - margemTopo - margemBaixo);

            int espacoGrupo = areaUtilLargura / categorias.length;
            int larguraBarra = Math.max(18, espacoGrupo - 16);

            g2.setColor(new Color(210, 215, 220));
            g2.drawLine(margemEsq, margemTopo + areaUtilAltura, getWidth() - margemDir, margemTopo + areaUtilAltura);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i < categorias.length; i++) {
                int alturaBarra = (int) Math.round((valores[i] / (double) max) * (areaUtilAltura - 10));
                int x = margemEsq + i * espacoGrupo + (espacoGrupo - larguraBarra) / 2;
                int y = margemTopo + areaUtilAltura - alturaBarra;

                g2.setColor(new Color(41, 98, 175));
                g2.fillRect(x, y, larguraBarra, alturaBarra);

                g2.setColor(Color.DARK_GRAY);
                g2.drawString(String.valueOf(valores[i]), x + larguraBarra / 2 - 4, Math.max(15, y - 4));
                g2.drawString(categorias[i], x, margemTopo + areaUtilAltura + 16);
            }
        }
    }

    // --- GRÁFICO 3: LINHA DE EVOLUÇÃO ---
    private static class PainelGraficoMensal extends JPanel {
        private List<Integer> pontos = new ArrayList<>();

        PainelGraficoMensal() {
            setBackground(Color.WHITE);
        }

        void setDados(List<Projeto> projetos) {
            pontos = new ArrayList<>();
            for (int i = 0; i < projetos.size(); i++) {
                pontos.add(i + 1);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margemEsq = 25;
            int margemDir = 20;
            int margemTopo = 25;
            int margemBaixo = 30;
            int largura = Math.max(100, getWidth() - margemEsq - margemDir);
            int altura = Math.max(80, getHeight() - margemTopo - margemBaixo);

            g2.setColor(new Color(210, 215, 220));
            g2.drawLine(margemEsq, margemTopo + altura, getWidth() - margemDir, margemTopo + altura);
            g2.drawLine(margemEsq, margemTopo, margemEsq, margemTopo + altura);

            if (pontos.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("Sem registros", getWidth() / 2 - 35, margemTopo + altura / 2);
                return;
            }

            int max = pontos.get(pontos.size() - 1);
            max = Math.max(max, 1);

            int n = pontos.size();
            int[] xs = new int[n];
            int[] ys = new int[n];

            for (int i = 0; i < n; i++) {
                xs[i] = n == 1 ? margemEsq + largura / 2 : margemEsq + (i * largura) / (n - 1);
                ys[i] = margemTopo + altura - (int) Math.round((pontos.get(i) / (double) max) * (altura - 10));
            }

            g2.setColor(new Color(40, 167, 69));
            g2.setStroke(new BasicStroke(2.5f));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
            }

            for (int i = 0; i < n; i++) {
                g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
            }
        }
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