require 'java'
#require 'compiler_support_asm'
require 'jruby'
require 'codegen2'

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
    
    @bytes = ClassBuilder.def_class :public, @name, :object, ["org.jruby.evaluator.Instruction"] do |c|
      @c = c
      
      @c.import "org.jruby.evaluator.EvaluationState", :evaluationstate
      @c.import "org.jruby.evaluator.InstructionContext", :instructioncontext
      @c.import "org.jruby.evaluator.Instruction", :instruction
      @c.import "org.jruby.IRuby", :iruby
      @c.import "org.jruby.RubyArray", :rubyarray
      @c.import "org.jruby.RubyString", :rubystring
      @c.import "org.jruby.runtime.builtin.IRubyObject", :irubyobject
      
      visit(blocknode)
    end
    
    cl.defineClass(@name, @bytes)
  end
  
  def write_class(file_output)
    file_output.write(@bytes)
  end
  
  def visit(node)
    node.accept(self.to_java_object)
  end
  
  def visitArrayNode(node)
    @m.construct_array :irubyobject, node.child_nodes.size do |i|
      # visit node at this index
      visit(node.child_nodes.get(i))
    end
    
    # push state var back down
    @m.local 1
    @m.field :evaluationstate, "runtime", :iruby
    # swap runtime and array to make newArray call
    @m.swap
    @m.call_method :rubyarray, "newArray", @c.array_cls(:irubyobject), :iruby, :interface
  end
  
  def visitDStrNode(iVisited)
    unless @building_string
      @building_string = true
      new_buffer = true
      @m.construct_obj :stringbuffer, [:string]
    end
    
    # stringbuffer on stack, visit string pieces
    iVisited.child_nodes.each do |n|
      unless n.nil?
        @m.call_method(:stringbuffer, "append", :string, :stringbuffer) do
          n.accept(self.to_java_object)
          @m.call_method(:string, "toString", :void, :object)
        end
      end
    end
    
    if new_buffer
      # replace stringbuffer with string
      @m.call_method :string, "toString", :void, :stringbuffer
      
      # push state var back down
      @m.local 1
      @m.field :evaluationstate, "runtime", :iruby
      # swap runtime and string to make newString call
      @m.swap
      @m.call_method :rubystring, "newString", :string, :iruby
      
      @building_string = false
    end
    
    nil
  end
  
  def visitNewlineNode(iVisited)
    @c.def_constructor :public do |m|
      m.call_super
      m.return_void
    end
  
    @c.def_method :public, :void, "execute", [:evaluationstate, :instructioncontext] do |m|
    
      @m = m
      
      m.local(1)
      m.call_method :void, "setResult", :irubyobject, :evaluationstate do
        visit(iVisited.next_node)
      end
            
      m.return_void
    end
  
    nil
  end
  
  def visitScopeNode(iVisited)
  end
  
  def visitStrNode(iVisited)
    # push state var back down
    @m.local 1
    @m.field :evaluationstate, "runtime", :iruby
    @m.call_method :rubystring, "newString", :string, :iruby, :interface do
      @m.constant(iVisited.value)
    end
  end
end

foo_ctx = JRuby.parse("def foo; \"bar\"; end;", "x")
bar_ctx = JRuby.parse("def bar; \"baz\"; end;", "xz")
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
