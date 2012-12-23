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
package com.pim.canal;

import rice.environment.Environment;
import rice.p2p.scribe.Topic;
import rice.pastry.commonapi.PastryIdFactory;

/**
 * Classe Canal. 
 * Creació de canals.
 *  
 * @author Benet Joan Darder
 *
 */
public class Canal {

    private Topic topic;
    private String channelName;

    /**
     * Constructor de Canal
     * @param channelName Nom del canal
     * @param env Medi
     */
    public Canal(String channelName, Environment env) {

		this.channelName = channelName;
		this.topic = new Topic(new PastryIdFactory(env), channelName);
    }
    public Canal() {}
    
    /* Getters i setters */   

	/**
     * Retorna el nom del Canal
     * @return String amb el nom del canal
     */
    public String getName() {
    	return channelName;
    }

    /**
     * Retorna l'objecte Topic del canal
     * @return objecte Topic
     */
    public Topic getTopic() {
    	return topic;
    }
}
