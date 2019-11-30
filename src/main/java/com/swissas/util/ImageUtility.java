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

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.intellij.util.Base64;

/**
 * A simple utility class
 *
 * @author Tavan Alain
 */

public class ImageUtility {
	
	private static ImageUtility INSTANCE;
	
	private ImageUtility(){
		
	}
	
	public static ImageUtility getInstance() {
		if(INSTANCE == null){
			INSTANCE = new ImageUtility();
		}
		return INSTANCE;
	}

	public ImageIcon getImageFromClipboard()
	{
		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
		{
			try
			{
				return new ImageIcon((Image)transferable.getTransferData(DataFlavor.imageFlavor));
			}
			catch (UnsupportedFlavorException | IOException e)
			{
				// handle this as desired
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println("getImageFromClipboard: That wasn't an image!");
		}
		return null;
	}

	public Image iconToImage(Icon icon) {
		if (icon instanceof ImageIcon) {
			return ((ImageIcon)icon).getImage();
		} else {
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			GraphicsEnvironment ge =
					GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			BufferedImage image = gc.createCompatibleImage(w, h);
			Graphics2D g = image.createGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
			return image;
		}
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
			imageString = Base64.encode(imageBytes);
			
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageString;
	}
}
