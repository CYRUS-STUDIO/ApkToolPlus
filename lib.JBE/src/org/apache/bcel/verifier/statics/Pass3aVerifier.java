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

import org.apache.bcel.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.verifier.*;
import org.apache.bcel.verifier.exc.*;

/**
 * This PassVerifier verifies a class file according to
 * pass 3, static part as described in The Java Virtual
 * Machine Specification, 2nd edition.
 * More detailed information is to be found at the do_verify()
 * method's documentation. 
 *
 * @version $Id: Pass3aVerifier.java,v 1.1 2005/12/16 14:11:30 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 * @see #do_verify()
 */
public final class Pass3aVerifier extends PassVerifier{

	/** The Verifier that created this. */
	private Verifier myOwner;

	/** 
	 * The method number to verify.
	 * This is the index in the array returned
	 * by JavaClass.getMethods().
	 */
	private int method_no;

	/** The one and only InstructionList object used by an instance of this class. It's here for performance reasons by do_verify() and its callees. */	
	InstructionList instructionList;
	/** The one and only Code object used by an instance of this class. It's here for performance reasons by do_verify() and its callees. */	
	Code code;

	/** Should only be instantiated by a Verifier. */
	public Pass3aVerifier(Verifier owner, int method_no){
		myOwner = owner;
		this.method_no = method_no;
	}

	/**
	 * Pass 3a is the verification of static constraints of
	 * JVM code (such as legal targets of branch instructions).
	 * This is the part of pass 3 where you do not need data
	 * flow analysis.
	 * JustIce also delays the checks for a correct exception
	 * table of a Code attribute and correct line number entries
	 * in a LineNumberTable attribute of a Code attribute (which
	 * conceptually belong to pass 2) to this pass. Also, most
	 * of the check for valid local variable entries in a
	 * LocalVariableTable attribute of a Code attribute is
	 * delayed until this pass.
	 * All these checks need access to the code array of the
	 * Code attribute.
	 *
	 * @throws InvalidMethodException if the method to verify does not exist.
	 */
	public VerificationResult do_verify(){
		if (myOwner.doPass2().equals(VerificationResult.VR_OK)){
			// Okay, class file was loaded correctly by Pass 1
			// and satisfies static constraints of Pass 2.
			JavaClass jc = Repository.lookupClass(myOwner.getClassName());
			Method[] methods = jc.getMethods();
			if (method_no >= methods.length){
				throw new InvalidMethodException("METHOD DOES NOT EXIST!");
			}
			Method method = methods[method_no];
			code = method.getCode();
			
			// No Code? Nothing to verify!
			if ( method.isAbstract() || method.isNative() ){ // IF mg HAS NO CODE (static constraint of Pass 2)
				return VerificationResult.VR_OK;
			}

			// TODO:
			// We want a very sophisticated code examination here with good explanations
			// on where to look for an illegal instruction or such.
			// Only after that we should try to build an InstructionList and throw an
			// AssertionViolatedException if after our examination InstructionList building
			// still fails.
			// That examination should be implemented in a byte-oriented way, i.e. look for
			// an instruction, make sure its validity, count its length, find the next
			// instruction and so on.
			try{
				instructionList = new InstructionList(method.getCode().getCode());
			}
			catch(RuntimeException re){
				return new VerificationResult(VerificationResult.VERIFIED_REJECTED, "Bad bytecode in the code array of the Code attribute of method '"+method+"'.");
			}
			
			instructionList.setPositions(true);

			// Start verification.
			VerificationResult vr = VerificationResult.VR_OK; //default
			try{
				delayedPass2Checks();
			}
			catch(ClassConstraintException cce){
				vr = new VerificationResult(VerificationResult.VERIFIED_REJECTED, cce.getMessage());
				return vr;
			}
			try{
				pass3StaticInstructionChecks();
				pass3StaticInstructionOperandsChecks();
			}
			catch(StaticCodeConstraintException scce){
				vr = new VerificationResult(VerificationResult.VERIFIED_REJECTED, scce.getMessage());
			}
			return vr;
		}
		else{ //did not pass Pass 2.
			return VerificationResult.VR_NOTYET;
		}
	}

