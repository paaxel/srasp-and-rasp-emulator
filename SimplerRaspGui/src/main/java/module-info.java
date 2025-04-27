module it.palex.raspgui {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.fxmisc.richtext;
    requires it.palex.srasp;
    requires it.palex.rasp;
    requires org.antlr.antlr4.runtime;
    requires org.apache.logging.log4j;
    requires org.fxmisc.flowless;
    requires reactfx;

    opens it.palex.raspgui to javafx.fxml;
    exports it.palex.raspgui;
}