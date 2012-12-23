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

import rice.p2p.commonapi.Id;

/**
 * Classe que modela un missatge privat.
 * Hereta de la classe <pre>com.pim.missatges.Missatge</pre>
 * 
 * @author Benet Joan Darder
 *
 */
public class MissatgePrivat extends Missatge {

	private static final long serialVersionUID = 5188350225893572405L;
	private String contingut;
	
	/**
	 * Constructor del missatge
	 * 	
	 * @param from qui ho envia
	 * @param to a on ho envia
	 */
	public MissatgePrivat(Id from, Id to) {
		super(from, to);
		this.contingut = new String();
	}
	
	/**
	 * Constructor del missatge
	 * 
	 * @param from qui ho envia
	 * @param to a on ho envia
	 * @param contingut que s'envia
	 */
	public MissatgePrivat(Id from, Id to, String contingut) {
		super(from, to);
		this.contingut = contingut;
	}
	
	/**
	 * Mètode que retorna el contingut del missatge
	 *  
	 * @return contingut del missatge
	 */
	public String getContingut(){
		return this.contingut;
	}
}
