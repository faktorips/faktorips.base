/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class IpsObjectTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot rootFolder;
    private IIpsObject ipsObject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        rootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsObject = newPolicyCmptType(ipsProject, "pack.TestProduct");
    }

    @Test
    public void testGetQualifiedName() throws CoreRuntimeException {
        assertEquals("pack.TestProduct", ipsObject.getQualifiedName());
        IIpsPackageFragment defaultFolder = rootFolder.getIpsPackageFragment("");
        IIpsSrcFile file = defaultFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestProduct", true, null);
        assertEquals("TestProduct", file.getIpsObject().getQualifiedName());
    }

    @Test
    public void testValidateEqualIpsObjectAlreadyExistsInIpsObjectPath() throws CoreRuntimeException {
        IIpsProject a = newIpsProject("aProject");
        IPolicyCmptType aPolicyProjectA = newPolicyCmptTypeWithoutProductCmptType(a, "faktorzehn.example.APolicy");
        IIpsProject b = newIpsProject("bProject");
        IPolicyCmptType aPolicyProjectB = newPolicyCmptTypeWithoutProductCmptType(b, "faktorzehn.example.APolicy");

        IIpsObjectPath bPath = b.getIpsObjectPath();
        IIpsObjectPathEntry[] bPathEntries = bPath.getEntries();
        List<IIpsObjectPathEntry> newbPathEntries = new ArrayList<>();
        newbPathEntries.add(new IpsProjectRefEntry((IpsObjectPath)bPath, a));
        for (IIpsObjectPathEntry bPathEntrie : bPathEntries) {
            newbPathEntries.add(bPathEntrie);
        }
        bPath.setEntries(newbPathEntries.toArray(new IIpsObjectPathEntry[newbPathEntries.size()]));
        b.setIpsObjectPath(bPath);

        MessageList msgList = aPolicyProjectA.validate(a);
        assertNull(msgList.getMessageByCode(IIpsObject.MSGCODE_SAME_IPSOBJECT_IN_IPSOBEJECTPATH_AHEAD));

        msgList = aPolicyProjectB.validate(b);
        assertNotNull(msgList.getMessageByCode(IIpsObject.MSGCODE_SAME_IPSOBJECT_IN_IPSOBEJECTPATH_AHEAD));
    }

    @Test
    public void testToXml() throws Exception {
        Element xml = ipsObject.toXml(newDocument());
        assertEquals(XmlUtil.XML_ATTRIBUTE_SPACE_VALUE, xml.getAttribute(XmlUtil.XML_ATTRIBUTE_SPACE));
    }

    @Test
    public void testDelete() throws CoreRuntimeException {
        ipsObject.delete();
        assertFalse(ipsObject.exists());
    }

    @Test
    public void testCreateCopy() throws RuntimeException {
        IIpsPackageFragment packageFragment = ipsObject.getIpsPackageFragment();
        IIpsSrcFile copiedObject = ipsObject.createCopy(packageFragment, "testKopie", false, new NullProgressMonitor());
        assertNotNull(copiedObject);
        assertTrue(copiedObject.exists());
        assertEquals("TestProduct", ipsObject.getName());
        assertEquals("testKopie.ipspolicycmpttype", copiedObject.getName());
        assertEquals(IpsObjectType.POLICY_CMPT_TYPE, ipsObject.getIpsObjectType());
    }
}
