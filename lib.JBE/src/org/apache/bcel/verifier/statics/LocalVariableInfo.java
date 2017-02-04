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
import java.util.Hashtable;

/**
 * A utility class holding the information about
 * the name and the type of a local variable in
 * a given slot (== index). This information
 * often changes in course of byte code offsets.
 *
 * @version $Id: LocalVariableInfo.java,v 1.3 2006/09/18 14:51:46 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public class LocalVariableInfo{

	/** The types database. KEY: String representing the offset integer. */
	private Hashtable<String, Type> types = new Hashtable<String, Type>();
	/** The names database. KEY: String representing the offset integer. */
	private Hashtable<String, String> names = new Hashtable<String, String>();

	/**
	 * Adds a name of a local variable and a certain slot to our 'names'
	 * (Hashtable) database.
	 */
	private void setName(int offset, String name){
		names.put( ((Integer.toString(offset))), name);
	}
	/**
	 * Adds a type of a local variable and a certain slot to our 'types'
	 * (Hashtable) database.
	 */
	private void setType(int offset, Type t){
		types.put( ((Integer.toString(offset))), t);
	}

	/**
	 * Returns the type of the local variable that uses this local
	 * variable slot at the given bytecode offset.
	 * Care for legal bytecode offsets yourself, otherwise the return value
	 * might be wrong.
	 * May return 'null' if nothing is known about the type of this local
	 * variable slot at the given bytecode offset.
	 */
	public Type getType(int offset){
		return (Type) types.get(Integer.toString(offset));
	}
	/**
	 * Returns the name of the local variable that uses this local
	 * variable slot at the given bytecode offset.
	 * Care for legal bytecode offsets yourself, otherwise the return value
	 * might be wrong.
	 * May return 'null' if nothing is known about the type of this local
	 * variable slot at the given bytecode offset.
	 */
	public String getName(int offset){
		return (String) (names.get(Integer.toString(offset)));
	}
	/**
	 * Adds some information about this local variable (slot).
	 * @throws LocalVariableInfoInconsistentException if the new information conflicts
	 *         with already gathered information.
	 */
	public void add(String name, int startpc, int length, Type t) throws LocalVariableInfoInconsistentException{
		for (int i=startpc; i<=startpc+length; i++){ // incl/incl-notation!
			add(i,name,t);
		}
	}

	/**
	 * Adds information about name and type for a given offset.
	 * @throws LocalVariableInfoInconsistentException if the new information conflicts
	 *         with already gathered information.
	 */
	private void add(int offset, String name, Type t) throws LocalVariableInfoInconsistentException{
		if (getName(offset) != null){
			if (! getName(offset).equals(name)){
				throw new LocalVariableInfoInconsistentException("At bytecode offset '"+offset+"' a local variable has two different names: '"+getName(offset)+"' and '"+name+"'.");
			}
		}
		if (getType(offset) != null){
			if (! getType(offset).equals(t)){
				throw new LocalVariableInfoInconsistentException("At bytecode offset '"+offset+"' a local variable has two different types: '"+getType(offset)+"' and '"+t+"'.");
			}
		}
		setName(offset, name);
		setType(offset, t);
	}
}
