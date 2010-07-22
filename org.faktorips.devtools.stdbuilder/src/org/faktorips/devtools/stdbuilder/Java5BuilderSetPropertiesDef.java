/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
