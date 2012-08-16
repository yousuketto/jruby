package org.jruby.ir.persistence.persist.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jruby.ir.IRClassBody;
import org.jruby.ir.IRClosure;
import org.jruby.ir.IRMethod;
import org.jruby.ir.IRModuleBody;
import org.jruby.ir.IRScope;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.instructions.AliasInstr;
import org.jruby.ir.instructions.AttrAssignInstr;
import org.jruby.ir.instructions.BEQInstr;
import org.jruby.ir.instructions.BFalseInstr;
import org.jruby.ir.instructions.BNEInstr;
import org.jruby.ir.instructions.BNilInstr;
import org.jruby.ir.instructions.BTrueInstr;
import org.jruby.ir.instructions.BUndefInstr;
import org.jruby.ir.instructions.BlockGivenInstr;
import org.jruby.ir.instructions.BranchInstr;
import org.jruby.ir.instructions.BreakInstr;
import org.jruby.ir.instructions.CallBase;
import org.jruby.ir.instructions.CallInstr;
import org.jruby.ir.instructions.CheckArgsArrayArityInstr;
import org.jruby.ir.instructions.CheckArityInstr;
import org.jruby.ir.instructions.ClassSuperInstr;
import org.jruby.ir.instructions.ClosureReturnInstr;
import org.jruby.ir.instructions.ConstMissingInstr;
import org.jruby.ir.instructions.CopyInstr;
import org.jruby.ir.instructions.DefineClassInstr;
import org.jruby.ir.instructions.DefineClassMethodInstr;
import org.jruby.ir.instructions.DefineInstanceMethodInstr;
import org.jruby.ir.instructions.DefineMetaClassInstr;
import org.jruby.ir.instructions.DefineModuleInstr;
import org.jruby.ir.instructions.EQQInstr;
import org.jruby.ir.instructions.EnsureRubyArrayInstr;
import org.jruby.ir.instructions.ExceptionRegionEndMarkerInstr;
import org.jruby.ir.instructions.ExceptionRegionStartMarkerInstr;
import org.jruby.ir.instructions.GVarAliasInstr;
import org.jruby.ir.instructions.GetClassVarContainerModuleInstr;
import org.jruby.ir.instructions.GetClassVariableInstr;
import org.jruby.ir.instructions.GetFieldInstr;
import org.jruby.ir.instructions.GetGlobalVariableInstr;
import org.jruby.ir.instructions.GetInstr;
import org.jruby.ir.instructions.InheritanceSearchConstInstr;
import org.jruby.ir.instructions.InstanceOfInstr;
import org.jruby.ir.instructions.InstanceSuperInstr;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.instructions.JumpIndirectInstr;
import org.jruby.ir.instructions.JumpInstr;
import org.jruby.ir.instructions.LabelInstr;
import org.jruby.ir.instructions.LexicalSearchConstInstr;
import org.jruby.ir.instructions.LineNumberInstr;
import org.jruby.ir.instructions.LoadLocalVarInstr;
import org.jruby.ir.instructions.Match2Instr;
import org.jruby.ir.instructions.Match3Instr;
import org.jruby.ir.instructions.MatchInstr;
import org.jruby.ir.instructions.MethodLookupInstr;
import org.jruby.ir.instructions.ModuleVersionGuardInstr;
import org.jruby.ir.instructions.MultipleAsgnBase;
import org.jruby.ir.instructions.NoResultCallInstr;
import org.jruby.ir.instructions.NopInstr;
import org.jruby.ir.instructions.NotInstr;
import org.jruby.ir.instructions.OptArgMultipleAsgnInstr;
import org.jruby.ir.instructions.PopBindingInstr;
import org.jruby.ir.instructions.PopFrameInstr;
import org.jruby.ir.instructions.ProcessModuleBodyInstr;
import org.jruby.ir.instructions.PushBindingInstr;
import org.jruby.ir.instructions.PushFrameInstr;
import org.jruby.ir.instructions.PutClassVariableInstr;
import org.jruby.ir.instructions.PutConstInstr;
import org.jruby.ir.instructions.PutFieldInstr;
import org.jruby.ir.instructions.PutGlobalVarInstr;
import org.jruby.ir.instructions.PutInstr;
import org.jruby.ir.instructions.RaiseArgumentErrorInstr;
import org.jruby.ir.instructions.ReceiveArgBase;
import org.jruby.ir.instructions.ReceiveClosureInstr;
import org.jruby.ir.instructions.ReceiveExceptionInstr;
import org.jruby.ir.instructions.ReceivePreReqdArgInstr;
import org.jruby.ir.instructions.ReceiveSelfInstr;
import org.jruby.ir.instructions.RecordEndBlockInstr;
import org.jruby.ir.instructions.ReqdArgMultipleAsgnInstr;
import org.jruby.ir.instructions.RescueEQQInstr;
import org.jruby.ir.instructions.RestArgMultipleAsgnInstr;
import org.jruby.ir.instructions.ReturnInstr;
import org.jruby.ir.instructions.SearchConstInstr;
import org.jruby.ir.instructions.SetReturnAddressInstr;
import org.jruby.ir.instructions.StoreLocalVarInstr;
import org.jruby.ir.instructions.SuperInstrType;
import org.jruby.ir.instructions.ThreadPollInstr;
import org.jruby.ir.instructions.ThrowExceptionInstr;
import org.jruby.ir.instructions.ToAryInstr;
import org.jruby.ir.instructions.UndefMethodInstr;
import org.jruby.ir.instructions.UnresolvedSuperInstr;
import org.jruby.ir.instructions.YieldInstr;
import org.jruby.ir.instructions.ZSuperInstr;
import org.jruby.ir.instructions.defined.BackrefIsMatchDataInstr;
import org.jruby.ir.instructions.defined.ClassVarIsDefinedInstr;
import org.jruby.ir.instructions.defined.DefinedObjectNameInstr;
import org.jruby.ir.instructions.defined.GetBackrefInstr;
import org.jruby.ir.instructions.defined.GetDefinedConstantOrMethodInstr;
import org.jruby.ir.instructions.defined.GetErrorInfoInstr;
import org.jruby.ir.instructions.defined.GlobalIsDefinedInstr;
import org.jruby.ir.instructions.defined.HasInstanceVarInstr;
import org.jruby.ir.instructions.defined.IsMethodBoundInstr;
import org.jruby.ir.instructions.defined.MethodDefinedInstr;
import org.jruby.ir.instructions.defined.MethodIsPublicInstr;
import org.jruby.ir.instructions.defined.RestoreErrorInfoInstr;
import org.jruby.ir.instructions.defined.SuperMethodBoundInstr;
import org.jruby.ir.instructions.ruby18.ReceiveOptArgInstr18;
import org.jruby.ir.instructions.ruby18.ReceiveRestArgInstr18;
import org.jruby.ir.instructions.ruby19.BuildLambdaInstr;
import org.jruby.ir.instructions.ruby19.GetEncodingInstr;
import org.jruby.ir.instructions.ruby19.ReceiveOptArgInstr19;
import org.jruby.ir.instructions.ruby19.ReceivePostReqdArgInstr;
import org.jruby.ir.instructions.ruby19.ReceiveRestArgInstr19;
import org.jruby.ir.instructions.specialized.OneArgOperandAttrAssignInstr;
import org.jruby.ir.instructions.specialized.OneFixnumArgNoBlockCallInstr;
import org.jruby.ir.instructions.specialized.OneOperandArgNoBlockCallInstr;
import org.jruby.ir.instructions.specialized.OneOperandArgNoBlockNoResultCallInstr;
import org.jruby.ir.instructions.specialized.SpecializedInstType;
import org.jruby.ir.instructions.specialized.ZeroOperandArgNoBlockCallInstr;
import org.jruby.ir.operands.BooleanLiteral;
import org.jruby.ir.operands.GlobalVariable;
import org.jruby.ir.operands.Label;
import org.jruby.ir.operands.LocalVariable;
import org.jruby.ir.operands.MethAddr;
import org.jruby.ir.operands.MethodHandle;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.StringLiteral;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.persistence.persist.string.producer.AbstractIRStringBuilder;
import org.jruby.ir.persistence.persist.string.producer.IRInstructionStringBuilder;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.runtime.CallType;

