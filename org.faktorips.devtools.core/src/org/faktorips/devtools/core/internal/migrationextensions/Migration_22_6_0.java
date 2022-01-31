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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.settings.ValueSetMethods;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.model.versionmanager.options.IpsEnumMigrationOption;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.runtime.MessageList;

public class Migration_22_6_0 extends MarkAsDirtyMigration {

    private static final String VERSION_22_6_0 = "22.6.0"; //$NON-NLS-1$
    private static final String MIGRATION_OPTION_UNIFY_VALUE_SET = "valueSetMethods"; //$NON-NLS-1$

    private final IpsMigrationOption<ValueSetMethods> valueSetMethodsOption = new IpsEnumMigrationOption<>(
            MIGRATION_OPTION_UNIFY_VALUE_SET,
            Messages.Migration_22_6_0_unifyValueSet,
            ValueSetMethods.ByValueSetType,
            ValueSetMethods.class);

    Migration_22_6_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                getAffectedIpsObjectTypes(projectToMigrate),
                VERSION_22_6_0,
                Messages.Migration_22_6_0_description);
    }

    private static Set<IpsObjectType> getAffectedIpsObjectTypes(IIpsProject ipsProject) {
        if (ipsProject.getProperties().isValidateIpsSchema()) {
            return Set.of(IIpsModel.get().getIpsObjectTypes());
        }
        return Set.of(IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        IIpsProject ipsProject = getIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();

        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel()
                .getIpsArtefactBuilderSetInfo(properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfigModel builderSetConfig = properties.getBuilderSetConfig();

        ValueSetMethods selectedValue = valueSetMethodsOption.getSelectedValue();
        builderSetConfig.setPropertyValue(MIGRATION_OPTION_UNIFY_VALUE_SET,
                selectedValue != null ? selectedValue.toString() : null,
                Messages.Migration_Option_Unify_Value_Set_Description);

        MigrationUtil.updateAllIpsArtefactBuilderSetDescriptions(builderSetInfo, builderSetConfig);

        ipsProject.setProperties(properties);
        updateManifest();
        return super.migrate(monitor);
    }

    private void updateManifest() {
        IIpsProject ipsProject = getIpsProject();
        IFile manifestFile = ipsProject.getProject().getFile(IpsBundleManifest.MANIFEST_NAME);
        if (manifestFile.exists()) {
            try {
                Manifest manifest = new Manifest(manifestFile.getContents());
                IpsBundleManifest ipsBundleManifest = new IpsBundleManifest(manifest);
                ipsBundleManifest.writeBuilderSettings(ipsProject);
            } catch (IOException | CoreException e) {
                throw new CoreRuntimeException(new IpsStatus("Can't read " + manifestFile, e)); //$NON-NLS-1$
            }

        }
    }

    @Override
    public Collection<IpsMigrationOption<?>> getOptions() {
        return Collections.singleton(valueSetMethodsOption);
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_22_6_0(ipsProject, featureId);
        }
    }
}
