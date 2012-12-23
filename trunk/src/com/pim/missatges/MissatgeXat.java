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
package com.pim.missatges;

import rice.p2p.commonapi.NodeHandle;


/**
 * Classe que modela un missatge de tipus <pre>com.pim.missatges.MissatgeScribe</pre>.
 * 
 * @author Benet Joan Darder
 *
 */
public class MissatgeXat extends MissatgeScribe{
	
	private static final long serialVersionUID = 8415628263338234041L;
	private String alies;
	private String contingut;
	
	/**
	 * Constructor del missatge amb paràmetres
	 * @param from node origen del missatge
	 * @param seq num de seqüència del missatge
	 * @param contingut Contingut del missatge
	 * @param alies del qui envia el missatge
	 */
	public MissatgeXat(NodeHandle from, int seq, String contingut, String alies){
		super(from, seq);
		this.alies = alies;
		this.contingut = contingut;
	}
	/**
	 * Mètode que retorna el contingut del missatge 
	 * @return contingut del missatge
	 */
	public String getContingut() {
		return this.contingut;
	}
	
	/**
	 * Mètode que retorna el node d'on s'ha enviat el missatge
	 * @return node origen
	 */
	public NodeHandle getFrom(){
		return this.from;
	}
	
	/**
	 * Mètode que modela l'objecte <pre>MissatgeXat</pre> a tipus <pre>String</pre>
	 */
	public String toString(){
		return this.alies+" diu: "+this.contingut;
	}

}
