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
        static final Symbol LBRACK = new Symbol(Terminals.LBRACK, 0, 0, 0, "[");
        static final Symbol RBRACK = new Symbol(Terminals.RBRACK, 0, 0, 0, "]");
        static final Symbol LPAREN = new Symbol(Terminals.LPAREN, 0, 0, 0, "(");
        static final Symbol RPAREN = new Symbol(Terminals.RPAREN, 0, 0, 0, ")");
        static final Symbol LBRACE = new Symbol(Terminals.LBRACE, 0, 0, 0, "{");
        static final Symbol RBRACE = new Symbol(Terminals.RBRACE, 0, 0, 0, "}");
        static final Symbol LT = new Symbol(Terminals.LT, 0, 0, 0, "<");
        static final Symbol GT = new Symbol(Terminals.GT, 0, 0, 0, ">");
        static final Symbol COMMA = new Symbol(Terminals.COMMA, 0, 0, 0, ",");
        static final Symbol EQ = new Symbol(Terminals.EQ, 0, 0, 0, "=");
        static final Symbol NULL_SYM = new Symbol(Terminals.NULL, 0, 0, 0, "null");
        static final Symbol DEAD_INSTR_MARKER = new Symbol(Terminals.DEAD_INSTR_MARKER, 0, 0, 0, "[DEAD]");
        static final Symbol DEAD_RESULT_INSTR_MARKER = new Symbol(Terminals.DEAD_RESULT_INSTR_MARKER, 0, 0, 0, "[DEAD-RESULT]");

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
            string.setLength(0); // reset for next use
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
    {FixnumLiteral}                              { return token(Terminals.FIXNUM); }
    {FloatLiteral}                               { return token(Terminals.FLOAT); }
    {BooleanLiteral}                             { return token(Terminals.BOOLEAN); }
    {Identifier}                                 { return token(Terminals.ID); }
    {LineTerminator}                             { return token(Terminals.EOLN); }

    "="                                          { return EQ; }
    "null"                                       { return NULL_SYM; }

    /* Markers that are common for all instructions */
    "[DEAD]"                                     { return DEAD_INSTR_MARKER; }
    "[DEAD-RESULT]"                              { return DEAD_RESULT_INSTR_MARKER; }

    /* separators */
    "["                                          { return LBRACK; }
    "]"                                          { return RBRACK; }
    "("                                          { return LPAREN; }
    ")"                                          { return RPAREN; }
    "{"                                          { return LBRACE; }
    "}"                                          { return RBRACE; }
    "<"                                          { return LT; }
    ">"                                          { return GT; }
    ","                                          { return COMMA; }
}

<STRING> {
    \"                                           { return finishString(); }
    \\\"                                         { string.append("\""); }
    \\                                           { string.append("\\"); }

    {StringCharacter}+                           { appendToString(); }
}

/* error fallback */
.|\n                                             { throw new Scanner.Exception(yyline, yycolumn, "unrecognized character '" + yytext() + "'"); }
