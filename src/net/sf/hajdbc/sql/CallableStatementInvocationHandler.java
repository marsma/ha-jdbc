/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (c) 2004-2007 Paul Ferraro
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
package net.sf.hajdbc.sql;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import net.sf.hajdbc.Database;

/**
 * @author Paul Ferraro
 *
 */
@SuppressWarnings("nls")
public class CallableStatementInvocationHandler<D> extends AbstractPreparedStatementInvocationHandler<D, CallableStatement>
{
	/**
	 * @param connection
	 * @param proxy
	 * @param invoker
	 * @param statementMap
	 * @throws Exception
	 */
	public CallableStatementInvocationHandler(Connection connection, SQLProxy<D, Connection> proxy, Invoker<D, Connection, CallableStatement> invoker, Map<Database<D>, CallableStatement> statementMap, FileSupport fileSupport) throws Exception
	{
		super(connection, proxy, invoker, CallableStatement.class, statementMap, fileSupport);
	}

	/**
	 * @see net.sf.hajdbc.sql.AbstractStatementInvocationHandler#getInvocationStrategy(java.sql.Statement, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	protected InvocationStrategy<D, CallableStatement, ?> getInvocationStrategy(CallableStatement statement, Method method, Object[] parameters) throws Exception
	{
		String methodName = method.getName();
		
		if (methodName.equals("registerOutParameter"))
		{
			return new DriverWriteInvocationStrategy<D, CallableStatement, Object>();
		}
		
		Class<?>[] types = method.getParameterTypes();
		
		if (methodName.startsWith("get") && (types != null) && (types.length > 0) && ((types[0].equals(Integer.TYPE) || types[0].equals(String.class))))
		{
			if (!method.equals(PreparedStatement.class.getMethod("getMoreResults", Integer.TYPE)))
			{
				return new DriverReadInvocationStrategy<D, CallableStatement, Object>();
			}
		}
		
		if (method.equals(CallableStatement.class.getMethod("wasNull")))
		{
			return new DriverReadInvocationStrategy<D, CallableStatement, Object>();
		}
		
		if (method.equals(CallableStatement.class.getMethod("execute")) || method.equals(CallableStatement.class.getMethod("executeUpdate")))
		{
			return new TransactionalDatabaseWriteInvocationStrategy<D, CallableStatement, Object>();
		}
		
		if (method.equals(CallableStatement.class.getMethod("executeQuery")))
		{
			return (statement.getResultSetConcurrency() == java.sql.ResultSet.CONCUR_READ_ONLY) ? new DatabaseReadInvocationStrategy<D, CallableStatement, Object>() : new EagerResultSetInvocationStrategy<D, CallableStatement>(statement, this.fileSupport);
		}
		
		return super.getInvocationStrategy(statement, method, parameters);
	}

	/**
	 * @see net.sf.hajdbc.sql.AbstractPreparedStatementInvocationHandler#isParameterizedType(java.lang.Class)
	 */
	@Override
	protected boolean isIndexType(Class<?> type)
	{
		return super.isIndexType(type) || type.equals(String.class);
	}
}
