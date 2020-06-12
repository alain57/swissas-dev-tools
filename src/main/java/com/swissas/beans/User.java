package com.swissas.beans;

import java.awt.Image;
import java.io.Serializable;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * A User Bean class
 *
 * @author Tavan Alain
 */

public class User implements Serializable {
	
	private static final String STAFF_PIC_FOLDER = ResourceBundle.getBundle("urls").getString("url.staff.pics");
	private static final int NAME_START_INDEX = 12;//html and body tags
	
	private String lc;
	private String team;
	private String infos;
	private String fullName;
	
	private transient ImageIcon picture;
	
	public User(){
		
	}
	
	public User(String lc, String team, String fullName, String infos){
		setLc(lc);
		setTeam(team);
		setInfos(infos);
		setFullName(fullName);
	}

	public void setLc(String lc) {
		this.lc = lc.toUpperCase();
	}
	
	public String getFullName() {
		return this.fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public void setInfos(String infos) {
		this.infos = infos;
	}
	
	public void setTeam(String team){
		this.team = team;
	}

	private void readPicture(){
		if(this.lc != null && !this.lc.isEmpty()){
			try {
				URL url = new URL(STAFF_PIC_FOLDER + this.lc + ".PNG");
				Image image = ImageIO.read(url);
				this.picture = new ImageIcon(image);
			}catch (Exception e){
				this.picture = null;
			}
		}
	}
	
	public ImageIcon getPicture() {
		if(this.picture == null) {
			readPicture();
		}
		return this.picture;
	}

	public String getLc() {
		return this.lc;
	}
	
	public String getTeam() {
		return this.team;
	}

	public String getInfos() {
		return this.infos;
	}
	
	public boolean isInTeam(String team){
		return getTeam() != null && getTeam().equals(team);
	}
	
	public String getLCAndName(){
		return this.lc + " (" + this.infos.substring(NAME_START_INDEX, this.infos.indexOf("<br/>")) + ")";
	}

}
