package com.swissas.util;

import java.awt.Color;
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
	private static final ImageUtility INSTANCE = new ImageUtility();
	
	private ImageUtility(){
		
	}
	
	public static ImageUtility getInstance() {
		return INSTANCE;
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
		Graphics g = image.createGraphics();
		try {
			//jpeg has no transparency, without a background the transparent pixels would be black
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			imageIcon.paintIcon(null, g, 0,0);
		} finally {
			g.dispose();
		}
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			ImageIO.write(image, "jpg", bos);
			return Base64.getEncoder().encodeToString(bos.toByteArray());
		} catch (IOException e) {
			LOGGER.error(e);
			return null;
		}
	}
}
