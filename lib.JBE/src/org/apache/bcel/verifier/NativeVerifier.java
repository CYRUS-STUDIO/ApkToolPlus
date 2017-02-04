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

/**
 * The NativeVerifier class implements a main(String[] args) method that's
 * roughly compatible to the one in the Verifier class, but that uses the
 * JVM's internal verifier for its class file verification.
 * This can be used for comparison runs between the JVM-internal verifier
 * and JustIce.
 *
 * @version $Id: NativeVerifier.java,v 1.1 2005/12/16 14:11:30 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public abstract class NativeVerifier{

	/**
	 * This class must not be instantiated.
	 */
	private NativeVerifier(){
	}
	
	/**
	 * Works only on the first argument.
	 */
	public static void main(String [] args){
		if (args.length != 1){
			System.out.println("Verifier front-end: need exactly one argument.");
			System.exit(1);
		}

		int dotclasspos = args[0].lastIndexOf(".class");
		if (dotclasspos != -1) args[0] = args[0].substring(0,dotclasspos);
		args[0] = args[0].replace('/','.');
		//System.out.println(args[0]);

		
		try{
			Class.forName(args[0]);
		}
		catch(ExceptionInInitializerError eiie){ //subclass of LinkageError!
			System.out.println("NativeVerifier: ExceptionInInitializerError encountered on '"+args[0]+"'.");
			System.out.println(eiie);
			System.exit(1);		
		}
		catch(LinkageError le){
			System.out.println("NativeVerifier: LinkageError encountered on '"+args[0]+"'.");
			System.out.println(le);
			System.exit(1);
		}
		catch(ClassNotFoundException cnfe){
			System.out.println("NativeVerifier: FILE NOT FOUND: '"+args[0]+"'.");
			System.exit(1);
		}
		catch(Throwable t){
			System.out.println("NativeVerifier: Unspecified verification error on'"+args[0]+"'.");
			System.exit(1);
		}
		
		System.out.println("NativeVerifier: Class file '"+args[0]+"' seems to be okay.");
		System.exit(0);

	}
}
