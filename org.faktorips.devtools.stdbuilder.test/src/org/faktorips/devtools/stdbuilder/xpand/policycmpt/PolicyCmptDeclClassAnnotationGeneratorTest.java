/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.policycmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.junit.Test;

public class PolicyCmptDeclClassAnnotationGeneratorTest {

    public PolicyCmptDeclClassAnnotationGenerator generator = new PolicyCmptDeclClassAnnotationGenerator();

    @Test
    public void test() {
        XPolicyCmptClass policy = mock(XPolicyCmptClass.class);
        when(policy.addImport(IpsPolicyCmptType.class)).thenReturn("IpsPolicyCmptType");

        IPolicyCmptType ipsObject = mock(IPolicyCmptType.class);
        when(ipsObject.getQualifiedName()).thenReturn("test.PolicyCmpt");
        when(policy.getIpsObjectPartContainer()).thenReturn(ipsObject);

        assertEquals("@IpsPolicyCmptType(name = \"test.PolicyCmpt\")" + System.getProperty("line.separator"), generator
                .createAnnotation(policy).getSourcecode());

        when(policy.isConfigured()).thenReturn(true);
        when(policy.getProductCmptClassName()).thenReturn("ProductCmptImplClass");
        assertEquals("@IpsPolicyCmptType(name = \"test.PolicyCmpt\")" + System.getProperty("line.separator")
                + "@IpsConfiguredBy(value = ProductCmptImplClass.class)" + System.getProperty("line.separator"),
                generator.createAnnotation(policy).getSourcecode());
    }
}
