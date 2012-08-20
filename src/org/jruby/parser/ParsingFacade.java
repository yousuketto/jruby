package org.jruby.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.jruby.ParseResult;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.ast.Node;
import org.jruby.ir.IRScope;
import org.jruby.ir.persistence.IRPersistenceException;
import org.jruby.ir.persistence.read.IRReader;
import org.jruby.ir.persistence.util.IRFileExpert;
import org.jruby.ir.persistence.util.ProfilingContext;
import org.jruby.management.ParserStats;
import org.jruby.runtime.DynamicScope;
import org.jruby.util.ByteList;

public class ParsingFacade {
    
    private final ParserStats parserStats;
    private final Parser astParser;
    private final Ruby runtime;
    private final RubyInstanceConfig config;
    
    public ParsingFacade(Ruby runtime) {
        this.runtime = runtime;
        config = runtime.getInstanceConfig();
        astParser = new Parser(runtime);
        parserStats = new ParserStats(runtime);
    }
    
    public ParserStats getParserStats() {
        return parserStats;
    }

    public Parser getASTParser() {
        return astParser;
    }
    
    @Deprecated    
    public Node parseFile(InputStream in, String file, DynamicScope scope, int lineNumber) {
        addLoadParseToStats();
        return parseRbAndGetAST(in, file, scope, lineNumber, false);
        
    }
    
    // Modern variant of parseFile function from above
    public ParseResult parseFile(String file, InputStream in, DynamicScope scope, int lineNumber) {
        addLoadParseToStats();
        ProfilingContext.INSTANCE.enterFile(file);
        if(RubyInstanceConfig.IR_READING) {
            try {
                return parseIRAndGetIRScope(file);
            } catch (Exception e) {
                System.out.println(e);
                // If something gone wrong with ir -
                return parseRbAndGetAST(in, file, scope, lineNumber, false);
            }
            
        } else { // Read .rb file
            ProfilingContext.INSTANCE.start(".rb -> AST");
            Node ast = parseRbAndGetAST(in, file, scope, lineNumber, false);
            ProfilingContext.INSTANCE.stop();
            
            return ast;
        }
    }    
    
    @Deprecated
    public Node parseFileFromMain(InputStream in, String file, DynamicScope scope) {
        addLoadParseToStats();
        return parseFileFromMainAndGetAST(in, file, scope);
    }
    
    // Modern variant of parseFileFromMain function from above
    public ParseResult parseFileFromMain(String file, InputStream in, DynamicScope scope) {
        addLoadParseToStats();
        
        if(RubyInstanceConfig.IR_READING) {
            try {
                return parseIRAndGetIRScope(file);
            } catch (Exception e) {
                System.out.println(e);
                return parseFileFromMainAndGetAST(in, file, scope);
            }
        } else {
            ProfilingContext.INSTANCE.start(".rb -> AST");
            Node ast = parseFileFromMainAndGetAST(in, file, scope);
            ProfilingContext.INSTANCE.stop();
            
            return ast;
        }
    }

    private ParseResult parseIRAndGetIRScope(String file) throws FileNotFoundException,
            IRPersistenceException, IOException {
        InputStream irIn = null;
        try {
            // Get IR from .ir file
            File irFile = IRFileExpert.INSTANCE.getIrFileForReading(file);
            irIn = new FileInputStream(irFile);
            ProfilingContext.INSTANCE.start(".ir -> IR");
            IRScope irScope = IRReader.read(irIn, runtime);
            ProfilingContext.INSTANCE.stop();
            return irScope;
        } finally {
            if (irIn != null) {
                irIn.close();
            }
        }
    }
    
    private Node parseFileFromMainAndGetAST(InputStream in, String file, DynamicScope scope) {
        return parseRbAndGetAST(in, file, scope, 0, true);
    }
    
    private Node parseRbAndGetAST(InputStream in, String file, DynamicScope scope, int lineNumber, boolean isFromMain) {
        return astParser.parse(file, in, scope, new ParserConfiguration(runtime,
                lineNumber, false, false, true, isFromMain, config));
    }
    
    

    public Node parseInline(InputStream in, String file, DynamicScope scope) {
        addEvalParseToStats();
        ParserConfiguration parserConfig = new ParserConfiguration(runtime, 0, false, true, false,
               config);
        if (runtime.is1_9()) parserConfig.setDefaultEncoding(runtime.getEncodingService().getLocaleEncoding());
        return astParser.parse(file, in, scope, parserConfig);
    }

    public Node parseEval(String content, String file, DynamicScope scope, int lineNumber) {
        addEvalParseToStats();
        return astParser.parse(file, content.getBytes(), scope, new ParserConfiguration(runtime,
                lineNumber, false, false, false, false, config));
    }

    @Deprecated
    public Node parse(String content, String file, DynamicScope scope, int lineNumber, 
            boolean extraPositionInformation) {
        return astParser.parse(file, content.getBytes(), scope, new ParserConfiguration(runtime,
                lineNumber, extraPositionInformation, false, true, config));
    }
    
    public Node parseEval(ByteList content, String file, DynamicScope scope, int lineNumber) {
        addEvalParseToStats();
        return astParser.parse(file, content, scope, new ParserConfiguration(runtime,
                   lineNumber, false, false, false, config));
    }
    
    public Node parse(ByteList content, String file, DynamicScope scope, int lineNumber, 
            boolean extraPositionInformation) {
        addJRubyModuleParseToStats();
        return astParser.parse(file, content, scope, new ParserConfiguration(runtime,
            lineNumber, extraPositionInformation, false, true, config));       
    }
    
    // Parser stats methods
    private void addLoadParseToStats() {
        if (parserStats != null) parserStats.addLoadParse();
    }
    
    private void addEvalParseToStats() {
        if (parserStats != null) parserStats.addEvalParse();
    }
    
    private void addJRubyModuleParseToStats() {
        if (parserStats != null) parserStats.addJRubyModuleParse();
    }

}
