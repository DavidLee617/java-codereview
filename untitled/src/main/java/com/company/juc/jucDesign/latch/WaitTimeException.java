package com.company.juc.jucDesign.latch;

public class WaitTimeException extends Exception{
    /**
	 *
	 */
	private static final long serialVersionUID = 9044593394165410678L;

	public WaitTimeException(String msg){
        super(msg);
    }
}
