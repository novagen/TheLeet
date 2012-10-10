/*
 * Database.java
 *
 * Created on March 3, 2011, 12:30 PM
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

package ao.db;

import java.sql.Connection;
import java.sql.ResultSet;

public interface Database {

    public void connect() throws Exception;

    public void disconnect() throws Exception;

    public void update(String statement) throws Exception;

    public ResultSet query(String statement) throws Exception;

    public Connection getConnection();

    public boolean isConnected();
}
