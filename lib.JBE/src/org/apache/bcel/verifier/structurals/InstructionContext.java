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

import org.apache.bcel.generic.InstructionHandle;
import java.util.ArrayList;

/**
 * An InstructionContext offers convenient access
 * to information like control flow successors and
 * such.
 *
 * @version $Id: InstructionContext.java,v 1.2 2006/09/04 15:43:18 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public interface InstructionContext{

	/**
	 * The getTag and setTag methods may be used for
	 * temporary flagging, such as graph colouring.
	 * Nothing in the InstructionContext object depends
	 * on the value of the tag. JustIce does not use it.
	 * 
	 * @see #setTag(int tag)
	 */
	public int getTag();

	/**
	 * The getTag and setTag methods may be used for
	 * temporary flagging, such as graph colouring.
	 * Nothing in the InstructionContext object depends
	 * on the value of the tag. JustIce does not use it.
	 * 
	 * @see #getTag()
	 */
	public void setTag(int tag);

	/**
	 * This method symbolically executes the Instruction
	 * held in the InstructionContext.
	 * It "merges in" the incoming execution frame situation
	 * (see The Java Virtual Machine Specification, 2nd
	 * edition, page 146).
	 * By so doing, the outgoing execution frame situation
	 * is calculated.
	 *
	 * This method is JustIce-specific and is usually of
	 * no sense for users of the ControlFlowGraph class.
	 * They should use getInstruction().accept(Visitor),
	 * possibly in conjunction with the ExecutionVisitor.
	 * 
	 *
	 * @see ControlFlowGraph
	 * @see ExecutionVisitor
	 * @see #getOutFrame(ArrayList)
	 * @return true -  if and only if the "outgoing" frame situation
	 * changed from the one before execute()ing.
	 */
	boolean execute(Frame inFrame, ArrayList executionPredecessors, InstConstraintVisitor icv, ExecutionVisitor ev);

	/**
	 * This method returns the outgoing execution frame situation;
	 * therefore <B>it has to be calculated by execute(Frame, ArrayList)
	 * first.</B>
	 *
	 * @see #execute(Frame, ArrayList, InstConstraintVisitor, ExecutionVisitor)
	 */
	Frame getOutFrame(ArrayList executionPredecessors);
	
	/**
	 * Returns the InstructionHandle this InstructionContext is wrapped around.
	 *
	 * @return The InstructionHandle this InstructionContext is wrapped around.
	 */
	InstructionHandle getInstruction();

	/**
	 * Returns the usual control flow successors.
	 * @see #getExceptionHandlers()
	 */
	InstructionContext[] getSuccessors();

	/**
	 * Returns the exception handlers that protect this instruction.
	 * They are special control flow successors.
	 */
	ExceptionHandler[] getExceptionHandlers();
}
