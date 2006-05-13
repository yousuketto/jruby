require 'java'
require 'compiler_support_asm'
require 'jruby'

include_class "java.io.FileOutputStream"

include_class "org.jruby.evaluator.Instruction"
include_class "org.jruby.ast.visitor.NodeVisitor"
include_class "org.jruby.Ruby"
include_class "org.jruby.evaluator.InstructionBundle"
include_class "org.jruby.util.JRubyClassLoader"
include_class("java.lang.Thread") {|p,c| "J#{c}"}

class CompiledBody < Instruction
  def execute(state, context)
  end
end

class CompilerVisitor < NodeVisitor
  def initialize
    super
  end
  
  def compile(blocknode, name, cl)
    @name = name
    visit(blocknode)
    
    bytecode = @class_writer.to_byte_array();
    
    cl.defineClass(@name, bytecode)
  end
  
  def write_class(file_output)
    file_output.write(@class_writer.to_byte_array())
  end
  
  def visit(node)
    node.accept(self.to_java_object)
  end
  
  def visitArrayNode(node)
    @method_visitor.visitLdcInsn(node.child_nodes.size)
    # hack to get around us always using long
    @method_visitor.visitInsn(L2I)
    @method_visitor.visitTypeInsn(ANEWARRAY, "org/jruby/runtime/builtin/IRubyObject")
    
    for i in 0...node.child_nodes.size do
      # dup array
      @method_visitor.visit_insn(DUP)
      # push index
      @method_visitor.visitLdcInsn(i)
      # hack to get around us always using long
      @method_visitor.visitInsn(L2I)
      
      # visit node at this index
      visit(node.child_nodes.get(i))
      
      # put into array
      @method_visitor.visitInsn(AASTORE)
    end
    
    # push state var back down
    @method_visitor.visitVarInsn(ALOAD, 1)
    @method_visitor.visitFieldInsn(GETFIELD, EVALSTATE_TYPE, "runtime", "L#{RUBY_TYPE};")
    # swap runtime and array to make newArray call
    @method_visitor.visitInsn(SWAP)
    @method_visitor.visitMethodInsn(INVOKEINTERFACE, RUBY_TYPE, "newArray", "([Lorg/jruby/runtime/builtin/IRubyObject;)Lorg/jruby/RubyArray;")
  end
  
  def visitDStrNode(iVisited)
    unless @building_string
      @building_string = true
      new_buffer = true
      # create a stringbuffer for str construction
      @method_visitor.visitTypeInsn(NEW, "java/lang/StringBuffer")
      @method_visitor.visitInsn(DUP)
      @method_visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V")
    end
    
    # stringbuffer on stack, visit string pieces
    iVisited.child_nodes.each {|n| n.accept(self.to_java_object) unless n.nil?}
    
    if new_buffer
      # replace stringbuffer with string
      @method_visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;")
      
      # push state var back down
      @method_visitor.visitVarInsn(ALOAD, 1)
      @method_visitor.visitFieldInsn(GETFIELD, EVALSTATE_TYPE, "runtime", "L#{RUBY_TYPE};")
      # swap runtime and string to make newString call
      @method_visitor.visitInsn(SWAP)
      @method_visitor.visitMethodInsn(INVOKEINTERFACE, RUBY_TYPE, "newString", "(Ljava/lang/String;)Lorg/jruby/RubyString;")
      
      @building_string = false
    end
    
    nil
  end
  
  def visitNewlineNode(iVisited)
    @class_writer = begin_instruction(@name)
    
    @method_visitor = @class_writer.visitMethod(ACC_PUBLIC, "execute", "(Lorg/jruby/evaluator/EvaluationState;Lorg/jruby/evaluator/InstructionContext;)V", nil, nil)
    
    # the below is preferred, but java support has trouble dispatching to it
    #local_names = iVisited.local_names
    #@method_visitor.visitLdcInsn(ILOAD, local_names.length)
    #@method_visitor.visitTypeInsn(ANEWARRAY, "[Ljava/lang/String;")
    # array populating would go here
    
    #@method_visitor.visitVarInsn(ALOAD, 1)
    #@method_visitor.visitMethodInsn(INVOKEVIRTUAL, EVALSTATE_TYPE, "getThreadContext", "()L#{THREADCONTEXT_TYPE};")
    #@method_visitor.visitVarInsn(ALOAD, 2)		
    #@method_visitor.visitTypeInsn(CHECKCAST, "org/jruby/ast/ScopeNode")
    #@method_visitor.visitMethodInsn(INVOKEVIRTUAL, SCOPENODE_TYPE, "getLocalNames", "()[Ljava/lang/String;")
    #@method_visitor.visitMethodInsn(INVOKEVIRTUAL, THREADCONTEXT_TYPE, "preScopedBody", "([Ljava/lang/String;)V")
    
    visit(iVisited.next_node)
    
    # don't forget post
    #@method_visitor.visitVarInsn(ALOAD, 1)
    #@method_visitor.visitMethodInsn(INVOKEVIRTUAL, EVALSTATE_TYPE, "getThreadContext", "()Lorg/jruby/runtime/ThreadContext;")
    #@method_visitor.visitMethodInsn(INVOKEVIRTUAL, THREADCONTEXT_TYPE, "postScopedBody", "()V")
    
    # set result
    @method_visitor.visitVarInsn(ALOAD, 1)
    @method_visitor.visitInsn(SWAP)
    @method_visitor.visitMethodInsn(INVOKEVIRTUAL, EVALSTATE_TYPE, "setResult", "(Lorg/jruby/runtime/builtin/IRubyObject;)V")

    @method_visitor.visitInsn(RETURN)
    @method_visitor.visitMaxs(10, 10)
    @method_visitor.visitEnd()
    
    @class_writer.visitEnd()
    
    nil
    nil
  end
  
  def visitScopeNode(iVisited)
  end
  
  def visitStrNode(iVisited)
    @method_visitor.visitLdcInsn(iVisited.value)
    @method_visitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;")
  end
