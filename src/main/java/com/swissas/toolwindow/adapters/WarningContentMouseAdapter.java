package com.swissas.toolwindow.adapters;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.swissas.toolwindow.WarningContent;
import com.swissas.toolwindow.WarningContentTreeNode;
import com.swissas.util.ProjectUtil;

/**
 * The Warning Content Mouse Adapter
 *
 * @author Tavan Alain
 */
public class WarningContentMouseAdapter extends MouseAdapter {
	private final Tree    tree;
	private final Project project;
	private final WarningContent warningContent;
	
	public WarningContentMouseAdapter(Project project, Tree tree, WarningContent warningContent) {
		this.project = project;
		this.tree = tree;
		this.warningContent = warningContent;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		WarningContentTreeNode selectedNode = (WarningContentTreeNode) this.tree.getLastSelectedPathComponent();
		if(selectedNode == null){
			return;
		}
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			if(ProjectUtil.getInstance().isPreviewProject()) {
				String filePath;
				if(selectedNode.getTreeType().equals(WarningContentTreeNode.TreeType.MESSAGE)) {
					int line = Optional.ofNullable(this.tree.getSelectionPath())
							.map(TreePath::getPath)
							.map(this::getLineFromObjectArray).orElse(0);
					filePath = findFilePath((WarningContentTreeNode) selectedNode.getParent());
					openFileAtLineIfPossible(line, filePath);
				}
			}else {
				Messages.showWarningDialog("You only need to fix warnings on Preview. No need to check them on other release", "Warning");
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			displayMenuForSelectedNodeOnPosition(selectedNode, e.getX(), e.getY());
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			switchMark(selectedNode);
		}
	}
	
	private void switchMark(WarningContentTreeNode selectedNode){
		selectedNode.switchMark();
		((DefaultTreeModel)this.tree.getModel()).reload(selectedNode);
	}
	
	private void displayMenuForSelectedNodeOnPosition(WarningContentTreeNode selectedNode,
	                                                  int x, int y) {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem markDoneMenu = new JMenuItem(
				selectedNode.isMarked() ? "reset mark as done" : "mark as done");
		markDoneMenu.addActionListener(event -> switchMark(selectedNode));
		popupMenu.add(markDoneMenu);
		JMenuItem forceRefresh = new JMenuItem("Refresh Warnings");
		forceRefresh.addActionListener(e -> this.warningContent.refresh());
		popupMenu.add(forceRefresh);
		if(selectedNode.getTreeType().equals(WarningContentTreeNode.TreeType.MESSAGE)) {
			JMenuItem findSimilarMenu = new JMenuItem("Find similar");
			findSimilarMenu.addActionListener(event -> findSimilar(selectedNode));
			popupMenu.add(findSimilarMenu);
		}
		popupMenu.show(this.tree, x, y);
	}

	private void findSimilar(WarningContentTreeNode selectedNode) {
		var message = selectedNode.getUserObject().toString();
		message = message.substring(0, message.lastIndexOf(':'));
		this.warningContent.filterSimilar(message);
		
	}

	private int getLineFromObjectArray(Object[] path) {
		String[] split = path[path.length - 1].toString().split(":");
		return Integer.parseInt(split[split.length-1]) - 1;
	}
	
	
	private String findFilePath(WarningContentTreeNode fileNode) {
		return Stream.of(fileNode.getPath()).skip(1).map(WarningContentTreeNode.class::cast)
		             .map(DefaultMutableTreeNode::getUserObject)
		             .map(Object::toString).collect(Collectors.joining("/"));
		
	}
	private void openFileAtLineIfPossible(int line, String projectFilePath) {
		if(projectFilePath == null) {
			return;
		}
		if(this.project != null) {
			String filepath = this.project.getBasePath() + "/" + projectFilePath;
			
			VirtualFile file = VfsUtil
					.findFileByIoFile(new File(filepath), true);
			if (file == null) {
				JOptionPane.showMessageDialog(null, ResourceBundle.getBundle("texts").getString(
						"opening.following.file.is.not.working") + filepath,
				                              ResourceBundle.getBundle("texts").getString("error"),
				                              JOptionPane.ERROR_MESSAGE);
			} else {
				new OpenFileDescriptor(this.project, file, line, 0).navigate(true);
			}
		}
	}
}
