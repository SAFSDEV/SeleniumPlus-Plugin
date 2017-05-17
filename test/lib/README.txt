The jars in this directory are either test dependencies or are unsigned jars from SeleniumPlus/eclipse/plugins.
Since the tests mock Eclipse classes, the Eclipse jars have to be unsigned or messages like the following are seen:
class "org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager"'s signer information does not match signer information of other classes in the same package
