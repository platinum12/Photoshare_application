package edu.sjsu.cmpe.richa.photoshare.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UploadPhotoServiceImpl implements UploadPhotoService {
	
	private final Logger logger = LoggerFactory.getLogger(edu.sjsu.cmpe.richa.photoshare.service.UploadPhotoServiceImpl.class);
	
	@Value( "${photoshare.webserver.base.url}" )
	private String baseUrl;
	
	@Value( "${photoshare.webserver.photoUpload.resource}" )
	private String photoUploadResource;
	
	@Value( "${photoshare.webserver.service.user}")
	private String serviceUser;
	
	@Value( "${photoshare.webserver.service.pwd}")
	private String servicePwd;

	public void uploadPhotoFileOrDirectory(File uploadFile) throws Exception {
		// If the file resource doesn't exists, exit.
		if (!uploadFile.exists()) {
			logger.error("Specified File resource doesn't exist : {}", uploadFile.getPath());
			throw new Exception("File doesn't exists");
		}
		
		// If specified resource is Directory, need to upload files from it.
		if (uploadFile.isDirectory()) {
			logger.info("Will upload files from Directory : {}", uploadFile.getName());
			File[] arrFiles = uploadFile.listFiles();
			uploadFileToServer(arrFiles);
		} else if (uploadFile.isFile()) { // else just upload the single file.
			logger.info("Will upload file : {} " , uploadFile.getName());
			File[] arrFiles = new File[] { uploadFile };
			uploadFileToServer(arrFiles);
		}

	}
	
	/**
	 * Uploads provided files to Server.
	 * @param arrFiles
	 */
	private void uploadFileToServer(File[] arrFiles) {
		if(arrFiles.length <= 0) {
			System.err.println("No Files to Upload! Existing!");
			System.exit(1);
		}
		
		// Iterate over the files and upload one at a time.
		for (File file : arrFiles) {
			uploadFileToServer(file);
		}
	}
	
	/**
	 * Uploads the provided single file to Server.
	 * @param file
	 */
	@SuppressWarnings({ "deprecation", "resource" })
	private void uploadFileToServer(File file) {
		String uploadUrl = this.baseUrl + this.photoUploadResource;
		byte[] fileBuff = new byte[(int) file.length()]; // buffer to store file
		// bytes.
		FileInputStream fis = null;
		BufferedInputStream bis = null;

		try {
			// Open the file.
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);

			// read the file contents in our buffer.
			bis.read(fileBuff);

			// Upload the file to your server.
			System.out.println("Uploading file ["+file.getName()+"] to server at "+uploadUrl+" ....");
			// Create HTTP client
			HttpClient httpClient = new DefaultHttpClient();
			
			// Send username and password in POST (as BASIC AUTH)
			String creds = serviceUser + ":" + servicePwd;
			byte[] encodedCreds = Base64.encodeBase64(creds.getBytes());
			String encodedCredStr = new String(encodedCreds);
			// Create HTTP POST request
			HttpPost postRequest = new HttpPost(uploadUrl);	
			// Pass the Credentials in POST Request.
			postRequest.setHeader("Authorization", "Basic " + encodedCredStr);
			
			// Generate Byte array body
			ByteArrayBody bab = new ByteArrayBody(fileBuff, file.getName());
			
			// We need Multipart data for file uploads using HTTP POST.
			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("fileToUpload", bab);
			
			// Add your Multipart request to HTTP POST request.
			postRequest.setEntity(reqEntity);
			
			// Send HTTP Request to Server and get back its response.
			HttpResponse response = httpClient.execute(postRequest);
			
			// Print HTTP Respose 
			System.out.println("Server Response : " + response);
			
		} catch (ClientProtocolException cpe) {
			System.err
			.println("ClientProtocolException while uploading file to Server!");
		} catch (IOException ioe) {
			System.err
			.println("IO Exception while uploading file to Server!");
		}  catch (Exception ex) {
			System.err
			.println("Some Exception while uploading file to Server!");
		} finally {
			// close all resources
			try {
				bis.close();
				fis.close();
			} catch (IOException ioe) {
				System.err
				.println("IO Exception while uploading file to Server!");
			}
		}
	}

}
