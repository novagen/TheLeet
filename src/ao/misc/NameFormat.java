/*
 * NameFormat.java
 *
 * Created on Sep 19, 2010, 1:53 PM
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

package ao.misc;

public class NameFormat {
    public static String format(String s) {
        String[] tokens = s.toLowerCase().split(" ");
        String temp = "";
        for(String token : tokens){
            temp = temp + " " + token.substring(0, 1).toUpperCase() + token.substring(1);
        }
        temp = temp.substring(1);
        return temp;
    }
}
