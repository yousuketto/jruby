require 'java'
require 'jruby'

include_class "org.jruby.ast.visitor.NodeVisitor"

class MyVisitor < NodeVisitor
    def initialize
        super
        @indent = ""
    end
    
    def indent
        @old = @indent
        @indent = @indent + " "
    end
    
    def undent
        @indent = @old
    end
    
    def visit(node)
        node.accept(self.to_java_object)
    end

    def visitAliasNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.old_name} #{new_name}"
        
    end
    
    def visitAndNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        visit(iVisited.first_node)
        visit(iVisited.second_node)
        undent
        nil
    end
    
    def visitArgsNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        iVisited.args.child_nodes.each {|n| puts @indent + n.name} unless iVisited.args == nil
        iVisited.opt_args.child_nodes.each {|n| visit(n)} unless iVisited.opt_args == nil
        visit(iVisited.block_arg_node) unless iVisited.block_arg_node == nil
        undent
        nil
    end
    
    def visitArgsCatNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        visit(iVisited.first_node)
        visit(iVisited.second_node)
        undent
        nil
    end
    
    def visitArrayNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        iVisited.child_nodes.each {|n| visit(n)}
        undent
        nil
    end
    
    def visitBackRefNode(iVisited)
        puts @indent + iVisited.to_s + " " + iVisited.type
        nil
    end
    
    def visitBeginNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        visit(iVisited.body_node)
        undent
        nil
    end
    
    def visitBignumNode(iVisited)
        puts @indent + iVisited.to_s + " " + iVisited.value
    end
    
    def visitBlockArgNode(iVisited)
        puts @indent + iVisited.to_s + " " + iVisited.count
    end
    
    def visitBlockNode(iVisited)
        puts @indent + iVisited.to_s
		indent
        iVisited.child_nodes.each {|n| n.accept(self.to_java_object)}
        undent
        nil
    end
    
    def visitBlockPassNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitBreakNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        visit(iVisited.value_node)
        undent
        nil
    end
    
    def visitConstDeclNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.name}"
        indent
        visit(iVisited.value_node)
        undent
        nil
    end
    
    def visitClassVarAsgnNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.name}"
        indent
        visit(iVisited.value_node)
        undent
        nil
    end
    
    def visitClassVarDeclNode(iVisited)
    	visitClassVarAsgnNode(iVisited)
    end
    
    def visitClassVarNode(iVisited)
        puts @indent + iVisited.to_s + " @@#{iVisited.name}"
    end
    
    def visitCallNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.name}"
        indent
        visit(iVisited.receiver_node)
        undent
        nil
    end
    
    def visitCaseNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        visit(iVisited.case_node)
        visit(iVisited.case_body)
        undent
        nil
    end
    
    def visitClassNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        visit(iVisited.CPath)
        visit(iVisited.super_node)
        visit(iVisited.body_node)
        undent
        nil
    end
    
    def visitColon2Node(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.name}"
        indent
        visit(iVisited.left_node) unless iVisited.left_node.nil?
        undent
        nil
    end
    
    def visitColon3Node(iVisited)
        puts @indent + iVisited.to_s + " ::#{iVisited.name}"
    end
    
    def visitConstNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.name}"
    end
    
    def visitDAsgnNode(iVisited)
        visitClassVarAsgnNode(iVisited)
    end
    
    def visitDRegxNode(iVisited)
        puts @indent + iVisited.to_s + " opts = #{iVisited.options}, once = ${iVisited.once}"
    end
    
    def visitDStrNode(iVisited)
        puts @indent + iVisited.to_s
		indent
        iVisited.child_nodes.each {|n| n.accept(self.to_java_object) unless n.nil?}
        undent
        nil
    end
    
    def visitDSymbolNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        visit(iVisited.node);
        undent
        nil
    end
    
    def visitDVarNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.name}"
    end
    
    def visitDXStrNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        iVisited.child_nodes.each {|n| visit(n)}
        undent
		nil
    end
    
    def visitDefinedNode(iVisited)
        puts @indent + iVisited.to_s
        indent
        visit(iVisited.expression_node)
        undent
        nil
    end
    
    def visitDefnNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.name_node.name}"
		indent
        iVisited.args_node.accept(self.to_java_object)
        iVisited.body_node.accept(self.to_java_object)
		undent
        nil
    end
    
    def visitDefsNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.name}"
        indent
        visit(iVisited.receiver_node)
        visit(iVisited.args_node)
        visit(iVisited.body_node)
        undent
        nil
    end
    
    def visitDotNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.exclusive ? '..' : '...'}"
        indent
        visit(iVisited.begin_node)
        visit(iVisited.end_node)
        undent
        nil
    end
    
    def visitEnsureNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitEvStrNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitFCallNode(iVisited)
        puts @indent + iVisited.to_s + " " + iVisited.name
        indent
        iVisited.child_nodes.each {|n| visit(n)}
        undent
        nil
    end
    
    def visitFalseNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitFixnumNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitFlipNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitFloatNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitForNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitGlobalAsgnNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitGlobalVarNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitHashNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitInstAsgnNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitInstVarNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitIfNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitIterNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitLocalAsgnNode(iVisited)
        puts @indent + iVisited.to_s + " #{iVisited.name}"
        indent
        iVisited.child_nodes.each {|n| visit(n)}
        undent
        nil
    end
    
    def visitLocalVarNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitMultipleAsgnNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitMatch2Node(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitMatch3Node(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitMatchNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitModuleNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitNewlineNode(iVisited)
        puts @indent + iVisited.to_s
        old = @indent
        @indent += " "
        iVisited.next_node.accept(self.to_java_object)
        @indent = old
        nil
    end
    
    def visitNextNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitNilNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitNotNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitNthRefNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitOpElementAsgnNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitOpAsgnNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitOpAsgnAndNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitOpAsgnOrNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitOptNNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitOrNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitPostExeNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitRedoNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitRegexpNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitRescueBodyNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitRescueNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitRetryNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitReturnNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitSClassNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitScopeNode(iVisited)
        puts @indent + iVisited.to_s
        old = @indent
        @indent = @indent + " "
        iVisited.body_node.accept(self.to_java_object)
        @indent = old
        nil
    end
    
    def visitSelfNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitSplatNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitStrNode(iVisited)
        puts @indent + iVisited.to_s + "\"" + iVisited.value + "\""
    end
    
    def visitSuperNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitSValueNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitSymbolNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitToAryNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitTrueNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitUndefNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitUntilNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitVAliasNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitVCallNode(iVisited)
        puts @indent + iVisited.to_s + " " + iVisited.method_name
    end
    
    def visitWhenNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitWhileNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitXStrNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitYieldNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitZArrayNode(iVisited)
        puts @indent + iVisited.to_s
    end
    
    def visitZSuperNode(iVisited)
        puts @indent + iVisited.to_s
    end
end

x = JRuby.parse("def foo; 'bar'; end; foo", "x")
x.accept(MyVisitor.new)
