/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.abstraction.AFile;
import org.faktorips.devtools.model.abstraction.AFolder;
import org.faktorips.devtools.model.abstraction.AResource;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class ResourceDeltaVisitorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private TestIpsSrcFilesChangeListener listener = new TestIpsSrcFilesChangeListener();

    private IpsModel ipsModel;

    private ProductCmptType productCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        ipsModel = (IpsModel)IIpsModel.get();
        productCmptType = newProductCmptType(ipsProject, "TestPCT");
        ipsModel.addIpsSrcFilesChangedListener(listener);
    }

    @Test
    public void testChangeIpsProjectProperties() throws CoreRuntimeException {
        productCmptType.validate(ipsProject);
        assertTrue(ipsModel.getValidationResultCache().getResult(productCmptType).isEmpty());

        AFile projectPropertiesFile = ipsProject.getIpsProjectPropertiesFile();
        projectPropertiesFile.touch(null);

        assertThat(ipsModel.getValidationResultCache().getResult(productCmptType), is(nullValue()));
    }

    @Test
    public void testChangeManifest() throws CoreRuntimeException {
        productCmptType.validate(ipsProject);
        assertTrue(ipsModel.getValidationResultCache().getResult(productCmptType).isEmpty());

        AFile manifestFile = ipsProject.getProject().getFile(java.nio.file.Path.of(IpsBundleManifest.MANIFEST_NAME));
        ((AFolder)manifestFile.getParent()).create(null);
        manifestFile.create(new ByteArrayInputStream(new byte[0]), null);
        manifestFile.touch(null);

        assertThat(ipsModel.getValidationResultCache().getResult(productCmptType), is(nullValue()));
    }

    @Test
    public void testChangeIpsSrcFile() throws JavaModelException {
        productCmptType.getEnclosingResource().touch(null);

        waitForIndexer();

        assertThat(listener.changedFiles, hasItems(productCmptType.getIpsSrcFile()));
    }

    @Test
    public void testChangeIpsSrcFileOffRoot() throws JavaModelException {
        AFolder folder = ipsProject.getProject().getFolder("test");
        folder.create(null);
        AResource file = productCmptType.getEnclosingResource();
        file.copy(folder.getWorkspaceRelativePath().resolve(file.getName()), null);

        waitForIndexer();

        assertTrue(listener.changedFiles.isEmpty());
    }

    private final class TestIpsSrcFilesChangeListener implements IIpsSrcFilesChangeListener {

        private List<IIpsSrcFile> changedFiles = new ArrayList<>();

        @Override
        public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
            changedFiles.addAll(event.getChangedIpsSrcFiles());
        }
    }

}
