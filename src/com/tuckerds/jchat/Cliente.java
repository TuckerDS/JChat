/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tuckerds.jchat;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.CANCEL_OPTION;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author tucker
 */
public class Cliente {
    
    public static void main(String[] args){
        String ip;
        if (args.length!=0) {
            ip = args[0];
        } else {
            ip="";
        }
        FrameCliente frmCliente = new FrameCliente(ip);
        frmCliente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
     Cliente(String ip){
        
        if (ip.equals("")) {
            ip = "";
        } else {
            ip = ip;
        }
        FrameCliente frmCliente = new FrameCliente(ip);
        frmCliente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}


/** Clase contenedora del tipo JFrame
 * 
 *  @author tucker
 *  @author tuckerds.com
 *  @version 0.1
 * 
 */
class FrameCliente extends JFrame{
    Socket sck;
    String ip;
    public FrameCliente(String ip){
        this.ip = ip;
        setBounds(600,300,500,350);
        PanelCliente pnlCliente = new PanelCliente(ip);
        add(pnlCliente);
        setTitle("CLIENTE");
        setVisible(true);
        addWindowListener(new CWindowAdapter(pnlCliente));
    }
    
    class CWindowAdapter extends WindowAdapter{
        PanelCliente p;
        public CWindowAdapter (PanelCliente p){
            this.p=p;
        }
        
        @Override
        public void windowClosing(WindowEvent we) {
            System.out.println("Saliendo");
            p.enviarOffline();
            System.exit(0);
        }
    }
}

/** Clase contenedora del tipo JPanel
 *  Implementa ClienteListener para manejar los eventos generados por el CCliente
 * 
 *  @author tucker
 *  @author tuckerds.com
 *  @version 0.1
 * 
 */
class PanelCliente extends JPanel implements ClienteListener{
    private JTextField txtMensaje;
    private JComboBox cbIP;
    private JButton btnEnviar;
    private JButton btnEnviarTodos;
    private JButton btnEnviarImagen;
    private JTextArea areaChat;
    private JLabel lblNick;
    private JScrollPane pnlScroll;
    
    //Calendar calendar;
    SimpleDateFormat df;
    ArrayList<String> aListaIPs;
    CCliente cliente;
    
    JPanel norte; 
    JPanel centro;
    JPanel sur;
    
    String ip;
    String localIP; 
    
    byte[] adjunto;
    
    public PanelCliente(String ip)  {
        
        this.ip = ip;
        
        //df = new SimpleDateFormat("yy/MM/dd-HH:mm:ss");
        df = new SimpleDateFormat("HH:mm:ss");
       
        if (ip.equals("")) {
            ip="";
            while (ip.equals("")){
                ip = JOptionPane.showInputDialog("IP o URL del Servidor: ");
                if (ip == null) {System.exit(0);}
                
          
                try {
                    InetAddress address;
                    address = InetAddress.getByName(new URL(ip).getHost());
                   
                    System.out.println(address);
                    ip = address.getHostAddress();
                } catch (MalformedURLException ex) {
                    System.out.println("URL incorrecta.");
                    //ex.printStackTrace();
                     try {
                        InetAddress.getByName(ip).isReachable(5); //IOException
                     } catch (IOException ioe) {
                        ip="";
                     }
                } catch (UnknownHostException ex) {
                    System.out.println("Servidor desconocido.");
                    ex.printStackTrace();
                } 
            }
            System.out.println("IP: " + ip);
        }
        
        String nick = JOptionPane.showInputDialog("Nick: ");
        //Etiqueta NICK
        JLabel lbl = new JLabel("Nick: ");
        //norte.add(lbl);
        //NICK
        lblNick = new JLabel(nick);
        //norte.add(lblNick);
        //Etiqueta IP
        JLabel lblTexto = new JLabel("IP: ");
        //norte.add(lblTexto);
        //IP
        cbIP = new JComboBox();
        cbIP.setEditable(true);
        //norte.add(cbIP);
        //AREA CHAT
        areaChat = new JTextArea(12,40);
        areaChat.setEditable(false);
        pnlScroll = new JScrollPane(areaChat);
        //centro.add(pnlScroll);
        pnlScroll.setAutoscrolls(true);
        
        //areaChat.setLineWrap(true);
        //add(areaChat);
        //MENSAJE
        txtMensaje = new JTextField(20);
        //sur.add(txtMensaje);
        
        JPanel pnlBotones = new JPanel();
        pnlBotones.setLayout(new FlowLayout());
        //BOTON
        btnEnviar=new JButton("Enviar");
        ListenerEnviar lstEnviar = new ListenerEnviar();
        btnEnviar.addActionListener(lstEnviar);
        //sur.add(btnEnviar);
        
        btnEnviarTodos=new JButton("Enviar a Todos");
        ListenerEnviarTodos lstEnviarTodos = new ListenerEnviarTodos();
        btnEnviarTodos.addActionListener(lstEnviarTodos);
        //sur.add(btnEnviarTodos);
        
        btnEnviarImagen=new JButton("Añadir imagen");
        ListenerEnviarArchivo lstEnviarImagen = new ListenerEnviarArchivo();
        btnEnviarImagen.addActionListener(lstEnviarImagen);
        btnEnviarImagen.setBackground(Color.LIGHT_GRAY);
        //sur.add(btnEnviarImagen);
        
        Box horBoxCabecera =  Box.createHorizontalBox();
        horBoxCabecera.add(lbl);
        horBoxCabecera.add(Box.createHorizontalStrut(5));
        horBoxCabecera.add(lblNick);
        horBoxCabecera.add(Box.createHorizontalStrut(10));
        horBoxCabecera.add(lblTexto);
        horBoxCabecera.add(Box.createHorizontalStrut(5));
        horBoxCabecera.add(cbIP);
        
        
        
        Box horBoxBotones =  Box.createHorizontalBox();
        horBoxBotones.add(Box.createHorizontalStrut(10));
        horBoxBotones.add(btnEnviar);
        horBoxBotones.add(Box.createHorizontalStrut(5));
        horBoxBotones.add(btnEnviarTodos);
        horBoxBotones.add(Box.createHorizontalStrut(5));
        horBoxBotones.add(btnEnviarImagen);
        horBoxBotones.add(Box.createHorizontalStrut(10));
        
        
        Box verBox = Box.createVerticalBox();
        verBox.add(Box.createVerticalStrut(10));
        verBox.add(Box.createGlue());
        verBox.add(horBoxCabecera);
        verBox.add(Box.createVerticalStrut(5));
        verBox.add(Box.createGlue());
        verBox.add(pnlScroll);
        verBox.add(Box.createVerticalStrut(5));
        verBox.add(txtMensaje);
        verBox.add(Box.createGlue());
        verBox.add(Box.createVerticalStrut(5));
        verBox.add(horBoxBotones);
        verBox.add(Box.createGlue());
        verBox.add(Box.createVerticalStrut(10));
        //sur.add(horBoxBotones);
        
        //add(norte, BorderLayout.NORTH);
        //add(centro, BorderLayout.CENTER);
        //add(sur, BorderLayout.SOUTH);
       
        add(verBox);
        
        //CONECTAR
        cliente = new CCliente();
        cliente.addClienteListener(this);
        cliente.conectar(ip, 3001);
       
    }
    
    //----metodos aux
    public void fileToBytes(File f){
        //byte[] array = Files.re
        try {
            
            FileInputStream streamLectura = new FileInputStream("/Users/tucker/Desktop/escudo.png");
            boolean finalArchivo = false;
            int contBytes=0;
            int arrayBytes[] = new int[183819];
            while (!finalArchivo){
                
                int byteEntrada= streamLectura.read();
                
                if (byteEntrada!=-1) 
                    arrayBytes[contBytes]=byteEntrada;
                else
                    finalArchivo=true;
                
                System.out.println(arrayBytes[contBytes]);
                contBytes++;
            }
            streamLectura.close();
            System.out.println(contBytes);
            //escribirFichero(arrayBytes);
        } catch (IOException e){
            System.out.println("errores");
        }
    
    }
    
    
    
    //--------------
    
    
    
    
    public void enviarOffline(){
        cliente.desconectar();
    }
    
    @Override
    public void eventoMensajeRecibido(CMensaje m) {
        System.out.println("Evento Recibido");
        long ms;
        String line;
        switch (m.getMsgType()){
            case 1: //ONLINE
                areaChat.append("[" + df.format(m.getDate()) + "]: " + m.getMensaje() + "\n");
                areaChat.setCaretPosition(areaChat.getDocument().getLength());
                break;
            case 2: //TEXT
                //ms = calendar.getTime().getTime() - m.getDate().getTime();
                ms = (new Date()).getTime() - m.getDate().getTime();
                if (m.haveAdjunto()) {
                    line = ("[" + df.format(m.getDate()) + " (" + ms + "ms)] " + m.getNick() + "(" + m.getIp() + "): [IMG] "+ m.getMensaje()+"\n");
                    MarcoImagen fi = new MarcoImagen(m.getAdjunto());
                    fi.setVisible(true);
                } else line = ("[" + df.format(m.getDate()) + " (" + ms + "ms)] " + m.getNick() + "(" + m.getIp() + "): "+ m.getMensaje()+"\n");
                areaChat.append(line);
                areaChat.setCaretPosition(areaChat.getDocument().getLength());
                
                break;
            case 3: //IPS
                //Eliminar Brackets
                String sIP= m.getMensaje().replaceAll("^\\[|]$", "");
                //Convertir cadena a s
                aListaIPs = new ArrayList<String>(Arrays.asList(sIP.split(", ")));
                //Resetear Combo y Rellenarlo
                cbIP.removeAllItems();
                for (String s:aListaIPs){
                    if (!(s.equals( m.getIp() ))){ //Si no es mi ip
                        cbIP.addItem(s);
                    }
                }
                break;
            case 4: //OFFLINE
                areaChat.append("[" + df.format(m.getDate()) + "]: " + m.getMensaje() + "\n");
                areaChat.setCaretPosition(areaChat.getDocument().getLength());
                break;
            case 5: //BROADCAST
                ms = (new Date()).getTime() - m.getDate().getTime();
                if (m.haveAdjunto()) {
                //ms = calendar.getTime().getTime() - m.getDate().getTime();
                    line = ("[" + df.format(m.getDate()) + " (" + ms + "ms)] " + m.getNick() + "(" + m.getIp() +") > TODOS: [IMG] "+ m.getMensaje()+"\n");
                    MarcoImagen fi = new MarcoImagen(m.getAdjunto());
                    fi.setVisible(true);
                } else line = ("[" + df.format(m.getDate()) + " (" + ms + "ms)] " + m.getNick() + "(" + m.getIp() +") > TODOS: "+ m.getMensaje()+"\n");
                areaChat.append(line);
                areaChat.setCaretPosition(areaChat.getDocument().getLength());
                break;
            case 0: //DEFAULT
                break;
        }
    }
    
    private class ListenerEnviar implements ActionListener{  
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Pulsado boton Enviar");
            //Crear Paquete Mensaje
            CMensaje datos = new CMensaje();
            datos.setNick(lblNick.getText());
            datos.setIp(cbIP.getSelectedItem().toString());
            datos.setMensaje(txtMensaje.getText());
            datos.setMsgType(CMensaje.MSG_TEXT);
            //datos.setDate(calendar.getTime());
            datos.setDate(new Date());
            if (adjunto != null) {
                datos.setAdjunto(adjunto);
                areaChat.append("[IMG]");
            }
            //Enviar Paquete
            cliente.enviarMensaje(datos);
            areaChat.append("["+ df.format(datos.getDate())+ "] YO->" + datos.getIp()+": " + datos.getMensaje() + "\n");
            txtMensaje.setText("");
            
        }
    }
    
    private class ListenerEnviarArchivo implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            
            JFileChooser fc = new JFileChooser();
            //fc.addChoosableFileFilter(new ImageFilter());
            fc.setAcceptAllFileFilterUsed(false);
            //Handle open button action.
            if (e.getSource() == btnEnviarImagen) {
                if (btnEnviarImagen.getText().equals("Añadir imagen")){
                    //int returnVal = fc.showOpenDialog(FileChooserDemo.this);
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            //imagen=ImageIO.read(archivo);
                            //imagen=ImageIO.read(new File("src/graficos/coche.png"));

                            Image imagen=ImageIO.read(file);
                            byte[] array = Files.readAllBytes(file.toPath());
                            adjunto = array;
                            System.out.println("Imagen a la espera");
                            btnEnviarImagen.setBackground(Color.green);
                            btnEnviarImagen.setText("Quitar imagen");
                           
                        } catch (IOException ex) {
                            System.out.println("No se encuentra");
                        }
                    //This is where a real application would open the file.
                    System.out.println("Opening: " + file.getName() + ".");
                    }
                } else {
                    System.out.println("Open command cancelled by user.");
                    adjunto = null;
                    btnEnviarImagen.setBackground(Color.LIGHT_GRAY);
                    btnEnviarImagen.setText("Añadir imagen");
                }
            }
        }
        //int returnVal = fc.showDialog(FileChooserDemo2.this, "Attach");
    }
    
    
    private class ListenerEnviarTodos implements ActionListener{  
        @Override
        public void actionPerformed(ActionEvent e) {
            //Date d = calendar.getTime();
            
            System.out.println("Pulsado boton Enviar");
            //Crear Paquete Mensaje
            CMensaje datos = new CMensaje();
            datos.setNick(lblNick.getText());
            datos.setIp(cbIP.getSelectedItem().toString());
            datos.setMensaje(txtMensaje.getText());
            datos.setMsgType(CMensaje.MSG_BROADCAST);
            //datos.setDate(calendar.getTime());
            datos.setDate(new Date());
            if (adjunto != null) {
                datos.setAdjunto(adjunto);
                areaChat.append("[IMG]");
            }
            //Enviar Paquete
            cliente.enviarMensaje(datos);
            areaChat.append("["+df.format(datos.getDate()) + "] YO->Todos: " + datos.getMensaje() + "\n");
            txtMensaje.setText("");
        }
    }
    
    class EventoCliente extends java.util.EventObject {
        public EventoCliente(Object source) {
            super(source);
        }
    }
}
/** Clase CCliente es la que maneja los flujos de entrada y salida de mensajes.
 * 
 *  @author tucker
 *  @author tuckerds.com
 *  @version 0.1
 *  @see CMensaje
 */
