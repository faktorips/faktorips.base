/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * The implementation of this interface is used to provide additional information for an
 * {@link Message}.
 * <p>
 * Currently there are two standard situations markers are expected to be used in (see
 * {@link #isRequiredInformationMissing()} and {@link #isTechnicalConstraintViolated()}).
 * Implementers are free to use markers for other purposes.
 */
public interface IMarker {

    /**
     * Returns whether the marked {@link Message} is stating that required information is missing.
     * In most cases the missing information is a value for an attribute or an association's target
     * and should be discernible from the {@link Message#getInvalidObjectProperties() Message's
     * InvalidObjectProperties}.
     * 
     * @return <code>true</code> if the marked message indicates that required information is
     *             missing, otherwise <code>false</code>
     */
    boolean isRequiredInformationMissing();

    /**
     * Returns whether the marked {@link Message} is stating that a technical constraint (like for
     * example a database column length) is violated. The property the constraint applies to should
     * be discernible from the {@link Message#getInvalidObjectProperties() Message's
     * InvalidObjectProperties}.
     * 
     * @return <code>true</code> if the marked message indicates that some technical constraints are
     *             violated, otherwise <code>false</code>
     */
    boolean isTechnicalConstraintViolated();

}
