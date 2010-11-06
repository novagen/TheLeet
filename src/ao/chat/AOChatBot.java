/*
 * AOChatBot.java
 *
 * Created on September 14, 2010, 3:30 PM
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
package ao.chat;

import ao.protocol.packets.utils.AOSimplePacketFactory;
import ao.protocol.*;
import ao.protocol.packets.AOMessagePacket;
import ao.protocol.packets.AOPacket;
import ao.protocol.packets.bi.*;
import ao.protocol.packets.in.*;
import ao.protocol.packets.out.*;
import ao.protocol.packets.utils.AOPacketFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Vector;

import ao.event.EventListenerList;

public class AOChatBot implements AOBot {
    //Base variables
    private AOPacketFactory m_packetFactory;
    private Thread m_thread = null;
    private State m_state = State.DISCONNECTED;
    private String m_loginSeed = null;
    private AOCharacter m_character = null;
    private int m_pingDelay;
    //Connection
    private Socket m_socket = null;
    private DataInputStream m_in = null;
    private DataOutputStream m_out = null;
    //Events
    private final EventListenerList m_listeners = new EventListenerList();
    // used for synchronization
    private final Object m_readLock = new Object();
    private final Object m_writeLock = new Object();
    private final Object m_stateLock = new Object();
    private Vector<Object[]> lookupQueue = new Vector();
    //Chat related
    private AOCharacterIDTable chartable = new AOCharacterIDTable();
    private AOGroupTable grouptable = new AOGroupTable();
    //Tell information
    private int lastTellOut = -1;
    private int lastTellIn = -1;

    public enum Queue {
        TELL, FADD, FREM, FDEL, INVITE, KICK;
    }   // end enum State

    /** Creates a new instance of AOSimpleBot */
    public AOChatBot() {
        this(60000, new AOSimplePacketFactory());
    }   // end AOSimpleBot

    /** Creates a new instance of AOSimpleBot */
    public AOChatBot(int pingDelay, AOPacketFactory packetFactory) {
        m_pingDelay = pingDelay;
        m_packetFactory = packetFactory;
    }   // end AOSimpleBot

    public State getState() {
        synchronized (m_stateLock) {
            return m_state;
        }
    }   // end getState()

    public AOCharacter getCharacter() {
        return m_character;
    }

    public AOPacket nextPacket() throws IOException {
        synchronized (m_readLock) {
            if (m_state == State.DISCONNECTED) {
                throw new AOBotStateException(
                        "This bot is not currently connected to a server. It must be connected before packets can be read.",
                        m_state, State.CONNECTED);
            } else {
                try {
                    // Read and parse a packet from the input stream
                    short type = m_in.readShort();
                    short length = m_in.readShort();
                    byte[] data = new byte[length];
                    m_in.readFully(data);

                    AOPacket packet = m_packetFactory.toPacket(type, data);

                    // DEBUG: display that a packet was recieved
                    System.out.println("IN: " + packet);

                    // Return the packet
                    return packet;
                } catch (SocketTimeoutException ex) {
                    // Send a ping
                    sendPacket(new AOPingPacket("AOChatBot.java", AOPacket.Direction.OUT));
                    // Read a ping
                    AOPacket packet = nextPacket();
                    return packet;
                } catch (SocketException ex) {
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
                } catch (EOFException ex) {
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
                } // end catch
            }   // end synchronized 
        }   // end else
    }   // end nextPacket()

    public void sendPacket(AOPacket packet) throws IOException {
        synchronized (m_writeLock) {
            if (m_state == State.DISCONNECTED) {
                throw new AOBotStateException(
                        "This bot is not currently connected to a server. It must be connected before packets can be sent.",
                        m_state, State.CONNECTED);
            } else {
                try {
                    short type = packet.getType();
                    byte[] data = packet.getData();

                    m_out.writeShort(type);
                    m_out.writeShort(data.length);
                    m_out.write(data, 0, data.length);
                    m_out.flush();

                    // DEBUG: display that a packet was sent
                    System.out.println("OUT: " + packet);
                } catch (SocketException ex) {
                    if (m_socket.isClosed()) {
                        println("Connection Lost...");
                        disconnect();
                        return;
                    } else {
                        throw ex;
                    }   // end else
                }   // end catch
            }   // end else
        }   // end synchronized 
    }   // end sendPacket()

    public void connect(int dimension) throws IOException {
        connect(AODimensionAddress.values()[dimension - 1]);
    }   // end connect()

    public void connect(AODimensionAddress server) throws IOException {
        connect(server.getURL(), server.getPort());
    }   // end connect()

    public void connect(String server, int port) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.DISCONNECTED) {
                throw new AOBotStateException(
                        "This bot is already connected to a server. It must be disconnected before you can connect it to a server.",
                        m_state, State.DISCONNECTED);
            } else {
                try {
                    m_socket = new Socket(server, port);
                    m_in = new DataInputStream(m_socket.getInputStream());
                    m_out = new DataOutputStream(m_socket.getOutputStream());

                    m_socket.setSoTimeout(m_pingDelay);

                    m_state = State.CONNECTED;

                    AOPacket packet = nextPacket();
                    if (packet instanceof AOLoginSeedPacket) {
                        m_loginSeed = ((AOLoginSeedPacket) packet).getLoginSeed();
                        println("Connected");
                        fireConnected();
                    } else {
                        println("Failed to connect");
                        disconnect();
                    }   // end else
                    firePacket(packet);
                } catch (IOException ex) {
                    println("Failed to connect");
                    disconnect();
                    throw ex;
                }   // end catch
            }   // end else
        }   // end synchronized
    }   // end connect()

    public void authenticate(String userName, String password) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.CONNECTED) {
                throw new AOBotStateException(
                        "This bot is either already authenticated or disconnected. "
                        + "It must connected and unauthenticated to be authenticated.",
                        m_state, State.CONNECTED);
            } else {
                try {
                    String key = AOLoginKeyGenerator.generateLoginKey(m_loginSeed, userName, password);
                    AOPacket packet = new AOLoginRequestPacket(
                            AOLoginKeyGenerator.PROTOCOL_VERSION, userName, key);
                    sendPacket(packet);

                    packet = nextPacket();
                    if (packet instanceof AOCharListPacket) {
                        m_state = State.AUTHENTICATED;
                        println("Authenticated");
                        fireAuthenticated();
                    } else {
                        println("Failed to authenticate");
                        disconnect();
                    }   // end else
                    firePacket(packet);
                } catch (IOException ex) {
                    println("Failed to authenticate");
                    disconnect();
                    throw ex;
                }   // end catch
            }   // end else
        }   // end synchronized
    }   // end authenticate()

    public void login(AOCharacter character) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.AUTHENTICATED) {
                throw new AOBotStateException(
                        "This bot is either already logged in or unauthenticated. "
                        + "It must connected, authenticated, and logged out in order to be logged in.",
                        m_state, State.AUTHENTICATED);
            } else {
                try {
                    AOPacket packet = new AOLoginSelectPacket(character.getID());
                    sendPacket(packet);

                    packet = nextPacket();
                    if (packet instanceof AOLoginOkPacket) {
                        m_state = State.LOGGED_IN;
                        m_character = character;

                        // Encourage garbage collection
                        System.runFinalization();
                        System.gc();

                        println("Logged in");
                        fireLoggedIn();
                    } else {
                        println("Failed to log in");
                        disconnect();
                    }   // end else
                    firePacket(packet);
                } catch (IOException ex) {
                    println("Failed to log in");
                    disconnect();
                    throw ex;
                }   // end catch
            }   // end else
        }   // end synchronized
    }   // end login()

    public void disconnect() throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.DISCONNECTED) {
                if (m_socket != null) {
                    m_socket.close();
                    m_in.close();
                    m_out.close();

                    m_socket = null;
                    m_in = null;
                    m_out = null;
                    
                    if (m_thread != null) {
                       //m_thread.stop();
                       m_thread.interrupt();
                    }
                }   // end if

                m_loginSeed = null;
                m_character = null;

                m_state = State.DISCONNECTED;

                // Encourage garbage collection
                System.runFinalization();
                System.gc();

                println("Disconnected");
                fireDisconnected();
            }   // end if
        }   // end synchronized
    }   // end disconnect()

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        disconnect();
    }   // end finalize()

    public void start() {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can be started.",
                        m_state, State.LOGGED_IN);
            } else {
                if (m_thread == null || !m_thread.isAlive()) {
                    m_thread = new Thread(this);
                    m_thread.start();
                    println("Started");
                    fireStarted();
                }   // end if
            }   // end else
        }   // end synchronized
    }   // end start()

    public void run() {
        if (m_state != State.LOGGED_IN) {
            throw new AOBotStateException("This bot is not currently logged in.", getState(), State.LOGGED_IN);
        }
        AOPacket packet = null;
        // Start listening for packets
        while (getState() == State.LOGGED_IN) {
            try {
                packet = nextPacket();
                if (packet instanceof AOClientNamePacket) {
                    AOClientNamePacket namePacket = (AOClientNamePacket) packet;
                    chartable.add(namePacket.getCharacterID(), namePacket.getCharacterName());
                } else if (packet instanceof AOClientLookupPacket) {
                    AOClientLookupPacket lookPacket = (AOClientLookupPacket) packet;
                    chartable.add(lookPacket.getCharacterID(), lookPacket.getCharacterName());
                    searchQueue(chartable.getID(lookPacket.getCharacterName()), lookPacket.getCharacterName());
                    if(lookPacket.getCharacterID() == -1){
                        println("Character "+ lookPacket.getCharacterName() + " does not exist");
                    }
                } else if (packet instanceof AOGroupAnnouncePacket) {
                    AOGroupAnnouncePacket msgPacket = (AOGroupAnnouncePacket) packet;
                    grouptable.add(msgPacket.getGroupID(), msgPacket.getGroupName());
                    //Fire packets off to any Message Packet listeners
                } else if (packet instanceof AOGroupMessagePacket) {
                    AOGroupMessagePacket msgPacket = (AOGroupMessagePacket) packet;
                    fireGroupMessage(msgPacket);
                } else if (packet instanceof AOPrivateMessagePacket) {
                    AOPrivateMessagePacket msgPacket = (AOPrivateMessagePacket) packet;
                    lastTellIn = msgPacket.getCharID();
                    firePrivateMessage(msgPacket);
                } else if (packet instanceof AOPrivateGroupMessagePacket) {
                    AOPrivateGroupMessagePacket msgPacket = (AOPrivateGroupMessagePacket) packet;
                    firePrivateGroupMessage(msgPacket);
                }
                firePacket(packet);
                Thread.sleep(1);
            } catch (Exception e) {
                fireException(e);
            }
        }
    }

    /**
     * Actions
     */
    private synchronized void enQueue(Queue type, String name, String msg) {
        Object[] temp = new Object[3];
        temp[0] = type;
        temp[1] = name;
        temp[2] = msg;
        lookupQueue.add(temp);
    }

    private synchronized void searchQueue(int id, String name) {
        for (int i = 0; i < lookupQueue.size(); i++) {
            Object[] current = lookupQueue.elementAt(i);
            Queue type = (Queue) current[0];
            String lname = (String) current[1];
            String msg = (String) current[2];
            if (id != -1 && lname.compareTo(name) == 0) {//Verify that the name is valid
                try {
                    switch (type) {
                        case TELL:
                            sendTell(lname, msg, false);
                            break;
                        case FADD:
                            addFriend(lname, false);
                            break;
                        case FREM:
                            removeFriend(lname, false);
                            break;
                        case FDEL:
                            deleteFriend(lname, false);
                            break;
                        case INVITE:
                            inviteUser(lname, false);
                            break;
                        case KICK:
                            kickUser(lname, false);
                            break;
                        default:
                            System.out.println("Error: invalid lookup in queue");
                            break;
                    }
                } catch (Exception e) {
                    fireException(e);
                }
                lookupQueue.removeElementAt(i);
                i--;
            }
        }
    }

    public void sendGMsg(String channel, String msg) throws IOException {
        byte[] id = grouptable.getID(channel);
        if (id != null) {
            sendGMsg(id, msg);
        }
    }

    public void sendGMsg(byte[] channel, String msg) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send messages.",
                        m_state, State.LOGGED_IN);
            } else if(msg.compareTo("") != 0) {
                AOPacket packet = new AOGroupMessagePacket(channel, msg);
                sendPacket(packet);
            }
        }
    }

    public void sendPMsg(String channel, String msg) throws IOException {
        if (chartable.getID(channel) != null && chartable.getID(channel) != -1) {
            sendPMsg(chartable.getID(channel), msg);
        }
    }

    public void sendPMsg(int channel, String msg) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send messages.",
                        m_state, State.LOGGED_IN);
            } else if(msg.compareTo("") != 0) {
                AOPacket packet = new AOPrivateGroupMessagePacket(channel, msg);
                sendPacket(packet);
            }
        }
    }

    public void lookup(String name) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can perform a lookup.",
                        m_state, State.LOGGED_IN);
            } else {
                AOPacket packet = new AOClientLookupPacket(name);
                sendPacket(packet);
            }
        }
    }

    public void sendTell(String name, String msg, boolean lookup) throws IOException {
        int id = -1;
        if (chartable.getID(name) != null) {
            id = chartable.getID(name);
        }
        if (id != -1) {
            sendTell(id, msg);
        } else if (lookup) {
            enQueue(Queue.TELL, name, msg);
            lookup(name);
        }
    }

    public void sendTell(int id, String msg) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send tells.",
                        m_state, State.LOGGED_IN);
            } else if(msg.compareTo("") != 0) {
                AOPacket packet = new AOPrivateMessagePacket(id, msg, AOMessagePacket.Direction.OUT);
                sendPacket(packet);
                firePacket(packet);
                lastTellOut = id;
            }
        }
    }

    public void joinGroup(String channel) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send messages.",
                        m_state, State.LOGGED_IN);
            } else {
                if (chartable.getID(channel) != null && chartable.getID(channel) != -1) {
                    AOPacket packet = new AOPrivateGroupJoinPacket(chartable.getID(channel));
                    sendPacket(packet);
                }
            }
        }
    }

    public void joinGroup(int channel) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send messages.",
                        m_state, State.LOGGED_IN);
            } else {
                AOPacket packet = new AOPrivateGroupJoinPacket(channel);
                sendPacket(packet);
            }
        }
    }

    public void leaveGroup(String channel) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send messages.",
                        m_state, State.LOGGED_IN);
            } else {
                if (chartable.getID(channel) != null && chartable.getID(channel) != -1) {
                    AOPacket packet = new AOPrivateGroupPartPacket(chartable.getID(channel));
                    sendPacket(packet);
                }
            }
        }
    }

    public void leaveGroup(int channel) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send messages.",
                        m_state, State.LOGGED_IN);
            } else {
                AOPacket packet = new AOPrivateGroupPartPacket(channel);
                sendPacket(packet);
            }
        }
    }

    public void addFriend(String name, boolean lookup) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before you can add a friend.",
                        m_state, State.LOGGED_IN);
            } else {
                int id = -1;
                if (chartable.getID(name) != null) {
                    id = chartable.getID(name);
                }
                if (id != -1) {
                    AOPacket packet = new AOFriendUpdatePacket(id, true);
                    sendPacket(packet);
                } else if (lookup) {
                    AOPacket packet = new AOClientLookupPacket(name);
                    //Add to friend update queue
                    enQueue(Queue.FADD, name, "");
                    sendPacket(packet);
                }
            }
        }
    }

    public void removeFriend(String name, boolean lookup) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before you can remove a friend.",
                        m_state, State.LOGGED_IN);
            } else {
                int id = -1;
                if (chartable.getID(name) != null) {
                    id = chartable.getID(name);
                }
                if (id != -1) {
                    AOPacket packet = new AOFriendUpdatePacket(id, false);
                    sendPacket(packet);
                } else if (lookup) {
                    AOPacket packet = new AOClientLookupPacket(name);
                    //Add to friend update queue
                    enQueue(Queue.FREM, name, "");
                    sendPacket(packet);
                }
            }
        }
    }

    public void deleteFriend(String name, boolean lookup) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can delete a friend.",
                        m_state, State.LOGGED_IN);
            } else {
                int id = -1;
                if (chartable.getID(name) != null) {
                    id = chartable.getID(name);
                }
                if (id != -1) {
                    AOPacket packet = new AOFriendRemovePacket(id);
                    sendPacket(packet);
                } else if (lookup) {
                    AOPacket packet = new AOClientLookupPacket(name);
                    //Add to friend remove queue
                    enQueue(Queue.FDEL, name, "");
                    sendPacket(packet);
                }
            }
        }
    }

    public void clearFriends() throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before you can clear your temporary friends.",
                        m_state, State.LOGGED_IN);
            } else {
                AOPacket packet = new AOChatCommandPacket("rem buddy ?");
                sendPacket(packet);
            }
        }
    }

    public void inviteUser(String name, boolean lookup) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send tells.",
                        m_state, State.LOGGED_IN);
            } else {
                int id = -1;
                if (chartable.getID(name) != null) {
                    id = chartable.getID(name);
                }
                if (id != -1) {
                    AOPacket packet = new AOPrivateGroupInvitePacket(id);
                    sendPacket(packet);
                } else if (lookup) {
                    AOPacket packet = new AOClientLookupPacket(name);
                    //Add to group invite queue
                    enQueue(Queue.INVITE, name, "");
                    sendPacket(packet);
                }
            }
        }
    }

    public void kickUser(String name, boolean lookup) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send tells.",
                        m_state, State.LOGGED_IN);
            } else {
                int id = -1;
                if (chartable.getID(name) != null) {
                    id = chartable.getID(name);
                }
                if (id != -1) {
                    AOPacket packet = new AOPrivateGroupKickPacket(id);
                    sendPacket(packet);
                } else if (lookup) {
                    AOPacket packet = new AOClientLookupPacket(name);
                    //Add to group kick queue
                    enQueue(Queue.KICK, name, "");
                    sendPacket(packet);
                }
            }
        }
    }

    public void kickAll() throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send tells.",
                        m_state, State.LOGGED_IN);
            } else {
                AOPacket packet = new AOPrivateGroupKickAllPacket();
                sendPacket(packet);
            }
        }
    }

    public void acceptInvite(String channel) throws IOException {
        if (chartable.getID(channel) != null && chartable.getID(channel) != -1) {
            acceptInvite(chartable.getID(channel));
        }
    }

    public void acceptInvite(int channel) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send tells.",
                        m_state, State.LOGGED_IN);
            } else {
                if (channel != -1) {
                    AOPacket packet = new AOPrivateGroupJoinPacket(channel);
                    sendPacket(packet);
                }
            }
        }
    }

    public void denyInvite(String channel) throws IOException {
        if (chartable.getID(channel) != null && chartable.getID(channel) != -1) {
            denyInvite(chartable.getID(channel));
        }
    }

    public void denyInvite(int channel) throws IOException {
        synchronized (m_stateLock) {
            if (m_state != State.LOGGED_IN) {
                throw new AOBotStateException(
                        "This bot is not currently logged in, it must be logged in before it can send tells.",
                        m_state, State.LOGGED_IN);
            } else {
                if (channel != -1) {
                    AOPacket packet = new AOPrivateGroupPartPacket(channel);
                    sendPacket(packet);
                }
            }
        }
    }

    public AOCharacterIDTable getCharTable() {
        return chartable;
    }

    public AOGroupTable getGroupTable() {
        return grouptable;
    }

    public int getLastTellIn(){
        return lastTellIn;
    }

    public int getLastTellOut(){
        return lastTellOut;
    }

    /**
     * Listeners
     */
    public void addListener(AOBotListener l) {
        m_listeners.add(AOBotListener.class, l);
    }   // end addListener

    public void removeListener(AOBotListener l) {
        m_listeners.remove(AOBotListener.class, l);
    }   // end removeListener()

    public void addGMsgListener(AOGroupMsgListener l) {
        m_listeners.add(AOGroupMsgListener.class, l);
    }

    public void removeGMsgListener(AOGroupMsgListener l) {
        m_listeners.remove(AOGroupMsgListener.class, l);
    }

    public void addPMsgListener(AOPrivateMsgListener l) {
        m_listeners.add(AOPrivateMsgListener.class, l);
    }

    public void removePMsgListener(AOPrivateMsgListener l) {
        m_listeners.remove(AOPrivateMsgListener.class, l);
    }

    public void addPGMsgListener(AOPrivateGroupMsgListener l) {
        m_listeners.add(AOPrivateGroupMsgListener.class, l);
    }

    public void removePGMsgListener(AOPrivateGroupMsgListener l) {
        m_listeners.remove(AOPrivateGroupMsgListener.class, l);
    }

    /** 
     * Fires a connected event to all listeners 
     * @see ao.protocol.AOBotListener#connected(ao.protocol.AOBot)
     */
    protected void fireConnected() {
        AOBotListener[] listeners = m_listeners.getListeners(AOBotListener.class);
        for (AOBotListener l : listeners) {
            l.connected(this);
        }   // end for
    }   // end fireConnected()

    /** 
     * Fires a authenticated event to all listeners 
     * @see ao.protocol.AOBotListener#authenticated(ao.protocol.AOBot)
     */
    protected void fireAuthenticated() {
        AOBotListener[] listeners = m_listeners.getListeners(AOBotListener.class);
        for (AOBotListener l : listeners) {
            l.authenticated(this);
        }   // end for
    }   // end fireAuthenticated()

    /** 
     * Fires a logged in event to all listeners 
     * @see ao.protocol.AOBotListener#loggedIn(ao.protocol.AOBot)
     */
    protected void fireLoggedIn() {
        AOBotListener[] listeners = m_listeners.getListeners(AOBotListener.class);
        for (AOBotListener l : listeners) {
            l.loggedIn(this);
        }   // end for
    }   // end fireLoggedIn()

    /** 
     * Fires a started event to all listeners 
     * @see ao.protocol.AOBotListener#started(ao.protocol.AOBot)
     */
    protected void fireStarted() {
        AOBotListener[] listeners = m_listeners.getListeners(AOBotListener.class);
        for (AOBotListener l : listeners) {
            l.started(this);
        }   // end for
    }   // end fireStarted()

    /** 
     * Fires a disconnected event to all listeners 
     * @see ao.protocol.AOBotListener#disconnected(ao.protocol.AOBot)
     */
    protected void fireDisconnected() {
        AOBotListener[] listeners = m_listeners.getListeners(AOBotListener.class);
        for (AOBotListener l : listeners) {
            l.disconnected(this);
        }   // end for
    }   // end fireDisconnected()

    protected void firePacket(AOPacket packet) {
        AOBotListener[] listeners = m_listeners.getListeners(AOBotListener.class);
        for (AOBotListener l : listeners) {
            l.packet(this, packet);
        }   // end for
    }

    protected void fireException(Exception e) {
        AOBotListener[] listeners = m_listeners.getListeners(AOBotListener.class);
        for (AOBotListener l : listeners) {
            l.exception(this, e);
        }   // end for
    }

    protected void fireGroupMessage(AOGroupMessagePacket packet) {
        AOGroupMsgListener[] listeners = m_listeners.getListeners(AOGroupMsgListener.class);
        for (AOGroupMsgListener l : listeners) {
            l.groupMsgPacket(this, packet);
        }   // end for
    }

    protected void firePrivateMessage(AOPrivateMessagePacket packet) {
        AOPrivateMsgListener[] listeners = m_listeners.getListeners(AOPrivateMsgListener.class);
        for (AOPrivateMsgListener l : listeners) {
            l.privateMsgPacket(this, packet);
        }   // end for
    }

    protected void firePrivateGroupMessage(AOPrivateGroupMessagePacket packet) {
        AOPrivateGroupMsgListener[] listeners = m_listeners.getListeners(AOPrivateGroupMsgListener.class);
        for (AOPrivateGroupMsgListener l : listeners) {
            l.privateGroupMsgPacket(this, packet);
        }   // end for
    }

    /**
     * Loggers
     */
    public void addLogger(AOBotLogger l) {
        m_listeners.add(AOBotLogger.class, l);
    }   // end addListener

    public void removeLogger(AOBotLogger l) {
        m_listeners.remove(AOBotLogger.class, l);
    }   // end removeListener()

    /** 
     * Tells all loggers to print a line-terminator
     * @see ao.protocol.AOBotLogger#println(ao.protocol.AOBot)
     */
    protected void println() {
        AOBotLogger[] listeners = m_listeners.getListeners(AOBotLogger.class);
        for (AOBotLogger l : listeners) {
            l.println(this);
        }   // end for
    }   // end println()

    /** 
     * Tells all loggers to print astring followed by a line-terminator
     * @see ao.protocol.AOBotLogger#println(ao.protocol.AOBot, String)
     */
    protected void println(String msg) {
        AOBotLogger[] listeners = m_listeners.getListeners(AOBotLogger.class);
        for (AOBotLogger l : listeners) {
            l.println(this, msg);
        }   // end for
    }   // end println()

    /** 
     * Tells all loggers to print a string
     * @see ao.protocol.AOBotLogger#print(ao.protocol.AOBot, String)
     */
    protected void print(String msg) {
        AOBotLogger[] listeners = m_listeners.getListeners(AOBotLogger.class);
        for (AOBotLogger l : listeners) {
            l.print(this, msg);
        }   // end for
    }   // end println()
}   // end class AOSimpleBot

