/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.cache;

import static org.faktorips.devtools.abstraction.mapping.PathMapping.toEclipsePath;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
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
    public void testFindProductCmptByUnqualifiedName_removeProductCmpt() throws CoreRuntimeException {
        Collection<IIpsSrcFile> oldResult = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("productCmptHausrat2013");

        productCmptHausrat2013.delete();

        Collection<IIpsSrcFile> result = unqualifiedNameCache
                .findProductCmptByUnqualifiedName("productCmptHausrat2013");

        assertNotNull(oldResult);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindProductCmptByUnqualifiedName_moreThanOneProdCmpt() throws CoreRuntimeException {
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

        IResource resource = productCmpt.getEnclosingResource().unwrap();
        resource.move(toEclipsePath(ipsProject.getProject().getWorkspaceRelativePath().resolve(resource.getName())), true, null);

        assertTrue(unqualifiedNameCache.findProductCmptByUnqualifiedName("Test").isEmpty());
    }

}
