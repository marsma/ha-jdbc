/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2004 Paul Ferraro
 * 
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation; either version 2.1 of the License, or (at your 
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact: ferraro@users.sourceforge.net
 */
package net.sf.ha.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author  Paul Ferraro
 * @version $Revision$
 * @since   1.0
 */
public class DatabaseClusterDescriptor
{
	private String name;
	private String validateSQL = "SELECT 1";
	private Set activeDatabaseSet = new LinkedHashSet();
	private Map databaseMap = Collections.synchronizedMap(new HashMap());
	
	/**
	 * @return Returns the databaseSet.
	 */
	public Map getDatabaseMap()
	{
		return this.databaseMap;
	}
	
	/**
	 * @param databaseSet The databaseSet to set.
	 */
	public void addDatabase(Object object)
	{
		Database database = (Database) object;
		this.databaseMap.put(database.getId(), database);
		this.activeDatabaseSet.add(database);
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return Returns the validateSQL.
	 */
	public String getValidateSQL()
	{
		return this.validateSQL;
	}
	
	/**
	 * @param validateSQL The validateSQL to set.
	 */
	public void setValidateSQL(String validateSQL)
	{
		this.validateSQL = validateSQL;
	}
	
	public Database firstDatabase() throws SQLException
	{
		synchronized (this.activeDatabaseSet)
		{
			if (this.activeDatabaseSet.size() == 0)
			{
				throw new SQLException("No active databases in cluster");
			}
			
			return (Database) this.activeDatabaseSet.iterator().next();
		}
	}
	
	public Database nextDatabase() throws SQLException
	{
		synchronized (this.activeDatabaseSet)
		{
			Database database = this.firstDatabase();
			
			if (this.activeDatabaseSet.size() > 1)
			{
				this.activeDatabaseSet.remove(database);
				
				this.activeDatabaseSet.add(database);
			}
			
			return database;
		}
	}

	public List getActiveDatabaseList() throws SQLException
	{
		synchronized (this.activeDatabaseSet)
		{
			if (this.activeDatabaseSet.size() == 0)
			{
				throw new SQLException("No active databases in cluster");
			}
			
			return new ArrayList(this.activeDatabaseSet);
		}
	}
	
	public void removeDatabase(Database database)
	{
		synchronized (this.activeDatabaseSet)
		{
			this.activeDatabaseSet.remove(database);
		}
	}
}
