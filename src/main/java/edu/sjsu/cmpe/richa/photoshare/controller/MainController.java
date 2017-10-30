package edu.sjsu.cmpe.richa.photoshare.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sjsu.cmpe.richa.photoshare.App;
import edu.sjsu.cmpe.richa.photoshare.model.CreateAccountResponse;
import edu.sjsu.cmpe.richa.photoshare.model.DeleteImageResponse;
import edu.sjsu.cmpe.richa.photoshare.model.User;
import edu.sjsu.cmpe.richa.photoshare.service.PhotoService;
import edu.sjsu.cmpe.richa.photoshare.service.UploadPhotoService;
import edu.sjsu.cmpe.richa.photoshare.service.UserService;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.concurrent.Task;

/**
 * This is the Main Controller class for FXML.
 * This class is not managed by Spring, but loaded in Application entry point.
 * Hence cannot Autowire any Spring beans, have to inject them manually.
 *
 * @author Richa Lakhe
 *
 */
public class MainController implements Initializable {
	
	private static final Logger logger = LoggerFactory.getLogger(edu.sjsu.cmpe.richa.photoshare.controller.MainController.class);
	
	@FXML
	private TabPane mainTabPane;
	
	@FXML
	private Button proceedBtn; // Proceed to screen after Welcome page.
	
	@FXML
	private Button loginLogoutButton;
	
	@FXML
	private TextField usernameTF;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private CheckBox iAgreeCheckBox;
	
	@FXML
	private TextField newUsernameTF;
	
	@FXML
	private PasswordField newPasswordField;
	
	@FXML
	private TextField newFirstnameTF;
	
	@FXML
	private TextField newLastnameTF;
	
	@FXML
	private TextField newEmailIdTF;
	
	@FXML
	private Button createAccountBtn;
	
	@FXML
	private Label disclaimerLabel; // holds the Disclaimer message on Sign-In page.
	
	@FXML
	private Label loginMsgLabel; 
	
	@FXML
	private Tab uploadPhotoTab;
	
	@FXML
	private Tab showPhotoTab;
	
	@FXML
	private Button browsePicsBtn;
	
	@FXML
	private Button browseDirBtn;
	
	@FXML
	private TextField uploadFileTF;
	
	@FXML
	private Button prevImageBtn;
	
	@FXML
	private Button nextImageBtn;
	
	@FXML
	private Button fetchImagesBtn;
	
	@FXML
	private ImageView mainImageView;
	
	@FXML
	private Label imageIndexLabel; // holds the index of currently displayed image.
	
	@FXML
	private ScrollPane imageScrollPane; // holds the image thumbnails on left sidebar.
    
    @FXML
    private Button downloadImageBtn;
    
    @FXML
    private TextField imagePathTF;
    
    @FXML
    private Button deleteImageBtn;
    
    private TilePane imageTilePane; //holds the images tile pane(when images are fetched).
	
	// Get Bean from Spring
	private UserService userService;
	
	// Get Bean from Spring
	private UploadPhotoService uploadService;
	
	// Get Bean from Spring.
	private PhotoService photoService;
	
	// Executor Service to download images from server and render them on client.
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	
	private List<Image> images = new ArrayList<Image>(); // holds the images to be displayed.
	private int currentImageIndex = 0; // index of currently shown image.
	private int DEFAULT_THUMBNAIL_WIDTH = 100;
	
	private Stage pgStage;
	
