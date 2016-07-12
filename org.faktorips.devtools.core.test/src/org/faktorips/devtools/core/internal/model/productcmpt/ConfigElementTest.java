/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests methods that are implemented in ConfigElement that are not supposed to be overridden in
 * subclasses. {@link ConfiguredDefault} is used as an example subclass.
 */
public class ConfigElementTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IConfiguredDefault configuredDefault;
    private IPolicyCmptTypeAttribute attribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("attribute");

        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        configuredDefault = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        productCmpt.getIpsSrcFile().save(true, null);
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
    }

    @Test
    public void testFindPcTypeAttribute() throws CoreException {
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        policyCmptType.setSupertype(policyCmptSupertype.getQualifiedName());

        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("a1");
        IPolicyCmptTypeAttribute a2 = policyCmptSupertype.newPolicyCmptTypeAttribute();
        a2.setName("a2");

        generation = productCmpt.getProductCmptGeneration(0);
        IConfiguredDefault defaultValue = generation.newPropertyValue(a1, IConfiguredDefault.class);
        assertEquals(a1, defaultValue.findPcTypeAttribute(ipsProject));
    }

    @Test
    public void testGetCaption() throws CoreException {
        ILabel label = attribute.getLabel(Locale.US);
        label.setValue("TheCaption");
        assertEquals("TheCaption", configuredDefault.getCaption(Locale.US));
    }

    @Test
    public void testGetCaptionNotExistent() throws CoreException {
        assertNull(configuredDefault.getCaption(Locale.TAIWAN));
    }

    @Test
    public void testGetCaptionNullPointer() throws CoreException {
        try {
            configuredDefault.getCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLastResortCaption() {
        assertEquals(StringUtils.capitalize(configuredDefault.getPolicyCmptTypeAttribute()),
                configuredDefault.getLastResortCaption());
    }
}
