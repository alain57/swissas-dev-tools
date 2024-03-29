package com.swissas.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * A simple utility class
 *
 * @author Tavan Alain
 */

public class ImageUtility {
	private static final Logger       LOGGER = Logger.getInstance("Swiss-as");
	private static       ImageUtility instance;
	
	private ImageUtility(){
		
	}
	
	public static ImageUtility getInstance() {
		if(instance == null){
			instance = new ImageUtility();
		}
		return instance;
	}
	
	public Image getImageFromClipboard() {
		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard()
		                                   .getContents(null);
		return getImageFromTransferable(transferable);
	}
	
	@Nullable
	public Image getImageFromTransferable(Transferable transferable) {
		try {
			return transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor) 
			       ? (Image)transferable.getTransferData(DataFlavor.imageFlavor) 
			       : null;
		} catch (UnsupportedFlavorException  | IOException e) {
			LOGGER.error(e);
			return null;
		}
	}

	public Image iconToImage(Icon icon) {
		Image result;
		if (icon instanceof ImageIcon) {
			result = ((ImageIcon)icon).getImage();
		} else {
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			BufferedImage image = gc.createCompatibleImage(w, h);
			Graphics2D g = image.createGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
			result = image;
		}
		return result;
	}
	
	public String imageToBase64Jpeg(ImageIcon imageIcon) {
		BufferedImage image = new BufferedImage(imageIcon.getIconWidth(),
		                                        imageIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
		String imageString = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Graphics g = image.createGraphics();
		imageIcon.paintIcon(null, g, 0,0);
		try {
			ImageIO.write(image, "jpg", bos);
			g.dispose();
			byte[] imageBytes = bos.toByteArray();
			imageString = Base64.getEncoder().encodeToString(imageBytes);
			
			bos.close();
		} catch (IOException e) {
			LOGGER.error(e);
		}
		return imageString;
	}
}