	private final String DISCLAIMER_MSG = "DISCLAIMER :\n" +
	"In no event will we be liable for any loss or damage including without limitation, indirect or consequential loss or damage, "
	+ "or any loss or damage whatsoever arising from loss of data or profits arising out of, or in connection with, the use of this Service."
	+ "\n"
	+ "Every effort is made to keep the webservice up and running smoothly. However, PhotoShare Service takes no responsibility for, "
	+ "and will not be liable for, the webservice being temporarily unavailable due to technical issues beyond our control."
	+ "\n\n" 
	+ "PRIVACY POLICY :"
	+ "\n" 
	+ "This privacy policy sets out how PhotoShare Service uses and protects any information that you give PhotoShare Service when you use this Client."
	+ "PhotoShare Service is committed to ensuring that your privacy is protected. Should we ask you to provide certain information by which you can be identified when using this client, "
	+ "then you can be assured that it will only be used in accordance with this privacy statement."
	+ "PhotoShare Service may change this policy from time to time by updating this page. You should check this page from time to time to ensure that you are happy with any changes. "
	+ "This policy is effective from May 2nd 2015.";
	
	
	public void initialize(URL location, ResourceBundle resources) {
		// Need to manually wire these beans since this class is not Spring managed!
		userService = App.getContext().getBean(UserService.class);
		uploadService = App.getContext().getBean(UploadPhotoService.class);
		photoService = App.getContext().getBean(PhotoService.class);
		
		
		// Set Disclaimer Message
		disclaimerLabel.setText(DISCLAIMER_MSG);
	}
	
	public void onProceedBtn(ActionEvent event) {
		// goto Sign-In Tab pane (index 1).
		SingleSelectionModel<Tab> selectionModel = mainTabPane.getSelectionModel();
		selectionModel.select(1);
		
	}
	
	public void onBrowsePicsBtn(ActionEvent event) {
		final FileChooser fileChooser = new FileChooser();
		List<File> list = fileChooser.showOpenMultipleDialog(App.getPrimaryStage());
		StringBuffer sb = new StringBuffer();
		if (list != null) {
			logger.info("Found {} files to upload", list.size());
			for (File file : list) {
				sb.append(file.getPath());
	            sb.append(";");
			}
			uploadFileTF.setText(sb.toString());
			
			/*// Send each file one after another.
			// Catch Exception if thrown and move onto next file.
			for (File file : list) {
				try {
					logger.info("Uploading file {} to server", file);
					uploadService.uploadPhotoFileOrDirectory(file);
				} catch (Exception e) {
					logger.error("Error while uploading the File {}", file);
					logger.error(e.getMessage());
				}
			}
			showMsgBox("Image Services", null, "Done Uploading all Selected Photos!", AlertType.INFORMATION);*/
		}
	}
	
