package com.swissas.toolwindow.adapters;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;

import com.intellij.ui.speedSearch.SpeedSearchSupply;
import com.intellij.ui.treeStructure.Tree;
import com.swissas.toolwindow.WarningContentTreeNode;


/**
 * The Warning Content Key Adapter
 *
 * @author Tavan Alain
 */
public class WarningContentKeyAdapter extends KeyAdapter {
	private final Tree tree;
	
	public WarningContentKeyAdapter(Tree tree) {
		this.tree = tree;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		boolean speedSearchVisible = Optional.ofNullable(SpeedSearchSupply.getSupply(this.tree)).map(SpeedSearchSupply::isPopupActive).orElse(false);
		WarningContentTreeNode selectedNode = (WarningContentTreeNode) this.tree
				.getLastSelectedPathComponent();
		if (selectedNode != null && !speedSearchVisible && (e.getKeyCode() == KeyEvent.VK_DELETE
		                             || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
			selectedNode.switchMark();
		}
	}
	
}
