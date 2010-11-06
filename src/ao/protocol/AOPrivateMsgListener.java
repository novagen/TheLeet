/*
 * AOBotListener.java
 *
 * Created on September 16, 2010, 2:38 PM
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

import java.util.EventListener;
import ao.protocol.packets.bi.AOPrivateMessagePacket;

/**
 * AOBotListener provides functionality for responding to bot events.
 *
 * @author Paul Smith
 * @see ao.protocol.AOBot
 */
public interface AOPrivateMsgListener extends EventListener {
    void privateMsgPacket(AOBot bot, AOPrivateMessagePacket packet);
}   // end interface AOBotListener