	/**
	 * These are the checks that could be done in pass 2 but are delayed to pass 3
	 * for performance reasons. Also, these checks need access to the code array
	 * of the Code attribute of a Method so it's okay to perform them here.
	 * Also see the description of the do_verify() method.
	 *
	 * @throws ClassConstraintException if the verification fails.
	 * @see #do_verify()
	 */
	private void delayedPass2Checks(){

		int[] instructionPositions = instructionList.getInstructionPositions();
		int codeLength = code.getCode().length;

		/////////////////////
		// LineNumberTable //
		/////////////////////
		LineNumberTable lnt = code.getLineNumberTable();
		if (lnt != null){
			LineNumber[] lineNumbers = lnt.getLineNumberTable();
			IntList offsets = new IntList();
			lineNumber_loop: for (int i=0; i < lineNumbers.length; i++){ // may appear in any order.
				for (int j=0; j < instructionPositions.length; j++){
					// TODO: Make this a binary search! The instructionPositions array is naturally ordered!
					int offset = lineNumbers[i].getStartPC();
					if (instructionPositions[j] == offset){
						if (offsets.contains(offset)){
							addMessage("LineNumberTable attribute '"+code.getLineNumberTable()+"' refers to the same code offset ('"+offset+"') more than once which is violating the semantics [but is sometimes produced by IBM's 'jikes' compiler].");
						}
						else{
							offsets.add(offset);
						}
						continue lineNumber_loop;
					}
				}
				throw new ClassConstraintException("Code attribute '"+code+"' has a LineNumberTable attribute '"+code.getLineNumberTable()+"' referring to a code offset ('"+lineNumbers[i].getStartPC()+"') that does not exist.");
			}
		}

		///////////////////////////
		// LocalVariableTable(s) //
		///////////////////////////
		/* We cannot use code.getLocalVariableTable() because there could be more
		   than only one. This is a bug in BCEL. */
		Attribute[] atts = code.getAttributes();
		for (int a=0; a<atts.length; a++){
			if (atts[a] instanceof LocalVariableTable){
				LocalVariableTable lvt = (LocalVariableTable) atts[a];
				if (lvt != null){
					LocalVariable[] localVariables = lvt.getLocalVariableTable();
					for (int i=0; i<localVariables.length; i++){
						int startpc = localVariables[i].getStartPC();
						int length  = localVariables[i].getLength();
				
						if (!contains(instructionPositions, startpc)){
							throw new ClassConstraintException("Code attribute '"+code+"' has a LocalVariableTable attribute '"+code.getLocalVariableTable()+"' referring to a code offset ('"+startpc+"') that does not exist.");
						}
						if ( (!contains(instructionPositions, startpc+length)) && (startpc+length != codeLength) ){
							throw new ClassConstraintException("Code attribute '"+code+"' has a LocalVariableTable attribute '"+code.getLocalVariableTable()+"' referring to a code offset start_pc+length ('"+(startpc+length)+"') that does not exist.");
						}
					}
				}
			}
		}
		
		////////////////////
		// ExceptionTable //
		////////////////////
		// In BCEL's "classfile" API, the startPC/endPC-notation is
		// inclusive/exclusive as in the Java Virtual Machine Specification.
		// WARNING: This is not true for BCEL's "generic" API.
		CodeException[] exceptionTable = code.getExceptionTable();
		for (int i=0; i<exceptionTable.length; i++){
			int startpc = exceptionTable[i].getStartPC();
			int endpc = exceptionTable[i].getEndPC();
			int handlerpc = exceptionTable[i].getHandlerPC();
			if (startpc >= endpc){
				throw new ClassConstraintException("Code attribute '"+code+"' has an exception_table entry '"+exceptionTable[i]+"' that has its start_pc ('"+startpc+"') not smaller than its end_pc ('"+endpc+"').");
			}
			if (!contains(instructionPositions, startpc)){
				throw new ClassConstraintException("Code attribute '"+code+"' has an exception_table entry '"+exceptionTable[i]+"' that has a non-existant bytecode offset as its start_pc ('"+startpc+"').");
			}
			if ( (!contains(instructionPositions, endpc)) && (endpc != codeLength)){
				throw new ClassConstraintException("Code attribute '"+code+"' has an exception_table entry '"+exceptionTable[i]+"' that has a non-existant bytecode offset as its end_pc ('"+startpc+"') [that is also not equal to code_length ('"+codeLength+"')].");
			}
			if (!contains(instructionPositions, handlerpc)){
				throw new ClassConstraintException("Code attribute '"+code+"' has an exception_table entry '"+exceptionTable[i]+"' that has a non-existant bytecode offset as its handler_pc ('"+handlerpc+"').");
			}
		}
	}

