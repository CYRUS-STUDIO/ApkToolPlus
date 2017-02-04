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

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.classfile.Deprecated;
import org.apache.bcel.classfile.EmptyVisitor; // Use _this_ one!
import org.apache.bcel.classfile.Visitor; // Use _this_ one!
import org.apache.bcel.generic.*;
import org.apache.bcel.verifier.*;
import org.apache.bcel.verifier.exc.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This PassVerifier verifies a class file according to
 * pass 2 as described in The Java Virtual Machine
 * Specification, 2nd edition.
 * More detailed information is to be found at the do_verify()
 * method's documentation.
 *
 * @version $Id: Pass2Verifier.java,v 1.3 2006/09/04 15:43:18 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 * @see #do_verify()
 */
public final class Pass2Verifier extends PassVerifier implements Constants{

	/**
	 * The LocalVariableInfo instances used by Pass3bVerifier.
	 * localVariablesInfos[i] denotes the information for the
	 * local variables of method number i in the
	 * JavaClass this verifier operates on.
	 */
	private LocalVariablesInfo[] localVariablesInfos;
	
	/** The Verifier that created this. */
	private Verifier myOwner;

	/**
	 * Should only be instantiated by a Verifier.
	 *
	 * @see Verifier
	 */
	public Pass2Verifier(Verifier owner){
		myOwner = owner;
	}

	/**
	 * Returns a LocalVariablesInfo object containing information
	 * about the usage of the local variables in the Code attribute
	 * of the said method or <B>null</B> if the class file this
	 * Pass2Verifier operates on could not be pass-2-verified correctly.
	 * The method number method_nr is the method you get using
	 * <B>Repository.lookupClass(myOwner.getClassname()).getMethods()[method_nr];</B>.
	 * You should not add own information. Leave that to JustIce.
	 */
	public LocalVariablesInfo getLocalVariablesInfo(int method_nr){
		if (this.verify() != VerificationResult.VR_OK) return null; // It's cached, don't worry.
		if (method_nr < 0 || method_nr >= localVariablesInfos.length){
			throw new AssertionViolatedException("Method number out of range.");
		}
		return localVariablesInfos[method_nr];
	}
	
	/**
	 * Pass 2 is the pass where static properties of the
	 * class file are checked without looking into "Code"
	 * arrays of methods.
	 * This verification pass is usually invoked when
	 * a class is resolved; and it may be possible that
	 * this verification pass has to load in other classes
	 * such as superclasses or implemented interfaces.
	 * Therefore, Pass 1 is run on them.<BR>
	 * Note that most referenced classes are <B>not</B> loaded
	 * in for verification or for an existance check by this
	 * pass; only the syntactical correctness of their names
	 * and descriptors (a.k.a. signatures) is checked.<BR>
	 * Very few checks that conceptually belong here
	 * are delayed until pass 3a in JustIce. JustIce does
	 * not only check for syntactical correctness but also
	 * for semantical sanity - therefore it needs access to
	 * the "Code" array of methods in a few cases. Please
	 * see the pass 3a documentation, too.
	 *
	 * @see org.apache.bcel.verifier.statics.Pass3aVerifier
	 */
	public VerificationResult do_verify(){
		VerificationResult vr1 = myOwner.doPass1();
		if (vr1.equals(VerificationResult.VR_OK)){
			
			// For every method, we could have information about the local variables out of LocalVariableTable attributes of
			// the Code attributes.
			localVariablesInfos = new LocalVariablesInfo[Repository.lookupClass(myOwner.getClassName()).getMethods().length];

			VerificationResult vr = VerificationResult.VR_OK; // default.
			try{
				constant_pool_entries_satisfy_static_constraints();
				field_and_method_refs_are_valid();
				every_class_has_an_accessible_superclass();
				final_methods_are_not_overridden();
			}
			catch (ClassConstraintException cce){
				vr = new VerificationResult(VerificationResult.VERIFIED_REJECTED, cce.getMessage());
			}
			return vr;
		}
		else
			return VerificationResult.VR_NOTYET;
	}

	/**
	 * Ensures that every class has a super class and that
	 * <B>final</B> classes are not subclassed.
	 * This means, the class this Pass2Verifier operates
	 * on has proper super classes (transitively) up to
	 * java.lang.Object.
	 * The reason for really loading (and Pass1-verifying)
	 * all of those classes here is that we need them in
	 * Pass2 anyway to verify no final methods are overridden
	 * (that could be declared anywhere in the ancestor hierarchy).
	 *
	 * @throws ClassConstraintException otherwise.
	 */
	private void every_class_has_an_accessible_superclass(){
		HashSet<String> hs = new HashSet<String>(); // save class names to detect circular inheritance
		JavaClass jc = Repository.lookupClass(myOwner.getClassName());
		int supidx = -1;

		while (supidx != 0){
			supidx = jc.getSuperclassNameIndex();
		
			if (supidx == 0){
				if (jc != Repository.lookupClass(Type.OBJECT.getClassName())){
					throw new ClassConstraintException("Superclass of '"+jc.getClassName()+"' missing but not "+Type.OBJECT.getClassName()+" itself!");
				}
			}
			else{
				String supername = jc.getSuperclassName();
				if (! hs.add(supername)){	// If supername already is in the list
					throw new ClassConstraintException("Circular superclass hierarchy detected.");
				}
				Verifier v = VerifierFactory.getVerifier(supername);
				VerificationResult vr = v.doPass1();

				if (vr != VerificationResult.VR_OK){
					throw new ClassConstraintException("Could not load in ancestor class '"+supername+"'.");
				}
				jc = Repository.lookupClass(supername);

				if (jc.isFinal()){
					throw new ClassConstraintException("Ancestor class '"+supername+"' has the FINAL access modifier and must therefore not be subclassed.");
				}
			}
		}
	}

	/**
	 * Ensures that <B>final</B> methods are not overridden.
	 * <B>Precondition to run this method:
	 * constant_pool_entries_satisfy_static_constraints() and
	 * every_class_has_an_accessible_superclass() have to be invoked before
	 * (in that order).</B>
	 *
	 * @throws ClassConstraintException otherwise.
	 * @see #constant_pool_entries_satisfy_static_constraints()
	 * @see #every_class_has_an_accessible_superclass()
	 */
	private void final_methods_are_not_overridden(){
		HashMap<String, String> hashmap = new HashMap<String, String>();
		JavaClass jc = Repository.lookupClass(myOwner.getClassName());
		
		int supidx = -1;
		while (supidx != 0){
			supidx = jc.getSuperclassNameIndex();

			Method[] methods = jc.getMethods();
			for (int i=0; i<methods.length; i++){
				String name_and_sig = (methods[i].getName()+methods[i].getSignature());

				if (hashmap.containsKey(name_and_sig)){
					if (methods[i].isFinal()){
						throw new ClassConstraintException("Method '"+name_and_sig+"' in class '"+hashmap.get(name_and_sig)+"' overrides the final (not-overridable) definition in class '"+jc.getClassName()+"'.");
					}
					else{
						if (!methods[i].isStatic()){ // static methods don't inherit
							hashmap.put(name_and_sig, jc.getClassName());
						}
					}
				}
				else{
					if (!methods[i].isStatic()){ // static methods don't inherit
						hashmap.put(name_and_sig, jc.getClassName());
					}
				}
			}
		
			jc = Repository.lookupClass(jc.getSuperclassName());	// Well, for OBJECT this returns OBJECT so it works (could return anything but must not throw an Exception).
		}

	}