class IRInstrStringExtractor extends IRVisitor {
    
    private final IRInstructionStringBuilder stringProducer;
    
    private IRInstrStringExtractor(final IRInstructionStringBuilder stringProducer) {
        this.stringProducer = stringProducer;
    }
    
    // Static factories that are used in translator
    static IRInstrStringExtractor createToplevelInstance() {
        IRInstructionStringBuilder stringProducer = new IRInstructionStringBuilder(null);
        return new IRInstrStringExtractor(stringProducer);
    }    
    static IRInstrStringExtractor createInstance(AbstractIRStringBuilder builder) {
        IRInstructionStringBuilder stringProducer = new IRInstructionStringBuilder(builder);
        return new IRInstrStringExtractor(stringProducer);
    }
    
    public String extract(final Instr instr) {
        produceString(instr);
        
        return stringProducer.getResultString();
    }
    
    public void produceString(final Instr instr) {
        stringProducer.appendPrefix(instr);

        instr.visit(this);

        stringProducer.appendMarkers(instr);
    }
    
 // Instructions without parameters

    public void BackrefIsMatchDataInstr(BackrefIsMatchDataInstr backrefismatchdatainstr) {}
    public void BlockGivenInstr(BlockGivenInstr blockgiveninstr) {}
    public void ExceptionRegionEndMarkerInstr(ExceptionRegionEndMarkerInstr exceptionregionendmarkerinstr) {}
    public void NopInstr(NopInstr nopinstr) {}
    public void PopBindingInstr(PopBindingInstr popbindinginstr) {}
    public void PopFrameInstr(PopFrameInstr popframeinstr) {}
    public void PushFrameInstr(PushFrameInstr pushframeinstr) {}
    public void ReceiveClosureInstr(ReceiveClosureInstr receiveclosureinstr) {}
    public void ReceiveSelfInstr(ReceiveSelfInstr receiveselfinstr) {}
    public void GetBackrefInstr(GetBackrefInstr getbackrefinstr) {}
    public void GetErrorInfoInstr(GetErrorInfoInstr geterrorinfoinstr) {}
    
