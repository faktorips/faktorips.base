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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.util.Locale;

import org.eclipse.core.runtime.ICoreRunnable;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.eclipse.internal.EclipseImplementation;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test for threading issues (we hopefully once HAD).
 * 
 * @author Jan Ortmann
 */
public class ModelManagementTest extends AbstractIpsPluginTest {

    private IPolicyCmptType type;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ICoreRunnable action = $ -> {
            IIpsProject project = newIpsProject("TestProject");
            AFolder folder = (AFolder)project.getIpsPackageFragmentRoots()[0].getCorrespondingResource();
            folder.getFile("A.ipspct");
            type = newPolicyCmptType(project, "A");
            type.newPolicyCmptTypeAssociation();
            type.getIpsSrcFile().save(true, null);
        };
        Abstractions.getWorkspace().run(action, null);
    }

    @Test
    public void test1() {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Start test1() =====");
        }
        assertEquals(1, type.getNumOfAssociations());
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Finished test1() =====");
        }
    }

    @Test
    public void test2() {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Start test2() =====");
        }
        assertEquals(1, type.getNumOfAssociations());
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Finished test2() =====");
        }
    }

    @Test
    public void testDirectChangesToTheCorrespondingFile() throws Exception {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Start testDirectChangesToTheCorrespondingFile() =====");
        }
        IIpsSrcFile ipsFile = type.getIpsSrcFile();
        IDescription description = type.newDescription();
        description.setLocale(Locale.GERMAN);
        description.setText("Blabla");
        ipsFile.save(true, null);
        String encoding = type.getIpsProject().getXmlFileCharset();
        AFile file = type.getIpsSrcFile().getCorrespondingFile();
        String content = StringUtil.readFromInputStream(file.getContents(), encoding);
        content = content.replaceAll("Blabla", "NewBlabla");
        file.setContents(StringUtil.getInputStreamForString(content, encoding), false, null);
        type = (IPolicyCmptType)ipsFile.getIpsObject(); // forces a reload
        assertEquals("NewBlabla", description.getText());
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Finished testDirectChangesToTheCorrespondingFile() =====");
        }
    }

    @Category(EclipseImplementation.class)
    @Test
    public void testChangeDirectlyOnDiskWithoutUsingTheEclipseApi() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("===== Start testChangeDirectlyOnDiskWithoutUsingTheEclipseApi() =====");
            }
            IIpsSrcFile ipsFile = type.getIpsSrcFile();
            IDescription description = type.newDescription();
            description.setLocale(Locale.GERMAN);
            description.setText("Blabla");
            ipsFile.save(true, null);
            Thread.sleep(2000); // wait for 2 seconds, so that the file definitly has a
            // different timestamp, otherwise refreshLocal won't refresh!
            // file timestamps (at least under windows xp) only differ in seconds, not milliseconds!
            String encoding = type.getIpsProject().getXmlFileCharset();
            AFile file = type.getIpsSrcFile().getCorrespondingFile();
            String content = StringUtil.readFromInputStream(file.getContents(), encoding);
            content = content.replaceAll("Blabla", "NewBlabla");
            File ioFile = file.getLocation().toFile();
            FileWriter writer = new FileWriter(ioFile);
            writer.write(content);
            writer.flush();
            writer.close();

            // before the refresh, object shouldn't be changed
            type = (IPolicyCmptType)ipsFile.getIpsObject();
            assertEquals("Blabla", description.getText());

            // now refresh the file from disk
            file.refreshLocal(AResourceTreeTraversalDepth.INFINITE, null);

            type = (IPolicyCmptType)ipsFile.getIpsObject(); // forces a reload
            assertEquals("NewBlabla", description.getText());
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("===== Finished testChangeDirectlyOnDiskWithoutUsingTheEclipseApi() =====");
            }
        }
    }
}
