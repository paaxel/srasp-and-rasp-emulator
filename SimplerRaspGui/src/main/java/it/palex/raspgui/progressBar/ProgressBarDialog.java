package it.palex.raspgui.progressBar;


import org.controlsfx.control.Notifications; 
import org.controlsfx.dialog.ProgressDialog;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProgressBarDialog<T> {

	
	private ProgressDialog progDiag;
	
	public ProgressBarDialog(Stage owner, String title, String header, Task<T> service, 
			Modality modality, boolean showMessageOnComplete){
		if(title==null || header==null || service==null || modality==null){
			throw new NullPointerException();
		}
		progDiag = new ProgressDialog(service);
        progDiag.setTitle(title);
        if(owner!=null){
        	progDiag.initOwner(owner);
        }
        
        progDiag.setHeaderText(header);
        progDiag.initModality(modality);
        progDiag.show();
	}
	
	
	public ProgressDialog getDialog(){
		return progDiag;
	}
}