class CCliente {
    String ip;
    Socket s;
    FlujoEntrada fEntrada;
    FlujoSalida fSalida;
    //Calendar calendar;
    //SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd-HH:mm:ss");
    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    
    
    //Crear array de objetos a la escucha
    ArrayList<ClienteListener> listeners = new ArrayList();
    //Crear Metodos para añadir o quitar objetos a la escucha
    
    public synchronized void addClienteListener(ClienteListener listener){
        listeners.add(listener);
    }
    public synchronized void removeClienteListener(ClienteListener listener){
        listeners.remove(listener);
    }
    /** Método que desata el evento
     * @param m CMensaje
     * @return void
     * @see CMensaje
     * @see ClienteListener
     */
    public synchronized void mensajeRecibido(CMensaje m){
        System.out.println("Evento Generado");
        for (ClienteListener cl: listeners) {
            cl.eventoMensajeRecibido(m);
        }
    }
    
    public void enviarMensaje(CMensaje m){
        fSalida.addMensaje(m);
    }
    
    private void enviarOnline(){
        CMensaje datos = new CMensaje();
        datos.setMsgType(CMensaje.MSG_ONLINE);
        //datos.setDate(calendar.getTime());
        datos.setDate(new Date());
        datos.setIp(ip);
        fSalida.addMensaje(datos);
        System.out.println("Mensaje ONLINE enviado.");
    }
    
