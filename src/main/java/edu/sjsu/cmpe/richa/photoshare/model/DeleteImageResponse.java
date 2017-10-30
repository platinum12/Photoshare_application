package edu.sjsu.cmpe.richa.photoshare.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteImageResponse {
	
	String status;
	
	String filename;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String toString() {
		return "DeleteImageResponse [status=" + status + ", filename="
				+ filename + "]";
	}

}
