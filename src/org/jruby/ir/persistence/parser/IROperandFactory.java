package org.jruby.ir.persistence.parser;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.BIG5Encoding;
import org.jcodings.specific.Big5HKSCSEncoding;
import org.jcodings.specific.Big5UAOEncoding;
import org.jcodings.specific.CP1251Encoding;
import org.jcodings.specific.CP949Encoding;
import org.jcodings.specific.EUCJPEncoding;
import org.jcodings.specific.EUCTWEncoding;
import org.jcodings.specific.EmacsMuleEncoding;
import org.jcodings.specific.GB18030Encoding;
import org.jcodings.specific.GBKEncoding;
import org.jcodings.specific.ISO8859_10Encoding;
import org.jcodings.specific.ISO8859_11Encoding;
import org.jcodings.specific.ISO8859_13Encoding;
import org.jcodings.specific.ISO8859_14Encoding;
import org.jcodings.specific.ISO8859_15Encoding;
import org.jcodings.specific.ISO8859_16Encoding;
import org.jcodings.specific.ISO8859_1Encoding;
import org.jcodings.specific.ISO8859_2Encoding;
import org.jcodings.specific.ISO8859_3Encoding;
import org.jcodings.specific.ISO8859_4Encoding;
import org.jcodings.specific.ISO8859_5Encoding;
import org.jcodings.specific.ISO8859_6Encoding;
import org.jcodings.specific.ISO8859_7Encoding;
import org.jcodings.specific.ISO8859_8Encoding;
import org.jcodings.specific.ISO8859_9Encoding;
import org.jcodings.specific.KOI8Encoding;
import org.jcodings.specific.KOI8REncoding;
import org.jcodings.specific.KOI8UEncoding;
import org.jcodings.specific.NonStrictEUCJPEncoding;
import org.jcodings.specific.NonStrictUTF8Encoding;
import org.jcodings.specific.SJISEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF16BEEncoding;
import org.jcodings.specific.UTF16LEEncoding;
import org.jcodings.specific.UTF32BEEncoding;
import org.jcodings.specific.UTF32LEEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.jruby.RubyLocalJumpError.Reason;
import org.jruby.ir.IRClosure;
import org.jruby.ir.IRScope;
import org.jruby.ir.operands.Array;
import org.jruby.ir.operands.AsString;
import org.jruby.ir.operands.Backref;
import org.jruby.ir.operands.BacktickString;
import org.jruby.ir.operands.Bignum;
import org.jruby.ir.operands.BooleanLiteral;
import org.jruby.ir.operands.ClosureLocalVariable;
import org.jruby.ir.operands.CompoundArray;
import org.jruby.ir.operands.CompoundString;
import org.jruby.ir.operands.CurrentScope;
import org.jruby.ir.operands.DynamicSymbol;
import org.jruby.ir.operands.Fixnum;
import org.jruby.ir.operands.Float;
import org.jruby.ir.operands.GlobalVariable;
import org.jruby.ir.operands.Hash;
import org.jruby.ir.operands.IRException;
import org.jruby.ir.operands.KeyValuePair;
import org.jruby.ir.operands.Label;
import org.jruby.ir.operands.LocalVariable;
import org.jruby.ir.operands.MethAddr;
import org.jruby.ir.operands.MethodHandle;
import org.jruby.ir.operands.Nil;
import org.jruby.ir.operands.NthRef;
import org.jruby.ir.operands.ObjectClass;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Range;
import org.jruby.ir.operands.Regexp;
import org.jruby.ir.operands.SValue;
import org.jruby.ir.operands.ScopeModule;
import org.jruby.ir.operands.Self;
import org.jruby.ir.operands.Splat;
import org.jruby.ir.operands.StandardError;
import org.jruby.ir.operands.StringLiteral;
import org.jruby.ir.operands.Symbol;
import org.jruby.ir.operands.TemporaryVariable;
import org.jruby.ir.operands.UndefinedValue;
import org.jruby.ir.operands.UnexecutableNil;
import org.jruby.ir.operands.WrappedIRClosure;
import org.jruby.util.KCode;
import org.jruby.util.RegexpOptions;

public enum IROperandFactory {
    INSTANCE;

    /** Array:[$operands] */
    public Array createArray(Operand[] operands) {
        return new Array(operands);
    }

    /** #{$source} */
    public AsString createAsString(Operand source) {
        return new AsString(source);
    }

