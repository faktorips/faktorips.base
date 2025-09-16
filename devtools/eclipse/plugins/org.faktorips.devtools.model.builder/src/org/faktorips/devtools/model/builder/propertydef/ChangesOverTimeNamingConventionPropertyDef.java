/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.propertydef;

import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.internal.ipsproject.ChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.runtime.Message;

/**
 * Property Definition that mirrors the {@link ChangesOverTimeNamingConvention} setting from the
 * {@link IIpsProjectProperties} as a builder property so that all settings relevant to the builder
 * can be accessed in one place.
 */
public class ChangesOverTimeNamingConventionPropertyDef extends IpsBuilderSetPropertyDef {

    public static final String MSG_CODE_DERIVED_PROPERTY_SET_MANUALLY = "derivedPropertySetManually"; //$NON-NLS-1$
    public static final String CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION = JavaBuilderSet.CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION;

    /**
     * Always returns <code>false</code>, because the property can not be set manually as it is
     * mirrored from
     * {@link IIpsProjectProperties#getChangesOverTimeNamingConventionIdForGeneratedCode()}.
     */
    @Override
    public boolean isAvailable(IIpsProject ipsProject) {
        return false;
    }

    @Override
    public String getDefaultValue(IIpsProject ipsProject) {
        return getValueFromIpsProject(ipsProject);
    }

    @Override
    public String getDisableValue(IIpsProject ipsProject) {
        return getValueFromIpsProject(ipsProject);
    }

    private String getValueFromIpsProject(IIpsProject ipsProject) {
        return ipsProject.getReadOnlyProperties().getChangesOverTimeNamingConventionIdForGeneratedCode();
    }

    @Override
    public Message validateValue(IIpsProject ipsProject, String value) {
        return Message.newWarning(MSG_CODE_DERIVED_PROPERTY_SET_MANUALLY,
                Messages.bind(Messages.ChangesOverTimeNamingConventionPropertyDef_msgDerivedPropertySetManually,
                        CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION, value,
                        getValueFromIpsProject(ipsProject)));
    }

}
