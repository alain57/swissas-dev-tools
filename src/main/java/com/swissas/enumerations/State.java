package com.swissas.enumerations;

import java.awt.Color;

public enum State {
	NOT_ASSIGNED("Not assigned yet",Color.RED),
	CHECKING("Looking for cause",Color.blue),
	ALREADY_FIXED("Fixed, should be OK with next run",new Color(34,139,34));
	
	private final String desc;
	private final Color color;
	
	State(String desc,Color color){
		this.desc=desc;
		this.color=color;
	}
	public String getDescription(){
		return this.desc;
	}
	
}