	/**
	 * These are the checks if constraints are satisfied which are described in the
	 * Java Virtual Machine Specification, Second Edition as Static Constraints on
	 * the instructions of Java Virtual Machine Code (chapter 4.8.1).
	 *
	 * @throws StaticCodeConstraintException if the verification fails.
	 */
	private void pass3StaticInstructionChecks(){
		
		// Code array must not be empty:
		// Enforced in pass 2 (also stated in the static constraints of the Code
		// array in vmspec2), together with pass 1 (reading code_length bytes and
		// interpreting them as code[]). So this must not be checked again here.

		if (! (code.getCode().length < 65536)){// contradicts vmspec2 page 152 ("Limitations"), but is on page 134.
			throw new StaticCodeInstructionConstraintException("Code array in code attribute '"+code+"' too big: must be smaller than 65536 bytes.");
		}

		// First opcode at offset 0: okay, that's clear. Nothing to do.
		
		// Only instances of the instructions documented in Section 6.4 may appear in
		// the code array.
		
		// For BCEL's sake, we cannot handle WIDE stuff, but hopefully BCEL does its job right :)
		
		// The last byte of the last instruction in the code array must be the byte at index
		// code_length-1 : See the do_verify() comments. We actually don't iterate through the
		// byte array, but use an InstructionList so we cannot check for this. But BCEL does
		// things right, so it's implicitly okay.
		
		// TODO: Check how BCEL handles (and will handle) instructions like IMPDEP1, IMPDEP2,
		//       BREAKPOINT... that BCEL knows about but which are illegal anyway.
		//       We currently go the safe way here.
		InstructionHandle ih = instructionList.getStart();
		while (ih != null){
			Instruction i = ih.getInstruction();
			if (i instanceof IMPDEP1){
				throw new StaticCodeInstructionConstraintException("IMPDEP1 must not be in the code, it is an illegal instruction for _internal_ JVM use!");
			}
			if (i instanceof IMPDEP2){
				throw new StaticCodeInstructionConstraintException("IMPDEP2 must not be in the code, it is an illegal instruction for _internal_ JVM use!");
			}
			if (i instanceof BREAKPOINT){
				throw new StaticCodeInstructionConstraintException("BREAKPOINT must not be in the code, it is an illegal instruction for _internal_ JVM use!");
			}
			ih = ih.getNext();
		}
		
		// The original verifier seems to do this check here, too.
		// An unreachable last instruction may also not fall through the
		// end of the code, which is stupid -- but with the original
		// verifier's subroutine semantics one cannot predict reachability.
		Instruction last = instructionList.getEnd().getInstruction();
		if (! ((last instanceof ReturnInstruction)	||
					(last instanceof RET)    							||
					(last instanceof GotoInstruction)			||
					(last instanceof ATHROW) )) // JSR / JSR_W would possibly RETurn and then fall off the code!
			throw new StaticCodeInstructionConstraintException("Execution must not fall off the bottom of the code array. This constraint is enforced statically as some existing verifiers do - so it may be a false alarm if the last instruction is not reachable.");
	}

	/**
	 * These are the checks for the satisfaction of constraints which are described in the
	 * Java Virtual Machine Specification, Second Edition as Static Constraints on
	 * the operands of instructions of Java Virtual Machine Code (chapter 4.8.1).
	 * BCEL parses the code array to create an InstructionList and therefore has to check
	 * some of these constraints. Additional checks are also implemented here.
	 *
	 * @throws StaticCodeConstraintException if the verification fails.
	 */
	private void pass3StaticInstructionOperandsChecks(){
		// When building up the InstructionList, BCEL has already done all those checks
		// mentioned in The Java Virtual Machine Specification, Second Edition, as
		// "static constraints on the operands of instructions in the code array".
		// TODO: see the do_verify() comments. Maybe we should really work on the
		//       byte array first to give more comprehensive messages.
		// TODO: Review Exception API, possibly build in some "offending instruction" thing
		//       when we're ready to insulate the offending instruction by doing the
		//       above thing.

		// TODO: Implement as much as possible here. BCEL does _not_ check everything.

		ConstantPoolGen cpg = new ConstantPoolGen(Repository.lookupClass(myOwner.getClassName()).getConstantPool());
		InstOperandConstraintVisitor v = new InstOperandConstraintVisitor(cpg);
	
		// Checks for the things BCEL does _not_ handle itself.
		InstructionHandle ih = instructionList.getStart();
		while (ih != null){
			Instruction i = ih.getInstruction();
			
			// An "own" constraint, due to JustIce's new definition of what "subroutine" means.
			if (i instanceof JsrInstruction){
				InstructionHandle target = ((JsrInstruction) i).getTarget();
				if (target == instructionList.getStart()){
					throw new StaticCodeInstructionOperandConstraintException("Due to JustIce's clear definition of subroutines, no JSR or JSR_W may have a top-level instruction (such as the very first instruction, which is targeted by instruction '"+ih+"' as its target.");
				}
				if (!(target.getInstruction() instanceof ASTORE)){
					throw new StaticCodeInstructionOperandConstraintException("Due to JustIce's clear definition of subroutines, no JSR or JSR_W may target anything else than an ASTORE instruction. Instruction '"+ih+"' targets '"+target+"'.");
				}
			}
			
			// vmspec2, page 134-137
			ih.accept(v);
			
			ih = ih.getNext();
		}

	}
	
	/** A small utility method returning if a given int i is in the given int[] ints. */
	private static boolean contains(int[] ints, int i){
		for (int j=0; j<ints.length; j++){
			if (ints[j]==i) return true;
		}
		return false;
	}

	/** Returns the method number as supplied when instantiating. */
	public int getMethodNo(){
		return method_no;
	}

	/**
	 * This visitor class does the actual checking for the instruction
	 * operand's constraints.
	 */
	private class InstOperandConstraintVisitor extends EmptyVisitor {
		/** The ConstantPoolGen instance this Visitor operates on. */
		private ConstantPoolGen cpg;

		/** The only Constructor. */
		InstOperandConstraintVisitor(ConstantPoolGen cpg){
			this.cpg = cpg;
		}

		/**
		 * Utility method to return the max_locals value of the method verified
		 * by the surrounding Pass3aVerifier instance.
		 */
		private int max_locals(){
			return Repository.lookupClass(myOwner.getClassName()).getMethods()[method_no].getCode().getMaxLocals();
		}

