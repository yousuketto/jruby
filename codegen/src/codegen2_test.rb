require 'codegen2'

class_bytes = ClassBuilder.def_class :public, "FooClass" do
  def_constructor :public do
    call_super
    return_void
  end

  def_method :public, :string, "myMethod", [:void], [:exception] do
    call_this GenUtils.array_cls(:string), "getStringArr"
    call_this :string, "getMessage"
    return_top :ref
  end
  
  def_method :private, :string, "getMessage", [:void] do
    construct_obj :stringbuffer, [:string] do
      constant "Now I will say: "
    end
    
    call_method :stringbuffer, "append", :string, :stringbuffer do
      constant "Hello CodeGen!"
    end
    call_method :string, "toString", :void, :stringbuffer
    return_top :ref
  end
  
  def_method :public, GenUtils.array_cls(:string), "getStringArr" do
    construct_array :string, 5 do |i|
      constant "string \##{i}"
    end
    
    array_set 2, :string do
      constant "replacement at index 2"
    end
    
    return_top :ref
  end
end

include_class "java.io.FileOutputStream"

fos = FileOutputStream.new("FooClass.class")

fos.write(class_bytes)

include_class "org.jruby.util.JRubyClassLoader"

jcl = JRubyClassLoader.new

jcl.define_class("FooClass", class_bytes)

include_class "FooClass"

f = FooClass.new

puts f.myMethod
puts f.getStringArr