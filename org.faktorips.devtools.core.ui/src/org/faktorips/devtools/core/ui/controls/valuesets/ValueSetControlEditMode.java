/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

/**
 * Enumeration the defines the kind of edit modes for the {@link ValueSetSpecificationControl}. At
 * the moment we don't need a ONLY_ABSTRACT_SETS mode.
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
