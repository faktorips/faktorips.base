/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.JavaGeneratorHelper;
import org.junit.Test;

public class GenProductCmptTypeTest extends ProductCmptTypeBuilderTest {

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceConfiguring() throws CoreException {
        genProductCmptType.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceConfiguredPolicy, productCmptType);
        IType javaInterfacePolicy = genProductCmptType.findGeneratedJavaTypeForPolicyCmptType(true);
        expectGetProductCmptMethod(javaInterfacePolicy);
        expectGetProductCmptGenMethod(javaInterfacePolicy);
        expectSetProductCmptMethod(javaInterfacePolicy);
        assertEquals(3, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationConfiguring() throws CoreException {
        genProductCmptType.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassConfiguredPolicy,
                productCmptType);
        IType javaClassPolicy = genProductCmptType.findGeneratedJavaTypeForPolicyCmptType(false);
        expectGetProductCmptMethod(javaClassPolicy);
        expectGetProductCmptGenMethod(javaClassPolicy);
        expectSetProductCmptMethod(javaClassPolicy);
        assertEquals(3, generatedJavaElements.size());
    }

    private void expectGetProductCmptMethod(IType javaType) {
        expectMethod(javaType, genProductCmptType.getMethodNameGetProductCmpt());
    }

    private void expectGetProductCmptGenMethod(IType javaType) {
        expectMethod(javaType, genProductCmptType.getMethodNameGetProductCmptGeneration());
    }

    private void expectSetProductCmptMethod(IType javaType) {
        expectMethod(javaType, genProductCmptType.getMethodNameSetProductCmpt(), unresolvedParam(JavaGeneratorHelper
                .getJavaNamingConvention().getPublishedInterfaceName(genProductCmptType.getType().getName())),
                booleanParam());
    }

}