    public void conectar(String host, int port){
        Socket s = null;
        //calendar = Calendar.getInstance();
        try { 
            s = new Socket(host, port);
            s.setKeepAlive(true);
            System.out.println("Conectado.");
            System.out.println("Creando los flujos...");
            fEntrada = new FlujoEntrada(s);
            fSalida = new FlujoSalida(s);
            Thread tSalida = new Thread(fSalida);
            Thread tEntrada = new Thread(fEntrada);
            tSalida.start();
            tEntrada.start();
            
            //ENVIAR ONLINE
            enviarOnline();
        } catch (IOException ex) {ex.printStackTrace();}
    }
    
    public void desconectar(){
        CMensaje datos = new CMensaje();
        datos.setMsgType(CMensaje.MSG_OFFLINE);
        //datos.setDate(calendar.getTime());
        datos.setDate(new Date());
        datos.setIp(ip);
        fSalida.addMensaje(datos);
        System.out.println("Mensaje OFFLINE enviado.");
    }
    
    /** Subclase interna de CCliente que corre en un hilo independiente,
    *   extiende la clase Thread.
    *   Maneja el ObjectOutputStream
    * 
    *  @since 0.1
    *  @see CCliente
    *  @see CMensaje
    *  @see Thread
    *  @see ObjectOutputStream 
    *  @see Socket
    */
    class FlujoSalida extends Thread{
        ObjectOutputStream streamSalida;
        Socket s;
        String m;
        boolean salir = false;
        ArrayList<CMensaje> mensajeList = new ArrayList();
        
