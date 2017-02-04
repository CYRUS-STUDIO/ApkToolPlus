package org.apache.bcel.verifier.exc;

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

/**
 * Instances of this class are thrown by BCEL's class file verifier "JustIce"
 * whenever
 * verification proves that some constraint of a class file (as stated in the
 * Java Virtual Machine Specification, Edition 2) is violated.
 * This is roughly equivalent to the VerifyError the JVM-internal verifiers
 * throw.
 *
 * @version $Id: VerifierConstraintViolatedException.java,v 1.1 2005/12/16 14:11:30 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public abstract class VerifierConstraintViolatedException extends RuntimeException{
	// /** The name of the offending class that did not pass the verifier. */
	// String name_of_offending_class;

	/** The specified error message. */
	private String detailMessage;
	/**
	 * Constructs a new VerifierConstraintViolatedException with null as its error message string.
	 */
	VerifierConstraintViolatedException(){
		super();
	}
	/**
	 * Constructs a new VerifierConstraintViolatedException with the specified error message.
	 */
	VerifierConstraintViolatedException(String message){
		super(message); // Not that important
		detailMessage = message;
	}


	/** Extends the error message with a string before ("pre") and after ("post") the
	    'old' error message. All of these three strings are allowed to be null, and null
	    is always replaced by the empty string (""). In particular, after invoking this
	    method, the error message of this object can no longer be null.
	*/
	public void extendMessage(String pre, String post){
		if (pre  == null) pre="";
		if (detailMessage == null) detailMessage="";
		if (post == null) post="";
		detailMessage = pre+detailMessage+post;
	}
	/**
	 * Returns the error message string of this VerifierConstraintViolatedException object.
	 * @return the error message string of this VerifierConstraintViolatedException.
	 */
	public String getMessage(){
		return detailMessage;
	}
}
