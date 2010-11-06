/*
 * AOCoreModule.java
 *
 * Created on Sep 16, 2010, 4:30 PM
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

package ao.chat.modules;

import ao.chat.AOChatBot;
import ao.protocol.AOBotStateException;
import ao.chat.modules.AOModuleList.Channel;
import ao.misc.AOByteConvert;

public class AOCoreModule implements AOModule {

    private String name = "Core";
    private String[] commands = {"prefix", "invite", "kick", "kickall", "tell", "relay"};

    public AOCoreModule() {
    }

    public String getName() {
        return name;
    }

    public String[] getCommands() {
        return commands;
    }

    public String help(AOChatBot chatbot, String prefix, String command) {
        String help = "<a href=\"text://";
        help += "<font color='#00ff00'>" + command + "</font><br><br>";

        if (command.compareTo(commands[0]) == 0) {//prefix
            help += "prefix shows the currently set command prefix (useful since /tell's do not require a command prefix)";
        } else if (command.compareTo(commands[1]) == 0) {//invite
            help += "invites a user to the bot's private group";
        } else if (command.compareTo(commands[2]) == 0) {//kick
            help += "kicks a user from the bot's private group";
        } else if (command.compareTo(commands[3]) == 0) {//kickall
            help += "kicks all users from the bot's private group";
        } else if (command.compareTo(commands[4]) == 0) {//tell
            help += "sends a tell to a user";
        } else if (command.compareTo(commands[5]) == 0) {//relay
            help += "relays a message to the bots private group";
        }

        help += "<br>";
        help += "\">" + command + " Help</a>";
        return help;
    }

    public void execute(AOChatBot chatbot, AOModuleUser user, Channel channel, byte[] id, String prefix, String command, String[] args) {
        try {
            if (command.compareTo(commands[0]) == 0) {//prefix
                System.out.println("0");
                reply(chatbot, user, channel, id, "This bots prefix is " + prefix);
            } else if (command.compareTo(commands[1]) == 0 && args != null) {//invite
                System.out.println("1");
                for (String name : args) {
                    chatbot.inviteUser(name, true);
                }
            } else if (command.compareTo(commands[2]) == 0 && args != null) {//kick
                System.out.println("2");
                for (String name : args) {
                    chatbot.kickUser(name, true);
                }
            } else if (command.compareTo(commands[3]) == 0) {//kickall
                System.out.println("3");
                chatbot.kickAll();
            } else if (command.compareTo(commands[4]) == 0 && args != null && args.length > 1) {//tell
                System.out.println("4");
                String msg = "";
                for (String part : args) {
                    if (msg.compareTo("") == 0) {
                        msg = "Relayed:";
                    } else {
                        msg = msg + " " + part;
                    }
                }
                chatbot.sendTell(args[0], msg, true);
            } else if (command.compareTo(commands[5]) == 0 && args != null && args.length > 1) {//relay
                System.out.println("5");
                String msg = "";
                for (String part : args) {
                        msg = msg + part;
                }
                chatbot.sendPMsg(chatbot.getCharacter().getID(), msg);
            }
        } catch (Exception e) {
            user.exception(chatbot, e);
        }
    }

    private void reply(AOChatBot chatbot, AOModuleUser user, Channel channel, byte[] id, String msg) {
        if (chatbot == null) {
            throw new NullPointerException("A reference to the bot was not supplied.");
        }

        // Make sure the bot is logged in
        AOChatBot.State botState = chatbot.getState();
        if (botState != AOChatBot.State.LOGGED_IN) {
            throw new AOBotStateException("The bot must be logged in to perform this action.", botState, AOChatBot.State.LOGGED_IN);
        }   // end if

        try {
            switch (channel) {
                case TELL:
                    chatbot.sendTell(AOByteConvert.byteToInt(id), msg);
                    break;
                case PRIVATE:
                    chatbot.sendPMsg(AOByteConvert.byteToInt(id), msg);
                    break;
                case GROUP:
                    chatbot.sendGMsg(id, msg);
                    break;
                case CON:
                    user.println(chatbot, msg);
                    break;
                default:
                    System.out.println("Channel reply error");
            }
        } catch (Exception e) {
            user.exception(chatbot, e);
        }
    }
}