		/**
		 * A utility method to always raise an exeption.
		 */
		private void constraintViolated(Instruction i, String message) {
			throw new StaticCodeInstructionOperandConstraintException("Instruction "+i+" constraint violated: "+message);
		}

		/**
		 * A utility method to raise an exception if the index is not
		 * a valid constant pool index.
		 */
		private void indexValid(Instruction i, int idx){
			if (idx < 0 || idx >= cpg.getSize()){
				constraintViolated(i, "Illegal constant pool index '"+idx+"'.");
			}
		}

		///////////////////////////////////////////////////////////
		// The Java Virtual Machine Specification, pages 134-137 //
		///////////////////////////////////////////////////////////
		/**
		 * Assures the generic preconditions of a LoadClass instance.
		 * The referenced class is loaded and pass2-verified.
		 */
		public void visitLoadClass(LoadClass o){
			ObjectType t = o.getLoadClassType(cpg);
			if (t != null){// null means "no class is loaded"
				Verifier v = VerifierFactory.getVerifier(t.getClassName());
				VerificationResult vr = v.doPass1();
				if (vr.getStatus() != VerificationResult.VERIFIED_OK){
					constraintViolated((Instruction) o, "Class '"+o.getLoadClassType(cpg).getClassName()+"' is referenced, but cannot be loaded: '"+vr+"'.");
				}
			}
		}
		
		// The target of each jump and branch instruction [...] must be the opcode [...]
		// BCEL _DOES_ handle this.

		// tableswitch: BCEL will do it, supposedly.
		
