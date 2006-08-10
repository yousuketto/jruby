require 'java'
require 'jruby'

include_class "org.jruby.ast.visitor.NodeVisitor"

class MyVisitor < NodeVisitor
  def initialize
    super
    @indent = ""
  end
  
  def indent
    @indent = @indent + " "
  end
  
  def undent
    @indent = @indent[0..-2]
  end
  
  def puts(str)
    Kernel.puts(@indent + str)
  end
  
  def visit(node)
    indent
    node.accept(self.to_java_object) if node
    undent
    nil
  end
  
  def visitAliasNode(iVisited)
    puts iVisited.to_s + " #{iVisited.old_name} #{new_name}"
    
  end
  
  def visitAndNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.first_node)
    visit(iVisited.second_node)
  end
  
  def visitArgsNode(iVisited)
    puts iVisited.to_s
    indent
    iVisited.args.child_nodes.each {|n| puts n.name} unless iVisited.args == nil
    undent
    iVisited.opt_args.child_nodes.each {|n| visit(n)} unless iVisited.opt_args == nil
    visit(iVisited.block_arg_node) unless iVisited.block_arg_node == nil
  end
  
  def visitArgsCatNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.first_node)
    visit(iVisited.second_node)
  end
  
  def visitArrayNode(iVisited)
    puts iVisited.to_s
    iVisited.child_nodes.each {|n| visit(n)}
    nil
  end
  
  def visitBackRefNode(iVisited)
    puts iVisited.to_s + " " + iVisited.type
  end
  
  def visitBeginNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.body_node)
  end
  
  def visitBignumNode(iVisited)
    puts iVisited.to_s + " " + iVisited.value
  end
  
  def visitBlockArgNode(iVisited)
    puts iVisited.to_s + " " + iVisited.count
  end
  
  def visitBlockNode(iVisited)
    puts iVisited.to_s
    iVisited.child_nodes.each {|n| visit(n)}
    nil
  end
  
  def visitBlockPassNode(iVisited)
    puts iVisited.to_s
  end
  
  def visitBreakNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.value_node)
  end
  
  def visitConstDeclNode(iVisited)
    puts iVisited.to_s + " #{iVisited.name}"
    visit(iVisited.value_node)
  end
  
  def visitClassVarAsgnNode(iVisited)
    puts iVisited.to_s + " #{iVisited.name}"
    visit(iVisited.value_node)
  end
  
  def visitClassVarDeclNode(iVisited)
    visitClassVarAsgnNode(iVisited)
  end
  
  def visitClassVarNode(iVisited)
    puts iVisited.to_s + " @@#{iVisited.name}"
  end
  
  def visitCallNode(iVisited)
    puts iVisited.to_s + " #{iVisited.name}"
    visit(iVisited.receiver_node)
    visit(iVisited.args_node)
  end
  
  def visitCaseNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.case_node)
    visit(iVisited.case_body)
  end
  
  def visitClassNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.CPath)
    visit(iVisited.super_node)
    visit(iVisited.body_node)
  end
  
  def visitColon2Node(iVisited)
    puts iVisited.to_s + " #{iVisited.name}"
    visit(iVisited.left_node) unless iVisited.left_node.nil?
  end
  
  def visitColon3Node(iVisited)
    puts iVisited.to_s + " ::#{iVisited.name}"
  end
  
  def visitConstNode(iVisited)
    puts iVisited.to_s + " #{iVisited.name}"
  end
  
  def visitDAsgnNode(iVisited)
    visitClassVarAsgnNode(iVisited)
  end
  
  def visitDRegxNode(iVisited)
    puts iVisited.to_s + " opts = #{iVisited.options}, once = ${iVisited.once}"
  end
  
  def visitDStrNode(iVisited)
    puts iVisited.to_s
    iVisited.child_nodes.each {|n| n.accept(self.to_java_object) unless n.nil?}
    nil
  end
  
  def visitDSymbolNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.node);
  end
  
  def visitDVarNode(iVisited)
    puts iVisited.to_s + " #{iVisited.name}"
  end
  
  def visitDXStrNode(iVisited)
    puts iVisited.to_s
    iVisited.child_nodes.each {|n| visit(n)}
  end
  
  def visitDefinedNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.expression_node)
  end
  
  def visitDefnNode(iVisited)
    puts iVisited.to_s + " #{iVisited.name_node.name}"
    iVisited.args_node.accept(self.to_java_object)
    iVisited.body_node.accept(self.to_java_object)
  end
  
  def visitDefsNode(iVisited)
    puts iVisited.to_s + " #{iVisited.name}"
    visit(iVisited.receiver_node)
    visit(iVisited.args_node)
    visit(iVisited.body_node)
  end
  
  def visitDotNode(iVisited)
    puts iVisited.to_s + " #{iVisited.exclusive ? '..' : '...'}"
    visit(iVisited.begin_node)
    visit(iVisited.end_node)
  end
  
  def visitEnsureNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.ensure_node)
    visit(iVisited.body_node)
  end
  
  def visitEvStrNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.body)
  end
  
  def visitFCallNode(iVisited)
    puts iVisited.to_s + " " + iVisited.name
    visit(iVisited.args_node)
  end
  
  def visitFalseNode(iVisited)
    puts iVisited.to_s
  end
  
  def visitFixnumNode(iVisited)
    puts iVisited.to_s + " " + iVisited.value.to_s
  end
  
  def visitFlipNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.begin_node)
    visit(iVisited.end_node)
  end
  
  def visitFloatNode(iVisited)
    puts iVisited.to_s + " " + iVisited.value.to_s
  end
  
  def visitForNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.var_node)
    visit(iVisited.iter_node)
    visit(iVisited.body_node)
  end
  
  def visitGlobalAsgnNode(iVisited)
    puts iVisited.to_s + " " + iVisited.name
    visit(iVisited.value_node)
  end
  
  def visitGlobalVarNode(iVisited)
    puts iVisited.to_s + " " + iVisited.name
  end
  
  def visitHashNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.list_node)
  end
  
  def visitInstAsgnNode(iVisited)
    visitGlobalAsgnNode(iVisited)
  end
  
  def visitInstVarNode(iVisited)
    visitGlobalVarNode(iVisited)
  end
  
  def visitIfNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.condition)
    visit(iVisited.then_body)
    visit(iVisited.else_body)
  end
  
  def visitIterNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.var_node)
    visit(iVisited.body_node)
    visit(iVisited.iter_node)
  end
  
  def visitLocalAsgnNode(iVisited)
    puts iVisited.to_s + " var name " + iVisited.name + " is var index " + iVisited.count.to_s
    visit(iVisited.value_node)
  end
  
  def visitLocalVarNode(iVisited)
    puts iVisited.to_s + " var index " + iVisited.count.to_s
  end
  
  def visitMultipleAsgnNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.head_node)
    visit(iVisited.args_node)
  end
  
  def visitMatch2Node(iVisited)
    puts iVisited.to_s
    visit(iVisited.receiver_node)
    visit(iVisited.value_node)
  end
  
  def visitMatch3Node(iVisited)
    visitMatch2Node(iVisited)
  end
  
  def visitMatchNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.regexp_node)
  end
  
  def visitModuleNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.CPath)
    visit(iVisited.body_node)
  end
  
  def visitNewlineNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.next_node)
  end
  
  def visitNextNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.value_node)
  end
  
  def visitNilNode(iVisited)
    puts iVisited.to_s
  end
  
  def visitNotNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.condition_node)
  end
  
  def visitNthRefNode(iVisited)
    puts iVisited.to_s + " match number " + iVisited.match_number.to_s
  end
  
  def visitOpElementAsgnNode(iVisited)
    puts iVisited.to_s + " " + iVisited.operator_name
    visit(iVisited.receiver_node)
    visit(iVisited.args_node)
    visit(iVisited.value_node)
  end
  
  def visitOpAsgnNode(iVisited)
    puts iVisited.to_s + " variable = " + iVisited.variable_name + " method = " + iVisited.method_name
    visit(iVisited.receiver_node)
    visit(iVisited.value_node)
  end
  
  def visitOpAsgnAndNode(iVisited)
    visitOpAsgnNode(iVisited)
  end
  
  def visitOpAsgnOrNode(iVisited)
    visitopAsgnOrNode(iVisited)
  end
  
  def visitOptNNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.body_node)
  end
  
  def visitOrNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.first_node)
    visit(iVisited.second_node)
  end
  
  def visitPostExeNode(iVisited)
    puts iVisited.to_s
  end
  
  def visitRedoNode(iVisited)
    puts iVisited.to_s
  end
  
  def visitRegexpNode(iVisited)
    puts iVisited.to_s + " " + iVisited.value + " " + iVisited.options.to_s
  end
  
  def visitRescueBodyNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.exception_nodes)
    visit(iVisited.body_node)
    visit(ivisited.opt_rescue_node)
  end
  
  def visitRescueNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.body_node)
    visit(iVisited.rescue_body_node)
    visit(iVisited.else_node)
  end
  
  def visitRetryNode(iVisited)
    puts iVisited.to_s
  end
  
  def visitReturnNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.value_node)
  end
  
  def visitSClassNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.recv_node)
    visit(iVisted.body_node)
  end
  
  def visitScopeNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.body_node)
  end
  
  def visitSelfNode(iVisited)
    puts iVisited.to_s
  end
  
  def visitSplatNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.value)
  end
  
  def visitStrNode(iVisited)
    puts iVisited.to_s + "\"" + iVisited.value + "\""
  end
  
  def visitSuperNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.args_node)
  end
  
  def visitSValueNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.value)
  end
  
  def visitSymbolNode(iVisited)
    puts iVisited.to_s + " " + iVisited.name
  end
  
  def visitToAryNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.value)
  end
  
  def visitTrueNode(iVisited)
    puts iVisited.to_s
  end
  
  def visitUndefNode(iVisited)
    puts iVisited.to_s + " " + iVisited.name
  end
  
  def visitUntilNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.condition_node)
    visit(iVisited.body_node)
  end
  
  def visitVAliasNode(iVisited)
    puts iVisited.to_s + " aliasing " + iVisited.old_name + " as " + iVisited.new_name
  end
  
  def visitVCallNode(iVisited)
    puts iVisited.to_s + " " + iVisited.method_name
  end
  
  def visitWhenNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.expression_nodes)
    visit(iVisited.body_node)
    visit(iVisited.next_case)
  end
  
  def visitWhileNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.condition_node)
    visit(iVisited.body_node)
  end
  
  def visitXStrNode(iVisited)
    puts iVisited.to_s + " \"" + iVisited.value + "\""
  end
  
  def visitYieldNode(iVisited)
    puts iVisited.to_s
    visit(iVisited.args_node)
  end
  
  def visitZArrayNode(iVisited)
    puts iVisited.to_s
  end
  
  def visitZSuperNode(iVisited)
    puts iVisited.to_s
  end
end

x = JRuby.parse(File.read(ARGV[0]), ARGV[0])
x.accept(MyVisitor.new)
