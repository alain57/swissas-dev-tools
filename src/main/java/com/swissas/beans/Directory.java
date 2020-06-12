package com.swissas.beans;

import java.util.Objects;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;

public class Directory extends AttributeChildrenBean{
	
	private String pathFromRootToName;
	
	public Directory(String name, String pathFromRootToName) {
		super(name);
		this.pathFromRootToName = pathFromRootToName;
	}
	
	
	public String getPathFromRootToName() {
		return this.pathFromRootToName;
	}
	
	public void setPathFromRootToName(String pathFromRootToName) {
		this.pathFromRootToName = pathFromRootToName;
	}
	
	@Override
	public int compareTo(@NotNull AttributeChildrenBean o) {
		if(o instanceof File) {
			return -1;
		}
		Directory otherDir = (Directory)o;
		return new CompareToBuilder()
				.append(getPathFromRootToName(), otherDir.getPathFromRootToName())
				.append(getMainAttribute(), otherDir.getMainAttribute())
				.toComparison();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		Directory directory = (Directory) o;
		return Objects.equals(this.pathFromRootToName, directory.pathFromRootToName);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.pathFromRootToName);
	}
}
