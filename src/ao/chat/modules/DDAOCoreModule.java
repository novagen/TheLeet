/*
 * DDAOCoreModule.java
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
import ao.misc.*;

public class DDAOCoreModule implements AOModule {

    private String name = "DDAO Core";
    private String[] commands = {
        "invite",
        "kick",
        "kickall",
        "tell",
        "t",
        "reply",
        "r",
        "group",
        "g",
        "private",
        "p",
        "leave",
        "friend",
        "f"
    };

    public DDAOCoreModule() {
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

        if (command.compareTo(commands[0]) == 0) {//invite
            help += "invites a user to your private group channel";
        } else if (command.compareTo(commands[1]) == 0) {//kick
            help += "kicks a user from your private group channel";
        } else if (command.compareTo(commands[2]) == 0) {//kickall
            help += "kicks everyone from your private group channel";
        } else if (command.compareTo(commands[3]) == 0) {//tell
            help += "sends a tell to a user";
        } else if (command.compareTo(commands[4]) == 0) {//t
            help += "sends a tell to the last user you sent a tell to";
        } else if (command.compareTo(commands[5]) == 0 || command.compareTo(commands[6]) == 0) {//reply/r
            help += "sends a reply tell to the last user who sent you a tell";
        } else if (command.compareTo(commands[7]) == 0 || command.compareTo(commands[8]) == 0) {//group/g
            help += "sends a message to a group channel";
        } else if (command.compareTo(commands[9]) == 0 || command.compareTo(commands[10]) == 0) {//private/p
            help += "sends a message to a private group channel";
        } else if (command.compareTo(commands[11]) == 0) {//leave
            help += "leaves a private group channel";
        } else if (command.compareTo(commands[12]) == 0 || command.compareTo(commands[13]) == 0) {//friend/f
            help += "lets you add, remove, delete, clear friends";
        }

        help += "<br>";
        help += "\">" + command + " Help</a>";
        return help;
    }

    public void execute(AOChatBot chatbot, AOModuleUser user, Channel channel, byte[] id, String prefix, String command, String[] args) {
        try {
            if (channel == Channel.CON) {
                if (command.compareTo(commands[0]) == 0 && args != null) {//invite
                    for (String s : args) {
                        chatbot.inviteUser(s, true);
                    }
                } else if (command.compareTo(commands[1]) == 0 && args != null) {//kick
                    for (String s : args) {
                        chatbot.kickUser(s, true);
                    }
                } else if (command.compareTo(commands[2]) == 0) {//kickall
                    chatbot.kickAll();
                } else if (command.compareTo(commands[3]) == 0 && args != null && args.length > 1) {//tell
                    String msg = "";
                    for (String part : args) {
                        if(msg.compareTo("") == 0){
                            msg = " ";
                        } else {
                            msg = msg + " " + part;
                        }
                    }
                    msg = msg.substring(2);
                    chatbot.sendTell(AONameFormat.format(args[0]), msg, true);
                } else if (command.compareTo(commands[4]) == 0 && args != null && args.length > 0) {//t
                    String msg = "";
                    for (String part : args) {
                        msg = msg + " " + part;
                    }
                    msg = msg.substring(1);
                    chatbot.sendTell(chatbot.getLastTellOut(), msg);
                } else if (command.compareTo(commands[5]) == 0 && args != null && args.length > 0) {//reply
                    String msg = "";
                    for (String part : args) {
                        msg = msg + " " + part;
                    }
                    msg = msg.substring(1);
                    chatbot.sendTell(chatbot.getLastTellIn(), msg);
                } else if (command.compareTo(commands[6]) == 0 && args != null && args.length > 0) {//r
                    String msg = "";
                    for (String part : args) {
                        msg = msg + " " + part;
                    }
                    msg = msg.substring(1);
                    chatbot.sendTell(chatbot.getLastTellIn(), msg);
                } else if (command.compareTo(commands[7]) == 0 && args != null && args.length > 1) {//group
                    String msg = "";
                    for (String part : args) {
                        if(msg.compareTo("") == 0){
                            msg = " ";
                        } else {
                            msg = msg + " " + part;
                        }
                    }
                    msg = msg.substring(2);
                    chatbot.sendGMsg(AONameFormat.format(args[0]), msg);
                } else if (command.compareTo(commands[8]) == 0 && args != null && args.length > 1) {//g
                    String msg = "";
                    for (String part : args) {
                        if(msg.compareTo("") == 0){
                            msg = " ";
                        } else {
                            msg = msg + " " + part;
                        }
                    }
                    msg = msg.substring(2);
                    chatbot.sendGMsg(AONameFormat.format(args[0]), msg);
                } else if (command.compareTo(commands[9]) == 0 && args != null && args.length > 1) {//private
                    String msg = "";
                    for (String part : args) {
                        if(msg.compareTo("") == 0){
                            msg = " ";
                        } else {
                            msg = msg + " " + part;
                        }
                    }
                    msg = msg.substring(2);
                    chatbot.sendPMsg(AONameFormat.format(args[0]), msg);
                } else if (command.compareTo(commands[10]) == 0 && args != null && args.length > 1) {//p
                    String msg = "";
                    for (String part : args) {
                        if(msg.compareTo("") == 0){
                            msg = " ";
                        } else {
                            msg = msg + " " + part;
                        }
                    }
                    msg = msg.substring(2);
                    chatbot.sendPMsg(AONameFormat.format(args[0]), msg);
                } else if (command.compareTo(commands[11]) == 0 && args != null) {//leave
                    for (String s : args) {
                        chatbot.leaveGroup(AONameFormat.format(s));
                    }
                } else if (command.compareTo(commands[12]) == 0 && args != null && args.length > 0) {//friend
                    if(args[0].compareTo("add") == 0 || args[0].compareTo("a") == 0 && args.length == 2){
                        chatbot.addFriend(AONameFormat.format(args[1]), true);
                    } else if (args[0].compareTo("remove") == 0 || args[0].compareTo("rem") == 0 || args[0].compareTo("r") == 0 && args.length == 2){
                        chatbot.removeFriend(AONameFormat.format(args[1]), true);
                    } else if (args[0].compareTo("delete") == 0 || args[0].compareTo("del") == 0 || args[0].compareTo("d") == 0 && args.length == 2){
                        chatbot.deleteFriend(AONameFormat.format(args[1]), true);
                    } else if (args[0].compareTo("clear") == 0 || args[0].compareTo("clr") == 0 || args[0].compareTo("c") == 0 && args.length == 1){
                        chatbot.clearFriends();
                    }
                } else if (command.compareTo(commands[13]) == 0 && args != null && args.length > 0) {//f
                    if(args[0].compareTo("add") == 0 || args[0].compareTo("a") == 0 && args.length == 2){
                        chatbot.addFriend(AONameFormat.format(args[1]), true);
                    } else if (args[0].compareTo("remove") == 0 || args[0].compareTo("rem") == 0 || args[0].compareTo("r") == 0 && args.length == 2){
                        chatbot.removeFriend(AONameFormat.format(args[1]), true);
                    } else if (args[0].compareTo("delete") == 0 || args[0].compareTo("del") == 0 || args[0].compareTo("d") == 0 && args.length == 2){
                        chatbot.deleteFriend(AONameFormat.format(args[1]), true);
                    } else if (args[0].compareTo("clear") == 0 || args[0].compareTo("clr") == 0 || args[0].compareTo("c") == 0 && args.length == 1){
                        chatbot.clearFriends();
                    }
                }
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
