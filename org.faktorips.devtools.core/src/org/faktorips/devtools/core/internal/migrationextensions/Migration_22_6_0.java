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
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.settings.UnifyValueSetMethods;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.model.versionmanager.options.IpsEnumMigrationOption;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.runtime.MessageList;

public class Migration_22_6_0 extends MarkAsDirtyMigration {

    private static final String VERSION_22_6_0 = "22.6.0"; //$NON-NLS-1$
    private static final String MIGRATION_OPTION_UNIFY_VALUE_SET = "unifyValueSetMethods"; //$NON-NLS-1$

    private final IpsMigrationOption<UnifyValueSetMethods> unifyValueSetMethodsOption = new IpsEnumMigrationOption<>(
            MIGRATION_OPTION_UNIFY_VALUE_SET,
            Messages.Migration_22_6_0_unifyValueSet,
            UnifyValueSetMethods.OldMethods,
            UnifyValueSetMethods.class);

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

        UnifyValueSetMethods selectedValue = unifyValueSetMethodsOption.getSelectedValue();
        builderSetConfig.setPropertyValue(MIGRATION_OPTION_UNIFY_VALUE_SET,
                selectedValue != null ? selectedValue.toString() : null,
                Messages.Migration_Option_Unify_Value_Set_Description);

        MigrationUtil.updateAllIpsArtefactBuilderSetDescriptions(builderSetInfo, builderSetConfig);

        ipsProject.setProperties(properties);
        return super.migrate(monitor);
    }

    @Override
    public Collection<IpsMigrationOption<?>> getOptions() {
        return Collections.singleton(unifyValueSetMethodsOption);
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_22_6_0(ipsProject, featureId);
        }
    }
}