    /** \$$name (e.g. $a) */
    public Backref createBackref(String name) {
        char t = name.charAt(0);
        return new Backref(t);
    }

    public BacktickString createBacktickString(Operand[] pieces) {
        return new BacktickString(Arrays.asList(pieces));
    }

    /** Bignum:$bignumString */
    public Bignum createBignum(String bignumString) {
        BigInteger value = new BigInteger(bignumString);
        return new Bignum(value);
    }

    /** true|false */
    public BooleanLiteral createBooleanLiteral(String booleanLiteralString) {
        boolean truthy = Boolean.parseBoolean(booleanLiteralString);
        return new BooleanLiteral(truthy);
    }

    public BooleanLiteral createTrueLiteral() {
        return new BooleanLiteral(true);
    }

    public BooleanLiteral createFalseLiteral() {
        return new BooleanLiteral(false);
    }

    /** <$name($scopeDepthString:$locationString)> */
    public ClosureLocalVariable createClosureLocalVariable(IRClosure scope, String name,
            String scopeDepthString, String locationString) {
        int scopeDepth = Integer.parseInt(scopeDepthString);
        int location = Integer.parseInt(locationString);
        return new ClosureLocalVariable(scope, name, scopeDepth, location);
    }

    /** ArgsPush:[$a1, $a2] */
    public CompoundArray createArgsPush(Operand a1, Operand a2) {
        return new CompoundArray(a1, a2, true);
    }

    /** ArgsCat:[$a1, $a2] */
    public CompoundArray createArgsCat(Operand a1, Operand a2) {
        return new CompoundArray(a1, a2, false);
    }

    /** CompoundString:$pieces */
    public CompoundString createCompoundString(Encoding encoding, Operand[] pieces) {
        return new CompoundString(Arrays.asList(pieces), encoding);
    }
    
    public Encoding createEncoding(String name) {
        if(name == null) {
            return null;
        } else if (ASCIIEncoding.INSTANCE.toString().equals(name)) {
            return ASCIIEncoding.INSTANCE;
        } else if (USASCIIEncoding.INSTANCE.toString().equals(name)) {
            return USASCIIEncoding.INSTANCE;
        } else if (UTF8Encoding.INSTANCE.equals(name)) {
            return UTF8Encoding.INSTANCE;
        } else if(BIG5Encoding.INSTANCE.toString().equals(name)) {
            return BIG5Encoding.INSTANCE;
        } else if (Big5HKSCSEncoding.INSTANCE.toString().equals(name)) {
            return Big5HKSCSEncoding.INSTANCE;
        } else if (Big5UAOEncoding.INSTANCE.toString().equals(name)) {
            return Big5UAOEncoding.INSTANCE;
        } else if (NonStrictEUCJPEncoding.INSTANCE.toString().equals(name)) {
            return NonStrictEUCJPEncoding.INSTANCE;
        } else if (SJISEncoding.INSTANCE.toString().equals(name)) {
            return SJISEncoding.INSTANCE;
        } else if (CP949Encoding.INSTANCE.toString().equals(name)) {
            return CP949Encoding.INSTANCE;
        } else if (GBKEncoding.INSTANCE.toString().equals(name)) {
            return GBKEncoding.INSTANCE;
        } else if (EmacsMuleEncoding.INSTANCE.toString().equals(name)) {
            return EmacsMuleEncoding.INSTANCE;
        } else if (EUCJPEncoding.INSTANCE.toString().equals(name)) {
            return EUCJPEncoding.INSTANCE;
        } else if (EUCTWEncoding.INSTANCE.toString().equals(name)) {
            return EUCTWEncoding.INSTANCE;
        } else if (GB18030Encoding.INSTANCE.toString().equals(name)) {
            return GB18030Encoding.INSTANCE;
        } else if (NonStrictUTF8Encoding.INSTANCE.toString().equals(name)) {
            return NonStrictUTF8Encoding.INSTANCE;
        } else if (UTF32BEEncoding.INSTANCE.toString().equals(name)) {
            return UTF32BEEncoding.INSTANCE;
        } else if (UTF32LEEncoding.INSTANCE.toString().equals(name)) {
            return UTF32LEEncoding.INSTANCE;
        } else if (UTF16BEEncoding.INSTANCE.toString().equals(name)) {
            return UTF16BEEncoding.INSTANCE;
        } else if (UTF16LEEncoding.INSTANCE.toString().equals(name)) {
            return UTF16LEEncoding.INSTANCE;
        } else if (CP1251Encoding.INSTANCE.toString().equals(name)) {
            return CP1251Encoding.INSTANCE;
        } else if (UTF32BEEncoding.INSTANCE.toString().equals(name)) {
            return UTF32BEEncoding.INSTANCE;
        } else if (ISO8859_10Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_10Encoding.INSTANCE;
        } else if (ISO8859_11Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_11Encoding.INSTANCE;
        } else if (ISO8859_13Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_13Encoding.INSTANCE;
        } else if (ISO8859_14Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_14Encoding.INSTANCE;
        } else if (ISO8859_15Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_15Encoding.INSTANCE;
        } else if (ISO8859_16Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_16Encoding.INSTANCE;
        } else if (ISO8859_1Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_1Encoding.INSTANCE;
        } else if (ISO8859_2Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_2Encoding.INSTANCE;
        } else if (ISO8859_3Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_3Encoding.INSTANCE;
        } else if (ISO8859_4Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_4Encoding.INSTANCE;
        } else if (ISO8859_5Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_5Encoding.INSTANCE;
        } else if (ISO8859_6Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_6Encoding.INSTANCE;
        } else if (ISO8859_7Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_7Encoding.INSTANCE;
        } else if (ISO8859_8Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_8Encoding.INSTANCE;
        } else if (ISO8859_9Encoding.INSTANCE.toString().equals(name)) {
            return ISO8859_9Encoding.INSTANCE;
        } else if (KOI8Encoding.INSTANCE.toString().equals(name)) {
            return KOI8Encoding.INSTANCE;
        } else if (KOI8REncoding.INSTANCE.toString().equals(name)) {
            return KOI8REncoding.INSTANCE;
        } else if (KOI8UEncoding.INSTANCE.toString().equals(name)) {
            return KOI8UEncoding.INSTANCE;
        } else {
            return UTF8Encoding.INSTANCE;
        }
    }