	/**
	 * Ensures that the constant pool entries satisfy the static constraints
	 * as described in The Java Virtual Machine Specification, 2nd Edition.
	 *
	 * @throws ClassConstraintException otherwise.
	 */
	private void constant_pool_entries_satisfy_static_constraints(){
		// Most of the consistency is handled internally by BCEL; here
		// we only have to verify if the indices of the constants point
		// to constants of the appropriate type and such.
		JavaClass jc = Repository.lookupClass(myOwner.getClassName());
		new CPESSC_Visitor(jc); // constructor implicitely traverses jc
	}

	/**
	 * A Visitor class that ensures the constant pool satisfies the static
	 * constraints.
   * The visitXXX() methods throw ClassConstraintException instances otherwise.
   *
   * @see #constant_pool_entries_satisfy_static_constraints()
	 */
	private class CPESSC_Visitor extends org.apache.bcel.classfile.EmptyVisitor implements Visitor{
		private Class CONST_Class;
		/*
        private Class CONST_Fieldref;
		private Class CONST_Methodref;
		private Class CONST_InterfaceMethodref;
        */
		private Class CONST_String;
		private Class CONST_Integer;
		private Class CONST_Float;
		private Class CONST_Long;
		private Class CONST_Double;
		private Class CONST_NameAndType;
		private Class CONST_Utf8;

		private final JavaClass jc;
		private final ConstantPool cp; // ==jc.getConstantPool() -- only here to save typing work and computing power.
		private final int cplen; // == cp.getLength() -- to save computing power.
		private DescendingVisitor carrier;

		private HashSet<String> field_names = new HashSet<String>();
		private HashSet<String> field_names_and_desc = new HashSet<String>();
		private HashSet<String> method_names_and_desc = new HashSet<String>();

		private CPESSC_Visitor(JavaClass _jc){
			jc = _jc;
			cp = _jc.getConstantPool();
			cplen = cp.getLength();

			CONST_Class = org.apache.bcel.classfile.ConstantClass.class;
			/*
            CONST_Fieldref = org.apache.bcel.classfile.ConstantFieldref.class;
			CONST_Methodref = org.apache.bcel.classfile.ConstantMethodref.class;
			CONST_InterfaceMethodref = org.apache.bcel.classfile.ConstantInterfaceMethodref.class;
            */
			CONST_String = org.apache.bcel.classfile.ConstantString.class;
			CONST_Integer = org.apache.bcel.classfile.ConstantInteger.class;
			CONST_Float = org.apache.bcel.classfile.ConstantFloat.class;
			CONST_Long = org.apache.bcel.classfile.ConstantLong.class;
			CONST_Double = org.apache.bcel.classfile.ConstantDouble.class;
			CONST_NameAndType = org.apache.bcel.classfile.ConstantNameAndType.class;
			CONST_Utf8 = org.apache.bcel.classfile.ConstantUtf8.class;

			carrier = new DescendingVisitor(_jc, this);
			carrier.visit();
		}

