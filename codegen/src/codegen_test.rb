require 'codegen'

cc = ClassCreator.new("org.jruby.FooClass")
mc = cc.new_method("doFoo", MethodUtils.desc(["java.lang.String", "int", "java.lang.Object"], "void"))

mc.ldc("somestring")
mc.invokevirtual(ClassUtils.cls("java.lang.Object"), "toString", MethodUtils.desc([], "java.lang.String"))
