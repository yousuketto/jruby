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

module GenUtils
  def method_desc(retval, *params)
    m_desc = "("
    if params && params != [nil]
      params.each do |param|
        m_desc << desc(param)
      end
    end
    m_desc << ")"
    m_desc << desc(retval)

    m_desc
  end
  
  
  def desc(retval)
    desc = ""
    case retval
    when :void
      desc << "V"
    when :int
      desc << "I"
    when :float
      desc << "F"
    when :double
      desc << "D"
    else
          if retval.to_s[0..1] == '[L'
            desc << retval
          else
            desc << "L#{cls(retval).gsub('.', '/')};"
          end
    end

    desc
  end
  
  def cls(type)
    return imports[type].gsub(".", "/") if imports.include? type
    type.to_s.gsub(".", "/")
  end
  
  def array_cls(type)
    return type if (type.to_s[0] == "[") # if already array, return
    return "[L#{imports[type]};" if imports.include? type
    
    case type
    when :int
      return "[I"
    when :float
      return "[F"
    when :double
      return "[D"
    when :void
      raise ArgumentError, "array of void is illegal"
    else
      # if type passed in is already array, don't re-array it (no nesting array types here)
      if type[0] == "["
        return type
      else
        return "[L#{cls(type).gsub(".", "/")};"
      end
    end
  end

  def vis(vis)
    new_vis = 0
    
    case vis
    when Array
      vis.each {|v| new_vis |= eval("Opcodes::ACC_#{v.to_s.upcase}")}
    else
      new_vis = eval("Opcodes::ACC_#{vis.to_s.upcase}")
    end

    new_vis
  end
end

class ClassBuilder
  include Opcodes
  include GenUtils

  attr_accessor :class_visitor
  attr_accessor :classname
  attr_accessor :parentname
  attr_accessor :interfaces
  attr_accessor :imports
  
  def initialize
  	@imports = {
      :string => "java/lang/String",
      :object => "java/lang/Object",
      :stringbuffer => "java/lang/StringBuffer",
      :exception => "java/lang/Exception",
      :thread => "java/lang/Thread",
      :throwable => "java/lang/Throwable" }
  end
  
  def self.def_class(visiblility, classname, parentname=nil, ifcs=nil, sig=nil, &b)
    cc = ClassBuilder.new()    
    parentname ||= cc.cls("java.lang.Object");
    cc.start_class cc.vis(visiblility), cc.cls(classname), cc.cls(parentname), ifcs, sig
    
    yield cc
    
    cc.end_class
    
    cc.class_visitor.to_byte_array
  end
  
  def import(classname, sym)
  	@imports[sym] = classname.gsub(".", "/")
  end
  
  def def_constructor(vis, params=nil, excepts=nil, signature=nil, &b)
    def_method(vis, :void, "<init>", params, excepts, signature, &b)
  end
  
  def def_method(vis, retval, name, params=nil, excepts=nil, signature=nil, &b)
    unless excepts.nil?
      excepts_arr = JString[].new(excepts.size)
      excepts = excepts.each_index {|i| excepts_arr[i] = cls(excepts[i])}
    end
  
    mc = MethodBuilder.new(self)
    mc.start_method(vis(vis), name, method_desc(retval, *params), excepts_arr, signature)
    
	yield mc
    
    mc.end_method()
  end

  def start_class(vis, classname, parentname, ifcs, signature)  
    @classname = cls(classname)
    @parentname = cls(parentname)
    if ifcs then
	  str_ifcs = JString[].new(ifcs.size)
      ifcs.each_index {|i| str_ifcs[i] = cls(ifcs[i])}
      @interfaces = str_ifcs
	end
	
	@class_visitor = ClassWriter.new(false)
	@class_visitor.visit(V1_1, vis, classname, signature, parentname, interfaces)
  end
  
  def end_class()
    class_visitor.visitEnd()
  end
end 

