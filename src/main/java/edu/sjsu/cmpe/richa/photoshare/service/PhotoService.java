package edu.sjsu.cmpe.richa.photoshare.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import edu.sjsu.cmpe.richa.photoshare.model.DeleteImageResponse;
import javafx.scene.image.Image;

public interface PhotoService {

	/**
	 * Get the Set of Images URLs which belong to specified User.
	 * @param username
	 * @return
	 */
	public Set<String> fetchImageUrls(String username);
	
	public Future<List<Image>> fetchImagesAsync(String username);
	
	public boolean cleanupExecutor();
	
	public String getImageBasePath();
	
	public DeleteImageResponse deleteImage(String filename);
}
