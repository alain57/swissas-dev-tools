package com.swissas.actions_on_save;

import com.intellij.openapi.application.RunResult;

/**
 * Composite of {@link com.intellij.openapi.application.Result} and {@link RunResult}.
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */
public class Result<T> {
	
	public enum ResultCode {
		OK
	}
	
	private final T result;
	
	Result(T result) {
		this.result = result;
	}
	
	Result(RunResult<T> result) {
		this.result = result.getResultObject();
	}
	
	public T getResult() {
		return this.result;
	}
	
	@Override
	public String toString() {
		return this.result.toString();
	}
	
}