	public void onUploadPicsBtn(ActionEvent event) {
		logger.debug("Upload Photos Button clicked!");
		// Define JavaFX task that will do the heavy-lifting on background thread.
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				
				// Form Image files from TextBox.
				String fileStrs = uploadFileTF.getText();
				String[] fileArr = fileStrs.split(";");
				List<File> list = new ArrayList<File>();
				for(String fileStr : fileArr) {
					if(StringUtils.isNotBlank(fileStr)) {
						logger.info("Adding file {} to upload list", fileStr);
						File file = new File(fileStr);
						list.add(file);
					}
				}
				logger.info("Found {} files to upload", list.size());
				
				logger.info("Starting to Upload Image(s)...");
				// Send each file one after another.
				// Catch Exception if thrown and move onto next file.
				for (File file : list) {
					try {
						logger.info("Uploading file {} to server", file);
						uploadService.uploadPhotoFileOrDirectory(file);
					} catch (Exception e) {
						logger.error("Error while uploading the File {}", file);
						logger.error(e.getMessage());
					}
				}
				
				logger.info("Done uploading images {} in MainController. Will render UI in separate thread.", list.size());
				Platform.runLater(new Runnable() {
					// UI related operation here will be run in JavaFX thread.
					public void run() {
						// close the Progress Stage.
						pgStage.close();
						showMsgBox("Image Services", null, "Done Uploading all Selected Photos!", AlertType.INFORMATION);
					}
				});
				return null;
			}
		}; // Task def ends here.
		
		// start the Task now .
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		pgStage = showProgressBar("Uploading images....", 300, 200);
		
	}
	
	public void onBrowseDirBtn(ActionEvent event) {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		final File selectedDirectory = directoryChooser.showDialog(App.getPrimaryStage());
		if (selectedDirectory != null) {
			logger.info("Selected Directory {} to upload photos from", selectedDirectory.getAbsolutePath());
			uploadFileTF.setText(selectedDirectory.getAbsolutePath() + ";");
			
			/*try {
				uploadService.uploadPhotoFileOrDirectory(selectedDirectory);
			} catch (Exception e) {
				logger.error("Error while uploading the Files from Directory {}", selectedDirectory.getAbsolutePath());
				logger.error(e.getMessage());
			}
			
			showMsgBox("Image Services", null, "Done Uploading all Photos from selected Directory!", AlertType.INFORMATION);*/
		}
	}
	
	public void onNextImageBtn(ActionEvent event) {
		logger.debug("Next Image clicked!");
		showNextImage();
	}
	
	public void onPrevImageBtn(ActionEvent event) {
		logger.debug("Previous Image clicked!");
		showPrevImage();
	}
	
	/**
	 * Fetch Images button event handler.
	 * @param event
	 */
	public void onFetchImagesBtn(final ActionEvent event) {

		// Define JavaFX task that will do the heavylifting on background thread.
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				logger.info("Starting to Image download and render thread in MainController...");
				onFetchImagesAsync(event);
				logger.info(
						"Done downloading and render images {} in MainController",
						images.size());
				Platform.runLater(new Runnable() {
					/* @Override */
					public void run() {

						if (CollectionUtils.isNotEmpty(images)) {

							// Create TilePane and add each Image to it.
							final TilePane tile = new TilePane();
							try {
								tile.setPadding(new Insets(5, 5, 5, 5));
								tile.setVgap(4);
								tile.setHgap(4);
								tile.setPrefHeight(App.getPrimaryStage()
										.getHeight());
								tile.setStyle("-fx-background-color: DAE6F3;");

								for (Image curImage : images) {
									ImageView curImageView = createImageView(curImage);
									curImageView.addEventHandler(
											MouseEvent.MOUSE_CLICKED,
											new EventHandler<MouseEvent>() {
												/**
												 * Handle each MouseClick event
												 * on Images from TilePane
												 * Display the clicked Image and
												 * update the image index.
												 */
												public void handle(
														MouseEvent event) {
													logger.debug(
															"Image Tile Pressed! and generated event : {}",
															event);
													ImageView imageView = (ImageView) event
															.getSource();
													Image selectedImage = imageView
															.getImage();
													// find the image from
													// images list
													int selectedImageIndex = -1;
													for (int i = 0; i < images
															.size(); i++) {
														if (images
																.get(i)
																.equals(selectedImage)) {
															logger.info(
																	"User clicked on Image of index = {}",
																	i);
															selectedImageIndex = i;
														}
													}
													// set currentImageIndex to
													// one before selected Image
													// index.
													currentImageIndex = selectedImageIndex - 1;
													showNextImage();
													event.consume();
												}
											});
									tile.getChildren().add(curImageView);
								}
							} catch (Exception e) {
								logger.error(
										"Error while creating TilePane. Exception : {}",
										e.getMessage());
							}

							// save reference to this tilePane in classwide
							// variable.
							imageTilePane = tile;

							imageScrollPane
							.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
							imageScrollPane
							.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
							imageScrollPane.setFitToHeight(true);
							imageScrollPane.setFitToWidth(true);
							imageScrollPane.setContent(tile);

						}
						logger.info("Fetched {} images from Server",
								images.size());
						// since we just fetched images, reset image index = -1,
						// so we display 0th image.
						currentImageIndex = -1;
						// now display the first image if any available
						showNextImage();

						// close the Progress Stage.
						pgStage.close();

						// show success box.
						// showMsgBox("Image Services", null,
						// "Fetched All Images!", AlertType.INFORMATION);

					}
				});
				return null;
			}
		}; // Task def ends here.

		// start the Task now .
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		pgStage = showProgressBar("Downloading images....", 300, 200);
		
	}
		
	/**
	 * Fetch images from server.
	 * @param event
	 */
	public void onFetchImagesAsync(ActionEvent event) {
		logger.info("FetchImages Button clicked!");
		
		// TODO: Retrieve the current logged-in username from userService.
		Future<List<Image>> futureImageSet = photoService.fetchImagesAsync("admin");
		
		// retrieve images from Future.
		try {
			long startTime = System.currentTimeMillis();
			logger.info("Waiting to get all images from server....");
			images = futureImageSet.get();
			logger.info("Got Images {} from Server. Time taken = {} sec", images.size(), (System.currentTimeMillis()-startTime)/1000);
			
		} catch (InterruptedException e1) {
			logger.error("Error while fetching Images from Server. Exception {}", e1.getMessage());
			showMsgBox("Image Services", null, "Error while fetching Images from Server. Please re-fetch!", AlertType.ERROR);
			return ;
		} catch (ExecutionException e1) {
			logger.error("Error while fetching Images from Server. Exception {}", e1.getMessage());
			showMsgBox("Image Services", null, "Error while fetching Images from Server. Please re-fetch!", AlertType.ERROR);
			return ;
		} finally {
			// disable ProgressBar and ProgressIndicator
			
		}
	}
	
	public void onFetchImagesBtn2(/*ActionEvent event*/) {
		logger.info("FetchImages Button clicked!");
		
		
		// TODO: Retrieve the current logged-in username from userService.
		Future<List<Image>> futureImageSet = photoService.fetchImagesAsync("admin");
		
		// retrieve images from Future.
		try {
			long startTime = System.currentTimeMillis();
			logger.info("Waiting to get all images from server....");
			images = futureImageSet.get();
			logger.info("Got Images {} from Server. Time taken = {} sec", images.size(), (System.currentTimeMillis()-startTime)/1000);
		} catch (InterruptedException e1) {
			logger.error("Error while fetching Images from Server. Exception {}", e1.getMessage());
			showMsgBox("Image Services", null, "Error while fetching Images from Server. Please re-fetch!", AlertType.ERROR);
			return ;
		} catch (ExecutionException e1) {
			logger.error("Error while fetching Images from Server. Exception {}", e1.getMessage());
			showMsgBox("Image Services", null, "Error while fetching Images from Server. Please re-fetch!", AlertType.ERROR);
			return ;
		} finally {
			// disable ProgressBar and ProgressIndicator
			
		}
		
		if(CollectionUtils.isNotEmpty(images)) {
			
			// Create TilePane and add each Image to it.
			final TilePane tile = new TilePane();
			try {
				tile.setPadding(new Insets(5, 5, 5, 5));
				tile.setVgap(4);
				tile.setHgap(4);
				tile.setPrefHeight(App.getPrimaryStage().getHeight());
				tile.setStyle("-fx-background-color: DAE6F3;");

				for(Image curImage : images) {
					ImageView curImageView = createImageView(curImage);
					curImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                        /**
                         * Handle each MouseClick event on Images from TilePane
                         * Display the clicked Image and update the image index.
                         */
						public void handle(MouseEvent event) {
							logger.debug("Image Tile Pressed! and generated event : {}", event);
							ImageView imageView = (ImageView)event.getSource();
							Image selectedImage = imageView.getImage();
							// find the image from images list
							int selectedImageIndex = -1;
							for(int i=0; i < images.size(); i++) {
								if(images.get(i).equals(selectedImage)) {
									logger.info("User clicked on Image of index = {}", i);
									selectedImageIndex = i;
								}
							}
							// set currentImageIndex to one before selected Image index.
							currentImageIndex = selectedImageIndex -1;
							showNextImage();
							event.consume();
						}
					});
					tile.getChildren().add(curImageView);
				}
			} catch (Exception e) {
				logger.error("Error while creating TilePane. Exception : {}", e.getMessage());
			}
			
			// save reference to this tilePane in classwide variable.
			imageTilePane = tile;
			
			imageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			imageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			imageScrollPane.setFitToHeight(true);
			imageScrollPane.setFitToWidth(true);
			imageScrollPane.setContent(tile);
			
		}
		logger.info("Fetched {} images from Server", images.size());
		// since we just fetched images, reset image index = -1, so we display 0th image.
		this.currentImageIndex = -1; 
		// now display the first image if any available
		showNextImage();
				
		// show success box.
		showMsgBox("Image Services", null, "Fetched All Images!", AlertType.INFORMATION);
	}
	
	public void onDownloadImageBtn(ActionEvent event) throws IOException {
		String curFilePath = imagePathTF.getText(); // this could be pre-generated one, or user modified.
		
		// Get the current image and also generate a file path from it.
		Image curImage = images.get(currentImageIndex);
		String imageName = curImage.impl_getUrl().substring(curImage.impl_getUrl().lastIndexOf("/")+1);
		String imageExt = imageName.substring(imageName.lastIndexOf(".")+1);
		String filePath = photoService.getImageBasePath() + imageName;
		
		// use pre-generated file path, if it exists in textbox. 
		if(StringUtils.isNotBlank(curFilePath)) {
			filePath = curFilePath;
		}
		
		imagePathTF.setText(filePath);
		File file = new File(filePath);
		
		logger.info("Downloading Image to File {} ", filePath);
		
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(curImage, null), imageExt, file);
			showMsgBox("Image Services", null, "Downloaded Image to Path : " + filePath, AlertType.INFORMATION);
		} catch (IOException ioe) {
			logger.error("Error while saving Image {} to disk! {}", filePath, ioe);
			showMsgBox("Image Services", null, "Error saving Image to Path : " + filePath, AlertType.ERROR);
		} catch (IllegalArgumentException  iae) {
			logger.error("Error while saving Image {} to disk! {} ", filePath, iae);
			showMsgBox("Image Services", null, "Error saving Image to Path : " + filePath, AlertType.ERROR);
		}
		
	}
	
	public void onDeleteImageBtn(ActionEvent event) {
		logger.debug("Delete Image button pressed");
	    
		// find the image of current image.
		Image curImage = images.get(currentImageIndex);
		String imageName = curImage.impl_getUrl().substring(curImage.impl_getUrl().lastIndexOf("/")+1);
		
		// try to delete image from server.
		DeleteImageResponse response = photoService.deleteImage(imageName);
		
		// check the response and remove from Gallery only if deleted from server.
		if(!response.getStatus().equals("DELETED")) {
			logger.error("Couldn't delete image {} from server. Response {}", imageName, response );
			showMsgBox("Image Services", null, "Unable to delete Image from Server " + imageName, AlertType.ERROR);
			return;
		}
		
		// remove current image from images list.
		Image imageToDelete = images.remove(currentImageIndex);
		logger.info("Removed Image at index {} from images list.", currentImageIndex);
		imageTilePane.getChildren().remove(currentImageIndex);
		logger.info("Removed {} deleted image from Image TilePane. Showing Next image if available.", true);
		// show next image if available.
		--currentImageIndex; // since next image should would take the index at which deleted image was at.
		showNextImage();
	}
	
	
	
	
	
	public void onFetchImagesBtn2(ActionEvent event) {
		logger.info("FetchImages Button clicked!");
		
		// TODO: Retrieve the current logged-in username from userService.
		Set<String> imageUrlSet = photoService.fetchImageUrls("admin");
		images = new ArrayList<Image>(); // reset images array.
		if(CollectionUtils.isNotEmpty(imageUrlSet)) {
			
			// Create TilePane and add each Image to it.
			final TilePane tile = new TilePane();
			try {
				tile.setPadding(new Insets(5, 5, 5, 5));
				tile.setVgap(4);
				tile.setHgap(4);
				tile.setPrefHeight(App.getPrimaryStage().getHeight());
				tile.setStyle("-fx-background-color: DAE6F3;");

				for(String curImageUrl : imageUrlSet) {
					Image curImage = new Image(curImageUrl);
					images.add(curImage);
					ImageView curImageView = createImageView(curImage);
					curImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                        /**
                         * Handle each MouseClick event on Images from TilePane
                         * Display the clicked Image and update the image index.
                         */
						public void handle(MouseEvent event) {
							logger.debug("Image Tile Pressed! and generated event : {}", event);
							ImageView imageView = (ImageView)event.getSource();
							Image selectedImage = imageView.getImage();
							// find the image from images list
							int selectedImageIndex = -1;
							for(int i=0; i < images.size(); i++) {
								if(images.get(i).equals(selectedImage)) {
									logger.info("User clicked on Image of index = {}", i);
									selectedImageIndex = i;
								}
							}
							// set currentImageIndex to one before selected Image index.
							currentImageIndex = selectedImageIndex -1;
							showNextImage();
							event.consume();
						}
					});
					tile.getChildren().add(curImageView);
				}
			} catch (Exception e) {
				logger.error("Error while creating TilePane. Exception : {}", e.getMessage());
			}
			
			imageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			imageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			imageScrollPane.setFitToHeight(true);
			imageScrollPane.setFitToWidth(true);
			imageScrollPane.setContent(tile);
			
		}
		logger.info("Fetched {} images from Server", images.size());
		// since we just fetched images, reset image index = -1, so we display 0th image.
		this.currentImageIndex = -1; 
		// now display the first image if any available
		showNextImage();
				
		// show success box.
		showMsgBox("Image Services", null, "Fetched All Images!", AlertType.INFORMATION);
	}
	
