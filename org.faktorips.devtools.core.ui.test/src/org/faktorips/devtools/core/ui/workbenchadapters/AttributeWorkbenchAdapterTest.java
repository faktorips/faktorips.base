/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.workbenchadapters;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.junit.Before;
import org.junit.Test;

public class AttributeWorkbenchAdapterTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot root;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("AdapterTestProject");
        root = newIpsPackageFragmentRoot(ipsProject, null, "root");
    }

    @Test
    public void testGetImageDescriptorOverwrittenAttribute() throws CoreException {
        IProductCmptType aSuperType = newProductCmptType(root, "ASuperType");
        IProductCmptType bNormalType = newProductCmptType(root, "BNormalType");
        bNormalType.setSupertype(aSuperType.getQualifiedName());

        IProductCmptProperty superAttribute = aSuperType.newProductCmptTypeAttribute("overwrittenAttribute");
        IProductCmptTypeAttribute normalAttribute = bNormalType.newProductCmptTypeAttribute("overwrittenAttribute");
        normalAttribute.setOverwrite(true);

        IWorkbenchAdapter superAdapter = (IWorkbenchAdapter)superAttribute.getAdapter(IWorkbenchAdapter.class);
        assertNotNull(superAdapter);
        assertTrue(superAdapter instanceof AttributeWorkbenchAdapter);

        ImageDescriptor imageDescriptor = superAdapter.getImageDescriptor(superAttribute);
        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithOverwrittenOverlay(false).equals(imageDescriptor));

        imageDescriptor = superAdapter.getImageDescriptor(normalAttribute);
        assertNotNull(imageDescriptor);
        assertTrue(createImageDescriptorWithOverwrittenOverlay(true).equals(imageDescriptor));
    }

    private ImageDescriptor createImageDescriptorWithOverwrittenOverlay(boolean isOverride) {
        String baseImage = AttributeWorkbenchAdapter.PUBLISHED_BASE_IMAGE;
        String[] overlays = new String[4];
        if (isOverride) {
            overlays[3] = OverlayIcons.OVERRIDE_OVR;
        }
        return IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseImage, overlays);
    }
}
