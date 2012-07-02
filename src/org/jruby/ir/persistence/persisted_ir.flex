package org.jruby.ir.persistence;

import java.util.Stack;

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
  Stack<Integer> prevStates;

  private Symbol token(short id)
  {
	return new Symbol(id, yyline + 1, yycolumn + 1, yylength(), yytext());
  }

  private Symbol token(short id, Object value)
  {
	return new Symbol(id, yyline + 1, yycolumn + 1, yylength(), value);
  }
  
  private Symbol enterState(int state)
  {
    int curState = yystate(); 
    prevStates.push(curState);
	yybegin(state);
  }
  
  private Symbol enterOperand(int state)
  {
    enterState(state);
    // fake state at the top of stack
    prevStates.push(state);
  }
  
  private Symbol returnToPrevState()
  {
    int prevState = prevStates.pop();
	yybegin(prevState);
  }
  
  private Symbol leaveOperand()
  {
    // two steps up
    prevStates.pop();
    returnToPrevState();
  }

%}

LineTerminator = \r|\n|\r\n
WhiteSpace = [ \t\f]

/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

/* Numbers */
FixnumLiteral = 0 | (-)?[1-9][0-9]*

FloatLiteral = (-)?({FLit1}|{FLit2}|{FLit3}) {Exponent}?

