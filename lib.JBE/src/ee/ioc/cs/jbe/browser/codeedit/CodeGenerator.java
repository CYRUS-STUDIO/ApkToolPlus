/*
 *
 */
package ee.ioc.cs.jbe.browser.codeedit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreePath;

import org.gjt.jclasslib.bytecode.AbstractInstruction;
import org.gjt.jclasslib.bytecode.BranchInstruction;
import org.gjt.jclasslib.bytecode.ImmediateByteInstruction;
import org.gjt.jclasslib.bytecode.ImmediateIntInstruction;
import org.gjt.jclasslib.bytecode.ImmediateShortInstruction;
import org.gjt.jclasslib.bytecode.IncrementInstruction;
import org.gjt.jclasslib.bytecode.InvokeInterfaceInstruction;
import org.gjt.jclasslib.bytecode.LookupSwitchInstruction;
import org.gjt.jclasslib.bytecode.MatchOffsetPair;
import org.gjt.jclasslib.bytecode.MultianewarrayInstruction;
import org.gjt.jclasslib.bytecode.Opcodes;
import org.gjt.jclasslib.bytecode.OpcodesUtil;
import org.gjt.jclasslib.bytecode.TableSwitchInstruction;
import org.gjt.jclasslib.io.ByteCodeReader;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;

import ee.ioc.cs.jbe.browser.BrowserTreeNode;


public class CodeGenerator {

	private String getBranchInstr(AbstractInstruction instruction,
			ClassFile classFile, HashMap hm) {
		int branchTarget = ((BranchInstruction) instruction).getBranchOffset();
		AbstractInstruction targetInstruction = (AbstractInstruction) hm
				.get(new Integer(branchTarget));
		String instrName = instruction.getOpcodeVerbose();
		return instrName + " "
				+ Integer.toString(targetInstruction.getOffset() + 1);
	}

	private String getIntInstr(AbstractInstruction instruction,
			ClassFile classFile) {
		int immInt = ((ImmediateIntInstruction) instruction).getImmediateInt();
		String instrName = instruction.getOpcodeVerbose();
		return instrName + " " + Integer.toString(immInt);
	}

	private String getByteInstr(AbstractInstruction instruction,
			ClassFile classFile) {
		int immByte = ((ImmediateByteInstruction) instruction)
				.getImmediateByte();

		String instrArg = "";
		if (instruction.getOpcode() == Opcodes.OPCODE_LDC) {
			try {
				if (getConstType(classFile.getConstantPoolEntry(immByte).getTagVerbose()).equals("S")) {
					instrArg = "\""+classFile.getConstantPoolEntryName(immByte)+"\"";
				} else {
					instrArg = classFile.getConstantPoolEntryName(immByte);
				}

			} catch (InvalidByteCodeException e) {
				e.printStackTrace();
			}
		} else if (instruction.getOpcode() == Opcodes.OPCODE_NEWARRAY) {
			instrArg = OpcodesUtil.getArrayTypeVerbose(immByte);
		} else {
			instrArg = Integer.toString(immByte);
            if (instruction instanceof IncrementInstruction) {
            	instrArg += " " + ((IncrementInstruction)instruction).getIncrementConst();

            }
		}
		String instrName = instruction.getOpcodeVerbose();
		return instrName + " " + instrArg;
	}

	private String getLookupSwitchInstruction(AbstractInstruction instruction,
			HashMap hm) {
		List matchOffsetPairs = ((LookupSwitchInstruction) instruction)
				.getMatchOffsetPairs();
		int matchOffsetPairsCount = matchOffsetPairs.size();

		String instrName = ((LookupSwitchInstruction) instruction)
				.getOpcodeVerbose();
		StringBuffer instrArg = new StringBuffer(instrName);
		instrArg.append("\n");

		AbstractInstruction targetInstruction;
		MatchOffsetPair matchOffsetPairEntry;
		for (int i = 0; i < matchOffsetPairsCount; i++) {
			matchOffsetPairEntry = (MatchOffsetPair) matchOffsetPairs.get(i);
			targetInstruction = (AbstractInstruction) hm.get(new Integer(
					matchOffsetPairEntry.getOffset()));
			instrArg.append("  ")
					.append(matchOffsetPairEntry.getMatch() + ": ").append(
							targetInstruction.getOffset() + 1).append("\n");

		}
		targetInstruction = (AbstractInstruction) hm.get(new Integer(
				((LookupSwitchInstruction) instruction).getDefaultOffset()));
		instrArg.append("  default: ")
				.append(targetInstruction.getOffset() + 1);

		return instrArg.toString();

	}

