package org.jruby.ir.persistence;

import beaver.Symbol;
import beaver.Scanner;

import example.ExampleParser.Terminals;

/**
* Scanner for persisted IR
*/
%%

%class PersistedIRScanner
%extends Scanner

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
        StringBuilder string = new StringBuilder();

        private Symbol token (short id) {
	        return new Symbol(id, yyline + 1, yycolumn + 1, yylength(), yytext());
        }

        private Symbol token (short id, Object value) {
	        return new Symbol(id, yyline + 1, yycolumn + 1, yylength(), value);
        }
        
        private void appendToString() {
                string.append( yytext() );
        }
    
        private Symbol finishStringAs(short id) {
                yybegin(YYINITIAL); 
                String value = string.toString();
                string.setLength(0);
                return token(id, value);
        }

%}

LineTerminator = \r|\n|\r\n
WhiteSpace = [ \t\f]

/* identifiers */
Identifier = [a-zA-Z_][:jletterdigit:]*[!?]?

/* Numbers */
FixnumLiteral = 0 | [+-]?[1-9][0-9]*

FloatLiteral = [+-]?({FLit1}|{FLit2}|{FLit3}) {Exponent}?

/* Float elements */
FLit1 = [0-9]+ \. [0-9]*
FLit2 = \. [0-9]+
FLit3 = [0-9]+
Exponent = [eE][+-]?[0-9]+

/* String */
StringCharacter = [^\"\\\"]
SymbolCharacter = [^\']

InsideChevrons = [^<>] 

%state STRING, SYMBOL, INSIDE_CHEVRONS

%%

<YYINITIAL> {
    /* string literal */
    \"                                           { yybegin(STRING); }
    
    /* symbol literal */
    \'                                           { yybegin(SYMBOL); }                                                        
    
    /* whitespace */
    {WhiteSpace}                                 { /* ignore */ }
    
    {LineTerminator}                             { return token(Terminals.EOLN); }
    
    {FixnumLiteral}                              { return token(Terminals.FIXNUM); }
    
    {FloatLiteral}                               { return token(Terminals.FLOAT); }
    
    "="                                          { return token(Terminals.EQ); }
    
    /* scope markers */
    "Scope:"                                     { yybegin(INSIDE_CHEVRONS); return token(Terminals.SCOPE_START_MARKER); }

    /* operand markers */
    "Array:"                                     { return token(Terminals.ARRAY_MARKER); }
    "Bignum:"                                    { return token(Terminals.BIGNUM_MARKER); }
    "ArgsPush:"                                  { return token(Terminals.ARGS_PUSH_MARKER); }
    "ArgsCat:"                                   { return token(Terminals.ARGS_CAT_MARKER); }
    "CompoundString:"                            { return token(Terminals.COMPOUND_STRING_MARKER); }
    "scope"                                      { yybegin(INSIDE_CHEVRONS); return token(Terminals.SCOPE_MARKER); }
    "Fixnum:"                                    { return token(Terminals.FIXNUM_MARKER); }
    "Float:"                                     { return token(Terminals.FLOAT_MARKER); } 
    "LocalJumpError:"                            { return token(Terminals.IREXCEPTION_MARKER); }
    "RE:"                                        { return token(Terminals.REGEXP_MARKER); }    
    "RegexpOptions"                              { yybegin(REGEXP_OPTIONS); return token(Terminals.REGEXP_OPTIONS_MARKER); }
    "module"                                     { yybegin(INSIDE_CHEVRONS); return token(Terminals.MODULE_MARKER); }
    "SValue"                                     { return token(Terminals.SVALUE_MARKER); }
    
    /* special cases */
    "-unknown-super-target-"                     { return token(Terminals.UNKNOWN_SUPER_TARGET); } 
    "<Class:Object>"                             { return token(Terminals.OBJECT_CLASS); }
    "%self"                                      { return token(Terminals.SELF); }
    "StandardError"                              { return token(Terminals.STANDARD_ERROR); }
    "%undefined"                                 { return token(Terminals.UNDEFINED_VALUE); }
    "nil(unexecutable)"                          { return token(Terminals.UNEXECUTABLE_NIL); }
    
    /* nil literal */
    "nil"                                        { return token(Terminals.NIL); }
    
    /* boolean literals */
    "true"                                       { return token(Terminals.TRUE); }
    "false"                                      { return token(Terminals.FALSE); }
    
    /* range type markers */
    ".."                                         { return token(Terminals.EXCLUSIVE); }
    "..."                                        { return token(Terminals.INCLUSIVE); }
    
    "kcode:"                                     { return token(Terminals.KCODE_MARKER); }
    
    /* separators */
    "|"                                          { return token(Terminals.BAR); }
    "["                                          { return token(Terminals.LBRACK); }
    "]"                                          { return token(Terminals.RBRACK); }
    "("                                          { return token(Terminals.LPAREN); }
    ")"                                          { return token(Terminals.RPAREN); }
    "{"                                          { return token(Terminals.LBRACE); }
    "}"                                          { return token(Terminals.RBRACE); }
    "<"                                          { return token(Terminals.LT); }
    ">"                                          { return token(Terminals.GT); }
    ";"                                          { return token(Terminals.SEMICOLON); }
    ","                                          { return token(Terminals.COMMA); }
    "."                                          { return token(Terminals.DOT); }
    
    /* special symbols */
    "`"                                          { return token(Terminals.BACKTICK); }
    "*"                                          { return token(Terminals.ASTERISK); }
    "=>"                                         { return token(Terminals.GTE); }
    ":"                                          { return token(Terminals.COLON); }
    "$"                                          { return token(Terminals.DOLLAR); }
    "#"                                          { return token(Terminals.HASH); }
    "%"                                          { return token(Terminals.PERCENT);
}

<STRING> {
    \"                                           { return finishStringAs(Terminals.STRING_LITERAL); }
    "\\\""                                       { string.append("\""); }

    {StringCharacter}+                           { appendToString(); }
}

<SYMBOL> {
    \'                                           { return finishStringAs(Terminals.SYMBOL_LITERAL); }

    {SymbolCharacter}+                           { appendToString(); }
}

/* in case if scope or module contain whitespaces */
<INSIDE_CHEVRONS> {
    "<"                                          { return token(Terminals.LT); }
    {InsideChevrons}+                            { return token(Terminals.STRING); }
    ">"                                          { yybegin(YYINITIAL); return token(Terminals.GT); }    
}

/* error fallback */
.|\n                                             { throw new Scanner.Exception(yyline + 1, yycolumn + 1, "unrecognized character '" + yytext() + "'"); }