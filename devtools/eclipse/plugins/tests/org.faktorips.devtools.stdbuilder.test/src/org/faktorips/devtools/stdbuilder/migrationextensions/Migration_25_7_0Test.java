/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.migrationextensions;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.migrationextensions.Migration_25_7_0;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Test;

public class Migration_25_7_0Test extends AbstractStdBuilderTest {

    @SuppressWarnings({ "deprecation" })
    @Test
    public void testMigrate_Manifest() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_25_7_0Test_MF");
        IProject project = ipsProject.getProject().unwrap();
        copy("ipsproject", project);
        project.getFile(".ipsproject").delete(true, null);
        project.getFile("ipsproject").move(Path.fromPortableString(".ipsproject"), true, null);
        IFolder metaInf = project.getFolder("META-INF");
        metaInf.create(true, true, null);
        IFile manifest = copy("MANIFEST.MF", metaInf);

        IpsModel.reInit();
        ipsProject = IIpsModel.get().getIpsProject("Migration_25_7_0Test_MF");

        Migration_25_7_0 migration = new Migration_25_7_0(ipsProject, "irrelevant");

        migration.migrate(new NullProgressMonitor());

        String manifestContent = Files.readString(manifest.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
        assertThat(manifestContent, containsString("Fips-RuntimeIdPrefix: migration25.7."));
    }

    private IFile copy(String fileName, IContainer container) throws CoreException {
        IFile file = container.getFile(new Path(fileName));
        InputStream inputStream = getClass().getResourceAsStream(getClass().getSimpleName() + '.' + fileName);
        if (file.exists()) {
            file.setContents(inputStream, true, true, null);
        } else {
            file.create(inputStream, true, null);
        }
        return file;
    }

}