    // Branch Instructions

    public void BEQInstr(final BEQInstr beqinstr) {
        commonForBranchInstrWithArg2(beqinstr);
    }

    public void BNEInstr(final BNEInstr bneinstr) {
        commonForBranchInstrWithArg2(bneinstr);
    }

    private void commonForBranchInstrWithArg2(final BranchInstr branchInstr) {
        final Operand arg1 = branchInstr.getArg1();
        final Operand arg2 = branchInstr.getArg2();
        final Label jumpTarget = branchInstr.getJumpTarget();
        
        stringProducer.appendParameters(arg1, arg2, jumpTarget);
    }

    public void BFalseInstr(final BFalseInstr bfalseinstr) {
        commonForBranchInstrWithoutArg2(bfalseinstr);
    }

    public void BNilInstr(final BNilInstr bnilinstr) {
        commonForBranchInstrWithoutArg2(bnilinstr);
    }

    public void BTrueInstr(final BTrueInstr btrueinstr) {
        commonForBranchInstrWithoutArg2(btrueinstr);
    }

    public void BUndefInstr(final BUndefInstr bundefinstr) {
        commonForBranchInstrWithoutArg2(bundefinstr);
    }

    private void commonForBranchInstrWithoutArg2(final BranchInstr branchInstr) {
        final Operand arg1 = branchInstr.getArg1();
        final Label jumpTarget = branchInstr.getJumpTarget();
        
        stringProducer.appendParameters(arg1, jumpTarget);
    }

    // Call Instructions

    public void CallInstr(final CallInstr callinstr) {
        commonForUnspecializedGeneralCallInstr(callinstr);
    }
    
    public void NoResultCallInstr(final NoResultCallInstr noresultcallinstr) {
        commonForUnspecializedGeneralCallInstr(noresultcallinstr);
    }

    private void commonForUnspecializedGeneralCallInstr(final CallBase callBase) {
        final List<Object> parameters = getParametersForGeneralCallInstr(callBase);
        
        final Operand closure = callBase.getClosure();        
        parameters.add(closure);
        
        stringProducer.appendParameters(parameters.toArray());
    }
    
    private List<Object> getParametersForGeneralCallInstr(final CallBase callBase) {
        final List<Object> parameters = new ArrayList<Object>(5);
        
        final Operand receiver = callBase.getReceiver();
        final CallType callType = callBase.getCallType();
        final MethAddr methodAddr = callBase.getMethodAddr();
        final Operand[] callArgs = callBase.getCallArgs();
        
        Collections.addAll(parameters, receiver, callType, methodAddr, callArgs);
        
        return parameters;
    }

    // specialized CallInstr
    public void OneFixnumArgNoBlockCallInstr(
            final OneFixnumArgNoBlockCallInstr onefixnumargnoblockcallinstr) {
        commonForSpecializedGeneralCallInstr(onefixnumargnoblockcallinstr, SpecializedInstType.ONE_FIXNUM);
    }

    public void OneOperandArgNoBlockCallInstr(
            final OneOperandArgNoBlockCallInstr oneoperandargnoblockcallinstr) {
        commonForSpecializedGeneralCallInstr(oneoperandargnoblockcallinstr, SpecializedInstType.ONE_OPERAND);
    }

    public void ZeroOperandArgNoBlockCallInstr(
            final ZeroOperandArgNoBlockCallInstr zerooperandargnoblockcallinstr) {
        commonForSpecializedGeneralCallInstr(zerooperandargnoblockcallinstr, SpecializedInstType.ZERO_OPERAND);
    }    

    // specialized NoResultCallInstr
    public void OneOperandArgNoBlockNoResultCallInstr(
            final OneOperandArgNoBlockNoResultCallInstr oneoperandargnoblocknoresultcallinstr) {
        commonForSpecializedGeneralCallInstr(oneoperandargnoblocknoresultcallinstr, SpecializedInstType.ONE_OPERAND);
    }

    private void commonForSpecializedGeneralCallInstr(
            final CallBase callInstr, final SpecializedInstType type) {
        final List<Object> parameters = getParametersForGeneralCallInstr(callInstr);
        parameters.add(type);
        
        stringProducer.appendParameters(parameters.toArray());
    }

