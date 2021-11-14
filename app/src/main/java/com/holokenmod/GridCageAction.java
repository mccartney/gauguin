package com.holokenmod;

public enum GridCageAction {
	
	ACTION_NONE(""),
	ACTION_ADD("+"),
	ACTION_SUBTRACT("-"),
	ACTION_MULTIPLY("*"),
	ACTION_DIVIDE("/");
	
	private final String displayName;
	
	GridCageAction(String displayName) {
		this.displayName = displayName;
	}
	
	public String getOperationDisplayName() {
		return displayName;
	}
}