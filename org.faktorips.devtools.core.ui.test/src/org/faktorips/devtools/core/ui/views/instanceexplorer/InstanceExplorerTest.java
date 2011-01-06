/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.instanceexplorer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class InstanceExplorerTest extends AbstractIpsPluginTest {

    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private ProductCmptType pcType;
    private IIpsSrcFile pdSrcFile2;
    private ProductCmptType pcType2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        pdRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        pdFolder = pdRootFolder.createPackageFragment("products.folder", true, null);
        pdSrcFile = pdFolder.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "TestProduct", true, null);
        pcType = (ProductCmptType)pdSrcFile.getIpsObject();

        pdSrcFile2 = pdFolder.createIpsFile(IpsObjectType.PRODUCT_CMPT_TYPE, "TestProduct2", true, null);
        pcType2 = (ProductCmptType)pdSrcFile2.getIpsObject();
    }

    public void testSupport() {
        assertTrue(InstanceExplorer.supports(pcType));
    }

    public void testIsChanged() throws CoreException {
        InstanceExplorer test = new InstanceExplorer();

        Set<IIpsSrcFile> ipsSrcFiles = new HashSet<IIpsSrcFile>();
        ipsSrcFiles.add(pcType.getIpsSrcFile());
        assertTrue(test.isDependendObjectChanged(pcType, ipsSrcFiles));
        assertFalse(test.isDependendObjectChanged(pcType2, ipsSrcFiles));
    }

    public void testShowInstancesOf() throws Exception {
        InstanceExplorerMock testMock = new InstanceExplorerMock();
        testMock.showInstancesOf(pcType);
        assertEquals(pcType, testMock.element);

    }

    private class InstanceExplorerMock extends InstanceExplorer {
        private IIpsObject element;

        @Override
        protected void setInputData(final IIpsMetaClass element) {
            this.element = element;
        }

    }
}
