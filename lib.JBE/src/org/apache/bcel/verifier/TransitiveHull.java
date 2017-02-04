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

import org.apache.bcel.*;
import org.apache.bcel.classfile.JavaClass;

/**
 * This class has a main method implementing a demonstration program
 * of how to use the VerifierFactoryObserver. It transitively verifies
 * all class files encountered; this may take up a lot of time and,
 * more notably, memory.
 *
 * @version $Id: TransitiveHull.java,v 1.3 2006/09/05 15:41:47 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public class TransitiveHull implements VerifierFactoryObserver{

	/** Used for indentation. */
	private int indent = 0;

	/** Not publicly instantiable. */
	private TransitiveHull(){
	}
	
	/* Implementing VerifierFactoryObserver. */
	public void update(String classname){

		System.gc(); // avoid swapping if possible.

		for (int i=0; i<indent; i++){
			System.out.print(" ");
		}
		System.out.println(classname);
		indent += 1;

		Verifier v = VerifierFactory.getVerifier(classname);
	
		VerificationResult vr;
		vr = v.doPass1();
		if (vr != VerificationResult.VR_OK) //System.exit(1);
			System.out.println("Pass 1:\n"+vr);

		vr = v.doPass2();
      if (vr != VerificationResult.VR_OK) //System.exit(1);
			System.out.println("Pass 2:\n"+vr);

		if (vr == VerificationResult.VR_OK){
			JavaClass jc = Repository.lookupClass(v.getClassName());
			for (int i=0; i<jc.getMethods().length; i++){
				vr = v.doPass3a(i);
				if (vr != VerificationResult.VR_OK) //System.exit(1);
					System.out.println(v.getClassName()+", Pass 3a, method "+i+" ['"+jc.getMethods()[i]+"']:\n"+vr);

				vr = v.doPass3b(i);
				if (vr != VerificationResult.VR_OK) //System.exit(1);
					System.out.println(v.getClassName()+", Pass 3b, method "+i+" ['"+jc.getMethods()[i]+"']:\n"+vr);
			}
		}

		indent -= 1;
	}

	/**
	 * This method implements a demonstration program
	 * of how to use the VerifierFactoryObserver. It transitively verifies
	 * all class files encountered; this may take up a lot of time and,
	 * more notably, memory.
	 */
	public static void main(String[] args){
		if (args.length != 1){
			System.out.println("Need exactly one argument: The root class to verify.");
			System.exit(1);
		}

		int dotclasspos = args[0].lastIndexOf(".class");
		if (dotclasspos != -1) args[0] = args[0].substring(0,dotclasspos);
		args[0] = args[0].replace('/', '.');
	
		TransitiveHull th = new TransitiveHull();
		VerifierFactory.attach(th);
		VerifierFactory.getVerifier(args[0]); // the observer is called back and does the actual trick.
		VerifierFactory.detach(th);
	}
}
