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

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.junit.Before;
import org.junit.Test;

public class IpsSrcFilesChangeListenerTest extends AbstractIpsPluginTest implements IIpsSrcFilesChangeListener {
    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private PolicyCmptType pcType;
    private IPolicyCmptType supertype;
    private IPolicyCmptType supersupertype;
    private IpsSrcFilesChangedEvent event;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        pdRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        pdFolder = pdRootFolder.createPackageFragment("products.folder", true, null);
        pdSrcFile = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();

        // create two more types that act as supertype and supertype's supertype
        IIpsSrcFile file1 = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supertype", true, null);
        supertype = (PolicyCmptType)file1.getIpsObject();
        IIpsSrcFile file2 = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supersupertype", true, null);
        supersupertype = (PolicyCmptType)file2.getIpsObject();
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        IIpsModel.get().addIpsSrcFilesChangedListener(this);
    }

    @Test
    public void testIpsSrcFilesChanged() throws CoreRuntimeException {
        // TODO: Gibt es eine Möglichkeit zwei save-Event gleichzeitig zu testen (saveAll)
        pcType.getIpsSrcFile().save(true, null);
        while (event == null) {
        }
        Set<IIpsSrcFile> ipsSrcFiles = event.getChangedIpsSrcFiles();
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            assertTrue("TestPolicy.ipspolicycmpttype".equals(ipsSrcFile.getName()));
        }
    }

    @Override
    public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
        this.event = event;
    }

}
