package com.company.juc.jucDesign.activeobject.general;

public class IllegalActiveMethod extends Exception{

	/**
	 *
	 */
	private static final long serialVersionUID = -7308036283840077263L;
    public IllegalActiveMethod(String msg){
        super(msg);
    }
}