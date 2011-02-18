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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.junit.Test;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class GenPolicyCmptTypeTest extends PolicyCmptTypeBuilderTest {

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceConfigured() throws CoreException {
        genPolicyCmptType.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                policyCmptType);
        expectCreatePolicyCmptMethod(genPolicyCmptType.findGeneratedJavaTypeForProductCmptType(true));
        assertEquals(1, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationConfigured() throws CoreException {
        genPolicyCmptType.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, policyCmptType);
        expectCreatePolicyCmptMethod(genPolicyCmptType.findGeneratedJavaTypeForProductCmptType(false));
        assertEquals(1, generatedJavaElements.size());
    }

    private void expectCreatePolicyCmptMethod(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genPolicyCmptType.getMethodNameCreatePolicyCmpt(), new String[] {});
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

}
