/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpttype;

/**
 * The aggregation kind as specified in the UML super structure document.
 * 
 * @author Jan Ortmann
 */
public enum AggregationKind {

    NONE("None"), //$NON-NLS-1$

    SHARED("Shared"), //$NON-NLS-1$

    COMPOSITE("Composite"); //$NON-NLS-1$

    private final String name;

    private AggregationKind(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

}
