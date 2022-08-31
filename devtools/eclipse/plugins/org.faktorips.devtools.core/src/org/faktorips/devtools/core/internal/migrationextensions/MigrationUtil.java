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

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.annotation.UtilityClass;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;

/**
 * Utility class for common migration steps.
 */
@UtilityClass
public class MigrationUtil {

    private MigrationUtil() {
        // do not instantiate
    }

    /**
     * This migration simply updates the .ipsproject Property file with the default values for the
     * {@link IIpsArtefactBuilderSetConfigModel}.
     */
    public static void updateBuilderSetDefaults(IIpsProject ipsProject) {
        IIpsProjectProperties properties = ipsProject.getProperties();
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel()
                .getIpsArtefactBuilderSetInfo(properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfigModel builderSetConfig = properties.getBuilderSetConfig();
        for (IIpsBuilderSetPropertyDef propertyDef : builderSetInfo.getPropertyDefinitions()) {
            String value = builderSetConfig.getPropertyValue(propertyDef.getName());
            if (propertyDef.isAvailable(ipsProject) && null == value) {
                // we use the disabled value here, because that value is used when the property is
                // not set
                value = propertyDef.getDisableValue(ipsProject);
                if (value == null) {
                    value = propertyDef.getDefaultValue(ipsProject);
                }
                builderSetConfig.setPropertyValue(propertyDef.getName(), value, propertyDef.getDescription());
            }
        }
        updateMissingDescriptions(builderSetInfo, builderSetConfig);
        ipsProject.setProperties(properties);
    }

    private static void updateMissingDescriptions(IIpsArtefactBuilderSetInfo builderSetInfo,
            IIpsArtefactBuilderSetConfigModel builderSetConfig) {
        for (String property : builderSetConfig.getPropertyNames()) {
            String propertyDescription = builderSetConfig.getPropertyDescription(property);
            if (IpsStringUtils.isBlank(propertyDescription)) {
                String newDescription = builderSetInfo.getPropertyDefinition(property).getDescription();
                builderSetConfig.setPropertyValue(property, builderSetConfig.getPropertyValue(property),
                        newDescription);
            }
        }
    }

    /**
     * This migration refreshes the {@link IIpsArtefactBuilderSetConfigModel} section of the
     * .ipsproject Property file with the default descriptions.
     *
     * @param builderSetInfo the configured IpsArtefactBuilderSetInfo for the project
     * @param builderSetConfig the configured IpsArtefactBuilderSetConfigModel for the project
     */
    public static void updateAllIpsArtefactBuilderSetDescriptions(IIpsArtefactBuilderSetInfo builderSetInfo,
            IIpsArtefactBuilderSetConfigModel builderSetConfig) {
        for (String property : builderSetConfig.getPropertyNames()) {
            String newDescription = builderSetInfo.getPropertyDefinition(property).getDescription();
            builderSetConfig.setPropertyValue(property, builderSetConfig.getPropertyValue(property), newDescription);
        }
    }
}
