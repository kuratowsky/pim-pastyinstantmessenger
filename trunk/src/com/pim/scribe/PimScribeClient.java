/*******************************************************************************
 * Copyright (c) 2012 Benet Joan Darder.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Benet Joan Darder - initial API and implementation
 ******************************************************************************/
package com.pim.scribe;

import java.util.Iterator;
import java.util.Vector;

import com.pim.canal.Canal;
import com.pim.missatges.Missatge;
import com.pim.missatges.MissatgePrivat;
import com.pim.missatges.MissatgeScribe;
import com.pim.missatges.MissatgeXat;

import rice.environment.Environment;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.ScribeClient;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeImpl;
import rice.p2p.scribe.Topic;


/**
 * Classe que implementa <pre>ScribeClient</pre> per poder rebre missatges <pre>Scribe</pre> 
 * i <pre>Application</pre> per poder rebre missatges de node a node, anomenem-lo privat.
 * 
 * @author Benet Joan Darder
 *
 */
public class PimScribeClient implements ScribeClient, Application{
	private int seqNum = 0;	
	private Scribe pimScribe;
	private Endpoint endpoint;
	private Vector<Canal> canalsSubscrits = new Vector<Canal>();
	private String alies;
	private Node node;
	private StringBuffer missatges;

	/**
	 * Constructor de client Scribe.
	 * @param node de tipus <pre>PastryNode</pre>
	 */
	public PimScribeClient(Node node) {
		this.endpoint = node.buildEndpoint(this, "PIMinstance");
		this.node = node;
		this.pimScribe = new ScribeImpl(node,"PIMScribeInstance");		
		this.endpoint.register();
		this.missatges = new StringBuffer();		
	}

	/**
	 * Retorna l'endpoint de l'aplicació
	 * @return enpoint
	 */
	public Endpoint getEndPoint(){
		return this.endpoint;
	}
		
	/**
	 * Retorna els canals en els que l'aplicació está subscrita
	 * @return vector amb tots els canals que l'aplicació s'ha subscrit
	 */
	public Vector<Canal> getCanalsSubscrits(){
		return this.canalsSubscrits;
	}
	
	/**
	 * Retorna l'implementació Scribe 
	 * @return Scribe
	 */
	public Scribe getScribe(){
		return pimScribe;
	}
	
	/**
	 * Retorna el node 
	 * @return node
	 */
	public Node getNode(){
		return node;
	}
	
	/**
	 * Retorna els missatges enviats/rebuts de l'aplicació
	 * @return missatges
	 */
	public String getMissatges(){
		return this.missatges.toString();
	}
		
	/**
	 * Mètode per subscriure a un canal. 
	 * Detecta si un node ja està subscrit a un canal. Si hi està no el deixa subscriure.
	 * 
	 * @param nomDelCanal Nom del canal que volem entrar
	 * @param env Medi
	 * @return booleà, si s'ha subscrit true si ja hi estava false
	 */
	@SuppressWarnings("deprecation")
	public boolean subscribe(String nomDelCanal, Environment env) {
		
		Iterator<Canal> cit = this.canalsSubscrits.iterator();
		boolean existeix = false;
		while (cit.hasNext()&& !existeix){
			Canal c = cit.next();
			if(nomDelCanal.equals(c.getName()))	existeix = true;			
		}
		if (!existeix){		
			Canal canal = new Canal(nomDelCanal, env);
			this.canalsSubscrits.add(canal);
			this.pimScribe.subscribe(canal.getTopic(), this);
			return true;
		}else return false;
	}
	
	/**
	 * Mètode per sortir del canal
	 * 
	 * @param nomDelCanal nom del canal del que volem sortir
	 */
	@SuppressWarnings("deprecation")
	public void unsubscribe(String nomDelCanal){
		Iterator<Canal> cit = this.canalsSubscrits.iterator();
		while (cit.hasNext()){
			Canal c = cit.next();
			if(nomDelCanal.equals(c.getName())){
				this.pimScribe.unsubscribe(c.getTopic(), this);
				cit.remove();
			}
		}
	}

