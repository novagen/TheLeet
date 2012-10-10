/*
 * ServerConnection.java
 *
 * Created on June 22, 2011
 *************************************************************************
 * Copyright 2011 Kevin Kendall
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
package ao.chat;

import ao.event.EventListenerList;
import ao.protocol.ServerListener;
import ao.protocol.ServerLogger;
import ao.protocol.ServerStateException;
import ao.protocol.packets.Packet;
import ao.protocol.packets.bi.PingPacket;
import ao.protocol.packets.utils.PacketFactory;
import ao.protocol.packets.utils.SimplePacketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 *
 * @author SITS_13
 */
public class ServerConnection implements Runnable {

    private PacketFactory m_packetFactory;
    private Socket m_socket;
    private ServerState m_state = ServerState.DISCONNECTED;
    private boolean m_debug;
    //Connection
    private DataOutputStream m_out;
    private DataInputStream m_in;
    private boolean timingout = false;
    //Events
    private final EventListenerList m_listeners = new EventListenerList();
    // used for synchronization
    private final Object m_readLock = new Object();
    private final Object m_writeLock = new Object();
    private final Object m_stateLock = new Object();
    private final Object m_timeoutLock = new Object();

    public enum ServerState {

        DISCONNECTED, CONNECTED;
    }   // end enum State

    public ServerConnection(Socket socket, boolean debug) {
        m_socket = socket;
        m_debug = debug;
        m_packetFactory = new SimplePacketFactory();
    }

    public ServerState getState() {
        return m_state;
    }

    public void run() {
        try {
            m_out = new DataOutputStream(m_socket.getOutputStream());
            m_in = new DataInputStream(m_socket.getInputStream());
            m_state = ServerState.CONNECTED;
        } catch (IOException ex) {
            m_state = ServerState.DISCONNECTED;
            fireException(ex);
        }
        while (getState() != ServerState.DISCONNECTED) {
            try {
                Packet packet = nextPacket();
                if (packet instanceof PingPacket) {
                    PingPacket ping = (PingPacket) packet;
                    synchronized (m_timeoutLock) {
                        if (ping.getDirection() == Packet.Direction.TO_CLIENT) {
                            timingout = false;
                        }
                    }
                }
                firePacket(packet);
            } catch (IOException ex) {
                fireException(ex);
            }
            Thread.yield();
        }
    }

    public Packet nextPacket() throws IOException {
        synchronized (m_readLock) {
            try {
                // Read and parse a packet from the input stream
                short type = m_in.readShort();
                short length = m_in.readShort();
                byte[] data = new byte[length];
                m_in.readFully(data);

                Packet packet = m_packetFactory.toPacket(type, data);

                // DEBUG: display that a packet was recieved
                if (m_debug) {
                    System.out.println("IN: " + packet);
                }

                // Return the packet
                return packet;
            } catch (SocketTimeoutException ex) {
                if (m_state != ServerState.DISCONNECTED) {
                    synchronized (m_timeoutLock) {
                        if (!timingout) {
                            timingout = true;
                            // Send a ping
                            sendPacket(new PingPacket("Java AOChat API ping", Packet.Direction.TO_SERVER));
                            // Read a ping
                            Packet packet = nextPacket();
                            return packet;
                        } else {
                            synchronized (m_stateLock) {
                                if (m_socket == null || m_socket.isClosed()) {
                                    println("Connection Lost...");
                                    disconnect();
                                    return null;
                                } else {
                                    println("Connection Lost...");
                                    disconnect();
                                    throw ex;
                                }   // end else
                            }   // end synchronized
                        }
                    }
                } else {
                    return null;
                }
            } catch (SocketException ex) {
                if (m_socket == null || m_socket.isClosed()) {
                    println("Connection Lost...");
                    disconnect();
                    return null;
                } else {
                    println("Connection Lost...");
                    disconnect();
                    throw ex;
                }   // end else
            } catch (EOFException ex) {
                if (m_socket == null || m_socket.isClosed()) {
                    println("Connection Lost...");
                    disconnect();
                    return null;
                } else {
                    println("Connection Lost...");
                    disconnect();
                    throw ex;
                }   // end else
            } // end catch
        }   // end else
    }   // end nextPacket()

    public void sendPacket(Packet packet) throws IOException {
        synchronized (m_writeLock) {
            if (m_state == ServerState.DISCONNECTED) {
                throw new ServerStateException(
                        "The client is not currently connected to the server. It must be connected before packets can be sent.",
                        m_state, ServerState.CONNECTED);
            } else {
                try {
                    short type = packet.getType();
                    byte[] data = packet.getData();

                    m_out.writeShort(type);
                    m_out.writeShort(data.length);
                    m_out.write(data, 0, data.length);
                    m_out.flush();

                    // DEBUG: display that a packet was sent
                    if (m_debug) {
                        System.out.println("OUT: " + packet);
                    }
                } catch (SocketException ex) {
                    if (m_socket.isClosed()) {
                        println("Connection Lost...");
                        disconnect();
                        return;
                    } else {
                        throw ex;
                    }   // end else
                }   // end catch
            }
        }   // end synchronized 
    }   // end sendPacket()

    public void disconnect() throws IOException {
        synchronized (m_stateLock) {
            m_state = ServerState.DISCONNECTED;
            m_socket.close();
        }

        // Encourage garbage collection
        System.runFinalization();
        System.gc();

        println("Disconnected");
        fireDisconnected();
    }

    public void addListener(ServerListener listener) {
        m_listeners.add(ServerListener.class, listener);
    }

    public void removeListener(ServerListener l) {
        m_listeners.remove(ServerListener.class, l);
    }   // end removeListener()

    protected void fireDisconnected() {
        ServerListener[] listeners = m_listeners.getListeners(ServerListener.class);
        for (ServerListener l : listeners) {
            l.disconnected(this);
        }   // end for
    }   // end fireDisconnected()

    protected void firePacket(Packet packet) {
        ServerListener[] listeners = m_listeners.getListeners(ServerListener.class);
        for (ServerListener l : listeners) {
            l.packet(this, packet);
        }   // end for
    }

    protected void fireException(Exception e) {
        ServerListener[] listeners = m_listeners.getListeners(ServerListener.class);
        for (ServerListener l : listeners) {
            l.exception(this, e);
        }   // end for
    }

    protected void println(String msg) {
        ServerLogger[] listeners = m_listeners.getListeners(ServerLogger.class);
        for (ServerLogger l : listeners) {
            l.print(this, msg);
        }   // end for
    }   // end println()
}
