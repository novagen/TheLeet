/*
 * CharacterIDTable.java
 *
 * Created on March 28, 2008, 2:20 PM
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

import java.util.HashMap;

/**
 * <p></p>
 *
 * @author Paul Smith
 */
public class CharacterIDTable {
    
    private HashMap<String, Integer> m_nameToIDMap;
    private HashMap<Integer, String> m_IDToNameMap;
    
    private final Object m_tableLock = new Object();
    
    /** Creates a new instance of AOCharacterIDTable */
    public CharacterIDTable() {
        m_nameToIDMap = new HashMap<String, Integer>(134);
        m_IDToNameMap = new HashMap<Integer, String>(134);
    }   // end AOCharacterIDTable()
    
    public void reset() {
        synchronized (m_tableLock) { 
            m_nameToIDMap.clear();
            m_IDToNameMap.clear();
        }   // end synchronized
        
        System.gc();
    }   // end reset()
    
    public void add(int id, String name) {
        add( Integer.valueOf(id), name );
    }   // end add
    
    public void add(Integer id, String name) {
        synchronized (m_tableLock) { 
            m_nameToIDMap.put(name, id);
            m_IDToNameMap.put(id, name);
        }   // end synchronized
    }   // end add
    
    public Integer getID(String name) {
        synchronized (m_tableLock) { 
            return m_nameToIDMap.get(name);
        }   // end synchronized
    }   // end getID
    
    public String getName(int id) {
        return getName( Integer.valueOf(id) );
    }   // end getID
    
    public String getName(Integer id) {
        synchronized (m_tableLock) { 
            return m_IDToNameMap.get(id);
        }   // end synchronized
    }   // end getID
    
}   // end class AOCharacterIDTable
