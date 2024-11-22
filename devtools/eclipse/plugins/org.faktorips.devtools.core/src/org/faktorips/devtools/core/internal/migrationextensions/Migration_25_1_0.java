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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.model.versionmanager.options.IpsBooleanMigrationOption;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;

public class Migration_25_1_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "25.1.0"; //$NON-NLS-1$
    private static final String MIGRATION_OPTION_ALL_ENUM_ATTRIBUTES_MANDATORY = "AllEnumAttributesMandatory"; //$NON-NLS-1$

    private final IpsMigrationOption<Boolean> enumAttributesMandatoryOption = new IpsBooleanMigrationOption(
            MIGRATION_OPTION_ALL_ENUM_ATTRIBUTES_MANDATORY,
            Messages.Migration_25_1_0_mandatoryEnumAttributes,
            false);

    public Migration_25_1_0(IIpsProject projectToMigrate, String featureId) {
        this(projectToMigrate, featureId, VERSION);
    }

    Migration_25_1_0(IIpsProject projectToMigrate, String featureId, String migrationVersion) {
        super(projectToMigrate,
                featureId,
                Set.of(IIpsModel.get().getIpsObjectTypes()),
                migrationVersion,
                Messages.Migration_25_1_0_description);
    }

    @Override
    public Collection<IpsMigrationOption<?>> getOptions() {
        return List.of(enumAttributesMandatoryOption);
    }

    void setAllEnumAttributesMandatory(boolean allEnumAttributesMandatory) {
        enumAttributesMandatoryOption.setSelectedValue(allEnumAttributesMandatory);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) {
        super.migrate(srcFile);
        if (enumAttributesMandatoryOption.getSelectedValue()
                && IpsObjectType.ENUM_TYPE.equals(srcFile.getIpsObjectType())) {
            IEnumType enumType = (IEnumType)srcFile.getIpsObject();
            enumType.getEnumAttributes(false).forEach(a -> a.setMandatory(true));
        }
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_25_1_0(ipsProject, featureId);
        }
    }
}