    public void AttrAssignInstr(final AttrAssignInstr attrassigninstr) {
        final List<Object> parameters = getCommonParametersForAttrAssign(attrassigninstr);
        
        stringProducer.appendParameters(parameters.toArray());
    }

    // Specialized AttrAssignInstr
    public void OneArgOperandAttrAssignInstr(
            final OneArgOperandAttrAssignInstr oneargoperandattrassigninstr) {
        final List<Object> parameters = getCommonParametersForAttrAssign(oneargoperandattrassigninstr);
        parameters.add(SpecializedInstType.ONE_OPERAND);
        
        stringProducer.appendParameters(parameters);
    }

    private List<Object> getCommonParametersForAttrAssign(final AttrAssignInstr attrassigninstr) {        
        final List<Object> parameters = new ArrayList<Object>(4);
        
        final Operand receiver = attrassigninstr.getReceiver();
        final MethAddr methodAddr = attrassigninstr.getMethodAddr();
        final Operand[] callArgs = attrassigninstr.getCallArgs();
        
        Collections.addAll(parameters, receiver, methodAddr, callArgs);
        
        return parameters;
    }

    public void ClassSuperInstr(final ClassSuperInstr classsuperinstr) {        
        commonForResolvedSupeInstr(classsuperinstr, SuperInstrType.CLASS);        
    }
    
    public void InstanceSuperInstr(final InstanceSuperInstr instancesuperinstr) {
        commonForResolvedSupeInstr(instancesuperinstr, SuperInstrType.INSTANCE);
    }

    private void commonForResolvedSupeInstr(final CallInstr superInstr, final SuperInstrType type) {
        final Operand receiver = superInstr.getReceiver();
        final MethAddr methodAddr = superInstr.getMethodAddr();
        final Operand[] callArgs = superInstr.getCallArgs();
        final Operand closure = superInstr.getClosure();
        
        stringProducer.appendParameters(type, receiver, methodAddr, callArgs, closure);
    }

    public void ConstMissingInstr(final ConstMissingInstr constmissinginstr) {
        final Operand receiver = constmissinginstr.getReceiver();
        final String missingConst = constmissinginstr.getMissingConst();
        
        stringProducer.appendParameters(receiver, missingConst);
    }    

    public void UnresolvedSuperInstr(final UnresolvedSuperInstr unresolvedsuperinstr) {
        final Operand receiver = unresolvedsuperinstr.getReceiver();
        final Operand[] callArgs = unresolvedsuperinstr.getCallArgs();
        final Operand closure = unresolvedsuperinstr.getClosure();
        
        stringProducer.appendParameters(SuperInstrType.UNRESOLVED, receiver, callArgs, closure);
    }

    public void ZSuperInstr(final ZSuperInstr zsuperinstr) {
        final Operand receiver = zsuperinstr.getReceiver();
        final Operand closure = zsuperinstr.getClosure();
        
        stringProducer.appendParameters(receiver, closure);
    }

    // Get Instructions

    public void GetClassVariableInstr(final GetClassVariableInstr getclassvariableinstr) {
        coomonForMostGetInstr(getclassvariableinstr);
    }

    public void GetFieldInstr(final GetFieldInstr getfieldinstr) {
        coomonForMostGetInstr(getfieldinstr);
    }

    private void coomonForMostGetInstr(final GetInstr getInstr) {
        final Operand source = getInstr.getSource();
        final String ref = getInstr.getRef();
        
        stringProducer.appendParameters(source, ref);
    }

    public void GetGlobalVariableInstr(final GetGlobalVariableInstr getglobalvariableinstr) {
        Operand source = getglobalvariableinstr.getSource();
        
        stringProducer.appendParameters(source);
    }

    // Jump Instructions

    public void JumpIndirectInstr(final JumpIndirectInstr jumpindirectinstr) {
        final Variable jumpTarget = jumpindirectinstr.getJumpTarget();
        
        stringProducer.appendParameters(jumpTarget);
    }

    public void JumpInstr(final JumpInstr jumpinstr) {
        final Label jumpTarget = jumpinstr.getJumpTarget();
        
        stringProducer.appendParameters(jumpTarget);
    }

    // Label Instruction
    public void LabelInstr(final LabelInstr labelinstr) {
        final Label label = labelinstr.getLabel();        
        
        stringProducer.appendParameters(label);
    }

    // Put instructions

    public void PutClassVariableInstr(final PutClassVariableInstr putclassvariableinstr) {
        commonForMostPutInstr(putclassvariableinstr);
    }

    public void PutConstInstr(final PutConstInstr putconstinstr) {
        commonForMostPutInstr(putconstinstr);
    }

    public void PutFieldInstr(final PutFieldInstr putfieldinstr) {
        commonForMostPutInstr(putfieldinstr);
    }

