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
 * Missatge de broadcast via subscribe.
 * 
 * @author Benet Joan Darder
 *
 */
public class MissatgeXat extends MissatgeScribe{
	
	private static final long serialVersionUID = 8415628263338234041L;
	private String alies;
	private String contingut;
	
	public MissatgeXat(NodeHandle from, int seq, String contingut, String alies){
		super(from, seq);
		this.alies = alies;
		this.contingut = contingut;
	}

	public String getContingut() {
		return this.contingut;
	}
	
	public NodeHandle getFrom(){
		return this.from;
	}
	
	public String toString(){
		return this.alies+" diu: "+this.contingut;
	}

}