class MethodBuilder
  include Opcodes
  include GenUtils
  
  attr_accessor :methodname
  attr_accessor :methoddesc
  attr_accessor :method_visitor
  attr_accessor :class_builder

  def initialize(class_builder)
    @class_builder = class_builder
    # need better way to do calculation of stack size
    @stack_size = 10
  end
  
  # defaults to calling super.<methodname>(<methoddesc>)
  def call_super(retval=:void, name=methodname, params=nil)
    call_method(retval, name, params, class_builder.parentname, invoke_type=:super)
  end
  
  def call_this(retval, name, params=nil)
    call_method(retval, name, params, class_builder.classname, invoke_type=:this)
  end
  
  def return_void
    @method_visitor.visitInsn(RETURN)
  end
  
  # this could be smarter, watching the last item put on the stack and only returning void if we're supposed to
  def return_top(type)
    case type
    when :int
      @method_visitor.visitInsn(IRETURN)
    when :ref
      @method_visitor.visitInsn(ARETURN)
    end
  end
  
  def call_method(retval, name, params=nil, cls=class_builder.classname, invoke_type=:virtual)
    if block_given?
      raise ArgumentError, "cannot yield for values with nil params" if params.nil?
      yield self
    end 
  
    if invoke_type == :virtual
      invoke_type = INVOKEVIRTUAL
    elsif invoke_type == :interface
      invoke_type = INVOKEINTERFACE
    elsif invoke_type == :super || invoke_type == :this
      invoke_type = INVOKESPECIAL
      # load 'this' if super() or this()
      @method_visitor.visitVarInsn(ALOAD, 0)
    elsif invoke_type == :construct
      invoke_type = INVOKESPECIAL
    else
      raise ArgumentError, "invalue invocation type #{invoke_type}, expected one of :virtual, :interface, :super, :this, or :construct"
    end
    
    @method_visitor.visitMethodInsn(invoke_type, class_builder.cls(cls), name, class_builder.method_desc(retval, *params))
  end
  
  def constant(c)
    @method_visitor.visitLdcInsn(c)
  end
  
  def construct_obj(type, params=nil)
    new_obj type
    dup
    if block_given?
      raise ArgumentError, "cannot yield for values with nil params" if params.nil?
      yield self
    end
    call_constructor(type, params)
  end
  
  def construct_array(type, size)
    constant(size)
    # HACK: Fixnums always come out as long, so convert
    @method_visitor.visitInsn(L2I)
    
    # TODO: add other types
    case type
    when :int
      @method_visitor.visitIntInsn(NEWARRAY, T_INT)
    else
      @method_visitor.visitTypeInsn(ANEWARRAY, class_builder.cls(type))
    end
    
    if block_given?
      for i in 0...size
        array_set(i, type) {|mb| yield i}
      end
    end
  end
  
  def new_obj(type)
    type = cls(type)
    
    @method_visitor.visitTypeInsn(NEW, type)
  end
  
  def dup()
    @method_visitor.visitInsn(DUP)
  end
  
  def swap()
  	@method_visitor.visitInsn(SWAP)
  end
  
  def local(index, type=:ref)
  	case type
  	when :int
	  	@method_visitor.visitVarInsn(ILOAD, index)
	when :ref
		@method_visitor.visitVarInsn(ALOAD, index)
	end
  end
  
  def field(container, name, type)
    @method_visitor.visitFieldInsn(GETFIELD, class_builder.cls(container), "runtime", class_builder.desc(type))
  end
  
  def call_constructor(type, params, &b)
    call_method(:void, "<init>", params, type, :construct, &b)
  end
  
  # expects array on stack, block to provide value
  def array_set(index, type=:ref)
    dup
    # push index
    @method_visitor.visitLdcInsn(index)
    # hack to get around us always using long
    @method_visitor.visitInsn(L2I)
    
    yield self # yield to client to allow setting up value
    
    # put into array
    @method_visitor.visitInsn(AASTORE)
  end
      
  def start_method(vis, name, desc, excepts, signature)
    @methodname = name
    @methoddesc = desc
    @method_visitor = class_builder.class_visitor.visitMethod(vis, name, desc, signature, excepts)
  end
  
  def end_method
    @method_visitor.visitMaxs(@stack_size, @stack_size)
    @method_visitor.visitEnd()
  end
end

class ParamBuilder
  include GenUtils

  def initialize(method_builder, params)
    @method_builder = method_builder
    @params = params
  end
  
  def constant(const)
    @method_builder.constant(const)
  end
end