    public void PutGlobalVarInstr(final PutGlobalVarInstr putglobalvarinstr) {
        final GlobalVariable target = (GlobalVariable) putglobalvarinstr.getTarget();
        final String varName = target.getName();
        final Operand value = putglobalvarinstr.getValue();
        
        stringProducer.appendParameters(varName, value);
    }

    private void commonForMostPutInstr(final PutInstr putInstr) {
        final Operand target = putInstr.getTarget();
        final String ref = putInstr.getRef();
        final Operand value = putInstr.getValue();
        
        stringProducer.appendParameters(target, ref, value);
    }

    // Subclasses of MultipleAsgnBaseInstr

    public void ReqdArgMultipleAsgnInstr(final ReqdArgMultipleAsgnInstr reqdargmultipleasgninstr) {
        final List<Object> parameters = getCommonParametersForMultipleAsgnBase(reqdargmultipleasgninstr);
        
        parameters.add(reqdargmultipleasgninstr.getPreArgsCount());
        parameters.add(reqdargmultipleasgninstr.getPostArgsCount());
        
        stringProducer.appendParameters(parameters.toArray());
    }

    public void RestArgMultipleAsgnInstr(final RestArgMultipleAsgnInstr restargmultipleasgninstr) {
        final List<Object> parameters = getCommonParametersForMultipleAsgnBase(restargmultipleasgninstr);
        
        parameters.add(restargmultipleasgninstr.getPreArgsCount());
        parameters.add(restargmultipleasgninstr.getPostArgsCount());
        
        stringProducer.appendParameters(parameters.toArray());
    }

    private List<Object> getCommonParametersForMultipleAsgnBase(final MultipleAsgnBase multipleAsgnBase) {
        final List<Object> parameters = new ArrayList<Object>(4);
        
        final Operand array = multipleAsgnBase.getArray();
        final int index = multipleAsgnBase.getIndex();
        
        Collections.addAll(parameters, array, index);
        
        return parameters;
    }

    // Subclasses of DefinedObjectNameInstr

    public void ClassVarIsDefinedInstr(final ClassVarIsDefinedInstr classvarisdefinedinstr) {
        commonForAllDefinedObjectName(classvarisdefinedinstr);
    }

    public void GetDefinedConstantOrMethodInstr(
            final GetDefinedConstantOrMethodInstr getdefinedconstantormethodinstr) {
        commonForAllDefinedObjectName(getdefinedconstantormethodinstr);
    }

    public void HasInstanceVarInstr(final HasInstanceVarInstr hasinstancevarinstr) {
        commonForAllDefinedObjectName(hasinstancevarinstr);
    }

    public void IsMethodBoundInstr(final IsMethodBoundInstr ismethodboundinstr) {
        commonForAllDefinedObjectName(ismethodboundinstr);
    }

    public void MethodDefinedInstr(final MethodDefinedInstr methoddefinedinstr) {
        commonForAllDefinedObjectName(methoddefinedinstr);
    }

    public void MethodIsPublicInstr(final MethodIsPublicInstr methodispublicinstr) {
        commonForAllDefinedObjectName(methodispublicinstr);
    }

    private void commonForAllDefinedObjectName(final DefinedObjectNameInstr definedObjectNameInstr) {
        final Operand object = definedObjectNameInstr.getObject();
        final StringLiteral name = definedObjectNameInstr.getName();
        
        stringProducer.appendParameters(object, name);
    }

    public void AliasInstr(final AliasInstr aliasinstr) {
        final Variable receiver = aliasinstr.getReceiver();
        final Operand newName = aliasinstr.getNewName();
        final Operand oldName = aliasinstr.getOldName();
        
        stringProducer.appendParameters(receiver, newName, oldName);
    }

    public void BreakInstr(final BreakInstr breakinstr) {
        final Operand returnValue = breakinstr.getReturnValue();
        final IRScope scopeToReturnTo = breakinstr.getScopeToReturnTo();
        
        stringProducer.appendParameters(returnValue, scopeToReturnTo);
    }

    public void CheckArgsArrayArityInstr(final CheckArgsArrayArityInstr checkargsarrayarityinstr) {
        final Operand argsArray = checkargsarrayarityinstr.getArgsArray();
        int required = checkargsarrayarityinstr.required;
        int opt = checkargsarrayarityinstr.opt;
        int rest = checkargsarrayarityinstr.rest;
        
        stringProducer.appendParameters(argsArray, required, opt, rest);
    }

    public void CheckArityInstr(final CheckArityInstr checkarityinstr) {
        final int required = checkarityinstr.required;
        final int opt = checkarityinstr.opt;
        final int rest = checkarityinstr.rest;
        
        stringProducer.appendParameters(required, opt, rest);
    }

    public void ClosureReturnInstr(final ClosureReturnInstr closurereturninstr) {
        final Operand returnValue = closurereturninstr.getReturnValue();
        
        stringProducer.appendParameters(returnValue);
    }