/*	public void onTilePaneImageClick(MouseEvent event) {
		logger.info("Image Tile Pressed! and generated event : {}", event);
		event.consume();
	}*/
	
	private ImageView createImageView(final Image image) { 
	     final ImageView imageView = new ImageView(image);  
	     imageView.setFitWidth(DEFAULT_THUMBNAIL_WIDTH); 
	     imageView.setPreserveRatio(true);  
	     imageView.setSmooth(true);  
	     imageView.setCache(true);
	     return imageView ;  
	} 
	
	private void showNextImage() {
		if(images.size() == 0) {
			logger.warn("No Images to display! Size of Images = {}", images.size());
			return;
			// TODO : Might want to show some default image.
		}
		int nextImageIndex = ++currentImageIndex;
		if(images.size() >= (nextImageIndex+1)) {
			logger.debug("Showing Image at Index {}", nextImageIndex);
			mainImageView.setImage(images.get(nextImageIndex));
		} else {
			// reset counter to zero .
			currentImageIndex = 0;
			logger.warn("No Image available to show at Index {} so rolling back to zero index", nextImageIndex);
			// Show first image.
			mainImageView.setImage(images.get(0));
		}
		updateImageIndexLabel(currentImageIndex+1, images.size());
	}
	
	private void showPrevImage() {
		if(images.size() == 0) {
			logger.warn("No Images to display! Size of Images = {}", images.size());
			return;
			// TODO : Might want to show some default image.
		}
		int prevImageIndex = --currentImageIndex;
		// if we have reached start of the list, start from end of the list.
		if(prevImageIndex <= -1) {
			prevImageIndex = (images.size() - 1);
			currentImageIndex = prevImageIndex;
		} 
		logger.debug("Showing Image at Index {}", prevImageIndex);
		mainImageView.setImage(images.get(prevImageIndex));
		updateImageIndexLabel(currentImageIndex+1, images.size());
	}
	
	private void updateImageIndexLabel(int currentIndex, int totalSize) {
		String str = "Displaying " + currentIndex + " / " + totalSize;
		imageIndexLabel.setText(str);
		
		Image curImage = images.get(currentImageIndex);
		String imageName = curImage.impl_getUrl().substring(curImage.impl_getUrl().lastIndexOf("/")+1);
		String imageExt = imageName.substring(imageName.lastIndexOf(".")+1);
		String filePath = photoService.getImageBasePath() + imageName;
		imagePathTF.setText(filePath);
	}
	
