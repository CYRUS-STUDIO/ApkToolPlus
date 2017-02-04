package org.apache.bcel.verifier.statics;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.exc.*;

/**
 * A utility class holding the information about
 * the names and the types of the local variables in
 * a given method.
 *
 * @version $Id: LocalVariablesInfo.java,v 1.1 2005/12/16 14:11:30 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public class LocalVariablesInfo{
	
	/** The information about the local variables is stored here. */
	private LocalVariableInfo[] localVariableInfos;

	/** The constructor. */
	LocalVariablesInfo(int max_locals){
		localVariableInfos = new LocalVariableInfo[max_locals];
		for (int i=0; i<max_locals; i++){
			localVariableInfos[i] = new LocalVariableInfo();
		}
	}

	/** Returns the LocalVariableInfo for the given slot. */
	public LocalVariableInfo getLocalVariableInfo(int slot){
		if (slot < 0 || slot >= localVariableInfos.length){
			throw new AssertionViolatedException("Slot number for local variable information out of range.");
		}
		return localVariableInfos[slot];
	}

	/**
	 * Adds information about the local variable in slot 'slot'. Automatically 
	 * adds information for slot+1 if 't' is Type.LONG or Type.DOUBLE.
	 * @throws LocalVariableInfoInconsistentException if the new information conflicts
	 *         with already gathered information.
	 */
	public void add(int slot, String name, int startpc, int length, Type t) throws LocalVariableInfoInconsistentException{
		// The add operation on LocalVariableInfo may throw the '...Inconsistent...' exception, we don't throw it explicitely here.
		
		if (slot < 0 || slot >= localVariableInfos.length){
			throw new AssertionViolatedException("Slot number for local variable information out of range.");
		}

		localVariableInfos[slot].add(name, startpc, length, t);
		if (t == Type.LONG) localVariableInfos[slot+1].add(name, startpc, length, LONG_Upper.theInstance());
		if (t == Type.DOUBLE) localVariableInfos[slot+1].add(name, startpc, length, DOUBLE_Upper.theInstance());
	}
}