	private String getTableSwitchInstr(AbstractInstruction instruction,
			HashMap hm) {
		int lowByte = ((TableSwitchInstruction) instruction).getLowByte();
		int highByte = ((TableSwitchInstruction) instruction).getHighByte();
		int[] jumpOffsets = ((TableSwitchInstruction) instruction)
				.getJumpOffsets();
		String instrName = ((TableSwitchInstruction) instruction)
				.getOpcodeVerbose();
		StringBuffer instrArg = new StringBuffer(instrName + " " + lowByte);
		instrArg.append("\n");

		AbstractInstruction targetInstruction;

		for (int i = 0; i <= highByte - lowByte; i++) {
			targetInstruction = (AbstractInstruction) hm.get(new Integer(
					jumpOffsets[i]));
			instrArg.append("  ").append(targetInstruction.getOffset() + 1)
					.append("\n");
		}
		targetInstruction = (AbstractInstruction) hm.get(new Integer(
				((TableSwitchInstruction) instruction).getDefaultOffset()));
		instrArg.append("  default: ")
				.append(targetInstruction.getOffset() + 1);
		return instrArg.toString();
	}

	private String getShortInstr(AbstractInstruction instruction,
			ClassFile classFile) {
		int immShort = ((ImmediateShortInstruction) instruction)
				.getImmediateShort();

		String instrArg = "";
		if (instruction.getOpcode() == Opcodes.OPCODE_LDC_W) {
			try {
				if (getConstType(classFile.getConstantPoolEntry(immShort).getTagVerbose()).equals("S")) {
					instrArg = "\""+classFile.getConstantPoolEntryName(immShort)+"\"";
				} else {
					instrArg = classFile.getConstantPoolEntryName(immShort);
				}

			} catch (InvalidByteCodeException e) {
				e.printStackTrace();
			}
		} else if (instruction.getOpcode() == Opcodes.OPCODE_LDC2_W) {
			try {
				instrArg = classFile.getConstantPoolEntryName(immShort);

			} catch (InvalidByteCodeException e) {
				e.printStackTrace();
			}
		} else if (instruction instanceof MultianewarrayInstruction) {
			int dimension = ((MultianewarrayInstruction) instruction)
					.getDimensions();
			try {
				instrArg = classFile.getConstantPoolEntryName(immShort) + " "
						+ Integer.toString(dimension);
			} catch (InvalidByteCodeException e) {
				e.printStackTrace();
			}

		} else if (instruction.getOpcode() != Opcodes.OPCODE_SIPUSH) {
			try {
				instrArg = classFile.getConstantPoolEntryName(immShort);
			} catch (InvalidByteCodeException e) {
				e.printStackTrace();
			}
			if (instruction instanceof InvokeInterfaceInstruction) {
				instrArg += " " + ((InvokeInterfaceInstruction)instruction).getCount();
			}

		} else {
			instrArg = Integer.toString(immShort);
            if (instruction instanceof IncrementInstruction) {
            	instrArg += " " + ((IncrementInstruction)instruction).getIncrementConst();

            }
		}
		String instrName = instruction.getOpcodeVerbose();
		return instrName + " " + instrArg;

	}

	private String getConstType(String constEntryName) {
		if (constEntryName.equals(CPInfo.CONSTANT_DOUBLE_VERBOSE)) {
			return "";
		} else if (constEntryName.equals(CPInfo.CONSTANT_FLOAT_VERBOSE)) {
			return "(float)";
		} else if (constEntryName.equals(CPInfo.CONSTANT_INTEGER_VERBOSE)) {
			return "";
		} else if (constEntryName.equals(CPInfo.CONSTANT_LONG_VERBOSE)) {
			return "(int)";
		} else if (constEntryName.equals(CPInfo.CONSTANT_STRING_VERBOSE)) {
			return "S";
		}
		return "";
	}

