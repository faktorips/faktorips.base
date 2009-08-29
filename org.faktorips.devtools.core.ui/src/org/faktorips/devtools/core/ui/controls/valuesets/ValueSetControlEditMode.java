/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.valuesets;

/**
 * Enumeration the defines the kind of edit modes for the {@link ValueSetSpecificationControl}. At the moment
 * we don't need a ONLY_ABSTRACT_SETS mode.
 * 
 * @author Jan Ortmann
 */
public enum ValueSetControlEditMode {

    ONLY_NONE_ABSTRACT_SETS,

    ALL_KIND_OF_SETS;

    public boolean canDefineAbstractSets() {
        return this == ALL_KIND_OF_SETS;
    }

    public boolean canDefineNoneAbstractSets() {
        return true;
    }
}
