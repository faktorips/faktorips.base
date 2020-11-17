/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import org.faktorips.devtools.model.productcmpttype.AggregationKind;

/**
 * Describes the kind of relation, aggregation or association.
 */
public enum AssociationType {

    COMPOSITION_MASTER_TO_DETAIL("comp", Messages.AssociationType_label_composition_master_to_detail), //$NON-NLS-1$

    COMPOSITION_DETAIL_TO_MASTER("reverseComp", Messages.AssociationType_label_composition_detail_to_master), //$NON-NLS-1$

    ASSOCIATION("ass", Messages.AssociationType_label_association), //$NON-NLS-1$

    AGGREGATION("aggr", Messages.AssociationType_label_aggregation); //$NON-NLS-1$

    private final String id;
    private final String name;

    public static final AssociationType getRelationType(String id) {
        if (id.equals("agg")) { //$NON-NLS-1$
            // Renamed aggregation to composition (including the id!)
            return COMPOSITION_MASTER_TO_DETAIL;
        }
        for (AssociationType at : values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    /**
     * Returns true if this type is either {@link #COMPOSITION_MASTER_TO_DETAIL} or
     * {@link #AGGREGATION};
     * 
     */
    public boolean isMasterToDetail() {
        return this == COMPOSITION_MASTER_TO_DETAIL || this == AGGREGATION;
    }

    public boolean isCompositionMasterToDetail() {
        return this == COMPOSITION_MASTER_TO_DETAIL;
    }

    public boolean isCompositionDetailToMaster() {
        return this == COMPOSITION_DETAIL_TO_MASTER;
    }

    public boolean isAssoziation() {
        return this == ASSOCIATION;
    }

    public AggregationKind getAggregationKind() {
        if (isCompositionMasterToDetail()) {
            return AggregationKind.COMPOSITE;
        } else if (this == AGGREGATION) {
            return AggregationKind.SHARED;
        }
        return AggregationKind.NONE;
    }

    private AssociationType(String id, String name) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns the corresponding association type:<br>
     * <ul>
     * <li>ASSOZIATION => out: ASSOZIATION
     * <li>COMPOSITION_MASTER_TO_DETAIL => out: COMPOSITION_DETAIL_TO_MASTER
     * <li>COMPOSITION_DETAIL_TO_MASTER => out: COMPOSITION_MASTER_TO_DETAIL
     * </ul>
     * Returns <code>null</code> if no corresponding type exists.
     */
    public AssociationType getCorrespondingAssociationType() {
        return isAssoziation() ? AssociationType.ASSOCIATION
                : isCompositionDetailToMaster() ? AssociationType.COMPOSITION_MASTER_TO_DETAIL
                        : isCompositionMasterToDetail() ? AssociationType.COMPOSITION_DETAIL_TO_MASTER : null;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

}
