/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class Migration_25_7_0Test extends AbstractIpsPluginTest {

    @Test
    public void testMigrate() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_25_7_0Test");
        IProject project = ipsProject.getProject().unwrap();
        project.getFile(".ipsproject").delete(true, null);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project");
        copy(project, ".classpath");
        copy(project, ".ipsproject");
        IFolder metaInf = project.getFolder("META-INF");
        metaInf.create(true, true, null);
        copy(metaInf, "MANIFEST.MF");
        ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));

        Migration_25_7_0 migration = new Migration_25_7_0(ipsProject, "irrelevant");

        MessageList messageList = migration.canMigrate();

        assertThat(messageList.containsErrorMsg(), is(false));
        assertThat(migration.getTargetVersion().contains("25.7"), is(true));
    }

    private IFile copy(IContainer container, String fileName) throws CoreException {
        return copy(container, fileName, fileName);
    }

    private IFile copy(IContainer container, String inputFileName, String outputFileName) throws CoreException {
        IFile file = container.getFile(new Path(outputFileName));
        InputStream inputStream = getClass().getResourceAsStream(getClass().getSimpleName() + inputFileName);
        if (file.exists()) {
            file.setContents(inputStream, true, true, null);
        } else {
            file.create(inputStream, true, null);
        }
        return file;
    }
}
