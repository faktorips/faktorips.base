/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NewProductCmptPMOTest {

    private static final String PROJECT_NAME = "projectName";
    private SingletonMockHelper singletonMockHelper;
    private IIpsModel ipsModel;

    @Before
    public void setUp() {
        IpsPlugin ipsPlugin = mock(IpsPlugin.class);
        ipsModel = mock(IIpsModel.class);
        singletonMockHelper = new SingletonMockHelper();
        singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);
        when(ipsPlugin.getIpsModel()).thenReturn(ipsModel);
    }

    @Test
    public void testUpdateBaseTypeList_noProject() throws Exception {
        NewProductCmptPMO pmo = new NewProductCmptPMO(null);

        IIpsPackageFragmentRoot ipsPackageFragmentRoot = mockPackageFragmentRoot();

        pmo.setPackageRoot(ipsPackageFragmentRoot);

        assertTrue(pmo.getBaseTypes().isEmpty());
    }

    private IIpsPackageFragmentRoot mockPackageFragmentRoot() throws CoreException {
        IIpsPackageFragmentRoot ipsPackageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        IIpsProject ipsProject = mock(IIpsProject.class);
        IProductCmptNamingStrategy productCmptNamingStrategy = mock(IProductCmptNamingStrategy.class);
        when(ipsPackageFragmentRoot.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.findIpsSrcFiles(any(IpsObjectType.class))).thenReturn(new IIpsSrcFile[0]);
        when(ipsProject.getProductCmptNamingStrategy()).thenReturn(productCmptNamingStrategy);
        when(ipsProject.getSourceIpsPackageFragmentRoots()).thenReturn(
                new IIpsPackageFragmentRoot[] { ipsPackageFragmentRoot });
        return ipsPackageFragmentRoot;
    }

    @Test
    public void testUpdateBaseTypeList_withProject() throws Exception {
        NewProductCmptPMO pmo = new NewProductCmptPMO(null);

        IIpsPackageFragmentRoot packageFragmentRoot = mockPackageFragmentRoot();
        IIpsProject ipsProject = packageFragmentRoot.getIpsProject();

        ArrayList<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>();

        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(
                ipsSrcFiles.toArray(new IIpsSrcFile[0]));
        when(ipsProject.getName()).thenReturn(PROJECT_NAME);

        when(ipsModel.getIpsProject(PROJECT_NAME)).thenReturn(ipsProject);

        pmo.setIpsProject(ipsProject);
        assertTrue(pmo.getBaseTypes().isEmpty());

        IIpsSrcFile ipsSrcFile1 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType1 = mock(IProductCmptType.class);
        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType2 = mock(IProductCmptType.class);
        IIpsSrcFile ipsSrcFile3 = mock(IIpsSrcFile.class);
        IProductCmptType productCmptType3 = mock(IProductCmptType.class);
        ipsSrcFiles.add(ipsSrcFile1);
        ipsSrcFiles.add(ipsSrcFile2);
        ipsSrcFiles.add(ipsSrcFile3);
        when(ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE)).thenReturn(
                ipsSrcFiles.toArray(new IIpsSrcFile[0]));

        when(ipsSrcFile1.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE)).thenReturn("true");
        when(ipsSrcFile1.getIpsObject()).thenReturn(productCmptType1);
        when(ipsSrcFile2.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE)).thenReturn("false");
        when(ipsSrcFile2.getIpsObject()).thenReturn(productCmptType2);
        when(ipsSrcFile3.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE)).thenReturn(null);
        when(ipsSrcFile3.getIpsObject()).thenReturn(productCmptType3);

        when(ipsSrcFile3.getPropertyValue(IType.PROPERTY_SUPERTYPE)).thenReturn("findSuperType");

        when(ipsProject.findIpsSrcFile(new QualifiedNameType("findSuperType", IpsObjectType.PRODUCT_CMPT_TYPE)))
                .thenReturn(ipsSrcFile1);

        // refresh the list
        pmo.setIpsProject(ipsProject);

        assertTrue(pmo.getBaseTypes().size() == 2);
        assertTrue(pmo.getBaseTypes().contains(productCmptType2));
        assertTrue(pmo.getBaseTypes().contains(productCmptType3));
    }

    @After
    public void tearDown() {
        singletonMockHelper.reset();
    }

    @Test
    public void testSetPackageRoot() {

    }

    @Test
    public void testInitDefaults() {

    }

}
