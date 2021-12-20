/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class IpsObjectPathXmlPersisterTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPathXmlPersister persistor;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        persistor = new IpsObjectPathXmlPersister();
    }

    @Test
    public void testStoreXml() {
        AProject project = ipsProject.getProject();
        IpsObjectPath path = new IpsObjectPath(ipsProject);

        // test case 1: output folder and base package defined per entry
        path.setOutputDefinedPerSrcFolder(true);

        IIpsSrcFolderEntry entry0 = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("modelclasses"));
        entry0.setSpecificOutputFolderForMergableJavaFiles(project.getFolder("javasrc").getFolder("modelclasses"));
        entry0.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.model");
        entry0.setSpecificOutputFolderForDerivedJavaFiles(project.getFolder("javasrc").getFolder(
                "modelclasses.extensions"));
        entry0.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample.model.extensions");
        IIpsSrcFolderEntry entry1 = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("products"));
        entry1.setSpecificOutputFolderForMergableJavaFiles(project.getFolder("javasrc").getFolder("products"));
        entry1.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample.products");
        entry1.setSpecificOutputFolderForDerivedJavaFiles(project.getFolder("javasrc").getFolder("products")
                .getFolder("extensions"));
        entry1.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample.products.extensions");
        path.setEntries(new IIpsObjectPathEntry[] { entry0, entry1 });

        Element element = new IpsObjectPathXmlPersister().store(newDocument(), path);
        path = new IpsObjectPath(ipsProject);
        path = persistor.read(ipsProject, element);
        assertTrue(path.isOutputDefinedPerSrcFolder());
        assertEquals("", path.getBasePackageNameForMergableJavaClasses());
        assertNull(path.getOutputFolderForMergableSources());
        assertEquals("", path.getBasePackageNameForDerivedJavaClasses());
        assertNull(path.getOutputFolderForDerivedSources());
        assertEquals(2, path.getEntries().length);

        // test case 2: output folder and package defined via the path for all entries
        path.setOutputDefinedPerSrcFolder(false);
        path.setOutputFolderForMergableSources(project.getFolder("generated"));
        path.setBasePackageNameForMergableJavaClasses("org.sample.generated");
        path.setOutputFolderForDerivedSources(project.getFolder("extensions"));
        path.setBasePackageNameForDerivedJavaClasses("org.sample.extensions");
        path.setOutputFolderForDerivedSources(project.getFolder("derived"));
        element = new IpsObjectPathXmlPersister().store(newDocument(), path);
        path = new IpsObjectPath(ipsProject);
        path = persistor.read(ipsProject, element);
        assertFalse(path.isOutputDefinedPerSrcFolder());
        assertEquals("org.sample.generated", path.getBasePackageNameForMergableJavaClasses());
        assertEquals(project.getFolder("generated"), path.getOutputFolderForMergableSources());
        assertEquals("org.sample.extensions", path.getBasePackageNameForDerivedJavaClasses());
        assertEquals(2, path.getEntries().length);
        assertEquals(project.getFolder("derived"), path.getOutputFolderForDerivedSources());

    }

    @Test
    public void testStoreXmlUseManifest() {

        IpsObjectPath path = new IpsObjectPath(ipsProject);
        path.setUsingManifest(true);

        Element element = new IpsObjectPathXmlPersister().store(newDocument(), path);
        assertTrue(Boolean.parseBoolean(element.getAttribute("useManifest")));
    }

    @Test
    public void testReadXml() {
        Element docElement = getTestDocument().getDocumentElement();

        // test case 1
        IIpsObjectPath path = new IpsObjectPathXmlPersister().read(ipsProject,
                XmlUtil.getElement(docElement, IpsObjectPathXmlPersister.XML_TAG_NAME, 0));

        assertTrue(path.isOutputDefinedPerSrcFolder());
        assertEquals("", path.getBasePackageNameForMergableJavaClasses());
        assertNull(path.getOutputFolderForMergableSources());
        assertEquals("", path.getBasePackageNameForDerivedJavaClasses());
        assertEquals(ipsProject.getProject().getFolder("derived"), path.getOutputFolderForDerivedSources());

        IIpsObjectPathEntry[] entries = path.getEntries();
        assertEquals(2, entries.length);
        assertEquals("ipssrc/modelclasses",
                PathUtil.toPortableString(((IIpsSrcFolderEntry)entries[0]).getSourceFolder().getProjectRelativePath()));
        assertEquals("ipssrc/products",
                PathUtil.toPortableString(((IIpsSrcFolderEntry)entries[1]).getSourceFolder().getProjectRelativePath()));

        // test case 2
        path = new IpsObjectPathXmlPersister().read(ipsProject,
                XmlUtil.getElement(docElement, IpsObjectPathXmlPersister.XML_TAG_NAME, 1));

        assertFalse(path.isOutputDefinedPerSrcFolder());
        assertEquals("org.sample.generated", path.getBasePackageNameForMergableJavaClasses());
        assertEquals("generated", path.getOutputFolderForMergableSources().getName());
        assertEquals("org.sample.extension", path.getBasePackageNameForDerivedJavaClasses());
        assertEquals("extensions", path.getOutputFolderForDerivedSources().getName());

        entries = path.getEntries();
        assertEquals(2, entries.length);
        assertEquals("ipssrc/modelclasses",
                PathUtil.toPortableString(((IIpsSrcFolderEntry)entries[0]).getSourceFolder().getProjectRelativePath()));
        assertEquals("ipssrc/products",
                PathUtil.toPortableString(((IIpsSrcFolderEntry)entries[1]).getSourceFolder().getProjectRelativePath()));

    }

    @Test(expected = IllegalStateException.class)
    public void testReadXmlUseManifest() {
        Element docElement = getTestDocument().getDocumentElement();

        IIpsObjectPath path = persistor.read(ipsProject,
                XmlUtil.getElement(docElement, IpsObjectPathXmlPersister.XML_TAG_NAME, 2));
        assertTrue(path.isUsingManifest());
    }

}
