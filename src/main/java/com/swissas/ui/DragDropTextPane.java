package com.swissas.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * A TextPane component supporting drag and drop and returns the images that were added to it
 * 
 * @author Tavan Alain
 */
public class DragDropTextPane extends JTextPane implements DropTargetListener {
	
	private final List<String> imagesStyles;
	private       int          imageNumber = 0;
	
	public DragDropTextPane() {
		new DropTarget(this, this);
		setDragEnabled(true);
		this.imagesStyles = new ArrayList<>();
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
				List<File> files = (List) transferable.getTransferData(d);
				for (File file : files) {
					insertIconCorrectWay(file);
				}
			} catch (UnsupportedFlavorException | BadLocationException | IOException e) {
				e.printStackTrace();
			}
		}
		dropTargetDropEvent.getDropTargetContext().dropComplete(true);
	}
	
	private void insertIconCorrectWay(File file) throws BadLocationException, IOException {
		String mimetype = new MimetypesFileTypeMap().getContentType(file);
		String type = mimetype.split("/")[0];
		String fileName = file.getName();
		if (type.equals("image") && !this.imagesStyles.contains(fileName)) {
			ImageIcon imageIcon = new ImageIcon(ImageIO.read(file));
			this.imageNumber++;
			Style style = addStyle(fileName, null);
			StyleConstants.setIcon(style, imageIcon);
			StyledDocument doc = getStyledDocument();
			doc.insertString(doc.getLength(), " ", style);
			this.imagesStyles.add(fileName);
		}
	}
	
	public List<ImageIcon> getImages() {
		List<ImageIcon> result = new ArrayList<>();
		DefaultStyledDocument document = (DefaultStyledDocument) getStyledDocument();
		for (String styleName : this.imagesStyles) {
			Style style = document.getStyle(styleName);
			Icon icon = StyleConstants.getIcon(style);
			if (icon instanceof ImageIcon) {
				result.add((ImageIcon) icon);
			}
		}
		return result;
	}
}
