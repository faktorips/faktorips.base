/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsSrcFileContentTest extends AbstractIpsPluginTest {

    @Test
    public void testGetRootPropertyValue() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestFile");
        IIpsSrcFile ipsSrcFile = policyCmptType.getIpsSrcFile();

        policyCmptType.setSupertype(null);
        String nullPropertyValue = ipsSrcFile.getPropertyValue(PolicyCmptType.PROPERTY_SUPERTYPE);
        assertThat(nullPropertyValue, is(nullValue()));

        policyCmptType.setSupertype("");
        String emptyPropertyValue = ipsSrcFile.getPropertyValue(PolicyCmptType.PROPERTY_SUPERTYPE);
        assertThat(emptyPropertyValue, is(""));

        policyCmptType.setSupertype("supertype");
        String stringPropertyValue = ipsSrcFile.getPropertyValue(PolicyCmptType.PROPERTY_SUPERTYPE);
        assertThat(stringPropertyValue, is("supertype"));
    }

}
