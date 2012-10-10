/*
 * PluginList.java
 *
 * Created on Sep 16, 2010, 4:00 PM
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
package ao.plugins;

import ao.plugins.Reply.Channel;
import ao.misc.AOML;
import java.util.ArrayList;

public class PluginList {

    private ArrayList<Plugin> plugins = new ArrayList<Plugin>();
    private PluginUser user;
    private boolean cononly;
    private String prefix;
    private String name;

    public PluginList(PluginUser u, String p, String n) {
        user = u;
        cononly = false;
        prefix = p;
        name = n;
    }

    public PluginList(PluginUser u, String p, String n, boolean con) {
        user = u;
        cononly = con;
        prefix = p;
        name = n;
    }

    public String helpMenu(int permission) {
        String help = "";
        for (Plugin plugin : plugins) {
            if (permission >= plugin.getPermissions(null)) {
                help += "<font color='#00ff00'>" + plugin.getName() + "</font>\n\n";

                String[] commands = plugin.getCommands();
                for (String command : commands) {
                    if (permission >= plugin.getPermissions(command)) {
                        help += AOML.Chatcmd(command, "/tell " + name + " " + prefix + "help " + command) + "\n";
                    }
                }   // end for
                help += "\n";
            }
        }   // end for
        return AOML.Blob("Help Menu", help);
    }

    public int getModules() {
        return plugins.size();
    }

    public void add(Plugin plugin) {
        plugin.setBotName(name);
        plugin.setPrefix(prefix);
        plugin.uses(user);
        plugins.add(plugin);
    }
    
    public void setBotName(String name){
        this.name = name;
        for(Plugin plugin : plugins){
            plugin.setBotName(name);
        }
    }
    
    public void setPrefix(String prefix){
        this.prefix = prefix;
        for(Plugin plugin : plugins){
            plugin.setPrefix(prefix);
        }
    }

    public Plugin getModule(int i) {
        return plugins.get(i);
    }

    public void remove(Plugin module) {
        plugins.remove(module);
    }

    public void remove(int i) {
        plugins.remove(i);
    }

    public boolean execute(Command c) {
        boolean executed = false;
        if (!cononly || c.getChannel() == Channel.CON) {
            //Help menu
            if (c.getName().toLowerCase().compareTo("help") == 0 && (c.getArgs() == null || c.getArgs().length <= 0)) {
                Reply.send(name, user, c.getChannel(), c.getChannelid(), c.getCharacterid(), helpMenu(c.getPermission()));
                executed = true;
            //Command information
            } else if (c.getName().toLowerCase().compareTo("help") == 0 && c.getArgs().length > 0 && c.getArgs()[0] != null) {
                for (Plugin plugin : plugins) {
                    String[] commands = plugin.getCommands();
                    for (String cmd : commands) {
                        if (cmd.compareTo(c.getArgs()[0]) == 0) {
                            Reply.send(name, user, c.getChannel(), c.getChannelid(), c.getCharacterid(), plugin.help(c));
                            executed = true;
                        }
                    }
                }
            //Execute a command
            } else {
                for (Plugin plugin : plugins) {
                    String[] commands = plugin.getCommands();
                    for (String cmd : commands) {
                        if (cmd.compareTo(c.getName()) == 0 && c.getPermission() >= plugin.getPermissions(cmd)) {
                            try {
                                executed = plugin.execute(c);
                            } catch (Exception e) {
                                user.exception(name, e);
                            }
                        }
                    }
                }
            }
        }
        return executed;
    }
}
