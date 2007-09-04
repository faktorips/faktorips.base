/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.util.StringUtil;

/**
 *
 * @author Markus Blum
 */
public class IpsPackageSortDefinitionTest extends AbstractIpsPluginTest {

    private IpsPackageFragmentArbitrarySortDefinition sorter;
    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot rootPackage;

    IIpsPackageFragment pack1;
    IIpsPackageFragment pack2;
    IIpsPackageFragment service;

    protected void setUp() throws Exception {
         super.setUp();

         sorter = new IpsPackageFragmentArbitrarySortDefinition();
         ipsProject = this.newIpsProject("TestProject");
         rootPackage = ipsProject.getIpsPackageFragmentRoots()[0];

         service = rootPackage.createPackageFragment("products.kranken.leistungsarten", true, null);
         pack1 = rootPackage.createPackageFragment("products.kranken.leistungsarten.fix", true, null);
         pack2 = rootPackage.createPackageFragment("products.kranken.leistungsarten.optional", true, null);

         IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");

         ArrayList list = new ArrayList(2);
         list.add("products");

         createPackageOrderFile((IFolder) rootPackage.getCorrespondingResource(), list);
         list.clear();

         list.add("unfall");
         list.add("kranken");
         list.add("folder");
         list.add("haftpflicht");
         list.add("hausrat");

         createPackageOrderFile((IFolder) products.getCorrespondingResource(), list);
         list.clear();

         list.add("optional");
         list.add("fix");

         createPackageOrderFile((IFolder) service.getCorrespondingResource(), list);
         list.clear();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCompare() {

    }

    public void testGetSegmentNames () throws CoreException {
        String packageNames = getSortDefinitionContent(pack1);
        sorter.initPersistenceContent(packageNames, this.ipsProject.getPlainTextFileCharset());

        assertEquals(2, sorter.getSegmentNames().length);

        IpsPackageFragmentArbitrarySortDefinition test = new IpsPackageFragmentArbitrarySortDefinition();
        assertEquals(0, test.getSegmentNames().length);

    }

    public void testInitPersistenceContent() throws CoreException {
        assertEquals(0, sorter.getSegmentNames().length);

        String packageNames = getSortDefinitionContent(pack1);
        sorter.initPersistenceContent(packageNames, this.ipsProject.getPlainTextFileCharset());

        assertEquals(2, sorter.getSegmentNames().length);
    }

    public void testSetSegmentNames() throws CoreException {

        String packageNames = getSortDefinitionContent(pack1);
        sorter.initPersistenceContent(packageNames, this.ipsProject.getPlainTextFileCharset());

        // change content
        String[] content = new String[3];

        content[0] = "folder1";
        content[1] = "folder2";
        content[2] = "folder3";

        sorter.setSegmentNames(content);

        String[] result = sorter.getSegmentNames();

        assertEquals(3, result.length);
        assertEquals("folder1", result[0]);
        assertEquals("folder2", result[1]);
        assertEquals("folder3", result[2]);
    }

    public void testToPersistenceContent() throws CoreException {
        String packageNames = getSortDefinitionContent(pack1);
        sorter.initPersistenceContent(packageNames, this.ipsProject.getPlainTextFileCharset());

        String content = sorter.toPersistenceContent();

        assertNotNull(content);

        IpsPackageFragmentArbitrarySortDefinition test = new IpsPackageFragmentArbitrarySortDefinition();
        test.initPersistenceContent(content, this.ipsProject.getPlainTextFileCharset());

        assertEquals(2, test.getSegmentNames().length);
    }

    private String getSortDefinitionContent(IIpsPackageFragment fragment) throws CoreException {
        IFolder folder;

        if (fragment.isDefaultPackage()) {
            folder = (IFolder) fragment.getRoot().getCorrespondingResource();
        } else {
            folder = (IFolder) fragment.getParentIpsPackageFragment().getCorrespondingResource();
        }

        IFile file = folder.getFile(new Path(IpsPackageFragment.SORT_ORDER_FILE));
        String content;

        try {
             content = StringUtil.readFromInputStream(file.getContents(), fragment.getIpsProject().getPlainTextFileCharset());
        } catch (IOException e) {
            IpsPlugin.log(e);
            return null;
        }

        return content;
    }

}
