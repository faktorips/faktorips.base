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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.JavaGeneratiorHelper;

public class GenProductCmptTypeTest extends ProductCmptTypeBuilderTest {

    public void testGetGeneratedJavaElementsForPublishedInterfaceConfiguring() throws CoreException {
        genProductCmptType.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceConfiguredPolicy, productCmptType);
        IType javaInterfacePolicy = genProductCmptType.findGeneratedJavaTypeForPolicyCmptType(true);
        expectGetProductCmptMethod(javaInterfacePolicy);
        expectGetProductCmptGenMethod(javaInterfacePolicy);
        expectSetProductCmptMethod(javaInterfacePolicy);
        assertEquals(3, generatedJavaElements.size());
    }

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
        String methodName = genProductCmptType.getMethodNameGetProductCmpt();
        IMethod expectedMethod = javaType.getMethod(methodName, new String[0]);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectGetProductCmptGenMethod(IType javaType) {
        String methodName = genProductCmptType.getMethodNameGetProductCmptGeneration();
        IMethod expectedMethod = javaType.getMethod(methodName, new String[0]);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectSetProductCmptMethod(IType javaType) {
        String methodName = genProductCmptType.getMethodNameSetProductCmpt();
        String[] parameterTypeSignatures = new String[] {
                "Q"
                        + JavaGeneratiorHelper.getJavaNamingConvention().getPublishedInterfaceName(
                                genProductCmptType.getType().getName()) + ";", "Z" };
        IMethod expectedMethod = javaType.getMethod(methodName, parameterTypeSignatures);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

}
