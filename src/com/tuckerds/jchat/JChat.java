/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tuckerds.jchat;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author tucker
 * @version 0.1
 * 
 */
public class JChat {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        switch (args.length){
            case 0:
                new JChat();
                break;
            case 1:
                new JChat(args[0]);
                break;
            case 2:
                break;
            default:
                new JChat();
                break;
        }
        
        
        // TODO code application logic here
    }
    
    JChat(String t){
        if (t.equals("server")){new Servidor();}
        else if (t.equals("client")){new Cliente("");}
        else {System.out.println("Los paramentos son [server|ip_servidor] \n" + 
                                 "Ejemplo: JChat server" + 
                                 "Ejemplo: JChat 192.168.0.10");}
    }
    
    JChat(){
        final JFrame frame = new JFrame();
        frame.setBounds(600,300,300,100);
        frame.setLayout(new FlowLayout());
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("JChat");
        JButton btnServer = new JButton("Servidor");
        JButton btnClient = new JButton("Cliente");
        
        btnServer.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                new Servidor();
                frame.setVisible(false);
                //frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        
        btnClient.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                new Cliente("");
                frame.setVisible(false);
                
            }
        });
        
        frame.add(btnServer);
        frame.add(btnClient);
        frame.setVisible(true);
        
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
                /*
                if (JOptionPane.showConfirmDialog(frame, 
                    "¿Está seguro de cerrar la ventana?", "¿Desea cerrar?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    System.exit(0);}
                */
            }
        });
    }
    
    
}