    public void CopyInstr(final CopyInstr copyinstr) {
        final Operand source = copyinstr.getSource();
        
        stringProducer.appendParameters(source);
    }

    public void DefineClassInstr(final DefineClassInstr defineclassinstr) {
        final IRClassBody newIRClassBody = defineclassinstr.getNewIRClassBody();
        final Operand container = defineclassinstr.getContainer();
        final Operand superClass = defineclassinstr.getSuperClass();
        
        stringProducer.appendParameters(newIRClassBody, container, superClass);
    }

    public void DefineClassMethodInstr(final DefineClassMethodInstr defineclassmethodinstr) {
        final Operand container = defineclassmethodinstr.getContainer();
        final IRMethod method = defineclassmethodinstr.getMethod();
        
        stringProducer.appendParameters(container, method);
    }

    public void DefineInstanceMethodInstr(final DefineInstanceMethodInstr defineinstancemethodinstr) {
        final Operand container = defineinstancemethodinstr.getContainer();
        final IRMethod method = defineinstancemethodinstr.getMethod();
        
        stringProducer.appendParameters(container, method);
    }

    public void DefineMetaClassInstr(final DefineMetaClassInstr definemetaclassinstr) {
        final IRModuleBody metaClassBody = definemetaclassinstr.getMetaClassBody();
        final Operand object = definemetaclassinstr.getObject();
        
        stringProducer.appendParameters(metaClassBody, object);
    }

    public void DefineModuleInstr(final DefineModuleInstr definemoduleinstr) {
        final IRModuleBody newIRModuleBody = definemoduleinstr.getNewIRModuleBody();
        final Operand container = definemoduleinstr.getContainer();
        
        stringProducer.appendParameters(newIRModuleBody, container);
    }

    public void EnsureRubyArrayInstr(final EnsureRubyArrayInstr ensurerubyarrayinstr) {
        final Operand object = ensurerubyarrayinstr.getObject();
        
        stringProducer.appendParameters(object);
    }

    public void EQQInstr(final EQQInstr eqqinstr) {
        final Operand arg1 = eqqinstr.getArg1();
        final Operand arg2 = eqqinstr.getArg2();
        
        stringProducer.appendParameters(arg1, arg2);
    }

    public void ExceptionRegionStartMarkerInstr(
            final ExceptionRegionStartMarkerInstr exceptionregionstartmarkerinstr) {
        final Label begin =  exceptionregionstartmarkerinstr.begin;
        final Label end =  exceptionregionstartmarkerinstr.end;
        final Label firstRescueBlockLabel =  exceptionregionstartmarkerinstr.firstRescueBlockLabel;
        final Label ensureBlockLabel = exceptionregionstartmarkerinstr.ensureBlockLabel;
        
        stringProducer.appendParameters(begin, end, firstRescueBlockLabel, ensureBlockLabel);
    }

    public void GetClassVarContainerModuleInstr(
            final GetClassVarContainerModuleInstr getclassvarcontainermoduleinstr) {
        final Operand startingScope = getclassvarcontainermoduleinstr.getStartingScope();
        final Operand object = getclassvarcontainermoduleinstr.getObject();
        
        stringProducer.appendParameters(startingScope, object);
    }

    public void GVarAliasInstr(final GVarAliasInstr gvaraliasinstr) {
        final Operand newName = gvaraliasinstr.getNewName();
        final Operand oldName = gvaraliasinstr.getOldName();
        
        stringProducer.appendParameters(newName, oldName);
    }

    public void InheritanceSearchConstInstr(final InheritanceSearchConstInstr inheritancesearchconstinstr) {
        final Operand currentModule = inheritancesearchconstinstr.getCurrentModule();
        final String constName = inheritancesearchconstinstr.getConstName();
        final boolean noPrivateConsts = inheritancesearchconstinstr.isNoPrivateConsts();
        
        stringProducer.appendParameters(currentModule, constName, noPrivateConsts);
    }

    public void InstanceOfInstr(final InstanceOfInstr instanceofinstr) {
        final Operand object = instanceofinstr.getObject();
        final String className = instanceofinstr.getClassName();
        
        stringProducer.appendParameters(object, className);
    }

    public void LexicalSearchConstInstr(final LexicalSearchConstInstr lexicalsearchconstinstr) {
        final Operand definingScope = lexicalsearchconstinstr.getDefiningScope();
        final String constName = lexicalsearchconstinstr.getConstName();
        
        stringProducer.appendParameters(definingScope, constName);
    }

    public void LineNumberInstr(final LineNumberInstr linenumberinstr) {
        final int lineNumber = linenumberinstr.lineNumber;
        
        stringProducer.appendParameters(lineNumber);
    }

