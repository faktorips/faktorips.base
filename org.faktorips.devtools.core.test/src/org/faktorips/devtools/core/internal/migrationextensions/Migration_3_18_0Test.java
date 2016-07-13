/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.migrationextensions;

import static org.faktorips.abstracttest.matcher.Matchers.isEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

public class Migration_3_18_0Test extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private Migration_3_18_0 migration;

    private void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        migration = (Migration_3_18_0)new Migration_3_18_0.Factory().createIpsProjectMigrationOpertation(ipsProject,
                "irrelevant");
    }

    @Test
    public void testMigrate_addsUniqueQualifierIfMoreThanOneSrcFolder() throws Exception {
        setUpMigration();
        newIpsPackageFragmentRoot(ipsProject, null, "secondRoot");

        MessageList messageList = migration.migrate(new NullProgressMonitor());

        assertThat(messageList, isEmpty());
        IIpsSrcFolderEntry[] sourceFolderEntries = ipsProject.getIpsObjectPath().getSourceFolderEntries();
        assertThat("first entry should not have unique qualifier", getUniqueQualifier(sourceFolderEntries[0]),
                is(equalTo("")));
        assertThat("second entry should have unique qualifier", getUniqueQualifier(sourceFolderEntries[1]),
                is(equalTo("secondRoot")));
    }

    @Test
    public void testMigrate_doesNotChangeSingleSrcFolder() throws Exception {
        setUpMigration();

        MessageList messageList = migration.migrate(new NullProgressMonitor());

        assertThat(messageList, isEmpty());
        IIpsSrcFolderEntry[] sourceFolderEntries = ipsProject.getIpsObjectPath().getSourceFolderEntries();
        assertThat("first entry should not have unique qualifier", getUniqueQualifier(sourceFolderEntries[0]),
                is(equalTo("")));
    }

    @Test
    public void testMigrate_RemovesDiscontinuedProperties() throws Exception {
        setUpMigration();
        IIpsProjectProperties properties = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel builderSetConfig = properties.getBuilderSetConfig();
        builderSetConfig.setPropertyValue("useJavaEnumTypes", "true", "irrelevant");
        builderSetConfig.setPropertyValue("useTypesafeCollections", "true", "irrelevant");
        ipsProject.setProperties(properties);

        MessageList messageList = migration.migrate(new NullProgressMonitor());

        assertThat(messageList, isEmpty());
        assertThat(ipsProject.getProperties().getBuilderSetConfig().getPropertyValue("useJavaEnumTypes"),
                is(nullValue()));
        assertThat(ipsProject.getProperties().getBuilderSetConfig().getPropertyValue("useTypesafeCollections"),
                is(nullValue()));
    }

    private String getUniqueQualifier(IIpsSrcFolderEntry ipsSrcFolderEntry) {
        String basePackageNameForDerivedJavaClasses = ipsSrcFolderEntry.getBasePackageNameForDerivedJavaClasses();
        String uniqueBasePackageNameForDerivedArtifacts = ipsSrcFolderEntry
                .getUniqueBasePackageNameForDerivedArtifacts();
        return uniqueBasePackageNameForDerivedArtifacts.replace(basePackageNameForDerivedJavaClasses, "");
    }

}
