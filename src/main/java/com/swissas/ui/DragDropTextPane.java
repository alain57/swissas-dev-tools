package com.swissas.ui;

import java.awt.Image;
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
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static javax.swing.text.StyleConstants.IconAttribute;

/**
 * A TextPane component supporting drag and drop and returns the images that were added to it
 *
 * @author Tavan Alain
 */
public class DragDropTextPane extends JTextPane implements DropTargetListener, DataProvider {
	private static final Logger               LOGGER = Logger.getInstance("Swiss-as");
	
	public DragDropTextPane() {
		new DropTarget(this, this);
		setDragEnabled(true);
	}
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		//Method isn't needed but required bw the DropTargetListener
	}
	
	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		//Method isn't needed but required bw the DropTargetListener
	}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		//Method isn't needed but required bw the DropTargetListener
	}
	
	@Override
	public void dragExit(DropTargetEvent dte) {
		//Method isn't needed but required bw the DropTargetListener
	}
	
	@Override
	public void drop(DropTargetDropEvent dropTargetDropEvent) {
		Transferable transferable = dropTargetDropEvent.getTransferable();
		for (DataFlavor d : transferable.getTransferDataFlavors()) {
			dropTargetDropEvent
					.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			try {
				((List<File>) transferable.getTransferData(d)).forEach(this::insertIfImage);
			} catch (UnsupportedFlavorException | IOException e) {
				LOGGER.error(e);
			}
		}
		dropTargetDropEvent.getDropTargetContext().dropComplete(true);
	}
	
	private void insertIfImage(File file) {
		String mimetype = new MimetypesFileTypeMap().getContentType(file);
		String type = mimetype.split("/")[0];
		if ("image".equals(type)) {
			try {
				insertIcon(new ImageIcon(ImageIO.read(file)));
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}
	
	public List<ImageIcon> getImages() {
		List<ImageIcon> result = new ArrayList<>();
		StyledDocument doc = getStyledDocument();
		for (int i = 0; i < doc.getLength(); i++) {
			Element element = doc.getCharacterElement(i);
			if ("icon".equals(element.getName())) {
				result.add((ImageIcon) element.getAttributes().getAttribute(IconAttribute));
			}
		}
		return result;
	}
	
	@Nullable
	@Override
	public Object getData(@NotNull String dataId) {
		if (PlatformDataKeys.PASTE_PROVIDER.is(dataId)) {
			return this;
		}
		return null;
	}
	
	public void pastePicture() {
		Transferable transferable = CopyPasteManager.getInstance().getContents();
		if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			ImageIcon imageIcon;
			try {
				imageIcon = new ImageIcon(
						(Image) transferable.getTransferData(DataFlavor.imageFlavor));
				insertIcon(imageIcon);
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}
