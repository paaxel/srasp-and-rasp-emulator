module it.palex.srasp {
    requires org.antlr.antlr4.runtime;
    requires it.palex.rasp;

    exports it.palex.srasp;
    exports it.palex.srasp.compiler;
    exports it.palex.srasp.lang;
    exports it.palex.srasp.parser;
}