		// lookupswitch: BCEL will do it, supposedly.
		
		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		// LDC and LDC_W (LDC_W is a subclass of LDC in BCEL's model)
		public void visitLDC(LDC o){
			indexValid(o, o.getIndex());
			Constant c = cpg.getConstant(o.getIndex());
			if (! ( (c instanceof ConstantInteger)	||
							(c instanceof ConstantFloat) 		||
							(c instanceof ConstantString) ) ){
				constraintViolated(o, "Operand of LDC or LDC_W must be one of CONSTANT_Integer, CONSTANT_Float or CONSTANT_String, but is '"+c+"'.");
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		// LDC2_W
		public void visitLDC2_W(LDC2_W o){
			indexValid(o, o.getIndex());
			Constant c = cpg.getConstant(o.getIndex());
			if (! ( (c instanceof ConstantLong)	||
							(c instanceof ConstantDouble) ) ){
				constraintViolated(o, "Operand of LDC2_W must be CONSTANT_Long or CONSTANT_Double, but is '"+c+"'.");
			}
			try{
				indexValid(o, o.getIndex()+1);
			}
			catch(StaticCodeInstructionOperandConstraintException e){
				throw new AssertionViolatedException("OOPS: Does not BCEL handle that? LDC2_W operand has a problem.");
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
 		//getfield, putfield, getstatic, putstatic
 		public void visitFieldInstruction(FieldInstruction o){
			indexValid(o, o.getIndex());
			Constant c = cpg.getConstant(o.getIndex());
			if (! (c instanceof ConstantFieldref)){
				constraintViolated(o, "Indexing a constant that's not a CONSTANT_Fieldref but a '"+c+"'.");
			}
			
			String field_name = o.getFieldName(cpg);
 
			JavaClass jc = Repository.lookupClass(o.getClassType(cpg).getClassName());
			Field[] fields = jc.getFields();
			Field f = null;
			for (int i=0; i<fields.length; i++){
				if (fields[i].getName().equals(field_name)){
					f = fields[i];
					break;
				}
			}
			if (f == null){
				/* TODO: also look up if the field is inherited! */
				constraintViolated(o, "Referenced field '"+field_name+"' does not exist in class '"+jc.getClassName()+"'.");
			}
			else{
				/* TODO: Check if assignment compatibility is sufficient.
				   What does Sun do? */
				Type f_type = Type.getType(f.getSignature());
				Type o_type = o.getType(cpg);
				
				/* TODO: Is there a way to make BCEL tell us if a field
				has a void method's signature, i.e. "()I" instead of "I"? */
				
				if (! f_type.equals(o_type)){
					constraintViolated(o, "Referenced field '"+field_name+"' has type '"+f_type+"' instead of '"+o_type+"' as expected.");
				}
				/* TODO: Check for access modifiers here. */
			}
		}	

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitInvokeInstruction(InvokeInstruction o){
			indexValid(o, o.getIndex());
			if (	(o instanceof INVOKEVIRTUAL)	||
						(o instanceof INVOKESPECIAL)	||
						(o instanceof INVOKESTATIC)	){
				Constant c = cpg.getConstant(o.getIndex());
				if (! (c instanceof ConstantMethodref)){
					constraintViolated(o, "Indexing a constant that's not a CONSTANT_Methodref but a '"+c+"'.");
				}
				else{
					// Constants are okay due to pass2.
					ConstantNameAndType cnat = (ConstantNameAndType) (cpg.getConstant(((ConstantMethodref) c).getNameAndTypeIndex()));
					ConstantUtf8 cutf8 = (ConstantUtf8) (cpg.getConstant(cnat.getNameIndex()));
					if (cutf8.getBytes().equals(Constants.CONSTRUCTOR_NAME) && (!(o instanceof INVOKESPECIAL)) ){
						constraintViolated(o, "Only INVOKESPECIAL is allowed to invoke instance initialization methods.");
					}
					if ( (! (cutf8.getBytes().equals(Constants.CONSTRUCTOR_NAME)) ) && (cutf8.getBytes().startsWith("<")) ){
						constraintViolated(o, "No method with a name beginning with '<' other than the instance initialization methods may be called by the method invocation instructions.");
					}
				}
			}
			else{ //if (o instanceof INVOKEINTERFACE){
				Constant c = cpg.getConstant(o.getIndex());
				if (! (c instanceof ConstantInterfaceMethodref)){
					constraintViolated(o, "Indexing a constant that's not a CONSTANT_InterfaceMethodref but a '"+c+"'.");
				}
				// TODO: From time to time check if BCEL allows to detect if the
				// 'count' operand is consistent with the information in the
				// CONSTANT_InterfaceMethodref and if the last operand is zero.
				// By now, BCEL hides those two operands because they're superfluous.
				
				// Invoked method must not be <init> or <clinit>
				ConstantNameAndType cnat = (ConstantNameAndType) (cpg.getConstant(((ConstantInterfaceMethodref)c).getNameAndTypeIndex()));
				String name = ((ConstantUtf8) (cpg.getConstant(cnat.getNameIndex()))).getBytes();
				if (name.equals(Constants.CONSTRUCTOR_NAME)){
					constraintViolated(o, "Method to invoke must not be '"+Constants.CONSTRUCTOR_NAME+"'.");
				}
				if (name.equals(Constants.STATIC_INITIALIZER_NAME)){
					constraintViolated(o, "Method to invoke must not be '"+Constants.STATIC_INITIALIZER_NAME+"'.");
				}
			}
		
			// The LoadClassType is the method-declaring class, so we have to check the other types.
			
			Type t = o.getReturnType(cpg);
			if (t instanceof ArrayType){
				t = ((ArrayType) t).getBasicType();
			}
			if (t instanceof ObjectType){
				Verifier v = VerifierFactory.getVerifier(((ObjectType) t).getClassName());
				VerificationResult vr = v.doPass2();
				if (vr.getStatus() != VerificationResult.VERIFIED_OK){
					constraintViolated(o, "Return type class/interface could not be verified successfully: '"+vr.getMessage()+"'.");
				}
			}
			
			Type[] ts = o.getArgumentTypes(cpg);
			for (int i=0; i<ts.length; i++){
				t = ts[i];
				if (t instanceof ArrayType){
					t = ((ArrayType) t).getBasicType();
				}
				if (t instanceof ObjectType){
					Verifier v = VerifierFactory.getVerifier(((ObjectType) t).getClassName());
					VerificationResult vr = v.doPass2();
					if (vr.getStatus() != VerificationResult.VERIFIED_OK){
						constraintViolated(o, "Argument type class/interface could not be verified successfully: '"+vr.getMessage()+"'.");
					}
				}
			}
			
		}
		
		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitINSTANCEOF(INSTANCEOF o){
			indexValid(o, o.getIndex());
			Constant c = cpg.getConstant(o.getIndex());
			if (!	(c instanceof ConstantClass)){
				constraintViolated(o, "Expecting a CONSTANT_Class operand, but found a '"+c+"'.");
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitCHECKCAST(CHECKCAST o){
			indexValid(o, o.getIndex());
			Constant c = cpg.getConstant(o.getIndex());
			if (!	(c instanceof ConstantClass)){
				constraintViolated(o, "Expecting a CONSTANT_Class operand, but found a '"+c+"'.");
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitNEW(NEW o){
			indexValid(o, o.getIndex());
			Constant c = cpg.getConstant(o.getIndex());
			if (!	(c instanceof ConstantClass)){
				constraintViolated(o, "Expecting a CONSTANT_Class operand, but found a '"+c+"'.");
			}
			else{
				ConstantUtf8 cutf8 = (ConstantUtf8) (cpg.getConstant( ((ConstantClass) c).getNameIndex() ));
				Type t = Type.getType("L"+cutf8.getBytes()+";");
				if (t instanceof ArrayType){
					constraintViolated(o, "NEW must not be used to create an array.");
				}
			}
			
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitMULTIANEWARRAY(MULTIANEWARRAY o){
			indexValid(o, o.getIndex());
			Constant c = cpg.getConstant(o.getIndex());
			if (!	(c instanceof ConstantClass)){
				constraintViolated(o, "Expecting a CONSTANT_Class operand, but found a '"+c+"'.");
			}
			int dimensions2create = o.getDimensions();
			if (dimensions2create < 1){
				constraintViolated(o, "Number of dimensions to create must be greater than zero.");
			}
			Type t = o.getType(cpg);
			if (t instanceof ArrayType){
				int dimensions = ((ArrayType) t).getDimensions();
				if (dimensions < dimensions2create){
					constraintViolated(o, "Not allowed to create array with more dimensions ('+dimensions2create+') than the one referenced by the CONSTANT_Class '"+t+"'.");
				}
			}
			else{
				constraintViolated(o, "Expecting a CONSTANT_Class referencing an array type. [Constraint not found in The Java Virtual Machine Specification, Second Edition, 4.8.1]");
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitANEWARRAY(ANEWARRAY o){
			indexValid(o, o.getIndex());
			Constant c = cpg.getConstant(o.getIndex());
			if (!	(c instanceof ConstantClass)){
				constraintViolated(o, "Expecting a CONSTANT_Class operand, but found a '"+c+"'.");
			}
			Type t = o.getType(cpg);
			if (t instanceof ArrayType){
				int dimensions = ((ArrayType) t).getDimensions();
				if (dimensions >= 255){
					constraintViolated(o, "Not allowed to create an array with more than 255 dimensions.");
				}
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitNEWARRAY(NEWARRAY o){
			byte t = o.getTypecode();
			if (!	(	(t == Constants.T_BOOLEAN)	||
							(t == Constants.T_CHAR)			||
							(t == Constants.T_FLOAT)		||
							(t == Constants.T_DOUBLE)		||
							(t == Constants.T_BYTE)			||
							(t == Constants.T_SHORT)		||
							(t == Constants.T_INT)			||
							(t == Constants.T_LONG)	)	){
				constraintViolated(o, "Illegal type code '+t+' for 'atype' operand.");
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitILOAD(ILOAD o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative.");
			}
			else{
				int maxminus1 =  max_locals()-1;
				if (idx > maxminus1){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-1 '"+maxminus1+"'.");
				}
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitFLOAD(FLOAD o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative.");
			}
			else{
				int maxminus1 =  max_locals()-1;
				if (idx > maxminus1){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-1 '"+maxminus1+"'.");
				}
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitALOAD(ALOAD o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative.");
			}
			else{
				int maxminus1 =  max_locals()-1;
				if (idx > maxminus1){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-1 '"+maxminus1+"'.");
				}
			}
		}
		
		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitISTORE(ISTORE o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative.");
			}
			else{
				int maxminus1 =  max_locals()-1;
				if (idx > maxminus1){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-1 '"+maxminus1+"'.");
				}
			}
		}
		
		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitFSTORE(FSTORE o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative.");
			}
			else{
				int maxminus1 =  max_locals()-1;
				if (idx > maxminus1){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-1 '"+maxminus1+"'.");
				}
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitASTORE(ASTORE o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative.");
			}
			else{
				int maxminus1 =  max_locals()-1;
				if (idx > maxminus1){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-1 '"+maxminus1+"'.");
				}
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitIINC(IINC o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative.");
			}
			else{
				int maxminus1 =  max_locals()-1;
				if (idx > maxminus1){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-1 '"+maxminus1+"'.");
				}
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitRET(RET o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative.");
			}
			else{
				int maxminus1 =  max_locals()-1;
				if (idx > maxminus1){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-1 '"+maxminus1+"'.");
				}
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitLLOAD(LLOAD o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative. [Constraint by JustIce as an analogon to the single-slot xLOAD/xSTORE instructions; may not happen anyway.]");
			}
			else{
				int maxminus2 =  max_locals()-2;
				if (idx > maxminus2){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-2 '"+maxminus2+"'.");
				}
			}
		}
		
		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitDLOAD(DLOAD o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative. [Constraint by JustIce as an analogon to the single-slot xLOAD/xSTORE instructions; may not happen anyway.]");
			}
			else{
				int maxminus2 =  max_locals()-2;
				if (idx > maxminus2){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-2 '"+maxminus2+"'.");
				}
			}
		}
		
		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitLSTORE(LSTORE o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative. [Constraint by JustIce as an analogon to the single-slot xLOAD/xSTORE instructions; may not happen anyway.]");
			}
			else{
				int maxminus2 =  max_locals()-2;
				if (idx > maxminus2){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-2 '"+maxminus2+"'.");
				}
			}
		}
		
		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitDSTORE(DSTORE o){
			int idx = o.getIndex();
			if (idx < 0){
				constraintViolated(o, "Index '"+idx+"' must be non-negative. [Constraint by JustIce as an analogon to the single-slot xLOAD/xSTORE instructions; may not happen anyway.]");
			}
			else{
				int maxminus2 =  max_locals()-2;
				if (idx > maxminus2){
					constraintViolated(o, "Index '"+idx+"' must not be greater than max_locals-2 '"+maxminus2+"'.");
				}
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitLOOKUPSWITCH(LOOKUPSWITCH o){
			int[] matchs = o.getMatchs();
			int max = Integer.MIN_VALUE;
			for (int i=0; i<matchs.length; i++){
				if (matchs[i] == max && i != 0){
					constraintViolated(o, "Match '"+matchs[i]+"' occurs more than once.");
				}
				if (matchs[i] < max){
					constraintViolated(o, "Lookup table must be sorted but isn't.");
				}
				else{
					max = matchs[i];
				}
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitTABLESWITCH(TABLESWITCH o){ 	
			// "high" must be >= "low". We cannot check this, as BCEL hides
			// it from us.
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitPUTSTATIC(PUTSTATIC o){
			String field_name = o.getFieldName(cpg);
			JavaClass jc = Repository.lookupClass(o.getClassType(cpg).getClassName());
			Field[] fields = jc.getFields();
			Field f = null;
			for (int i=0; i<fields.length; i++){
				if (fields[i].getName().equals(field_name)){
					f = fields[i];
					break;
				}
			}
			if (f == null){
				throw new AssertionViolatedException("Field not found?!?");
			}

			if (f.isFinal()){
				if (!(myOwner.getClassName().equals(o.getClassType(cpg).getClassName()))){
					constraintViolated(o, "Referenced field '"+f+"' is final and must therefore be declared in the current class '"+myOwner.getClassName()+"' which is not the case: it is declared in '"+o.getClassType(cpg).getClassName()+"'.");
				}
			}

			if (! (f.isStatic())){
				constraintViolated(o, "Referenced field '"+f+"' is not static which it should be.");
			}

			String meth_name = Repository.lookupClass(myOwner.getClassName()).getMethods()[method_no].getName();

			// If it's an interface, it can be set only in <clinit>.
			if ((!(jc.isClass())) && (!(meth_name.equals(Constants.STATIC_INITIALIZER_NAME)))){
				constraintViolated(o, "Interface field '"+f+"' must be set in a '"+Constants.STATIC_INITIALIZER_NAME+"' method.");
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitGETSTATIC(GETSTATIC o){
			String field_name = o.getFieldName(cpg);
			JavaClass jc = Repository.lookupClass(o.getClassType(cpg).getClassName());
			Field[] fields = jc.getFields();
			Field f = null;
			for (int i=0; i<fields.length; i++){
				if (fields[i].getName().equals(field_name)){
					f = fields[i];
					break;
				}
			}
			if (f == null){
				throw new AssertionViolatedException("Field not found?!?");
			}

			if (! (f.isStatic())){
				constraintViolated(o, "Referenced field '"+f+"' is not static which it should be.");
			}
		}

		/* Checks if the constraints of operands of the said instruction(s) are satisfied. */
		//public void visitPUTFIELD(PUTFIELD o){
			// for performance reasons done in Pass 3b
		//}
		
		/* Checks if the constraints of operands of the said instruction(s) are satisfied. */
		//public void visitGETFIELD(GETFIELD o){
			// for performance reasons done in Pass 3b
		//}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitINVOKEINTERFACE(INVOKEINTERFACE o){
			// INVOKEINTERFACE is a LoadClass; the Class where the referenced method is declared in,
			// is therefore resolved/verified.
			// INVOKEINTERFACE is an InvokeInstruction, the argument and return types are resolved/verified,
			// too. So are the allowed method names.
			String classname = o.getClassName(cpg);
			JavaClass jc = Repository.lookupClass(classname);
			Method[] ms = jc.getMethods();
			Method m = null;
			for (int i=0; i<ms.length; i++){
				if ( (ms[i].getName().equals(o.getMethodName(cpg))) &&
				     (Type.getReturnType(ms[i].getSignature()).equals(o.getReturnType(cpg))) &&
				     (objarrayequals(Type.getArgumentTypes(ms[i].getSignature()), o.getArgumentTypes(cpg))) ){
					m = ms[i];
					break;
				}
			}
			if (m == null){
				constraintViolated(o, "Referenced method '"+o.getMethodName(cpg)+"' with expected signature not found in class '"+jc.getClassName()+"'. The native verfier does allow the method to be declared in some superinterface, which the Java Virtual Machine Specification, Second Edition does not.");
			}
			if (jc.isClass()){
				constraintViolated(o, "Referenced class '"+jc.getClassName()+"' is a class, but not an interface as expected.");
			}
		}

		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitINVOKESPECIAL(INVOKESPECIAL o){
			// INVOKESPECIAL is a LoadClass; the Class where the referenced method is declared in,
			// is therefore resolved/verified.
			// INVOKESPECIAL is an InvokeInstruction, the argument and return types are resolved/verified,
			// too. So are the allowed method names.
			String classname = o.getClassName(cpg);
			JavaClass jc = Repository.lookupClass(classname);
			Method[] ms = jc.getMethods();
			Method m = null;
			for (int i=0; i<ms.length; i++){
				if ( (ms[i].getName().equals(o.getMethodName(cpg))) &&
				     (Type.getReturnType(ms[i].getSignature()).equals(o.getReturnType(cpg))) &&
				     (objarrayequals(Type.getArgumentTypes(ms[i].getSignature()), o.getArgumentTypes(cpg))) ){
					m = ms[i];
					break;
				}
			}
			if (m == null){
				constraintViolated(o, "Referenced method '"+o.getMethodName(cpg)+"' with expected signature not found in class '"+jc.getClassName()+"'. The native verfier does allow the method to be declared in some superclass or implemented interface, which the Java Virtual Machine Specification, Second Edition does not.");
			}
			
			JavaClass current = Repository.lookupClass(myOwner.getClassName());
			if (current.isSuper()){
			
				if ((Repository.instanceOf( current, jc )) && (!current.equals(jc))){
					
					if (! (o.getMethodName(cpg).equals(Constants.CONSTRUCTOR_NAME) )){
						// Special lookup procedure for ACC_SUPER classes.
						
						int supidx = -1;
						
						Method meth = null;
						while (supidx != 0){
							supidx = current.getSuperclassNameIndex();
							current = Repository.lookupClass(current.getSuperclassName());
							
							Method[] meths = current.getMethods();
							for (int i=0; i<meths.length; i++){
								if	( (meths[i].getName().equals(o.getMethodName(cpg))) &&
				     				(Type.getReturnType(meths[i].getSignature()).equals(o.getReturnType(cpg))) &&
				     				(objarrayequals(Type.getArgumentTypes(meths[i].getSignature()), o.getArgumentTypes(cpg))) ){
									meth = meths[i];
									break;
								}
							}
							if (meth != null) break;
						}
						if (meth == null){
							constraintViolated(o, "ACC_SUPER special lookup procedure not successful: method '"+o.getMethodName(cpg)+"' with proper signature not declared in superclass hierarchy.");
						}						
					}
				}
			}
			
			
		}
		
		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitINVOKESTATIC(INVOKESTATIC o){
			// INVOKESTATIC is a LoadClass; the Class where the referenced method is declared in,
			// is therefore resolved/verified.
			// INVOKESTATIC is an InvokeInstruction, the argument and return types are resolved/verified,
			// too. So are the allowed method names.
			String classname = o.getClassName(cpg);
			JavaClass jc = Repository.lookupClass(classname);
			Method[] ms = jc.getMethods();
			Method m = null;
			for (int i=0; i<ms.length; i++){
				if ( (ms[i].getName().equals(o.getMethodName(cpg))) &&
				     (Type.getReturnType(ms[i].getSignature()).equals(o.getReturnType(cpg))) &&
				     (objarrayequals(Type.getArgumentTypes(ms[i].getSignature()), o.getArgumentTypes(cpg))) ){
					m = ms[i];
					break;
				}
			}
			if (m == null){
				constraintViolated(o, "Referenced method '"+o.getMethodName(cpg)+"' with expected signature not found in class '"+jc.getClassName()+"'. The native verifier possibly allows the method to be declared in some superclass or implemented interface, which the Java Virtual Machine Specification, Second Edition does not.");
			}
			
			if (! (m.isStatic())){ // implies it's not abstract, verified in pass 2.
				constraintViolated(o, "Referenced method '"+o.getMethodName(cpg)+"' has ACC_STATIC unset.");
			}
		
		}


		/** Checks if the constraints of operands of the said instruction(s) are satisfied. */
		public void visitINVOKEVIRTUAL(INVOKEVIRTUAL o){
			// INVOKEVIRTUAL is a LoadClass; the Class where the referenced method is declared in,
			// is therefore resolved/verified.
			// INVOKEVIRTUAL is an InvokeInstruction, the argument and return types are resolved/verified,
			// too. So are the allowed method names.
			String classname = o.getClassName(cpg);
			JavaClass jc = Repository.lookupClass(classname);
			Method[] ms = jc.getMethods();
			Method m = null;
			for (int i=0; i<ms.length; i++){
				if ( (ms[i].getName().equals(o.getMethodName(cpg))) &&
				     (Type.getReturnType(ms[i].getSignature()).equals(o.getReturnType(cpg))) &&
				     (objarrayequals(Type.getArgumentTypes(ms[i].getSignature()), o.getArgumentTypes(cpg))) ){
					m = ms[i];
					break;
				}
			}
			if (m == null){
				constraintViolated(o, "Referenced method '"+o.getMethodName(cpg)+"' with expected signature not found in class '"+jc.getClassName()+"'. The native verfier does allow the method to be declared in some superclass or implemented interface, which the Java Virtual Machine Specification, Second Edition does not.");
			}
			if (! (jc.isClass())){
				constraintViolated(o, "Referenced class '"+jc.getClassName()+"' is an interface, but not a class as expected.");
			}
					
		}

		
		// WIDE stuff is BCEL-internal and cannot be checked here.

		/**
		 * A utility method like equals(Object) for arrays.
		 * The equality of the elements is based on their equals(Object)
		 * method instead of their object identity.
		 */ 
		private boolean objarrayequals(Object[] o, Object[] p){
			if (o.length != p.length){
				return false;
			}
			
			for (int i=0; i<o.length; i++){
				if (! (o[i].equals(p[i])) ){
					return false;
				}
			}
			
			return true;
		}

	}
}