    /** scope<$name> */
    public CurrentScope createCurrentScope(String name) {
        // FIXME: its stubbed by current scope right now
        IRScope currentScope = IRParsingContext.INSTANCE.getCurrentScope();
        return new CurrentScope(currentScope);
    }

    /** :CompoundString$pieces */
    public DynamicSymbol createDynamicSymbol(CompoundString n) {
        return new DynamicSymbol(n);
    }

    /** Fixnum:$valueString */
    public Fixnum createFixnum(String valueString) {
        Long value = Long.valueOf(valueString);
        return new Fixnum(value);
    }

    /** Float:$valueString */
    public org.jruby.ir.operands.Float createFloat(String valueString) {
        Double value = Double.valueOf(valueString);
        return new Float(value);
    }

    public GlobalVariable createGlobalVariable(String name) {
        return new GlobalVariable(name);
    }

    public Hash createHash(KeyValuePair[] pairs) {
        return new Hash(Arrays.asList(pairs));
    }

    /** $key => $value */
    public KeyValuePair createKeyValuePair(Operand key, Operand value) {
        return new KeyValuePair(key, value);
    }

    public IRException createIRException(String type) {
        Reason reason = Reason.valueOf(type.toUpperCase());
        switch (reason) {
        case BREAK:
            return IRException.BREAK_LocalJumpError;
        case NEXT:
            return IRException.NEXT_LocalJumpError;
        case REDO:
            return IRException.REDO_LocalJumpError;
        case RETRY:
            return IRException.RETRY_LocalJumpError;
        case RETURN:
            return IRException.RETURN_LocalJumpError;
        default:
            // what is that?
            return null;
        }
    }

    /** $label */
    public Label createLabel(String label) {
        return new Label(label);
    }

    /** $name($scopeDepthString:$locationString) */
    public LocalVariable createLocalVariable(String name, String scopeDepthString,
            String locationString) {
        int scopeDepth = Integer.parseInt(scopeDepthString);
        int location = Integer.parseInt(locationString);
        
        return new LocalVariable(name, scopeDepth, location);
    }
    
    /** <$name($scopeDepthString:$locationString)> */
    public ClosureLocalVariable createClosureLocalVariable(String name, String scopeDepthString,
            String locationString) {
        int scopeDepth = Integer.parseInt(scopeDepthString);
        int location = Integer.parseInt(locationString);
        // FIXME: closure is null so far
        return new ClosureLocalVariable(null, name, scopeDepth, location);
    }

