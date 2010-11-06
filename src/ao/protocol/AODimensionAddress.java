/*
 * AODimensionAddress.java
 *
 * Created on March 18, 2008, 11:42 AM
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
 * <p>AODimensionAddress is primarily a utility class that stores
 * the urls and ports of RK1, RK2, and RK3.</p>
 * 
 * <p>FIXME: The url and port pairs seem to be incorrect for RK2 and RK3.</p>
 * 
 * @author Paul Smith
 */
public enum AODimensionAddress {
    RK1("Atlantean (Rubi-Ka 1)",         "chat.d1.funcom.com", 7101),
    RK2("Rimor (Rubi-Ka 2)",             "chat.d2.funcom.com", 7102),
    RK3("Die Neue Welt (German Server)", "chat.d3.funcom.com",  7103),
    TEST("Test-Live (Test Server)",      "chat.dt.funcom.com", 7109);
    
    private final String m_name;
    private final String m_url;
    private final int    m_port;
    
    /**
     * Creates a new instance of AODimensionAddress
     * 
     * 
     * @param name
     *        the name of the server
     * @param url
     *        the url of the server
     * @param port
     *        the port to connect to on the server
     */
    private AODimensionAddress(String name, String url, int port) {
        m_name = name;
        m_url  = url;
        m_port = port;
    }   // end AODimensionAddress()
    
    /** Returns the name of the server */
    public String getName() { return m_name; }
    /** Returns the url of the server */
    public String getURL() { return m_url; }
    /** Returns the port to connect to on the server */
    public int getPort() { return m_port; }
    
    public String toString() { return m_name; }
    
}   // end enum AODimensionAddress