	public String getMethodName(TreePath treePath) {
		BrowserTreeNode x = (BrowserTreeNode) treePath.getLastPathComponent();
		BrowserTreeNode y = (BrowserTreeNode) x.getParent();
		return y.getUserObject().toString();
	}

	public int getMethodIndex(TreePath treePath) {
		BrowserTreeNode x = (BrowserTreeNode) treePath.getLastPathComponent();
		BrowserTreeNode y = (BrowserTreeNode) x.getParent();
		return y.getIndex();
	}

	public String makeMethod(byte[] code, ClassFile classFile) {
		StringBuffer methodBody = new StringBuffer();
		try {
			ArrayList instructions = (ArrayList) ByteCodeReader
					.readByteCode(code);
			HashMap<Integer, AbstractInstruction> hm = new HashMap<Integer, AbstractInstruction>();
			Iterator it = instructions.iterator();
			AbstractInstruction instruction;

			int oldOffset = 0;
			// put instructions in a hashmap for easy access,
			// to recompute branch offsets later
			for (int i = 0; i < instructions.size(); i++) {
				instruction = (AbstractInstruction) instructions.get(i);
				oldOffset = instruction.getOffset();
				instruction.setOffset(i);
				hm.put(new Integer(oldOffset), instruction);
				if (instruction instanceof BranchInstruction) {
					((BranchInstruction) instruction).setBranchOffset(oldOffset
							+ ((BranchInstruction) instruction)
									.getBranchOffset());

				} else if (instruction instanceof LookupSwitchInstruction) {
					List matchOffsetPairs = ((LookupSwitchInstruction) instruction)
							.getMatchOffsetPairs();
					int matchOffsetPairsCount = matchOffsetPairs.size();
					MatchOffsetPair matchOffsetPairEntry;
					for (int j = 0; j < matchOffsetPairsCount; j++) {
						matchOffsetPairEntry = (MatchOffsetPair) matchOffsetPairs
								.get(j);
						matchOffsetPairEntry.setOffset(oldOffset
								+ matchOffsetPairEntry.getOffset());
					}
					((LookupSwitchInstruction) instruction)
							.setDefaultOffset(((LookupSwitchInstruction) instruction)
									.getDefaultOffset()
									+ oldOffset);

				} else if (instruction instanceof TableSwitchInstruction) {
					int[] jumpOffsets = ((TableSwitchInstruction) instruction)
							.getJumpOffsets();
					for (int j = 0; j < jumpOffsets.length; j++) {
						jumpOffsets[j] += oldOffset;
					}
					((TableSwitchInstruction) instruction)
							.setDefaultOffset(((TableSwitchInstruction) instruction)
									.getDefaultOffset()
									+ oldOffset);
				}
			}
			while (it.hasNext()) {
				instruction = (AbstractInstruction) it.next();
				String instrString = "";
				if (instruction instanceof ImmediateByteInstruction) {
					instrString = getByteInstr(instruction, classFile);
				} else if (instruction instanceof ImmediateShortInstruction) {
					instrString = getShortInstr(instruction, classFile);

				} else if (instruction instanceof ImmediateIntInstruction) {
					instrString = getIntInstr(instruction, classFile);

				} else if (instruction instanceof BranchInstruction) {
					instrString = getBranchInstr(instruction, classFile, hm);

				} else if (instruction instanceof TableSwitchInstruction) {
					instrString = getTableSwitchInstr(instruction, hm);

				} else if (instruction instanceof LookupSwitchInstruction) {
					instrString = getLookupSwitchInstruction(instruction, hm);
				} else {
					instrString = instruction.getOpcodeVerbose();
				}

				methodBody.append(instrString);
				if (it.hasNext()) {
					methodBody.append("\n");
					
				}
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return methodBody.toString();
	}

}
