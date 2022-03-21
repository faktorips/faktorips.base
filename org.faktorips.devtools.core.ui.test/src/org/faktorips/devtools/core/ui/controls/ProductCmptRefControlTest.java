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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class ProductCmptRefControlTest extends AbstractIpsPluginTest {

    @Test
    public void testGetSrcFiles_SingleProject() {
        IIpsProject project = newIpsProject("BaseProject");

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");

        ProductCmpt productCmpt = newProductCmpt(project, "Product");
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());
        productCmpt.newGeneration();

        ProductCmpt secondProductCmpt = newProductCmpt(project, "SecondProduct");
        secondProductCmpt.setProductCmptType(productCmptType.getQualifiedName());
        secondProductCmpt.newGeneration();

        ProductCmptRefControl productCmptRefControl = new ProductCmptRefControl(project, new Shell(), new UIToolkit(
                null));

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productCmpt.getIpsSrcFile()));
        assertTrue(list.contains(secondProductCmpt.getIpsSrcFile()));

        assertEquals(2, list.size());
    }

    @Test
    public void testGetSrcFilesExclude() {
        IIpsProject project = newIpsProject("BaseProject");

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");

        ProductCmpt productCmpt = newProductCmpt(project, "Product");
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());
        productCmpt.newGeneration();

        ProductCmpt secondProductCmpt = newProductCmpt(project, "SecondProduct");
        secondProductCmpt.setProductCmptType(productCmptType.getQualifiedName());
        secondProductCmpt.newGeneration();

        ProductCmptRefControl productCmptRefControl = new ProductCmptRefControl(project, new Shell(), new UIToolkit(
                null));

        productCmptRefControl.setProductCmptsToExclude(new IProductCmpt[] { productCmpt });

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertFalse(list.contains(productCmpt.getIpsSrcFile()));
        assertTrue(list.contains(secondProductCmpt.getIpsSrcFile()));

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

        ProductCmpt productCmpt = newProductCmpt(project, "Product");
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());
        productCmpt.newGeneration();
        IIpsSrcFile productIpsSrcFile = productCmpt.getIpsSrcFile();

        ProductCmpt subProductCmpt = newProductCmpt(subProject, "SubProduct");
        subProductCmpt.setProductCmptType(productCmptType.getQualifiedName());
        subProductCmpt.newGeneration();
        IIpsSrcFile subProductIpsSrcFile = subProductCmpt.getIpsSrcFile();

        ProductCmpt anotherProductCmpt = newProductCmpt(anotherProject, "AnotherProduct");
        anotherProductCmpt.setProductCmptType(productCmptType.getQualifiedName());
        anotherProductCmpt.newGeneration();
        IIpsSrcFile anotherProductIpsSrcFile = anotherProductCmpt.getIpsSrcFile();

        ProductCmptRefControl productCmptRefControl = new ProductCmptRefControl(projects, new Shell(), new UIToolkit(
                null));

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productIpsSrcFile));
        assertTrue(list.contains(subProductIpsSrcFile));
        assertFalse(list.contains(anotherProductIpsSrcFile));

        assertEquals(2, list.size());
    }

    @Test
    public void testTemplates() {
        IIpsProject project = newIpsProject("BaseProject");

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");
        IProductCmptType otherType = newProductCmptType(project, "OtherProductType");

        ProductCmpt template = newProductTemplate(project, "Template1");
        ProductCmpt template2 = newProductTemplate(project, "Template2");
        ProductCmpt template3 = newProductTemplate(project, "Template3");
        template.setProductCmptType(productCmptType.getQualifiedName());
        template2.setProductCmptType(productCmptType.getQualifiedName());
        template3.setProductCmptType(otherType.getQualifiedName());

        ProductCmpt productCmpt = newProductCmpt(project, "Product");
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());
        productCmpt.newGeneration();

        ProductCmptRefControl productCmptRefControl = new ProductCmptRefControl(project, new Shell(), new UIToolkit(
                null));
        productCmptRefControl.setProductCmptType(productCmptType, true);
        productCmptRefControl.setSearchTemplates(true);

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());
        assertThat(list.size(), is(2));
        assertTrue(list.contains(template.getIpsSrcFile()));
        assertTrue(list.contains(template2.getIpsSrcFile()));

    }

}
