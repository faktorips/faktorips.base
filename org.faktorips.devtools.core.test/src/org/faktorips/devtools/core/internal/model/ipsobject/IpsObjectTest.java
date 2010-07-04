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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.message.MessageList;

public class IpsObjectTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot rootFolder;
    private IIpsSrcFile srcFile;
    private IIpsObject ipsObject;
    private ContentChangeEvent lastEvent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        rootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsObject = newPolicyCmptType(ipsProject, "pack.TestProduct");
        srcFile = ipsObject.getIpsSrcFile();
    }

    public void testGetQualifiedName() throws CoreException {
        assertEquals("pack.TestProduct", ipsObject.getQualifiedName());
        IIpsPackageFragment defaultFolder = rootFolder.getIpsPackageFragment("");
        IIpsSrcFile file = defaultFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestProduct", true, null);
        assertEquals("TestProduct", file.getIpsObject().getQualifiedName());
    }

    public void testSetDescription() {
        ipsObject.getIpsModel().addChangeListener(this);
        ipsObject.setDescription("new description");
        assertEquals("new description", ipsObject.getDescription());
        assertTrue(srcFile.isDirty());
        assertEquals(srcFile, lastEvent.getIpsSrcFile());
    }

    public void testValidateEqualIpsObjectAlreadyExistsInIpsObjectPath() throws CoreException {
        IIpsProject a = newIpsProject("aProject");
        IPolicyCmptType aPolicyProjectA = newPolicyCmptTypeWithoutProductCmptType(a, "faktorzehn.example.APolicy");
        IIpsProject b = newIpsProject("bProject");
        IPolicyCmptType aPolicyProjectB = newPolicyCmptTypeWithoutProductCmptType(b, "faktorzehn.example.APolicy");

        IIpsObjectPath bPath = b.getIpsObjectPath();
        IIpsObjectPathEntry[] bPathEntries = bPath.getEntries();
        List<IIpsObjectPathEntry> newbPathEntries = new ArrayList<IIpsObjectPathEntry>();
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

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

}
