/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.LocalizedStringsSet;

/**
 * This subclass of <code>JavaGeneratorForIpsPart</code> works together with <code>DefaultJavaSourceFileBuilder</code>.
 * 
 * @see DefaultJavaSourceFileBuilder
 * 
 * @author Jan Ortmann
 */
public abstract class DefaultJavaGeneratorForIpsPart extends JavaGeneratorForIpsPart {

    
    private DefaultJavaSourceFileBuilder javaSourceFileBuilder;
    
    // true if the generation should generate part of the implementation class, false if it should generate part
    // of the interface.
    private boolean generateImplementation;
    
    public DefaultJavaGeneratorForIpsPart(
            IIpsObjectPartContainer part, 
            DefaultJavaSourceFileBuilder builder, 
            LocalizedStringsSet stringsSet,
            boolean generateImplementation) throws CoreException {
        super(part, builder, stringsSet);
        this.javaSourceFileBuilder = builder;
        this.generateImplementation = generateImplementation;
        init();
    }
    
    /**
     * Hook for subclasses to initialize member variables before the code generation starts.
     * 
     * @throws CoreException if an error occurs while initializing the generator
     */
    protected void init() throws CoreException {
        
    }
    
    /**
     * Returns <code>true</code> if the generator should generate part of the implementation class, 
     * <code>false</code> if it should generate part of the interface.
     */
    public boolean isGeneratingImplementationClass() {
        return generateImplementation;
    }
    
    /**
     * Returns <code>true</code> if the generator should generate part of the interface, 
     * <code>false</code> if it should generate part of the implementation class.
     */
    public boolean isGeneratingInterface() {
        return !generateImplementation;
    }
    
    /**
     * Generates the source code for the ips object part this is a generator for.
     */
    public void generate() throws CoreException {
        generateConstants(getConstantBuilder());
        if (isGeneratingImplementationClass()) {
            generateMemberVariables(getMemberVarBuilder());
        }
        generateMethods(getMethodBuilder());
    }
    
    /**
     * Subclasses have to implement generation of the methods here. Whether the generator is used for generating the
     * implementation class or the interface can be queried via {@link #isGeneratingImplementationClass()} and 
     * {@link #isGeneratingInterface()}.
     * 
     * @param builder The builder for the type's method section.
     * 
     * @throws CoreException if an error occurs while generating the member variables
     * 
     * @see #isGeneratingImplementationClass()
     * @see #isGeneratingInterface()
     */
    protected abstract void generateMethods(JavaCodeFragmentBuilder builder) throws CoreException;

    /**
     * Subclasses have to implement generation of the member variables here. This method is only called if the
     * generator is generating an implementation class.
     * 
     * @param builder The builder for the type's member variables section.
     * 
     * @throws CoreException if an error occurs while generating the member variables
     * 
     * @see #isGeneratingImplementationClass()
     */
    protected abstract void generateMemberVariables(JavaCodeFragmentBuilder builder) throws CoreException;
    
    /**
     * Subclasses have to implement generation of the constants (final statics) here. Whether the generator is used for generating the
     * implementation class or the interface can be queried via {@link #isGeneratingImplementationClass()} and 
     * {@link #isGeneratingInterface()}.
     * 
     * @param builder The builder for the type's constants section.
     * 
     * @throws CoreException if an error occurs while generating the member variables
     * 
     * @see #isGeneratingImplementationClass()
     * @see #isGeneratingInterface()
     */
    protected abstract void generateConstants(JavaCodeFragmentBuilder builder) throws CoreException;
    
    /**
     * Returns the {@link <code>JavaCodeFragmentBuilder</code>} that assembles the code for the constant definitions.
     */
    protected JavaCodeFragmentBuilder getConstantBuilder() {
        return javaSourceFileBuilder.getMainTypeSection().getConstantBuilder();
    }

    /**
     * Returns the {@link <code>JavaCodeFragmentBuilder</code>} that assembles the code for the member variable definitions.
     */
    protected JavaCodeFragmentBuilder getMemberVarBuilder() {
        return javaSourceFileBuilder.getMainTypeSection().getMemberVarBuilder();
    }

    /**
     * Returns the {@link <code>JavaCodeFragmentBuilder</code>} that assembles the code for the method definitions.
     */
    protected JavaCodeFragmentBuilder getMethodBuilder() {
        return javaSourceFileBuilder.getMainTypeSection().getMethodBuilder();
    }    

}
