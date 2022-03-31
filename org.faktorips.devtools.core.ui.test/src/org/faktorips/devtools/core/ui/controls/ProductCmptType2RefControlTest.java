/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class ProductCmptType2RefControlTest extends AbstractIpsPluginTest {

    @Test
    public void testGetSrcFiles_SingleProject() {
        IIpsProject project = newIpsProject("BaseProject");

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");
        IProductCmptType secondProductCmptType = newProductCmptType(project, "SecondProductType");
        secondProductCmptType.setAbstract(true);

        IpsObjectRefControl productCmptRefControl = new ProductCmptType2RefControl(project, new Shell(), new UIToolkit(
                null), false);

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productCmptType.getIpsSrcFile()));
        assertTrue(list.contains(secondProductCmptType.getIpsSrcFile()));

        assertEquals(2, list.size());
    }

    @Test
    public void testGetSrcFiles_ExcludeAbstract() {
        IIpsProject project = newIpsProject("BaseProject");

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");
        productCmptType.setAbstract(false);
        IProductCmptType secondProductCmptType = newProductCmptType(project, "SecondProductType");
        secondProductCmptType.setAbstract(true);

        IpsObjectRefControl productCmptRefControl = new ProductCmptType2RefControl(project, new Shell(), new UIToolkit(
                null), true);

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productCmptType.getIpsSrcFile()));
        assertFalse(list.contains(secondProductCmptType.getIpsSrcFile()));

        assertEquals(1, list.size());
    }

    @Test
    public void testGetSrcFiles_MultiProject() {
        IIpsProject project = newIpsProject("BaseProject");
        IIpsProject subProject = newIpsProject("SubProject");
        IIpsProject anotherProject = newIpsProject("AnotherProject");

        IIpsObjectPath subIpsObjectPath = subProject.getIpsObjectPath();
        subIpsObjectPath.newIpsProjectRefEntry(project);
        subProject.setIpsObjectPath(subIpsObjectPath);

        IIpsObjectPath anotherIpsObjectPath = anotherProject.getIpsObjectPath();
        anotherIpsObjectPath.newIpsProjectRefEntry(project);
        anotherProject.setIpsObjectPath(anotherIpsObjectPath);

        List<IIpsProject> projects = Arrays.asList(project, subProject);

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");

        IProductCmptType subProductCmptType = newProductCmptType(subProject, "SubProductType");

        IProductCmptType anotherProductCmptType = newProductCmptType(anotherProject, "AnotherProduct");

        IpsObjectRefControl productCmptRefControl = new ProductCmptType2RefControl(projects, new Shell(),
                new UIToolkit(null), false);

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productCmptType.getIpsSrcFile()));
        assertTrue(list.contains(subProductCmptType.getIpsSrcFile()));
        assertFalse(list.contains(anotherProductCmptType.getIpsSrcFile()));

        assertEquals(2, list.size());
    }
}
