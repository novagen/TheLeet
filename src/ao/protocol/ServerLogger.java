/*
 * ServerLogger.java
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

package ao.protocol;

import ao.chat.ServerConnection;
import java.util.EventListener;

/**
 * ServerLogger provides functionality for receiving log data from a server connection.
 *
 * @author Kevin Kendall
 * @see ao.protocol.Client
 */
public interface ServerLogger extends EventListener {
    /** Prints a string. */
    void print(ServerConnection con, String msg);
}   // end interface BotLogger
