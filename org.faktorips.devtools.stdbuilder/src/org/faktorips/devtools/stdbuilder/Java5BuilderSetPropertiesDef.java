/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An implementation of the {@link IIpsBuilderSetPropertyDef} interface specific for Java 5
 * properties. The default value for the enablement depends on the java project setting.
 * 
 * @author Peter Erzberger
 */
public class Java5BuilderSetPropertiesDef extends IpsBuilderSetPropertyDef {

    /**
     * Returns "true" if the java project setting are greater equals to 1.5 and false otherwise.
     */
    @Override
    public String getDefaultValue(IIpsProject ipsProject) {
        return Boolean.toString(ComplianceCheck.isComplianceLevelAtLeast5(ipsProject));
    }

    /**
     * Returns "false".
     */
    @Override
    public String getDisableValue(IIpsProject ipsProject) {
        return Boolean.toString(false);
    }

    /**
     * Returns <code>true</code> if the java project setting are greater equals to 1.5 and false
     * otherwise.
     */
    @Override
    public boolean isAvailable(IIpsProject ipsProject) {
        return ComplianceCheck.isComplianceLevelAtLeast5(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "boolean";
    }
}
