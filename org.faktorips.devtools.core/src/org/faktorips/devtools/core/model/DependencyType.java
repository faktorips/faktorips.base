/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

/**
 * An enum that describes the type of dependency.
 * 
 */
public enum DependencyType {

    INSTANCEOF("instance of dependency"), //$NON-NLS-1$

    SUBTYPE("subtype dependency"), //$NON-NLS-1$

    REFERENCE_COMPOSITION_MASTER_DETAIL("master to detail composition dependency"), //$NON-NLS-1$

    REFERENCE("reference dependency"), //$NON-NLS-1$

    DATATYPE("datatype dependency"), //$NON-NLS-1$

    /**
     * Dependency type for objects that normally have no relation to each other, in the sense of
     * references in a model, but require validating if one of them changes.
     * <p>
     * Note that this type of dependency is <em>only</em> used in <em>error cases</em>. Objects are
     * invalid but <em>become valid</em> if one of them changes (thus the re-validation). In the
     * opposite case, where objects <em>become invalid</em> if one of them changes, "natural"
     * dependencies exist, e.g. references.
     * <p>
     * For example this type is used for table contents of single-table structures. If there are two
     * table contents, both have errors. If one of them is deleted the other must be re-validated to
     * become error free.
     */
    VALIDATION("validation dependency"); //$NON-NLS-1$

    private String name;

    private DependencyType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
