package edu.sjsu.cmpe.richa.photoshare.service;

import java.io.File;

public interface UploadPhotoService {
	
	/**
	 * Upload using given File object, which can represent either a File or Directory.
	 * If Directory is specified, all files (1st hierarchy) will be uploaded.
	 * If File is specified, just that file will be uploaded.
	 *
	 * @param uploadFile File object representing either a 'File' or 'Directory'.
	 */
	public void uploadPhotoFileOrDirectory(File uploadFile) throws Exception;

}
