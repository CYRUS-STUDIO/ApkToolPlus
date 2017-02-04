package org.apache.bcel.verifier;

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

/**
 * A VerificationResult is what a PassVerifier returns
 * after verifying.
 *
 * @version $Id: VerificationResult.java,v 1.1 2005/12/16 14:11:30 andos Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 *
 */
public class VerificationResult{

	/**
	 * Constant to indicate verification has not been tried yet.
	 * This happens if some earlier verification pass did not return VERIFIED_OK.
	 */
	public static final int VERIFIED_NOTYET = 0;
	/** Constant to indicate verification was passed. */
	public static final int VERIFIED_OK = 1;
	/** Constant to indicate verfication failed. */
	public static final int VERIFIED_REJECTED = 2;

	/**
	 * This string is the canonical message for verifications that have not been tried yet.
	 * This happens if some earlier verification pass did not return VERIFIED_OK.
	 */
	private static final String VERIFIED_NOTYET_MSG = "Not yet verified.";
	/** This string is the canonical message for passed verification passes. */
	private static final String VERIFIED_OK_MSG = "Passed verification.";

	/**
	 * Canonical VerificationResult for not-yet-tried verifications.
	 * This happens if some earlier verification pass did not return VERIFIED_OK.
	 */
	public static final VerificationResult VR_NOTYET = new VerificationResult(VERIFIED_NOTYET, VERIFIED_NOTYET_MSG);
	/** Canonical VerificationResult for passed verifications. */
	public static final VerificationResult VR_OK = new VerificationResult(VERIFIED_OK, VERIFIED_OK_MSG);

	/** The numeric status. */
	private int numeric;

	/** The detailed message. */
	private String detailMessage;

	/** The usual constructor. */
	public VerificationResult(int status, String message){
		numeric = status;
		detailMessage = message;
	}

	/** Returns one one the VERIFIED_OK, VERIFIED_NOTYET, VERIFIED_REJECTED constants. */
	public int getStatus(){
		return numeric;
	}

	/** Returns a detailed message. */
	public String getMessage(){
		return detailMessage;
	}

	/**
	 * Returns if two VerificationResult instances are equal.
	 */ 
	public boolean equals(Object o){
		if (! (o instanceof VerificationResult)) return false;
		VerificationResult other = (VerificationResult) o;
		return ((other.numeric == this.numeric) && (other.detailMessage.equals(this.detailMessage)));
	}
	
	/**
	 * Returns a String representation of the VerificationResult.
	 */
	public String toString(){
		String ret="";
		if (numeric == VERIFIED_NOTYET)   ret = "VERIFIED_NOTYET";
		if (numeric == VERIFIED_OK)       ret = "VERIFIED_OK";
		if (numeric == VERIFIED_REJECTED) ret = "VERIFIED_REJECTED";
		ret+="\n"+detailMessage+"\n";
		return ret;
	}
}
