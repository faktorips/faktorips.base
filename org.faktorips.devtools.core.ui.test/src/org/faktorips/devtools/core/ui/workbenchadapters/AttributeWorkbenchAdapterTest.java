/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.junit.Before;
import org.junit.Test;

public class AttributeWorkbenchAdapterTest extends AbstractIpsPluginTest {

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

        IWorkbenchAdapter superAdapter = (IWorkbenchAdapter)superAttribute.getAdapter(IWorkbenchAdapter.class);
        assertNotNull(superAdapter);
        assertTrue(superAdapter instanceof AttributeWorkbenchAdapter);

        ImageDescriptor imageDescriptor = superAdapter.getImageDescriptor(superAttribute);
        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithOverlays(false, false, false).equals(imageDescriptor));

        imageDescriptor = superAdapter.getImageDescriptor(normalAttribute);
        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithOverlays(true, false, false).equals(imageDescriptor));
    }

    @Test
    public void testGetImageDescriptor_staticOverlayForStaticProductAttribute() throws CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "TestProductType");
        IProductCmptTypeAttribute productCmptTypeAttribute = productCmptType
                .newProductCmptTypeAttribute("testAttribute");
        productCmptTypeAttribute.setChangingOverTime(false);

        IWorkbenchAdapter adapter = (IWorkbenchAdapter)productCmptTypeAttribute.getAdapter(IWorkbenchAdapter.class);
        ImageDescriptor imageDescriptor = adapter.getImageDescriptor(productCmptTypeAttribute);
        assertEquals(createImageDescriptorWithOverlays(false, true, false), imageDescriptor);
    }

    @Test
    public void testGetImageDescriptor_staticOverlayForStaticPolicyAttribute_productRelevant() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestPolicyType");
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType("TestProductType");
        newProductCmpt(ipsProject, "TestProductType");

        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute("testAttribute");
        policyCmptTypeAttribute.setProductRelevant(true);
        policyCmptTypeAttribute.setChangingOverTime(false);

        IWorkbenchAdapter adapter = (IWorkbenchAdapter)policyCmptTypeAttribute.getAdapter(IWorkbenchAdapter.class);
        ImageDescriptor imageDescriptor = adapter.getImageDescriptor(policyCmptTypeAttribute);
        assertEquals(createImageDescriptorWithOverlays(false, true, true), imageDescriptor);
    }

    @Test
    public void testGetImageDescriptor_staticOverlayForStaticPolicyAttribute_notProductRelevant() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "TestPolicyType");
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute("testAttribute");
        policyCmptTypeAttribute.setChangingOverTime(false);

        IWorkbenchAdapter adapter = (IWorkbenchAdapter)policyCmptTypeAttribute.getAdapter(IWorkbenchAdapter.class);
        ImageDescriptor imageDescriptor = adapter.getImageDescriptor(policyCmptTypeAttribute);
        assertEquals(createImageDescriptorWithOverlays(false, false, false), imageDescriptor);
    }

    private ImageDescriptor createImageDescriptorWithOverlays(boolean override,
            boolean nonChangingOverTime,
            boolean productRelevant) {
        String baseImage = AttributeWorkbenchAdapter.PUBLISHED_BASE_IMAGE;
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
        return IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseImage, overlays);
    }
}
