package org.apache.bcel.verifier.structurals;

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

import org.apache.bcel.generic.*;

/**
 * This interface defines properties of JVM bytecode subroutines.
 * Note that it is 'abused' to maintain the top-level code in a
 * consistent fashion, too.
 *
 * @version $Id: Subroutine.java,v 1.1 2005/12/16 14:11:30 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public interface Subroutine{
	/**
	 * Returns all the JsrInstructions that have the
	 * first instruction of this subroutine as their target.
	 * <B>Must not be invoked on the 'top-level subroutine'.</B>
	 */
	public InstructionHandle[] getEnteringJsrInstructions();
	
	/**
	 * Returns the one and only RET that leaves the subroutine.
	 * Note that JustIce has a pretty rigid notion of a subroutine.
	 * <B>Must not be invoked on the 'top-level subroutine'.</B>
	 *
	 * @see org.apache.bcel.verifier.structurals.Subroutines
	 */
	public InstructionHandle getLeavingRET();

	/**
	 * Returns all instructions that together form this subroutine.
	 * Note that an instruction is part of exactly one subroutine
	 * (the top-level code is considered to be a special subroutine) -
	 * else it is not reachable at all (dead code).
	 */
	public InstructionHandle[] getInstructions();

	/**
	 * Returns if the given InstructionHandle refers to an instruction
	 * that is part of this subroutine. This is a convenience method
	 * that saves iteration over the InstructionHandle objects returned
	 * by getInstructions().
	 *
	 * @see #getInstructions()
	 */
	public boolean contains(InstructionHandle inst);

	/**
	 * Returns an int[] containing the indices of the local variable slots
	 * accessed by this Subroutine (read-accessed, write-accessed or both);
	 * local variables referenced by subroutines of this subroutine are
	 * not included.
	 *
	 * @see #getRecursivelyAccessedLocalsIndices()
	 */
	public int[] getAccessedLocalsIndices();

	/**
	 * Returns an int[] containing the indices of the local variable slots
	 * accessed by this Subroutine (read-accessed, write-accessed or both);
	 * local variables referenced by subroutines of this subroutine are
	 * included.
	 *
	 * @see #getAccessedLocalsIndices()
	 */
	public int[] getRecursivelyAccessedLocalsIndices();
		
	/**
	 * Returns the subroutines that are directly called from this subroutine.
	 */
	public Subroutine[] subSubs();
}
