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
import rice.p2p.scribe.ScribeContent;

/**
 * Missatge de broadcast indicant el NodeHandler d'origen que demana altres canals oberts.
 * 
 * @author Benet Joan Darder
 *
 */
public class MissatgeScribe implements ScribeContent{
	
	private static final long serialVersionUID = -6424703050299263807L;
	/**
	 * L'origen del contingut
	 */
	NodeHandle from;
	/**
	 * Num de seqüència del contingut
	 */
	int seq;
	
	/**
	 * Constructor MissatgeScribe
	 * 
	 * @param from Qui ha enviat el missatge
	 * @param seq Num de seqüència del contingut
	 */
	public MissatgeScribe(NodeHandle from, int seq) {
		this.from = from;
		this.seq = seq;
    }
    
    public String toString() {
    	return "PIM MissatgeScribe #"+this.seq+" amb origen "+this.from;
    }

    /**
     * Qui ha enviat el missatge
     * @return objecte nodeHandle
     */
    public NodeHandle getSender() {
    	return this.from;
    }
}