/* Float elements */
FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* String */
StringCharacter = [^\"]
SymbolCharacter = [^\']

Backref = "&" | "`" | "\'" | "+"
KCode = "NIL" | "NONE" | "UTF8" | "SJIS" | "EUC" 
CallType = "NORMAL" | "FUNCTIONAL" | "SUPER" | "VARIABLE" | "UNKNOWN"

%state OPERANDS, RVALUE, LOCAL_VARIABLE, STRING, SYMBOL, REGEXP, REGEXP_OPTIONS, KCODE, CALL

%%

<YYINITIAL> {
  {LineTerminator}               { return token(Terminals.EOLN); }
  
  /* instruction names */
  "alias"                        { yybegin(OPERANDS); return token(Terminals.ALIAS); }
  "block_given"                  { yybegin(OPERANDS); return token(Terminals.BLOCK_GIVEN); }
  "call"                         { yybegin(CALL); return token(Terminals.CALL); }
  
  "="                            { yybegin(RESULT_INSTR); return token(Terminals.EQ); }  
  
  // variable
  {Identifier}                   { enterState(VARIABLE); return token(Terminals.INSTR); }
  
  // beginning of temporary variable
  "%"                            { enterState(TEMP_VAR); return token(Terminals.PERCENT);
}

<STRING> {
  \"                             { yybegin(YYINITIAL); return token(Terminals.STRING_LITERAL, string.toString()); }

  {StringCharacter}+             { string.append( yytext() ); }
}

// Ignore whitespaces except for strings 
{WhiteSpace}                   { /* ignore */ }

<TEMP_VAR> {
  /* special cases */
  "self"                         { returnToPrevState(); return token(Terminals.SELF); }
  "undefined"                    { returnToPrevState(); return token(Terminals.UNDEFINED_VALUE); }
  
  {Identifier}                   { returnToPrevState(); return token(Terminals.IDENTIFIER); }
}

<VARIABLE> {
  {Identifier}                   { returnToPrevState(); return token(Terminals.IDENTIFIER); }
  "("                            { return token(Terminals.LPAREN); }
  ")"                            { return token(Terminals.RPAREN); }  
}

<RVALUE> {

  /* string literal */
  \"                             { yybegin(STRING); string.setLength(0); }
  
  /* symbol literal */
  \'                             { yybegin(SYMBOL); string.setLength(0); }
  
  /* instruction names */
  "block_given"                  { yybegin(OPERANDS); return token(Terminals.BLOCK_GIVEN); }
  
}

<OPERANDS> {
  /* separators */
  "("                            { return token(Terminals.LPAREN); }
  ")"                            { return token(Terminals.RPAREN); } 
    
}

<OPERAND> {
  /* operand markers */
  "Array:"                       { enterState(ARRAY); return token(Terminals.ARRAY_MARKER); }
  "BacktickString:"              { enterState(ARRAY); return token(Terminals.BACKTICK_STRING_MARKER); }
  "Bignum:"                      { enterState(FIXNUM); return token(Terminals.BIGNUM_MARKER); }
  "ArgsPush:"                    { enterState(ARRAY); return token(Terminals.ARGS_PUSH_MARKER); }
  "ArgsCat:"                     { enterState(ARRAY); return token(Terminals.ARGS_CAT_MARKER); }
  "ClosureLocalVariable:"        { enterState(CLOSURE_LOCAL_VAR); return token(Terminals.CLOSURE_LOCAL_VAR_MARKER); }
  "CompoundString:"              { enterState(ARRAY); return token(Terminals.COMPOUND_STRING_MARKER); }
  "scope"                        { enterState(CURRENT_SCOPE); return token(Terminals.SCOPE_MARKER); }
  "Fixnum:"                      { enterState(FIXNUM); return token(Terminals.FIXNUM_MARKER); }
  "Float:"                       { enterState(FLOAT); return token(Terminals.FLOAT_MARKER); }  
  "GlobalVariable:"              { /* String */ return token(Terminals.GLOBAL_VAR_MARKER); }
  "Hash:"                        { enterState(HASH); return token(Terminals.HASH_MARKER); }
  "LocalJumpError:"              { /* String */ return token(Terminals.IR_EXCEPTION_MARKER); }
  "Label:"                       { /* String */ return token(Terminals.LABEL_MARKER); }
  "MethAddr:"                    { /* String */ return token(Terminals.METH_ADDR_MARKER); }
  "MethodHandle:"                { enterState(METHOD_HANDLE); return token(Terminals.METH_HANDLE_MARKER); }
  "NthRef:"                      { enterState(NTH_REF); return token(Terminals.NTH_REF_MARKER); }
  "Range:"                       { enterState(RANGE); return token(Terminals.RANGE_MARKER); }
  "RE:"                          { enterState(REGEXP); return token(Terminals.REGEXP_MARKER); }  
  "module"                       { enterState(CURRENT_SCOPE); return token(Terminals.MODULE_MARKER); }
  "SValue:"                      { enterState(SVALUE); return token(Terminals.SVALUE_MARKER); }
  
  /* special symbols */
  "#"                            { enterState(AS_STRING); return token(Terminals.HASH); }
  "$"                            { enterState(BACKREF); return token(Terminals.DOLLAR); }
  // start of DynamicSymbol
  ":"                            { return token(Terminals.COLON); }
  // start of splat
  "*"                            { return token(Terminals.ASTERISK); }
  
  /* special cases */
  "-unknown-super-target-"       { returnToPrevState(); return token(Terminals.UNKNOWN_SUPER_TARGET); } 
  "<Class:Object>"               { returnToPrevState(); return token(Terminals.OBJECT_CLASS); }
  "StandardError"                { returnToPrevState(); return token(Terminals.STANDARD_ERROR); }
  "nil(unexecutable)"            { returnToPrevState(); return token(Terminals.UNEXECUTABLE_NIL); }
  
  /* nil literal */
  "nil"                          { returnToPrevState(); return token(Terminals.NIL); }
  
  /* boolean literals */
  "true"                         { returnToPrevState(); return token(Terminals.TRUE); }
  "false"                        { returnToPrevState(); return token(Terminals.FALSE); } 
  
  /* no match, try to find it in prev state */
  .                              { returnToPrevState(); yypushback(yylenght()); }
}

<ARRAY> {
  "["                            { enterState(OPERAND); return token(Terminals.LBRACK); }
  ","                            { enterState(OPERAND); return token(Terminals.COMMA); }
  "]"                            { leaveOperand(); return token(Terminals.RBRACK); }
}

<AS_STRING> {
  "{"                            { enterState(OPERAND); return token(Terminals.LBRACK); }
  "}"                            { leaveOperand(); return token(Terminals.RBRACK); }
}

<BACKREF> {
  {BACKREF}                      { leaveOperand(); return token(Terminals.BACKREF); }
}

<CLOSURE_LOCAL_VAR> {
  "<"                            { /* string */ return token(Terminals.LT); }
  "("                            { enterOperand(FIXNUM); return token(Terminals.LPAREN); }
  ":"                            { enterOperand(FIXNUM); return token(Terminals.COLON); }
  ")"                            { return token(Terminals.RPAREN); }  
  ">"                            { leaveOperand(); return token(Terminals.GT); }
}

<LOCAL_VAR> {
  // string
  "("                            { enterOperand(FIXNUM); return token(Terminals.LPAREN); }
  ":"                            { enterOperand(FIXNUM); return token(Terminals.COLON); }
  ")"                            { leaveOperand(); return token(Terminals.RPAREN); }
}

<CURRENT_SCOPE, CURRENT_MODULE> {
  "<"                            { return token(Terminals.LT); }
  // string
  ">"                            { leaveOperand(); return token(Terminals.GT); }
}

<FIXNUM> {
  {FixnumLiteral}                { leaveOperand(); return token(Terminals.FIXNUM_LITERAL); }
}

<FLOAT> {
  {FloatLiteral}                 { leaveOperand(); return token(Terminals.FLOAT_LITERAL); }
}

<HASH> {
  "{"                            { enterState(OPERAND); return token(Terminals.LPAREN); }
  "=>"                           { enterState(OPERAND); return token(Terminals.GE); }
  ","                            { enterState(OPERAND); return token(Terminals.COMMA); }
  "}"                            { leaveOperand(); return token(Terminals.RPAREN); }
}

<METHOD_HANDLE> {
  "<"                            { enterState(OPERAND); return token(Terminals.LT); }
  "."                            { enterState(OPERAND); return token(Terminals.DOT); }
  ">"                            { leaveOperand(); return token(Terminals.GT); }
}

<NTH_REF> {
  "$"                            { enterState(FIXNUM); return token(Terminals.DOLLAR); }
}

<RANGE> {
  "("                            { enterState(OPERAND); return token(Terminals.LPAREN); }
  
  /* range type markers */
  ".."                           { enterState(OPERAND); return token(Terminals.EXCLUSIVE); }
  "..."                          { enterState(OPERAND); return token(Terminals.INCLUSIVE); }
  
  ")"                            { leaveOperand(); return token(Terminals.RPAREN); }  
}

<SVALUE> {
  "("                            { enterOperand(OPERAND); return token(Terminals.LPAREN); }
  ")"                            { leaveOperand(); return token(Terminals.RPAREN); }
}

<SYMBOL> {
  \'                             { yybegin(YYINITIAL); return token(Terminals.SYMBOL_LITERAL, string.toString()); }

  {SymbolCharacter}+             { string.append( yytext() ); }
}

<REGEXP> {
  "|"                            { enterState(OPERAND); return token(Terminals.BAR); }
  "|RegexpOptions"               { yybegin(REGEXP_OPTIONS); return token(Terminals.REGEXP_OPTIONS_MARKER); }

}

<REGEXP_OPTIONS> {
  ","                            { return token(Terminals.COMMA); }
  "kcode:"                       { yybegin(KCODE); return token(Terminals.KCODE_MARKER); }
  "encodingNone"                 { return token(Terminals.ENC_NODE); }
  "extended"                     { return token(Terminals.EXPECTED); }
  "fixed"                        { return token(Terminals.FIXED); }
  "ignorecase"                   { return token(Terminals.IGNORECASE); }
  "java"                         { return token(Terminals.JAVA); }
  "kcodeDefault"                 { return token(Terminals.KCODE_DEFAULT); }
  "literal"                      { return token(Terminals.LITERAL); }
  "multiline"                    { return token(Terminals.MULTILINE); }
  "once"                         { return token(Terminals.ONCE); }
  ")"                            { yybegin(YYINITIAL); return token(Terminals.RPAREN); }
  "("                            { return token(Terminals.LPAREN); }
}

<KCODE> {
  {KCode}                        { yybegin(REGEXP_OPTIONS); return token(Terminals.KCODE); }
}

<CALL> {
  "["                            { return token(Terminals.LBRACK); }
  {CallType}                     { return token(Terminals.CALL_TYPE); }
  "]"                            { yybegin(OPERANDS); return token(Terminals.RBRACK); }
}


/* error fallback */
.|\n                             { throw new Scanner.Exception(yyline + 1, yycolumn + 1, "unrecognized character '" + yytext() + "'"); }