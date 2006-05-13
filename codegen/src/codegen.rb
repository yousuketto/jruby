require 'java'

include_class "org.objectweb.asm.ClassWriter"
include_class("org.objectweb.asm.Opcodes") {|p,c| "J" + c}
include_class "org.objectweb.asm.Type"
include_class("java.lang.String") {|p,c| "J#{c}"}

module Opcodes
  JOpcodes.constants.each {|c| eval("Opcodes::#{c} = JOpcodes::#{c}", TOPLEVEL_BINDING)}
  
  def self.insn_map
    @insn_map
  end

  zero_op_insns = [:NOP, :ACONST_NULL, :ICONST_M1, :ICONST_0, :ICONST_1, :ICONST_2, :ICONST_3, :ICONST_4, :ICONST_5, :LCONST_0, :LCONST_1, :FCONST_0, :FCONST_1, :FCONST_2, :DCONST_0, :DCONST_1, :IALOAD, :LALOAD, :FALOAD, :DALOAD, :AALOAD, :BALOAD, :CALOAD, :SALOAD, :IASTORE, :LASTORE, :FASTORE, :DASTORE, :AASTORE, :BASTORE, :CASTORE, :SASTORE, :POP, :POP2, :DUP, :DUP_X1, :DUP_X2, :DUP2, :DUP2_X1, :DUP2_X2, :SWAP, :IADD, :LADD, :FADD, :DADD, :ISUB, :LSUB, :FSUB, :DSUB, :IMUL, :LMUL, :FMUL, :DMUL, :IDIV, :LDIV, :FDIV, :DDIV, :IREM, :LREM, :FREM, :DREM, :INEG, :LNEG, :FNEG, :DNEG, :ISHL, :LSHL, :ISHR, :LSHR, :IUSHR, :LUSHR, :IAND, :LAND, :IOR, :LOR, :IXOR, :LXOR, :I2L, :I2F, :I2D, :L2I, :L2F, :L2D, :F2I, :F2L, :F2D, :D2I, :D2L, :D2F, :I2B, :I2C, :I2S, :LCMP, :FCMPL, :FCMPG, :DCMPL, :DCMPG, :IRETURN, :LRETURN, :FRETURN, :DRETURN, :ARETURN, :RETURN, :ARRAYLENGTH, :ATHROW, :MONITORENTER, :MONITOREXIT]
  int_op_insns = [:BIPUSH, :SIPUSH, :NEWARRAY]
  var_insns = [:ILOAD, :LLOAD, :FLOAD, :DLOAD, :ALOAD, :ISTORE, :LSTORE, :FSTORE, :DSTORE, :ASTORE, :RET]
  type_insns = [:NEW, :ANEWARRAY, :CHECKCAST, :INSTANCEOF]
  field_insns = [:GETSTATIC, :PUTSTATIC, :GETFIELD, :PUTFIELD]
  method_insns = [:INVOKEVIRTUAL, :INVOKESPECIAL, :INVOKESTATIC, :INVOKEINTERFACE]
  jump_insns = [:IFEQ, :IFNE, :IFLT, :IFGE, :IFGT, :IFLE, :IF_ICMPEQ, :IF_ICMPNE, :IF_ICMPLT, :IF_ICMPGE, :IF_ICMPGT, :IF_ICMPLE, :IF_ACMPEQ, :IF_ACMPNE, :GOTO, :JSR, :IFNULL, :IFNONNULL]
  
  @insn_map = {}
  zero_op_insns.each {|insn| @insn_map[insn] = :visit_insn}
  int_op_insns.each {|insn| @insn_map[insn] = :visit_int_insn}
  var_insns.each {|insn| @insn_map[insn] = :visit_var_insn}
  type_insns.each {|insn| @insn_map[insn] = :visit_type_insn}
  field_insns.each {|insn| @insn_map[insn] = :visit_field_insn}
  method_insns.each {|insn| @insn_map[insn] = :visit_method_insn}
  jump_insns.each {|insn| @insn_map[insn] = :visit_jump_insn}
  @insn_map[:LDC] = :visit_ldc_insn
  @insn_map[:IINC] = :visit_iinc_insn
  @insn_map[:TABLESWITCH] = :visit_table_switch_insn
  @insn_map[:LOOKUPSWITCH] = :visit_lookup_switch_insn
  @insn_map[:MULTIANEWARRAY] = :visitMultiANewArrayInsn
end

module ClassUtils
  def ClassUtils.cls(classname)
    classname.sub(".", "/")
  end
end

class ClassCreator
  include Opcodes
  
  def initialize(classname, parentname="java.lang.Object", ifcs=nil, vis=ACC_PUBLIC)  
    @classname = classname.sub(".", "/")
    @parentname = parentname.sub(".", "/")
    if ifcs then
	  str_ifcs = JString[].new(ifcs.size)
      ifcs.each_index {|i| str_ifcs[i] = ifcs[i].sub(".", "/")}
      @interfaces = str_ifcs
	end
	
	@cv = ClassWriter.new(false)
	
	if block_given? then
	  yield @cv
	else
	  @cv.visit(V1_1, vis, @classname, nil, @parentname, ifcs)
	end
  end
  
  def new_method(name, desc, excepts=nil)
    excepts = excepts.collect {|ex, arr| arr ||= []; arr << ex.sub('.', '/'); arr} if excepts
  
    mv = @cv.visit_method(ACC_PUBLIC, name, desc, nil, excepts)
    MethodCreator.new(mv)
  end
end

module MethodUtils
  def MethodUtils.desc(params, retval)
    desc = ""
    desc << "("
    if params 
      params.each do |param|
        case param
        when "int"
          desc << "I"
        when "float"
          desc << "F"
        when "double"
          desc << "D"
        else
          desc << "L#{ClassUtils.cls(param)};"
        end
      end
    end
    desc << ")"
    case retval
    when "void"
      desc << "V"
    when "int"
      desc << "I"
    when "float"
      desc << "F"
    when "double"
      desc << "D"
    else
      desc << "L#{ClassUtils.cls(retval)};"
    end

    desc
  end
end    

class MethodCreator
  include Opcodes
  
  Opcodes.insn_map.each_key do |key|
    if [:visit_ldc_insn, :visit_iinc_insn, :visit_tableswitch_insn, :visit_lookupswitch_insn, :visitMultiANewArrayInsn].include?(Opcodes.insn_map[key])
      method_def = <<EOE
def #{key.to_s.downcase}(*args)
  @mv.#{Opcodes.insn_map[key]}(*args)
end
EOE
    else
      method_def = <<EOE
def #{key.to_s.downcase}(*args)
  @mv.#{Opcodes.insn_map[key]}(#{key}, *args)
end
EOE
    end
  
    eval method_def
  end


  def initialize(mv)
    @mv = mv
  end
end