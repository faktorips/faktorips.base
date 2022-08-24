/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.text.MessageFormat;

import org.faktorips.devtools.model.IIpsModelExtensions;

/**
 * Type of a delta.
 * 
 * @author Jan Ortmann
 */
public enum DeltaType {

    MISSING_PROPERTY_VALUE(Messages.DeltaType_missingValue, Kind.ADD),
    VALUE_WITHOUT_PROPERTY(Messages.DeltaType_propertiesNotFoundInTheModel, Kind.DELETE),
    PROPERTY_TYPE_MISMATCH(Messages.DeltaType_propertiesWithTypeMismatch, Kind.MODIFY),
    HIDDEN_ATTRIBUTE_MISMATCH(Messages.DeltaType_hiddenAttributeMismatch, Kind.MODIFY),
    VALUE_SET_MISMATCH(Messages.DeltaType_ValueSetMismatches, Kind.MODIFY),
    VALUE_HOLDER_MISMATCH(Messages.DeltaType_valueHolderMismatch, Kind.MODIFY),
    MULTILINGUAL_MISMATCH(Messages.DeltaType_multilingualMismatch, Kind.MODIFY),
    LINK_WITHOUT_ASSOCIATION(Messages.DeltaType_LinksNotFoundInTheModel, Kind.DELETE),
    LINK_CHANGING_OVER_TIME_MISMATCH(Messages.DeltaType_LinksWithWrongParent, Kind.MODIFY),
    MISSING_TEMPLATE_LINK(Messages.DeltaType_missingTemplateLink, Kind.ADD),
    REMOVED_TEMPLATE_LINK(Messages.DeltaType_removedTemplateLink, Kind.DELETE),
    INVALID_GENERATIONS(MessageFormat.format(Messages.DeltaType_invalidGenerations,
            IIpsModelExtensions.get().getModelPreferences().getChangesOverTimeNamingConvention()
                    .getGenerationConceptNamePlural(true)),
            Kind.DELETE),
    DATATYPE_MISMATCH(Messages.DeltaType_datatypeMissmatch, Kind.MODIFY);

    private final String description;

    private final Kind kind;

    DeltaType(String description, Kind kind) {
        this.description = description;
        this.kind = kind;
    }

    public String getDescription() {
        return description;
    }

    public Kind getKind() {
        return kind;
    }

    public static enum Kind {

        /**
         * Indicate that something will be added after fix
         */
        ADD,

        /**
         * Indicate that something will be deleted after fix
         */
        DELETE,

        /**
         * Indicate that some modification will be performed after fix
         */
        MODIFY;

    }

}
