package edu.sjsu.cmpe.richa.photoshare;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import edu.sjsu.cmpe.richa.photoshare.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Main Application class containing the entry-point.
 * Spring ApplicationContext will be loaded in this class.
 * This class itself is annotated as Spring Component to enable scanning of itself.
 * 
 * @author Richa Lakhe
 */
@Component
public class App extends Application
{
	private static final Logger logger = LoggerFactory.getLogger(edu.sjsu.cmpe.richa.photoshare.App.class.getName());
	private static Stage primaryStage;
	private static ApplicationContext context;
	
    public static void main( String[] args )
    {
        logger.info( "Starting Application...." );
        logger.info("Trying to load Spring ApplicationContext from AppConfig class....");
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        setContext(context);
        logger.info("Successfuly loaded Spring ApplicationContext: {} ", context);
        logger.info("ApplicationName : {} , Total Bean Count : {}", context.getApplicationName(), context.getBeanDefinitionCount());
        logger.info("Beans found : {}", Arrays.asList(context.getBeanDefinitionNames()));
        launch(args);
        logger.info("Exiting Application.");
    }

	@Override
	public void start(Stage primaryStage) throws Exception {
		URL mainFxmlUrl = getClass().getResource("/fxml/Main.fxml");
		Parent rootParent = FXMLLoader.load(mainFxmlUrl);
		Scene scene = new Scene(rootParent, 1090, 807);
		scene.getStylesheets().add("/css/main.css");
		
		primaryStage.setResizable(true);
		primaryStage.setScene(scene);
		primaryStage.setTitle("PhotoShare");
		primaryStage.show();
		setPrimaryStage(primaryStage);		
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage stage) {
		primaryStage = stage;
	}

	public static ApplicationContext getContext() {
		return context;
	}

	public static void setContext(ApplicationContext context) {
		App.context = context;
	}
	
	
}
