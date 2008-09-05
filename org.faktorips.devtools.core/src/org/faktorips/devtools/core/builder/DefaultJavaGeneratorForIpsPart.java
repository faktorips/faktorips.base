/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.LocalizedStringsSet;

/**
 * This subclass of <code>JavaGeneratorForIpsPart</code> works together with
 * <code>DefaultJavaSourceFileBuilder</code>.
 * 
 * @see DefaultJavaSourceFileBuilder
 * @author Jan Ortmann
 */
public abstract class DefaultJavaGeneratorForIpsPart extends JavaGeneratorForIpsPart {

    protected static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    public DefaultJavaGeneratorForIpsPart(
            IIpsObjectPartContainer part, 
            LocalizedStringsSet localizedStringsSet) throws CoreException {
        super(part, localizedStringsSet);
    }
    
    /**
     * Generates the source code for the ips object part this is a generator for.
     * 
     * @param generatesInterface TODO
     */
    public void generate(boolean generatesInterface, IIpsProject ipsProject, TypeSection mainSection) throws CoreException {
        generateConstants(mainSection.getConstantBuilder(), ipsProject, generatesInterface);
        if (!generatesInterface) {
            generateMemberVariables(mainSection.getMemberVarBuilder(), ipsProject, generatesInterface);
        }
        generateMethods(mainSection.getMethodBuilder(), ipsProject, generatesInterface);
    }
    
    /**
     * Subclasses have to implement generation of the methods here. Whether the generator is used
     * for generating the implementation class or the interface can be queried via
     * {@link #isGeneratingImplementationClass()} and {@link #isGeneratingInterface()}.
     * 
     * @param builder The builder for the type's method section.
     * @param generatesInterface TODO
     * @throws CoreException if an error occurs while generating the member variables
     * @see #isGeneratingImplementationClass()
     * @see #isGeneratingInterface()
     */
    protected abstract void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface) throws CoreException;

    /**
     * Subclasses have to implement generation of the member variables here. This method is only
     * called if the generator is generating an implementation class.
     * 
     * @param builder The builder for the type's member variables section.
     * @param generatesInterface TODO
     * @throws CoreException if an error occurs while generating the member variables
     * @see #isGeneratingImplementationClass()
     */
    protected abstract void generateMemberVariables(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface) throws CoreException;
    
    /**
     * Subclasses have to implement generation of the constants (final statics) here. Whether the
     * generator is used for generating the implementation class or the interface can be queried via
     * {@link #isGeneratingImplementationClass()} and {@link #isGeneratingInterface()}.
     * 
     * @param builder The builder for the type's constants section.
     * @param generatesInterface TODO
     * @throws CoreException if an error occurs while generating the member variables
     * @see #isGeneratingImplementationClass()
     * @see #isGeneratingInterface()
     */
    protected abstract void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface) throws CoreException;
}