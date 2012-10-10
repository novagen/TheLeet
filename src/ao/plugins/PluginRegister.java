/*
 * PluginRegister.java
 *
 * Created on July 28, 2011
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

package ao.plugins;

import java.util.ArrayList;

public class PluginRegister {

    private static ArrayList<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
    
    public static boolean Register(String classname){
        try {
            Class.forName(classname);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    public static boolean Register(String classname, ClassLoader loader){
        try {
            Class.forName(classname, true, loader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    public static void Register(Class<?> p){
        Class<? extends Plugin> c;
        try{
            c = p.asSubclass(Plugin.class);
            plugins.add(c);
        } catch (Exception ex){
        }
    }

    public static ArrayList<Class<? extends Plugin>> getPlugins() {
        return plugins;
    }
    
    public static void Clear(){
        plugins.clear();
    }
}