    /** $name */
    public MethAddr createMethAddr(String name) {
        return new MethAddr(name);
    }
    
    public MethAddr createUnknownSuperTarget() {
        return MethAddr.UNKNOWN_SUPER_TARGET;
    }

    /** <$receiver.$methodName> */
    public MethodHandle createMethodHandle(Operand methodName, Operand receiver) {
        return new MethodHandle(methodName, receiver);
    }

    /** nil */
    public Nil createNil() {
        return new Nil();
    }

    /** \$matchNumber */
    public NthRef createNthRef(String matchNumberString) {
        int matchNumber = Integer.parseInt(matchNumberString);
        return new NthRef(matchNumber);
    }

    /** <Class:Object> */
    public ObjectClass createObjectClass() {
        return new ObjectClass();
    }

    /** Range:($begin...$end) */
    public Range createInclusiveRange(Operand begin, Operand end) {
        return new Range(begin, end, false);
    }

    /** Range:($begin..$end) */
    public Range createExclusiveRange(Operand begin, Operand end) {
        return new Range(begin, end, true);
    }

    /** RE:|$regexp|$options */
    public Regexp createRegexp(Operand regexp, RegexpOptions options) {
        return new Regexp(regexp, options);
    }

    /**
     * RegexpOptions(kcode:$kcode(, encodingNone)?(, extended)?(, fixed)?(,
     * ignorecase)?(, java)?(, kcodeDefault)?(, literal)?(, multiline)?(,
     * once)?)
     */
    public RegexpOptions createRegexpOptions(String kcodeString, String[] options) {
        KCode kCode = KCode.valueOf(kcodeString);

        if (options != null) {
            List<String> optionList = Arrays.asList(options);

            boolean isKCodeDefault = false;
            if (optionList.contains("kcodeDefault")) {
                isKCodeDefault = true;
                // already used
                optionList.remove("kcodeDefault");
            }
            RegexpOptions result = new RegexpOptions(kCode, isKCodeDefault);

            for (String option : optionList) {
                if ("encodingNone".equals(option)) {
                    result.setEncodingNone(true);
                } else if ("extended".equals(option)) {
                    result.setExtended(true);
                } else if ("fixed".equals(option)) {
                    result.setFixed(true);
                } else if ("ignorecase".equals(option)) {
                    result.setIgnorecase(true);
                } else if ("java".equals(option)) {
                    result.setJava(true);
                } else if ("literal".equals(option)) {
                    result.setLiteral(true);
                } else if ("multiline".equals(option)) {
                    result.setMultiline(true);
                } else if ("once".equals(option)) {
                    result.setOnce(true);
                }
            }

            return result;
        } else {
            return new RegexpOptions(kCode, false);
        }

    }

    /** module<$name> */
    public ScopeModule createScopeModule(String name) {
        // FIXME: its stubbed by current scope right now
        IRScope currentScope = IRParsingContext.INSTANCE.getCurrentScope();
        return new ScopeModule(currentScope);
    }

    /** %self */
    public Self createSelf() {
        return Self.SELF;
    }

    /** *$array */
    public Splat createSplat(Operand array) {
        return new Splat(array);
    }

    /** StandardError */
    public StandardError createStandardError() {
        return new StandardError();
    }

    /** "$s" */
    public StringLiteral createStringLiteral(String s) {
        return new StringLiteral(s);
    }

    /** SValue:($array) */
    public SValue createSValue(Operand array) {
        return new SValue(array);
    }

    /** :$name */
    public Symbol createSymbol(String name) {
        return new Symbol(name);
    }

    /** %undefined */
    public UndefinedValue createUndefininedValue() {
        return UndefinedValue.UNDEFINED;
    }

    /** nil(unexecutable) */
    public UnexecutableNil createUnexecutableNil() {
        return UnexecutableNil.U_NIL;
    }
    
    public TemporaryVariable createTemporaryVariable(String name) {
        IRScope currentScope = IRParsingContext.INSTANCE.getCurrentScope();
        
        return currentScope.getNewTemporaryVariable("%" + name);
    }

    public WrappedIRClosure createWrappedIRClosure(IRClosure scope) {
        return new WrappedIRClosure(scope);
    }

}