        FlujoSalida(Socket s){
            this.s = s;
        }
        
        public synchronized void addMensaje(CMensaje m){
            mensajeList.add(m);
            System.out.println("Añadiendo a la cola:" + m.toString());
            notifyAll();
        }
        
        private synchronized void gestionarMensajes(){
            if (mensajeList.size()!=0) {
                System.out.println("Mensajes en cola: " + mensajeList.size());
                CMensaje m = mensajeList.get(mensajeList.size()-1);
                if (enviarMensaje(m)) {
                    System.out.println("Eliminado mensaje: " + m.toString());
                    if (m.getMsgType() == CMensaje.MSG_OFFLINE) salir = true;
                    mensajeList.remove(m);
                }
            }
            notifyAll();
        }
        
        private synchronized boolean enviarMensaje(CMensaje m){
            try {
                streamSalida.writeObject(m);
                System.out.println("Enviado: " + m.getMsgType() + m.getMensaje());
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            } finally{
                notifyAll();
            }
        }
        
        @Override
        public void run() {
            try {
                streamSalida = new ObjectOutputStream(s.getOutputStream());
                System.out.println("Creado flujo de Salida");
                while(!salir){
                    gestionarMensajes();
                }
                System.out.println("Se ha salido");
                streamSalida.close();
                s.close();
            } catch (IOException ex) { ex.printStackTrace();}
        }
    }
    
