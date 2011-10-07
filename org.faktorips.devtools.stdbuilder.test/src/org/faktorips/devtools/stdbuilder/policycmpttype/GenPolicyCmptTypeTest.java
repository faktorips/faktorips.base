/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.junit.Test;

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

    @Test
    public void testname() throws Exception {
        PolicyCmptType policyCmptType2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "test2");
        IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)policyCmptType2.newAttribute();
        genPolicyCmptType.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                policyCmptType);
        assertNull(genPolicyCmptType.getGenerator(attribute));
    }

    private void expectCreatePolicyCmptMethod(IType javaType) {
        expectMethod(javaType, genPolicyCmptType.getMethodNameCreatePolicyCmpt());
    }

}
