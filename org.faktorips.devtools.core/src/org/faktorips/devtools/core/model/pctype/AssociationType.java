/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;

import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

/**
 * Describes the kind of relation, aggregation or assoziation. 
 */
public class AssociationType extends DefaultEnumValue {
    
    public final static AssociationType COMPOSITION_MASTER_TO_DETAIL;

    public final static AssociationType COMPOSITION_DETAIL_TO_MASTER;
    
    public final static AssociationType AGGREGATION;

    public final static AssociationType ASSOCIATION;
    
    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("RelationType", AssociationType.class); //$NON-NLS-1$
        COMPOSITION_MASTER_TO_DETAIL = new AssociationType(enumType, "comp", "Composition (Master to Detail)", "AssociationType-Composition.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        COMPOSITION_DETAIL_TO_MASTER = new AssociationType(enumType, "reverseComp", "Composition (Detail to Master)", "AssociationType-CompositionDetailToMaster.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ASSOCIATION = new AssociationType(enumType, "ass", "Association", "AssociationType-Association.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AGGREGATION = new AssociationType(enumType, "aggr", "Aggregation", "AssociationType-Aggregation.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    public final static AssociationType getRelationType(int i) {
    	return (AssociationType)enumType.getEnumValue(i);
    }
    
    public final static AssociationType getRelationType(String id) {
        if (id.equals("agg")) { //$NON-NLS-1$
            // renamed aggregation to composition (inkl the id!)
            return COMPOSITION_MASTER_TO_DETAIL;
        }
        return (AssociationType)enumType.getEnumValue(id);
    }
    
    private String imageName;
    
    public boolean isCompositionMasterToDetail() {
        return this==COMPOSITION_MASTER_TO_DETAIL;
    }
    
    public boolean isCompositionDetailToMaster() {
        return this==COMPOSITION_DETAIL_TO_MASTER;
    }
    
    public boolean isAssoziation() {
        return this==ASSOCIATION;
    }
    
    public String getImageName() {
        return imageName;
    }
    
    public AggregationKind getAggregationKind() {
        if (isCompositionMasterToDetail() ) {
            return AggregationKind.COMPOSITE;
        } else if (this==AGGREGATION){
            return AggregationKind.SHARED;
        } 
        return AggregationKind.NONE;
    }
    
    private AssociationType(DefaultEnumType type, String id, String name, String imageName) {
        super(type, id, name);
        this.imageName = imageName;
    }
    
}