    /** Subclase interna de CCliente que corre en un hilo independiente,
    *   extiende la clase Thread.
    *   Maneja el InputStream
    * 
    *  @author tucker
    *  @author tuckerds.com
    *  @version 0.1
    * 
    *  @see CCliente
    *  @see CMensaje
    *  @see Thread
    *  @see ObjectInputStream
    *  @see Socket
    * 
    */
    class FlujoEntrada extends Thread {
        ObjectInputStream flujoEntrada;
        Socket s;
        String m;
        CMensaje msgIn;
        
        public FlujoEntrada(Socket s) {
            this.s=s;
        }
        
        @Override
        public void run(){
            try {
                flujoEntrada = new ObjectInputStream(s.getInputStream());
                System.out.println("Creado flujo de Entrada");
                while(s.isConnected()){
                    msgIn = (CMensaje)flujoEntrada.readObject();
                    mensajeRecibido(msgIn);
                    System.out.println("Mensaje Recibido" +  msgIn.toString() + ")");
                }
                flujoEntrada.close();
                System.out.println("El flujo de entrada se ha cerrado debido a que el Socket no esta conectado");
            } 
            catch (IOException ex) { ex.printStackTrace();} 
            catch (ClassNotFoundException e) {e.printStackTrace();}
        }
    }
}

/** Clase Objeto que empaqueta los datos del mensaje.
 *  CMensaje es un objeto Serializable.
 * 
 *  @author tucker
 *  @author tuckerds.com
 *  @version 0.1
 *  @since 0.1
 * 
 *  @see CServidor
 *  @see CClientHandler
 *  @see Serializable
 */
