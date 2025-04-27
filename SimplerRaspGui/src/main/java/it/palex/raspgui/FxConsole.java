package it.palex.raspgui;

import java.util.concurrent.Semaphore;

import it.palex.rasp.inout.InputReader;
import it.palex.rasp.inout.OutputObserver;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class FxConsole implements InputReader, OutputObserver {

	private TextArea textArea;
	private Semaphore semConsumer = new Semaphore(0);
	private boolean isWaitingConsumer = false;
    private final Object lock = new Object();
	private String currentText = null;
	
	
	public FxConsole(TextArea textArea) {
		this.textArea = textArea;
		this.textArea.editableProperty().set(false);
		this.listenEnter();
	}
	
	private void listenEnter() {
		this.textArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (keyEvent.getCode() == KeyCode.ENTER) {
					String text = textArea.getText();
					
					synchronized (lock) {
						if(isWaitingConsumer) {
							int index = text.lastIndexOf(">>>");
							
							if(index>0) {
								currentText = text.substring(index + 3).trim();
							}else {
								currentText = "";
							}
							
							isWaitingConsumer = false;
							semConsumer.release();
						}
					}
					
				}
			}
		});
	}
	
	private Object stringBufferLock = new Object();
	private StringBuilder outputStringBuffer = new StringBuilder(1024);
	private Runnable currentTextPrintRunnable = null;
	private boolean arePrintedAllChars = false;
	private static final int TEXT_AREA_MAX_LENGHT = 3000;
	
	@Override
	public void printOut(String output) {
		//do not use textArea.appendText(str.toString()); directly without a runnable
		//because this causes a NullPointerException of javafx
		synchronized(stringBufferLock) {
			outputStringBuffer.append(output);
			outputStringBuffer.append("\n");
			
			if(currentTextPrintRunnable==null || arePrintedAllChars) {
				currentTextPrintRunnable = createRunnableTextConsumer();
		        
		        javafx.application.Platform.runLater(currentTextPrintRunnable);
			}
			
			
		}
	}
	
	
	private Runnable createRunnableTextConsumer() {
		return new Runnable() {
            @Override
            public void run() {
            	synchronized (stringBufferLock) {
            		textArea.appendText(outputStringBuffer.toString());
            		
            		if(textArea.getLength()>TEXT_AREA_MAX_LENGHT*2) {
            			textArea.deleteText(0, textArea.getLength()-TEXT_AREA_MAX_LENGHT);
            		}
            		
    				outputStringBuffer.delete(0, outputStringBuffer.length());
    				arePrintedAllChars = true;
				}
            }
        };
	}

	@Override
	public int readInt() {
		this.textArea.editableProperty().set(true);
		boolean invalidInt = true;
		
		synchronized (lock) {
			this.isWaitingConsumer = true;
		}
		
		while(invalidInt) {
			try {
				semConsumer.acquire();
				
				try {
					int val = Integer.parseInt(currentText);
					this.textArea.editableProperty().set(true);
					return val;
				}catch(NumberFormatException e) {
					printOut("Invalid input number!\n Try again >>>\n");
					synchronized (lock) {
						this.isWaitingConsumer = true;
					}
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
		throw new RuntimeException("Program should never arrive at this point");		
	}

	@Override
	public void close() {
		//nothing to do
	}

}
