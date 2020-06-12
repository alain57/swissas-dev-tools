package com.swissas.toolwindow.adapters;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
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
	
	public WarningContentMouseAdapter(Project project, Tree tree) {
		this.project = project;
		this.tree = tree;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		WarningContentTreeNode selectedNode = (WarningContentTreeNode) this.tree
				.getLastSelectedPathComponent();
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			if(ProjectUtil.getInstance().isPreviewProject()) {
				int line = 0;
				String filePath = null;
				if(selectedNode.getTreeType().equals(WarningContentTreeNode.TreeType.Message)) {
					Object[] path = this.tree.getSelectionPath().getPath();
					line = getLineFromObjectArray(path);
					filePath = findFilePath((WarningContentTreeNode) selectedNode.getParent());
					openFileAtLineIfPossible(line, filePath);
				}
			}else {
				Messages.showWarningDialog("You only need to fix warnings on Preview. No need to check them on other release", "Warning");
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			if (selectedNode != null) {
				displayMenuForSelectedNodeOnPosition(selectedNode, e.getX(), e.getY());
			}
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
		JMenuItem markDone = new JMenuItem(
				selectedNode.isMarked() ? "reset mark as done" : "mark as done");
		markDone.addActionListener(event -> switchMark(selectedNode));
		popupMenu.add(markDone);
		popupMenu.show(this.tree, x, y);
	}
	
	private int getLineFromObjectArray(Object[] path) {
		return Integer.parseInt(path[path.length-1].toString().split(":")[1]) - 1;
	}
	
	
	private String findFilePath(WarningContentTreeNode fileNode) {
		return Stream.of(fileNode.getPath()).skip(1).map(WarningContentTreeNode.class::cast)
		             .map(DefaultMutableTreeNode::getUserObject)
		             .map(Object::toString).map(s -> s.substring(0, s.indexOf(" ("))).collect(Collectors.joining("/"));
		
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
