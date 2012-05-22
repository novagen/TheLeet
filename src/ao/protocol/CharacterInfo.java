/*
 * CharacterInfo.java
 *
 * Created on July 21, 2007, 2:15 PM
 *************************************************************************
 * Copyright 2008 Paul Smith
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

package ao.protocol;

/**
 * <p>AOCharacter stores the ID, name, and level of a character.</p>
 *
 * @author Paul Smith
 */
public class CharacterInfo {
    
    private final int    m_id;
    private final String m_name;
    private final int    m_level;
    private final int    m_online;
    
    /** 
     * Creates a new instance of AOCharacter 
     *
     * @param id 
     *        the id of this character
     * @param name
     *        the name of this character
     * @param level
     *        the level of this character
     * @param online
     *        ???
     */
    public CharacterInfo(int id, String name, int level, int online) {
        m_id     = id;
        m_name   = name;
        m_level  = level;
        m_online = online;
    }   // end AOCharacter()
    
    /** Returns the ID of this character. */
    public int getID() { return m_id; }
    /** Returns the name of this character. */
    public String getName() { return m_name; }
    /** Returns the level of this character. */
    public int getLevel() { return m_level; }
    public int getOnline() { return m_online; }
    
    public String toString() {        
        return m_name;
    }   // end toString()
    
    public String toString(boolean showID) {        
        return m_name + (showID ? " (" + Integer.toHexString(m_id) + ")" : "");
    }   // end toString()
    
}   // end class AOCharacter
