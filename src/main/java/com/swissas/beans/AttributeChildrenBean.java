package com.swissas.beans;

import java.util.Collections;
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
		this(element.attr(attributeKey));
	}
	
	public AttributeChildrenBean(String mainAttribute) {
		this.children = new TreeSet<>();
		setMainAttribute(mainAttribute);
	}
	
	public String getText() {
		return getMainAttribute();
	}
	
	public String getMainAttribute() {
		return this.mainAttribute;
	}
	
	public void setMainAttribute(String mainAttribute) {
		this.mainAttribute = mainAttribute;
	}
	
	public Set<AttributeChildrenBean> getChildren() {
		return Collections.unmodifiableSet(this.children);
	}
	
	public void addChildren(AttributeChildrenBean child) {
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
