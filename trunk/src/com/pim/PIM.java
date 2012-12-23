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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import rice.environment.Environment;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

import com.pim.canal.Canal;
import com.pim.scribe.PimScribeClient;

public class PIM {
	private Vector<PimScribeClient> apps = new Vector<PimScribeClient>();

	public PIM(int bindport, InetSocketAddress bootaddress, int numNodes, Environment env, Canal canal) throws Exception {
		// Construeix en PastryNodeFactory
		PastryNodeFactory factory = new SocketPastryNodeFactory( new RandomNodeIdFactory(env), bindport, env);

		// bulcle del constructor de nodes/apps
		for (int curNode = 0; curNode < numNodes; curNode++) {
			// construeix un nou node
			PastryNode node = factory.newNode();
			// construeix una nova aplicació Scribe
			PimScribeClient app = new PimScribeClient(node);
			apps.add(app);
			
			node.boot(bootaddress);
			// the node may require sending several messages to fully boot into the ring
			synchronized(node) {
				while(!node.isReady() && !node.joinFailed()) {
					// delay so we don't busy-wait
					node.wait(500);		          
					// abort if can't join
					if (node.joinFailed()) {
						throw new IOException("Could not join the FreePastry ring.  Reason:"+node.joinFailedReason()); 
					}
				}       
			}
			//System.out.println("Node creat correctament: " + node);
		}
		//subscrivim les aplicacions Scribe a un canal per defecte
		for(PimScribeClient app:apps){
			app.subscribe(canal.getName(), env);
		}		
		env.getTimeSource().sleep(1000);
	}

	/**
	 * Retorna un vector amb els nodes Scribe
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
	
	public void sortirDelCanal(int i, String canal){
		PimScribeClient appSortirCanal = getAppByIndex(i);
		appSortirCanal.unsubscribe(canal);
	}
	
	public static void printTree(Vector<PimScribeClient> apps) {
		Hashtable<NodeHandle, PimScribeClient> appTable = new Hashtable<NodeHandle, PimScribeClient>();
		Iterator<PimScribeClient> i = apps.iterator();

		while (i.hasNext()) {
			PimScribeClient app = (PimScribeClient) i.next();
			appTable.put(app.getEndPoint().getLocalNodeHandle(), app);
		}
		NodeHandle seed = ((PimScribeClient) apps.get(0)).getEndPoint().getLocalNodeHandle();

		// get the root
		NodeHandle root = getRoot(seed, appTable);

		System.out.println("Print Children");
		// print the tree from the root down
		recursivelyPrintChildren(root, 0, appTable);

	}

	/**
	 * Recursively crawl up the tree to find the root.
	 */
	public static NodeHandle getRoot(NodeHandle seed, Hashtable<NodeHandle, PimScribeClient> appTable) {
		PimScribeClient app = (PimScribeClient) appTable.get(seed);
		if (app.isRoot())
			return seed;
		NodeHandle nextSeed = app.getParent();
		return getRoot(nextSeed, appTable);
	}

	/**
	 * Print's self, then children.
	 */
	public static void recursivelyPrintChildren(NodeHandle curNode,
			int recursionDepth, Hashtable<NodeHandle, PimScribeClient> appTable) {
		// print self at appropriate tab level
		String s = "";
		for (int numTabs = 0; numTabs < recursionDepth; numTabs++) {
			s += "  ";
		}
		s += curNode.getId().toString();
		System.out.println(s);

		// recursively print all children
		PimScribeClient app = (PimScribeClient) appTable.get(curNode);
		NodeHandle[] children = app.getChildren();
		for (int curChild = 0; curChild < children.length; curChild++) {
			recursivelyPrintChildren(children[curChild], recursionDepth + 1, appTable);
		}
	}

	

}
