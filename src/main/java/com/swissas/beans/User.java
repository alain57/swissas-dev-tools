package com.swissas.beans;

import java.io.Serializable;

/**
 * simple bean for users
 *
 * @author TALA
 */

public class User implements Serializable {

	private static final int HTML_FULL_NAME_POSITION = 0;
	private static final int HTML_JOB_POSITION = 2;
	private static final int HTML_LC_POSITION = 4;
	private static final int HTML_TEL_POSITION = 5;
	private static final int HTML_MAIL_POSITION = 7;
	private static final int HTML_TEAM_POSITION = 8;
	
	
	private String fullName;
	private String lc;
	private String job;
	private String tel;
	private String mail;
	private String team;
	
	public User(){
		this.fullName ="";
		this.lc = "";
		this.job = "";
		this.tel = "";
		this.mail = "";
		this.team = "";
	}
	
	public User(String[] htmlValues){
		setFullName(htmlValues[HTML_FULL_NAME_POSITION].trim());
		setJob(htmlValues[HTML_JOB_POSITION].split(":")[1].trim());
		setLc(htmlValues[HTML_LC_POSITION].split(":")[1].trim());
		setTel(htmlValues[HTML_TEL_POSITION].split(":")[1].trim());
		setMail(htmlValues[HTML_MAIL_POSITION].split(":")[1].trim());
		setTeam(htmlValues[HTML_TEAM_POSITION].split(":")[1].trim());
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setLc(String lc) {
		this.lc = lc;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getLc() {
		return this.lc;
	}

	public String getJob() {
		return this.job;
	}

	public String getTel() {
		return this.tel;
	}

	public String getMail() {
		return this.mail;
	}

	public String getTeam() {
		return this.team;
	}

	public String getFullName() {
		return this.fullName;
	}

	@Override
	public String toString() {
		return "User{" +
				"fullName='" + this.fullName + '\'' +
				", lc='" + this.lc + '\'' +
				", job='" + this.job + '\'' +
				", tel='" + this.tel + '\'' +
				", mail='" + this.mail + '\'' +
				", team='" + this.team + '\'' +
				'}';
	}
}
