/*
 *
 */
package ee.ioc.cs.jbe.browser.codeedit;

import java.util.ArrayList;
import org.apache.bcel.generic.*;
import org.gjt.jclasslib.bytecode.OpcodesUtil;


public class JAsmParser {
	/*
	 * Parses the input code and returns an instructionlist, but also has a
	 * sideeffect: it updates the constant pool on the fly to have the required
	 * constants in the constant pool.
	 */
	private JAsmParseException parseException = new JAsmParseException();

	public InstructionList parse(String code, ConstantPoolGen cpg)
			throws JAsmParseException {
		code = code.replaceAll("\r", "");
		String[] codeLines = code.split("\n");
		if (codeLines.length == 1 && codeLines[0].equals("")) {
			return new InstructionList();
		}
		InstructionList instructions = new InstructionList();
		ArrayList<InstructionHandle> instructionHandleList = new ArrayList<InstructionHandle> ();
		ArrayList<TempSwitchData> lookupSwitches = new ArrayList<TempSwitchData>();
		ArrayList<TempSwitchData>  tableSwitches = new ArrayList<TempSwitchData>();
		ArrayList<BranchPair> branches = new ArrayList<BranchPair>();
		InstructionHandle ih;
		String[] instrElems;
		int codeLength = countLines(codeLines);
		// InstructionHandle[] iha = new InstructionHandle[strt.countTokens()];
		int labels = 0;
		int switchMode = 0; // 0- normal , 1- tableswitch, 2 - lookupswitch
		String fullInstr;
		String instrName;
		TempSwitchData tempSwitch = new TempSwitchData();

		for (int i = 0; i < codeLines.length; i++) {
			fullInstr = codeLines[i];
			//switchmode, 1 denoting tableswitch, 2 lookupswitch
			if (beginsWithWhitespace(fullInstr) && switchMode == 1) {
				boolean isDefault = isDefaultLine(fullInstr.trim());
				if (isDefault) {
					int target = getLookupTarget(fullInstr.trim(), labels,
							codeLength); 
					tempSwitch.getBranchPairs().add(
							new BranchPair(-1, target));
				} else {
					int target = getTableArg(fullInstr.trim(), labels, codeLength);
					tempSwitch.getBranchPairs().add(
							new BranchPair(tempSwitch.getInitialLab(), target));
					tempSwitch.incInitialLab();
				}
				
				

			} else if (beginsWithWhitespace(fullInstr) && switchMode == 2) {

				int target = getLookupTarget(fullInstr.trim(), labels,
						codeLength);
				int value = getLookupSource(fullInstr.trim(), labels);
				tempSwitch.getBranchPairs().add(new BranchPair(value, target));
			} else if (beginsWithWhitespace(fullInstr)) {
						parseException.addError(JAsmParseException.WHITESPACE_ERROR,
								fullInstr, labels-1);
				
			} else {
				if (switchMode == 1) {
					TABLESWITCH ts = new TABLESWITCH();
					ih = instructions.append(ts);
					instructionHandleList.add(ih);
					tempSwitch.setHandle(ih);
					tableSwitches.add(tempSwitch);
					labels++;
					switchMode = 0;
				} else if (switchMode == 2) {
					LOOKUPSWITCH ls = new LOOKUPSWITCH();
					ih = instructions.append(ls);
					instructionHandleList.add(ih);
					tempSwitch.setHandle(ih);
					lookupSwitches.add(tempSwitch);
					labels++;
					switchMode = 0;
				} 

				instrElems = fullInstr.split(" ");
				instrName = instrElems[0].toLowerCase().trim();

				if (instrName.equals("bipush")) {
					byte arg = getSingleByteArg(instrElems, labels);
					ih = instructions.append(new BIPUSH(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("sipush")) {
					short arg = getSingleShortArg(instrElems, labels);
					ih = instructions.append(new SIPUSH(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iinc")) {
					int arg1 = 0;
					int arg2 = 0;
					try {
						arg1 = Integer.parseInt(instrElems[1]);
						arg2 = Integer.parseInt(instrElems[2]);
					} catch (NumberFormatException nfe) {
						parseException.addError(
								JAsmParseException.INT_REQUIRED, instrElems[0],
								labels);
					} catch (ArrayIndexOutOfBoundsException aobe) {
						parseException.addError(
								JAsmParseException.MISSING_ARGUMENTS,
								instrElems[0], labels);
					}

					ih = instructions.append(new IINC(arg1, arg2));
					instructionHandleList.add(ih);
					labels++;
				}
				/*
				 * Class and object operations.
				 */
				else if (instrName.equals("anewarray")) {
					int arg = getClassConstRef(instrElems, cpg, labels);
					ih = instructions.append(new ANEWARRAY(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("checkcast")) {
					int arg = getClassConstRef(instrElems, cpg, labels);
					ih = instructions.append(new CHECKCAST(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("instanceof")) {
					int arg = getClassConstRef(instrElems, cpg, labels);
					ih = instructions.append(new INSTANCEOF(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("new")) {
					int arg = getClassConstRef(instrElems, cpg, labels);
					ih = instructions.append(new NEW(arg));
					instructionHandleList.add(ih);
					labels++;
				}
				/*
				 * Invoke instructions
				 */
				else if (instrName.equals("invokevirtual")) {
					int arg = getMethodConstRef(instrElems, cpg, labels);
					ih = instructions.append(new INVOKEVIRTUAL(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("invokestatic")) {
					int arg = getMethodConstRef(instrElems, cpg, labels);

					ih = instructions.append(new INVOKESTATIC(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("invokespecial")) {
					int arg = getMethodConstRef(instrElems, cpg, labels);

					ih = instructions.append(new INVOKESPECIAL(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("invokeinterface")) {
					int index = getInterfaceConstRef(instrElems, cpg, labels);
					int nargs = Integer.parseInt(instrElems[2]);
					ih = instructions.append(new INVOKEINTERFACE(index, nargs));
					instructionHandleList.add(ih);
					labels++;
				}
				/*
				 * Field instructions
				 */
				else if (instrName.equals("getstatic")) {
					int arg = getFieldConstRef(instrElems, cpg, labels);

					ih = instructions.append(new GETSTATIC(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("getfield")) {
					int arg = getFieldConstRef(instrElems, cpg, labels);

					ih = instructions.append(new GETFIELD(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("putstatic")) {
					int arg = getFieldConstRef(instrElems, cpg, labels);

					ih = instructions.append(new PUTSTATIC(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("putfield")) {
					int arg = getFieldConstRef(instrElems, cpg, labels);

					ih = instructions.append(new PUTFIELD(arg));
					instructionHandleList.add(ih);
					labels++;
				}
				/*
				 * Newarray instructions
				 */
				else if (instrName.equals("newarray")) {
					byte arg = getArrayRef(instrElems, labels);

					ih = instructions.append(new NEWARRAY(arg));
					instructionHandleList.add(ih);
					labels++;
				}

				else if (instrName.equals("multianewarray")) {
					short dim = 1;
					int arg = 0;
					try {
						dim = Short.parseShort(instrElems[2]);
					} catch (NumberFormatException nfe) {
						parseException.addError(
								JAsmParseException.SHORT_REQUIRED,
								instrElems[0], labels);
					} catch (ArrayIndexOutOfBoundsException aobe) {
						parseException.addError(
								JAsmParseException.MISSING_ARGUMENTS,
								instrElems[0], labels);
					}

					try {
						arg = Integer.parseInt(instrElems[1]);
					} catch (NumberFormatException nfe) {
						String classN = instrElems[1];
						arg = cpg.addClass(classN);
					} catch (ArrayIndexOutOfBoundsException aobe) {
						parseException.addError(
								JAsmParseException.MISSING_ARGUMENTS,
								instrElems[0], labels);
					}

					ih = instructions.append(new MULTIANEWARRAY(arg, dim));
					instructionHandleList.add(ih);
					labels++;
				}
				/*
				 * Load constant instructions
				 */
				else if (instrName.equals("ldc")) {
					int arg = getConstRef4ldc(instrElems, cpg, labels);
					ih = instructions.append(new LDC(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ldc_w")) {
					int arg = getConstRef4ldc(instrElems, cpg, labels);
					ih = instructions.append(new LDC_W(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ldc2_w")) {
					int arg = getConstRefldc2_w(instrElems, cpg, labels);
					ih = instructions.append(new LDC2_W(arg));
					instructionHandleList.add(ih);
					labels++;
				}
				/*
				 * Local Variable instructions
				 */
				else if (instrName.equals("ret")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new RET(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("aload")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new ALOAD(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("astore")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new ASTORE(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dload")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new DLOAD(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dstore")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new DSTORE(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fload")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new FLOAD(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fstore")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new FSTORE(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iload")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new ILOAD(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("istore")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new ISTORE(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lload")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new LLOAD(arg));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lstore")) {
					int arg = getSingleIntArg(instrElems, labels);
					ih = instructions.append(new LSTORE(arg));
					instructionHandleList.add(ih);
					labels++;
				}
				/*
				 * Switch instructions
				 */
				else if (instrName.equals("tableswitch")) {
					switchMode = 1;
					int arg = getSingleIntArg(instrElems, labels);
					tempSwitch = new TempSwitchData(2, arg);
				} else if (instrName.equals("lookupswitch")) {
					switchMode = 2;
					tempSwitch = new TempSwitchData(1);
				}

				/*
				 * 0 parameter instructions
				 */
				else if (instrName.equals("aaload")) {
					ih = instructions.append(new AALOAD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("aastore")) {
					ih = instructions.append(new AASTORE());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("aconst_null")) {
					ih = instructions.append(new ACONST_NULL());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("aload_0")) {
					ih = instructions.append(new ALOAD(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("aload_1")) {
					ih = instructions.append(new ALOAD(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("aload_2")) {
					ih = instructions.append(new ALOAD(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("aload_3")) {
					ih = instructions.append(new ALOAD(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("areturn")) {
					ih = instructions.append(new ARETURN());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("arraylength")) {
					ih = instructions.append(new ARRAYLENGTH());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("astore_0")) {
					ih = instructions.append(new ASTORE(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("astore_1")) {
					ih = instructions.append(new ASTORE(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("astore_2")) {
					ih = instructions.append(new ASTORE(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("astore_3")) {
					ih = instructions.append(new ASTORE(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("athrow")) {
					ih = instructions.append(new ATHROW());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("baload")) {
					ih = instructions.append(new BALOAD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("bastore")) {
					ih = instructions.append(new BASTORE());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("breakpoint")) {
					ih = instructions.append(new BREAKPOINT());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("caload")) {
					ih = instructions.append(new CALOAD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("castore")) {
					ih = instructions.append(new CASTORE());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("d2f")) {
					ih = instructions.append(new D2F());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("d2i")) {
					ih = instructions.append(new D2I());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("d2l")) {
					ih = instructions.append(new D2L());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dadd")) {
					ih = instructions.append(new DADD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("daload")) {
					ih = instructions.append(new DALOAD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dastore")) {
					ih = instructions.append(new DASTORE());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dcmpg")) {
					ih = instructions.append(new DCMPG());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dcmpl")) {
					ih = instructions.append(new DCMPL());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dconst_0")) {
					ih = instructions.append(new DCONST(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dconst_1")) {
					ih = instructions.append(new DCONST(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ddiv")) {
					ih = instructions.append(new DDIV());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dload_0")) {
					ih = instructions.append(new DLOAD(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dload_1")) {
					ih = instructions.append(new DLOAD(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dload_2")) {
					ih = instructions.append(new DLOAD(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dload_3")) {
					ih = instructions.append(new DLOAD(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dmul")) {
					ih = instructions.append(new DMUL());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dneg")) {
					ih = instructions.append(new DNEG());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("drem")) {
					ih = instructions.append(new DREM());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dreturn")) {
					ih = instructions.append(new DRETURN());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dstore_0")) {
					ih = instructions.append(new DSTORE(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dstore_1")) {
					ih = instructions.append(new DSTORE(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dstore_2")) {
					ih = instructions.append(new DSTORE(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dstore_3")) {
					ih = instructions.append(new DSTORE(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dsub")) {
					ih = instructions.append(new DSUB());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dup")) {
					ih = instructions.append(new DUP());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dup2")) {
					ih = instructions.append(new DUP2());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dup2_x1")) {
					ih = instructions.append(new DUP2_X1());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dup2_x2")) {
					ih = instructions.append(new DUP2_X2());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dup_x1")) {
					ih = instructions.append(new DUP_X1());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("dup_x2")) {
					ih = instructions.append(new DUP_X2());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("f2d")) {
					ih = instructions.append(new F2D());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("f2i")) {
					ih = instructions.append(new F2I());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("f2l")) {
					ih = instructions.append(new F2L());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fadd")) {
					ih = instructions.append(new FADD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("faload")) {
					ih = instructions.append(new FALOAD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fastore")) {
					ih = instructions.append(new FASTORE());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fcmpg")) {
					ih = instructions.append(new FCMPG());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fcmpl")) {
					ih = instructions.append(new FCMPL());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fconst_0")) {
					ih = instructions.append(new FCONST(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fconst_1")) {
					ih = instructions.append(new FCONST(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fconst_2")) {
					ih = instructions.append(new FCONST(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fdiv")) {
					ih = instructions.append(new FDIV());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fload_0")) {
					ih = instructions.append(new FLOAD(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fload_1")) {
					ih = instructions.append(new FLOAD(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fload_2")) {
					ih = instructions.append(new FLOAD(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fload_3")) {
					ih = instructions.append(new FLOAD(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fmul")) {
					ih = instructions.append(new FMUL());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fneg")) {
					ih = instructions.append(new FNEG());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("frem")) {
					ih = instructions.append(new FREM());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("freturn")) {
					ih = instructions.append(new FRETURN());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fstore_0")) {
					ih = instructions.append(new FSTORE(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fstore_1")) {
					ih = instructions.append(new FSTORE(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fstore_2")) {
					ih = instructions.append(new FSTORE(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fstore_3")) {
					ih = instructions.append(new FSTORE(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("fsub")) {
					ih = instructions.append(new FSUB());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("i2d")) {
					ih = instructions.append(new I2D());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("i2f")) {
					ih = instructions.append(new I2F());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("i2l")) {
					ih = instructions.append(new I2L());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iadd")) {
					ih = instructions.append(new IADD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iaload")) {
					ih = instructions.append(new IALOAD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iand")) {
					ih = instructions.append(new IAND());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iastore")) {
					ih = instructions.append(new IASTORE());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iconst_0")) {
					ih = instructions.append(new ICONST(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iconst_1")) {
					ih = instructions.append(new ICONST(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iconst_2")) {
					ih = instructions.append(new ICONST(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iconst_3")) {
					ih = instructions.append(new ICONST(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iconst_4")) {
					ih = instructions.append(new ICONST(4));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iconst_5")) {
					ih = instructions.append(new ICONST(5));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iconst_m1")) {
					ih = instructions.append(new ICONST(-1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("idiv")) {
					ih = instructions.append(new IDIV());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iload_0")) {
					ih = instructions.append(new ILOAD(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iload_1")) {
					ih = instructions.append(new ILOAD(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iload_2")) {
					ih = instructions.append(new ILOAD(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iload_3")) {
					ih = instructions.append(new ILOAD(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("imul")) {
					ih = instructions.append(new IMUL());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ineg")) {
					ih = instructions.append(new INEG());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("i2b")) {
					ih = instructions.append(new I2B());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("i2c")) {
					ih = instructions.append(new I2C());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("i2s")) {
					ih = instructions.append(new I2S());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ior")) {
					ih = instructions.append(new IOR());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("irem")) {
					ih = instructions.append(new IREM());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ireturn")) {
					ih = instructions.append(new IRETURN());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ishl")) {
					ih = instructions.append(new ISHL());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ishr")) {
					ih = instructions.append(new ISHR());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("istore_0")) {
					ih = instructions.append(new ISTORE(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("istore_1")) {
					ih = instructions.append(new ISTORE(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("istore_2")) {
					ih = instructions.append(new ISTORE(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("istore_3")) {
					ih = instructions.append(new ISTORE(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("isub")) {
					ih = instructions.append(new ISUB());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("iushr")) {
					ih = instructions.append(new IUSHR());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ixor")) {
					ih = instructions.append(new IXOR());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("l2d")) {
					ih = instructions.append(new L2D());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("l2f")) {
					ih = instructions.append(new L2F());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("l2i")) {
					ih = instructions.append(new L2I());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ladd")) {
					ih = instructions.append(new LADD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("laload")) {
					ih = instructions.append(new LALOAD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("land")) {
					ih = instructions.append(new LAND());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lastore")) {
					ih = instructions.append(new LASTORE());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lcmp")) {
					ih = instructions.append(new LCMP());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lconst_0")) {
					ih = instructions.append(new LCONST(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lconst_1")) {
					ih = instructions.append(new LCONST(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("ldiv")) {
					ih = instructions.append(new LDIV());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lload_0")) {
					ih = instructions.append(new LLOAD(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lload_1")) {
					ih = instructions.append(new LLOAD(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lload_2")) {
					ih = instructions.append(new LLOAD(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lload_3")) {
					ih = instructions.append(new LLOAD(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lmul")) {
					ih = instructions.append(new LMUL());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lneg")) {
					ih = instructions.append(new LNEG());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lor")) {
					ih = instructions.append(new LOR());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lrem")) {
					ih = instructions.append(new LREM());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lreturn")) {
					ih = instructions.append(new LRETURN());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lshl")) {
					ih = instructions.append(new LSHL());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lshr")) {
					ih = instructions.append(new LSHR());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lstore_0")) {
					ih = instructions.append(new LSTORE(0));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lstore_1")) {
					ih = instructions.append(new LSTORE(1));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lstore_2")) {
					ih = instructions.append(new LSTORE(2));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lstore_3")) {
					ih = instructions.append(new LSTORE(3));
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lsub")) {
					ih = instructions.append(new LSUB());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lushr")) {
					ih = instructions.append(new LUSHR());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("lxor")) {
					ih = instructions.append(new LXOR());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("monitorenter")) {
					ih = instructions.append(new MONITORENTER());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("monitorexit")) {
					ih = instructions.append(new MONITOREXIT());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("nop")) {
					ih = instructions.append(new NOP());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("pop")) {
					ih = instructions.append(new POP());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("pop2")) {
					ih = instructions.append(new POP2());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("return")) {
					ih = instructions.append(new RETURN());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("saload")) {
					ih = instructions.append(new SALOAD());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("sastore")) {
					ih = instructions.append(new SASTORE());
					instructionHandleList.add(ih);
					labels++;
				} else if (instrName.equals("swap")) {
					ih = instructions.append(new SWAP());
					instructionHandleList.add(ih);
					labels++;
				}
				// Jump instructions
				else if (instrName.equals("goto")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new GOTO(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("goto_w")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new GOTO_W(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("if_acmpeq")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IF_ACMPEQ(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("if_acmpne")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IF_ACMPNE(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("if_icmpeq")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IF_ICMPEQ(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("if_icmpge")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IF_ICMPGE(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("if_icmpgt")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IF_ICMPGT(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("if_icmple")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IF_ICMPLE(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("if_icmplt")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IF_ICMPLT(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("if_icmpne")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IF_ICMPNE(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("ifeq")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IFEQ(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("ifge")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IFGE(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("ifgt")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					;
					ih = instructions.append(new IFGT(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("ifle")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IFLE(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("iflt")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IFLT(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("ifne")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IFNE(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("ifnonnull")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IFNONNULL(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("ifnull")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new IFNULL(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("jsr")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new JSR(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else if (instrName.equals("jsr_w")) {
					int arg = getJumpArg(instrElems, labels, codeLength);
					ih = instructions.append(new JSR_W(null));
					instructionHandleList.add(ih);
					branches.add(new BranchPair(labels, arg));
					labels++;
				} else {
					parseException.addError(JAsmParseException.SYNTAX_ERROR,
							fullInstr, labels);
					labels++;
				}
			}
		}
		if (parseException.errorCount() > 0) {
			throw parseException;
		}

		for (int i = 0; i < lookupSwitches.size(); i++) {
			TempSwitchData tsd = (TempSwitchData) lookupSwitches.get(i);

			int targetArrSize = 0;
			for (int j = 0; j < tsd.getBranchPairs().size(); j++) {
				BranchPair bp = (BranchPair) tsd.getBranchPairs().get(j);
				if (bp.source != -1) {
					targetArrSize++;
				}
			}
			int[] targets = new int[targetArrSize];
			InstructionHandle[] targetInstrs = new InstructionHandle[targetArrSize];

			int count = 0;
			InstructionHandle defaultTarget = null;
			for (int j = 0; j < tsd.getBranchPairs().size(); j++) {
				BranchPair bp = (BranchPair) tsd.getBranchPairs().get(j);
				if (bp.source != -1) {
					targets[count] = bp.source;
					targetInstrs[count] = (InstructionHandle) instructionHandleList
							.get(bp.target - 1);
					count++;
				} else {
					defaultTarget = (InstructionHandle) instructionHandleList
							.get(bp.target - 1);
				}
			}

			LOOKUPSWITCH lus = (LOOKUPSWITCH) tsd.ih.getInstruction();
			lus.setMatchesTargets(targets, targetInstrs);
			lus.setTarget(defaultTarget);

		}
		
		for (int i = 0; i < tableSwitches.size(); i++) {
			TempSwitchData tsd = (TempSwitchData) tableSwitches.get(i);

			int targetArrSize = 0;
			for (int j = 0; j < tsd.getBranchPairs().size(); j++) {
				BranchPair bp = (BranchPair) tsd.getBranchPairs().get(j);
				if (bp.source != -1) {
					targetArrSize++;
				}
			}
			int[] targets = new int[targetArrSize];
			InstructionHandle[] targetInstrs = new InstructionHandle[targetArrSize];

			int count = 0;
			InstructionHandle defaultTarget = null;
			for (int j = 0; j < tsd.getBranchPairs().size(); j++) {
				BranchPair bp = (BranchPair) tsd.getBranchPairs().get(j);
				if (bp.source != -1) {
					targets[count] = bp.source;
					targetInstrs[count] = (InstructionHandle) instructionHandleList
							.get(bp.target - 1);
					count++;
				} else {
					defaultTarget = (InstructionHandle) instructionHandleList
							.get(bp.target - 1);
				}
			}

			TABLESWITCH ts = (TABLESWITCH) tsd.ih.getInstruction();
			ts.setMatchesTargets(targets, targetInstrs);
			ts.setTarget(defaultTarget);

		}
		for (int i = 0; i < branches.size(); i++) {
			BranchPair bp = (BranchPair) branches.get(i);
			ih = (InstructionHandle) instructionHandleList.get(bp.source);

			if (ih.getInstruction() instanceof GotoInstruction) {
				GotoInstruction jInst = (GotoInstruction) ih.getInstruction();
				jInst.setTarget((InstructionHandle) instructionHandleList
						.get(bp.target - 1));
			} else {
				IfInstruction jInst = (IfInstruction) ih.getInstruction();
				jInst.setTarget((InstructionHandle) instructionHandleList
						.get(bp.target - 1));
			}

		}

		return instructions;
	}

	private boolean isDefaultLine(String arg) {
		String[] args = arg.split(":");
		if (args.length == 2 & args[0].trim().equals("default")) {
			return true;
		}
		return false;
	}

	private int getLookupSource(String arg, int line) {
		try {
			String[] args = arg.split(":");
			if (args.length != 2) {
				parseException.addError(JAsmParseException.BAD_LOOKUP_ARGUMENT,
						arg, line);
				return 1;
			}
			if (args[0].trim().equals("default")) {
				return -1;
			}
			int b = Integer.parseInt(args[0].trim());
			return b;
		} catch (ArrayIndexOutOfBoundsException exc1) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS, arg,
					line);
			return 1;
		} catch (NumberFormatException exc1) {
			parseException.addError(JAsmParseException.BAD_LOOKUP_ARGUMENT,
					arg, line);
			return 1;
		}
	}

	private int getLookupTarget(String arg, int line, int codeLength) {
		try {
			String[] args = arg.split(":");
			if (args.length != 2) {
				parseException.addError(JAsmParseException.BAD_LOOKUP_ARGUMENT,
						arg, line);
				return 1;
			}
			int b = Integer.parseInt(args[1].trim());
			if (b < 1 || b > codeLength) {
				parseException.addError(JAsmParseException.JUMP_OUT_OF_DOMAIN,
						arg, line);
				return 1;
			}
			return b;
		} catch (ArrayIndexOutOfBoundsException exc1) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS, arg,
					line);
			return 1;
		} catch (NumberFormatException exc1) {
			parseException.addError(JAsmParseException.BAD_LOOKUP_ARGUMENT,
					arg, line);
			return 1;
		}
	}

	private int countLines(String[] codeLines) {
		int count = 0;
		for (int i = 0; i < codeLines.length; i++) {
			if (!beginsWithWhitespace(codeLines[i])) {
				count++;
			}
		}
		return count;
	}

	private boolean beginsWithWhitespace(String line) {
		if (!line.equals("")) {
			if (line.charAt(0) == ' ' || line.charAt(0) == '\t')
				return true;
		}
		return false;
	}

	private int getTableArg(String arg, int line, int codeLength) {
		try {
			int i = Integer.parseInt(arg);
			if (i < 1 || i > codeLength) {
				parseException.addError(JAsmParseException.JUMP_OUT_OF_DOMAIN,
						arg, line);
				return 1;
			}
			return i;
		} catch (ArrayIndexOutOfBoundsException exc1) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS, arg,
					line);
			return 1;
		} catch (NumberFormatException exc1) {
			parseException.addError(JAsmParseException.INT_REQUIRED, arg, line);
			return 1;
		}
	}

	private int getJumpArg(String[] instrElems, int line, int codeLength) {
		try {
			int b = Integer.parseInt(instrElems[1]);
			if (b < 1 || b > codeLength) {
				parseException.addError(JAsmParseException.JUMP_OUT_OF_DOMAIN,
						instrElems[0], line);
				return 1;
			}
			return b;
		} catch (ArrayIndexOutOfBoundsException exc1) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 1;
		} catch (NumberFormatException exc1) {
			parseException.addError(JAsmParseException.INT_REQUIRED,
					instrElems[0], line);
			return 1;
		}
	}

	private short getSingleShortArg(String[] instrElems, int line) {
		try {
			short b = Short.parseShort(instrElems[1]);
			return b;
		} catch (ArrayIndexOutOfBoundsException exc1) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		} catch (NumberFormatException exc1) {
			parseException.addError(JAsmParseException.SHORT_REQUIRED,
					instrElems[0], line);
			return 0;
		}
	}

	private int getSingleIntArg(String[] instrElems, int line) {
		try {
			int b = Integer.parseInt(instrElems[1]);
			return b;
		} catch (ArrayIndexOutOfBoundsException exc1) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		} catch (NumberFormatException exc1) {
			parseException.addError(JAsmParseException.INT_REQUIRED,
					instrElems[0], line);
			return 0;
		}
	}

	private byte getSingleByteArg(String[] instrElems, int line) {
		try {
			byte b = Byte.parseByte(instrElems[1]);
			return b;
		} catch (ArrayIndexOutOfBoundsException exc1) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		} catch (NumberFormatException exc1) {
			parseException.addError(JAsmParseException.BYTE_REQUIRED,
					instrElems[0], line);
			return 0;
		}
	}

	private int getConstRefldc2_w(String[] instrElems, ConstantPoolGen cpg,
			int line) {
		if (instrElems.length < 2) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		}

		try {
			long larg = Long.parseLong(instrElems[1]);
			return cpg.addLong(larg);
		} catch (NumberFormatException nfei) {

		}

		try {
			double darg = Double.parseDouble(instrElems[1]);
			return cpg.addDouble(darg);
		} catch (NumberFormatException nfed) {

		}

		parseException.addError(JAsmParseException.ARG_TYPE_ERROR_LDC2_W,
				instrElems[0], line);

		return 0;
	}

	private byte getArrayRef(String[] instrElems, int line) {
		if (instrElems.length < 2) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		}
		byte arg;
		try {
			arg = Byte.parseByte(instrElems[1]);
		} catch (NumberFormatException nfe) {

			arg = OpcodesUtil.getArrayType(instrElems[1]);
			if (arg == 0) {
				parseException.addError(JAsmParseException.ARG_TYPE_ERROR,
						instrElems[0], line);
			}
		}
		return arg;
	}

	private int getConstRef4ldc(String[] instrElems, ConstantPoolGen cpg,
			int line) {
		if (instrElems.length < 2) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		}

		try {
			int iarg = Integer.parseInt(instrElems[1]);
			return cpg.addInteger(iarg);
		} catch (NumberFormatException nfei) {

		}

		try {
			float farg = Float.parseFloat(instrElems[1]);
			return cpg.addFloat(farg);
		} catch (NumberFormatException nfed) {

		}

		if (instrElems[1].startsWith("\"")) {
			StringBuffer sb = new StringBuffer(instrElems[1]);
			for (int i = 2; i < instrElems.length; i++) {
				sb.append(" ").append(instrElems[i]);
			}
			String sarg = sb.toString();
			if (sarg.startsWith("\"") && sarg.endsWith("\"")) {
				sarg = sarg.substring(1, sarg.length() - 1);
				return cpg.addString(sarg);
			} else {
				parseException.addError(JAsmParseException.ARG_TYPE_ERROR,
						instrElems[0], line);
				return 0;
			}

		}
		parseException.addError(JAsmParseException.ARG_TYPE_ERROR,
				instrElems[0], line);
		return 0;
	}

	private int getClassConstRef(String[] instrElems, ConstantPoolGen cpg,
			int line) {
		if (instrElems.length < 2) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		}
		int arg;
		try {
			arg = Integer.parseInt(instrElems[1]);
		} catch (NumberFormatException nfe) {
			String classN = instrElems[1];
			arg = cpg.addClass(classN);

		}
		return arg;
	}

	private int getFieldConstRef(String[] instrElems, ConstantPoolGen cpg,
			int line) {
		if (instrElems.length < 3) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		}
		int arg;
		try {
			arg = Integer.parseInt(instrElems[1]);
		} catch (NumberFormatException nfe) {
			String classN = getClassFromFieldName(instrElems[1]);
			String fieldN = getFieldFromFieldName(instrElems[1]);
			String descr = instrElems[2];
			arg = cpg.addFieldref(classN, fieldN, descr);

		}
		return arg;
	}

	private int getMethodConstRef(String[] instrElems, ConstantPoolGen cpg,
			int line) {
		if (instrElems.length < 2) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		}
		int arg;
		try {
			arg = Integer.parseInt(instrElems[1]);
		} catch (NumberFormatException nfe) {
			String classN = getClassFromFullMethod(instrElems[1]);
			String methodN = getMethodFromFullMethod(instrElems[1]);
			String descr = getDescrFromFullMethod(instrElems[1]);
			arg = cpg.addMethodref(classN, methodN, descr);
		}
		return arg;
	}
		 	
	private int getInterfaceConstRef(String[] instrElems, ConstantPoolGen cpg,
			int line) {
		if (instrElems.length < 2) {
			parseException.addError(JAsmParseException.MISSING_ARGUMENTS,
					instrElems[0], line);
			return 0;
		}
		int arg;
		try {
			arg = Integer.parseInt(instrElems[1]);
		} catch (NumberFormatException nfe) {
			String classN = getClassFromFullMethod(instrElems[1]);
			String methodN = getMethodFromFullMethod(instrElems[1]);
			String descr = getDescrFromFullMethod(instrElems[1]);
			arg = cpg.addInterfaceMethodref(classN, methodN, descr);

		}
		return arg;
	}

	public String getClassFromFullMethod(String fullMethod) {
		String classAndMeth = fullMethod.substring(0, fullMethod.indexOf('('));
		String className = getClassFromFieldName(classAndMeth);
		return className;
	}

	public String getMethodFromFullMethod(String fullMethod) {
		String classAndMeth = fullMethod.substring(0, fullMethod.indexOf('('));
		String methName = getFieldFromFieldName(classAndMeth);
		return methName;
	}

	public String getDescrFromFullMethod(String fullMethod) {
		String description = fullMethod.substring(fullMethod.indexOf('('),
				fullMethod.length());
		return description;
	}

	public String getClassFromFieldName(String fieldName) {
		String className = fieldName.substring(0, fieldName.lastIndexOf('/'));
		return className.replace('/', '.');

	}

	public String getFieldFromFieldName(String fieldName) {
		String field = fieldName.substring(fieldName.lastIndexOf('/') + 1,
				fieldName.length());
		return field;

	}

	class BranchPair {
		int source, target;

		BranchPair(int s, int t) {
			source = s;
			target = t;
		}
	}

	class TempSwitchData {
		int type; // 1 - table, 2 - lookup

		int initialLab;

		ArrayList<BranchPair> branchPairs = new ArrayList<BranchPair>();

		private InstructionHandle ih;

		public TempSwitchData(int type, int label) {
			this.type = type;
			initialLab = label;
		}

		public void setHandle(InstructionHandle ih) {
			this.ih = ih;
		}

		public void incInitialLab() {
			initialLab++;
		}

		public int getInitialLab() {
			return initialLab;
		}

		public ArrayList<BranchPair> getBranchPairs() {
			return branchPairs;
		}

		public TempSwitchData() {

		}

		public TempSwitchData(int type) {
			this.type = type;
		}

	}
}
