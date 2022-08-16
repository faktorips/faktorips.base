/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.faktorips.codegen.JavaCodeFragmentBuilder;

/**
 * A TypeSection assembles the code fragments for a type, e.g. the main class or an inner class,
 * during the generation process. Therefore it provides subsections for typical code sections like
 * for constants, attributes, methods, constructors and java doc for the type.
 * 
 * @author Peter Erzberger
 */
public final class TypeSection {

    private boolean isClass = true;
    private boolean isEnum = false;
    private int classModifier;
    private String[] extendedInterfaces;
    private String superClass;
    private String unqualifiedName;

    private JavaCodeFragmentBuilder constantBuilder = new JavaCodeFragmentBuilder();
    private JavaCodeFragmentBuilder javadocForTypeBuilder = new JavaCodeFragmentBuilder();
    private JavaCodeFragmentBuilder annotationsForTypeBuilder = new JavaCodeFragmentBuilder();
    private JavaCodeFragmentBuilder memberVariableBuilder = new JavaCodeFragmentBuilder();
    private JavaCodeFragmentBuilder constructorBuilder = new JavaCodeFragmentBuilder();
    private JavaCodeFragmentBuilder methodBuilder = new JavaCodeFragmentBuilder();
    private JavaCodeFragmentBuilder enumDefinitionBuilder = new JavaCodeFragmentBuilder();

    /**
     * Returns the class modifier for the type represented by this TypeSection.
     * 
     * @see Modifier
     */
    public int getClassModifier() {
        return classModifier;
    }

    /**
     * Sets the class modifier for the type represented by this TypeSection.
     * 
     * @see Modifier
     */
    public void setClassModifier(int classModifier) {
        this.classModifier = classModifier;
    }

    /**
     * Returns the qualified names of the interfaces that the generated class or interface extends.
     */
    public String[] getExtendedInterfaces() {
        return extendedInterfaces == null ? null : Arrays.copyOf(extendedInterfaces, extendedInterfaces.length);
    }

    /**
     * Sets the qualified names of the interfaces that the generated class or interface extends.
     */
    public void setExtendedInterfaces(String[] extendedInterfaces) {
        this.extendedInterfaces = Arrays.copyOf(extendedInterfaces, extendedInterfaces.length);
    }

    /**
     * Returns if the type that is to generate is a class or an interface.
     */
    public boolean isClass() {
        return isClass;
    }

    /**
     * Sets if the type that is to generate is a class or an interface.
     */
    public void setClass(boolean isClass) {
        this.isClass = isClass;
        if (this.isClass) {
            isEnum = false;
        }
    }

    /**
     * Returns if the type that is to generate is an enumeration or an interface.
     */
    public boolean isEnum() {
        return isEnum;
    }

    /**
     * Sets if the type that is to generate is an enumeration or an interface.
     */
    public void setEnum(boolean isEnum) {
        this.isEnum = isEnum;
        if (isEnum) {
            isClass = false;
        }
    }

    /**
     * Returns the qualified name of the super class that the generated class extends.
     * <code>null</code> indicates no superclass will be extended by the generated class.
     */
    public String getSuperClass() {
        return superClass;
    }

    /**
     * Sets the qualified name of the super class that the generated class extends.
     * <code>null</code> indicates no superclass will be extended by the generated class.
     */
    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    /**
     * Returns the unqualified name of the generated class or interface.
     */
    public String getUnqualifiedName() {
        return unqualifiedName;
    }

    /**
     * Sets the unqualified name of the generated class or interface.
     */
    public void setUnqualifiedName(String unqualifiedName) {
        this.unqualifiedName = unqualifiedName;
    }

    /**
     * Returns the {@link JavaCodeFragmentBuilder} that assembles the code for the constant
     * definitions.
     */
    public JavaCodeFragmentBuilder getConstantBuilder() {
        return constantBuilder;
    }

    /**
     * Returns the {@link JavaCodeFragmentBuilder} that assembles the code for the initialization of
     * the enumeration.
     */
    public JavaCodeFragmentBuilder getEnumDefinitionBuilder() {
        return enumDefinitionBuilder;
    }

    /**
     * Returns the {@link JavaCodeFragmentBuilder} that assembles the code for the constructor
     * definitions.
     */
    public JavaCodeFragmentBuilder getConstructorBuilder() {
        return constructorBuilder;
    }

    /**
     * Returns the {@link JavaCodeFragmentBuilder} that assembles the code for the member variable
     * definitions.
     */
    public JavaCodeFragmentBuilder getMemberVarBuilder() {
        return memberVariableBuilder;
    }

    /**
     * Returns the {@link JavaCodeFragmentBuilder} that assembles the code for the method
     * definitions.
     */
    public JavaCodeFragmentBuilder getMethodBuilder() {
        return methodBuilder;
    }

    /**
     * Returns the {@link JavaCodeFragmentBuilder} that assembles the code for the java doc of the
     * type of this TypeSection.
     */
    public JavaCodeFragmentBuilder getJavaDocForTypeBuilder() {
        return javadocForTypeBuilder;
    }

    /**
     * Returns the {@link JavaCodeFragmentBuilder} that assembles the annotations for the type of
     * this TypeSection.
     */
    public JavaCodeFragmentBuilder getAnnotationsForTypeBuilder() {
        return annotationsForTypeBuilder;
    }

}
