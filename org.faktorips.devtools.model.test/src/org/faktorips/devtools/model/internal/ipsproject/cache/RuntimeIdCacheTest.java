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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class RuntimeIdCacheTest extends AbstractIpsPluginTest {

    private IpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private RuntimeIdCache runtimeIdCache;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)newIpsProject();

        runtimeIdCache = new RuntimeIdCache(ipsProject);
        productCmptType = newProductCmptType(ipsProject, "productCmptType");
        productCmpt = newProductCmpt(productCmptType, "b.productCmpt2020");
    }

    @Test
    public void testFindProductCmptByRuntimeId_ChangeRuntimeId() {
        productCmpt.setRuntimeId("runtimeId");
        assertThat(runtimeIdCache.findProductCmptByRuntimeId("runtimeId").size(), is(1));

        productCmpt.setRuntimeId("newRuntimeId");

        assertThat(runtimeIdCache.findProductCmptByRuntimeId("newRuntimeId").size(), is(1));
        assertThat(runtimeIdCache.findProductCmptByRuntimeId("runtimeId").size(), is(0));
    }

    @Test
    public void testFindProductCmptByRuntimeId_RemoveProductCmpt() {
        productCmpt.setRuntimeId("runtimeId");
        Collection<IIpsSrcFile> beforeDeletion = runtimeIdCache.findProductCmptByRuntimeId("runtimeId");
        assertThat(beforeDeletion.size(), is(1));

        productCmpt.delete();
        Collection<IIpsSrcFile> afterDeletion = runtimeIdCache.findProductCmptByRuntimeId("runtimeId");

        assertThat(afterDeletion.size(), is(0));
    }

    @Test
    public void testFindProductCmptByRuntimeId_MoreThanOneProdCmptWithSameId() {
        productCmpt.setRuntimeId("runtimeId");
        ProductCmpt newProductCmpt = newProductCmpt(productCmptType, "b.productCmpt2020-2");
        newProductCmpt.setRuntimeId("runtimeId");

        Collection<IIpsSrcFile> result = runtimeIdCache.findProductCmptByRuntimeId("runtimeId");

        assertThat(result.size(), is(2));
    }

    @Test
    public void testFindProductCmptByRuntimeId_ChangeIdByText() throws IpsException, IOException {
        assertThat(runtimeIdCache.findProductCmptByRuntimeId("runtimeId").size(), is(0));
        assertThat(runtimeIdCache.findProductCmptByRuntimeId("productCmpt2020").size(), is(1));

        AFile file = (AFile)productCmpt.getEnclosingResource();
        InputStreamReader inputStreamReader = new InputStreamReader(file.getContents());
        String content = new BufferedReader(inputStreamReader).lines().collect(Collectors.joining("\n"));
        inputStreamReader.close();

        String modifiedContent = content.replace("runtimeId=\"productCmpt2020",
                "runtimeId=\"runtimeId");
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(modifiedContent.getBytes())) {
            file.setContents(inputStream, true, new NullProgressMonitor());
        }

        assertThat(runtimeIdCache.findProductCmptByRuntimeId("runtimeId").size(), is(1));
        assertThat(runtimeIdCache.findProductCmptByRuntimeId("productCmpt2020").size(), is(0));
    }

    @Test
    public void testNoResourceChangeForIpsSrcFileOffRoot() throws Exception {
        productCmpt.setRuntimeId("runtimeId");
        IIpsSrcFile ipsSrcFile = productCmpt.getIpsSrcFile();

        assertThat(runtimeIdCache.findProductCmptByRuntimeId("runtimeId"), hasItem(ipsSrcFile));

        IResource resource = productCmpt.getEnclosingResource().unwrap();
        resource.move(toEclipsePath(ipsProject.getProject().getWorkspaceRelativePath().resolve(resource.getName())), true, null);

        assertThat(runtimeIdCache.findProductCmptByRuntimeId("runtimeId").size(), is(0));
    }

    @Test
    public void testFileDeletion() throws Exception {
        productCmpt.setRuntimeId("runtimeId");
        assertThat(runtimeIdCache.findProductCmptByRuntimeId("runtimeId").size(), is(1));

        AResource resource = productCmpt.getEnclosingResource();
        resource.delete(new NullProgressMonitor());
        // to make sure the change is detected at deletion time
        // (and not by findProductCmptByRuntimeId checking if the file exists)
        // a new file is created at the same location
        productCmpt = newProductCmpt(ipsProject, "foo.Test");
        productCmpt.setRuntimeId("differentRuntimeId");

        assertThat(runtimeIdCache.findProductCmptByRuntimeId("runtimeId").size(), is(0));
    }
}
