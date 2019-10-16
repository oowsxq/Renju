package com.utils;

import javax.swing.*;
import java.awt.*;

public class MyTextPanel extends JScrollPane {
    private JTextArea textArea = new JTextArea();

    public MyTextPanel(){
        textArea.setLineWrap(true);
        textArea.setFont(new Font(null, Font.PLAIN, 12));
        setViewportView(textArea);
    }

    public void appendText(String text){
        textArea.append(text);
    }

    public void setText(String text){
        textArea.setText(text);
    }
}
