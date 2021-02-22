/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.junit.Before;
import org.junit.Test;

public class AttributeDecoratorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("AdapterTestProject");
    }

    @Test
    public void testGetImageDescriptorOverwrittenAttribute() throws CoreException {
        IProductCmptType aSuperType = newProductCmptType(ipsProject, "ASuperType");
        IProductCmptType bNormalType = newProductCmptType(ipsProject, "BNormalType");
        bNormalType.setSupertype(aSuperType.getQualifiedName());

        IProductCmptProperty superAttribute = aSuperType.newProductCmptTypeAttribute("overwrittenAttribute");
        IProductCmptTypeAttribute normalAttribute = bNormalType.newProductCmptTypeAttribute("overwrittenAttribute");
        normalAttribute.setOverwrite(true);

        IIpsElementDecorator decorator = IIpsDecorators.get(superAttribute.getClass());
        assertNotNull(decorator);
        assertTrue(decorator instanceof AttributeDecorator);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(superAttribute);
        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithOverlays(false, false, false).equals(imageDescriptor));

        imageDescriptor = decorator.getImageDescriptor(normalAttribute);
        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithOverlays(true, false, false).equals(imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_staticOverlayForStaticProductAttribute() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "TestProductType");
        IProductCmptTypeAttribute productCmptTypeAttribute = productCmptType
                .newProductCmptTypeAttribute("testAttribute");
        productCmptTypeAttribute.setChangingOverTime(false);

        IIpsElementDecorator decorator = IIpsDecorators.get(productCmptTypeAttribute.getClass());
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(productCmptTypeAttribute);

        assertEquals(createImageDescriptorWithOverlays(false, true, false), imageDescriptor);
    }

    @Test
    public void testGetImageDescriptor_staticOverlayForStaticPolicyAttribute_productRelevant() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestPolicyType");
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType("TestProductType");
        newProductCmpt(ipsProject, "TestProductType");

        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute("testAttribute");
        policyCmptTypeAttribute.setValueSetConfiguredByProduct(true);
        policyCmptTypeAttribute.setChangingOverTime(false);

        IIpsElementDecorator decorator = IIpsDecorators.get(policyCmptTypeAttribute.getClass());
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(policyCmptTypeAttribute);

        assertEquals(createImageDescriptorWithOverlays(false, true, true), imageDescriptor);
    }

    @Test
    public void testGetImageDescriptor_staticOverlayForStaticPolicyAttribute_notProductRelevant() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestPolicyType");
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute("testAttribute");
        policyCmptTypeAttribute.setChangingOverTime(false);

        IIpsElementDecorator decorator = IIpsDecorators.get(policyCmptTypeAttribute.getClass());
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(policyCmptTypeAttribute);

        assertEquals(createImageDescriptorWithOverlays(false, false, false), imageDescriptor);
    }

    private ImageDescriptor createImageDescriptorWithOverlays(boolean override,
            boolean nonChangingOverTime,
            boolean productRelevant) {
        String baseImage = AttributeDecorator.PUBLISHED_BASE_IMAGE;
        String[] overlays = new String[4];
        if (nonChangingOverTime) {
            overlays[0] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
        }
        if (productRelevant) {
            overlays[1] = OverlayIcons.PRODUCT_OVR;
        }
        if (override) {
            overlays[3] = OverlayIcons.OVERRIDE_OVR;
        }
        return IpsDecorators.getImageHandling().getSharedOverlayImage(baseImage, overlays);
    }
}
