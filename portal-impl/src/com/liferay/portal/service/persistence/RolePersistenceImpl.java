/**
 * Copyright (c) 2000-2007 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.service.persistence;

import com.liferay.portal.NoSuchRoleException;
import com.liferay.portal.SystemException;
import com.liferay.portal.kernel.dao.DynamicQuery;
import com.liferay.portal.kernel.dao.DynamicQueryInitializer;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringMaker;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.impl.RoleImpl;
import com.liferay.portal.service.persistence.BasePersistence;
import com.liferay.portal.spring.hibernate.HibernateUtil;

import com.liferay.util.dao.hibernate.QueryPos;
import com.liferay.util.dao.hibernate.QueryUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.springframework.dao.DataAccessException;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.Iterator;
import java.util.List;

/**
 * <a href="RolePersistenceImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class RolePersistenceImpl extends BasePersistence
	implements RolePersistence {
	public Role create(long roleId) {
		Role role = new RoleImpl();
		role.setNew(true);
		role.setPrimaryKey(roleId);

		return role;
	}

	public Role remove(long roleId) throws NoSuchRoleException, SystemException {
		Session session = null;

		try {
			session = openSession();

			Role role = (Role)session.get(RoleImpl.class, new Long(roleId));

			if (role == null) {
				if (_log.isWarnEnabled()) {
					_log.warn("No Role exists with the primary key " + roleId);
				}

				throw new NoSuchRoleException(
					"No Role exists with the primary key " + roleId);
			}

			return remove(role);
		}
		catch (NoSuchRoleException nsee) {
			throw nsee;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public Role remove(Role role) throws SystemException {
		Session session = null;

		try {
			session = openSession();
			session.delete(role);
			session.flush();
			clearGroups.clear(role.getPrimaryKey());
			clearPermissions.clear(role.getPrimaryKey());
			clearUsers.clear(role.getPrimaryKey());

			return role;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public com.liferay.portal.model.Role update(
		com.liferay.portal.model.Role role) throws SystemException {
		return update(role, false);
	}

	public com.liferay.portal.model.Role update(
		com.liferay.portal.model.Role role, boolean saveOrUpdate)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			if (saveOrUpdate) {
				session.saveOrUpdate(role);
			}
			else {
				if (role.isNew()) {
					session.save(role);
				}
			}

			session.flush();
			role.setNew(false);

			return role;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public Role findByPrimaryKey(long roleId)
		throws NoSuchRoleException, SystemException {
		Role role = fetchByPrimaryKey(roleId);

		if (role == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("No Role exists with the primary key " + roleId);
			}

			throw new NoSuchRoleException(
				"No Role exists with the primary key " + roleId);
		}

		return role;
	}

	public Role fetchByPrimaryKey(long roleId) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			return (Role)session.get(RoleImpl.class, new Long(roleId));
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List findByCompanyId(long companyId) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("FROM com.liferay.portal.model.Role WHERE ");
			query.append("companyId = ?");
			query.append(" ");
			query.append("ORDER BY ");
			query.append("name ASC");

			Query q = session.createQuery(query.toString());
			int queryPos = 0;
			q.setLong(queryPos++, companyId);

			return q.list();
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List findByCompanyId(long companyId, int begin, int end)
		throws SystemException {
		return findByCompanyId(companyId, begin, end, null);
	}

	public List findByCompanyId(long companyId, int begin, int end,
		OrderByComparator obc) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("FROM com.liferay.portal.model.Role WHERE ");
			query.append("companyId = ?");
			query.append(" ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("name ASC");
			}

			Query q = session.createQuery(query.toString());
			int queryPos = 0;
			q.setLong(queryPos++, companyId);

			return QueryUtil.list(q, getDialect(), begin, end);
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public Role findByCompanyId_First(long companyId, OrderByComparator obc)
		throws NoSuchRoleException, SystemException {
		List list = findByCompanyId(companyId, 0, 1, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();
			msg.append("No Role exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("companyId=");
			msg.append(companyId);
			msg.append(StringPool.CLOSE_CURLY_BRACE);
			throw new NoSuchRoleException(msg.toString());
		}
		else {
			return (Role)list.get(0);
		}
	}

	public Role findByCompanyId_Last(long companyId, OrderByComparator obc)
		throws NoSuchRoleException, SystemException {
		int count = countByCompanyId(companyId);
		List list = findByCompanyId(companyId, count - 1, count, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();
			msg.append("No Role exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("companyId=");
			msg.append(companyId);
			msg.append(StringPool.CLOSE_CURLY_BRACE);
			throw new NoSuchRoleException(msg.toString());
		}
		else {
			return (Role)list.get(0);
		}
	}

	public Role[] findByCompanyId_PrevAndNext(long roleId, long companyId,
		OrderByComparator obc) throws NoSuchRoleException, SystemException {
		Role role = findByPrimaryKey(roleId);
		int count = countByCompanyId(companyId);
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("FROM com.liferay.portal.model.Role WHERE ");
			query.append("companyId = ?");
			query.append(" ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("name ASC");
			}

			Query q = session.createQuery(query.toString());
			int queryPos = 0;
			q.setLong(queryPos++, companyId);

			Object[] objArray = QueryUtil.getPrevAndNext(q, count, obc, role);
			Role[] array = new RoleImpl[3];
			array[0] = (Role)objArray[0];
			array[1] = (Role)objArray[1];
			array[2] = (Role)objArray[2];

			return array;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public Role findByC_N(long companyId, String name)
		throws NoSuchRoleException, SystemException {
		Role role = fetchByC_N(companyId, name);

		if (role == null) {
			StringMaker msg = new StringMaker();
			msg.append("No Role exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("companyId=");
			msg.append(companyId);
			msg.append(", ");
			msg.append("name=");
			msg.append(name);
			msg.append(StringPool.CLOSE_CURLY_BRACE);

			if (_log.isWarnEnabled()) {
				_log.warn(msg.toString());
			}

			throw new NoSuchRoleException(msg.toString());
		}

		return role;
	}

	public Role fetchByC_N(long companyId, String name)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("FROM com.liferay.portal.model.Role WHERE ");
			query.append("companyId = ?");
			query.append(" AND ");

			if (name == null) {
				query.append("name IS NULL");
			}
			else {
				query.append("name = ?");
			}

			query.append(" ");
			query.append("ORDER BY ");
			query.append("name ASC");

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, companyId);

			if (name != null) {
				q.setString(queryPos++, name);
			}

			List list = q.list();

			if (list.size() == 0) {
				return null;
			}

			Role role = (Role)list.get(0);

			return role;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public Role findByC_C_C(long companyId, long classNameId, long classPK)
		throws NoSuchRoleException, SystemException {
		Role role = fetchByC_C_C(companyId, classNameId, classPK);

		if (role == null) {
			StringMaker msg = new StringMaker();
			msg.append("No Role exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("companyId=");
			msg.append(companyId);
			msg.append(", ");
			msg.append("classNameId=");
			msg.append(classNameId);
			msg.append(", ");
			msg.append("classPK=");
			msg.append(classPK);
			msg.append(StringPool.CLOSE_CURLY_BRACE);

			if (_log.isWarnEnabled()) {
				_log.warn(msg.toString());
			}

			throw new NoSuchRoleException(msg.toString());
		}

		return role;
	}

	public Role fetchByC_C_C(long companyId, long classNameId, long classPK)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("FROM com.liferay.portal.model.Role WHERE ");
			query.append("companyId = ?");
			query.append(" AND ");
			query.append("classNameId = ?");
			query.append(" AND ");
			query.append("classPK = ?");
			query.append(" ");
			query.append("ORDER BY ");
			query.append("name ASC");

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, companyId);
			q.setLong(queryPos++, classNameId);
			q.setLong(queryPos++, classPK);

			List list = q.list();

			if (list.size() == 0) {
				return null;
			}

			Role role = (Role)list.get(0);

			return role;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List findWithDynamicQuery(DynamicQueryInitializer queryInitializer)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			DynamicQuery query = queryInitializer.initialize(session);

			return query.list();
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List findWithDynamicQuery(DynamicQueryInitializer queryInitializer,
		int begin, int end) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			DynamicQuery query = queryInitializer.initialize(session);
			query.setLimit(begin, end);

			return query.list();
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List findAll() throws SystemException {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	public List findAll(int begin, int end) throws SystemException {
		return findAll(begin, end, null);
	}

	public List findAll(int begin, int end, OrderByComparator obc)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("FROM com.liferay.portal.model.Role ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("name ASC");
			}

			Query q = session.createQuery(query.toString());

			return QueryUtil.list(q, getDialect(), begin, end);
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public void removeByCompanyId(long companyId) throws SystemException {
		Iterator itr = findByCompanyId(companyId).iterator();

		while (itr.hasNext()) {
			Role role = (Role)itr.next();
			remove(role);
		}
	}

	public void removeByC_N(long companyId, String name)
		throws NoSuchRoleException, SystemException {
		Role role = findByC_N(companyId, name);
		remove(role);
	}

	public void removeByC_C_C(long companyId, long classNameId, long classPK)
		throws NoSuchRoleException, SystemException {
		Role role = findByC_C_C(companyId, classNameId, classPK);
		remove(role);
	}

	public void removeAll() throws SystemException {
		Iterator itr = findAll().iterator();

		while (itr.hasNext()) {
			remove((Role)itr.next());
		}
	}

	public int countByCompanyId(long companyId) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("SELECT COUNT(*) ");
			query.append("FROM com.liferay.portal.model.Role WHERE ");
			query.append("companyId = ?");
			query.append(" ");

			Query q = session.createQuery(query.toString());
			int queryPos = 0;
			q.setLong(queryPos++, companyId);

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public int countByC_N(long companyId, String name)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("SELECT COUNT(*) ");
			query.append("FROM com.liferay.portal.model.Role WHERE ");
			query.append("companyId = ?");
			query.append(" AND ");

			if (name == null) {
				query.append("name IS NULL");
			}
			else {
				query.append("name = ?");
			}

			query.append(" ");

			Query q = session.createQuery(query.toString());
			int queryPos = 0;
			q.setLong(queryPos++, companyId);

			if (name != null) {
				q.setString(queryPos++, name);
			}

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public int countByC_C_C(long companyId, long classNameId, long classPK)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("SELECT COUNT(*) ");
			query.append("FROM com.liferay.portal.model.Role WHERE ");
			query.append("companyId = ?");
			query.append(" AND ");
			query.append("classNameId = ?");
			query.append(" AND ");
			query.append("classPK = ?");
			query.append(" ");

			Query q = session.createQuery(query.toString());
			int queryPos = 0;
			q.setLong(queryPos++, companyId);
			q.setLong(queryPos++, classNameId);
			q.setLong(queryPos++, classPK);

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public int countAll() throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("SELECT COUNT(*) ");
			query.append("FROM com.liferay.portal.model.Role");

			Query q = session.createQuery(query.toString());
			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public List getGroups(long pk) throws NoSuchRoleException, SystemException {
		return getGroups(pk, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	}

	public List getGroups(long pk, int begin, int end)
		throws NoSuchRoleException, SystemException {
		return getGroups(pk, begin, end, null);
	}

	public List getGroups(long pk, int begin, int end, OrderByComparator obc)
		throws NoSuchRoleException, SystemException {
		Session session = null;

		try {
			session = HibernateUtil.openSession();

			StringMaker sm = new StringMaker();
			sm.append(_SQL_GETGROUPS);

			if (obc != null) {
				sm.append("ORDER BY ");
				sm.append(obc.getOrderBy());
			}
			else {
				sm.append("ORDER BY ");
				sm.append("Group_.name ASC");
			}

			String sql = sm.toString();
			SQLQuery q = session.createSQLQuery(sql);
			q.addEntity("Group_", com.liferay.portal.model.impl.GroupImpl.class);

			QueryPos qPos = QueryPos.getInstance(q);
			qPos.add(pk);

			return QueryUtil.list(q, HibernateUtil.getDialect(), begin, end);
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
		finally {
			HibernateUtil.closeSession(session);
		}
	}

	public int getGroupsSize(long pk) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			SQLQuery q = session.createSQLQuery(_SQL_GETGROUPSSIZE);
			q.addScalar(HibernateUtil.getCountColumnName(), Hibernate.LONG);

			QueryPos qPos = QueryPos.getInstance(q);
			qPos.add(pk);

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public boolean containsGroup(long pk, long groupPK)
		throws SystemException {
		try {
			return containsGroup.contains(pk, groupPK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public boolean containsGroups(long pk) throws SystemException {
		if (getGroupsSize(pk) > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public void addGroup(long pk, long groupPK)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			addGroup.add(pk, groupPK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addGroup(long pk, com.liferay.portal.model.Group group)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			addGroup.add(pk, group.getPrimaryKey());
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addGroups(long pk, long[] groupPKs)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			for (int i = 0; i < groupPKs.length; i++) {
				addGroup.add(pk, groupPKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addGroups(long pk, List groups)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			for (int i = 0; i < groups.size(); i++) {
				com.liferay.portal.model.Group group = (com.liferay.portal.model.Group)groups.get(i);
				addGroup.add(pk, group.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void clearGroups(long pk)
		throws NoSuchRoleException, SystemException {
		try {
			clearGroups.clear(pk);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeGroup(long pk, long groupPK)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			removeGroup.remove(pk, groupPK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeGroup(long pk, com.liferay.portal.model.Group group)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			removeGroup.remove(pk, group.getPrimaryKey());
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeGroups(long pk, long[] groupPKs)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			for (int i = 0; i < groupPKs.length; i++) {
				removeGroup.remove(pk, groupPKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeGroups(long pk, List groups)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			for (int i = 0; i < groups.size(); i++) {
				com.liferay.portal.model.Group group = (com.liferay.portal.model.Group)groups.get(i);
				removeGroup.remove(pk, group.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void setGroups(long pk, long[] groupPKs)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			clearGroups.clear(pk);

			for (int i = 0; i < groupPKs.length; i++) {
				addGroup.add(pk, groupPKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void setGroups(long pk, List groups)
		throws NoSuchRoleException, com.liferay.portal.NoSuchGroupException, 
			SystemException {
		try {
			clearGroups.clear(pk);

			for (int i = 0; i < groups.size(); i++) {
				com.liferay.portal.model.Group group = (com.liferay.portal.model.Group)groups.get(i);
				addGroup.add(pk, group.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public List getPermissions(long pk)
		throws NoSuchRoleException, SystemException {
		return getPermissions(pk, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	}

	public List getPermissions(long pk, int begin, int end)
		throws NoSuchRoleException, SystemException {
		return getPermissions(pk, begin, end, null);
	}

	public List getPermissions(long pk, int begin, int end,
		OrderByComparator obc) throws NoSuchRoleException, SystemException {
		Session session = null;

		try {
			session = HibernateUtil.openSession();

			StringMaker sm = new StringMaker();
			sm.append(_SQL_GETPERMISSIONS);

			if (obc != null) {
				sm.append("ORDER BY ");
				sm.append(obc.getOrderBy());
			}

			String sql = sm.toString();
			SQLQuery q = session.createSQLQuery(sql);
			q.addEntity("Permission_",
				com.liferay.portal.model.impl.PermissionImpl.class);

			QueryPos qPos = QueryPos.getInstance(q);
			qPos.add(pk);

			return QueryUtil.list(q, HibernateUtil.getDialect(), begin, end);
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
		finally {
			HibernateUtil.closeSession(session);
		}
	}

	public int getPermissionsSize(long pk) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			SQLQuery q = session.createSQLQuery(_SQL_GETPERMISSIONSSIZE);
			q.addScalar(HibernateUtil.getCountColumnName(), Hibernate.LONG);

			QueryPos qPos = QueryPos.getInstance(q);
			qPos.add(pk);

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public boolean containsPermission(long pk, long permissionPK)
		throws SystemException {
		try {
			return containsPermission.contains(pk, permissionPK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public boolean containsPermissions(long pk) throws SystemException {
		if (getPermissionsSize(pk) > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public void addPermission(long pk, long permissionPK)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			addPermission.add(pk, permissionPK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addPermission(long pk,
		com.liferay.portal.model.Permission permission)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			addPermission.add(pk, permission.getPrimaryKey());
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addPermissions(long pk, long[] permissionPKs)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			for (int i = 0; i < permissionPKs.length; i++) {
				addPermission.add(pk, permissionPKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addPermissions(long pk, List permissions)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			for (int i = 0; i < permissions.size(); i++) {
				com.liferay.portal.model.Permission permission = (com.liferay.portal.model.Permission)permissions.get(i);
				addPermission.add(pk, permission.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void clearPermissions(long pk)
		throws NoSuchRoleException, SystemException {
		try {
			clearPermissions.clear(pk);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removePermission(long pk, long permissionPK)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			removePermission.remove(pk, permissionPK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removePermission(long pk,
		com.liferay.portal.model.Permission permission)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			removePermission.remove(pk, permission.getPrimaryKey());
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removePermissions(long pk, long[] permissionPKs)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			for (int i = 0; i < permissionPKs.length; i++) {
				removePermission.remove(pk, permissionPKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removePermissions(long pk, List permissions)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			for (int i = 0; i < permissions.size(); i++) {
				com.liferay.portal.model.Permission permission = (com.liferay.portal.model.Permission)permissions.get(i);
				removePermission.remove(pk, permission.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void setPermissions(long pk, long[] permissionPKs)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			clearPermissions.clear(pk);

			for (int i = 0; i < permissionPKs.length; i++) {
				addPermission.add(pk, permissionPKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void setPermissions(long pk, List permissions)
		throws NoSuchRoleException, 
			com.liferay.portal.NoSuchPermissionException, SystemException {
		try {
			clearPermissions.clear(pk);

			for (int i = 0; i < permissions.size(); i++) {
				com.liferay.portal.model.Permission permission = (com.liferay.portal.model.Permission)permissions.get(i);
				addPermission.add(pk, permission.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public List getUsers(long pk) throws NoSuchRoleException, SystemException {
		return getUsers(pk, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	}

	public List getUsers(long pk, int begin, int end)
		throws NoSuchRoleException, SystemException {
		return getUsers(pk, begin, end, null);
	}

	public List getUsers(long pk, int begin, int end, OrderByComparator obc)
		throws NoSuchRoleException, SystemException {
		Session session = null;

		try {
			session = HibernateUtil.openSession();

			StringMaker sm = new StringMaker();
			sm.append(_SQL_GETUSERS);

			if (obc != null) {
				sm.append("ORDER BY ");
				sm.append(obc.getOrderBy());
			}

			String sql = sm.toString();
			SQLQuery q = session.createSQLQuery(sql);
			q.addEntity("User_", com.liferay.portal.model.impl.UserImpl.class);

			QueryPos qPos = QueryPos.getInstance(q);
			qPos.add(pk);

			return QueryUtil.list(q, HibernateUtil.getDialect(), begin, end);
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
		finally {
			HibernateUtil.closeSession(session);
		}
	}

	public int getUsersSize(long pk) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			SQLQuery q = session.createSQLQuery(_SQL_GETUSERSSIZE);
			q.addScalar(HibernateUtil.getCountColumnName(), Hibernate.LONG);

			QueryPos qPos = QueryPos.getInstance(q);
			qPos.add(pk);

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception e) {
			throw HibernateUtil.processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	public boolean containsUser(long pk, long userPK) throws SystemException {
		try {
			return containsUser.contains(pk, userPK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public boolean containsUsers(long pk) throws SystemException {
		if (getUsersSize(pk) > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public void addUser(long pk, long userPK)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			addUser.add(pk, userPK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addUser(long pk, com.liferay.portal.model.User user)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			addUser.add(pk, user.getPrimaryKey());
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addUsers(long pk, long[] userPKs)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			for (int i = 0; i < userPKs.length; i++) {
				addUser.add(pk, userPKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addUsers(long pk, List users)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			for (int i = 0; i < users.size(); i++) {
				com.liferay.portal.model.User user = (com.liferay.portal.model.User)users.get(i);
				addUser.add(pk, user.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void clearUsers(long pk) throws NoSuchRoleException, SystemException {
		try {
			clearUsers.clear(pk);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeUser(long pk, long userPK)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			removeUser.remove(pk, userPK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeUser(long pk, com.liferay.portal.model.User user)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			removeUser.remove(pk, user.getPrimaryKey());
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeUsers(long pk, long[] userPKs)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			for (int i = 0; i < userPKs.length; i++) {
				removeUser.remove(pk, userPKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeUsers(long pk, List users)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			for (int i = 0; i < users.size(); i++) {
				com.liferay.portal.model.User user = (com.liferay.portal.model.User)users.get(i);
				removeUser.remove(pk, user.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void setUsers(long pk, long[] userPKs)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			clearUsers.clear(pk);

			for (int i = 0; i < userPKs.length; i++) {
				addUser.add(pk, userPKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void setUsers(long pk, List users)
		throws NoSuchRoleException, com.liferay.portal.NoSuchUserException, 
			SystemException {
		try {
			clearUsers.clear(pk);

			for (int i = 0; i < users.size(); i++) {
				com.liferay.portal.model.User user = (com.liferay.portal.model.User)users.get(i);
				addUser.add(pk, user.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	protected void initDao() {
		containsGroup = new ContainsGroup(this);
		addGroup = new AddGroup(this);
		clearGroups = new ClearGroups(this);
		removeGroup = new RemoveGroup(this);
		containsPermission = new ContainsPermission(this);
		addPermission = new AddPermission(this);
		clearPermissions = new ClearPermissions(this);
		removePermission = new RemovePermission(this);
		containsUser = new ContainsUser(this);
		addUser = new AddUser(this);
		clearUsers = new ClearUsers(this);
		removeUser = new RemoveUser(this);
	}

	protected ContainsGroup containsGroup;
	protected AddGroup addGroup;
	protected ClearGroups clearGroups;
	protected RemoveGroup removeGroup;
	protected ContainsPermission containsPermission;
	protected AddPermission addPermission;
	protected ClearPermissions clearPermissions;
	protected RemovePermission removePermission;
	protected ContainsUser containsUser;
	protected AddUser addUser;
	protected ClearUsers clearUsers;
	protected RemoveUser removeUser;

	protected class ContainsGroup extends MappingSqlQuery {
		protected ContainsGroup(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(), _SQL_CONTAINSGROUP);
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rowNumber)
			throws SQLException {
			return new Integer(rs.getInt("COUNT_VALUE"));
		}

		protected boolean contains(long roleId, long groupId) {
			List results = execute(new Object[] {
						new Long(roleId), new Long(groupId)
					});

			if (results.size() > 0) {
				Integer count = (Integer)results.get(0);

				if (count.intValue() > 0) {
					return true;
				}
			}

			return false;
		}
	}

	protected class AddGroup extends SqlUpdate {
		protected AddGroup(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(),
				"INSERT INTO Groups_Roles (roleId, groupId) VALUES (?, ?)");
			_persistenceImpl = persistenceImpl;
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void add(long roleId, long groupId) {
			if (!_persistenceImpl.containsGroup.contains(roleId, groupId)) {
				update(new Object[] { new Long(roleId), new Long(groupId) });
			}
		}

		private RolePersistenceImpl _persistenceImpl;
	}

	protected class ClearGroups extends SqlUpdate {
		protected ClearGroups(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(),
				"DELETE FROM Groups_Roles WHERE roleId = ?");
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void clear(long roleId) {
			update(new Object[] { new Long(roleId) });
		}
	}

	protected class RemoveGroup extends SqlUpdate {
		protected RemoveGroup(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(),
				"DELETE FROM Groups_Roles WHERE roleId = ? AND groupId = ?");
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void remove(long roleId, long groupId) {
			update(new Object[] { new Long(roleId), new Long(groupId) });
		}
	}

	protected class ContainsPermission extends MappingSqlQuery {
		protected ContainsPermission(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(), _SQL_CONTAINSPERMISSION);
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rowNumber)
			throws SQLException {
			return new Integer(rs.getInt("COUNT_VALUE"));
		}

		protected boolean contains(long roleId, long permissionId) {
			List results = execute(new Object[] {
						new Long(roleId), new Long(permissionId)
					});

			if (results.size() > 0) {
				Integer count = (Integer)results.get(0);

				if (count.intValue() > 0) {
					return true;
				}
			}

			return false;
		}
	}

	protected class AddPermission extends SqlUpdate {
		protected AddPermission(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(),
				"INSERT INTO Roles_Permissions (roleId, permissionId) VALUES (?, ?)");
			_persistenceImpl = persistenceImpl;
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void add(long roleId, long permissionId) {
			if (!_persistenceImpl.containsPermission.contains(roleId,
						permissionId)) {
				update(new Object[] { new Long(roleId), new Long(permissionId) });
			}
		}

		private RolePersistenceImpl _persistenceImpl;
	}

	protected class ClearPermissions extends SqlUpdate {
		protected ClearPermissions(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(),
				"DELETE FROM Roles_Permissions WHERE roleId = ?");
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void clear(long roleId) {
			update(new Object[] { new Long(roleId) });
		}
	}

	protected class RemovePermission extends SqlUpdate {
		protected RemovePermission(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(),
				"DELETE FROM Roles_Permissions WHERE roleId = ? AND permissionId = ?");
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void remove(long roleId, long permissionId) {
			update(new Object[] { new Long(roleId), new Long(permissionId) });
		}
	}

	protected class ContainsUser extends MappingSqlQuery {
		protected ContainsUser(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(), _SQL_CONTAINSUSER);
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rowNumber)
			throws SQLException {
			return new Integer(rs.getInt("COUNT_VALUE"));
		}

		protected boolean contains(long roleId, long userId) {
			List results = execute(new Object[] {
						new Long(roleId), new Long(userId)
					});

			if (results.size() > 0) {
				Integer count = (Integer)results.get(0);

				if (count.intValue() > 0) {
					return true;
				}
			}

			return false;
		}
	}

	protected class AddUser extends SqlUpdate {
		protected AddUser(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(),
				"INSERT INTO Users_Roles (roleId, userId) VALUES (?, ?)");
			_persistenceImpl = persistenceImpl;
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void add(long roleId, long userId) {
			if (!_persistenceImpl.containsUser.contains(roleId, userId)) {
				update(new Object[] { new Long(roleId), new Long(userId) });
			}
		}

		private RolePersistenceImpl _persistenceImpl;
	}

	protected class ClearUsers extends SqlUpdate {
		protected ClearUsers(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(),
				"DELETE FROM Users_Roles WHERE roleId = ?");
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void clear(long roleId) {
			update(new Object[] { new Long(roleId) });
		}
	}

	protected class RemoveUser extends SqlUpdate {
		protected RemoveUser(RolePersistenceImpl persistenceImpl) {
			super(persistenceImpl.getDataSource(),
				"DELETE FROM Users_Roles WHERE roleId = ? AND userId = ?");
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void remove(long roleId, long userId) {
			update(new Object[] { new Long(roleId), new Long(userId) });
		}
	}

	private static final String _SQL_GETGROUPS = "SELECT {Group_.*} FROM Group_ INNER JOIN Groups_Roles ON (Groups_Roles.groupId = Group_.groupId) WHERE (Groups_Roles.roleId = ?)";
	private static final String _SQL_GETGROUPSSIZE = "SELECT COUNT(*) AS COUNT_VALUE FROM Groups_Roles WHERE roleId = ?";
	private static final String _SQL_CONTAINSGROUP = "SELECT COUNT(*) AS COUNT_VALUE FROM Groups_Roles WHERE roleId = ? AND groupId = ?";
	private static final String _SQL_GETPERMISSIONS = "SELECT {Permission_.*} FROM Permission_ INNER JOIN Roles_Permissions ON (Roles_Permissions.permissionId = Permission_.permissionId) WHERE (Roles_Permissions.roleId = ?)";
	private static final String _SQL_GETPERMISSIONSSIZE = "SELECT COUNT(*) AS COUNT_VALUE FROM Roles_Permissions WHERE roleId = ?";
	private static final String _SQL_CONTAINSPERMISSION = "SELECT COUNT(*) AS COUNT_VALUE FROM Roles_Permissions WHERE roleId = ? AND permissionId = ?";
	private static final String _SQL_GETUSERS = "SELECT {User_.*} FROM User_ INNER JOIN Users_Roles ON (Users_Roles.userId = User_.userId) WHERE (Users_Roles.roleId = ?)";
	private static final String _SQL_GETUSERSSIZE = "SELECT COUNT(*) AS COUNT_VALUE FROM Users_Roles WHERE roleId = ?";
	private static final String _SQL_CONTAINSUSER = "SELECT COUNT(*) AS COUNT_VALUE FROM Users_Roles WHERE roleId = ? AND userId = ?";
	private static Log _log = LogFactory.getLog(RolePersistenceImpl.class);
}