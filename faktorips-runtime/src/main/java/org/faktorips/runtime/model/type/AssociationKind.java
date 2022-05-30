/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

/**
 * The possible kinds of associations.
 * 
 * @see Association#getAssociationKind()
 */
public enum AssociationKind {
    /**
     * An association between two elements not in a parent-child-relation.
     */
    Association,
    /**
     * A parent-child relation
     */
    Composition,
    /**
     * A child-parent relation
     */
    CompositionToMaster;
}