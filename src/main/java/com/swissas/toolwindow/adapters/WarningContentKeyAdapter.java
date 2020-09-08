package com.swissas.toolwindow.adapters;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;

import com.intellij.openapi.util.Key;
import com.intellij.ui.SpeedSearchBase;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.swissas.toolwindow.WarningContentTreeNode;


/**
 * The Warning Content Key Adapter
 *
 * @author Tavan Alain
 */
public class WarningContentKeyAdapter extends KeyAdapter {
	private final Tree tree;
	private static final Key SPEED_SEARCH_COMPONENT_MARKER = Key.findKeyByName("SPEED_SEARCH_COMPONENT_MARKER");//not nice but SpeedSearchSupply.SPEED_SEARCH_COMPONENT_MARKER is private :/
	
	public WarningContentKeyAdapter(Tree tree) {
		this.tree = tree;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		Boolean speedSearchVisible = Optional.ofNullable(this.tree.getClientProperty(SPEED_SEARCH_COMPONENT_MARKER))
				.map(TreeSpeedSearch.class::cast)
				.map(SpeedSearchBase::isPopupActive).orElse(false);
		WarningContentTreeNode selectedNode = (WarningContentTreeNode) this.tree
				.getLastSelectedPathComponent();
		if (selectedNode != null && !speedSearchVisible && (e.getKeyCode() == KeyEvent.VK_DELETE
		                             || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
			selectedNode.switchMark();
		}
	}
	
}
