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

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;

/**
 * The aggregation kind as specified in the UML super structure document.
 * 
 * @author Jan Ortmann
 */
public class AggregationKind extends DefaultEnumValue {

    public final static AggregationKind NONE;

    public final static AggregationKind SHARED;

    public final static AggregationKind COMPOSITE;

    private final static DefaultEnumType enumType;

    public final static DefaultEnumType getEnumType() {
        return enumType;
    }

    public final static AggregationKind getKind(String id) {
        return (AggregationKind)enumType.getEnumValue(id);
    }

    static {
        enumType = new DefaultEnumType("AggregationKind", AggregationKind.class); //$NON-NLS-1$
        NONE = new AggregationKind(enumType, "none", "None"); //$NON-NLS-1$ //$NON-NLS-2$ 
        SHARED = new AggregationKind(enumType, "shared", "Shared"); //$NON-NLS-1$ //$NON-NLS-2$ 
        COMPOSITE = new AggregationKind(enumType, "composite", "Composite"); //$NON-NLS-1$ //$NON-NLS-2$ 
    }

    private AggregationKind(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

}
