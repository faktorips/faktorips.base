/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.instanceexplorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsMetaClass;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class InstanceExplorerTest extends AbstractIpsPluginTest {

    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private ProductCmptType pcType;
    private IIpsSrcFile pdSrcFile2;
    private ProductCmptType pcType2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        pdRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        pdFolder = pdRootFolder.createPackageFragment("products.folder", true, null);
        pdSrcFile = pdFolder.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "TestProduct", true, null);
        pcType = (ProductCmptType)pdSrcFile.getIpsObject();

        pdSrcFile2 = pdFolder.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "TestProduct2", true, null);
        pcType2 = (ProductCmptType)pdSrcFile2.getIpsObject();
    }

    @Test
    public void testSupport() {
        assertTrue(InstanceExplorer.supports(pcType));
    }

    @Test
    public void testIsChanged() {
        InstanceExplorer test = new InstanceExplorer();

        Set<IIpsSrcFile> ipsSrcFiles = new HashSet<>();
        ipsSrcFiles.add(pcType.getIpsSrcFile());
        assertTrue(test.isDependendObjectChanged(pcType, ipsSrcFiles));
        assertFalse(test.isDependendObjectChanged(pcType2, ipsSrcFiles));
    }

    @Test
    public void testShowInstancesOf() throws Exception {
        InstanceExplorerMock testMock = new InstanceExplorerMock();
        testMock.showInstancesOf(pcType);
        assertEquals(pcType, testMock.element);

    }

    private static class InstanceExplorerMock extends InstanceExplorer {
        private IIpsObject element;

        @Override
        protected void setInputData(final IIpsMetaClass element) {
            this.element = element;
        }

    }
}
