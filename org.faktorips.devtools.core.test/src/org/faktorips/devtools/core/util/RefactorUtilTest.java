/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RefactorUtilTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "test.Policy");
    }

    @Test
    public void testCopyIpsSrcFile() throws CoreException {
        IIpsPackageFragment targetIpsPackage = policyCmptType.getIpsPackageFragment();
        IIpsSrcFile fileToBeCopied = policyCmptType.getIpsSrcFile();
        fileToBeCopied.save(true, null);
        IIpsSrcFile copiedSrcFile = RefactorUtil.copyIpsSrcFile(fileToBeCopied, targetIpsPackage, "Foo", null);
        assertTrue(copiedSrcFile.exists());

        assertEquals("Foo" + "." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension(), copiedSrcFile.getName());
        assertEquals(3, targetIpsPackage.getIpsSrcFiles().length);
    }

}
