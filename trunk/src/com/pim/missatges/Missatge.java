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
import rice.p2p.commonapi.Message;

/**
 * Missatge estandar
 * 
 * @author Benet Joan Darder
 *
 */
public class Missatge implements Message{	
	private static final long serialVersionUID = -8482348361222915912L;
	/**
	 * D'on ve el missatge
	 */
	private Id from;
	/**
	 * Cap on va el missatge
	 */
	private Id to;

	public Missatge(){}
	
	public Missatge(Id from, Id to){
		this.from = from;
		this.to = to;
	}

	public String toString(){
		return "Missatge de: " + this.from + " a: "+this.to;
	}

	public int getPriority(){
		return Message.LOW_PRIORITY;
	}

	public Id getSender(){
		return this.from;
	}
}
