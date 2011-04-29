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

package org.faktorips.devtools.core.internal.model.type;

import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;

/**
 * Describes the kind of relation, aggregation or association.
 */
public enum AssociationType {

    COMPOSITION_MASTER_TO_DETAIL("comp", "Composition (Master to Detail)"), //$NON-NLS-1$//$NON-NLS-2$

    COMPOSITION_DETAIL_TO_MASTER("reverseComp", "Composition (Detail to Master)"), //$NON-NLS-1$//$NON-NLS-2$

    ASSOCIATION("ass", "Association"), //$NON-NLS-1$//$NON-NLS-2$

    AGGREGATION("aggr", "Aggregation"); //$NON-NLS-1$//$NON-NLS-2$

    private final String id;
    private final String name;

    public final static AssociationType getRelationType(String id) {
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
