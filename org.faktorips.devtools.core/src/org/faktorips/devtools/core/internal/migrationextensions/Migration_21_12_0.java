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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.model.versionmanager.options.IpsBooleanMigrationOption;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.runtime.MessageList;

public class Migration_21_12_0 extends MarkAsDirtyMigration {

    private static final String VERSION_21_12 = "21.12.0"; //$NON-NLS-1$
    private static final String MIGRATION_OPTION_ADD_SCHEMAS = "AddSchemas"; //$NON-NLS-1$

    private final IpsMigrationOption<Boolean> addSchemasOption = new IpsBooleanMigrationOption(
            MIGRATION_OPTION_ADD_SCHEMAS,
            Messages.Migration_21_12_0_AddSchemas,
            false);

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
    public MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException {
        IIpsProject ipsProject = getIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setValidateIpsSchema(addSchemasOption.getSelectedValue());

        MigrationUtil.updateAllIpsArtefactBuilderSetDescriptions(
                ipsProject.getIpsModel().getIpsArtefactBuilderSetInfo(properties.getBuilderSetId()),
                properties.getBuilderSetConfig());

        ipsProject.setProperties(properties);

        return super.migrate(monitor);
    }

    @Override
    public Collection<IpsMigrationOption<?>> getOptions() {
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
