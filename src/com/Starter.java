package com;

import com.controller.GameFrame;

import javax.swing.*;

public class Starter {
    public static void main (String []args){
        System.out.println("Hello, this is renju chess");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new GameFrame("Renju Game"));
    }
}
