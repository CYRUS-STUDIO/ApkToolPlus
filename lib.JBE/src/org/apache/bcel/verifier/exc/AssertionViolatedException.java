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
 * Instances of this class should never be thrown. When such an instance is thrown,
 * this is due to an INTERNAL ERROR of BCEL's class file verifier &quot;JustIce&quot;.
 *
 * @version $Id: AssertionViolatedException.java,v 1.1 2005/12/16 14:11:30 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public final class AssertionViolatedException extends RuntimeException{
	/** The error message. */
	private String detailMessage;
	/** Constructs a new AssertionViolatedException with null as its error message string. */
	public AssertionViolatedException(){
		super();
	}
	/**
	 * Constructs a new AssertionViolatedException with the specified error message preceded
	 * by &quot;INTERNAL ERROR: &quot;.
	 */
	public AssertionViolatedException(String message){
		super(message = "INTERNAL ERROR: "+message); // Thanks to Java, the constructor call here must be first.
		detailMessage=message;
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
	 * Returns the error message string of this AssertionViolatedException object.
	 * @return the error message string of this AssertionViolatedException.
	 */
	public String getMessage(){
		return detailMessage;
	}

	/** 
	 * DO NOT USE. It's for experimental testing during development only.
	 */
	public static void main(String[] args){
		AssertionViolatedException ave = new AssertionViolatedException("Oops!");
		ave.extendMessage("\nFOUND:\n\t","\nExiting!!\n");
		throw ave;
	}

}
