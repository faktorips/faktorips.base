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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsSrcFileOffRootTest extends AbstractIpsPluginTest {

    private IpsSrcFileOffRoot srcFileOffRoot;
    private AFile file;
    private IIpsSrcFile ipsSrcFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject ipsProject = newIpsProject();
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "Test");
        ipsSrcFile = productCmptType.getIpsSrcFile();
        AFolder folder = ipsProject.getProject().getFolder("test");
        folder.create(null);
        ipsSrcFile.getCorrespondingFile().copy(folder.getWorkspaceRelativePath().resolve(ipsSrcFile.getName()), null);
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
        assertThat(srcFileOffRoot.getCorrespondingResource(), is((AResource)file));
    }

    @Test
    public void testIsContainedInIpsRoot() throws Exception {
        assertThat(srcFileOffRoot.isContainedInIpsRoot(), is(false));
    }

    @Test
    public void testSameContent() throws Exception {
        assertThat(isEqualContent(), is(true));
    }

    public boolean isEqualContent() throws IpsException, IOException {
        InputStream i1 = ipsSrcFile.getContentFromEnclosingResource();
        InputStream i2 = srcFileOffRoot.getContentFromEnclosingResource();
        byte[] buf1 = new byte[64 * 1024];
        byte[] buf2 = new byte[64 * 1024];
        try {
            DataInputStream d2 = new DataInputStream(i2);
            try (d2) {
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
            }
        } catch (EOFException ioe) {
            return false;
        } finally {
            i1.close();
            i2.close();
        }
    }

}
