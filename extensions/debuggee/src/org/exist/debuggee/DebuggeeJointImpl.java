/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2009 The eXist Project
 *  http://exist-db.org
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id:$
 */
package org.exist.debuggee;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.exist.xquery.Expression;
import org.exist.xquery.XQueryContext;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class DebuggeeJointImpl implements DebuggeeJoint, Commands, Status {
	
	private int status = FIRST_RUN;
	
	private Expression currentExpr = null;
	private List<Expression> stack = new ArrayList<Expression>();
	
	private int command = STOP_ON_FIRST_LINE;
	
	public DebuggeeJointImpl() {
	}

	/* (non-Javadoc)
	 * @see org.exist.debuggee.DebuggeeJoint#expressionEnd(org.exist.xquery.Expression)
	 */
	public void expressionEnd(Expression expr) {
		System.out.println("expressionEnd expr = "+expr.toString());
	}

	/* (non-Javadoc)
	 * @see org.exist.debuggee.DebuggeeJoint#expressionStart(org.exist.xquery.Expression)
	 */
	public void expressionStart(Expression expr) {
		System.out.println("expressionStart expr = "+expr.toString());
		
		if (status == FIRST_RUN)
			stack.add(expr);
		
		currentExpr = expr;
		
		while (true) {
			if (command == STOP_ON_FIRST_LINE && status == FIRST_RUN)
				waitCommand();
			
			if (command == STEP_INTO) {
				status = DEBUGGING;
				command = WAIT;
				break;
			}
		}
	}
	
	private void waitCommand() {
		try {
			synchronized (this) {
				this.notifyAll();
			}
			synchronized (currentExpr) {
				currentExpr.wait();
			}
		} catch (InterruptedException e) {
			//UNDERSTAND: what to do?
		}
	}

	/* (non-Javadoc)
	 * @see org.exist.debuggee.DebuggeeJoint#getContext()
	 */
	public XQueryContext getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	public String stepInto() {
		command = STEP_INTO;

		synchronized (currentExpr) {
			if (currentExpr != null)
				currentExpr.notifyAll();
		}
		
		try {
			synchronized (this) {
				this.wait();
			}
		} catch (InterruptedException e) {
			return "error";
		}
		
		return "starting";
	}

	public String stepOut() {
		// TODO Auto-generated method stub
		return null;
	}

	public String stepOver() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean featureSet(String name, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	public List stackGet() {
		// TODO Auto-generated method stub
		return null;
	}
}
