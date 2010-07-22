/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.codegen;

import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * A builder to create JavaCodeFragments with a uniform coding style.
 * <p>
 * The JavaCodeFragmentBuilder uses the method chaining pattern. That means: every method modifying
 * the {@link JavaCodeFragment} returNs this JavaCodeFragmentBuilder. So you could chain the methods
 * like this: javaCodeFragmentBuilder.methodBegin(<method signature parameter>).appendln(<one
 * line>).methodEnd();
 * 
 * @author Jan Ortmann
 */
public class JavaCodeFragmentBuilder {

    // the fragment under construction
    private JavaCodeFragment fragment;

    // The modifier that was passed in the last methodBegin called.
    private int lastMethodModifier;

    /**
     * Creates a new fragment builder to build Java sourcecode.
     */
    public JavaCodeFragmentBuilder() {
        this(false);
    }

    /**
     * Creates a new fragment builder to build Java sourcecode.
     */
    public JavaCodeFragmentBuilder(boolean indent) {
        fragment = new JavaCodeFragment(indent);
    }

    /**
     * Returns the fragment under construction.
     */
    public JavaCodeFragment getFragment() {
        return fragment;
    }

    /**
     * Adds an import entry to the code fragment under construction.
     * 
     * @param qualifiedClassName the java class that is added to the import declaration
     */
    public JavaCodeFragmentBuilder addImport(String qualifiedClassName) {
        fragment.addImport(qualifiedClassName);
        return this;
    }

    /**
     * Adds an import entry to the code fragment under construction.
     */
    public JavaCodeFragmentBuilder addImport(Class<?> clazz) {
        fragment.addImport(clazz.getName());
        return this;
    }

    /**
     * Appends a line separator to fragment's sourcecode.
     */
    public JavaCodeFragmentBuilder appendln() {
        fragment.appendln();
        return this;
    }

    /**
     * Appends the given String to the fragment's sourcecode.
     */
    public JavaCodeFragmentBuilder append(String s) {
        fragment.append(s);
        return this;
    }

    /**
     * Encloses the given String with doublequotes (") and appends it to fragment.
     */
    public JavaCodeFragmentBuilder appendQuoted(String s) {
        fragment.appendQuoted(s);
        return this;
    }

    /**
     * Appends the given char to the fragment's sourcecode.
     */
    public JavaCodeFragmentBuilder append(char c) {
        fragment.append(c);
        return this;
    }

    /**
     * Appends the given int to the fragment's sourcecode.
     */
    public JavaCodeFragmentBuilder append(int i) {
        fragment.append(i);
        return this;
    }

    /**
     * Appends the class' unqualified name to the sourcecode and updates the import declaration (if
     * necessary).
     */
    public JavaCodeFragmentBuilder appendClassName(Class<?> clazz) {
        fragment.appendClassName(clazz);
        return this;
    }

    /**
     * Appends the unqualified class name to the sourcecode and updates the import declaration (if
     * necessary).
     */
    public JavaCodeFragmentBuilder appendClassName(String qualifiedClassName) {
        fragment.appendClassName(qualifiedClassName);
        return this;
    }

    /**
     * Appends the given String and a line separator to the fragment's sourcecode.
     */
    public JavaCodeFragmentBuilder appendln(String s) {
        fragment.appendln(s);
        return this;
    }

    /**
     * Appends the given char to the fragment's sourcecode.
     */
    public JavaCodeFragmentBuilder appendln(char c) {
        fragment.appendln(c);
        return this;
    }

    /**
     * Appends the given fragment to the fragment under construction and idents it properly.
     */
    public JavaCodeFragmentBuilder append(JavaCodeFragment fragment) {
        this.fragment.append(fragment);
        return this;
    }

    /**
     * Append the Java modifier translated to a String, e.g. for java.lang.reflect.Modifier.PUBLIC
     * "public" is appended.
     * 
     * @param modifier Modifier according to java.lang.reflect.Modifier
     * 
     * @see java.lang.reflect.Modifier
     */
    public JavaCodeFragmentBuilder appendJavaModifier(int modifier) {
        append(Modifier.toString(modifier));
        return this;
    }

    /**
     * Adds an opening bracket followed by a newline and increases the indentation level by one
     * afterwards.
     */
    public JavaCodeFragmentBuilder openBracket() {
        if (!fragment.bol()) {
            fragment.appendln();
        }
        fragment.appendln('{');
        fragment.incIndentationLevel();
        return this;
    }

    /**
     * Adds a closing bracket and decreases the indentation level by one afterwards.
     */
    public JavaCodeFragmentBuilder closeBracket() {
        fragment.decIndentationLevel();
        if (!fragment.bol()) {
            fragment.appendln();
        }
        fragment.appendln('}');
        return this;
    }