    public void LoadLocalVarInstr(final LoadLocalVarInstr loadlocalvarinstr) {
        final IRScope scope = loadlocalvarinstr.getScope();
        final LocalVariable localVar = loadlocalvarinstr.getLocalVar();
        
        stringProducer.appendParameters(scope, localVar);
    }

    public void Match2Instr(final Match2Instr match2instr) {
        final Operand receiver = match2instr.getReceiver();
        final Operand arg = match2instr.getArg();
        
        stringProducer.appendParameters(receiver, arg);
    }

    public void Match3Instr(final Match3Instr match3instr) {
        final Operand receiver = match3instr.getReceiver();
        final Operand arg = match3instr.getArg();
        
        stringProducer.appendParameters(receiver, arg);
    }

    public void MatchInstr(final MatchInstr matchinstr) {
        final Operand receiver = matchinstr.getReceiver();
        
        stringProducer.appendParameters(receiver);
    }

    public void MethodLookupInstr(final MethodLookupInstr methodlookupinstr) {
        final MethodHandle methodHandle = methodlookupinstr.getMethodHandle();
        
        stringProducer.appendParameters(methodHandle);
    }

    public void ModuleVersionGuardInstr(final ModuleVersionGuardInstr moduleversionguardinstr) {
        final Operand candidateObj = moduleversionguardinstr.getCandidateObj();
        final int expectedVersion = moduleversionguardinstr.getExpectedVersion();
        final String name = moduleversionguardinstr.getModule().getName();
        final Label failurePathLabel = moduleversionguardinstr.getFailurePathLabel();
        
        stringProducer.appendParameters(candidateObj, expectedVersion, name, failurePathLabel);
    }

    public void NotInstr(final NotInstr notinstr) {
        final Operand arg = notinstr.getArg();
        
        stringProducer.appendParameters(arg);
    }

    public void OptArgMultipleAsgnInstr(final OptArgMultipleAsgnInstr optargmultipleasgninstr) {
        final Operand array = optargmultipleasgninstr.getArray();
        final int index = optargmultipleasgninstr.getIndex();
        final int minArgsLength = optargmultipleasgninstr.getMinArgsLength();
        
        stringProducer.appendParameters(array, index, minArgsLength);
    }

    public void ProcessModuleBodyInstr(final ProcessModuleBodyInstr processmodulebodyinstr) {
        final Operand moduleBody = processmodulebodyinstr.getModuleBody();
        
        stringProducer.appendParameters(moduleBody);
    }

    public void PushBindingInstr(final PushBindingInstr pushbindinginstr) {
        final IRScope scope = pushbindinginstr.getScope();
        
        stringProducer.appendParameters(scope);
    }

    public void RaiseArgumentErrorInstr(final RaiseArgumentErrorInstr raiseargumenterrorinstr) {
        final int required = raiseargumenterrorinstr.getRequired();
        final int opt = raiseargumenterrorinstr.getOpt();
        final int rest = raiseargumenterrorinstr.getRest();
        final int numArgs = raiseargumenterrorinstr.getNumArgs();
        
        stringProducer.appendParameters(required, opt, rest, numArgs);
    }

    public void ReceiveExceptionInstr(final ReceiveExceptionInstr receiveexceptioninstr) {
        final boolean checkType = receiveexceptioninstr.isCheckType();
        
        stringProducer.appendParameters(checkType);
    }

    public void ReceivePreReqdArgInstr(final ReceivePreReqdArgInstr receiveprereqdarginstr) {
        final int argIndex = receiveprereqdarginstr.getArgIndex();
        
        stringProducer.appendParameters(argIndex);
    }

    public void RecordEndBlockInstr(final RecordEndBlockInstr recordendblockinstr) {
        final IRClosure endBlockClosure = recordendblockinstr.getEndBlockClosure();
        
        stringProducer.appendParameters(endBlockClosure);
    }

    public void RescueEQQInstr(final RescueEQQInstr rescueeqqinstr) {
        final Operand arg1 = rescueeqqinstr.getArg1();
        final Operand arg2 = rescueeqqinstr.getArg2();
        
        stringProducer.appendParameters(arg1, arg2);
    }

    public void ReturnInstr(final ReturnInstr returninstr) {
        final Operand returnValue = returninstr.getReturnValue();
        final IRMethod methodToReturnFrom = returninstr.methodToReturnFrom;
        
        stringProducer.appendParameters(returnValue, methodToReturnFrom);
    }

    public void SearchConstInstr(final SearchConstInstr searchconstinstr) {
        final String constName = searchconstinstr.getConstName();
        final Operand startingScope = searchconstinstr.getStartingScope();
        final boolean noPrivateConsts = searchconstinstr.isNoPrivateConsts();
        
        stringProducer.appendParameters(constName, startingScope, noPrivateConsts);
    }

    public void SetReturnAddressInstr(final SetReturnAddressInstr setreturnaddressinstr) {
        final Label returnAddr = setreturnaddressinstr.getReturnAddr();
        
        stringProducer.appendParameters(returnAddr);
    }

