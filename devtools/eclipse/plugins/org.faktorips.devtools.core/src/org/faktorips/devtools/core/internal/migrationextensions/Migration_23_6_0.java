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

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.settings.JaxbSupportVariant;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.model.versionmanager.options.IpsEnumMigrationOption;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.runtime.MessageList;

public class Migration_23_6_0 extends MarkAsDirtyMigration {

    private static final String VERSION_23_6_0 = "23.6.0"; //$NON-NLS-1$
    private static final String MIGRATION_OPTION_JAXB_VARIANT = "generateJaxbSupport"; //$NON-NLS-1$

    private final IpsMigrationOption<JaxbSupportVariant> jaxbVariantOption = new IpsEnumMigrationOption<>(
            MIGRATION_OPTION_JAXB_VARIANT,
            Messages.Migration_23_6_0_jaxbVariant,
            JaxbSupportVariant.ClassicJAXB,
            JaxbSupportVariant.class);

    public Migration_23_6_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                Set.of(IIpsModel.get().getIpsObjectTypes()),
                VERSION_23_6_0,
                Messages.Migration_23_6_0_description);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws InvocationTargetException {
        IIpsProject ipsProject = getIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();

        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel()
                .getIpsArtefactBuilderSetInfo(properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfigModel builderSetConfig = properties.getBuilderSetConfig();

        JaxbSupportVariant selectedValue = jaxbVariantOption.getSelectedValue();
        builderSetConfig.setPropertyValue(MIGRATION_OPTION_JAXB_VARIANT,
                selectedValue != null ? selectedValue.toString() : null,
                Messages.Migration_Option_JAXB_Variant_Description);

        MigrationUtil.updateAllIpsArtefactBuilderSetDescriptions(builderSetInfo, builderSetConfig);

        ipsProject.setProperties(properties);
        updateManifest();
        return super.migrate(monitor);
    }

    @Override
    public Collection<IpsMigrationOption<?>> getOptions() {
        return Collections.singleton(jaxbVariantOption);
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_23_6_0(ipsProject, featureId);
        }
    }
}
