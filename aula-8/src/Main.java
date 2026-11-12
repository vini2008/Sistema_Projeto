package com.sistema; // ou o teu package atual

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;
import view.TelaProjetos;

public class Main {
    public static void main(String[] args) {
        // Ativar o visual moderno FlatLaf antes de criar a janela
        try {
            FlatDarkLaf.setup(); // Tema escuro moderno (ou FlatLightLaf para tema claro)
        } catch(Exception ex) {
            System.err.println("Falha ao inicializar o FlatLaf");
        }

        // Abrir a interface gráfica principal
        SwingUtilities.invokeLater(() -> {
            new TelaProjetos().setVisible(true);
        });
    }
}