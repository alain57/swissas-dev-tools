package com.swissas.beans;

import java.awt.Image;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
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
	private static final int NAME_START_INDEX = 12;//HTML and body tags
	
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
				URL url = URI.create(STAFF_PIC_FOLDER + this.lc + ".PNG").toURL();
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
		int nameEnd = this.infos == null ? -1 : this.infos.indexOf("<br/>");
		if (nameEnd > NAME_START_INDEX) {
			return this.lc + " (" + this.infos.substring(NAME_START_INDEX, nameEnd) + ")";
		}
		return this.fullName == null || this.fullName.isBlank() ? this.lc : this.lc + " (" + this.fullName + ")";
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		User user = (User) o;
		return Objects.equals(this.lc, user.lc) && Objects.equals(this.team, user.team)
		       && Objects.equals(this.fullName, user.fullName) && Objects.equals(this.infos, user.infos);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.lc, this.team, this.fullName, this.infos);
	}

}
