/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tuckerds.jchat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** Clase generadora del frame.
 *  El JFrame realmente es quien implementa la Clase CServidor.
 *  
 *  @author tucker
 *  @author tuckerds.com
 *  @version 0.1
 * 
 *  @see CServidor
 */
public class Servidor {
    public static void main(String[] args){
        FrameServidor  frmServidor  = new FrameServidor();
        frmServidor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    Servidor(){
        FrameServidor  frmServidor  = new FrameServidor();
        frmServidor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}


/** Clase contenedora que implementa la interfaz ServidorListener
 *  Recibe los eventos generados por la clase CServidor
 *  
 *  @author tucker
 *  @author tuckerds.com
 *  @version 0.1
 * 
 *  @see CServidor
 *  @see ServidorListener
 */
class FrameServidor extends JFrame implements  ServidorListener {
    //Hace de Listener de clientes
    private JTextArea areaTexto;
    private JScrollPane pnlScroll;
    private JTextField txtCampo;
    private JButton btnEnviar;
    
    //CONSTRUCTOR
    public FrameServidor ()  {
        setBounds(600,300,280,350);
        setTitle("SERVIDOR");
        
        txtCampo = new JTextField(20);
        btnEnviar=new JButton("Enviar");
        areaTexto = new JTextArea(12,40);
        areaTexto.setEditable(false);
        pnlScroll = new JScrollPane(areaTexto);
        pnlScroll.setAutoscrolls(true);
        
        Box verBox = Box.createVerticalBox();
        verBox.add(Box.createVerticalStrut(5));
        verBox.add(txtCampo);
        verBox.add(Box.createVerticalStrut(5));
        verBox.add(btnEnviar);
        verBox.add(Box.createVerticalStrut(5));
        verBox.add(pnlScroll);
        verBox.add(Box.createVerticalStrut(10));
        add(verBox);
        
        setVisible(true);
        
        CServidor servidor = new CServidor();
        servidor.addServidorListener(this);
        servidor.conectar(3001);
        Thread tServidor = new Thread(servidor);
        tServidor.start();
    }
 
    @Override
    public void eventoMensajeRecibido(CMensaje m, String ip) {
        areaTexto.append(ip + ":" + m.toString()+"\n");
    }

    @Override
    public void eventoClienteDesconectado(String ip) {
        areaTexto.append(ip + " desconectado."+"\n");
    }

    @Override
    public void eventoNuevoClienteConectado(String ip) {
        areaTexto.append(ip + " conectado."+"\n");
    }
}

/** Clase que monitoriza y da acceso a los clientes.
 *  Se mantiene a la escucha y crea un clase CClientHanler
 *  al recibir una conexión nueva. La ejecuta en un hilo independiente.
 *  Genera los eventos:
 *      eventoMensajeRecibido(CMensaje m, String ip)
 *      eventoClienteDesconectado(String ip)
 *       
 *  @author tucker
 *  @author tuckerds.com
 *  @version 0.1
 *  
 *  @see CClientHandler
 *  @see CMensaje
 */
class CServidor implements Runnable{
    private ServerSocket serverSocket;
    ArrayList<Thread> tClientes = new ArrayList();
    ArrayList<CClientHandler> clientes = new ArrayList();
    //Calendar calendar;
    SimpleDateFormat df;
    
    //Crear array de objetos a la escucha
    ArrayList<ServidorListener> listeners = new ArrayList();
    
    //Crear Metodos para añadir o quitar objetos a la escucha
    public synchronized void addServidorListener(ServidorListener listener){
        listeners.add(listener);
        notifyAll();
    }
    public synchronized void removeServidorListener(ServidorListener listener){
        listeners.remove(listener);
    }
    
    //Metodo que desata el evento 
    public synchronized void mensajeRecibido(CMensaje m, String ip){
        System.out.println("Evento Generado(mensajeRecibido)");
        for (ServidorListener cl: listeners) {
            cl.eventoMensajeRecibido(m,ip);
        }
        
        switch (m.getMsgType()){
            case 1: //ONLINE
                clienteConectado(ip);
                updateIP();
                m.setDate(new Date());
                m.setMensaje(ip + " se ha conectado.");
                sendBroadCast(m);
                break;
            case 2: //TEXTO
                try {
                    if (InetAddress.getByName(m.getIp()).isReachable(5)) {
                        sendMensaje(m);
                    }
                } 
                catch (UnknownHostException ex) {ex.printStackTrace();}   //HOST DESCONOCIDO
                catch (IOException ex) {ex.printStackTrace();}              //TIMEOUT
                break;
            case 3: //IP
                break;
            case 4: //OFFLINE
                updateIP();
                m.setDate(new Date());
                //m.setDate(calendar.getTime());
                m.setMensaje(ip + " se ha desconectado.");
                sendBroadCast(m);
                break;
            case 5: //MSG_BROADCAST
                sendBroadCast(m);
                break;
            case 0: //DEFAULT
                break;
            }
        notifyAll();
        
    }
    
    public synchronized void clienteConectado(String ip){
        System.out.println("Evento Generado (clienteConectado)");
        for (ServidorListener cl: listeners) {
            cl.eventoNuevoClienteConectado(ip);
        }
        notifyAll();
    }
    
    public synchronized void clienteDesconectado(Thread t, CClientHandler c, String ip){
        System.out.println("Evento Generado (clienteDesconectado)");
        for (ServidorListener cl: listeners) {
            cl.eventoClienteDesconectado(ip);
        }
        clientes.remove(c);
        tClientes.remove(t);
        updateIP();
        notifyAll();
    }
    
    public void conectar(int port){
        //calendar = Calendar.getInstance();
        //df = new SimpleDateFormat("yy/mm/dd-HH:mm:ss");
        df = new SimpleDateFormat("HH:mm:ss");
        try {
            serverSocket = new ServerSocket(3001);
            //serverSocket = ServerSocketFactory.getDefault().createServerSocket(3000);
            System.out.println("Servidor a la escucha");
        } catch (IOException ex) { 
            System.out.println("E/S Error creando ServerSocket");
            ex.printStackTrace();}
    }
    
    public synchronized void checkStatus(){
        System.out.println("COMPROBACION");
        //Comprobar lista de clientes y el estado del socket
        for (CClientHandler c:clientes) {
            System.out.println(c.getIP() + " conectado: " + c.isSocketConected());
        }
        
        //Comprobar lista de hilos
        for (Thread t:tClientes){
            System.out.println("nombre hilo: " + t.getName() + ", Vivo?:" + t.isAlive());
        }
        
        //Apagar hilos con socket desconectados y eliminar de la lista de clientes
        for (CClientHandler c:clientes) {
            if (c.isSocketConected() == false){
                c.closeTread();
                clientes.remove(c);
            }
        }
        
        //Eliminar hilos no vivos de la lista de hilos
        for (Thread t:tClientes){
            if (t.isAlive() == false) {
                tClientes.remove(t);
                //clientes.remove(t);
            }
        }
        
        System.out.println("RESULTADO");
        //Comprobar lista de clientes y el estado del socket
        for (CClientHandler c:clientes) {
            System.out.println(c.getIP() + " conectado: " + c.isSocketConected());
        }
        //Comprobar lista de hilos
        for (Thread t:tClientes){
            System.out.println("nombre hilo: " + t.getName() + ", Vivo?:" + t.isAlive());
        }
        notifyAll();
    }
    
    public synchronized void updateIP(){
        CMensaje m = new CMensaje();
        m.setMsgType(CMensaje.MSG_IPS);
        checkStatus();
        CClientHandler.listIP.clear();
        for (CClientHandler c:clientes) {
            CClientHandler.listIP.add(c.getIP());
        }
        m.setMensaje(CClientHandler.listIP.toString());
        sendBroadCast(m);
        notifyAll();
    }
    
    public void sendBroadCast(CMensaje m){
        for (CClientHandler c:clientes) {
            c.procesarMensaje(m);
            System.out.println("Mensaje broadcast enviado a: " + c.getIP());
        }
    }
    
    public void sendMensaje(CMensaje m){
        String ip = m.getIp();
        for (CClientHandler c:clientes) {
            if (ip.equals(c.getIP())){
                c.procesarMensaje(m);
            }
            System.out.println("Mensaje enviado a: " + c.getIP());
        }
    }
    
    @Override
    public void run() {
        //String nick, ip, mensaje;
        //CMensaje datos;
        //ArrayList<String> alIPs = new ArrayList();
        try {
            while(true){
                Socket socketToClient = serverSocket.accept();
                System.out.println("Cliente Conectado");
                
                CClientHandler cliente = new CClientHandler(socketToClient, this);
                Thread tClientHandler = new Thread(cliente);
                tClientes.add(tClientHandler);
                clientes.add(cliente);
                tClientHandler.start();
                
                cliente.getIP();
            }
        } catch (IOException ex) {
            System.out.println("E/S Error creando Socket para Cliente");
            ex.printStackTrace();
        }
    }

}

/** Clase dedicada a cada cliente.
 *  Corre en un hilo al ser lanzada cuando el servidor recibe una nueva conexión. 
 *  Se comunica con el servidor, que es el que genera los eventos y gestiona todo.
 * 
 *  @author tucker
 *  @author tuckerds.com
 *  @version 0.1
 * 
 *  @see CServidor
 *  @see CMensaje
 */
class CClientHandler implements Runnable{
    static ArrayList<String> listIP = new ArrayList();
    private Socket s;
    ObjectInputStream flujoEntrada;
    ObjectOutputStream flujoSalida;
    private String ip;
    Boolean salir=false;
    //ServerListener sl;
    CServidor sl;
    public String getIP(){
        return this.ip;
    }
    
    public CClientHandler(Socket sCliente, CServidor sl){
        s=sCliente;
        this.sl = sl;
        System.out.println("Creado nuevo socket");
        InetAddress localizacion=s.getInetAddress();
        ip = localizacion.getHostAddress();
        //sl.updateIP();
    }
    
    public boolean isSocketConected(){
        return s.isConnected();
    }
    
    public void close(){
        closeAll();
    }
    
    private void closeAll(){
        try {
            flujoEntrada.close();
            flujoSalida.close();
            s.close();
            sl.updateIP();
            System.out.println("Cliente desconectado");
            this.closeTread();
        } catch (IOException ex) {
            System.out.println("Problema al cerrar todo");
            ex.printStackTrace();
        }
    }
    
    private synchronized void sendMensaje(CMensaje m) {
        try {
            if (s.isConnected()) {
                flujoSalida.writeObject(m);
                flujoSalida.flush();
            } else {
                System.out.println("Socket cerrado");
                sl.updateIP();
            }
        } catch (IOException ex) {
            
            System.out.println("Error en sendMenjaje() en cliente:" + this.getIP());
            System.out.println("DATOS DEL MENSAJE:");
            System.out.println("TYPE(" + m.getMsgType() + ")" + 
                               " NICK(" + m.getNick() + ")" +
                               " IP(" + m.getIp() + ")" +
                               " MSG(" + m.getMensaje() + ")");
                   
            ex.printStackTrace();
        } finally {
            notifyAll();
        }
        
    }
    
    public void procesarMensaje(CMensaje m) {
        System.out.println("Procesando Mensaje");
        sendMensaje(m);
    }

    public void closeTread(){
        salir = true;
    }
    @Override
    public void run() {
        try {
            //CREAR FLUJOS DE E/S
            
            flujoEntrada = new ObjectInputStream(s.getInputStream());
            flujoSalida = new ObjectOutputStream(s.getOutputStream());
            //ATENDER PETICION
            System.out.println("Creados flujos E/S");
            

            CMensaje datos;
            
            //BUCLE FLUJO DE ENTRADA
            while (!salir) {
                //s = (String) flujoEntrada.readObject();
                datos = (CMensaje)flujoEntrada.readObject();
                sl.checkStatus();
                sl.mensajeRecibido(datos, ip);
                if (datos.getMsgType() == CMensaje.MSG_OFFLINE){
                    salir = true;
                    sl.clienteDesconectado(Thread.currentThread(), this, ip);
                }
            }
            //Si sale del bucle desconectar todo
            closeAll();
        } 
        catch (IOException ioE) {
            System.out.println("Error E/S en " + this.getIP());
            System.out.println("DATOS DEL MENSAJE: ");
            System.out.println("CLASE: " + ioE.getClass().toString());
            ioE.printStackTrace();}
        catch (ClassNotFoundException e) {
            System.out.println("Clase no esperada en " + this.getIP());
            e.printStackTrace();}
    }
}