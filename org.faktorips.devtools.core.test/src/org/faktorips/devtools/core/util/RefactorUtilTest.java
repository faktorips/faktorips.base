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

package org.faktorips.devtools.core.util;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RefactorUtilTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "test.Policy");
    }

    public void testCopyIpsSrcFile() throws CoreException {
        IIpsPackageFragment targetIpsPackage = policyCmptType.getIpsPackageFragment();
        IIpsSrcFile fileToBeCopied = policyCmptType.getIpsSrcFile();
        IIpsSrcFile copiedSrcFile = RefactorUtil.copyIpsSrcFile(fileToBeCopied, targetIpsPackage, "Foo", null);
        assertTrue(copiedSrcFile.exists());

        assertEquals("Foo" + "." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension(), copiedSrcFile.getName());
        assertEquals(3, targetIpsPackage.getIpsSrcFiles().length);
    }

}