    public void StoreLocalVarInstr(final StoreLocalVarInstr storelocalvarinstr) {
        final Operand value = storelocalvarinstr.getValue();
        final IRScope scope = storelocalvarinstr.getScope();
        final LocalVariable localVar = storelocalvarinstr.getLocalVar();
        
        stringProducer.appendParameters(value, scope, localVar);
    }

    public void ThreadPollInstr(final ThreadPollInstr threadpollinstr) {
        final boolean onBackEdge = threadpollinstr.onBackEdge;
        
        stringProducer.appendParameters(onBackEdge);
    }

    public void ThrowExceptionInstr(final ThrowExceptionInstr throwexceptioninstr) {
        final Operand exceptionArg = throwexceptioninstr.getExceptionArg();
        
        stringProducer.appendParameters(exceptionArg);
    }

    public void ToAryInstr(final ToAryInstr toaryinstr) {
        final Operand array = toaryinstr.getArray();
        BooleanLiteral dontToAryArrays = toaryinstr.getDontToAryArrays();
        
        stringProducer.appendParameters(array, dontToAryArrays);
    }

    public void UndefMethodInstr(final UndefMethodInstr undefmethodinstr) {
        final Operand methodName = undefmethodinstr.getMethodName();
        
        stringProducer.appendParameters(methodName);
    }

    public void YieldInstr(final YieldInstr yieldinstr) {
        final Operand blockArg = yieldinstr.getBlockArg();
        final Operand yieldArg = yieldinstr.getYieldArg();
        final boolean unwrapArray = yieldinstr.isUnwrapArray();
        
        stringProducer.appendParameters(blockArg, yieldArg, unwrapArray);        
    }

    public void GlobalIsDefinedInstr(final GlobalIsDefinedInstr globalisdefinedinstr) {
        final StringLiteral name = globalisdefinedinstr.getName();
        
        stringProducer.appendParameters(name);
    }

    public void RestoreErrorInfoInstr(final RestoreErrorInfoInstr restoreerrorinfoinstr) {
        final Operand arg = restoreerrorinfoinstr.getArg();
        
        stringProducer.appendParameters(arg);
    }

    public void SuperMethodBoundInstr(final SuperMethodBoundInstr supermethodboundinstr) {
        final Operand object = supermethodboundinstr.getObject();
        
        stringProducer.appendParameters(object);
    }

    // ruby 1.8 specific

    public void ReceiveOptArgInstr18(final ReceiveOptArgInstr18 receiveoptarginstr) {
        commonFarAllReceiveArgInstr18(receiveoptarginstr);
    }

    public void ReceiveRestArgInstr18(final ReceiveRestArgInstr18 receiverestarginstr) {
        commonFarAllReceiveArgInstr18(receiverestarginstr);
    }

    private void commonFarAllReceiveArgInstr18(final ReceiveArgBase receiveArgBase) {
        final int argIndex = receiveArgBase.getArgIndex();
        
        stringProducer.appendParameters(argIndex);
    }

    // ruby 1.9 specific

    public void BuildLambdaInstr(final BuildLambdaInstr buildlambdainstr) {
        final String lambdaBodyName = buildlambdainstr.getLambdaBodyName();
        final ISourcePosition position = buildlambdainstr.getPosition();        
        
        stringProducer.appendParameters(lambdaBodyName, position);
    }

    public void GetEncodingInstr(final GetEncodingInstr getencodinginstr) {
        final String charsetName = getencodinginstr.getEncoding().getCharsetName();
        
        stringProducer.appendParameters(charsetName);
    }

    public void ReceiveOptArgInstr19(final ReceiveOptArgInstr19 receiveoptarginstr) {
        final int argIndex = receiveoptarginstr.getArgIndex();
        final int minArgsLength = receiveoptarginstr.minArgsLength;
        
        stringProducer.appendParameters(argIndex, minArgsLength);
    }

    public void ReceivePostReqdArgInstr(ReceivePostReqdArgInstr receivepostreqdarginstr) {
        int argIndex = receivepostreqdarginstr.getArgIndex();
        int preReqdArgsCount = receivepostreqdarginstr.preReqdArgsCount;
        int postReqdArgsCount =receivepostreqdarginstr.postReqdArgsCount;
        
        stringProducer.appendParameters(argIndex, preReqdArgsCount, postReqdArgsCount);
    }

    public void ReceiveRestArgInstr19(final ReceiveRestArgInstr19 receiverestarginstr) {
        final int argIndex = receiverestarginstr.getArgIndex();
        final int totalRequiredArgs = receiverestarginstr.getTotalRequiredArgs();
        final int totalOptArgs = receiverestarginstr.getTotalOptArgs();
        
        stringProducer.appendParameters(argIndex, totalRequiredArgs, totalOptArgs);
    }

}
