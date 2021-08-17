/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.policycmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.junit.Test;

public class PolicyCmptDeclClassAnnGenTest {

    public PolicyCmptDeclClassAnnGen generator = new PolicyCmptDeclClassAnnGen();

    @Test
    public void test() {
        XPolicyCmptClass policy = mockPolicyCmptClass();

        assertEquals("@IpsPolicyCmptType(name = \"test.PolicyCmpt\")" + System.lineSeparator(), generator
                .createAnnotation(policy).getSourcecode());
    }

    @Test
    public void testConfigured() {
        XPolicyCmptClass policy = mockPolicyCmptClass();
        when(policy.isConfigured()).thenReturn(true);
        when(policy.getProductCmptClassName()).thenReturn("ProductCmptImplClass");

        assertEquals("@IpsPolicyCmptType(name = \"test.PolicyCmpt\")" + System.lineSeparator()
                + "@IpsConfiguredBy(ProductCmptImplClass.class)" + System.lineSeparator(),
                generator
                        .createAnnotation(policy).getSourcecode());
    }

    private XPolicyCmptClass mockPolicyCmptClass() {
        XPolicyCmptClass policy = mock(XPolicyCmptClass.class);
        when(policy.addImport(IpsPolicyCmptType.class)).thenReturn("IpsPolicyCmptType");

        IPolicyCmptType ipsObject = mock(IPolicyCmptType.class);
        when(ipsObject.getQualifiedName()).thenReturn("test.PolicyCmpt");
        when(policy.getIpsObjectPartContainer()).thenReturn(ipsObject);

        return policy;
    }
}
