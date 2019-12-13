package com.swissas.toolwindow.adapters;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
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
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2
		    && this.tree.getSelectionPath() != null
		    && this.tree.getSelectionPath().getPath().length > 3) {
			if(ProjectUtil.getInstance().isPreviewProject()) {
				Object[] path = this.tree.getSelectionPath().getPath();
				int line = getLineFromObjectArray(path);
				openFileAtLineIfPossible(line, path);
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
		int line = 0;
		if (path.length > 3) {
			String lineAndDesc = path[3].toString();
			line = Integer.parseInt(
					lineAndDesc.substring(5, lineAndDesc.indexOf(" :"))) - 1;
		}
		return line;
	}
	
	private void openFileAtLineIfPossible(int line, Object[] objectArray) {
		if(this.project != null) {
			String pathAndAmountOfErrors = objectArray[2].toString();
			String filepath = this.project.getBasePath() + "/" + pathAndAmountOfErrors
					.substring(0, pathAndAmountOfErrors.indexOf(" ("));
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
