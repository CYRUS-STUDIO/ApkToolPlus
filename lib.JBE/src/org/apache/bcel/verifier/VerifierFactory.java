package org.apache.bcel.verifier;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class produces instances of the Verifier class. Its purpose is to make
 * sure that they are singleton instances with respect to the class name they
 * operate on. That means, for every class (represented by a unique fully qualified
 * class name) there is exactly one Verifier.
 *
 * @version $Id: VerifierFactory.java,v 1.4 2006/09/18 14:51:46 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 * @see Verifier
 */
public class VerifierFactory{

	/**
	 * The HashMap that holds the data about the already-constructed Verifier instances.
	 */
	private static HashMap<String, Verifier> hashMap = new HashMap<String, Verifier>();

	/**
	 * The VerifierFactoryObserver instances that observe the VerifierFactory.
	 */
	private static Vector observers = new Vector();

	/**
	 * The VerifierFactory is not instantiable.
	 */
	private VerifierFactory(){}
	
	/**
	 * Returns the (only) verifier responsible for the class with the given name.
	 * Possibly a new Verifier object is transparently created.
	 * @return the (only) verifier responsible for the class with the given name.
	 */
	public static Verifier getVerifier(String fully_qualified_classname){
		
		Verifier v = (Verifier) (hashMap.get(fully_qualified_classname));
		if (v==null){
			v = new Verifier(fully_qualified_classname);
			//hashMap.put(fully_qualified_classname, v);
			notify(fully_qualified_classname);
		}
		
		return v;
	}

	/**
	 * Notifies the observers of a newly generated Verifier.
	 */
	private static void notify(String fully_qualified_classname){
		// notify the observers
		Iterator i = observers.iterator();
		while (i.hasNext()){
			VerifierFactoryObserver vfo = (VerifierFactoryObserver) i.next();
			vfo.update(fully_qualified_classname);
		}
	}

	/**
	 * Returns all Verifier instances created so far.
	 * This is useful when a Verifier recursively lets
	 * the VerifierFactory create other Verifier instances
	 * and if you want to verify the transitive hull of
	 * referenced class files.
	 */
	public static Verifier[] getVerifiers(){
		Verifier[] vs = new Verifier[hashMap.values().size()];
		return (Verifier[]) (hashMap.values().toArray(vs));	// Because vs is big enough, vs is used to store the values into and returned!
	}

	/**
	 * Adds the VerifierFactoryObserver o to the list of observers.
	 */
	public static void attach(VerifierFactoryObserver o){
		observers.addElement(o);
	}
	
	/**
	 * Removes the VerifierFactoryObserver o from the list of observers.
	 */
	public static void detach(VerifierFactoryObserver o){
			observers.removeElement(o);
	}
}
