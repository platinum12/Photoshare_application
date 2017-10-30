package edu.sjsu.cmpe.richa.photoshare.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO defining the User object 
 * 
 * @author Richa
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
	private String id;
	
	private String username;
	
	private String firstName;
	
	private String lastName;
	
	private String emailId;
	
	private String password;
	
	private String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", firstName="
				+ firstName + ", lastName=" + lastName + ", emailId=" + emailId
				+ ", password=" + password + ", status=" + status + "]";
	}
}
