/*
 * Plugin.java
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

public interface Plugin {
    String getName();
    String[] getCommands();
    void config(CommandDesc desc);
    int getPermissions(String command);
    void setBotName(String name);
    void setPrefix(String prefix);
    String help(Command command);
    void uses(PluginUser user);
    boolean execute(Command command) throws Exception;
}