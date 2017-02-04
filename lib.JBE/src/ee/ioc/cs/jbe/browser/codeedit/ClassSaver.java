/*
 *
 */
package ee.ioc.cs.jbe.browser.codeedit;

import java.io.IOException;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public class ClassSaver implements Runnable {

	public static final int SAVE_MISC = 1;

	public static final int SAVE_CODE = 2;

	public static final int SAVE_CONSTANT = 3;

	public static final int REMOVE_CONSTANT = 4;

	public static final int SAVE_FIELD = 5;

	public static final int REMOVE_FIELD = 6;

	public static final int SAVE_METHOD = 7;

	public static final int REMOVE_METHOD = 8;

	public static final int SAVE_EXCEPTION = 9;

	public static final int REMOVE_EXCEPTION = 10;

	public static final int REMOVE_INTERFACE = 11;

	public static final int ADD_METHOD = 12;

	public static final int ADD_INTERFACE = 13;

	static public final int ADD_FIELD = 14;

	private int state;

	private int maxStack;

	private int maxLocals;

	private int index;

	private String fileName;

	private String methodBody;

	private boolean exceptionOccured = false;

	private String exceptionVerbose = "";

	private byte constType;

	private String[] constInfo;

	private int startPc;

	private int endPc;

	private int handlerPc;

	private String handlerClass;

	private int exceptionIndex;

	private int accessFlags;

	private String methodName;

	private String methodDescriptor;

	private String interfaceName;

	private String fieldDescriptor;

	private String fieldName;

	public ClassSaver(int state, String fileName, int maxStack, int maxLocals,
			int methodIndex) {
		this.state = state;
		this.fileName = fileName;
		this.maxStack = maxStack;
		this.maxLocals = maxLocals;
		this.index = methodIndex;

	}

	public ClassSaver(int state, String fileName, String methodBody,
			int methodIndex) {
		this.state = state;
		this.fileName = fileName;
		this.methodBody = methodBody;
		this.index = methodIndex;
	}

	public ClassSaver(int state, String fileName, int index) {
		this.state = state;
		this.fileName = fileName;
		this.index = index;
	}

	public ClassSaver(int state, String fileName, String[] constInfo,
			byte constType) {
		this.state = state;
		this.fileName = fileName;
		this.constInfo = constInfo;
		this.constType = constType;
	}

	public ClassSaver(int state, String fileName, int methodIndex, int startPc,
			int endPc, int handlerPc, String handlerClass) {
		this.state = state;
		this.fileName = fileName;
		this.index = methodIndex;
		this.startPc = startPc;
		this.endPc = endPc;
		this.handlerPc = handlerPc;
		this.handlerClass = handlerClass;
	}

	public ClassSaver(int state, String fileName, int methodIndex,
			int exceptionIndex) {
		this.state = state;
		this.fileName = fileName;
		this.index = methodIndex;
		this.exceptionIndex = exceptionIndex;

	}

	public ClassSaver(int state, String fileName, int accessFlags,
			String methodName, String methodDescriptor) {
		this.state = state;
		this.fileName = fileName;
		this.accessFlags = accessFlags;
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
	}

	public ClassSaver(int state, String fileName, String interfaceName) {
		this.state = state;
		this.fileName = fileName;
		this.interfaceName = interfaceName;
	}

	public ClassSaver(int state, String fileName, String fieldName,
			String fieldDescriptor, int accessFlags) {
		this.state = state;
		this.fileName = fileName;
		this.fieldName = fieldName;
		this.fieldDescriptor = fieldDescriptor;
		this.accessFlags = accessFlags;
	}

	public void saveMisc(String fileName, int maxStack, int maxLocals,
			int methodIndex) {
		try {
			JavaClass javaClass = new ClassParser(fileName).parse();
			String className = javaClass.getClassName();
			Method[] methods = javaClass.getMethods();
			ConstantPool constants = javaClass.getConstantPool();
			ConstantPoolGen cpg = new ConstantPoolGen(constants);

			MethodGen mg = new MethodGen(methods[methodIndex], className, cpg);

			mg.setMaxStack(maxStack);

			mg.setMaxLocals(maxLocals);
			methods[methodIndex] = mg.getMethod();

			javaClass.setConstantPool(cpg.getFinalConstantPool());

			javaClass.dump(fileName);

		} catch (ClassFormatException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();
		} catch (IOException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();
		}

	}

	public void saveCode(String fileName, String jasm, int methodIndex) {
		try {
			JavaClass javaClass = new ClassParser(fileName).parse();
			String className = javaClass.getClassName();

			// Set up constant pool
			ConstantPool constants = javaClass.getConstantPool();
			ConstantPoolGen cpg = new ConstantPoolGen(constants);

			JAsmParser codeParser = new JAsmParser();

			// Set up methods
			Method[] methods = javaClass.getMethods();

			MethodGen mg = new MethodGen(methods[methodIndex], className, cpg);

			// Note that parsing has a sideeffect, it updates
			// constant pool, to have the required constants ready.
			// Of course the constant pool isnt dumped into the classfile unless
			// parsing was successful

			InstructionList il = codeParser.parse(jasm, cpg);
			il.setPositions(true);
			mg.removeLineNumbers();
			mg.removeLocalVariables();
			mg.setInstructionList(il);

			methods[methodIndex] = mg.getMethod();

			javaClass.setConstantPool(cpg.getFinalConstantPool());

			javaClass.dump(fileName);

		} catch (ClassFormatException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();
		} catch (JAsmParseException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getErrorVerbose();

		}
	}

	public void saveConstant(String fileName, String[] constInfo, byte constType) {

		try {
			JavaClass javaClass = new ClassParser(fileName).parse();

			ConstantPool constants = javaClass.getConstantPool();
			ConstantPoolGen cpg = new ConstantPoolGen(constants);

			switch (constType) {
			case Constants.CONSTANT_Class:
				cpg.addClass(constInfo[0]);
				break;
			case Constants.CONSTANT_Double:
				cpg.addDouble(Double.parseDouble(constInfo[0]));
				break;
			case Constants.CONSTANT_Integer:
				cpg.addInteger(Integer.parseInt(constInfo[0]));
				break;
			case Constants.CONSTANT_Fieldref:
				cpg.addFieldref(constInfo[0], constInfo[1], constInfo[2]);
				break;
			case Constants.CONSTANT_Float:
				cpg.addFloat(Float.parseFloat(constInfo[0]));
				break;
			case Constants.CONSTANT_InterfaceMethodref:
				cpg.addInterfaceMethodref(constInfo[0], constInfo[1],
						constInfo[2]);
				break;
			case Constants.CONSTANT_Methodref:
				cpg.addMethodref(constInfo[0], constInfo[1], constInfo[2]);
				break;
			case Constants.CONSTANT_Long:
				cpg.addLong(Long.parseLong(constInfo[0]));
				break;
			case Constants.CONSTANT_NameAndType:
				cpg.addNameAndType(constInfo[0], constInfo[1]);
				break;
			case Constants.CONSTANT_String:
				cpg.addString(constInfo[0]);
				break;
			case Constants.CONSTANT_Utf8:
				cpg.addUtf8(constInfo[0]);
				break;
			}
			javaClass.setConstantPool(cpg.getFinalConstantPool());
			javaClass.dump(fileName);

		} catch (ClassFormatException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();
			e.printStackTrace();
		}
	}

	public void removeConstant(String fileName, int constantPoolIndex) {
		try {
			JavaClass javaClass = new ClassParser(fileName).parse();
			ConstantPool cp = javaClass.getConstantPool();
			Constant[] constants = cp.getConstantPool();
			Constant[] newPool = new Constant[constants.length - 1];
			int j = 0;
			for (int i = 0; i < constants.length; i++) {
				if (i != constantPoolIndex) {
					newPool[j] = constants[i];
					j++;
				}

			}
			cp.setConstantPool(newPool);
			javaClass.setConstantPool(cp);
			javaClass.dump(fileName);

		} catch (IOException ioe) {
			exceptionOccured = true;
			exceptionVerbose = ioe.getMessage();
			ioe.printStackTrace();
		}
	}

	public void removeMethod(String fileName, int methodIndex) {
		try {
			JavaClass javaClass = new ClassParser(fileName).parse();
			Method[] methods = javaClass.getMethods();

			Method[] newMethods = new Method[methods.length - 1];
			int j = 0;
			for (int i = 0; i < methods.length; i++) {
				if (i != methodIndex) {
					newMethods[j] = methods[i];
					j++;
				}

			}
			javaClass.setMethods(newMethods);
			javaClass.dump(fileName);

		} catch (IOException ioe) {
			exceptionOccured = true;
			exceptionVerbose = ioe.getMessage();

			ioe.printStackTrace();
		}
	}

	public void removeField(String fileName, int fieldIndex) {
		try {
			JavaClass javaClass = new ClassParser(fileName).parse();
			Field[] fields = javaClass.getFields();

			Field[] newFields = new Field[fields.length - 1];
			int j = 0;
			for (int i = 0; i < fields.length; i++) {
				if (i != fieldIndex) {
					newFields[j] = fields[i];
					j++;
				}

			}
			javaClass.setFields(newFields);
			javaClass.dump(fileName);

		} catch (IOException ioe) {
			exceptionOccured = true;
			exceptionVerbose = ioe.getMessage();

			ioe.printStackTrace();
		}
	}

	public void removeInterface(String fileName, int interfaceIndex) {
		try {
			JavaClass javaClass = new ClassParser(fileName).parse();
			int[] interfaces = javaClass.getInterfaceIndices();

			int[] newInterfaces = new int[interfaces.length - 1];
			int j = 0;
			for (int i = 0; i < interfaces.length; i++) {
				if (i != interfaceIndex) {
					newInterfaces[j] = interfaces[i];
					j++;
				}

			}
			javaClass.setInterfaces(newInterfaces);
			javaClass.dump(fileName);

		} catch (IOException ioe) {
			exceptionOccured = true;
			exceptionVerbose = ioe.getMessage();

			ioe.printStackTrace();
		}

	}

	public void removeException(String fileName, int methodIndex,
			int exceptionIndex) {

		try {
			JavaClass javaClass = new ClassParser(fileName).parse();
			CodeException[] codeExceptions = javaClass.getMethods()[methodIndex]
					.getCode().getExceptionTable();
			CodeException[] newExceptions = new CodeException[codeExceptions.length - 1];
			int j = 0;
			for (int i = 0; i < codeExceptions.length; i++) {
				if (i != exceptionIndex) {
					newExceptions[j] = codeExceptions[i];
					j++;
				}

			}

			javaClass.getMethods()[methodIndex].getCode().setExceptionTable(
					newExceptions);
			javaClass.dump(fileName);
		} catch (IOException ioe) {
			exceptionOccured = true;
			exceptionVerbose = ioe.getMessage();

			ioe.printStackTrace();
		}

	}

	public void addException(String fileName, int methodIndex, int startPc,
			int endPc, int handlerPc, String handlerClass) {
		JavaClass javaClass;
		try {
			javaClass = new ClassParser(fileName).parse();
			CodeException[] codeExceptions = javaClass.getMethods()[methodIndex]
					.getCode().getExceptionTable();

			// resize the array via creating a new one and using the arraycopy
			CodeException[] newExceptions = new CodeException[codeExceptions.length + 1];
			System.arraycopy(codeExceptions, 0, newExceptions, 0,
					codeExceptions.length);

			// create a constant for handler
			ConstantPool constants = javaClass.getConstantPool();
			ConstantPoolGen cpg = new ConstantPoolGen(constants);
			int handlerIndex = cpg.addClass(handlerClass);
			javaClass.setConstantPool(cpg.getFinalConstantPool());

			CodeException ce = new CodeException(startPc, endPc, handlerPc,
					handlerIndex);
			newExceptions[newExceptions.length - 1] = ce;

			javaClass.getMethods()[methodIndex].getCode().setExceptionTable(
					newExceptions);
			javaClass.dump(fileName);

		} catch (ClassFormatException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();

			e.printStackTrace();
		} catch (IOException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();

			e.printStackTrace();
		}

	}

	public void addMethod(String fileName, int accessFlags, String methodName,
			String methodDescriptor) {
		try {

			JavaClass javaClass = new ClassParser(fileName).parse();

			ConstantPool constants = javaClass.getConstantPool();
			ConstantPoolGen cpg = new ConstantPoolGen(constants);

			constants = javaClass.getConstantPool();

			MethodGen methodGen = new MethodGen(accessFlags, methodName,
					methodDescriptor, javaClass.getClassName(),
					new InstructionList(), cpg);
			methodGen.removeLocalVariables();
			methodGen.setMaxLocals();
			methodGen.setMaxStack();
			Method[] oldMethods = javaClass.getMethods();
			Method[] newMethods = new Method[oldMethods.length + 1];
			for (int i = 0; i < oldMethods.length; i++) {
				newMethods[i] = oldMethods[i];
			}
			newMethods[oldMethods.length] = methodGen.getMethod();

			javaClass.setConstantPool(cpg.getFinalConstantPool());
			javaClass.setMethods(newMethods);
			javaClass.dump(fileName);

		} catch (ClassFormatException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			exceptionOccured = true;
			exceptionVerbose = e.getMessage();
			e.printStackTrace();
		}
	}

	public void addInterface(String fileName, String interfaceName) {
		try {
			JavaClass javaClass = new ClassParser(fileName).parse();

			ConstantPool constants = javaClass.getConstantPool();
			ConstantPoolGen cpg = new ConstantPoolGen(constants);
			int ref = cpg.addClass(interfaceName);
			int[] interfaces = javaClass.getInterfaceIndices();

			int[] newInterfaces = new int[interfaces.length + 1];
			for (int i = 0; i < interfaces.length; i++) {
				newInterfaces[i] = interfaces[i];
			}
			newInterfaces[interfaces.length] = ref;

			javaClass.setConstantPool(cpg.getFinalConstantPool());
			javaClass.setInterfaces(newInterfaces);
			javaClass.dump(fileName);

		} catch (ClassFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addField(String fileName, String fieldName,
			String fieldDescriptor, int access) {
		try {
			JavaClass javaClass = new ClassParser(fileName).parse();

			ConstantPool constants = javaClass.getConstantPool();
			ConstantPoolGen cpg = new ConstantPoolGen(constants);

			constants = javaClass.getConstantPool();

			FieldGen fieldGen = new FieldGen(access, fieldName,
					fieldDescriptor, cpg);

			Field[] oldFields = javaClass.getFields();
			Field[] newFields = new Field[oldFields.length + 1];
			for (int i = 0; i < oldFields.length; i++) {
				newFields[i] = oldFields[i];
			}
			newFields[oldFields.length] = fieldGen.getField();

			javaClass.setConstantPool(cpg.getFinalConstantPool());
			javaClass.setFields(newFields);
			javaClass.dump(fileName);

		} catch (ClassFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		if (state == SAVE_MISC) {
			saveMisc(fileName, maxStack, maxLocals, index);
		} else if (state == SAVE_CODE) {
			saveCode(fileName, methodBody, index);
		} else if (state == REMOVE_FIELD) {
			removeField(fileName, index);
		} else if (state == REMOVE_METHOD) {
			removeMethod(fileName, index);
		} else if (state == REMOVE_CONSTANT) {
			removeConstant(fileName, index);
		} else if (state == REMOVE_INTERFACE) {
			removeInterface(fileName, index);
		} else if (state == SAVE_CONSTANT) {
			saveConstant(fileName, constInfo, constType);
		} else if (state == SAVE_EXCEPTION) {
			addException(fileName, index, startPc, endPc, handlerPc,
					handlerClass);
		} else if (state == REMOVE_EXCEPTION) {
			removeException(fileName, index, exceptionIndex);
		} else if (state == ADD_METHOD) {
			addMethod(fileName, accessFlags, methodName, methodDescriptor);
		} else if (state == ADD_INTERFACE) {
			addInterface(fileName, interfaceName);
		} else if (state == ADD_FIELD) {
			addField(fileName, fieldName, fieldDescriptor, accessFlags);
		}

	}

	public boolean exceptionOccured() {
		return exceptionOccured;
	}

	public String getExceptionVerbose() {
		return exceptionVerbose;
	}

}
