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
import org.apache.bcel.verifier.exc.*;
import java.util.*;

/**
 * This class represents a control flow graph of a method.
 *
 * @version $Id: ControlFlowGraph.java,v 1.2 2006/09/04 15:43:18 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public class ControlFlowGraph{

	/**
	 * Objects of this class represent a node in a ControlFlowGraph.
	 * These nodes are instructions, not basic blocks.
	 */
	private class InstructionContextImpl implements InstructionContext{

		/**
		 * The TAG field is here for external temporary flagging, such
		 * as graph colouring.
		 *
		 * @see #getTag()
		 * @see #setTag(int)
		 */
		private int TAG;

		/**
		 * The InstructionHandle this InstructionContext is wrapped around.
		 */
		private InstructionHandle instruction;

		/**
		 * The 'incoming' execution Frames.
		 */
		private HashMap<InstructionContextImpl, Frame> inFrames;	// key: the last-executed JSR

		/**
		 * The 'outgoing' execution Frames.
		 */
		private HashMap<InstructionContextImpl, Frame> outFrames; // key: the last-executed JSR 

		/**
		 * The 'execution predecessors' - a list of type InstructionContext 
		 * of those instances that have been execute()d before in that order.
		 */
		private ArrayList executionPredecessors = null; // Type: InstructionContext
	
		/**
		 * Creates an InstructionHandleImpl object from an InstructionHandle.
		 * Creation of one per InstructionHandle suffices. Don't create more.
		 */
		public InstructionContextImpl(InstructionHandle inst){
			if (inst == null) throw new AssertionViolatedException("Cannot instantiate InstructionContextImpl from NULL.");
		
			instruction = inst;
			inFrames = new HashMap<InstructionContextImpl, Frame>();
			outFrames = new HashMap<InstructionContextImpl, Frame>();
		}

		/* Satisfies InstructionContext.getTag(). */
		public int getTag(){
			return TAG;
		}

		/* Satisfies InstructionContext.setTag(int). */
		public void setTag(int tag){
			TAG = tag;
		}

		/**
		 * Returns the exception handlers of this instruction.
		 */
		public ExceptionHandler[] getExceptionHandlers(){
			return exceptionhandlers.getExceptionHandlers(getInstruction());
		}

		/**
		 * Returns a clone of the "outgoing" frame situation with respect to the given ExecutionChain.
		 */	
		public Frame getOutFrame(ArrayList execChain){
			executionPredecessors = execChain;

			Frame org;

			InstructionContext jsr = lastExecutionJSR();

			org = (Frame) outFrames.get(jsr);

			if (org == null){
				throw new AssertionViolatedException("outFrame not set! This:\n"+this+"\nExecutionChain: "+getExecutionChain()+"\nOutFrames: '"+outFrames+"'.");
			}
			return org.getClone();
		}

		/**
		 * "Merges in" (vmspec2, page 146) the "incoming" frame situation;
		 * executes the instructions symbolically
		 * and therefore calculates the "outgoing" frame situation.
		 * Returns: True iff the "incoming" frame situation changed after
		 * merging with "inFrame".
		 * The execPreds ArrayList must contain the InstructionContext
		 * objects executed so far in the correct order. This is just
		 * one execution path [out of many]. This is needed to correctly
		 * "merge" in the special case of a RET's successor.
		 * <B>The InstConstraintVisitor and ExecutionVisitor instances
		 * must be set up correctly.</B>
		 * @return true - if and only if the "outgoing" frame situation
		 * changed from the one before execute()ing.
		 */
		public boolean execute(Frame inFrame, ArrayList execPreds, InstConstraintVisitor icv, ExecutionVisitor ev){

			executionPredecessors = (ArrayList) execPreds.clone();

			//sanity check
			if ( (lastExecutionJSR() == null) && (subroutines.subroutineOf(getInstruction()) != subroutines.getTopLevel() ) ){
				throw new AssertionViolatedException("Huh?! Am I '"+this+"' part of a subroutine or not?");
			}
			if ( (lastExecutionJSR() != null) && (subroutines.subroutineOf(getInstruction()) == subroutines.getTopLevel() ) ){
				throw new AssertionViolatedException("Huh?! Am I '"+this+"' part of a subroutine or not?");
			}

			Frame inF = (Frame) inFrames.get(lastExecutionJSR());
			if (inF == null){// no incoming frame was set, so set it.
				inFrames.put(lastExecutionJSR(), inFrame);
				inF = inFrame;
			}
			else{// if there was an "old" inFrame
				if (inF.equals(inFrame)){ //shortcut: no need to merge equal frames.
					return false;
				}
				if (! mergeInFrames(inFrame)){
					return false;
				}
			}
			
			// Now we're sure the inFrame has changed!
			
			// new inFrame is already merged in, see above.		
			Frame workingFrame = inF.getClone();

			try{
				// This verifies the InstructionConstraint for the current
				// instruction, but does not modify the workingFrame object.
//InstConstraintVisitor icv = InstConstraintVisitor.getInstance(VerifierFactory.getVerifier(method_gen.getClassName()));
				icv.setFrame(workingFrame);
				getInstruction().accept(icv);
			}
			catch(StructuralCodeConstraintException ce){
				ce.extendMessage("","\nInstructionHandle: "+getInstruction()+"\n");
				ce.extendMessage("","\nExecution Frame:\n"+workingFrame);
				extendMessageWithFlow(ce);
				throw ce;
			}

			// This executes the Instruction.
			// Therefore the workingFrame object is modified.
//ExecutionVisitor ev = ExecutionVisitor.getInstance(VerifierFactory.getVerifier(method_gen.getClassName()));
			ev.setFrame(workingFrame);
			getInstruction().accept(ev);
			//getInstruction().accept(ExecutionVisitor.withFrame(workingFrame));
			outFrames.put(lastExecutionJSR(), workingFrame);

			return true;	// new inFrame was different from old inFrame so merging them
										// yielded a different this.inFrame.
		}

		/**
		 * Returns a simple String representation of this InstructionContext.
		 */
		public String toString(){
		//TODO: Put information in the brackets, e.g.
		//      Is this an ExceptionHandler? Is this a RET? Is this the start of
		//      a subroutine?
			String ret = getInstruction().toString(false)+"\t[InstructionContext]";
			return ret;
		}

		/**
		 * Does the actual merging (vmspec2, page 146).
		 * Returns true IFF this.inFrame was changed in course of merging with inFrame.
		 */
		private boolean mergeInFrames(Frame inFrame){
			// TODO: Can be performance-improved.
			Frame inF = (Frame) inFrames.get(lastExecutionJSR());
			OperandStack oldstack = inF.getStack().getClone();
			LocalVariables oldlocals = inF.getLocals().getClone();
			try{
				inF.getStack().merge(inFrame.getStack());
				inF.getLocals().merge(inFrame.getLocals());
			}
			catch (StructuralCodeConstraintException sce){
				extendMessageWithFlow(sce);
				throw sce;
			}
			if (	oldstack.equals(inF.getStack()) &&
						oldlocals.equals(inF.getLocals()) ){
				return false;
			}
			else{
				return true;
			}
		}

		/**
		 * Returns the control flow execution chain. This is built
		 * while execute(Frame, ArrayList)-ing the code represented
		 * by the surrounding ControlFlowGraph.
		 */
		private String getExecutionChain(){
			String s = this.toString();
			for (int i=executionPredecessors.size()-1; i>=0; i--){
				s = executionPredecessors.get(i)+"\n" + s;
			}
			return s;
		}


		/**
		 * Extends the StructuralCodeConstraintException ("e") object with an at-the-end-extended message.
		 * This extended message will then reflect the execution flow needed to get to the constraint
		 * violation that triggered the throwing of the "e" object.
		 */
		private void extendMessageWithFlow(StructuralCodeConstraintException e){
			String s = "Execution flow:\n";
			e.extendMessage("", s+getExecutionChain());
		}

		/*
		 * Fulfils the contract of InstructionContext.getInstruction().
		 */
		public InstructionHandle getInstruction(){
			return instruction;
		}

		/**
		 * Returns the InstructionContextImpl with an JSR/JSR_W
		 * that was last in the ExecutionChain, without
		 * a corresponding RET, i.e.
		 * we were called by this one.
		 * Returns null if we were called from the top level.
		 */
		private InstructionContextImpl lastExecutionJSR(){
			
			int size = executionPredecessors.size();
			int retcount = 0;
			
			for (int i=size-1; i>=0; i--){
				InstructionContextImpl current = (InstructionContextImpl) (executionPredecessors.get(i));
				Instruction currentlast = current.getInstruction().getInstruction();
				if (currentlast instanceof RET) retcount++;
				if (currentlast instanceof JsrInstruction){
					retcount--;
					if (retcount == -1) return current;
				}
			}
			return null;
		}

		/* Satisfies InstructionContext.getSuccessors(). */
		public InstructionContext[] getSuccessors(){
			return contextsOf(_getSuccessors());
		}

		/**
		 * A utility method that calculates the successors of a given InstructionHandle
		 * That means, a RET does have successors as defined here.
		 * A JsrInstruction has its target as its successor
		 * (opposed to its physical successor) as defined here.
		 */
// TODO: implement caching!
		private InstructionHandle[] _getSuccessors(){
			final InstructionHandle[] empty = new InstructionHandle[0];
			final InstructionHandle[] single = new InstructionHandle[1];
			final InstructionHandle[] pair = new InstructionHandle[2];
		
			Instruction inst = getInstruction().getInstruction();
		
			if (inst instanceof RET){
				Subroutine s = subroutines.subroutineOf(getInstruction());
				if (s==null){ //return empty; // RET in dead code. "empty" would be the correct answer, but we know something about the surrounding project...
					throw new AssertionViolatedException("Asking for successors of a RET in dead code?!");
				}
//TODO: remove
throw new AssertionViolatedException("DID YOU REALLY WANT TO ASK FOR RET'S SUCCS?");
/*
				InstructionHandle[] jsrs = s.getEnteringJsrInstructions();
				InstructionHandle[] ret = new InstructionHandle[jsrs.length];
				for (int i=0; i<jsrs.length; i++){
					ret[i] = jsrs[i].getNext();
				}
				return ret;
*/
			}
		
			// Terminates method normally.
			if (inst instanceof ReturnInstruction){
				return empty;
			}
		
			// Terminates method abnormally, because JustIce mandates
			// subroutines not to be protected by exception handlers.
			if (inst instanceof ATHROW){
				return empty;
			}
		
			// See method comment.
			if (inst instanceof JsrInstruction){
				single[0] = ((JsrInstruction) inst).getTarget();
				return single;
			}

			if (inst instanceof GotoInstruction){
				single[0] = ((GotoInstruction) inst).getTarget();
				return single;
			}

			if (inst instanceof BranchInstruction){
				if (inst instanceof Select){
					// BCEL's getTargets() returns only the non-default targets,
					// thanks to Eli Tilevich for reporting.
					InstructionHandle[] matchTargets = ((Select) inst).getTargets();
					InstructionHandle[] ret = new InstructionHandle[matchTargets.length+1];
					ret[0] = ((Select) inst).getTarget();
					System.arraycopy(matchTargets, 0, ret, 1, matchTargets.length);
					return ret;
				}
				else{
					pair[0] = getInstruction().getNext();
					pair[1] = ((BranchInstruction) inst).getTarget();
					return pair;
				}
			}

			// default case: Fall through.		
			single[0] = getInstruction().getNext();
			return single;
		}

	} // End Inner InstructionContextImpl Class.

	/** The MethofGen object we're working on. */
	//private final MethodGen method_gen;

	/** The Subroutines object for the method whose control flow is represented by this ControlFlowGraph. */
	private final Subroutines subroutines;

	/** The ExceptionHandlers object for the method whose control flow is represented by this ControlFlowGraph. */
	private final ExceptionHandlers exceptionhandlers;

	/** All InstructionContext instances of this ControlFlowGraph. */
	private Hashtable<InstructionHandle, InstructionContextImpl> instructionContexts = new Hashtable<InstructionHandle, InstructionContextImpl>(); //keys: InstructionHandle, values: InstructionContextImpl

	/** 
	 * A Control Flow Graph.
	 */
	public ControlFlowGraph(MethodGen method_gen){
		subroutines = new Subroutines(method_gen);
		exceptionhandlers = new ExceptionHandlers(method_gen);

		InstructionHandle[] instructionhandles = method_gen.getInstructionList().getInstructionHandles();
		for (int i=0; i<instructionhandles.length; i++){
			instructionContexts.put(instructionhandles[i], new InstructionContextImpl(instructionhandles[i]));
		}
		
		//this.method_gen = method_gen;
	}

	/**
	 * Returns the InstructionContext of a given instruction.
	 */
	public InstructionContext contextOf(InstructionHandle inst){
		InstructionContext ic = (InstructionContext) instructionContexts.get(inst);
		if (ic == null){
			throw new AssertionViolatedException("InstructionContext requested for an InstructionHandle that's not known!");
		}
		return ic;
	}

	/**
	 * Returns the InstructionContext[] of a given InstructionHandle[],
	 * in a naturally ordered manner.
	 */
	public InstructionContext[] contextsOf(InstructionHandle[] insts){
		InstructionContext[] ret = new InstructionContext[insts.length];
		for (int i=0; i<insts.length; i++){
			ret[i] = contextOf(insts[i]);
		}
		return ret;
	}

	/**
	 * Returns an InstructionContext[] with all the InstructionContext instances
	 * for the method whose control flow is represented by this ControlFlowGraph
	 * <B>(NOT ORDERED!)</B>.
	 */
	public InstructionContext[] getInstructionContexts(){
		InstructionContext[] ret = new InstructionContext[instructionContexts.values().size()];
		return (InstructionContext[]) instructionContexts.values().toArray(ret);
	}

	/**
	 * Returns true, if and only if the said instruction is not reachable; that means,
	 * if it not part of this ControlFlowGraph.
	 */
	public boolean isDead(InstructionHandle i){
		return instructionContexts.containsKey(i);
	}	 
}
