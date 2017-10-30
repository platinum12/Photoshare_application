package edu.sjsu.cmpe.richa.photoshare.service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.scene.image.Image;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import edu.sjsu.cmpe.richa.photoshare.model.DeleteImageResponse;
import edu.sjsu.cmpe.richa.photoshare.model.PhotoCollection;

@Service
public class PhotoServiceImpl implements PhotoService {
	
private static final Logger logger = LoggerFactory.getLogger(edu.sjsu.cmpe.richa.photoshare.service.PhotoServiceImpl.class);
	
	@Value( "${photoshare.webserver.base.url}" )
	private String baseUrl;
	
	@Value( "${photoshare.webservice.getPhotoList.resource}" )
	private String getPhotoListResource;
	
	@Value( "${photoshare.webserver.service.user}")
	private String serviceUser;
	
	@Value( "${photoshare.webserver.service.pwd}")
	private String servicePwd;
	
	@Value( "${photoshare.client.download.image.path.base}")
	private String imageBasePath;
	
	@Value( "${photoshare.webservice.deleteImage.resource}")
	private String deleteImageResource;
	
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	public Set<String> fetchImageUrls(String username) {
		
		RestTemplate restTemplate = new RestTemplate();		
		
		// Form the URL from which to fetch Images.
		String getPhotoListUrl = baseUrl + getPhotoListResource;
		// Form the Authorization header.
		HttpEntity<String> requestEntity = new HttpEntity<String>(createHeaders(serviceUser, servicePwd));
		logger.debug("Trying to get photoList from URL : " + getPhotoListUrl + " with request params : " + requestEntity);
		PhotoCollection photoCollection = null;
		
		// Try and GET the Image URLs from Remote server.
		try {
			ResponseEntity<PhotoCollection> response = restTemplate.exchange(getPhotoListUrl, HttpMethod.GET,
						requestEntity, PhotoCollection.class);

			//String responseBody = response.getBody();
			photoCollection = response.getBody();
			logger.debug("Got PhotoCollection : " + photoCollection);
		} catch (RestClientException rcex) {
			logger.error("Unable to GET request {} from server!", getPhotoListUrl);
		} catch (Exception ex) {
			logger.error("Exception when trying to GET request {} from server!", getPhotoListUrl);
		}
		
		// Remove non-image URLs and form the final Set of Image URLs.
		Set<String> imageUrlSet = new HashSet<String>();
		if (CollectionUtils.isNotEmpty(photoCollection.getImages())) {
			for (String imageUrl : photoCollection.getImages()) {
				if (!imageUrl.endsWith(".") && !imageUrl.endsWith("..")) {
					imageUrlSet.add(imageUrl);
				}
			}
		}
		
		logger.debug("Found following Image URLs for user :{}", username, imageUrlSet);
		return imageUrlSet;
	}
	
	/**
	 * Create HTTP Authorization header for Remote Server API invocation.
	 * @param username
	 * @param password
	 * @return
	 */
	@SuppressWarnings("serial")
	HttpHeaders createHeaders( final String username, final String password ){
		   return new HttpHeaders(){
		      {
		         String auth = username + ":" + password;
		         byte[] encodedAuth = Base64.encodeBase64( 
		            auth.getBytes(Charset.forName("US-ASCII")) );
		         String authHeader = "Basic " + new String( encodedAuth );
		         set( "Authorization", authHeader );
		      }
		   };
	}
	
	
	/**
	 * Fetch Images Asynchronously (in separate thread).
	 * 
	 * @param username
	 * @return Future which holds the Set of Images.
	 */
	public Future<List<Image>> fetchImagesAsync(String username) {
		
		Future<List<Image>> future = executorService.submit(new Callable<List<Image>>(){
		    public List<Image> call() throws Exception {
		        logger.info("Starting to fetch Images Async...");
		        Set<String> imageUrls = fetchImageUrls();
		        logger.info("Number of Image URLs found = {}", imageUrls.size());
		        List<Image> images = new ArrayList<Image>();
		        logger.info("Converting Image URLs to images....");
		        for(String curImageUrl : imageUrls) {
					Image curImage = new Image(curImageUrl);
					images.add(curImage);
		        }
		        logger.info("Done converting Image URLs to Images {}", images.size());
		        return images;
		    }
		});
		
		return future;
	}
	
	private Set<String> fetchImageUrls() {
		
		RestTemplate restTemplate = new RestTemplate();		
		
		// Form the URL from which to fetch Images.
		String getPhotoListUrl = baseUrl + getPhotoListResource;
		// Form the Authorization header.
		HttpEntity<String> requestEntity = new HttpEntity<String>(createHeaders(serviceUser, servicePwd));
		logger.debug("Trying to get photoList from URL : " + getPhotoListUrl + " with request params : " + requestEntity);
		PhotoCollection photoCollection = null;
		
		// Try and GET the Image URLs from Remote server.
		try {
			ResponseEntity<PhotoCollection> response = restTemplate.exchange(getPhotoListUrl, HttpMethod.GET,
						requestEntity, PhotoCollection.class);

			//String responseBody = response.getBody();
			photoCollection = response.getBody();
			logger.debug("Got PhotoCollection : " + photoCollection);
		} catch (RestClientException rcex) {
			logger.error("Unable to GET request {} from server!", getPhotoListUrl);
		} catch (Exception ex) {
			logger.error("Exception when trying to GET request {} from server!", getPhotoListUrl);
		}
		
		// Remove non-image URLs and form the final Set of Image URLs.
		Set<String> imageUrlSet = new HashSet<String>();
		if (CollectionUtils.isNotEmpty(photoCollection.getImages())) {
			for (String imageUrl : photoCollection.getImages()) {
				if (!imageUrl.endsWith(".") && !imageUrl.endsWith("..")) {
					imageUrlSet.add(imageUrl);
				}
			}
		}
		
		logger.debug("Found following Image URLs for user :{}", imageUrlSet);
		return imageUrlSet;
	}
	
	
	public DeleteImageResponse deleteImage(String filename) {

		RestTemplate restTemplate = new RestTemplate();

		String deleteImageUrl = baseUrl + deleteImageResource;
		// Form the Authorization header and form data.
		MultiValueMap<String,String> formData=new LinkedMultiValueMap<String,String>();
		formData.add("filename", filename);
		HttpEntity<MultiValueMap<String,String>> requestEntity = 
				new HttpEntity<MultiValueMap<String,String>>(formData,createHeaders(serviceUser, servicePwd));
		logger.debug("Trying to delete Image using URL : " + deleteImageUrl + " with request params : " + requestEntity);
		DeleteImageResponse response = null;
		try {
			
			ResponseEntity<DeleteImageResponse> responseEntity = restTemplate.exchange(deleteImageUrl, HttpMethod.POST,
					requestEntity, DeleteImageResponse.class);
			response = responseEntity.getBody();
			logger.debug("Delete Image HTTP response : " + response);
		} catch (RestClientException rcex) {
			logger.error("Unable to POST request {} to server!", deleteImageUrl);
		} catch (Exception ex) {
			logger.error("Exception when trying to POST request {} to server!", deleteImageUrl);
		}


		return response;
	}
	
	public boolean cleanupExecutor() {
		executorService.shutdownNow();
		return true;
	}
	
	public String getImageBasePath() {
		return this.imageBasePath;
	}

}
