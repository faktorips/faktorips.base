/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

/**
 * Type of a delta.
 * 
 * @author Jan Ortmann
 */
public enum DeltaType {

    MISSING_PROPERTY_VALUE(Messages.DeltaType_missingValue),
    VALUE_WITHOUT_PROPERTY(Messages.DeltaType_propertiesNotFoundInTheModel),
    VALUE_SET_MISMATCH(Messages.DeltaType_ValueSetMismatches),
    PROPERTY_TYPE_MISMATCH(Messages.DeltaType_propertiesWithTypeMismatch),
    LINK_WITHOUT_ASSOCIATION(Messages.DeltaType_LinksNotFoundInTheModel);

    private final String description;

    public String getDescription() {
        return description;
    }

    private DeltaType(String description) {
        this.description = description;
    }

}
