/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.core.resources.IFile;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomIconSectionTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
    }

    @Test
    public void testComputeIconPath_NestedSourceFolder() throws Exception {
        AFolder nestedRoot = createFolder(
                createFolder(createFolder(ipsProject.getProject().getFolder("src"), "main"), "ips"), "model");
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.newSourceFolderEntry(nestedRoot);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        AFolder productsFolder = createFolder(nestedRoot, "products");
        createFileWithContent(productsFolder, "myIcon.gif", "dummy");
        IFile iconFile = (IFile)productsFolder.getFile("myIcon.gif").unwrap();

        assertEquals("products/myIcon.gif", CustomIconSection.computeIconPath(iconFile));
    }

    @Test
    public void testComputeIconPath_SourceFolderDirectlyBelowProject() throws Exception {
        IIpsPackageFragmentRoot defaultRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        AFolder defaultRootFolder = (AFolder)defaultRoot.getCorrespondingResource();
        createFileWithContent(defaultRootFolder, "myIcon.gif", "dummy");
        IFile iconFile = (IFile)defaultRootFolder.getFile("myIcon.gif").unwrap();

        assertEquals("myIcon.gif", CustomIconSection.computeIconPath(iconFile));
    }

    private AFolder createFolder(AFolder parent, String name) {
        AFolder folder = parent.getFolder(name);
        if (!folder.exists()) {
            folder.create(null);
        }
        return folder;
    }

}
