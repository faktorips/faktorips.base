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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.model.versionmanager.IpsMigrationOption;
import org.faktorips.runtime.MessageList;

public class Migration_21_12_0 extends MarkAsDirtyMigration {

    private static final String VERSION_21_12 = "21.12.0"; //$NON-NLS-1$
    private static final String MIGRATION_OPTION_ADD_SCHEMAS = "AddSchemas"; //$NON-NLS-1$

    private final IpsMigrationOption addSchemasOption = new IpsMigrationOption(MIGRATION_OPTION_ADD_SCHEMAS,
            Messages.Migration_21_12_0_AddSchemas, false);

    public Migration_21_12_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                new LinkedHashSet<>(Arrays.asList(IIpsModel.get().getIpsObjectTypes())),
                VERSION_21_12,
                Messages.Migration_21_12_0_description);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreRuntimeException, InvocationTargetException {
        IIpsProject ipsProject = getIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setValidateIpsSchema(addSchemasOption.isActive());
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel()
                .getIpsArtefactBuilderSetInfo(properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfigModel builderSetConfig = properties.getBuilderSetConfig();
        updateDescriptions(builderSetInfo, builderSetConfig);
        ipsProject.setProperties(properties);

        return super.migrate(monitor);
    }

    private static void updateDescriptions(IIpsArtefactBuilderSetInfo builderSetInfo,
            IIpsArtefactBuilderSetConfigModel builderSetConfig) {
        for (String property : builderSetConfig.getPropertyNames()) {
            String newDescription = builderSetInfo.getPropertyDefinition(property).getDescription();
            builderSetConfig.setPropertyValue(property, builderSetConfig.getPropertyValue(property), newDescription);
        }
    }

    @Override
    public Collection<IpsMigrationOption> getOptions() {
        return Collections.singleton(addSchemasOption);
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_21_12_0(ipsProject, featureId);
        }
    }
}
