/*
 *
 */
package ee.ioc.cs.jbe.browser.codeedit;

import java.util.ArrayList;

public class JAsmParseException extends Exception {

	private static final long serialVersionUID = -1932490682546150245L;
	public static final String SYNTAX_ERROR = "Syntax error";
    public static final String ARG_TYPE_ERROR = "Argument type error";
    public static final String ARG_TYPE_ERROR_LDC2_W = "Wrong arguments for LDC2_W instruction";
    public static final String MISSING_ARGUMENTS = "Arguments missing";
    public static final String BYTE_REQUIRED = "Instruction requires an argument of type byte";
    public static final String INT_REQUIRED = "Instruction requires an argument of type int";
    public static final String SHORT_REQUIRED = "Instruction requires an argument of type short";
    public static final String JUMP_OUT_OF_DOMAIN = "Jump target outside the domain of the code";
	public static final String BAD_LOOKUP_ARGUMENT = "Bad lookup switch argument";
	public static final String WHITESPACE_ERROR = "Only arguments to tableswitch and lookupswitch instructions should begin with a whitespace";
    
    private ArrayList<Error> errors = new ArrayList<Error>();
    
    private class Error {
        private String error;
        private int line;
        private String instruction;
        public Error(String t, String s,  int i) {
            error = t;
            instruction = s;
            line = i+1;
        }
        public String toString() {
            return "Error: "+error +" at line "+ line+ ", instruction \""+ instruction+"\"";
            
        }
    }
    
    void addError (String errorType, String instruction, int line) {
        errors.add(new Error(errorType, instruction,  line));
        
    }

    public int errorCount() {
        return errors.size();
    }

    public String getErrorVerbose() {
        StringBuffer errorVerbose = new StringBuffer();
        for (int i=0; i < errors.size(); i++){
            errorVerbose.append(errors.get(i).toString()).append("\n");
        }
        return errorVerbose.toString();
    }
    
}
