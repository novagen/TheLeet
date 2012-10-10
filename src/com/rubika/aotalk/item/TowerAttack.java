/*
 * ChatMessage.java
 *
 *************************************************************************
 * Copyright 2010 Christofer Engel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rubika.aotalk.item;

import java.util.Comparator;

public class TowerAttack {
    private int id;
    private String zone;
    private String attacker;
    private String attackerFaction;
    private String defenderFaction;
    private String attackerOrg;
    private String defenderOrg;
    private String sitename;
    private int minlevel;
    private int maxlevel;
    private long timestamp;
    
    public TowerAttack() {
    }
    
    public TowerAttack(int id, long timestamp, String zone, String attacker, String attackerFaction, String defenderFaction, String attackerOrg, String defenderOrg, String sitename, int minlevel, int maxlevel) {
	    super();
	    this.zone = zone;
	    this.id = id;
	    this.timestamp = timestamp;
	    this.attacker = attacker;
	    this.attackerFaction = attackerFaction;
	    this.defenderFaction = defenderFaction;
	    this.attackerOrg = attackerOrg;
	    this.defenderOrg = defenderOrg;
	    this.sitename = sitename;
	    this.minlevel = minlevel;
    	this.maxlevel = maxlevel;
    }
    
    public int getId() {
    	return this.id;
    }
    
    public long getTimestamp() {
    	return this.timestamp;
    }
    
    public String getZone() {
    	return this.zone;
    }
    
    public String getAttacker() {
    	return this.attacker;
    }
    
    public String getAttackerFaction() {
    	return this.attackerFaction;
    }
    
    public String getDefenderFaction() {
    	return this.defenderFaction;
    }
    
    public String getAttackerOrg() {
    	return this.attackerOrg;
    }
    
    public String getDefenderOrg() {
    	return this.defenderOrg;
    }
   
    public String getSitename() {
    	return this.sitename;
    }
    
    public int getMinlevel() {
    	return this.minlevel;
    }
    
    public int getMaxlevel() {
    	return this.maxlevel;
    }

    public static class CustomComparator implements Comparator<TowerAttack> {
	    @Override
	    public int compare(TowerAttack object1, TowerAttack object2) {
	        return object1.getZone().compareTo(object2.getZone());
	    }
	}
}