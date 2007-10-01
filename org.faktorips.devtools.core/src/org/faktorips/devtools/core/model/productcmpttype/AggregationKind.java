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

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;

/**
 * The aggregation kind as specified in the UML superstructure document.
 * 
 * @author Jan Ortmann
 */
public class AggregationKind extends DefaultEnumValue {

    public final static AggregationKind NONE;
    
    public final static AggregationKind SHARED;

    private final static DefaultEnumType enumType; 
    
    public final static AggregationKind getKind(String id) {
        return (AggregationKind)enumType.getEnumValue(id);
    }
    
    static {
        enumType = new DefaultEnumType("AggregationKind", AggregationKind.class); //$NON-NLS-1$
        NONE = new AggregationKind(enumType, "none", "None");
        SHARED = new AggregationKind(enumType, "shared", "Shared");
    }
    
    private AggregationKind(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

}
