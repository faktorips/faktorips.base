/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    PROPERTY_TYPE_MISMATCH(Messages.DeltaType_propertiesWithTypeMismatch),
    VALUE_SET_MISMATCH(Messages.DeltaType_ValueSetMismatches),
    VALUE_HOLDER_MISMATCH(Messages.DeltaType_valueHolderMismatch),
    MULTILINGUAL_MISMATCH(Messages.DeltaType_multilingualMismatch),
    LINK_WITHOUT_ASSOCIATION(Messages.DeltaType_LinksNotFoundInTheModel),
    LINK_CHANGING_OVER_TIME_MISMATCH(Messages.DeltaType_LinksWithWrongParent);

    private final String description;

    private DeltaType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
