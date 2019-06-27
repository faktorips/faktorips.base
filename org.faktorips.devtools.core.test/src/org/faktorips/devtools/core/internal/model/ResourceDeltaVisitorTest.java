/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class ResourceDeltaVisitorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private TestIpsSrcFilesChangeListener listener = new TestIpsSrcFilesChangeListener();

    private IpsModel ipsModel;

    private ProductCmptType productCmptType;

    @Override
    @Before
    public void setUp() throws CoreException {
        ipsProject = newIpsProject();
        ipsModel = (IpsModel)IpsPlugin.getDefault().getIpsModel();
        productCmptType = newProductCmptType(ipsProject, "TestPCT");
        ipsModel.addIpsSrcFilesChangedListener(listener);
    }

    @Test
    public void testChangeIpsProjectProperties() throws CoreException {
        productCmptType.validate(ipsProject);
        assertTrue(ipsModel.getValidationResultCache().getResult(productCmptType).isEmpty());

        IFile projectPropertiesFile = ipsProject.getIpsProjectPropertiesFile();
        projectPropertiesFile.touch(null);

        assertThat(ipsModel.getValidationResultCache().getResult(productCmptType), is(nullValue()));
    }

    @Test
    public void testChangeManifest() throws CoreException {
        productCmptType.validate(ipsProject);
        assertTrue(ipsModel.getValidationResultCache().getResult(productCmptType).isEmpty());

        IFile manifestFile = ipsProject.getProject().getFile(new Path(IpsBundleManifest.MANIFEST_NAME));
        ((IFolder)manifestFile.getParent()).create(true, true, null);
        manifestFile.create(new ByteArrayInputStream(new byte[0]), true, null);
        manifestFile.touch(null);

        assertThat(ipsModel.getValidationResultCache().getResult(productCmptType), is(nullValue()));
    }

    @Test
    public void testChangeIpsSrcFile() throws CoreException {
        productCmptType.getEnclosingResource().touch(null);

        waitForIndexer();

        assertThat(listener.changedFiles, hasItems(productCmptType.getIpsSrcFile()));
    }

    @Test
    public void testChangeIpsSrcFileOffRoot() throws CoreException {
        IFolder folder = ipsProject.getProject().getFolder("test");
        folder.create(true, true, null);
        IResource file = productCmptType.getEnclosingResource();
        file.copy(folder.getFullPath().append(file.getName()), true, null);

        waitForIndexer();

        assertTrue(listener.changedFiles.isEmpty());
    }

    private final class TestIpsSrcFilesChangeListener implements IIpsSrcFilesChangeListener {

        private List<IIpsSrcFile> changedFiles = new ArrayList<IIpsSrcFile>();

        @Override
        public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
            changedFiles.addAll(event.getChangedIpsSrcFiles());
        }
    }

}