    /**
     * Creates the Java source code for a method including signature, body and java doc.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor
     * @param methodName the name of the method.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param body the method body
     * @param javadoc the java documentation
     */
    public JavaCodeFragmentBuilder method(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            JavaCodeFragment body,
            String javadoc) {

        javaDoc(javadoc);
        methodBegin(modifier, returnType, methodName, argName, argClass);
        append(body);
        methodEnd();
        return this;
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType The className that the methods returns an instance of or <code>null</code>
     *            to indicate no return type in case of a constructor. The return type
     *            <code>void</code> is indicated by <code>java.lang.Void.class</code>:
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public JavaCodeFragmentBuilder methodBegin(int modifier,
            Class<?> returnType,
            String methodName,
            String[] argName,
            Class<?>[] argClass) {
        methodBegin(modifier, returnType, methodName, argName, argClass, null);
        return this;
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType The className that the methods returns an instance of or <code>null</code>
     *            to indicate no return type in case of a constructor. The return type
     *            <code>void</code> is indicated by <code>java.lang.Void.class</code>:
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public JavaCodeFragmentBuilder methodBegin(int modifier,
            Class<?> returnType,
            String methodName,
            String[] argName,
            Class<?>[] argClass,
            Class<?>[] exceptionClasses) {

        signatureInternal(modifier, methodName, new ClassAsParameterTypeSupport(argName, argClass, exceptionClasses,
                returnType), false);
        if (!Modifier.isAbstract(modifier)) {
            openBracket();
        }
        return this;
    }

    /**
     * Creates the Java source code for a method including signature, body and java doc.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName the name of the method.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param body the method body
     * @param javadoc the java documentation
     */
    public JavaCodeFragmentBuilder method(int modifier,
            Class<?> returnType,
            String methodName,
            String[] argName,
            Class<?>[] argClass,
            JavaCodeFragment body,
            String javadoc) {

        javaDoc(javadoc);
        methodBegin(modifier, returnType, methodName, argName, argClass);
        append(body);
        methodEnd();
        return this;
    }

    /**
     * Creates the Java source code for a method including signature, body and java doc.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName the name of the method.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param exceptionClasses Exception classes that can be thrown by the generated method
     * @param body the method body
     * @param javadoc the java documentation
     */
    public JavaCodeFragmentBuilder method(int modifier,
            Class<?> returnType,
            String methodName,
            String[] argName,
            Class<?>[] argClass,
            Class<?>[] exceptionClasses,
            JavaCodeFragment body,
            String javadoc) {

        javaDoc(javadoc);
        methodBegin(modifier, returnType, methodName, argName, argClass, exceptionClasses);
        append(body);
        methodEnd();
        return this;
    }

    /**
     * Creates the Java source code for a method including signature, body and java doc.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName the name of the method.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param body the method body
     * @param javadoc the java documentation
     * @param javaDocAnnotations annotations of the java documentation
     */
    public JavaCodeFragmentBuilder method(int modifier,
            Class<?> returnType,
            String methodName,
            String[] argName,
            Class<?>[] argClass,
            JavaCodeFragment body,
            String javadoc,
            String[] javaDocAnnotations) {

        javaDoc(javadoc, javaDocAnnotations);
        methodBegin(modifier, returnType, methodName, argName, argClass);
        append(body);
        methodEnd();
        return this;
    }

    /**
     * Creates the Java source code for a method including signature, body and java doc.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName the name of the method.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param body the method body
     * @param javadoc the java documentation
     * @param javaDocAnnotations annotations of the java documentation
     * @param annotations Java 5 annotations
     */
    public JavaCodeFragmentBuilder method(int modifier,
            Class<?> returnType,
            String methodName,
            String[] argName,
            Class<?>[] argClass,
            JavaCodeFragment body,
            String javadoc,
            String[] javaDocAnnotations,
            String[] annotations) {

        javaDoc(javadoc, javaDocAnnotations);
        annotation(annotations);
        methodBegin(modifier, returnType, methodName, argName, argClass);
        append(body);
        methodEnd();
        return this;
    }

    /**
     * Creates the Java source code for a method including signature, body and java doc.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor
     * @param methodName the name of the method.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param body the method body
     * @param javadoc the java documentation
     * @param javaDocAnnotations annotations of the java documentation
     */
    public JavaCodeFragmentBuilder method(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            JavaCodeFragment body,
            String javadoc,
            String[] javaDocAnnotations) {

        javaDoc(javadoc, javaDocAnnotations);
        methodBegin(modifier, returnType, methodName, argName, argClass);
        append(body);
        methodEnd();
        return this;
    }

    /**
     * Creates the Java source code for a method including signature, body and java doc.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor
     * @param methodName the name of the method.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param body the method body
     * @param javadoc the java documentation
     * @param javaDocAnnotations annotations of the java documentation
     * @param annotations Java 5 annotations
     */
    public JavaCodeFragmentBuilder method(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            JavaCodeFragment body,
            String javadoc,
            String[] javaDocAnnotations,
            String[] annotations) {

        javaDoc(javadoc, javaDocAnnotations);
        annotation(annotations);
        methodBegin(modifier, returnType, methodName, argName, argClass);
        append(body);
        methodEnd();
        return this;
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     */
    public JavaCodeFragmentBuilder methodBegin(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String javaDoc) {

        javaDoc(javaDoc);
        methodBegin(modifier, returnType, methodName, argName, argClass);
        return this;
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     * @param javaDocAnnotations annotations of the java documentation
     */
    public JavaCodeFragmentBuilder methodBegin(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String javaDoc,
            String[] javaDocAnnotations) {

        javaDoc(javaDoc, javaDocAnnotations);
        methodBegin(modifier, returnType, methodName, argName, argClass);
        return this;
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     * @param javaDocAnnotations annotations of the java documentation
     */
    public JavaCodeFragmentBuilder methodBegin(int modifier,
            Class<?> returnType,
            String methodName,
            String[] argName,
            Class<?>[] argClass,
            String javaDoc,
            String[] javaDocAnnotations) {

        javaDoc(javaDoc, javaDocAnnotations);
        methodBegin(modifier, returnType, methodName, argName, argClass);
        return this;
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public JavaCodeFragmentBuilder methodBegin(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass) {
        signature(modifier, returnType, methodName, argName, argClass);
        if (!Modifier.isAbstract(modifier)) {
            openBracket();
        }
        return this;
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public JavaCodeFragmentBuilder methodBegin(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String[] exceptionClasses) {
        signature(modifier, returnType, methodName, argName, argClass, exceptionClasses);
        if (!Modifier.isAbstract(modifier)) {
            openBracket();
        }
        return this;
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public JavaCodeFragmentBuilder signature(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass) {
        signature(modifier, returnType, methodName, argName, argClass, false);
        return this;
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param argFinal indicates if the arguments are prefix be a final modifier
     */
    public JavaCodeFragmentBuilder signature(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            boolean argFinal) {

        signatureInternal(modifier, methodName, new StringAsParameterTypeSupport(argName, argClass, null, returnType),
                argFinal);
        return this;
    }

    private JavaCodeFragmentBuilder signatureInternal(int modifier,
            String methodName,
            MethodSignatureTypesSupport support,
            boolean argFinal) {

        lastMethodModifier = modifier;
        append(Modifier.toString(modifier));
        append(' ');
        if (support.hasReturnType()) {
            support.appendReturnType();
            append(' ');
        }
        append(methodName);
        append('(');
        for (int i = 0; i < support.getNumberOfParameters(); i++) {
            if (i > 0) {
                append(", "); //$NON-NLS-1$
            }
            if (argFinal) {
                append("final "); //$NON-NLS-1$
            }
            support.appendParameterType(i);
            append(' ');
            support.appendParameterName(i);
        }
        append(')');

        if (support.getNumberOfExceptionExtensions() == 0) {
            return this;
        }
        append(" throws "); //$NON-NLS-1$
        for (int i = 0, max = support.getNumberOfExceptionExtensions(); i < max; i++) {
            if (i > 0) {
                append(", "); //$NON-NLS-1$
            }
            support.appendExceptionExtension(i);
        }
        return this;
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param exceptionClasses the thrown exceptions
     */
    public JavaCodeFragmentBuilder signature(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String[] exceptionClasses) {

        signatureInternal(modifier, methodName, new StringAsParameterTypeSupport(argName, argClass, exceptionClasses,
                returnType), false);
        return this;
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     * @param javaDocAnnotations annotations of the java documentation
     */
    public JavaCodeFragmentBuilder signature(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String javaDoc,
            String[] javaDocAnnotations) {

        javaDoc(javaDoc, javaDocAnnotations);
        signature(modifier, returnType, methodName, argName, argClass);
        return this;
    }

    /**
     * Appends the sourcecode for the the end of a method. If the method is abstract a semicolon is
     * generated, otherwise a closing bracket. If the method is abstract or not, is determined from
     * the modifier used in the last call to methodBegin().
     */
    public JavaCodeFragmentBuilder methodEnd() {
        if (Modifier.isAbstract(lastMethodModifier)) {
            fragment.appendln(";"); //$NON-NLS-1$
        } else {
            closeBracket();
        }
        fragment.appendln();
        return this;
    }

    /**
     * Appends the sourcecode for the beginning of a new class at the end of the fragment under
     * construction.
     */
    public JavaCodeFragmentBuilder classBegin(int modifier, String className) {
        classBegin(modifier, className, (String)null, null);
        return this;
    }

    /**
     * Appends the sourcecode for the beginning of a new class at the end of the fragment under
     * construction.
     */
    public JavaCodeFragmentBuilder classBegin(int modifier,
            String className,
            Class<?> extendsClass,
            Class<?> interfaces[]) {

        String extendsClassString = extendsClass == null ? null : extendsClass.getName();
        if (interfaces == null) {
            classBegin(modifier, className, extendsClassString, null);
            return this;
        }
        String[] interfaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaceNames[i] = interfaces[i].getName();
        }
        classBegin(modifier, className, extendsClassString, interfaceNames);
        return this;
    }

    /**
     * Appends the sourcecode for the beginning of a new class at the end of the fragment under
     * construction.
     */
    public JavaCodeFragmentBuilder classBegin(int modifier,
            String className,
            String extendsClassName,
            String interfaces[]) {

        fragment.append(Modifier.toString(modifier));
        fragment.append(" class "); //$NON-NLS-1$
        fragment.append(className);
        if (extendsClassName != null) {
            fragment.append(" extends "); //$NON-NLS-1$
            fragment.appendClassName(extendsClassName);
        }
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (i == 0) {
                    fragment.append(" implements "); //$NON-NLS-1$
                } else {
                    fragment.append(", "); //$NON-NLS-1$
                }
                fragment.appendClassName(interfaces[i]);
            }
        }
        fragment.appendln();
        openBracket();
        fragment.appendln();
        return this;
    }

    /**
     * Appends the sourcecode for the beginning of a new enum at the end of the fragment under
     * construction.
     */
    public JavaCodeFragmentBuilder enumBegin(int modifier,
            String className,
            String extendsClassName,
            String interfaces[]) {

        fragment.append(Modifier.toString(modifier));
        fragment.append(" enum "); //$NON-NLS-1$
        fragment.append(className);
        if (extendsClassName != null) {
            fragment.append(" extends "); //$NON-NLS-1$
            fragment.appendClassName(extendsClassName);
        }
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (i == 0) {
                    fragment.append(" implements "); //$NON-NLS-1$
                } else {
                    fragment.append(", "); //$NON-NLS-1$
                }
                fragment.appendClassName(interfaces[i]);
            }
        }
        fragment.appendln();
        openBracket();
        fragment.appendln();
        return this;
    }

    /**
     * Writes the code at the end of a class.
     */
    public JavaCodeFragmentBuilder classEnd() {
        closeBracket();
        return this;
    }

    /**
     * Appends the sourcecode for the beginning of a new interface at the end of the fragment under
     * construction.
     */
    public JavaCodeFragmentBuilder interfaceBegin(String interfaceName) {
        interfaceBegin(interfaceName, ""); //$NON-NLS-1$
        return this;
    }

    /**
     * Appends the sourcecode for the beginning of a new interface at the end of the fragment under
     * construction.
     */
    public JavaCodeFragmentBuilder interfaceBegin(String interfaceName, String extendsInterfaceName) {
        fragment.append("public interface "); //$NON-NLS-1$
        fragment.append(interfaceName);
        if (StringUtils.isNotEmpty(extendsInterfaceName)) {
            fragment.append(" extends "); //$NON-NLS-1$
            fragment.appendClassName(extendsInterfaceName);
        }
        fragment.appendln();
        openBracket();
        return this;
    }

    /**
     * Appends the sourcecode for the beginning of a new interface at the end of the fragment under
     * construction.
     */
    public JavaCodeFragmentBuilder interfaceBegin(String interfaceName, String[] extendedInterfaces) {
        fragment.append("public interface "); //$NON-NLS-1$
        fragment.append(interfaceName);
        if (extendedInterfaces != null && extendedInterfaces.length > 0) {
            fragment.append(" extends "); //$NON-NLS-1$
            for (int i = 0; i < extendedInterfaces.length; i++) {
                if (i > 0) {
                    fragment.append(", "); //$NON-NLS-1$
                }
                fragment.appendClassName(extendedInterfaces[i]);
            }
        }
        fragment.appendln();
        openBracket();
        return this;
    }

    /**
     * Creates a new variable declaration.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param clazz The class the variable is an instance of
     * @param varName the variable's name.
     */
    public JavaCodeFragmentBuilder varDeclaration(int modifier, Class<?> clazz, String varName) {
        varDeclaration(modifier, clazz.getName(), varName);
        return this;
    }

    /**
     * Creates a new variable declaration.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param clazz The class the variable is an instance of
     * @param varName the variable's name.
     * @param expression the initial value of the variable
     */
    public JavaCodeFragmentBuilder varDeclaration(int modifier,
            Class<?> clazz,
            String varName,
            JavaCodeFragment expression) {

        varDeclaration(modifier, clazz.getName(), varName, expression);
        return this;
    }

    /**
     * Creates a new variable declaration.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param className The class' name the variable is an instance of
     * @param varName the variable's name.
     * @param expression the initial value of the variable
     */
    public JavaCodeFragmentBuilder varDeclaration(int modifier,
            String className,
            String varName,
            JavaCodeFragment expression) {
        if (modifier > 0) {
            fragment.append(Modifier.toString(modifier));
            fragment.append(' ');
        }
        if (className != null) {
            appendClassName(className);
            append(' ');
        }
        fragment.append(varName);
        if (expression != null) {
            fragment.append(" = "); //$NON-NLS-1$
            fragment.append(expression);
        }
        fragment.appendln(";"); //$NON-NLS-1$
        return this;
    }

    /**
     * Creates a new variable declaration.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param className The class' name the variable is an instance of
     * @param varName the variable's name.
     */
    public JavaCodeFragmentBuilder varDeclaration(int modifier, String className, String varName) {
        varDeclaration(modifier, className, varName, null);
        return this;
    }

    /**
     * Writes a variable definition.
     */
    public JavaCodeFragmentBuilder varDefinition(Class<?> varClass, String varName, String varValue) {
        varDefinition(varClass.getName(), varName, varValue);
        return this;
    }

    /**
     * Writes a variable definition.
     */
    public JavaCodeFragmentBuilder varDefinition(String classOrTypeName, String variableName, String variableValue) {
        fragment.append(classOrTypeName);
        fragment.append(' ');
        assignment(variableName, variableValue);
        return this;
    }

    /**
     * Appends a new assignment.
     * 
     * <code>
     * varName = value;
     * </code>
     */
    public JavaCodeFragmentBuilder assignment(String variable, JavaCodeFragment expression) {
        fragment.append(variable);
        fragment.append(" = "); //$NON-NLS-1$
        fragment.append(expression);
        fragment.appendln(";"); //$NON-NLS-1$
        return this;
    }

    /**
     * Creates a new variable declaration.
     * 
     * <code>
     * varName = value;
     * </code>
     */
    public JavaCodeFragmentBuilder assignment(String variable, String value) {
        fragment.append(variable);
        fragment.append(" = "); //$NON-NLS-1$
        fragment.append(value);
        fragment.appendln(";"); //$NON-NLS-1$
        return this;
    }

    public JavaCodeFragmentBuilder singleLineComment(String comment) {
        fragment.append("// "); //$NON-NLS-1$
        if (comment != null) {
            fragment.appendln(comment);
        }
        return this;
    }

    public JavaCodeFragmentBuilder multiLineComment(String comment) {
        fragment.appendln("/*"); //$NON-NLS-1$
        if (comment != null) {
            fragment.appendln("   "); //$NON-NLS-1$
            fragment.appendln(comment);
        }
        fragment.appendln("*/"); //$NON-NLS-1$
        return this;
    }

    /**
     * Put the given text into a Javadoc comment.
     */
    public JavaCodeFragmentBuilder javaDoc(String text) {
        javaDoc(text, null);
        return this;
    }

    /**
     * Puts the given text and annotations into a java doc comment. For an annotation only the
     * annotation name and optionally separated by a space character an annotation text needs to be
     * specified. The '@' character will be automatically added.
     */
    public JavaCodeFragmentBuilder javaDoc(String text, String[] annotations) {
        if (text == null && annotations == null) {
            return this;
        }
        fragment.appendln("/**"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(text)) {
            String[] lines = StringUtils.split(text, SystemUtils.LINE_SEPARATOR);
            for (String line : lines) {
                fragment.append(" * "); //$NON-NLS-1$
                fragment.appendln(line);
            }
        }
        if (annotations != null) {
            // create an empty line to separate the text (if any) from the custom tags.
            if (StringUtils.isNotEmpty(text)) {
                fragment.appendln(" * "); //$NON-NLS-1$
            }
            for (String annotation : annotations) {
                fragment.append(" * @"); //$NON-NLS-1$
                fragment.appendln(annotation);
            }
        }
        fragment.appendln(" */"); //$NON-NLS-1$
        return this;
    }

    /**
     * Writes the annotation and a line separator. For an annotation only the annotation name needs
     * to be specified. The '@' character will be automatically added.
     */
    public JavaCodeFragmentBuilder annotationLn(Class<?> annotation) {
        if (annotation == null) {
            return this;
        }
        fragment.append("@"); //$NON-NLS-1$
        fragment.appendClassName(annotation);
        fragment.appendln();
        return this;
    }

    /**
     * Writes the annotation with the indicated parameters and a line separator. '@' character and a
     * line feed will be automatically added. Import statements are added automatically to the code
     * fragment (if needed).
     * 
     * @param annotation The annotation class
     * @param params Parameters for the annotation without paranthesis. If <code>null</code> or an
     *            empty String, paranthesis aren't added.
     */
    public JavaCodeFragmentBuilder annotationLn(Class<?> annotation, String params) {
        if (annotation == null) {
            return this;
        }
        annotationLn(annotation.getName(), params);
        return this;
    }

    /**
     * Writes the annotation with the indicated parameter of type String. '@' character and a line
     * feed will be automatically added. Import statements are added automatically to the code
     * fragment (if needed).
     * 
     * <pre>
     * Example 
     *   annotation : javax.xml.bind.annotation.XmlRootElement
     *   paramName  : name
     *   stringValue: policy
     *   Result: @XmlElement(name=&quot;Policy&quot;)
     * </pre>
     * 
     * @param annotation The annotation class
     * @param paramName The name of the parameter
     * @param stringValue The unqoted string value for the parameter. This method generates the
     *            quotes.
     * 
     */
    public JavaCodeFragmentBuilder annotationLn(Class<?> annotation, String paramName, String stringValue) {
        if (annotation == null) {
            return this;
        }
        annotationLn(annotation.getName(), paramName, stringValue);
        return this;
    }

    /**
     * Writes the annotation with the indicated parameters. '@' character and a line feed will be
     * automatically added. Import statements are added automatically to the code fragment (if
     * needed).
     * 
     * @param annotation The annotation class
     * @param params Parameters for the annotation without paranthesis. If <code>null</code> or an
     *            empty String, paranthesis aren't added.
     */
    public JavaCodeFragmentBuilder annotationLn(String annotation, String params) {
        if (annotation == null) {
            return this;
        }
        fragment.append("@"); //$NON-NLS-1$
        fragment.appendClassName(annotation);
        if (params != null && params.length() > 0) {
            fragment.append('(');
            fragment.append(params);
            fragment.append(')');
        }
        fragment.appendln();
        return this;
    }

    /**
     * Writes the annotation with the indicated parameter of type String. '@' character and a line
     * feed will be automatically added. Import statements are added automatically to the code
     * fragment (if needed).
     * 
     * <pre>
     * Example 
     *   annotation : javax.xml.bind.annotation.XmlRootElement
     *   paramName  : name
     *   stringValue: policy
     *   Result: @XmlElement(name=&quot;Policy&quot;)
     * </pre>
     * 
     * @param annotation The annotation class
     * @param paramName The name of the parameter
     * @param stringValue The unqoted string value for the parameter. This method generates the
     *            quotes.
     * 
     */
    public JavaCodeFragmentBuilder annotationLn(String annotation, String paramName, String stringValue) {
        if (annotation == null) {
            return this;
        }
        fragment.append("@"); //$NON-NLS-1$
        fragment.appendClassName(annotation);
        fragment.append('(');
        fragment.append(paramName);
        fragment.append('=');
        fragment.appendQuoted(stringValue);
        fragment.append(')');
        fragment.appendln();
        return this;
    }

    /**
     * Writes the annotation with the indicated parameter of type Class. '@' character and a line
     * feed will be automatically added. Import statements are added automatically to the code
     * fragment (if needed).
     * 
     * <pre>
     * Example 
     *   annotation : javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
     *   paramName  : value
     *   classValue: EnumValueXmlAdapter.class
     *   Result: @XmlJavaTypeAdapter(value = EnumValueXmlAdapter.class)
     * </pre>
     * 
     * @param annotation The annotation class
     * @param paramName The name of the parameter
     * @param classValue The class value for the parameter
     */
    public JavaCodeFragmentBuilder annotationLn(String annotation, String paramName, Class<?> classValue) {
        if (annotation == null) {
            return this;
        }
        fragment.append("@"); //$NON-NLS-1$
        fragment.appendClassName(annotation);
        fragment.append('(');
        fragment.append(paramName);
        fragment.append('=');
        fragment.appendClassName(classValue);
        fragment.append(".class"); //$NON-NLS-1$
        fragment.append(')');
        fragment.appendln();
        return this;
    }

    /**
     * Writes the annotation for a class value. '@' character and a line feed will be automatically
     * added. Import statements are added automatically to the code fragment (if needed).
     * 
     * <pre>
     * Example 
     *   annotation : javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
     *   paramName  : value
     *   classValue: EnumValueXmlAdapter.class
     *   Result: @XmlJavaTypeAdapter(value = EnumValueXmlAdapter.class)
     * </pre>
     * 
     * @param annotation The annotation class
     * @param paramName The name of the parameter
     * @param qualifiedClassName The class value for the parameter
     */
    public JavaCodeFragmentBuilder annotationClassValueLn(String annotation, String paramName, String qualifiedClassName) {
        if (annotation == null) {
            return this;
        }
        fragment.append("@"); //$NON-NLS-1$
        fragment.appendClassName(annotation);
        fragment.append('(');
        fragment.append(paramName);
        fragment.append('=');
        fragment.appendClassName(qualifiedClassName);
        fragment.append(".class"); //$NON-NLS-1$
        fragment.append(')');
        fragment.appendln();
        return this;
    }

    /**
     * Writes the annotation. For an annotation the (fully qualified) annotation class name needs to
     * be specified. The '@' character and a line feed will be automatically added. The annoation
     * may contain parameters in paranthesis.
     * 
     * @param annotation The fully qualified annotation name
     */
    public JavaCodeFragmentBuilder annotationLn(String annotation) {
        if (annotation == null) {
            return this;
        }
        fragment.append("@"); //$NON-NLS-1$
        int index = annotation.indexOf('(');
        if (index == -1) {
            fragment.appendClassName(annotation);
            fragment.appendln();
            return this;
        }
        fragment.appendClassName(annotation.substring(0, index));
        fragment.appendln(annotation.substring(index));
        return this;
    }

    /**
     * Writes the annotations. For an annotation only the annotation name needs to be specified. The
     * '@' character and a line feed will be automatically added.
     */
    public JavaCodeFragmentBuilder annotationLn(Class<?>[] annotations) {
        if (annotations == null) {
            return this;
        }
        for (Class<?> annotation : annotations) {
            annotationLn(annotation);
        }
        return this;
    }

    /**
     * Writes the annotations. For an annotation only the annotation name needs to be specified. The
     * '@' character and a line feed will be automatically added.
     */
    public JavaCodeFragmentBuilder annotation(String[] annotations) {
        if (annotations == null) {
            return this;
        }
        for (String annotation : annotations) {
            annotationLn(annotation);
        }
        return this;
    }

    /**
     * Writes a method call to the java code fragment builder.
     * 
     * @param name The name of the method to call
     * @param parameters the parameters for the method call
     * @return the fragment builder for method chaining
     */
    public JavaCodeFragmentBuilder methodCall(String name, String[] parameters, boolean finishLine) {
        fragment.append(name);
        appendParameters(parameters);
        if (finishLine) {
            fragment.appendln(';');
        }
        return this;
    }

    /**
     * Writes a method call to the java code fragment builder.
     * 
     * @param name The name of the method to call
     * @param parameterFragments the parameters for the method call
     * @return the fragment builder for method chaining
     */
    public JavaCodeFragmentBuilder methodCall(String name, JavaCodeFragment[] parameterFragments, boolean finishLine) {
        fragment.append(name);
        appendParameters(parameterFragments);
        if (finishLine) {
            fragment.appendln(';');
        }
        return this;
    }

    /**
     * @see #methodCall(String, String[], boolean)
     * 
     * @param name the name of the method
     * @param parameters the list of parameters
     * @return the fragment builder for method chaining
     */
    public JavaCodeFragmentBuilder methodCall(String name, List<String> parameters, boolean finishLine) {
        return methodCall(name, parameters.toArray(new String[parameters.size()]), finishLine);
    }

    /**
     * append a constructor call: new <name>(parameters[0], parameters[1], ...)
     * 
     * @param finishLine append a semicolon and new line if true
     * @return the JavaCodeFragmentBuilder for Method chaining
     */
    public JavaCodeFragmentBuilder constructorCall(String className, String[] parameters, boolean finishLine) {
        append("new "); //$NON-NLS-1$
        appendClassName(className);
        appendParameters(parameters);
        if (finishLine) {
            fragment.appendln(';');
        }
        return this;
    }

    /**
     * @see #constructorCall(String, String[], boolean)
     * @return @see #constructorCall(String, String[], boolean)
     */
    public JavaCodeFragmentBuilder constructorCall(String className, List<String> parameters, boolean finishLine) {
        return constructorCall(className, parameters.toArray(new String[parameters.size()]), finishLine);
    }

    public JavaCodeFragmentBuilder appendParameters(String[] parameters) {
        JavaCodeFragment[] jcfParams = new JavaCodeFragment[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            jcfParams[i] = new JavaCodeFragment(parameters[i]);
        }
        appendParameters(jcfParams);
        return this;
    }

    public JavaCodeFragmentBuilder appendParameters(JavaCodeFragment[] parameters) {
        append('(');
        int i = 1;
        for (JavaCodeFragment aParameter : parameters) {
            append(aParameter);
            if (i < parameters.length) {
                fragment.append(", "); //$NON-NLS-1$
            }
            i++;
        }
        append(')');
        return this;
    }

    /**
     * Appends the generic parameters to the builder: <className1, className2, ...>
     * <p>
     * Note: You have to make sure that generics are supported by target compiler
     */
    public JavaCodeFragmentBuilder appendGenerics(String... classNames) {
        if (classNames.length > 0) {
            append('<');
            for (String className : classNames) {
                append(className);
                if (!className.equals(classNames[classNames.length - 1])) {
                    append(", "); //$NON-NLS-1$
                }
            }
            append('>');
        }
        return this;
    }

    /**
     * Appends the generic parameters to the builder: <class1, class2, ...>
     * <p>
     * Note: You have to make sure that generics are supported by target compiler
     */
    public JavaCodeFragmentBuilder appendGenerics(Class<?>... classes) {
        if (classes.length > 0) {
            append('<');
            int i = 1;
            for (Class<?> clazz : classes) {
                appendClassName(clazz);
                if (i < classes.length) {
                    append(", "); //$NON-NLS-1$
                    i++;
                }
            }
            append('>');
        }
        return this;
    }

    @Override
    public String toString() {
        return fragment.toString();
    }

    /**
     * The <code>method</code> methods of this builder accept <code>java.lang.String</code> or
     * <code>java.lang.Class</code> values for defining the type of a parameter or the return type
     * of a method. The slight variations on how to process the two different types a covered by
     * implementations of this class.
     * 
     * @author Peter Erzberger
     */
    private static abstract class MethodSignatureTypesSupport {

        protected void check(String[] parameterNames, Object[] parameterTypes) {
            if (parameterNames != null && parameterNames.length != parameterTypes.length) {
                throw new RuntimeException("Named and Class array must have the same length"); //$NON-NLS-1$
            }
        }

        public abstract boolean hasReturnType();

        public abstract void appendReturnType();

        public abstract int getNumberOfParameters();

        public abstract void appendParameterName(int index);

        public abstract void appendParameterType(int index);

        public abstract void appendExceptionExtension(int index);

        public abstract int getNumberOfExceptionExtensions();
    }

    private class ClassAsParameterTypeSupport extends MethodSignatureTypesSupport {

        private String[] parameterNames = null;
        private Class<?>[] parameterTypes = null;
        private Class<?> returnType = null;
        private Class<?>[] exceptionClasses = null;

        /**
         * 
         */
        public ClassAsParameterTypeSupport(String[] parameterNames, Class<?>[] parameterTypes,
                Class<?>[] exceptionClasses, Class<?> returnType) {
            check(parameterNames, parameterTypes);
            this.parameterNames = parameterNames;
            this.parameterTypes = parameterTypes;
            this.exceptionClasses = exceptionClasses;
            this.returnType = returnType;
        }

        @Override
        public void appendParameterName(int index) {
            append(parameterNames[index]);
        }

        @Override
        public void appendParameterType(int index) {
            appendClassName(parameterTypes[index]);
        }

        @Override
        public void appendReturnType() {
            appendClassName(returnType);
        }

        @Override
        public int getNumberOfParameters() {
            return parameterNames == null ? 0 : parameterNames.length;
        }

        @Override
        public boolean hasReturnType() {
            return returnType != null;
        }

        @Override
        public void appendExceptionExtension(int index) {
            appendClassName(exceptionClasses[index]);
        }

        @Override
        public int getNumberOfExceptionExtensions() {
            return exceptionClasses == null ? 0 : exceptionClasses.length;
        }
    }

    private class StringAsParameterTypeSupport extends MethodSignatureTypesSupport {

        private String[] parameterNames = null;
        private String[] parameterTypes = null;
        private String[] exceptionClasses = null;
        private String returnType = null;

        /**
         * 
         */
        public StringAsParameterTypeSupport(String[] parameterNames, String[] parameterTypes,
                String[] exceptionClasses, String returnType) {
            check(parameterNames, parameterTypes);
            this.parameterNames = parameterNames;
            this.parameterTypes = parameterTypes;
            this.exceptionClasses = exceptionClasses;
            this.returnType = returnType;
        }

        @Override
        public void appendParameterName(int index) {
            append(parameterNames[index]);
        }

        @Override
        public void appendParameterType(int index) {
            appendClassName(parameterTypes[index]);
        }

        @Override
        public void appendReturnType() {
            appendClassName(returnType);
        }

        @Override
        public int getNumberOfParameters() {
            return parameterNames == null ? 0 : parameterNames.length;
        }

        @Override
        public boolean hasReturnType() {
            return returnType != null;
        }

        @Override
        public void appendExceptionExtension(int index) {
            appendClassName(exceptionClasses[index]);
        }

        @Override
        public int getNumberOfExceptionExtensions() {
            return exceptionClasses == null ? 0 : exceptionClasses.length;
        }
    }
}
