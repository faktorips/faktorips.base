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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.util.LocalizedStringsSet;

public class GenProductCmptType extends GenType {

    public GenProductCmptType(IType type, StandardBuilderSet builderSet, LocalizedStringsSet stringsSet) throws CoreException {
        super(type, builderSet, stringsSet);
    }
    
    public IProductCmptType getProductCmptType(){
        return (IProductCmptType)getType();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(boolean forInterface) throws CoreException {
        if(forInterface){
            String name = getType().getName() + getAbbreviationForGenerationConcept();
            return getJavaNamingConvention().getPublishedInterfaceName(name);
        }
        String generationAbb = getAbbreviationForGenerationConcept();
        return getJavaNamingConvention().getImplementationClassName(getType().getName() + generationAbb);
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorProductGen getMotorProductGen();
     * </pre>
     */
    public void generateMethodGetProductCmptGeneration(IIpsProject ipsProject, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        IPolicyCmptType pcType = getProductCmptType().findPolicyCmptType(ipsProject);
        String[] replacements = new String[]{getNameForGenerationConcept(), getType().getName(), pcType != null ? pcType.getName() : "missing"};
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT_GENERATION", replacements, methodsBuilder);
        generateSignatureGetProductCmptGeneration(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Returns the name of the method to access the product component generation, e.g. getMotorProductGen
     */
    //TODO duplicate method in PolicyCmptInterfaceBuilder
    public String getMethodNameGetProductCmptGeneration() throws CoreException {
        String[] replacements = new String[]{getType().getName(), getAbbreviationForGenerationConcept(), getNameForGenerationConcept()};
        return getLocalizedText("METHOD_GET_PRODUCTCMPT_GENERATION_NAME", replacements);
    }

    /**
     * Code sample:
     * <pre>
     * public IMotorProductGen getMotorProductGen()
     * </pre>
     */
    public void generateSignatureGetProductCmptGeneration(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetProductCmptGeneration();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, getQualifiedName(true), methodName, 
                new String[0], new String[0]);
    }

}
