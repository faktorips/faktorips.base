/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.util.Locale;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ICoreRunnable;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

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
            IDescription description = type.newDescription();
            description.setLocale(Locale.US);
            type.getIpsSrcFile().save(null);
        };
        ResourcesPlugin.getWorkspace().run(action, null);
    }

    /**
     * Same as above but with an open editor on the file.
     */
    @Test
    public void testChangeDirectlyOnDiskWithoutUsingTheEclipseApiAndOpenEditor() throws Exception {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Start testChangeDirectlyOnDiskWithoutUsingTheEclipseApiAndOpenEditor() =====");
        }
        IpsUIPlugin.getDefault().openEditor(type);
        IIpsSrcFile ipsFile = type.getIpsSrcFile();
        type.setDescriptionText(Locale.US, "Blabla");
        ipsFile.save(null);
        Thread.sleep(2000); // wait for 2 seconds, so that the file definitely has a
        // different timestamp, otherwise refreshLocal won't refresh!
        // file timestamps (at least under windows xp) only differ in seconds, not milliseconds!
        String encoding = type.getIpsProject().getXmlFileCharset();
        AFile file = type.getIpsSrcFile().getCorrespondingFile();
        String content = StringUtil.readFromInputStream(file.getContents(), encoding);
        content = content.replace("Blabla", "NewBlabla");
        File ioFile = file.getLocation().toFile();
        FileWriter writer = new FileWriter(ioFile);
        writer.write(content);
        writer.flush();
        writer.close();

        // now refresh the file from disk
        file.refreshLocal(AResourceTreeTraversalDepth.INFINITE, null);

        type = (IPolicyCmptType)ipsFile.getIpsObject(); // forces a reload
        assertEquals("NewBlabla", type.getDescriptionText(Locale.US));
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Finished testChangeDirectlyOnDiskWithoutUsingTheEclipseApiAndOpenEditor() =====");
        }
    }
}
