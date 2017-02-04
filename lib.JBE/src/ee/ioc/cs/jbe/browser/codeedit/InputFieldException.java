/*
 *
 */
package ee.ioc.cs.jbe.browser.codeedit;

public class InputFieldException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3644925700917443433L;
	String inputString;
    String errorSource;
    public InputFieldException(String input, String error) {
        inputString = input;
        errorSource = error;
    }
    public String getErrorVerbose() {
        return "Error: "+ "wrong type for input \""+ inputString +"\" at field "+errorSource;
    }

}
