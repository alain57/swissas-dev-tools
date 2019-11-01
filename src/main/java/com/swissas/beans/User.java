package com.swissas.beans;

import java.awt.Image;
import java.io.Serializable;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * simple bean for users
 *
 * @author TALA
 */

public class User implements Serializable {
	
	private static final ResourceBundle URL_BUNDLE = ResourceBundle.getBundle("urls");
	private static final String STAFF_PIC_FOLDER = URL_BUNDLE.getString("url.staff.pics");
	
	private String lc;
	private String infos;
	
	private transient ImageIcon picture;
	
	public User(){
		
	}
	
	public User(String lc, String infos){
		setLc(lc);
		setInfos(infos);
		readPicture();
	}

	public void setLc(String lc) {
		this.lc = lc;
	}
	
	public void setInfos(String infos) {
		this.infos = infos;
	}

	private void readPicture(){
		if(this.lc != null && !this.lc.isEmpty()){
			try {
				URL url = new URL(STAFF_PIC_FOLDER + this.lc + ".PNG");
				Image image = ImageIO.read(url);
				this.picture = new ImageIcon(image);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public ImageIcon getPicture() {
		return this.picture;
	}

	public String getLc() {
		return this.lc;
	}

	public String getInfos() {
		return this.infos;
	}
	
	public boolean hasTextInInfos(String text){
		return this.infos != null && this.infos.contains(text);
	}

}
