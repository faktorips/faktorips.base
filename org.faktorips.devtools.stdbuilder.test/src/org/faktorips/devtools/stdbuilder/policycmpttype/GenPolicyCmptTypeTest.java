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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

public class GenPolicyCmptTypeTest extends PolicyCmptTypeBuilderTest {

    public void testGetGeneratedJavaElementsForPublishedInterfaceConfigured() throws CoreException {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPolicyCmptType.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(true), policyCmptType);
        expectCreatePolicyCmptMethod(generatedJavaElements, genPolicyCmptType
                .findGeneratedJavaTypeForProductCmptType(true));
        assertEquals(1, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationConfigured() throws CoreException {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPolicyCmptType.getGeneratedJavaElementsForImplementation(generatedJavaElements, getGeneratedJavaType(false),
                policyCmptType);
        expectCreatePolicyCmptMethod(generatedJavaElements, genPolicyCmptType
                .findGeneratedJavaTypeForProductCmptType(false));
        assertEquals(1, generatedJavaElements.size());
    }

    private void expectCreatePolicyCmptMethod(List<IJavaElement> javaElements, IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genPolicyCmptType.getMethodNameCreatePolicyCmpt(), new String[] {});
        assertTrue(javaElements.contains(expectedMethod));
    }

}
