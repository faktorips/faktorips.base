/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.businessfct;

import static org.faktorips.abstracttest.matcher.Matchers.hasMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class BusinessFunctionImplTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testValidateDeprecated() throws Exception {
        org.faktorips.devtools.model.businessfct.BusinessFunction bf = (org.faktorips.devtools.model.businessfct.BusinessFunction)newIpsObject(
                ipsProject,
                IpsObjectType.BUSINESS_FUNCTION,
                "bf");

        MessageList msgList = bf.validate(ipsProject);

        assertThat(msgList,
                hasMessageCode(org.faktorips.devtools.model.businessfct.BusinessFunction.MSGCODE_DEPRECATED));
    }

}
