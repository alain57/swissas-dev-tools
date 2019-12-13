package com.swissas.toolwindow.adapters;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
		WarningContentTreeNode selectedNode = (WarningContentTreeNode) this.tree
				.getLastSelectedPathComponent();
		if (selectedNode != null && (e.getKeyCode() == KeyEvent.VK_DELETE
		                             || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
			selectedNode.switchMark();
		}
	}
	
}
