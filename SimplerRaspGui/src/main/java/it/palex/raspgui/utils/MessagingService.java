package it.palex.raspgui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MessagingService {

	
	public static void alertError(Stage stage, String header, String text){
		  Alert alert = new Alert(AlertType.ERROR);
		  alert.initModality(Modality.WINDOW_MODAL);
		  alert.initOwner(stage);
		  alert.setTitle("Errore");
		  alert.setHeaderText(header);
		  alert.setContentText(text);
		  alert.showAndWait();
	  }
	  
	  public static void alertWarning(Stage stage, String header, String text){
		  Alert alert = new Alert(AlertType.WARNING);
		  alert.initModality(Modality.WINDOW_MODAL);
		  alert.initOwner(stage);
		  alert.setTitle("Warning");
		  alert.setHeaderText(header);
		  alert.setContentText(text);
		  alert.showAndWait();
	  }
	  
	private static void showAlert(Stage stage, String title, String subtitle, String text) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initModality(Modality.WINDOW_MODAL);
		alert.initOwner(stage);
		alert.setTitle(title);
		if(subtitle!=null) {
			alert.setHeaderText(subtitle);
		}

		TextArea textArea = new TextArea(text);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setContent(expContent);

		alert.showAndWait();
	}

	public static void showGuide(Stage stage) {
		showAlert(stage, "Simple Rasp IDE", "User Guide", getGuide());
	}

	public static void showAbout(Stage stage) {
		showAlert(stage, "Simple Rasp IDE", "About", getAbout());
	}

	private static String getAbout() {
		String about = "Mini IDE developed for the exam 'Languages and Computational Models M' "
				+ "2020/2021.\r\n\r\n"
				+ "Developed by Alessandro Pagliaro.";
		return about;
	}

	public static String getGuide() {
		String guide = "The application aims to compile and execute a mini programming language "
				+ "on a RASP emulator.\r\n\r\n"
				+ "How to run a program:\r\n\r\n"
				+ "1) To execute the written program, click on 'Run>Compile' and then on 'Run>Run'.\n"
				+ "2) To terminate the running program, click on 'Run>Terminate'.\n"
				+ "3) If you receive the error 'Zero division', the line detail refers to the compiled program!\n"
				+ "4) If the program prints -2147483648, it potentially means an error was generated.";
		return guide;
	}
}
