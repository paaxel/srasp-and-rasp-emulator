package it.palex.raspgui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import it.palex.rasp.EmulatorFacade;
import it.palex.raspgui.progressBar.ProgressBarDialog;
import it.palex.raspgui.utils.MessagingService;
import it.palex.srasp.compiler.SimplerRaspNewCompiler;
import it.palex.srasp.lang.SimplerRaspLexer;
import it.palex.srasp.lang.SimplerRaspParser;
import it.palex.srasp.parser.SyntaxError;
import it.palex.srasp.parser.SyntaxErrorListener;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RaspGuiController implements Initializable {

	private static final Logger LOGGER = LogManager.getLogger(RaspGuiController.class);

	private static final String[] KEYWORDS = new String[] {
            "def", "number", "bool", "void", "cin",
            "cout", "return", "while", "if", "else",
            "main"
    };

	private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String COMMENT_PATTERN = "#[^\n]*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

	private static final String[] RASP_KEYWORDS = new String[] {
			"LOAD", "STORE", "WRITE", "HALT", "ADD", "DIV", "SUB", "MUL",
			"JZ", "JNZ", "JGZ", "JGEZ", "JLZ", "JLEZ", "JUMP", "READ"
	};
	private static final String RASP_KEYWORD_PATTERN = "\\b(" + String.join("|", RASP_KEYWORDS) + ")\\b";
	private static final String RASP_COLON_PATTERN = ":";
	private static final String RASP_MODE_PATTERN = "#|@";

	private static final Pattern PATTERN_RASP = Pattern.compile(
			"(?<KEYWORD>" + RASP_KEYWORD_PATTERN + ")"
					+ "|(?<COLON>" + RASP_COLON_PATTERN + ")"
					+ "|(?<MODE>" + RASP_MODE_PATTERN + ")"
	);
	
	@FXML
	private BorderPane primaryPane;

	@FXML
	private TextArea errorTextArea, consoleTextArea;

	@FXML
	private TitledPane sourceProgramTextAreaContainer, compiledProgramTextAreaContainer;
	
	@FXML
	private TabPane consoleOutTabPane;

	@FXML
	private Tab consoleTab, errorsTab;
	
	@FXML
	private MenuItem terminateMenuItem, runMenuItem, compileMenuItem;
	
	
	
	private CodeArea sourceProgramTextArea, compiledProgramTextArea;
	
	
	private boolean executionInProgress;
	private Object lock = new Object();
	
	private FxConsole console;
	private Thread executionProcess;
	private ExecutorService executor;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		executor = Executors.newSingleThreadExecutor();
		this.sourceProgramTextArea = new CodeArea();
		this.sourceProgramTextArea.clear();
		
		this.compiledProgramTextArea = new CodeArea();
		
		this.decorateCompiledProgramCodeArea();
		this.decorateSourceProgramCodeArea();
		
		VirtualizedScrollPane<CodeArea> scrollSP = new VirtualizedScrollPane<>(this.sourceProgramTextArea);
		this.sourceProgramTextAreaContainer.setContent(scrollSP);
		
		VirtualizedScrollPane<CodeArea> scrollCP = new VirtualizedScrollPane<>(this.compiledProgramTextArea);
		this.compiledProgramTextAreaContainer.setContent(scrollCP);
		
		this.compiledProgramTextArea.clear();
		this.errorTextArea.setText("");
		this.consoleTextArea.setText("");
		this.console = new FxConsole(consoleTextArea);
		this.terminateMenuItem.setDisable(true);
		this.runMenuItem.setDisable(true);
	}

	private void decorateCompiledProgramCodeArea() {
		this.compiledProgramTextArea.setPrefHeight(168.0d);
		this.compiledProgramTextArea.setPrefWidth(385.0d);
		this.compiledProgramTextArea.setEditable(false);
		this.compiledProgramTextArea.setParagraphGraphicFactory(
					LineNumberFactory.get(this.sourceProgramTextArea)
				);
		
		this.compiledProgramTextArea.setContextMenu(new DefaultContextMenu(false));

		Subscription cleanupWhenDone = this.compiledProgramTextArea.multiPlainChanges()
				.successionEnds(Duration.ofMillis(1000))
				.retainLatestUntilLater(executor)
				.supplyTask(this::computeHighlightingToCompiledAreaAsync)
				.awaitLatest(compiledProgramTextArea.multiPlainChanges())
				.filterMap(t -> {
					if(t.isSuccess()) {
						return Optional.of(t.get());
					} else {
						LOGGER.error(t.getFailure());
						return Optional.empty();
					}
				})
				.subscribe(this::applyHighlightingToCompiledArea);
	}

	private void decorateSourceProgramCodeArea() {
		this.sourceProgramTextArea.setPrefHeight(0.0d);
		this.sourceProgramTextArea.setPrefWidth(387.0d);
		this.sourceProgramTextArea.setParagraphGraphicFactory(
					LineNumberFactory.get(this.sourceProgramTextArea)
				);
		
		this.sourceProgramTextArea.setContextMenu(new DefaultContextMenu(true));
		
		Subscription cleanupWhenDone = this.sourceProgramTextArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .retainLatestUntilLater(executor)
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(sourceProgramTextArea.multiPlainChanges())
                .filterMap(t -> {
                    if(t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);
		
	}
	
	private Stage getStage() {
		Stage stage = (Stage) primaryPane.getScene().getWindow();
		return stage;
	}

	public void compileProgram() {
		if (this.sourceProgramTextArea.getText().trim().equals("")) {
			MessagingService.alertWarning(this.getStage(), "No program to compile!",
					"Please enter the source code to compile.");
			return;
		}
		
		synchronized(lock){
			if (this.executionInProgress) {
				MessagingService.alertWarning(this.getStage(), "Program running",
						"Please terminate the current execution before starting a new compilation.");
				return;
			}
		}
		
		Task<Void> compileTask = compileTask(sourceProgramTextArea.getText());

		@SuppressWarnings("unused")
		ProgressBarDialog<Void> dialog = new ProgressBarDialog<>(this.getStage(), "Compiling Program", "Compiling...",
				compileTask, Modality.WINDOW_MODAL, false);

		Platform.runLater(compileTask);
	}

	public Task<Void> compileTask(String sourceProgram) {
		return new Task<Void>() {
			@Override
			protected Void call() throws InterruptedException {

				try {
					compileProgram(sourceProgram);
					return null;
				} catch (Exception e) {
					LOGGER.error(e);
				}
				return null;
			}
		};
	}

	private void compileProgram(String sourceProgram) {
		this.compiledProgramTextArea.clear();

		CharStream inputCharStream = CharStreams.fromString(sourceProgram);
		// CharStreams.fromStream(stream);
		SimplerRaspLexer lexer = new SimplerRaspLexer(inputCharStream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SimplerRaspParser parser = new SimplerRaspParser(tokens);

		SyntaxErrorListener listener = new SyntaxErrorListener();
		parser.addErrorListener(listener);

		ParseTree three = parser.program();

		if (listener.getSyntaxErrors() != null && !listener.getSyntaxErrors().isEmpty()) {
			for (SyntaxError elem : listener.getSyntaxErrors()) {
				appendTextToErrorArea("Error at line <" + elem.getLine() + "," + elem.getCharPositionInLine()
						+ ">. Error: " + elem.getMessage()+"\n");
				this.consoleOutTabPane.getSelectionModel().select(this.errorsTab);
				this.runMenuItem.setDisable(true);
			}
		} else {
			
			SimplerRaspNewCompiler compiler = new SimplerRaspNewCompiler();
			try{
				compiler.visit(three);
				this.runMenuItem.setDisable(false);
				this.compiledProgramTextArea.appendText(compiler.getCompiledProgram());
			}catch(Exception e) {
				this.runMenuItem.setDisable(true);
				appendTextToErrorArea(e.getMessage()+"\n");
				this.consoleOutTabPane.getSelectionModel().select(this.errorsTab);
			}
		}
	}

	public void clearConsoleLogs() {
		this.consoleTextArea.setText("");
	}

	public void clearErrorLogs() {
		this.errorTextArea.setText("");
	}

	public void terminateProgram() {
		if (this.executionProcess!=null) {
			// interrupt rhe interpretation releasing possible locks
			this.executionProcess.interrupt();
			appendTextToConsoleArea("Interrupted program!\n");
			this.executionProcess = null;
			return;
		}
	}

	public void openAbout() {
		MessagingService.showAbout(getStage());
	}

	public void openGuide() {
		MessagingService.showGuide(getStage());
	}

	public void runProgram() {
		if (this.compiledProgramTextArea.getText().trim().equals("")) {
			MessagingService.alertWarning(this.getStage(), "No compiled program available!",
					"Please compile the source code before starting the execution.");
			return;
		}
		
		synchronized(lock){
			if(this.executionInProgress) {
				return;
			}
		}
		this.consoleTextArea.clear();
		this.consoleOutTabPane.getSelectionModel().select(this.consoleTab);
		
		Runnable compileTask = this.createRaspExecutionTask();
		this.executionProcess = new Thread(compileTask);
		
		this.executionProcess.start();
	}
	
	public Runnable createRaspExecutionTask() {
		String raspProgram = this.compiledProgramTextArea.getText();
		
		return new Runnable() {
			@Override
			public void run() {
				try {
					executeRaspProgram(raspProgram);
				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
		};
		
	}
		
	public void executeRaspProgram(String program) {		
		synchronized (lock) {
			this.terminateMenuItem.setDisable(false);
			this.runMenuItem.setDisable(true);
			this.compileMenuItem.setDisable(true);
			this.executionInProgress = true;
		}
		
		try {
			InputStream sourceCodeIs = new ByteArrayInputStream(program.getBytes());
			
			EmulatorFacade raspEmulator = new EmulatorFacade(sourceCodeIs, this.console, this.console);
			
			raspEmulator.executeProgram();
		}
		catch(Exception e) {
			appendTextToErrorArea("Error during program execution!\n"
					+ "Error: " + e.getMessage()+"\n");
			appendTextToConsoleArea(e.getMessage()+"\n");
		}
		
		synchronized (lock) {
			this.executionInProgress = false;
			this.terminateMenuItem.setDisable(true);
			this.runMenuItem.setDisable(false);
			this.compileMenuItem.setDisable(false);
		}
	}

	public void closeProgram() {
		this.executor.shutdown();
		this.terminateProgram();
		Platform.exit();
	}

	private void appendTextToErrorArea(String text) {
		javafx.application.Platform.runLater(
				()-> {
					errorTextArea.appendText(text);
				}
			);
	}
	
	private void appendTextToConsoleArea(String text) {
		javafx.application.Platform.runLater(
				()-> {
					this.consoleTextArea.appendText(text);
				}
			);
	}
	
	
	
	
	private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = this.sourceProgramTextArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

	private Task<StyleSpans<Collection<String>>> computeHighlightingToCompiledAreaAsync() {
		String text = this.compiledProgramTextArea.getText();
		Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
			@Override
			protected StyleSpans<Collection<String>> call() throws Exception {
				return computeHighlightingForCompiledArea(text);
			}
		};
		executor.execute(task);
		return task;
	}

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        this.sourceProgramTextArea.setStyleSpans(0, highlighting);
    }

	private void applyHighlightingToCompiledArea(StyleSpans<Collection<String>> highlighting) {
		this.compiledProgramTextArea.setStyleSpans(0, highlighting);
	}

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

	private static StyleSpans<Collection<String>> computeHighlightingForCompiledArea(String text) {
		Matcher matcher = PATTERN_RASP.matcher(text);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder
				= new StyleSpansBuilder<>();
		while(matcher.find()) {
			String styleClass =
					matcher.group("KEYWORD") != null ? "keyword" :
							matcher.group("COLON") != null ? "semicolon" :
									matcher.group("MODE") != null ? "paren" :
											null; /* never happens */ assert styleClass != null;
			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}
    
	
	
	private class DefaultContextMenu extends ContextMenu
    {
        private MenuItem selectAll, copy, paste;

        public DefaultContextMenu(boolean pasteEnabled)
        {
        	selectAll = new MenuItem( "Select All" );
        	selectAll.setOnAction( AE -> { selectAll(); } );
        	
        	copy = new MenuItem( "Copy" );
        	copy.setOnAction( AE -> { copy(); } );
        	
        	if(pasteEnabled) {
        		paste = new MenuItem( "Paste" );
            	paste.setOnAction( AE -> { paste(); } );
            	
            	getItems().addAll( selectAll, copy, paste );
        	}else {
        		getItems().addAll( selectAll, copy );
        	}
        }

        private void selectAll() {
            ((CodeArea) getOwnerNode()).selectAll();
        }
        
        private void copy() {
            ((CodeArea) getOwnerNode()).copy();
        }
        
        private void paste() {
            ((CodeArea) getOwnerNode()).paste();
        }

    }

}
