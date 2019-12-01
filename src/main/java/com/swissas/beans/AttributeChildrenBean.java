package com.swissas.beans;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Node;

/**
 * Simple Abstract class to prevent code repetition within multiple beans
 * @author Tavan Alain
 */
public abstract class AttributeChildrenBean implements Comparable<AttributeChildrenBean>{
	private String                           mainAttribute;
	private final Set<AttributeChildrenBean> children;
	
	public AttributeChildrenBean(Node element, String attributeKey){
		this.children = new TreeSet<>();
		setMainAttribute(element.attr(attributeKey));
	}
	
	public String getMainAttribute() {
		return this.mainAttribute;
	}
	
	public void setMainAttribute(String mainAttribute) {
		this.mainAttribute = mainAttribute;
	}
	
	protected Set<AttributeChildrenBean> getChildren() {
		return this.children;
	}
	
	protected void addChildren(AttributeChildrenBean child) {
		this.children.add(child);
	}
	
	@Override
	public int compareTo(@NotNull AttributeChildrenBean o) {
		return new CompareToBuilder()
				.append(this.mainAttribute, o.getMainAttribute())
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
		AttributeChildrenBean bean = (AttributeChildrenBean) o;
		return compareTo(bean) == 0;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.mainAttribute);
	}
}
