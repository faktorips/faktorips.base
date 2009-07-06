/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class DefaultLabelProviderTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IIpsPackageFragment packageFragment;
    private DefaultLabelProvider provider;
    private ILabelProvider providerWithIpsSourceFileMapping;

    protected void setUp() throws Exception {
        super.setUp();
        
        project = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        packageFragment = root.createPackageFragment("products.folder", true, null);  
        provider = new DefaultLabelProvider();
        providerWithIpsSourceFileMapping = DefaultLabelProvider.createWithIpsSourceFileMapping();
    }

    public void testGetTextForIpsSourceFiles() throws CoreException{
        String ipsObjectName = "testCaseTextTest";
        String fileName = ipsObjectName + "." + IpsObjectType.TEST_CASE.getFileExtension();
        
        assertEquals("<null>", provider.getText(null));
        
        IIpsSrcFile file = packageFragment.createIpsFile(IpsObjectType.TEST_CASE, ipsObjectName, true, null);
        assertEquals(fileName, provider.getText(file));
        assertEquals(ipsObjectName, providerWithIpsSourceFileMapping.getText(file));
    }
    
    public void testGetImageForIpsSourceFiles() throws CoreException{
        IIpsSrcFile file = packageFragment.createIpsFile(IpsObjectType.TEST_CASE, "testCaseImageTest", true, null);
        assertEquals(IpsObjectType.IPS_SOURCE_FILE.getImage(true), provider.getImage(file));
        assertEquals(IpsObjectType.TEST_CASE.getImage(true), providerWithIpsSourceFileMapping.getImage(file));
    }
}
