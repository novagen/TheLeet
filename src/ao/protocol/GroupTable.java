/*
 * GroupTable.java
 *
 *************************************************************************
 * Copyright 2010 Kevin Kendall
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
public class GroupTable {
    
    private HashMap<String, GroupID> m_nameToIDMap;
    private HashMap<GroupID, String> m_IDToNameMap;
    
    private final Object m_tableLock = new Object();
    
    /** Creates a new instance of AOGroupTable */
    public GroupTable() {
        m_nameToIDMap = new HashMap<String, GroupID>();
        m_IDToNameMap = new HashMap<GroupID, String>();
    }   // end AOGroupTable()
    
    public void reset() {
        synchronized (m_tableLock) { 
            m_nameToIDMap.clear();
            m_IDToNameMap.clear();
        }   // end synchronized
        
        System.gc();
    }   // end reset()
    
    public void add(byte[] id, String name) {
        synchronized (m_tableLock) { 
            m_nameToIDMap.put(name, new GroupID(id));
            m_IDToNameMap.put(new GroupID(id), name);
        }   // end synchronized
    }   // end add
    
    public byte[] getID(String name) {
        synchronized (m_tableLock) {
            if(m_nameToIDMap.get(name) == null){
                return null;
            } else {
                return m_nameToIDMap.get(name).getID();
            }
        }   // end synchronized
    }   // end getID
    
    public String getName(byte[] id) {
        synchronized (m_tableLock) { 
            return m_IDToNameMap.get( new GroupID(id) );
        }   // end synchronized
    }   // end getID
    
    private class GroupID {
        
        private final byte[] m_id;
        
        public GroupID(byte[] id) {
            if (id == null || id.length != 5) {
                throw new IllegalArgumentException("Invalid ID");
            } else {
                m_id = id;
            }   // end else
        }   // end GroupID()
        
        public boolean equals(Object obj) {
            if (obj instanceof GroupID) {
                byte[] id = ((GroupID)obj).getID();
                
                for (int i = 0; i < 5; ++i) {
                    if (m_id[i] != id[i]) { return false; }
                }   // end for
                
                return true;
            } else {
                return false;
            }   // end else
        }   // end equals()
        
        public int hashCode() {
            int result = 0;
            
            for (byte b : m_id) { result += b; }
            
            return result;
        }   // end hashCode()
        
        public byte[] getID() { return m_id; }
        
    }   // end class GroupID
    
}   // end class AOGroupTable