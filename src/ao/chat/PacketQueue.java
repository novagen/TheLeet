/*
 * PacketQueue.java
 *
 * Created on October 10, 2011, 11:20 PM
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

import ao.protocol.Client.ClientState;
import ao.protocol.packets.Packet;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class PacketQueue implements Runnable {

    private int delay;
    private boolean run = true;
    private long lastsent = System.currentTimeMillis();
    private int queueSize = 5;
    private final Object queueLock = new Object();
    private Queue<Packet> queue = new LinkedList<Packet>();
    private ChatClient client;

    public PacketQueue(ChatClient client) {
        this(client, 3000);
    }
    
    public PacketQueue(ChatClient client, int delay) {
        this.client = client;
        this.delay = delay;
    }

    public void add(Packet p) {
        queue.add(p);
    }

    public void stop() {
        run = false;
    }

    public boolean canSend() {
        synchronized (queueLock) {
            if (queueSize > 0) {
                queueSize--;
                lastsent = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        }
    }

    public void run() {
        while (run) {
            synchronized (queueLock) {
                if (queueSize < 5 && lastsent + delay < System.currentTimeMillis()) {
                    queueSize++;
                }
                try {
                    if (queue.size() > 0 && queueSize > 0 && client.getState() != ClientState.DISCONNECTED) {
                        Packet packet = queue.poll();
                        client.sendPacket(packet);
                    }
                } catch (IOException ex) {
                    client.fireException(ex);
                }
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
            }
        }
    }
}
