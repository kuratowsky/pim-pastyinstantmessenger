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
package com.pim;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Vector;

import rice.environment.Environment;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

import com.pim.canal.Canal;
import com.pim.scribe.PimScribeClient;

public class PIM {
	private Vector<PimScribeClient> apps = new Vector<PimScribeClient>();

	/**
	 * Crea tants de PastryNodes com el valor del paràmetre numNodes. Si existeix un anell ja creat, els nodes s'hi afegiran, si no s'en crearà un de nou.
	 * Un cop creat l'anell, tots els nodes s'els hi aplica funcionalitat Scribe i es subscriuen a un canal per defecte.  
	 * 
	 * @param bindport Port local per enllaçar
	 * @param bootaddress IP:port executar el node 
	 * @param numNodes nombre de nodes a llançar
	 * @param env Medi
	 * @param canal Canal a subscriure
	 * @throws Exception 
	 */
	public PIM(int bindport, InetSocketAddress bootaddress, int numNodes, Environment env, Canal canal) throws Exception {
		// Construeix en PastryNodeFactory
		PastryNodeFactory factory = new SocketPastryNodeFactory(new RandomNodeIdFactory(env), bindport, env);

		// bulcle del constructor de nodes/apps
		for (int curNode = 0; curNode < numNodes; curNode++) {
			// Construeix un nou node
			PastryNode node = factory.newNode();
			// Construeix una nova aplicació Scribe
			PimScribeClient app = new PimScribeClient(node);
			apps.add(app);

			node.boot(bootaddress);
			// El node ha d'enviar varis missatges per entrar dins l'anell.
			synchronized(node) {
				while(!node.isReady() && !node.joinFailed()) {
					// esperem 
					node.wait(500);		          
					// aborten si no pot entrar
					if (node.joinFailed()) {
						throw new IOException("Could not join the FreePastry ring.  Reason:"+node.joinFailedReason()); 
					}
				}       
			}
		}
		// Subscrivim les aplicacions Scribe a un canal per defecte
		for(PimScribeClient app:apps){
			app.subscribe(canal.getName(), env);
		}		
		env.getTimeSource().sleep(1000);
	}

	/**
	 * Retorna un vector amb tots els nodes Scribe
	 * @return els nodes scribe
	 */
	public Vector<PimScribeClient> getApps(){
		return apps;
	}

	/**
	 * Retorna un node Scribe del vector de nodes
	 * 
	 * @param i index del vector
	 * @return un node tipus PimScribeClient
	 */
	public PimScribeClient getAppByIndex(int i){
		return apps.get(i);
	}

	/**
	 * Destrueix un node en concret del vector de nodes.
	 * 
	 * @param i index del vector corresponent al node a destruir
	 */
	public void deleteAppByIndex(int i){
		PimScribeClient delapp = getAppByIndex(i);
		((PastryNode)delapp.getNode()).destroy();
		apps.remove(i);

	}
	/**
	 * Destrueix tots els nodes. S'utilitza quan tanquen l'aplicació, per alliberar tots els recursos utilitzats.
	 */
	public void destroyAllNodes() {
		for(int i=0; i<apps.size();i++){
			deleteAppByIndex(i);
		}
	}

	/**
	 * Sortim del canal
	 * @param i posició que ocupa el canal dins el vector de canalsSubscrits
	 * @param canal canal del que volem sortir
	 */
	public void sortirDelCanal(int i, String canal){
		PimScribeClient appSortirCanal = getAppByIndex(i);
		appSortirCanal.unsubscribe(canal);
	}
}
