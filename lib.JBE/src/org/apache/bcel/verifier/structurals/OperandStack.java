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
 * This class implements a stack used for symbolic JVM stack simulation.
 * [It's used an an operand stack substitute.]
 * Elements of this stack are org.apache.bcel.generic.Type objects.
 *
 * @version $Id: OperandStack.java,v 1.3 2006/09/18 14:51:46 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public class OperandStack{

	/** We hold the stack information here. */
	private ArrayList<Type> stack = new ArrayList<Type>();

	/** The maximum number of stack slots this OperandStack instance may hold. */
	private int maxStack;

	/**
	 * Creates an empty stack with a maximum of maxStack slots.
	 */
	public OperandStack(int maxStack){
		this.maxStack = maxStack;
	}

	/**
	 * Creates an otherwise empty stack with a maximum of maxStack slots and
	 * the ObjectType 'obj' at the top.
	 */
	public OperandStack(int maxStack, ObjectType obj){
		this.maxStack = maxStack;
		this.push(obj);
	}	
	/**
	 * Returns a deep copy of this object; that means, the clone operates
	 * on a new stack. However, the Type objects on the stack are
	 * shared.
	 */
	protected Object clone(){
		OperandStack newstack = new OperandStack(this.maxStack);
		newstack.stack = (ArrayList) this.stack.clone();
		return newstack;
	}

	/**
	 * Clears the stack.
	 */
	public void clear(){
		stack = new ArrayList<Type>();
	}

	/**
	 * Returns true if and only if this OperandStack
	 * equals another, meaning equal lengths and equal
	 * objects on the stacks.
	 */
	public boolean equals(Object o){
		if (!(o instanceof OperandStack)) return false;
		OperandStack s = (OperandStack) o;
		return this.stack.equals(s.stack);
	}

	/**
	 * Returns a (typed!) clone of this.
	 *
	 * @see #clone()
	 */
	public OperandStack getClone(){
		return (OperandStack) this.clone();
	}

	/**
	 * Returns true IFF this OperandStack is empty.
   */
	public boolean isEmpty(){
		return stack.isEmpty();
	}

	/**
	 * Returns the number of stack slots this stack can hold.
	 */
	public int maxStack(){
		return this.maxStack;
	}

	/**
	 * Returns the element on top of the stack. The element is not popped off the stack!
	 */
	public Type peek(){
		return peek(0);
	}

	/**
   * Returns the element that's i elements below the top element; that means,
   * iff i==0 the top element is returned. The element is not popped off the stack!
   */
	public Type peek(int i){
		return (Type) stack.get(size()-i-1);
	}

	/**
	 * Returns the element on top of the stack. The element is popped off the stack.
	 */
	public Type pop(){
		Type e = (Type) stack.remove(size()-1);
		return e;
	}

	/**
	 * Pops i elements off the stack. ALWAYS RETURNS "null"!!!
	 */
	public Type pop(int i){
		for (int j=0; j<i; j++){
			pop();
		}
		return null;
	}

	/**
	 * Pushes a Type object onto the stack.
	 */
	public void push(Type type){
		if (type == null) throw new AssertionViolatedException("Cannot push NULL onto OperandStack.");
		if (type == Type.BOOLEAN || type == Type.CHAR || type == Type.BYTE || type == Type.SHORT){
			throw new AssertionViolatedException("The OperandStack does not know about '"+type+"'; use Type.INT instead.");
		}
		if (slotsUsed() >= maxStack){
			throw new AssertionViolatedException("OperandStack too small, should have thrown proper Exception elsewhere. Stack: "+this);
		}
		stack.add(type);
	}

	/**
	 * Returns the size of this OperandStack; that means, how many Type objects there are.
	 */
	int size(){
		return stack.size();
	}

	/**
	 * Returns the number of stack slots used.
	 * @see #maxStack()
	 */	
	public int slotsUsed(){
		/*  XXX change this to a better implementation using a variable
		    that keeps track of the actual slotsUsed()-value monitoring
		    all push()es and pop()s.
		*/
		int slots = 0;
		for (int i=0; i<stack.size(); i++){
			slots += peek(i).getSize();
		}
		return slots;
	}
	
	/**
	 * Returns a String representation of this OperandStack instance.
	 */
	public String toString(){
		String s = "Slots used: "+slotsUsed()+" MaxStack: "+maxStack+".\n";
		for (int i=0; i<size(); i++){
			s+=peek(i)+" (Size: "+peek(i).getSize()+")\n";
		}
		return s;
	}

	/**
	 * Merges another stack state into this instance's stack state.
	 * See the Java Virtual Machine Specification, Second Edition, page 146: 4.9.2
	 * for details.
	 */
	public void merge(OperandStack s){
		if ( (slotsUsed() != s.slotsUsed()) || (size() != s.size()) )
			throw new StructuralCodeConstraintException("Cannot merge stacks of different size:\nOperandStack A:\n"+this+"\nOperandStack B:\n"+s);
		
		for (int i=0; i<size(); i++){
			// If the object _was_ initialized and we're supposed to merge
			// in some uninitialized object, we reject the code (see vmspec2, 4.9.4, last paragraph).
			if ( (! (stack.get(i) instanceof UninitializedObjectType)) && (s.stack.get(i) instanceof UninitializedObjectType) ){
				throw new StructuralCodeConstraintException("Backwards branch with an uninitialized object on the stack detected.");
			}
			// Even harder, we're not initialized but are supposed to broaden
			// the known object type
			if ( (!(stack.get(i).equals(s.stack.get(i)))) && (stack.get(i) instanceof UninitializedObjectType) && (!(s.stack.get(i) instanceof UninitializedObjectType))){
				throw new StructuralCodeConstraintException("Backwards branch with an uninitialized object on the stack detected.");
			}
			// on the other hand...
			if (stack.get(i) instanceof UninitializedObjectType){ //if we have an uninitialized object here
				if (! (s.stack.get(i) instanceof UninitializedObjectType)){ //that has been initialized by now
					stack.set(i, ((UninitializedObjectType) (stack.get(i))).getInitialized() ); //note that.
				}
			}
			if (! stack.get(i).equals(s.stack.get(i))){
				if (	(stack.get(i) instanceof ReferenceType) &&
							(s.stack.get(i) instanceof ReferenceType)  ){
					stack.set(i, ((ReferenceType) stack.get(i)).getFirstCommonSuperclass((ReferenceType) (s.stack.get(i))));
				}
				else{
					throw new StructuralCodeConstraintException("Cannot merge stacks of different types:\nStack A:\n"+this+"\nStack B:\n"+s);
				}
			}
		}
	}

	/**
	 * Replaces all occurences of u in this OperandStack instance
	 * with an "initialized" ObjectType.
	 */
	public void initializeObject(UninitializedObjectType u){
		for (int i=0; i<stack.size(); i++){
			if (stack.get(i) == u){
				stack.set(i, u.getInitialized());
			}
		}
	}

}
