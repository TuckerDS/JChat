/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tuckerds.jchat;

/** Interfaz de eventos de SServidor
 *  @author tucker
 *  @author tuckerds.com
 *  @see CServidor
 */
public interface ServidorListener {
    public void eventoMensajeRecibido(CMensaje m, String ip);
    public void eventoClienteDesconectado(String ip);
    public void eventoNuevoClienteConectado(String ip);
}
