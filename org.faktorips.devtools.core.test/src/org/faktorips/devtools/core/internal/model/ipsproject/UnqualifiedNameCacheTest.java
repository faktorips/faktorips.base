/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class UnqualifiedNameCacheTest extends AbstractIpsPluginTest {

    private IpsProject ipsProject;
    private IpsProject baseProject;
    private IProductCmptType hausrat;
    private IProductCmpt productCmptHausrat2013;
    private UnqualifiedNameCache unqualifiedNameCache;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)this.newIpsProject();

        baseProject = (IpsProject)this.newIpsProject();
        IIpsProjectProperties props = baseProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { "Integer" });
        baseProject.setProperties(props);
        hausrat = newProductCmptType(ipsProject, "hausrat");
        productCmptHausrat2013 = newProductCmpt(hausrat, "b.productCmptHausrat2013");
        unqualifiedNameCache = new UnqualifiedNameCache(ipsProject);
    }

    @Test
    public void testFindProductCmptByUnqualifiedName_ValidInput() {
        Collection<IIpsSrcFile> result = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("productCmptHausrat2013");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(productCmptHausrat2013.getIpsSrcFile()));
    }

    @Test
    public void testFindProductCmptByUnqualifiedName_InvalidInput() {
        Collection<IIpsSrcFile> result = unqualifiedNameCache.findProductCmptByUnqualifiedName("invalidProductName");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindProductCmptByUnqualifiedName_removeProductCmpt() throws CoreException {
        Collection<IIpsSrcFile> oldResult = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("productCmptHausrat2013");

        productCmptHausrat2013.delete();

        Collection<IIpsSrcFile> result = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("productCmptHausrat2013");

        assertNotNull(oldResult);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindProductCmptByUnqualifiedName_renameProductCmptType() throws CoreException {
        performRenameRefactoring(productCmptHausrat2013, "newproductCmptHausrat2013");

        Collection<IIpsSrcFile> resultWithNewName = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("newproductCmptHausrat2013");
        Collection<IIpsSrcFile> resultWithOldName = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("productCmptHausrat2013");

        assertNotNull(resultWithNewName);
        assertTrue(resultWithOldName.isEmpty());
    }

    @Test
    public void testFindProductCmptByUnqualifiedName_moveProdCmptType() throws CoreException {
        IIpsPackageFragmentRoot fragmentRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment targetIpsPackageFragment = fragmentRoot.createPackageFragment("target", true, null);
        performMoveRefactoring(productCmptHausrat2013, targetIpsPackageFragment);
        IIpsSrcFile ipsSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.PRODUCT_CMPT, "target.productCmptHausrat2013");

        Collection<IIpsSrcFile> result = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("productCmptHausrat2013");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(ipsSrcFile));
    }

    @Test
    public void testFindProductCmptByUnqualifiedName_moreThanOneProdCmpt() throws CoreException {
        ProductCmptType kfz = newProductCmptType(ipsProject, "kfz");
        newProductCmpt(kfz, "z.productCmptHausrat");
        productCmptHausrat2013 = newProductCmpt(hausrat, "b.productCmptHausrat");

        Collection<IIpsSrcFile> result = unqualifiedNameCache.findProductCmptByUnqualifiedName("productCmptHausrat");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testNoResourceChangeForIpsSrcFileOffRoot() throws Exception {
        ProductCmpt productCmpt = newProductCmpt(ipsProject, "foo.Test");
        IIpsSrcFile ipsSrcFile = productCmpt.getIpsSrcFile();

        assertThat(unqualifiedNameCache.findProductCmptByUnqualifiedName("Test"), hasItem(ipsSrcFile));

        IResource resource = productCmpt.getEnclosingResource();
        resource.move(ipsProject.getProject().getFullPath().append(resource.getName()), true, null);

        assertTrue(unqualifiedNameCache.findProductCmptByUnqualifiedName("Test").isEmpty());
    }

}
