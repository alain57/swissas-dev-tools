package com.swissas.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.swissas.util.ImageUtility;

/**
 * A TextPane component supporting drag and drop and returns the images that were added to it
 *
 * @author Tavan Alain
 */
public class DragDropTextPane extends JTextPane implements DropTargetListener {
	
	public DragDropTextPane() {
		new DropTarget(this, this);
		setDragEnabled(true);
		getActionMap().put(DefaultEditorKit.pasteAction, new DrapDropPasteAction());
	}
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		
	}
	
	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		
	}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		
	}
	
	@Override
	public void dragExit(DropTargetEvent dte) {
		
	}
	
	@Override
	public void drop(DropTargetDropEvent dropTargetDropEvent) {
		System.out.println("Drop event");
		Transferable transferable = dropTargetDropEvent.getTransferable();
		for (DataFlavor d : transferable.getTransferDataFlavors()) {
			dropTargetDropEvent
					.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			try {
				((List<File>) transferable.getTransferData(d)).forEach(this::insertIfImage);
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
		}
		dropTargetDropEvent.getDropTargetContext().dropComplete(true);
	}
	
	private void insertIfImage(File file){
		String mimetype = new MimetypesFileTypeMap().getContentType(file);
		String type = mimetype.split("/")[0];
		if (type.equals("image")) {
			try {
				insertIcon(new ImageIcon(ImageIO.read(file)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<ImageIcon> getImages() {
		List<ImageIcon> result = new ArrayList<>();
		StyledDocument doc = getStyledDocument();
		for(int i = 0; i < doc.getLength() ; i++){
			Element element = doc.getCharacterElement(i);
			if ("icon".equals(element.getName())) {
				result.add((ImageIcon)element.getAttributes().getAttribute(StyleConstants.CharacterConstants.IconAttribute));
			}
		}
		return result;
	}
	
	/**
	 * TODO remove once I find out why the copy/paste of picture is not working directly in IntelliJ :(
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		JFrame frame = new JFrame();
		DragDropTextPane comp = new DragDropTextPane();
		comp.setPreferredSize(new Dimension(500, 400));
		JPanel p = new JPanel();
		p.add(comp);
		frame.add(p);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	class DrapDropPasteAction extends DefaultEditorKit.PasteAction{
		@Override
		public void actionPerformed(ActionEvent e) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if(clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)){
				ImageIcon imageFromClipboard = ImageUtility.getInstance().getImageFromClipboard();
				if(imageFromClipboard != null) {
					insertIcon(imageFromClipboard);
				}
			}else {
				super.actionPerformed(e);
			}
		}
	}
}
