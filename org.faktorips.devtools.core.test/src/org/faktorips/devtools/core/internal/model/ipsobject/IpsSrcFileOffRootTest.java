/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsobject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsSrcFileOffRootTest extends AbstractIpsPluginTest {

    private IpsSrcFileOffRoot srcFileOffRoot;
    private IFile file;
    private IIpsSrcFile ipsSrcFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Test");
        ipsSrcFile = productCmptType.getIpsSrcFile();
        IFolder folder = ipsProject.getProject().getFolder("test");
        folder.create(true, true, null);
        ipsSrcFile.getCorrespondingFile().copy(folder.getFullPath().append(ipsSrcFile.getName()), true, null);
        file = folder.getFile(ipsSrcFile.getName());
        srcFileOffRoot = new IpsSrcFileOffRoot(file);
    }

    @Test
    public void testIsMutable() throws Exception {
        assertThat(srcFileOffRoot.isMutable(), is(false));
    }

    @Test
    public void testGetCorrespondingFile() throws Exception {
        assertThat(srcFileOffRoot.getCorrespondingFile(), is(file));
        assertThat(srcFileOffRoot.getCorrespondingResource(), is((IResource)file));
    }

    @Test
    public void testIsContainedInIpsRoot() throws Exception {
        assertThat(srcFileOffRoot.isContainedInIpsRoot(), is(false));
    }

    @Test
    public void testSameContent() throws Exception {
        assertThat(isEqualContent(), is(true));
    }

    public boolean isEqualContent() throws CoreException, IOException {
        InputStream i1 = ipsSrcFile.getContentFromEnclosingResource();
        InputStream i2 = srcFileOffRoot.getContentFromEnclosingResource();
        byte[] buf1 = new byte[64 * 1024];
        byte[] buf2 = new byte[64 * 1024];
        try {
            DataInputStream d2 = new DataInputStream(i2);
            try {
                int len;
                while ((len = i1.read(buf1)) > 0) {
                    d2.readFully(buf2, 0, len);
                    for (int i = 0; i < len; i++) {
                        if (buf1[i] != buf2[i]) {
                            return false;
                        }
                    }
                }
                return d2.read() < 0; // is the end of the second file also.
            } finally {
                d2.close();
            }
        } catch (EOFException ioe) {
            return false;
        } finally {
            i1.close();
            i2.close();
        }
    }

}
