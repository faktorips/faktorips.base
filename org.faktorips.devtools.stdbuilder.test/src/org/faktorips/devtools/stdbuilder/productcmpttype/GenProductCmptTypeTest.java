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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class GenProductCmptTypeTest extends ProductCmptTypeBuilderTest {

    public void testGetGeneratedJavaElementsForPublishedInterface() {
        productCmptType.setConfigurationForPolicyCmptType(false);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genProductCmptType.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, getGeneratedJavaType(),
                productCmptType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaType()));
        assertEquals(1, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        productCmptType.setConfigurationForPolicyCmptType(false);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genProductCmptType.getGeneratedJavaElementsForImplementation(generatedJavaElements, getGeneratedJavaType(),
                productCmptType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaType()));
        assertEquals(1, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceConfiguring() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genProductCmptType.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, getGeneratedJavaType(),
                productCmptType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaType()));
        expectGetProductCmptMethod(generatedJavaElements, genProductCmptType
                .getGeneratedJavaTypeForPolicyCmptType(true));
        expectGetProductCmptGenMethod(generatedJavaElements, genProductCmptType
                .getGeneratedJavaTypeForPolicyCmptType(true));
        expectSetProductCmptMethod(generatedJavaElements, genProductCmptType
                .getGeneratedJavaTypeForPolicyCmptType(true));
        assertEquals(4, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationConfiguring() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genProductCmptType.getGeneratedJavaElementsForImplementation(generatedJavaElements, getGeneratedJavaType(),
                productCmptType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaType()));
        expectGetProductCmptMethod(generatedJavaElements, genProductCmptType
                .getGeneratedJavaTypeForPolicyCmptType(false));
        expectGetProductCmptGenMethod(generatedJavaElements, genProductCmptType
                .getGeneratedJavaTypeForPolicyCmptType(false));
        expectSetProductCmptMethod(generatedJavaElements, genProductCmptType
                .getGeneratedJavaTypeForPolicyCmptType(false));
        assertEquals(4, generatedJavaElements.size());
    }

    private void expectGetProductCmptMethod(List<IJavaElement> javaElements, IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genProductCmptType.getMethodNameGetProductCmpt(), new String[] {});
        assertTrue(javaElements.contains(expectedMethod));
    }

    private void expectGetProductCmptGenMethod(List<IJavaElement> javaElements, IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genProductCmptType.getMethodNameGetProductCmptGeneration(),
                new String[] {});
        assertTrue(javaElements.contains(expectedMethod));
    }

    private void expectSetProductCmptMethod(List<IJavaElement> javaElements, IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genProductCmptType.getMethodNameSetProductCmpt(), new String[] {
                "Q" + genProductCmptType.getQualifiedName(true) + ";", "Z" });
        assertTrue(javaElements.contains(expectedMethod));
    }

}
