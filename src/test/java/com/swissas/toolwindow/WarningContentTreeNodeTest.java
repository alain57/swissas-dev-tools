package com.swissas.toolwindow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarningContentTreeNodeTest {
	
	private static WarningContentTreeNode parentWithTwoLeaves() {
		WarningContentTreeNode parent = new WarningContentTreeNode("parent");
		parent.add(new WarningContentTreeNode("one"));
		parent.add(new WarningContentTreeNode("two"));
		return parent;
	}
	
	@Test
	void newNodeIsADirectoryThatIsNotMarked() {
		WarningContentTreeNode node = new WarningContentTreeNode("node");
		
		assertThat(node.getTreeType()).isEqualTo(WarningContentTreeNode.TreeType.DIRECTORY);
		assertThat(node.isMarked()).isFalse();
		assertThat(node.isMine()).isFalse();
		assertThat(node.isCritical()).isFalse();
	}
	
	@Test
	void unmarkedLeavesAreCounted() {
		assertThat(parentWithTwoLeaves().getUnmarkedCount()).isEqualTo(2);
	}
	
	@Test
	void markingAChildReducesTheUnmarkedCount() {
		WarningContentTreeNode parent = parentWithTwoLeaves();
		
		((WarningContentTreeNode) parent.getChildAt(0)).switchMark();
		
		assertThat(parent.getUnmarkedCount()).isEqualTo(1);
		assertThat(parent.isMarked()).isFalse();
	}
	
	@Test
	void parentIsMarkedWhenAllItsChildrenAre() {
		WarningContentTreeNode parent = parentWithTwoLeaves();
		
		((WarningContentTreeNode) parent.getChildAt(0)).switchMark();
		((WarningContentTreeNode) parent.getChildAt(1)).switchMark();
		
		assertThat(parent.isMarked()).isTrue();
		assertThat(parent.getUnmarkedCount()).isZero();
	}
	
	@Test
	void markingAParentMarksAllItsChildren() {
		WarningContentTreeNode parent = parentWithTwoLeaves();
		
		parent.switchMark();
		
		assertThat(((WarningContentTreeNode) parent.getChildAt(0)).isMarked()).isTrue();
		assertThat(((WarningContentTreeNode) parent.getChildAt(1)).isMarked()).isTrue();
		
		parent.switchMark();
		
		assertThat(parent.getUnmarkedCount()).isEqualTo(2);
	}
	
	@Test
	void nodeIsMineWhenOneOfItsChildrenIs() {
		WarningContentTreeNode parent = parentWithTwoLeaves();
		
		((WarningContentTreeNode) parent.getChildAt(1)).setMine(true);
		
		assertThat(parent.isMine()).isTrue();
		assertThat(((WarningContentTreeNode) parent.getChildAt(0)).isMine()).isFalse();
	}
	
	@Test
	void criticalFlagIsKept() {
		WarningContentTreeNode node = new WarningContentTreeNode("node");
		
		node.setCritical(true);
		
		assertThat(node.isCritical()).isTrue();
	}
}
