/*
 * AOBotLogger.java
 *
 * Created on July 24, 2007, 9:39 PM
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

import java.util.EventListener;

/**
 * AOBotLogger provides functionality for recieving log data from a bot.
 *
 * @author Paul Smith
 * @see ao.protocol.AOBot
 */
public interface AOBotLogger extends EventListener {
    
    /** Prints a line terminator. */
    void println(AOBot bot);
    /** Prints a string followed by a line terminator. */
    void println(AOBot bot, String msg);
    /** Prints a string. */
    void print(AOBot bot, String msg);
    
}   // end interface AOBotLogger
