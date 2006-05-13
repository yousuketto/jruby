require 'java'

include_class "org.objectweb.asm.ClassWriter"
include_class "org.objectweb.asm.Opcodes"
include_class "org.objectweb.asm.Type"
include_class "org.jruby.util.JRubyClassLoader"
include_class("java.lang.String") {|p,c| "J#{c}"}

RUBY_TYPE = "org/jruby/IRuby"
RUNTIME_FIELD = "runtime"
EVALSTATE_TYPE = "org/jruby/evaluator/EvaluationState"
THREADCONTEXT_TYPE = "org/jruby/runtime/ThreadContext"
SCOPENODE_TYPE = "org/jruby/ast/ScopeNode"

Opcodes.constants.each {|c| eval("#{c} = Opcodes::#{c}", TOPLEVEL_BINDING)}

def begin_instruction(name)
  cw = ClassWriter.new(false)
  
  ifcs_array = JString[].new(1)
  ifcs_array[0] = "org/jruby/evaluator/Instruction"
  cw.visit(V1_1, ACC_PUBLIC, name, nil, "java/lang/Object", ifcs_array)
  #cw.visitField(ACC_PRIVATE, RUNTIME_FIELD, "Lorg/jruby/Ruby;", nil, nil)
  
  #ruby_t = Type.getType(RUBY_TYPE)
  #mv = cw.visit_method(ACC_PUBLIC, "<init>", "(L#{RUBY_TYPE};)V", nil, nil)
  mv = cw.visit_method(ACC_PUBLIC, "<init>", "()V", nil, nil)
  mv.visitVarInsn(ALOAD, 0)
  mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V")
  #mv.visitVarInsn(ruby_t.getOpcode(ILOAD), 1)
  #mv.visitFieldInsn(PUTFIELD, name, RUNTIME_FIELD, RUBY_TYPE)
  mv.visitInsn(RETURN)
  mv.visitMaxs(2, 2)
  mv.visitEnd
  
  cw
end