class CMensaje implements Serializable {    //Tiene que ser serializable
    private static final long serialVersionUID = 1L;    //Para mantener compatibilidad
    
    private String nick, ip, mensaje;
    private Date date; 
    private int msgType = 0;
    private byte[] adjunto=null;
    private boolean haveAdjunto =false;

    static final int MSG_DEFAULT=0;
    static final int MSG_ONLINE=1;
    static final int MSG_TEXT=2;
    static final int MSG_IPS=3;
    static final int MSG_OFFLINE=4;
    static final int MSG_BROADCAST=5;
   
    //GETTERS Y SETTERS
    public int getMsgType() {return msgType;}
    public void setMsgType(int msgType) {this.msgType = msgType;}
    public String getNick() {return nick;}
    public void setNick(String nick) {this.nick = nick;}
    public String getIp() {return ip;}
    public void setIp(String ip) {this.ip = ip;}
    public String getMensaje() {return mensaje;}
    public void setMensaje(String mensaje) {this.mensaje = mensaje;}
    public Date getDate() {return date;}
    public void setDate(Date date) {this.date = date;}
    
    public void setAdjunto(byte[] adjunto) {
        this.adjunto = adjunto;
        this.haveAdjunto = true;
    }
    
    public byte[] getAdjunto() {
        return adjunto;
    }
    
    public boolean haveAdjunto() {
        return haveAdjunto;
    }
    
    public String getType(){
        switch (this.getMsgType()){
            case 0: return "MSG_DEFAULT";
            case 1: return "MSG_ONLINE";
            case 2: return "MSG_TEXT";
            case 3: return "MSG_IPS";
            case 4: return "MSG_OFFLINE";
            case 5: return "MSG_BROADCAST";
            default: return "UNKNOW";
        }    
    }
    
    @Override
    public String toString(){
        String s;
        s = this.getType() + "("+this.getMsgType()+")" +
            " IP("+this.getIp()+")" +
            " NICK("+this.getNick()+")" +
            " MSG("+this.getMensaje()+")";
        return s;
    }
}

//CLASES DE MANEJO DE IMAGENES

class MarcoImagen extends JFrame{
    private byte[] imagen;
    
    public MarcoImagen(){
        setTitle("Imagen");
        setBounds(100,100,400,400);
        
        this.setVisible(true);
        
    }
    public MarcoImagen(byte[] img){
        this.imagen=img;       
        setTitle("Imagen");
        setBounds(100,100,400,400);
        JPanel panelBotones = new JPanel();
        JButton btnGuardar = new JButton("Guardar");
        
        LaminaImagen lamina=new LaminaImagen(this.imagen);
        add(lamina);
        panelBotones.add(btnGuardar);
        //add(panelBotones);
        //btnGuardar.getf
        this.setVisible(true);
    }
}

class LaminaImagen extends JPanel{
    byte[] bimagen;
    Image imagen;
    LaminaImagen(byte[] img){
        this.bimagen=img;
        try {
            imagen = ImageIO.read(new ByteArrayInputStream(bimagen));
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.setSize(imagen.getWidth(this), imagen.getWidth(this));
    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);    
        //File archivo=new File("src/graficos/coche.png");
        
        //g.drawImage(imagen, 300, 300, null);
        g.drawImage(imagen, 0, 0, this.getWidth(), this.getHeight(), this);
        
    }
}

 /*class SimpleConvertImage {/*
        private String base64String;
        
        public void encode(){
        
        }
    //public static void main(String[] args) throws IOException{
        String dirName="C:\\";
        ByteArrayOutputStream baos=new ByteArrayOutputStream(1000);
        BufferedImage img=ImageIO.read(new File(dirName,"rose.jpg"));
        ImageIO.write(img, "jpg", baos);
        baos.flush();

        String base64String=Base64.encode(baos.toByteArray());
        baos.close();
        
        byte[] bytearray = Base64.decode(base64String);

        BufferedImage imag=ImageIO.read(new ByteArrayInputStream(bytearray));
        ImageIO.write(imag, "jpg", new File(dirName,"snap.jpg"));
    //}
*/
