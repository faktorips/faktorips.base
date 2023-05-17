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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.eclipse.util.EclipseProjectUtil;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.pctype.CamelCaseToUpperUnderscoreColumnNamingStrategy;
import org.faktorips.devtools.model.internal.pctype.CamelCaseToUpperUnderscoreTableNamingStrategy;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class Migration_21_6_0Test extends AbstractIpsPluginTest {

    @Test
    public void testMigrate_TooOld() throws Exception {
        IProject project = newPlatformProject("TooOldIpsProject").unwrap();
        addJavaCapabilities(project);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project");
        copy(project, ".classpath");
        copy(project, ".ipsproject");
        NullProgressMonitor monitor = new NullProgressMonitor();
        Migration_21_6_0.update(project.getFile(".ipsproject"), c -> c.replace("20.12.0", "19.12.0"), monitor);
        IIpsProject ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));
        assertFalse(EclipseProjectUtil.hasIpsNature(project));
        Migration_21_6_0 migration_21_6_0 = new Migration_21_6_0(ipsProject, "irrelevant");

        MessageList messageList = migration_21_6_0.canMigrate();

        assertFalse(messageList.isEmpty());
        assertTrue(messageList.containsErrorMsg());
        assertFalse(EclipseProjectUtil.hasIpsNature(project));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testMigrate() throws Exception {
        IProject project = newPlatformProject("OldIpsProject").unwrap();
        addJavaCapabilities(project);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project");
        copy(project, ".classpath");
        copy(project, ".ipsproject");
        IIpsProject ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));
        assertFalse(EclipseProjectUtil.hasIpsNature(project));
        Migration_21_6_0 migration_21_6_0 = new Migration_21_6_0(ipsProject, "irrelevant");

        try {
            MessageList messageList = migration_21_6_0.migrate(new NullProgressMonitor());

            assertTrue(messageList.isEmpty());
            assertTrue(project.getDescription().hasNature(IpsProject.OLD_NATURE_ID));
            IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();
            assertThat(ipsProjectProperties.getProductCmptNamingStrategy(),
                    is(instanceOf(DateBasedProductCmptNamingStrategy.class)));
            assertThat(ipsProjectProperties.getPersistenceOptions().getTableColumnNamingStrategy(),
                    is(instanceOf(CamelCaseToUpperUnderscoreColumnNamingStrategy.class)));
            assertThat(ipsProjectProperties.getPersistenceOptions().getTableNamingStrategy(),
                    is(instanceOf(CamelCaseToUpperUnderscoreTableNamingStrategy.class)));
        } catch (NullPointerException npe) {
            npe.printStackTrace(System.err);
            assertEquals("", Arrays.stream(npe.getStackTrace()).map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
    }

    private void copy(IProject platformProject, String fileName) throws CoreException {
        IFile file = platformProject.getFile(fileName);
        InputStream inputStream = getClass().getResourceAsStream(getClass().getSimpleName() + fileName);
        if (file.exists()) {
            file.setContents(inputStream, true, true, null);
        } else {
            file.create(inputStream, true, null);
        }
    }

}
