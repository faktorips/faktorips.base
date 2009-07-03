/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * A builder to create JavaCodeFragments with a uniform coding style.
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
        fragment = new JavaCodeFragment();
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
    public void addImport(String qualifiedClassName) {
        fragment.addImport(qualifiedClassName);
    }

    /**
     * Adds an import entry to the code fragment under construction.
     */
    public void addImport(Class<?> clazz) {
        fragment.addImport(clazz.getName());
    }

    /**
     * Appends a line separator to fragment's sourcecode.
     */
    public void appendln() {
        fragment.appendln();
    }

    /**
     * Appends the given String to the fragment's sourcecode.
     */
    public void append(String s) {
        fragment.append(s);
    }

    /**
     * Encloses the given String with doublequotes (") and appends it to fragment.
     */
    public void appendQuoted(String s) {
        fragment.appendQuoted(s);
    }

    /**
     * Appends the given char to the fragment's sourcecode.
     */
    public void append(char c) {
        fragment.append(c);
    }

    /**
     * Appends the class' unqualified name to the sourcecode and updates the import declaration (if
     * necessary).
     */
    public void appendClassName(Class<?> clazz) {
        fragment.appendClassName(clazz);
    }

    /**
     * Appends the unqualified class name to the sourcecode and updates the import declaration (if
     * necessary).
     */
    public void appendClassName(String qualifiedClassName) {
        fragment.appendClassName(qualifiedClassName);
    }

    /**
     * Appends the given String and a line separator to the fragment's sourcecode.
     */
    public void appendln(String s) {
        fragment.appendln(s);
    }

    /**
     * Appends the given char to the fragment's sourcecode.
     */
    public void appendLn(char c) {
        fragment.appendln(c);
    }

    /**
     * Appends the given fragment to the fragment under construction and idents it properly.
     */
    public void append(JavaCodeFragment fragment) {
        this.fragment.append(fragment);
    }

    /**
     * Append the Java modifier translated to a String, e.g. for java.lang.reflect.Modifier.PUBLIC
     * "public" is appended.
     * 
     * @param modifier Modifier according to java.lang.reflect.Modifier
     * 
     * @see java.lang.reflect.Modifier
     */
    public void appendJavaModifier(int modifier) {
        append(Modifier.toString(modifier));
    }

    /**
     * Adds an opening bracket followed by a newline and increases the indentation level by one
     * afterwards.
     */
    public void openBracket() {
        if (!fragment.bol()) {
            fragment.appendln();
        }
        fragment.appendln('{');
        fragment.incIndentationLevel();
    }

    /**
     * Adds a closing bracket and decreases the indentation level by one afterwards.
     */
    public void closeBracket() {
        fragment.decIndentationLevel();
        if (!fragment.bol()) {
            fragment.appendln();
        }
        fragment.appendln('}');
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
    public void method(int modifier,
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
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType The className that the methods returns an instance of or <code>null</code>
     *            to indicate no return type in case of a constructor. The return type
     *            <code>void</code> is indictaed by <code>java.lang.Void.class</code>:
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public void methodBegin(int modifier, Class<?> returnType, String methodName, String[] argName, Class<?>[] argClass) {

        methodBegin(modifier, returnType, methodName, argName, argClass, null);
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType The className that the methods returns an instance of or <code>null</code>
     *            to indicate no return type in case of a constructor. The return type
     *            <code>void</code> is indicated by <code>java.lang.Void.class</code>:
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public void methodBegin(int modifier,
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
    public void method(int modifier,
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
    public void method(int modifier,
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
    public void method(int modifier,
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
    public void method(int modifier,
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
    public void method(int modifier,
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
    public void method(int modifier,
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
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     */
    public void methodBegin(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String javaDoc) {

        javaDoc(javaDoc);
        methodBegin(modifier, returnType, methodName, argName, argClass);
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     * @param javaDocAnnotations annotations of the java documentation
     */
    public void methodBegin(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String javaDoc,
            String[] javaDocAnnotations) {

        javaDoc(javaDoc, javaDocAnnotations);
        methodBegin(modifier, returnType, methodName, argName, argClass);
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     * @param javaDocAnnotations annotations of the java documentation
     */
    public void methodBegin(int modifier,
            Class<?> returnType,
            String methodName,
            String[] argName,
            Class<?>[] argClass,
            String javaDoc,
            String[] javaDocAnnotations) {

        javaDoc(javaDoc, javaDocAnnotations);
        methodBegin(modifier, returnType, methodName, argName, argClass);
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public void methodBegin(int modifier, String returnType, String methodName, String[] argName, String[] argClass) {
        signature(modifier, returnType, methodName, argName, argClass);
        if (!Modifier.isAbstract(modifier)) {
            openBracket();
        }
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public void methodBegin(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String[] exceptionClasses) {
        signature(modifier, returnType, methodName, argName, argClass, exceptionClasses);
        if (!Modifier.isAbstract(modifier)) {
            openBracket();
        }
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     * @param javaDocAnnotations annotations of the java documentation
     */
    public void signature(int modifier, String returnType, String methodName, String[] argName, String[] argClass) {
        signature(modifier, returnType, methodName, argName, argClass, false);
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     * @param javaDocAnnotations annotations of the java documentation
     * @param argFinal indicates if the arguments are prefix be a final modifier
     */
    public void signature(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            boolean argFinal) {
        signatureInternal(modifier, methodName, new StringAsParameterTypeSupport(argName, argClass, null, returnType),
                argFinal);
    }

    private void signatureInternal(int modifier,
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
                append(", ");
            }
            if (argFinal) {
                append("final ");
            }
            support.appendParameterType(i);
            append(' ');
            support.appendParameterName(i);
        }
        append(')');

        if (support.getNumberOfExceptionExtensions() == 0) {
            return;
        }
        append(" throws ");
        for (int i = 0, max = support.getNumberOfExceptionExtensions(); i < max; i++) {
            if (i > 0) {
                append(", ");
            }
            support.appendExceptionExtension(i);
        }
    }

    /**
     * Creates the Java source code for a method signature.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     * @param javaDocAnnotations annotations of the java documentation
     */
    public void signature(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String[] exceptionClasses) {

        signatureInternal(modifier, methodName, new StringAsParameterTypeSupport(argName, argClass, exceptionClasses,
                returnType), false);
    }

    /**
     * Creates the Java source code for a method signature. If the method is non abstract the
     * generated code ends with an opening bracket '{'. If the method is abstract the code ends with
     * the argument list's closing bracket ')'.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returns an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param javaDoc the java documentation for this method signature
     * @param javaDocAnnotations annotations of the java documentation
     */
    public void signature(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass,
            String javaDoc,
            String[] javaDocAnnotations) {

        javaDoc(javaDoc, javaDocAnnotations);
        signature(modifier, returnType, methodName, argName, argClass);
    }

    /**
     * Appends the sourcecode for the the end of a method. If the method is abstract a semicolon is
     * generated, otherwise a closing bracket. If the method is abstract or not, is determined from
     * the modifier used in the last call to methodBegin().
     */
    public void methodEnd() {
        if (Modifier.isAbstract(lastMethodModifier)) {
            fragment.appendln(";");
        } else {
            closeBracket();
        }
        fragment.appendln();
    }

    /**
     * Appends the sourcecode for the beginning of a new class at the end of the fragment under
     * construction.
     */
    public void classBegin(int modifier, String className) {
        classBegin(modifier, className, (String)null, null);
    }

    /**
     * Appends the sourcecode for the beginning of a new class at the end of the fragment under
     * construction.
     */
    public void classBegin(int modifier, String className, Class<?> extendsClass, Class<?> interfaces[]) {
        String extendsClassString = extendsClass == null ? null : extendsClass.getName();
        if (interfaces == null) {
            classBegin(modifier, className, extendsClassString, null);
            return;
        }
        String[] interfaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaceNames[i] = interfaces[i].getName();
        }
        classBegin(modifier, className, extendsClassString, interfaceNames);

    }

    /**
     * Appends the sourcecode for the beginning of a new class at the end of the fragment under
     * construction.
     */
    public void classBegin(int modifier, String className, String extendsClassName, String interfaces[]) {

        fragment.append(Modifier.toString(modifier));
        fragment.append(" class ");
        fragment.append(className);
        if (extendsClassName != null) {
            fragment.append(" extends ");
            fragment.appendClassName(extendsClassName);
        }
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (i == 0) {
                    fragment.append(" implements ");
                } else {
                    fragment.append(", ");
                }
                fragment.appendClassName(interfaces[i]);
            }
        }
        fragment.appendln();
        openBracket();
        fragment.appendln();
    }

    /**
     * Appends the sourcecode for the beginning of a new enum at the end of the fragment under
     * construction.
     */
    public void enumBegin(int modifier, String className, String extendsClassName, String interfaces[]) {

        fragment.append(Modifier.toString(modifier));
        fragment.append(" enum ");
        fragment.append(className);
        if (extendsClassName != null) {
            fragment.append(" extends ");
            fragment.appendClassName(extendsClassName);
        }
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (i == 0) {
                    fragment.append(" implements ");
                } else {
                    fragment.append(", ");
                }
                fragment.appendClassName(interfaces[i]);
            }
        }
        fragment.appendln();
        openBracket();
        fragment.appendln();
    }

    /**
     * Writes the code at the end of a class.
     */
    public void classEnd() {
        closeBracket();
    }

    /**
     * Appends the sourcecode for the beginning of a new interface at the end of the fragment under
     * construction.
     */
    public void interfaceBegin(String interfaceName) {
        interfaceBegin(interfaceName, "");
    }

    /**
     * Appends the sourcecode for the beginning of a new interface at the end of the fragment under
     * construction.
     */
    public void interfaceBegin(String interfaceName, String extendsInterfaceName) {
        fragment.append("public interface ");
        fragment.append(interfaceName);
        if (StringUtils.isNotEmpty(extendsInterfaceName)) {
            fragment.append(" extends ");
            fragment.appendClassName(extendsInterfaceName);
        }
        fragment.appendln();
        openBracket();
    }

    /**
     * Appends the sourcecode for the beginning of a new interface at the end of the fragment under
     * construction.
     */
    public void interfaceBegin(String interfaceName, String[] extendedInterfaces) {
        fragment.append("public interface ");
        fragment.append(interfaceName);
        if (extendedInterfaces != null && extendedInterfaces.length > 0) {
            fragment.append(" extends ");
            for (int i = 0; i < extendedInterfaces.length; i++) {
                if (i > 0) {
                    fragment.append(", ");
                }
                fragment.appendClassName(extendedInterfaces[i]);
            }
        }
        fragment.appendln();
        openBracket();
    }

    /**
     * Creates a new variable declaration.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param clazz The class the variable is an instance of
     * @param varName the variable's name.
     */
    public void varDeclaration(int modifier, Class<?> clazz, String varName) {

        varDeclaration(modifier, clazz.getName(), varName);
    }

    /**
     * Creates a new variable declaration.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param clazz The class the variable is an instance of
     * @param varName the variable's name.
     * @param expression the initial value of the variable
     */
    public void varDeclaration(int modifier, Class<?> clazz, String varName, JavaCodeFragment expression) {

        varDeclaration(modifier, clazz.getName(), varName, expression);
    }

    /**
     * Creates a new variable declaration.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param className The class' name the variable is an instance of
     * @param varName the variable's name.
     * @param expression the initial value of the variable
     */
    public void varDeclaration(int modifier, String className, String varName, JavaCodeFragment expression) {
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
            fragment.append(" = ");
            fragment.append(expression);
        }
        fragment.appendln(";");
    }

    /**
     * Creates a new variable declaration.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param className The class' name the variable is an instance of
     * @param varName the variable's name.
     */
    public void varDeclaration(int modifier, String className, String varName) {
        varDeclaration(modifier, className, varName, null);
    }

    /**
     * Writes a variable definition.
     */
    public void varDefinition(Class<?> varClass, String varName, String varValue) {
        varDefinition(varClass.getName(), varName, varValue);
    }

    /**
     * Writes a variable definition.
     */
    public void varDefinition(String classOrTypeName, String variableName, String variableValue) {
        fragment.append(classOrTypeName);
        fragment.append(' ');
        assignment(variableName, variableValue);
    }

    /**
     * Appends a new assignment.
     * 
     * <code>
     * varName = value;
     * </code>
     */
    public void assignment(String variable, JavaCodeFragment expression) {
        fragment.append(variable);
        fragment.append(" = ");
        fragment.append(expression);
        fragment.appendln(";");
    }

    /**
     * Creates a new variable declaration.
     * 
     * <code>
     * varName = value;
     * </code>
     */
    public void assignment(String variable, String value) {
        fragment.append(variable);
        fragment.append(" = ");
        fragment.append(value);
        fragment.appendln(";");
    }

    public void singleLineComment(String comment) {
        fragment.append("// ");
        if (comment != null) {
            fragment.appendln(comment);
        }
    }

    public void multiLineComment(String comment) {
        fragment.appendln("/*");
        if (comment != null) {
            fragment.appendln("   ");
            fragment.appendln(comment);
        }
        fragment.appendln("*/");
    }

    /**
     * Put the given text into a javadoc comment.
     */
    public void javaDoc(String text) {
        javaDoc(text, null);
    }

    /**
     * Puts the given text and annotations into a java doc comment. For an annotation only the
     * annotation name and optionally separated by a space character an annotation text needs to be
     * specified. The '@' character will be automatically added.
     * 
     * @param text
     * @param annotations
     */
    public void javaDoc(String text, String[] annotations) {
        if (text == null && annotations == null) {
            return;
        }
        fragment.appendln("/**");
        if (text != null) {
            String[] lines = StringUtils.split(text, SystemUtils.LINE_SEPARATOR);
            for (int i = 0; i < lines.length; i++) {
                fragment.append(" * ");
                fragment.appendln(lines[i]);
            }
        }
        if (annotations != null) {
            fragment.appendln(" * ");
            for (int i = 0; i < annotations.length; i++) {
                fragment.append(" * @");
                fragment.appendln(annotations[i]);
            }
        }
        fragment.appendln(" */");
    }

    /**
     * Writes the annotation and a line separator. For an annotation only the annotation name needs to be specified. The
     * '@' character will be automatically added.
     * 
     * @param annotation
     */
    public void annotationLn(Class<?> annotation) {
        if (annotation == null) {
            return;
        }
        fragment.append("@");
        fragment.appendClassName(annotation);
        fragment.appendln();
    }

    /**
     * Writes the annotation with the indicated parameters and a line separator. '@' character and a line feed will be automatically added.
     * Import statements are added automatically to the code fragment (if needed).  
     * 
     * @param annotation The annotation class
     * @param params     Parameters for the annotation without paranthesis. If <code>null</code> or an empty String,
     *                   paranthesis aren't added.
     */
    public void annotationLn(Class<?> annotation, String params) {
        if (annotation==null) {
            return;
        }
        annotationLn(annotation.getName(), params);
    }
    
    /**
     * Writes the annotation with the indicated parameter of type String. '@' character and a line feed will be automatically added.
     * Import statements are added automatically to the code fragment (if needed).  
     * <pre>
     * Example 
     *   annotation : javax.xml.bind.annotation.XmlRootElement
     *   paramName  : name
     *   stringValue: policy
     *   Result: @XmlElement(name="Policy")
     * </pre>
     * 
     * @param annotation The annotation class
     * @param paramName  The name of the parameter
     * @param stringValue The unqoted string value for the parameter. This method generates the quotes.
     * 
     */
    public void annotationLn(Class<?> annotation, String paramName, String stringValue) {
        if (annotation == null) {
            return;
        }
        annotationLn(annotation.getName(), paramName, stringValue);
    }

    /**
     * Writes the annotation with the indicated parameters. '@' character and a line feed will be automatically added.
     * Import statements are added automatically to the code fragment (if needed).  
     * 
     * @param annotation The annotation class
     * @param params     Parameters for the annotation without paranthesis. If <code>null</code> or an empty String,
     *                   paranthesis aren't added.
     */
    public void annotationLn(String annotation, String params) {
        if (annotation == null) {
            return;
        }
        fragment.append("@");
        fragment.appendClassName(annotation);
        if (params!=null && params.length()>0) {
            fragment.append('(');
            fragment.append(params);
            fragment.append(')');
        }
        fragment.appendln();
    }

    /**
     * Writes the annotation with the indicated parameter of type String. '@' character and a line feed will be automatically added.
     * Import statements are added automatically to the code fragment (if needed).  
     * <pre>
     * Example 
     *   annotation : javax.xml.bind.annotation.XmlRootElement
     *   paramName  : name
     *   stringValue: policy
     *   Result: @XmlElement(name="Policy")
     * </pre>
     * 
     * @param annotation The annotation class
     * @param paramName  The name of the parameter
     * @param stringValue The unqoted string value for the parameter. This method generates the quotes.
     * 
     */
    public void annotationLn(String annotation, String paramName, String stringValue) {
        if (annotation == null) {
            return;
        }
        fragment.append("@");
        fragment.appendClassName(annotation);
        fragment.append('(');
        fragment.append(paramName);
        fragment.append('=');
        fragment.appendQuoted(stringValue);
        fragment.append(')');
        fragment.appendln();
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
    public void annotationLn(String annotation, String paramName, Class<?> classValue) {
        if (annotation == null) {
            return;
        }
        fragment.append("@");
        fragment.appendClassName(annotation);
        fragment.append('(');
        fragment.append(paramName);
        fragment.append('=');
        fragment.appendClassName(classValue);
        fragment.append(".class");
        fragment.append(')');
        fragment.appendln();
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
     * @param classValue The class value for the parameter
     */
    public void annotationClassValueLn(String annotation, String paramName, String qualifiedClassName) {
        if (annotation == null) {
            return;
        }
        fragment.append("@");
        fragment.appendClassName(annotation);
        fragment.append('(');
        fragment.append(paramName);
        fragment.append('=');
        fragment.appendClassName(qualifiedClassName);
        fragment.append(".class");
        fragment.append(')');
        fragment.appendln();
    }

    /**
     * Writes the annotation. For an annotation the (fully qualified) annotation class name needs to be specified. The
     * '@' character and a line feed will be automatically added. The annoation may contain parameters in paranthesis. 
     * 
     * @param annotation The fully qualified annotation name
     */
    public void annotationLn(String annotation) {
        if (annotation == null) {
            return;
        }
        fragment.append("@");
        int index = annotation.indexOf('(');
        if (index==-1) {
            fragment.appendClassName(annotation);
            fragment.appendln();
            return;
        }
        fragment.appendClassName(annotation.substring(0, index));
        fragment.appendln(annotation.substring(index));
    }

    /**
     * Writes the annotations. For an annotation only the annotation name needs to be specified. The
     * '@' character and a line feed will be automatically added. 
     * 
     * @param annotations
     */
    public void annotationLn(Class<?>[] annotations) {
        if (annotations == null) {
            return;
        }
        for (int i = 0; i < annotations.length; i++) {
            annotationLn(annotations[i]);
        }
    }

    /**
     * Writes the annotations. For an annotation only the annotation name needs to be specified. The
     * '@' character and a line feed will be automatically added. 
     * 
     * @param annotations
     */
    public void annotation(String[] annotations) {
        if (annotations == null) {
            return;
        }
        for (int i = 0; i < annotations.length; i++) {
            annotationLn(annotations[i]);
        }
    }

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
                throw new RuntimeException("Named and Class array must have the same length");
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
        public ClassAsParameterTypeSupport(String[] parameterNames, Class<?>[] parameterTypes, Class<?>[] exceptionClasses,
                Class<?> returnType) {
            check(parameterNames, parameterTypes);
            this.parameterNames = parameterNames;
            this.parameterTypes = parameterTypes;
            this.exceptionClasses = exceptionClasses;
            this.returnType = returnType;
        }

        public void appendParameterName(int index) {
            append(parameterNames[index]);
        }

        public void appendParameterType(int index) {
            appendClassName(parameterTypes[index]);
        }

        public void appendReturnType() {
            appendClassName(returnType);
        }

        public int getNumberOfParameters() {
            return parameterNames == null ? 0 : parameterNames.length;
        }

        public boolean hasReturnType() {
            return returnType != null;
        }

        public void appendExceptionExtension(int index) {
            appendClassName(exceptionClasses[index]);
        }

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

        public void appendParameterName(int index) {
            append(parameterNames[index]);
        }

        public void appendParameterType(int index) {
            appendClassName(parameterTypes[index]);
        }

        public void appendReturnType() {
            appendClassName(returnType);
        }

        public int getNumberOfParameters() {
            return parameterNames == null ? 0 : parameterNames.length;
        }

        public boolean hasReturnType() {
            return returnType != null;
        }

        public void appendExceptionExtension(int index) {
            appendClassName(exceptionClasses[index]);
        }

        public int getNumberOfExceptionExtensions() {
            return exceptionClasses == null ? 0 : exceptionClasses.length;
        }
    }
}
