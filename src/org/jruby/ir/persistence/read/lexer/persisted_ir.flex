package org.jruby.ir.persistence.read.lexer;

import beaver.Symbol;
import beaver.Scanner;

import org.jruby.ir.persistence.read.parser.PersistedIRParser.Terminals;

/**
* Scanner for persisted IR
*/
%%

%class PersistedIRScanner
%extends Scanner
%public

%function nextToken
%type Symbol
%yylexthrow Scanner.Exception
%eofval{
	return new Symbol(Terminals.EOF, "end-of-file");
%eofval}

%unicode

%line
%column

%{

        {
            // Make them start from 1's
            yyline++;
            yycolumn++;
        }
        
        private final StringBuilder string = new StringBuilder();

        private Symbol token (short id) {
	        return new Symbol(id, yyline, yycolumn, yylength(), yytext());
        }

        private Symbol token (short id, Object value) {
	        return new Symbol(id, yyline, yycolumn, yylength(), value);
        }
        
        private void appendToString() {
            string.append( yytext() );
        }
    
        private Symbol finishString() {
            yybegin(YYINITIAL); 
            String value = string.toString();
            string.setLength(0);
            return token(Terminals.STRING, value);
        }

%}

LineTerminator = \r|\n|\r\n
WhiteSpace = [ \t\f]

/* Identifiers */
Identifier = [A-Z][A-Z_1-9]*

/* Boolean */
BooleanLiteral = (true|false)

/* Numbers */
FixnumLiteral = 0 | [+-]?[1-9][0-9]*

FloatLiteral = [+-]?{FLit} {Exponent}?

/* Float elements */
FLit = [0-9]+ \. [0-9]+
Exponent = E[+-]?[0-9]+

/* String */
StringCharacter = [^\"\\]

%state STRING

%%

<YYINITIAL> {
    /* String literal */
    \"                                           { yybegin(STRING); }                                                       
    
    
    {WhiteSpace}                                 { /* ignore */ }
    
    {LineTerminator}                             { return token(Terminals.EOLN); }
    
    {FixnumLiteral}                              { return token(Terminals.FIXNUM); }
    
    {FloatLiteral}                               { return token(Terminals.FLOAT); }
    
    "="                                          { return token(Terminals.EQ); }
    
    {BooleanLiteral}                             { return token(Terminals.BOOLEAN); }
    
    "null"                                       { return token(Terminals.NULL); }
    
    /* Markers that are common for all instructions */
    "[DEAD]"                                     { return token(Terminals.DEAD_INSTR_MARKER); }
    "[DEAD-RESULT]"                              { return token(Terminals.DEAD_RESULT_INSTR_MARKER); }
    
    {Identifier}                                 { return token(Terminals.ID); }
    
    /* separators */
    "["                                          { return token(Terminals.LBRACK); }
    "]"                                          { return token(Terminals.RBRACK); }
    "("                                          { return token(Terminals.LPAREN); }
    ")"                                          { return token(Terminals.RPAREN); }
    "{"                                          { return token(Terminals.LBRACE); }
    "}"                                          { return token(Terminals.RBRACE); }
    "<"                                          { return token(Terminals.LT); }
    ">"                                          { return token(Terminals.GT); }
    ","                                          { return token(Terminals.COMMA); }
}

<STRING> {
    \"                                           { return finishString(); }
    \\\"                                         { string.append("\""); }
    \\                                           { string.append("\\"); }

    {StringCharacter}+                           { appendToString(); }
}

/* error fallback */
.|\n                                             { throw new Scanner.Exception(yyline, yycolumn, "unrecognized character '" + yytext() + "'"); }