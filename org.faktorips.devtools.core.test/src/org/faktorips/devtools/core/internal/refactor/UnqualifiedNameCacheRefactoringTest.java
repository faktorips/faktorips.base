/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.faktorips.abstracttest.core.AbstractCoreIpsPluginTest;
import org.faktorips.devtools.model.internal.ipsproject.cache.UnqualifiedNameCache;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class UnqualifiedNameCacheRefactoringTest extends AbstractCoreIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsProject baseProject;
    private IProductCmptType hausrat;
    private IProductCmpt productCmptHausrat2013;
    private UnqualifiedNameCache unqualifiedNameCache;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();

        baseProject = this.newIpsProject();
        IIpsProjectProperties props = baseProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { "Integer" });
        baseProject.setProperties(props);
        hausrat = newProductCmptType(ipsProject, "hausrat");
        productCmptHausrat2013 = newProductCmpt(hausrat, "b.productCmptHausrat2013");
        unqualifiedNameCache = new UnqualifiedNameCache(ipsProject);
    }

    @Test
    public void testFindProductCmptByUnqualifiedName_renameProductCmptType() {
        performRenameRefactoring(productCmptHausrat2013, "newproductCmptHausrat2013");

        Collection<IIpsSrcFile> resultWithNewName = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("newproductCmptHausrat2013");
        Collection<IIpsSrcFile> resultWithOldName = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("productCmptHausrat2013");

        assertNotNull(resultWithNewName);
        assertTrue(resultWithOldName.isEmpty());
    }

    @Test
    public void testFindProductCmptByUnqualifiedName_moveProdCmptType() {
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

}