		private void checkIndex(Node referrer, int index, Class shouldbe){
			if ((index < 0) || (index >= cplen)){
				throw new ClassConstraintException("Invalid index '"+index+"' used by '"+tostring(referrer)+"'.");
			}
			Constant c = cp.getConstant(index);
			if (! shouldbe.isInstance(c)){
				/* String isnot = shouldbe.toString().substring(shouldbe.toString().lastIndexOf(".")+1); //Cut all before last "." */
				throw new ClassCastException("Illegal constant '"+tostring(c)+"' at index '"+index+"'. '"+tostring(referrer)+"' expects a '"+shouldbe+"'.");
			}
		}
		///////////////////////////////////////
		// ClassFile structure (vmspec2 4.1) //
		///////////////////////////////////////
		public void visitJavaClass(JavaClass obj){
			Attribute[] atts = obj.getAttributes();
			boolean foundSourceFile = false;
			boolean foundInnerClasses = false;

			// Is there an InnerClass referenced?
			// This is a costly check; existing verifiers don't do it!
			boolean hasInnerClass = new InnerClassDetector(jc).innerClassReferenced();

			for (int i=0; i<atts.length; i++){
				if ((! (atts[i] instanceof SourceFile)) &&
				    (! (atts[i] instanceof Deprecated))     &&
				    (! (atts[i] instanceof InnerClasses)) &&
				    (! (atts[i] instanceof Synthetic))){
					addMessage("Attribute '"+tostring(atts[i])+"' as an attribute of the ClassFile structure '"+tostring(obj)+"' is unknown and will therefore be ignored.");
				}

				if (atts[i] instanceof SourceFile){
					if (foundSourceFile == false) foundSourceFile = true;
					else throw new ClassConstraintException("A ClassFile structure (like '"+tostring(obj)+"') may have no more than one SourceFile attribute."); //vmspec2 4.7.7
				}

				if (atts[i] instanceof InnerClasses){
					if (foundInnerClasses == false) foundInnerClasses = true;
					else{
						if (hasInnerClass){
							throw new ClassConstraintException("A Classfile structure (like '"+tostring(obj)+"') must have exactly one InnerClasses attribute if at least one Inner Class is referenced (which is the case). More than one InnerClasses attribute was found.");
						}
					}
					if (!hasInnerClass){
						addMessage("No referenced Inner Class found, but InnerClasses attribute '"+tostring(atts[i])+"' found. Strongly suggest removal of that attribute.");
					}
				}

			}
			if (hasInnerClass && !foundInnerClasses){
				//throw new ClassConstraintException("A Classfile structure (like '"+tostring(obj)+"') must have exactly one InnerClasses attribute if at least one Inner Class is referenced (which is the case). No InnerClasses attribute was found.");
				//vmspec2, page 125 says it would be a constraint: but existing verifiers
				//don't check it and javac doesn't satisfy it when it comes to anonymous
				//inner classes
				addMessage("A Classfile structure (like '"+tostring(obj)+"') must have exactly one InnerClasses attribute if at least one Inner Class is referenced (which is the case). No InnerClasses attribute was found.");
			}
		}
		/////////////////////////////
		// CONSTANTS (vmspec2 4.4) //
		/////////////////////////////
		public void visitConstantClass(ConstantClass obj){
			if (obj.getTag() != Constants.CONSTANT_Class){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

		}
		public void visitConstantFieldref(ConstantFieldref obj){
			if (obj.getTag() != Constants.CONSTANT_Fieldref){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			checkIndex(obj, obj.getClassIndex(), CONST_Class);
			checkIndex(obj, obj.getNameAndTypeIndex(), CONST_NameAndType);
		}
		public void visitConstantMethodref(ConstantMethodref obj){
			if (obj.getTag() != Constants.CONSTANT_Methodref){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			checkIndex(obj, obj.getClassIndex(), CONST_Class);
			checkIndex(obj, obj.getNameAndTypeIndex(), CONST_NameAndType);
		}
		public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj){
			if (obj.getTag() != Constants.CONSTANT_InterfaceMethodref){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			checkIndex(obj, obj.getClassIndex(), CONST_Class);
			checkIndex(obj, obj.getNameAndTypeIndex(), CONST_NameAndType);
		}
		public void visitConstantString(ConstantString obj){
			if (obj.getTag() != Constants.CONSTANT_String){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			checkIndex(obj, obj.getStringIndex(), CONST_Utf8);
		}
		public void visitConstantInteger(ConstantInteger obj){
			if (obj.getTag() != Constants.CONSTANT_Integer){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			// no indices to check
		}
		public void visitConstantFloat(ConstantFloat obj){
			if (obj.getTag() != Constants.CONSTANT_Float){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			//no indices to check
		}
		public void visitConstantLong(ConstantLong obj){
			if (obj.getTag() != Constants.CONSTANT_Long){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			//no indices to check
		}
		public void visitConstantDouble(ConstantDouble obj){
			if (obj.getTag() != Constants.CONSTANT_Double){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			//no indices to check
		}
		public void visitConstantNameAndType(ConstantNameAndType obj){
			if (obj.getTag() != Constants.CONSTANT_NameAndType){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);
			//checkIndex(obj, obj.getDescriptorIndex(), CONST_Utf8); //inconsistently named in BCEL, see below.
			checkIndex(obj, obj.getSignatureIndex(), CONST_Utf8);
		}
		public void visitConstantUtf8(ConstantUtf8 obj){
			if (obj.getTag() != Constants.CONSTANT_Utf8){
				throw new ClassConstraintException("Wrong constant tag in '"+tostring(obj)+"'.");
			}
			//no indices to check
		}
		//////////////////////////
		// FIELDS (vmspec2 4.5) //
		//////////////////////////
		public void visitField(Field obj){

			if (jc.isClass()){
				int maxone=0;
				if (obj.isPrivate()) maxone++;
				if (obj.isProtected()) maxone++;
				if (obj.isPublic()) maxone++;
				if (maxone > 1){
					throw new ClassConstraintException("Field '"+tostring(obj)+"' must only have at most one of its ACC_PRIVATE, ACC_PROTECTED, ACC_PUBLIC modifiers set.");
				}

				if (obj.isFinal() && obj.isVolatile()){
					throw new ClassConstraintException("Field '"+tostring(obj)+"' must only have at most one of its ACC_FINAL, ACC_VOLATILE modifiers set.");
				}
			}
			else{ // isInterface!
				if (!obj.isPublic()){
					throw new ClassConstraintException("Interface field '"+tostring(obj)+"' must have the ACC_PUBLIC modifier set but hasn't!");
				}
				if (!obj.isStatic()){
					throw new ClassConstraintException("Interface field '"+tostring(obj)+"' must have the ACC_STATIC modifier set but hasn't!");
				}
				if (!obj.isFinal()){
					throw new ClassConstraintException("Interface field '"+tostring(obj)+"' must have the ACC_FINAL modifier set but hasn't!");
				}
			}

			if ((obj.getAccessFlags() & ~(ACC_PUBLIC|ACC_PRIVATE|ACC_PROTECTED|ACC_STATIC|ACC_FINAL|ACC_VOLATILE|ACC_TRANSIENT)) > 0){
				addMessage("Field '"+tostring(obj)+"' has access flag(s) other than ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_VOLATILE, ACC_TRANSIENT set (ignored).");
			}

			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			String name = obj.getName();
			if (! validFieldName(name)){
				throw new ClassConstraintException("Field '"+tostring(obj)+"' has illegal name '"+obj.getName()+"'.");
			}

			// A descriptor is often named signature in BCEL
			checkIndex(obj, obj.getSignatureIndex(), CONST_Utf8);

			String sig  = ((ConstantUtf8) (cp.getConstant(obj.getSignatureIndex()))).getBytes(); // Field or Method signature(=descriptor)

			try{
				Type.getType(sig);  /* Don't need the return value */
			}
			catch (ClassFormatError cfe){ // sometimes BCEL is a little harsh describing exceptional situations.
				throw new ClassConstraintException("Illegal descriptor (==signature) '"+sig+"' used by '"+tostring(obj)+"'.");
			}

			String nameanddesc = (name+sig);
			if (field_names_and_desc.contains(nameanddesc)){
				throw new ClassConstraintException("No two fields (like '"+tostring(obj)+"') are allowed have same names and descriptors!");
			}
			if (field_names.contains(name)){
				addMessage("More than one field of name '"+name+"' detected (but with different type descriptors). This is very unusual.");
			}
			field_names_and_desc.add(nameanddesc);
			field_names.add(name);

			Attribute[] atts = obj.getAttributes();
			for (int i=0; i<atts.length; i++){
				if ((! (atts[i] instanceof ConstantValue)) &&
				    (! (atts[i] instanceof Synthetic))     &&
				    (! (atts[i] instanceof Deprecated))){
					addMessage("Attribute '"+tostring(atts[i])+"' as an attribute of Field '"+tostring(obj)+"' is unknown and will therefore be ignored.");
				}
				if  (! (atts[i] instanceof ConstantValue)){
					addMessage("Attribute '"+tostring(atts[i])+"' as an attribute of Field '"+tostring(obj)+"' is not a ConstantValue and is therefore only of use for debuggers and such.");
				}
			}
		}
		///////////////////////////
		// METHODS (vmspec2 4.6) //
		///////////////////////////
		public void visitMethod(Method obj){

			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			String name = obj.getName();
			if (! validMethodName(name, true)){
				throw new ClassConstraintException("Method '"+tostring(obj)+"' has illegal name '"+name+"'.");
			}

			// A descriptor is often named signature in BCEL
			checkIndex(obj, obj.getSignatureIndex(), CONST_Utf8);

			String sig  = ((ConstantUtf8) (cp.getConstant(obj.getSignatureIndex()))).getBytes(); // Method's signature(=descriptor)

			Type t;
			Type[] ts; // needed below the try block.
			try{
				t  = Type.getReturnType(sig);
				ts = Type.getArgumentTypes(sig);
			}
			catch (ClassFormatError cfe){
				// Well, BCEL sometimes is a little harsh describing exceptional situations.
				throw new ClassConstraintException("Illegal descriptor (==signature) '"+sig+"' used by Method '"+tostring(obj)+"'.");
			}

			// Check if referenced objects exist.
			Type act = t;
			if (act instanceof ArrayType) act = ((ArrayType) act).getBasicType();
			if (act instanceof ObjectType){
				Verifier v = VerifierFactory.getVerifier( ((ObjectType) act).getClassName() );
				VerificationResult vr = v.doPass1();
				if (vr != VerificationResult.VR_OK) {
					throw new ClassConstraintException("Method '"+tostring(obj)+"' has a return type that does not pass verification pass 1: '"+vr+"'.");
				}
			}

			for (int i=0; i<ts.length; i++){
				act = ts[i];
				if (act instanceof ArrayType) act = ((ArrayType) act).getBasicType();
				if (act instanceof ObjectType){
					Verifier v = VerifierFactory.getVerifier( ((ObjectType) act).getClassName() );
					VerificationResult vr = v.doPass1();
					if (vr != VerificationResult.VR_OK) {
						throw new ClassConstraintException("Method '"+tostring(obj)+"' has an argument type that does not pass verification pass 1: '"+vr+"'.");
					}
				}
			}

			// Nearly forgot this! Funny return values are allowed, but a non-empty arguments list makes a different method out of it!
			if (name.equals(STATIC_INITIALIZER_NAME) && (ts.length != 0)){
				throw new ClassConstraintException("Method '"+tostring(obj)+"' has illegal name '"+name+"'. It's name resembles the class or interface initialization method which it isn't because of its arguments (==descriptor).");
			}

			if (jc.isClass()){
				int maxone=0;
				if (obj.isPrivate()) maxone++;
				if (obj.isProtected()) maxone++;
				if (obj.isPublic()) maxone++;
				if (maxone > 1){
					throw new ClassConstraintException("Method '"+tostring(obj)+"' must only have at most one of its ACC_PRIVATE, ACC_PROTECTED, ACC_PUBLIC modifiers set.");
				}

				if (obj.isAbstract()){
					if (obj.isFinal()) throw new ClassConstraintException("Abstract method '"+tostring(obj)+"' must not have the ACC_FINAL modifier set.");
					if (obj.isNative()) throw new ClassConstraintException("Abstract method '"+tostring(obj)+"' must not have the ACC_NATIVE modifier set.");
					if (obj.isPrivate()) throw new ClassConstraintException("Abstract method '"+tostring(obj)+"' must not have the ACC_PRIVATE modifier set.");
					if (obj.isStatic()) throw new ClassConstraintException("Abstract method '"+tostring(obj)+"' must not have the ACC_STATIC modifier set.");
					if (obj.isStrictfp()) throw new ClassConstraintException("Abstract method '"+tostring(obj)+"' must not have the ACC_STRICT modifier set.");
					if (obj.isSynchronized()) throw new ClassConstraintException("Abstract method '"+tostring(obj)+"' must not have the ACC_SYNCHRONIZED modifier set.");
				}
			}
			else{ // isInterface!
				if (!name.equals(STATIC_INITIALIZER_NAME)){//vmspec2, p.116, 2nd paragraph
					if (!obj.isPublic()){
						throw new ClassConstraintException("Interface method '"+tostring(obj)+"' must have the ACC_PUBLIC modifier set but hasn't!");
					}
					if (!obj.isAbstract()){
						throw new ClassConstraintException("Interface method '"+tostring(obj)+"' must have the ACC_STATIC modifier set but hasn't!");
					}
					if (	obj.isPrivate() ||
								obj.isProtected() ||
								obj.isStatic() ||
								obj.isFinal() ||
								obj.isSynchronized() ||
								obj.isNative() ||
								obj.isStrictfp() ){
						throw new ClassConstraintException("Interface method '"+tostring(obj)+"' must not have any of the ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_SYNCHRONIZED, ACC_NATIVE, ACC_ABSTRACT, ACC_STRICT modifiers set.");
					}
				}
			}

			// A specific instance initialization method... (vmspec2,Page 116).
			if (name.equals(CONSTRUCTOR_NAME)){
				//..may have at most one of ACC_PRIVATE, ACC_PROTECTED, ACC_PUBLIC set: is checked above.
				//..may also have ACC_STRICT set, but none of the other flags in table 4.5 (vmspec2, page 115)
				if (	obj.isStatic() ||
							obj.isFinal() ||
							obj.isSynchronized() ||
							obj.isNative() ||
							obj.isAbstract() ){
					throw new ClassConstraintException("Instance initialization method '"+tostring(obj)+"' must not have any of the ACC_STATIC, ACC_FINAL, ACC_SYNCHRONIZED, ACC_NATIVE, ACC_ABSTRACT modifiers set.");
				}
			}

			// Class and interface initialization methods...
			if (name.equals(STATIC_INITIALIZER_NAME)){
				if ((obj.getAccessFlags() & (~ACC_STRICT)) > 0){
					addMessage("Class or interface initialization method '"+tostring(obj)+"' has superfluous access modifier(s) set: everything but ACC_STRICT is ignored.");
				}
				if (obj.isAbstract()){
					throw new ClassConstraintException("Class or interface initialization method '"+tostring(obj)+"' must not be abstract. This contradicts the Java Language Specification, Second Edition (which omits this constraint) but is common practice of existing verifiers.");
				}
			}

			if ((obj.getAccessFlags() & ~(ACC_PUBLIC|ACC_PRIVATE|ACC_PROTECTED|ACC_STATIC|ACC_FINAL|ACC_SYNCHRONIZED|ACC_NATIVE|ACC_ABSTRACT|ACC_STRICT)) > 0){
				addMessage("Method '"+tostring(obj)+"' has access flag(s) other than ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_SYNCHRONIZED, ACC_NATIVE, ACC_ABSTRACT, ACC_STRICT set (ignored).");
			}

			String nameanddesc = (name+sig);
			if (method_names_and_desc.contains(nameanddesc)){
				throw new ClassConstraintException("No two methods (like '"+tostring(obj)+"') are allowed have same names and desciptors!");
			}
			method_names_and_desc.add(nameanddesc);

			Attribute[] atts = obj.getAttributes();
			int num_code_atts = 0;
			for (int i=0; i<atts.length; i++){
				if ((! (atts[i] instanceof Code)) &&
				    (! (atts[i] instanceof ExceptionTable))     &&
				    (! (atts[i] instanceof Synthetic)) &&
				    (! (atts[i] instanceof Deprecated))){
					addMessage("Attribute '"+tostring(atts[i])+"' as an attribute of Method '"+tostring(obj)+"' is unknown and will therefore be ignored.");
				}
				if ((! (atts[i] instanceof Code)) &&
						(! (atts[i] instanceof ExceptionTable))){
					addMessage("Attribute '"+tostring(atts[i])+"' as an attribute of Method '"+tostring(obj)+"' is neither Code nor Exceptions and is therefore only of use for debuggers and such.");
				}
				if ((atts[i] instanceof Code) && (obj.isNative() || obj.isAbstract())){
					throw new ClassConstraintException("Native or abstract methods like '"+tostring(obj)+"' must not have a Code attribute like '"+tostring(atts[i])+"'."); //vmspec2 page120, 4.7.3
				}
				if (atts[i] instanceof Code) num_code_atts++;
			}
			if ( !obj.isNative() && !obj.isAbstract() && num_code_atts != 1){
				throw new ClassConstraintException("Non-native, non-abstract methods like '"+tostring(obj)+"' must have exactly one Code attribute (found: "+num_code_atts+").");
			}
		}
		///////////////////////////////////////////////////////
		// ClassFile-structure-ATTRIBUTES (vmspec2 4.1, 4.7) //
		///////////////////////////////////////////////////////
		public void visitSourceFile(SourceFile obj){//vmspec2 4.7.7

			// zero or one SourceFile attr per ClassFile: see visitJavaClass()

			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			String name = ((ConstantUtf8) cp.getConstant(obj.getNameIndex())).getBytes();
			if (! name.equals("SourceFile")){
				throw new ClassConstraintException("The SourceFile attribute '"+tostring(obj)+"' is not correctly named 'SourceFile' but '"+name+"'.");
			}

			checkIndex(obj, obj.getSourceFileIndex(), CONST_Utf8);

			String sourcefilename = ((ConstantUtf8) cp.getConstant(obj.getSourceFileIndex())).getBytes(); //==obj.getSourceFileName() ?
			String sourcefilenamelc = sourcefilename.toLowerCase();

			if (	(sourcefilename.indexOf('/') != -1) ||
						(sourcefilename.indexOf('\\') != -1) ||
						(sourcefilename.indexOf(':') != -1) ||
						(sourcefilenamelc.lastIndexOf(".java") == -1)	){
				addMessage("SourceFile attribute '"+tostring(obj)+"' has a funny name: remember not to confuse certain parsers working on javap's output. Also, this name ('"+sourcefilename+"') is considered an unqualified (simple) file name only.");
			}
		}
		public void visitDeprecated(Deprecated obj){//vmspec2 4.7.10
			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			String name = ((ConstantUtf8) cp.getConstant(obj.getNameIndex())).getBytes();
			if (! name.equals("Deprecated")){
				throw new ClassConstraintException("The Deprecated attribute '"+tostring(obj)+"' is not correctly named 'Deprecated' but '"+name+"'.");
			}
		}
		public void visitSynthetic(Synthetic obj){//vmspec2 4.7.6
			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);
			String name = ((ConstantUtf8) cp.getConstant(obj.getNameIndex())).getBytes();
			if (! name.equals("Synthetic")){
				throw new ClassConstraintException("The Synthetic attribute '"+tostring(obj)+"' is not correctly named 'Synthetic' but '"+name+"'.");
			}
		}
		public void visitInnerClasses(InnerClasses obj){//vmspec2 4.7.5

			// exactly one InnerClasses attr per ClassFile if some inner class is refernced: see visitJavaClass()

			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			String name = ((ConstantUtf8) cp.getConstant(obj.getNameIndex())).getBytes();
			if (! name.equals("InnerClasses")){
				throw new ClassConstraintException("The InnerClasses attribute '"+tostring(obj)+"' is not correctly named 'InnerClasses' but '"+name+"'.");
			}

			InnerClass[] ics = obj.getInnerClasses();

			for (int i=0; i<ics.length; i++){
				checkIndex(obj, ics[i].getInnerClassIndex(), CONST_Class);
				int outer_idx = ics[i].getOuterClassIndex();
				if (outer_idx != 0){
					checkIndex(obj, outer_idx, CONST_Class);
				}
				int innername_idx = ics[i].getInnerNameIndex();
				if (innername_idx != 0){
					checkIndex(obj, innername_idx, CONST_Utf8);
				}
				int acc = ics[i].getInnerAccessFlags();
				acc = acc & (~ (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC | ACC_FINAL | ACC_INTERFACE | ACC_ABSTRACT));
				if (acc != 0){
					addMessage("Unknown access flag for inner class '"+tostring(ics[i])+"' set (InnerClasses attribute '"+tostring(obj)+"').");
				}
			}
			// Semantical consistency is not yet checked by Sun, see vmspec2 4.7.5.
			// [marked TODO in JustIce]
		}
		////////////////////////////////////////////////////////
		// field_info-structure-ATTRIBUTES (vmspec2 4.5, 4.7) //
		////////////////////////////////////////////////////////
		public void visitConstantValue(ConstantValue obj){//vmspec2 4.7.2
			// Despite its name, this really is an Attribute,
			// not a constant!
			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			String name = ((ConstantUtf8) cp.getConstant(obj.getNameIndex())).getBytes();
			if (! name.equals("ConstantValue")){
				throw new ClassConstraintException("The ConstantValue attribute '"+tostring(obj)+"' is not correctly named 'ConstantValue' but '"+name+"'.");
			}

			Object pred = carrier.predecessor();
			if (pred instanceof Field){ //ConstantValue attributes are quite senseless if the predecessor is not a field.
				Field f = (Field) pred;
				// Field constraints have been checked before -- so we are safe using their type information.
				Type field_type = Type.getType(((ConstantUtf8) (cp.getConstant(f.getSignatureIndex()))).getBytes());

				int index = obj.getConstantValueIndex();
				if ((index < 0) || (index >= cplen)){
					throw new ClassConstraintException("Invalid index '"+index+"' used by '"+tostring(obj)+"'.");
				}
				Constant c = cp.getConstant(index);

				if (CONST_Long.isInstance(c) && field_type.equals(Type.LONG)){
					return;
				}
				if (CONST_Float.isInstance(c) && field_type.equals(Type.FLOAT)){
					return;
				}
				if (CONST_Double.isInstance(c) && field_type.equals(Type.DOUBLE)){
					return;
				}
				if (CONST_Integer.isInstance(c) && (field_type.equals(Type.INT) || field_type.equals(Type.SHORT) || field_type.equals(Type.CHAR) || field_type.equals(Type.BYTE) || field_type.equals(Type.BOOLEAN))){
					return;
				}
				if (CONST_String.isInstance(c) && field_type.equals(Type.STRING)){
					return;
				}

				throw new ClassConstraintException("Illegal type of ConstantValue '"+obj+"' embedding Constant '"+c+"'. It is referenced by field '"+tostring(f)+"' expecting a different type: '"+field_type+"'.");
			}
		}
		// SYNTHETIC: see above
		// DEPRECATED: see above
		/////////////////////////////////////////////////////////
		// method_info-structure-ATTRIBUTES (vmspec2 4.6, 4.7) //
		/////////////////////////////////////////////////////////
		public void visitCode(Code obj){//vmspec2 4.7.3
			// No code attribute allowed for native or abstract methods: see visitMethod(Method).
			// Code array constraints are checked in Pass3 (3a and 3b).

			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			String name = ((ConstantUtf8) cp.getConstant(obj.getNameIndex())).getBytes();
			if (! name.equals("Code")){
				throw new ClassConstraintException("The Code attribute '"+tostring(obj)+"' is not correctly named 'Code' but '"+name+"'.");
			}

			Method m = null; // satisfy compiler
			if (!(carrier.predecessor() instanceof Method)){
				addMessage("Code attribute '"+tostring(obj)+"' is not declared in a method_info structure but in '"+carrier.predecessor()+"'. Ignored.");
				return;
			}
			else{
				m = (Method) carrier.predecessor();	// we can assume this method was visited before;
																						// i.e. the data consistency was verified.
			}

			if (obj.getCode().length == 0){
				throw new ClassConstraintException("Code array of Code attribute '"+tostring(obj)+"' (method '"+m+"') must not be empty.");
			}

			//In JustIce, the check for correct offsets into the code array is delayed to Pass 3a.
			CodeException[] exc_table = obj.getExceptionTable();
			for (int i=0; i<exc_table.length; i++){
				int exc_index = exc_table[i].getCatchType();
				if (exc_index != 0){ // if 0, it catches all Throwables
					checkIndex(obj, exc_index, CONST_Class);
					ConstantClass cc = (ConstantClass) (cp.getConstant(exc_index));
					checkIndex(cc, cc.getNameIndex(), CONST_Utf8); // cannot be sure this ConstantClass has already been visited (checked)!
					String cname = ((ConstantUtf8) cp.getConstant(cc.getNameIndex())).getBytes().replace('/','.');

					Verifier v = VerifierFactory.getVerifier(cname);
					VerificationResult vr = v.doPass1();

					if (vr != VerificationResult.VR_OK){
						throw new ClassConstraintException("Code attribute '"+tostring(obj)+"' (method '"+m+"') has an exception_table entry '"+tostring(exc_table[i])+"' that references '"+cname+"' as an Exception but it does not pass verification pass 1: "+vr);
					}
					else{
						// We cannot safely trust any other "instanceof" mechanism. We need to transitively verify
						// the ancestor hierarchy.
						JavaClass e = Repository.lookupClass(cname);
						JavaClass t = Repository.lookupClass(Type.THROWABLE.getClassName());
						JavaClass o = Repository.lookupClass(Type.OBJECT.getClassName());
						while (e != o){
							if (e == t) break; // It's a subclass of Throwable, OKAY, leave.

							v = VerifierFactory.getVerifier(e.getSuperclassName());
							vr = v.doPass1();
							if (vr != VerificationResult.VR_OK){
								throw new ClassConstraintException("Code attribute '"+tostring(obj)+"' (method '"+m+"') has an exception_table entry '"+tostring(exc_table[i])+"' that references '"+cname+"' as an Exception but '"+e.getSuperclassName()+"' in the ancestor hierachy does not pass verification pass 1: "+vr);
							}
							else{
								e = Repository.lookupClass(e.getSuperclassName());
							}
						}
						if (e != t) throw new ClassConstraintException("Code attribute '"+tostring(obj)+"' (method '"+m+"') has an exception_table entry '"+tostring(exc_table[i])+"' that references '"+cname+"' as an Exception but it is not a subclass of '"+t.getClassName()+"'.");
					}
				}
			}

			// Create object for local variables information
			// This is highly unelegant due to usage of the Visitor pattern.
			// TODO: rework it.
			int method_number = -1;
			Method[] ms = Repository.lookupClass(myOwner.getClassName()).getMethods();
			for (int mn=0; mn<ms.length; mn++){
				if (m == ms[mn]){
					method_number = mn;
					break;
				}
			}
			if (method_number < 0){ // Mmmmh. Can we be sure BCEL does not sometimes instantiate new objects?
				throw new AssertionViolatedException("Could not find a known BCEL Method object in the corresponding BCEL JavaClass object.");
			}
			localVariablesInfos[method_number] = new LocalVariablesInfo(obj.getMaxLocals());

			int num_of_lvt_attribs = 0;
			// Now iterate through the attributes the Code attribute has.
			Attribute[] atts = obj.getAttributes();
			for (int a=0; a<atts.length; a++){
				if ((! (atts[a] instanceof LineNumberTable)) &&
				    (! (atts[a] instanceof LocalVariableTable))){
					addMessage("Attribute '"+tostring(atts[a])+"' as an attribute of Code attribute '"+tostring(obj)+"' (method '"+m+"') is unknown and will therefore be ignored.");
				}
				else{// LineNumberTable or LocalVariableTable
					addMessage("Attribute '"+tostring(atts[a])+"' as an attribute of Code attribute '"+tostring(obj)+"' (method '"+m+"') will effectively be ignored and is only useful for debuggers and such.");
				}

				//LocalVariableTable check (partially delayed to Pass3a).
				//Here because its easier to collect the information of the
				//(possibly more than one) LocalVariableTables belonging to
				//one certain Code attribute.
				if (atts[a] instanceof LocalVariableTable){ // checks conforming to vmspec2 4.7.9

					LocalVariableTable lvt = (LocalVariableTable) atts[a];

					checkIndex(lvt, lvt.getNameIndex(), CONST_Utf8);

					String lvtname = ((ConstantUtf8) cp.getConstant(lvt.getNameIndex())).getBytes();
					if (! lvtname.equals("LocalVariableTable")){
						throw new ClassConstraintException("The LocalVariableTable attribute '"+tostring(lvt)+"' is not correctly named 'LocalVariableTable' but '"+lvtname+"'.");
					}

					Code code = obj;

					//In JustIce, the check for correct offsets into the code array is delayed to Pass 3a.
					LocalVariable[] localvariables = lvt.getLocalVariableTable();

					for (int i=0; i<localvariables.length; i++){
						checkIndex(lvt, localvariables[i].getNameIndex(), CONST_Utf8);
						String localname = ((ConstantUtf8) cp.getConstant(localvariables[i].getNameIndex())).getBytes();
						if (!validJavaIdentifier(localname)){
							throw new ClassConstraintException("LocalVariableTable '"+tostring(lvt)+"' references a local variable by the name '"+localname+"' which is not a legal Java simple name.");
						}

						checkIndex(lvt, localvariables[i].getSignatureIndex(), CONST_Utf8);
						String localsig  = ((ConstantUtf8) (cp.getConstant(localvariables[i].getSignatureIndex()))).getBytes(); // Local signature(=descriptor)
						Type t;
						try{
							t = Type.getType(localsig);
						}
						catch (ClassFormatError cfe){ // sometimes BCEL is a little harsh describing exceptional situations.
							throw new ClassConstraintException("Illegal descriptor (==signature) '"+localsig+"' used by LocalVariable '"+tostring(localvariables[i])+"' referenced by '"+tostring(lvt)+"'.");
						}
						int localindex = localvariables[i].getIndex();
						if ( ( (t==Type.LONG || t==Type.DOUBLE)? localindex+1:localindex) >= code.getMaxLocals()){
							throw new ClassConstraintException("LocalVariableTable attribute '"+tostring(lvt)+"' references a LocalVariable '"+tostring(localvariables[i])+"' with an index that exceeds the surrounding Code attribute's max_locals value of '"+code.getMaxLocals()+"'.");
						}

						try{
							localVariablesInfos[method_number].add(localindex, localname, localvariables[i].getStartPC(), localvariables[i].getLength(), t);
						}
						catch(LocalVariableInfoInconsistentException lviie){
							throw new ClassConstraintException("Conflicting information in LocalVariableTable '"+tostring(lvt)+"' found in Code attribute '"+tostring(obj)+"' (method '"+tostring(m)+"'). "+lviie.getMessage());
						}
					}// for all local variables localvariables[i] in the LocalVariableTable attribute atts[a] END

					num_of_lvt_attribs++;
					if (num_of_lvt_attribs > obj.getMaxLocals()){
						throw new ClassConstraintException("Number of LocalVariableTable attributes of Code attribute '"+tostring(obj)+"' (method '"+tostring(m)+"') exceeds number of local variable slots '"+obj.getMaxLocals()+"' ('There may be no more than one LocalVariableTable attribute per local variable in the Code attribute.').");
					}
				}// if atts[a] instanceof LocalVariableTable END
			}// for all attributes atts[a] END
		}// visitCode(Code) END

		public void visitExceptionTable(ExceptionTable obj){//vmspec2 4.7.4
			// incorrectly named, it's the Exceptions attribute (vmspec2 4.7.4)
			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			String name = ((ConstantUtf8) cp.getConstant(obj.getNameIndex())).getBytes();
			if (! name.equals("Exceptions")){
				throw new ClassConstraintException("The Exceptions attribute '"+tostring(obj)+"' is not correctly named 'Exceptions' but '"+name+"'.");
			}

			int[] exc_indices = obj.getExceptionIndexTable();

			for (int i=0; i<exc_indices.length; i++){
				checkIndex(obj, exc_indices[i], CONST_Class);

				ConstantClass cc = (ConstantClass) (cp.getConstant(exc_indices[i]));
				checkIndex(cc, cc.getNameIndex(), CONST_Utf8); // cannot be sure this ConstantClass has already been visited (checked)!
				String cname = ((ConstantUtf8) cp.getConstant(cc.getNameIndex())).getBytes().replace('/','.'); //convert internal notation on-the-fly to external notation

				Verifier v = VerifierFactory.getVerifier(cname);
				VerificationResult vr = v.doPass1();

				if (vr != VerificationResult.VR_OK){
					throw new ClassConstraintException("Exceptions attribute '"+tostring(obj)+"' references '"+cname+"' as an Exception but it does not pass verification pass 1: "+vr);
				}
				else{
					// We cannot safely trust any other "instanceof" mechanism. We need to transitively verify
					// the ancestor hierarchy.
					JavaClass e = Repository.lookupClass(cname);
					JavaClass t = Repository.lookupClass(Type.THROWABLE.getClassName());
					JavaClass o = Repository.lookupClass(Type.OBJECT.getClassName());
					while (e != o){
						if (e == t) break; // It's a subclass of Throwable, OKAY, leave.

						v = VerifierFactory.getVerifier(e.getSuperclassName());
						vr = v.doPass1();
						if (vr != VerificationResult.VR_OK){
							throw new ClassConstraintException("Exceptions attribute '"+tostring(obj)+"' references '"+cname+"' as an Exception but '"+e.getSuperclassName()+"' in the ancestor hierachy does not pass verification pass 1: "+vr);
						}
						else{
							e = Repository.lookupClass(e.getSuperclassName());
						}
					}
					if (e != t) throw new ClassConstraintException("Exceptions attribute '"+tostring(obj)+"' references '"+cname+"' as an Exception but it is not a subclass of '"+t.getClassName()+"'.");
				}
			}
		}
		// SYNTHETIC: see above
		// DEPRECATED: see above
		//////////////////////////////////////////////////////////////
		// code_attribute-structure-ATTRIBUTES (vmspec2 4.7.3, 4.7) //
		//////////////////////////////////////////////////////////////
		public void visitLineNumberTable(LineNumberTable obj){//vmspec2 4.7.8
			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			String name = ((ConstantUtf8) cp.getConstant(obj.getNameIndex())).getBytes();
			if (! name.equals("LineNumberTable")){
				throw new ClassConstraintException("The LineNumberTable attribute '"+tostring(obj)+"' is not correctly named 'LineNumberTable' but '"+name+"'.");
			}

			//In JustIce,this check is delayed to Pass 3a.
			//LineNumber[] linenumbers = obj.getLineNumberTable();
			// ...validity check...

		}
		public void visitLocalVariableTable(LocalVariableTable obj){//vmspec2 4.7.9
			//In JustIce,this check is partially delayed to Pass 3a.
			//The other part can be found in the visitCode(Code) method.
		}
		////////////////////////////////////////////////////
		// MISC-structure-ATTRIBUTES (vmspec2 4.7.1, 4.7) //
		////////////////////////////////////////////////////
		public void visitUnknown(Unknown obj){//vmspec2 4.7.1
			// Represents an unknown attribute.
			checkIndex(obj, obj.getNameIndex(), CONST_Utf8);

			// Maybe only misnamed? Give a (warning) message.
			addMessage("Unknown attribute '"+tostring(obj)+"'. This attribute is not known in any context!");
		}
		//////////
		// BCEL //
		//////////
		public void visitLocalVariable(LocalVariable obj){
			// This does not represent an Attribute but is only
			// related to internal BCEL data representation.

			// see visitLocalVariableTable(LocalVariableTable)
		}
		public void visitCodeException(CodeException obj){
			// Code constraints are checked in Pass3 (3a and 3b).
			// This does not represent an Attribute but is only
			// related to internal BCEL data representation.

			// see visitCode(Code)
		}
		public void visitConstantPool(ConstantPool obj){
			// No need to. We're piggybacked by the DescendingVisitor.
			// This does not represent an Attribute but is only
			// related to internal BCEL data representation.
		}
		public void visitInnerClass(InnerClass obj){
			// This does not represent an Attribute but is only
			// related to internal BCEL data representation.
		}
		public void visitLineNumber(LineNumber obj){
			// This does not represent an Attribute but is only
			// related to internal BCEL data representation.

			// see visitLineNumberTable(LineNumberTable)
		}
	}

	/**
	 * Ensures that the ConstantCP-subclassed entries of the constant
	 * pool are valid. According to "Yellin: Low Level Security in Java",
	 * this method does not verify the existence of referenced entities
	 * (such as classes) but only the formal correctness (such as well-formed
	 * signatures).
   * The visitXXX() methods throw ClassConstraintException instances otherwise.
	 * <B>Precondition: index-style cross referencing in the constant
	 * pool must be valid. Simply invoke constant_pool_entries_satisfy_static_constraints()
	 * before.</B>
	 *
	 * @throws ClassConstraintException otherwise.
	 * @see #constant_pool_entries_satisfy_static_constraints()
	 */
	private void field_and_method_refs_are_valid(){
		JavaClass jc = Repository.lookupClass(myOwner.getClassName());
		DescendingVisitor v = new DescendingVisitor(jc, new FAMRAV_Visitor(jc));
		v.visit();
	}

	/**
	 * A Visitor class that ensures the ConstantCP-subclassed entries
	 * of the constant pool are valid.
   * <B>Precondition: index-style cross referencing in the constant
   * pool must be valid.</B>
	 *
   * @see #constant_pool_entries_satisfy_static_constraints()
	 * @see org.apache.bcel.classfile.ConstantCP
	 */
	private class FAMRAV_Visitor extends EmptyVisitor implements Visitor{
		private final ConstantPool cp; // ==jc.getConstantPool() -- only here to save typing work.
		private FAMRAV_Visitor(JavaClass _jc){
			cp = _jc.getConstantPool();
		}
		
		public void visitConstantFieldref(ConstantFieldref obj){
			if (obj.getTag() != Constants.CONSTANT_Fieldref){
				throw new ClassConstraintException("ConstantFieldref '"+tostring(obj)+"' has wrong tag!");
			}
			int name_and_type_index = obj.getNameAndTypeIndex();
			ConstantNameAndType cnat = (ConstantNameAndType) (cp.getConstant(name_and_type_index));
			String name = ((ConstantUtf8) (cp.getConstant(cnat.getNameIndex()))).getBytes(); // Field or Method name
			if (!validFieldName(name)){
				throw new ClassConstraintException("Invalid field name '"+name+"' referenced by '"+tostring(obj)+"'.");
			}
			
			int class_index = obj.getClassIndex();
			ConstantClass cc = (ConstantClass) (cp.getConstant(class_index));
			String className = ((ConstantUtf8) (cp.getConstant(cc.getNameIndex()))).getBytes(); // Class Name in internal form
			if (! validClassName(className)){
				throw new ClassConstraintException("Illegal class name '"+className+"' used by '"+tostring(obj)+"'.");
			}

			String sig  = ((ConstantUtf8) (cp.getConstant(cnat.getSignatureIndex()))).getBytes(); // Field or Method signature(=descriptor)
						
			try{
				Type.getType(sig); /* Don't need the return value */
			}
			catch (ClassFormatError cfe){
				// Well, BCEL sometimes is a little harsh describing exceptional situations.
				throw new ClassConstraintException("Illegal descriptor (==signature) '"+sig+"' used by '"+tostring(obj)+"'.");
			}
		}

		public void visitConstantMethodref(ConstantMethodref obj){
			if (obj.getTag() != Constants.CONSTANT_Methodref){
				throw new ClassConstraintException("ConstantMethodref '"+tostring(obj)+"' has wrong tag!");
			}
			int name_and_type_index = obj.getNameAndTypeIndex();
			ConstantNameAndType cnat = (ConstantNameAndType) (cp.getConstant(name_and_type_index));
			String name = ((ConstantUtf8) (cp.getConstant(cnat.getNameIndex()))).getBytes(); // Field or Method name
			if (!validClassMethodName(name)){
				throw new ClassConstraintException("Invalid (non-interface) method name '"+name+"' referenced by '"+tostring(obj)+"'.");
			}

			int class_index = obj.getClassIndex();
			ConstantClass cc = (ConstantClass) (cp.getConstant(class_index));
			String className = ((ConstantUtf8) (cp.getConstant(cc.getNameIndex()))).getBytes(); // Class Name in internal form
			if (! validClassName(className)){
				throw new ClassConstraintException("Illegal class name '"+className+"' used by '"+tostring(obj)+"'.");
			}

			String sig  = ((ConstantUtf8) (cp.getConstant(cnat.getSignatureIndex()))).getBytes(); // Field or Method signature(=descriptor)
						
			try{
				Type   t  = Type.getReturnType(sig);
				if ( name.equals(CONSTRUCTOR_NAME) && (t != Type.VOID) ){
					throw new ClassConstraintException("Instance initialization method must have VOID return type.");
				}
			}
			catch (ClassFormatError cfe){
				// Well, BCEL sometimes is a little harsh describing exceptional situations.
				throw new ClassConstraintException("Illegal descriptor (==signature) '"+sig+"' used by '"+tostring(obj)+"'.");
			}
		}

		public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj){
			if (obj.getTag() != Constants.CONSTANT_InterfaceMethodref){
				throw new ClassConstraintException("ConstantInterfaceMethodref '"+tostring(obj)+"' has wrong tag!");
			}
			int name_and_type_index = obj.getNameAndTypeIndex();
			ConstantNameAndType cnat = (ConstantNameAndType) (cp.getConstant(name_and_type_index));
			String name = ((ConstantUtf8) (cp.getConstant(cnat.getNameIndex()))).getBytes(); // Field or Method name
			if (!validInterfaceMethodName(name)){
				throw new ClassConstraintException("Invalid (interface) method name '"+name+"' referenced by '"+tostring(obj)+"'.");
			}

			int class_index = obj.getClassIndex();
			ConstantClass cc = (ConstantClass) (cp.getConstant(class_index));
			String className = ((ConstantUtf8) (cp.getConstant(cc.getNameIndex()))).getBytes(); // Class Name in internal form
			if (! validClassName(className)){
				throw new ClassConstraintException("Illegal class name '"+className+"' used by '"+tostring(obj)+"'.");
			}

			String sig  = ((ConstantUtf8) (cp.getConstant(cnat.getSignatureIndex()))).getBytes(); // Field or Method signature(=descriptor)
						
			try{
				Type   t  = Type.getReturnType(sig);
				if ( name.equals(STATIC_INITIALIZER_NAME) && (t != Type.VOID) ){
					addMessage("Class or interface initialization method '"+STATIC_INITIALIZER_NAME+"' usually has VOID return type instead of '"+t+"'. Note this is really not a requirement of The Java Virtual Machine Specification, Second Edition.");
				}
			}
			catch (ClassFormatError cfe){
				// Well, BCEL sometimes is a little harsh describing exceptional situations.
				throw new ClassConstraintException("Illegal descriptor (==signature) '"+sig+"' used by '"+tostring(obj)+"'.");
			}

		}
		
	}

	/**
	 * This method returns true if and only if the supplied String
	 * represents a valid Java class name.
	 */
	private static final boolean validClassName(String name){
        /*
         * TODO: implement.
		 * Are there any restrictions?
         */
		return true;
	}
	/**
	 * This method returns true if and only if the supplied String
	 * represents a valid method name.
	 * This is basically the same as a valid identifier name in the
	 * Java programming language, but the special name for
	 * the instance initialization method is allowed and the special name
	 * for the class/interface initialization method may be allowed.
	 */
	private static boolean validMethodName(String name, boolean allowStaticInit){
		if (validJavaLangMethodName(name)) return true;
		
		if (allowStaticInit){
			return (name.equals(CONSTRUCTOR_NAME) || name.equals(STATIC_INITIALIZER_NAME));
		}
		else{
			return name.equals(CONSTRUCTOR_NAME);
		}
	}

	/**
	 * This method returns true if and only if the supplied String
	 * represents a valid method name that may be referenced by
	 * ConstantMethodref objects.
	 */
	private static boolean validClassMethodName(String name){
		return validMethodName(name, false);
	}

	/**
	 * This method returns true if and only if the supplied String
	 * represents a valid Java programming language method name stored as a simple
	 * (non-qualified) name.
	 * Conforming to: The Java Virtual Machine Specification, Second Edition, §2.7, §2.7.1, §2.2.
	 */
	private static boolean validJavaLangMethodName(String name){
		if (!Character.isJavaIdentifierStart(name.charAt(0))) return false;
		
		for (int i=1; i<name.length(); i++){
			if (!Character.isJavaIdentifierPart(name.charAt(i))) return false;
		}
		return true;
	}

	/**
	 * This method returns true if and only if the supplied String
	 * represents a valid Java interface method name that may be
	 * referenced by ConstantInterfaceMethodref objects.
	 */
	private static boolean validInterfaceMethodName(String name){
		// I guess we should assume special names forbidden here.
		if (name.startsWith("<")) return false;
		return validJavaLangMethodName(name);
	}

	/**
	 * This method returns true if and only if the supplied String
	 * represents a valid Java identifier (so-called simple name).
	 */
	private static boolean validJavaIdentifier(String name){
		// vmspec2 2.7, vmspec2 2.2
		if (!Character.isJavaIdentifierStart(name.charAt(0))) return false;
		
		for (int i=1; i<name.length(); i++){
			if (!Character.isJavaIdentifierPart(name.charAt(i))) return false;
		}
		return true;
	}

	/**
	 * This method returns true if and only if the supplied String
	 * represents a valid Java field name.
	 */
	private static boolean validFieldName(String name){
		// vmspec2 2.7, vmspec2 2.2
		return validJavaIdentifier(name);
	}

	/**
	 * This class serves for finding out if a given JavaClass' ConstantPool
	 * references an Inner Class.
	 * The Java Virtual Machine Specification, Second Edition is not very precise
	 * about when an "InnerClasses" attribute has to appear. However, it states that
	 * there has to be exactly one InnerClasses attribute in the ClassFile structure
	 * if the constant pool of a class or interface refers to any class or interface
	 * "that is not a member of a package". Sun does not mean "member of the default
	 * package". In "Inner Classes Specification" they point out how a "bytecode name"
	 * is derived so one has to deduce what a class name of a class "that is not a
	 * member of a package" looks like: there is at least one character in the byte-
	 * code name that cannot be part of a legal Java Language Class name (and not equal
	 * to '/'). This assumption is wrong as the delimiter is '$' for which
	 * Character.isJavaIdentifierPart() == true.
	 * Hence, you really run into trouble if you have a toplevel class called
	 * "A$XXX" and another toplevel class called "A" with in inner class called "XXX".
	 * JustIce cannot repair this; please note that existing verifiers at this
	 * time even fail to detect missing InnerClasses attributes in pass 2.
	 */
	private class InnerClassDetector extends EmptyVisitor{
		private boolean hasInnerClass = false;
		private JavaClass jc;
		private ConstantPool cp;
		private InnerClassDetector(){} // Don't use.
		/** Constructs an InnerClassDetector working on the JavaClass _jc. */
		public InnerClassDetector(JavaClass _jc){
			jc = _jc;
			cp = jc.getConstantPool();
			(new DescendingVisitor(jc, this)).visit();
		}
		/**
		 * Returns if the JavaClass this InnerClassDetector is working on
		 * has an Inner Class reference in its constant pool.
		 */
		public boolean innerClassReferenced(){
			return hasInnerClass;
		}
		/** This method casually visits ConstantClass references. */
		public void visitConstantClass(ConstantClass obj){
			Constant c = cp.getConstant(obj.getNameIndex());
			if (c instanceof ConstantUtf8){ //Ignore the case where it's not a ConstantUtf8 here, we'll find out later.
				String classname = ((ConstantUtf8) c).getBytes();
				if (classname.startsWith(jc.getClassName().replace('.','/')+"$")){
					hasInnerClass = true;
				}
			}
		}
	}
	
	/**
	 * This method is here to save typing work and improve code readability.
	 */
	private static String tostring(Node n){
		return new StringRepresentation(n).toString();
	}
}
