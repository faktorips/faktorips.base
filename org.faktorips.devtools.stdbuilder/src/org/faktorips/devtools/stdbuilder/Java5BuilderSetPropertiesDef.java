/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An implementation of the {@link IIpsBuilderSetPropertyDef} interface specific for Java 5 properties. The default value for the enablement
 * depends on the java project setting.
 * 
 * @author Peter Erzberger
 */
public class Java5BuilderSetPropertiesDef extends IpsBuilderSetPropertyDef {

    /**
     * Returns "true" if the java project setting are greater equals to 1.5 and false otherwise.
     */
    public String getDefaultValue(IIpsProject ipsProject) {
        return Boolean.toString(ComplianceCheck.isComplianceLevelAtLeast5(ipsProject));
    }

    /**
     * Returns "false".
     */
    public String getDisableValue(IIpsProject ipsProject) {
        return Boolean.toString(false);
    }

    /**
     * Returns <code>true</code> if the java project setting are greater equals to 1.5 and false otherwise.
     */
    public boolean isAvailable(IIpsProject ipsProject) {
        return ComplianceCheck.isComplianceLevelAtLeast5(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return "boolean";
    }
}
