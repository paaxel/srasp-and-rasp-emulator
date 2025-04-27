package it.palex.raspgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class RaspGuiApp extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/RaspGuiApp.fxml"));
        Parent root = loader.load();
        
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icon.png")));
        stage.setTitle("Simpler Rasp IDE");
        Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/simpler-rasp-keyword.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
        
        RaspGuiController controller = loader.getController();
        stage.setOnHidden(e -> controller.closeProgram());

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}