/*	public void onShowPhotoTab(ActionEvent event) {
		logger.info("Show Photos Tab activated {}", event);
	}*/
	
	public void onCreateAccountBtn(ActionEvent event) {
		// validate input
		boolean success = isNewAccountInputValid();
		
		// if valid create new Account
		if(success) {
			// Create User model from provided input
			User newUser = new User();
			newUser.setUsername(newUsernameTF.getText());
			newUser.setPassword(newPasswordField.getText());
			newUser.setFirstName(newFirstnameTF.getText());
			newUser.setLastName(newLastnameTF.getText());
			newUser.setEmailId(newEmailIdTF.getText());
			
			// Create new Account using UserService
			CreateAccountResponse response = userService.createNewUser(newUser);
			
			// Check the response from Server.
			if(response.getStatus() == 1) {
				// Create Succesfully;
				logger.info("SignedUp new User {} Successfully. Server Response {}", newUser, response.getMsg());
				showMsgBox("Authentication", null, "Created New User Successfully!", AlertType.INFORMATION);
			} else {
				// Not created.
				logger.info("Error Signing Up new User {}. Server Response {}", newUser, response.getMsg());
				showMsgBox("Authentication", "Sorry for Inconvenience.", "Error SigningUp New User on Server. Contact Administrator.", AlertType.ERROR);
			}
		}
	}
	
	private boolean isNewAccountInputValid() {
		// Validate Username
		String username = newUsernameTF.getText();
		if(!StringUtils.isNotBlank(username) || !StringUtils.isAlphanumeric(username) || username.length() > 50) {
			showMsgBox("Authentication", null, "Username Cannot be Blank and should only have Alphanumeric characters. Max allowed length is 50 characters.", AlertType.ERROR);
			return false;
		}
		
		// Validate Password
		String password = newPasswordField.getText();
		if(!StringUtils.isNotBlank(password) || !StringUtils.isAlphanumeric(password) || password.length() > 50) {
			showMsgBox("Authentication", null, "Password Cannot be Blank and should only have Alphanumeric characters. Max allowed length is 50 characters.", AlertType.ERROR);
			return false;
		}
		
		// Validate Firstname
		String firstname = newFirstnameTF.getText();
		if(!StringUtils.isNotBlank(firstname) || !StringUtils.isAlpha(firstname) || firstname.length() > 20) {
			showMsgBox("Authentication", null, "Firstname Cannot be Blank and should only have Alpha characters. Max allowed length is 20 characters.", AlertType.ERROR);
			return false;
		}
		
		// Validate Lastname
		String lastname = newLastnameTF.getText();
		if(!StringUtils.isNotBlank(lastname) || !StringUtils.isAlpha(lastname) || lastname.length() > 20) {
			showMsgBox("Authentication", null, "Lastname Cannot be Blank and should only have Alpha characters. Max allowed length is 20 characters.", AlertType.ERROR);
			return false;
		}
				
		// Validate Email 
		String emailId = newEmailIdTF.getText();
		EmailValidator emailValidator = EmailValidator.getInstance();
		boolean isEmailValid = emailValidator.isValid(emailId);
		if(!isEmailValid) {
			showMsgBox("Authentication", null, "Email ID is not in Valid format.", AlertType.ERROR);
			return false;
		}
		
		// Ensure user has agreed to the Terms and Conditions
		// Is Checked if : indeterminate == false, checked == true
		boolean checked = ((iAgreeCheckBox.selectedProperty().get()==true) && (iAgreeCheckBox.indeterminateProperty().get()==false));
		if(!checked) {
			showMsgBox("Authentication", null, "Need to check the Agreement checkbox to proceed.", AlertType.ERROR);
			return false;
		}
		
		return true;
	}
	
	public void onLoginLogout(ActionEvent event) {
		Button btn = (Button) event.getSource();
		String buttonText = btn.getText();
		User user = null;
		if(buttonText.equalsIgnoreCase("Login")) {
			logger.info("Logging In...");
			String username = usernameTF.getText();
			String password = passwordField.getText();

			user = userService.validateAndGetUser(username, password);
			if(user == null || user.getStatus().equals("Invalid")) {
				String errMsg = "Specified Username or Password is Invalid!";
				loginMsgLabel.setText(errMsg);
				logger.warn("No user found for given username {} and password {} " + username, password);
				showMsgBox("Authentication", null, "Specified Username or Password is Invalid!", AlertType.ERROR);
				return;
			}else if(user.getStatus().equals("inactive")) {
				String errMsg = "Account assoicated with specified User is NOT Active. Please contact Administrator!";
				loginMsgLabel.setText(errMsg);
				logger.warn("User associated with given username {} and password {} is NOT Active on Server." + username, password);
				showMsgBox("Authentication", null, "Account assoicated with specified User is NOT Active. Please contact Administrator!", AlertType.ERROR);
				return;
			}
			
			// On successful login 
			// 1. Update Login Button
			btn.setText("Logout");
			// 2. Other logic
			onLoginSuccess(user);
		}else {
			logger.info("Logging Out...");
			// On successful logout
			// 1. Update Login Button.
			btn.setText("Login");
			// 2. Other logic
			onLogoutSuccess();
		}
	}
	
	public void doExit() {
		photoService.cleanupExecutor();
		executorService.shutdownNow();
		Platform.exit();
	}
	
	public void showAbout() {
		showMsgBox("Photo Sharing Application", null, "Photo Sharing Application version 1.0", AlertType.INFORMATION);
	}
	
	private void onLoginSuccess(User loggedInUser) {

		// 1. Disable username/password fields
		usernameTF.setDisable(true);
		passwordField.setDisable(true);
		
		// 2. Update Msg.
		String msg = "Login Successfull!";
		loginMsgLabel.setText(msg);

		// 3. Enable tabs
		uploadPhotoTab.setDisable(false);
		showPhotoTab.setDisable(false);
		
		// 4. add User to login Cache
		userService.addLoggedInUserToCache(loggedInUser);
		
		// 5. Show Login Success Dialog
		showMsgBox("Authentication", null, "Logged-In Successfully!", AlertType.INFORMATION);
	}
	
	private void onLogoutSuccess() {

		// 1. Enable username/password fields
		usernameTF.setDisable(false);
		passwordField.setDisable(false);
		
		// 2. Update Msg.
		String msg = "Logout Successfull!";
		loginMsgLabel.setText(msg);

		// 3. Disable tabs
		uploadPhotoTab.setDisable(true);
		showPhotoTab.setDisable(true);
		
		// 4. remove User from Login Cache
		String username = usernameTF.getText();
		User loggedOutUser = new User();
		loggedOutUser.setUsername(username);
		userService.removeLoggedOutUserFromCache(loggedOutUser);
		
		// 5. Show Logout Success Dialog
		showMsgBox("Authentication", null, "Logged-Out Successfully!", AlertType.INFORMATION);
	}
	
	/**
	 * Display Message Dialog Box.
	 * @param msg
	 */
	private void showMsgBox(String title, String headerText, String contextText, AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contextText);
		alert.setResizable(true);

		alert.showAndWait();
	}
	
	private Stage showProgressBar2() {
		Stage stage = new Stage();
		stage.setScene(new Scene(VBoxBuilder.create().
			    children(new Text("hi")).
			    alignment(Pos.CENTER).padding(new Insets(5)).build()));
		stage.show();
		return stage;
	}
	private Stage showProgressBar(String msg, double width, double height) {
		
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.WINDOW_MODAL);
		
		// Add Indeterminate state (continous spinning) progress controls.
		ProgressBar pbar = new ProgressBar();
		ProgressIndicator pin = new ProgressIndicator();
		
		dialogStage.setScene(new Scene(VBoxBuilder.create().
		    children(new Text(msg), pbar, pin).
		    alignment(Pos.CENTER).padding(new Insets(5)).build()));
		dialogStage.setHeight(height);
		dialogStage.setWidth(width);
		dialogStage.show();
		logger.debug("Started showing Progress Bar/Indicator Stage....");
		return dialogStage;
	}

}
