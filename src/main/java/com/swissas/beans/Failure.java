package com.swissas.beans;

public class Failure {
	String id;
	String name;
	String className;
	String error;
	String state;
	
	/**
	 * @return The value of the id property
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Sets the given value to the id property
	 *
	 * @param id The value to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return The value of the error property
	 */
	public String getError() {
		return this.error;
	}
	
	/**
	 * Sets the given value to the error property
	 *
	 * @param error The value to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