end

foo_ctx = JRuby.parse("def foo; 'bar'; end;", "x")
bar_ctx = JRuby.parse("def bar; 'baz'; end;", "xz")
baz_ctx = JRuby.parse("def baz; end;", "x3")

foo_arr_ctx = JRuby.parse("def foo_arr; ['this', 'is', 'an', 'array', 'of', 'strings']; end", "y")
bar_arr_ctx = JRuby.parse("def bar_arr; ['this', 'is', 'an', 'array', 'of', 'strings']; end;", "yz")

# compile what we can into Instruction types
n = JRubyClassLoader.new()
compiler = CompilerVisitor.new
foo_class = compiler.compile(foo_ctx.next_node.body_node.body_node, "MyFooClass", n)
compiler.write_class(FileOutputStream.new("MyFooClass.class"))
foo_arr_class = compiler.compile(foo_arr_ctx.next_node.body_node.body_node, "MyArrClass", n)
compiler.write_class(FileOutputStream.new("MyArrClass.class"))

# instantiate the compiled instructions
foo = foo_class.newInstance()
foo_arr = foo_arr_class.newInstance()

# store the compiled instructions in the AST
foo_bndl = InstructionBundle.new(foo, foo_ctx)
foo_ctx.next_node.body_node.body_node.instruction = foo_bndl
foo_arr_bndl = InstructionBundle.new(foo_arr, foo_arr_ctx)
foo_arr_ctx.next_node.body_node.body_node.instruction = foo_arr_bndl

my_ruby = Ruby.getDefaultInstance()

# eval the defs, with embedded compiled instructions
my_ruby.eval(foo_ctx)
my_ruby.eval(bar_ctx)
my_ruby.eval(foo_arr_ctx)
my_ruby.eval(bar_arr_ctx)
my_ruby.eval(baz_ctx)

n = Time.now
my_ruby.evalScript("1_000_000.times {baz}")
puts "baz time: #{Time.now - n}"

n = Time.now
my_ruby.evalScript("1_000_000.times {foo}")
puts "foo time: #{Time.now - n}"

n = Time.now
my_ruby.evalScript("1_000_000.times {bar}")
puts "bar time: #{Time.now - n}"

n = Time.now
my_ruby.evalScript("1_000_000.times {foo_arr}")
puts "foo_arr time: #{Time.now - n}"

n = Time.now
my_ruby.evalScript("1_000_000.times {bar_arr}")
puts "bar_arr time: #{Time.now - n}"
