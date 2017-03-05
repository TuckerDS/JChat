/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tuckerds.jchat;

/** Interfaz de eventos de CCiente
 *  @author tucker
 *  @author tuckerds.com
 *  @see CCliente
 */
public interface ClienteListener {
    public void eventoMensajeRecibido(CMensaje m); 
}
