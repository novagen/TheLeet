/*
 * AONameFormat.java
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

public class AONameFormat {
    public static String format(String s) {
        String temp = s.toLowerCase();
        temp = temp.substring(0, 1).toUpperCase() + temp.substring(1);
        return temp;
    }
}
