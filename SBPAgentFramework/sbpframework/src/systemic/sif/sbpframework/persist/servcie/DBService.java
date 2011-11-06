/*
 * DBService.java
 * Created: 08/10/2011
 *
 * Copyright 2011 Systemic Pty Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package systemic.sif.sbpframework.persist.servcie;

import org.hibernate.Transaction;

import systemic.sif.sbpframework.persist.dao.BaseDAO;

/**
 * @author User
 *
 */
public abstract class DBService
{
	private Transaction tx = null;
//	private Session session = null;
	
	public abstract BaseDAO getDAO();
	
	public DBService()
	{
	}
	
	public void startTransaction()
	{
		tx = getDAO().getCurrentSession().beginTransaction();
	}
	
//	public void startNewTransactionScope()
//	{
//		session = HibernateUtil.getSessionFactory().openSession();
//		tx = session.beginTransaction();
//	}
	
	/* commits changes and finalises transaction */
	public void commit()
	{
		tx.commit();
//		if (session != null)
//		{
//			session.close();
//			session = null;
//		}
	}
	
	/* commits changes and finalises transaction */
	public void rollback()
	{
		tx.rollback();
//		if (session != null)
//		{
//			session.close();
//			session = null;
//		}
	}
	
	public void loadSubObject(Object proxy)
	{
		getDAO().loadSubObject(proxy);
	}

}
