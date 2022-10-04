package com.swissas.beans;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BranchFailure {
	String jobs;
	String committers;
	List<Failure> failures;
	
	/**
	 * @return The value of the jobs property
	 */
	public String getJobs() {
		return jobs;
	}
	
	/**
	 * Sets the given value to the jobs property
	 *
	 * @param jobs The value to set
	 */
	public void setJobs(String jobs) {
		this.jobs = jobs;
	}
	
	/**
	 * @return The value of the committers property
	 */
	public String getCommitters() {
		return committers;
	}
	
	/**
	 * Sets the given value to the committers property
	 *
	 * @param committers The value to set
	 */
	public void setCommitters(String committers) {
		this.committers = committers;
	}
	
	/**
	 * @return The value of the failures property
	 */
	public List<Failure> getFailures() {
		return failures;
	}
	
	/**
	 * Sets the given value to the failures property
	 *
	 * @param failures The value to set
	 */
	public void setFailures(List<Failure> failures) {
		this.failures = failures;
	}
	
	public List<String> get4Lc() {
		return Arrays.stream(this.committers.split(";")).collect(Collectors.toList());
	}
}
