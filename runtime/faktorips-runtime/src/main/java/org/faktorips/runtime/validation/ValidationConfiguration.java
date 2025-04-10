/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.validation;

import java.util.Locale;

import org.faktorips.runtime.IMarker;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Base configuration class for validation implementations.
 */
public abstract class ValidationConfiguration {
    private final Locale locale;
    private final IMarker technicalConstraintViolatedMarker;

    /**
     * Creates a new validation configuration with the specified locale.
     *
     * @param locale the locale to use for validation messages
     */
    protected ValidationConfiguration(Locale locale) {
        this(locale, null);
    }

    /**
     * Creates a new validation configuration with the specified locale and technical constraint
     * violation marker.
     *
     * @param locale the locale to use for validation messages
     * @param technicalConstraintViolatedMarker the marker to use for technical constraint
     *            violations
     */
    protected ValidationConfiguration(Locale locale, @CheckForNull IMarker technicalConstraintViolatedMarker) {
        this.locale = locale;
        this.technicalConstraintViolatedMarker = technicalConstraintViolatedMarker;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the marker to be used for technical constraint violations.
     *
     * @return the marker for technical constraint violation
     */
    protected IMarker getTechnicalConstraintViolatedMarker() {
        return technicalConstraintViolatedMarker;
    }
}
