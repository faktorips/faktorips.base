/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
    public void appendQuoted(String s)
    {
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
     * neccessary).
     */
    public void appendClassName(Class clazz) {
        fragment.appendClassName(clazz);
    }

    /**
     * Appends the unqualified class name to the sourcecode and updates the import declaration (if
     * neccessary).
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
     * @param returnType the className that the methods returnds an instance of or null to indicate
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
     * @param modifier      Access modifier according to java.lang.reflect.Modifier.
     * @param returnType    The className that the methods returns an instance of 
     *                      or <code>null</code> to indicate no return type in case of a constructor.
     *                      The return type <code>void</code> is indictaed by <code>java.lang.Void.class</code>: 
     * @param methodName
     * @param argName Argument names.
     * @param argClass Argument classes.
     */
    public void methodBegin(int modifier,
            Class returnType,
            String methodName,
            String[] argName,
            Class[] argClass) {

        String[] argClassNames = toStringArray(argClass);
        String returnTypeName = null;
        if (returnType != null) {
            if (returnType == Void.class) {
                returnTypeName = "void";
            } else {
                returnTypeName = returnType.getName();
            }
        }
        methodBegin(modifier, returnTypeName, methodName, argName, argClassNames);
    }

    /**
     * Creates the Java source code for a method including signature, body and java doc.
     * 
     * @param modifier Access modifier according to java.lang.reflect.Modifier.
     * @param returnType the className that the methods returnds an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName the name of the method.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param body the method body
     * @param javadoc the java documentation
     */
    public void method(int modifier,
            Class returnType,
            String methodName,
            String[] argName,
            Class[] argClass,
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
     * @param returnType the className that the methods returnds an instance of or null to indicate
     *            no return type in case of a constructor.
     * @param methodName the name of the method.
     * @param argName Argument names.
     * @param argClass Argument classes.
     * @param body the method body
     * @param javadoc the java documentation
     * @param javaDocAnnotations annotations of the java documentation
     */
    public void method(int modifier,
            Class returnType,
            String methodName,
            String[] argName,
            Class[] argClass,
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
     * @param returnType the className that the methods returnds an instance of or null to indicate
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

    private String[] toStringArray(Class[] classes) {
        if (classes == null) {
            return null;
        }
        String[] classNames = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            classNames[i] = classes[i].getName();
        }
        return classNames;
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
     */
    public void methodBegin(int modifier,
            Class returnType,
            String methodName,
            String[] argName,
            Class[] argClass,
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
            Class returnType,
            String methodName,
            String[] argName,
            Class[] argClass,
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
    public void methodBegin(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass) {
        signature(modifier, returnType, methodName, argName, argClass);
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
    public void signature(int modifier,
            String returnType,
            String methodName,
            String[] argName,
            String[] argClass) {
        if (argName != null && argName.length != argClass.length) {
            throw new RuntimeException("Named and Class array must have the same length. Names:"
                    + argName + " Classes:" + argClass);
        }
        lastMethodModifier = modifier;
        append(Modifier.toString(modifier));
        append(' ');
        if (returnType != null) {
            int bracketIndex = returnType.indexOf("[]");
            if (bracketIndex != -1) {
                appendClassName(returnType.substring(0, bracketIndex));
                append("[]");
                append(' ');
            } else {
                appendClassName(returnType);
                append(' ');
            }
        }
        append(methodName);
        append('(');
        if (argName != null) {
            for (int i = 0; i < argName.length; i++) {
                if (i > 0) {
                    append(", ");
                }
                appendClassName(argClass[i]);
                append(' ');
                append(argName[i]);
            }
        }
        append(')');
       
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
    public void classBegin(int modifier, String className, Class extendsClass, Class interfaces[]) {
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
    public void classBegin(int modifier,
            String className,
            String extendsClassName,
            String interfaces[]) {

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
        if (extendedInterfaces != null && extendedInterfaces.length>0) {
            fragment.append(" extends ");
            for (int i = 0; i < extendedInterfaces.length; i++) {
				if (i>0) {
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
    public void varDeclaration(int modifier, Class clazz, String varName) {

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
    public void varDeclaration(int modifier,
            Class clazz,
            String varName,
            JavaCodeFragment expression) {

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
    public void varDeclaration(int modifier,
            String className,
            String varName,
            JavaCodeFragment expression) {
        if (modifier > 0) {
            fragment.append(Modifier.toString(modifier));
            fragment.append(' ');
        }
        if (className != null) {
            int bracketIndex = className.indexOf("[]");
            if (bracketIndex != -1) {
                appendClassName(className.substring(0, bracketIndex));
                append("[]");
                append(' ');
            } else {
                appendClassName(className);
                append(' ');
            }
        }
        fragment.append(' ');
        fragment.append(varName);
        if (expression != null) {
            fragment.append(" = ");
            fragment.append(expression);
        }
        fragment.append(";");
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
    public void varDefinition(Class varClass, String varName, String varValue) {
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
        fragment.appendln("/**");
        if(text != null){
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
    
    public String toString() {
    	return fragment.toString();
    }
}