	/**
	 * Envia missatges multicast a tot un canal en concret.
	 * @param canal al que volem enviar el missatge
	 * @param txt contingut del missatge
	 * @return el missatge enviat
	 */
	public String sendMulticast(Canal canal, String txt) {
		sendMulticast(canal.getTopic(),txt);
		missatges.append("Missatge ").append(this.seqNum).append(" enviat al canal ").append(canal.getName()).append(" diu: ").append(txt).append("\n");
		return missatges.toString();
	}
	
	/**
	 * Envia missatges multicast a tot un canal en concret.
	 * @param tpc topic al que volem enviar el missatge
	 * @param txt el missatge a enviar
	 */
	public void sendMulticast(Topic tpc, String txt) {
		MissatgeXat missatge = new MissatgeXat(this.endpoint.getLocalNodeHandle(), this.seqNum, txt);
		pimScribe.publish(tpc, missatge); 
		this.seqNum++;
	}
	
	/**
	 * Mètode per enviar missatges directament a un node en concret.
	 * 
	 * @param nh <pre>NodeHandle</pre> del destinatari
	 * @param msg Missatge a enviar
	 * @return Missatge enviat
	 */
	public String routeMyMsgDirect(NodeHandle nh, String msg){
		Id idFrom = this.endpoint.getId();
		Id idTo = nh.getId();
		Message missatge = new MissatgePrivat(idFrom, idTo, msg);
		this.endpoint.route(null, missatge, nh);
		missatges.append("Missatge privat a ").append(idTo.toString()).append(" diu: ").append(msg).append("\n");
		return missatges.toString();
	}

	/**
	 * Mètode per enviar missatges directament a un node en concret.
	 * 
	 * @param idTo <pre>Id</pre> del destinatari
	 * @param msg Missatge a enviar
	 * @return Missatge enviat
	 */
	public String routeMyMsgDirect(Id idTo, String msg){
		Message missatge = new MissatgePrivat(this.endpoint.getId(), idTo, msg);
		this.endpoint.route(idTo, missatge, null);
		missatges.append("Missatge privat a ").append(idTo.toString()).append(" diu: ").append(msg).append("\n");
		return missatges.toString();
	}

	/**
	 * Mètode que s'executa quan rebem els missatges <pre>Scribe</pre>.
	 */
	public void deliver(Topic topic, ScribeContent content) {
		missatges.append("Missatge rebut al canal ").append(topic).append(" de ").append(((MissatgeXat)content).getFrom().getId().toString()).append(" diu ").append(((MissatgeXat)content).getContingut()).append(" \n");
		if (((MissatgeScribe)content).getSender() == null) {
			new Exception("Stack Trace").printStackTrace();
		}
	}
	
	/**
	 * Mètode que s'executa quan rebem els missatges privats (de node a node).
	 */
	@Override
	public void deliver(Id id, Message msg) {
		System.out.println("Id: " + id.toString() + " msg: " + msg);
		missatges.append("Missatge privat rebut de ").append(((Missatge)msg).getSender().toString()).append(" diu ").append(((MissatgePrivat)msg).getContingut()).append(" \n");		
	}
	
	/**
	 * Aquest mètode ha de retornar <pre>true</pre>!!!. 
	 * Quin mal de cap! no me funcionava la recepció de node a node. Era perque el mètode retornava <pre>false</pre>.
	 */
	@Override
	public boolean forward(RouteMessage arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void update(NodeHandle arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean anycast(Topic arg0, ScribeContent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void childAdded(Topic arg0, NodeHandle arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void childRemoved(Topic arg0, NodeHandle arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribeFailed(Topic arg0) {
		// TODO Auto-generated method stub

	}	
}
