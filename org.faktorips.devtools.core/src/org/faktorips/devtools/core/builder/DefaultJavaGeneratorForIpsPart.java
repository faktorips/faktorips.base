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

import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * This subclass of <code>JavaGeneratorForIpsPart</code> works together with <code>DefaultJavaSourceFileBuilder</code>.
 * 
 * @see DefaultJavaSourceFileBuilder
 * 
 * @author Jan Ortmann
 */
public class DefaultJavaGeneratorForIpsPart extends JavaGeneratorForIpsPart {

    private DefaultJavaSourceFileBuilder javaSourceFileBuilder;
    
    public DefaultJavaGeneratorForIpsPart(IIpsObjectPartContainer part, DefaultJavaSourceFileBuilder builder) {
        super(part, builder);
        javaSourceFileBuilder = builder;
    }
    
    /**
     * Returns the {@link <code>JavaCodeFragmentBuilder</code>} that assembles the code for the constant definitions.
     */
    public JavaCodeFragmentBuilder getConstantBuilder() {
        return javaSourceFileBuilder.getMainTypeSection().getConstantBuilder();
    }

    /**
     * Returns the {@link <code>JavaCodeFragmentBuilder</code>} that assembles the code for the member variable definitions.
     */
    public JavaCodeFragmentBuilder getMemberVarBuilder() {
        return javaSourceFileBuilder.getMainTypeSection().getMemberVarBuilder();
    }

    /**
     * Returns the {@link <code>JavaCodeFragmentBuilder</code>} that assembles the code for the method definitions.
     */
    public JavaCodeFragmentBuilder getMethodBuilder() {
        return javaSourceFileBuilder.getMainTypeSection().getMethodBuilder();
    }    

}
