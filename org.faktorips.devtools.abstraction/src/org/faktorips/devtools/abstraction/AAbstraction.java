/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

/**
 * An abstraction is a platform-independent concept. It offers access to a platform-dependent
 * implementation via the {@link #unwrap()} method to allow platform-dependent code to be called
 * with the abstraction but use its own implementation where necessary.
 * 
 * @implSpec There should be an implementation of every abstraction for every platform, extending
 *           {@link AWrapper} to wrap the platform-specific objects corresponding to that
 *           abstraction.
 */
public interface AAbstraction {

    /**
     * Returns the platform-specific object corresponding to this abstraction. The generic type
     * parameter is used to avoid casts when assigning to platform-specific objects. It is the
     * obligation of the caller to make sure the right type is used, otherwise a
     * {@link ClassCastException} will be thrown.
     * <p>
     * Sample usage:
     *
     * <pre>
     * {@code
     * interface ACalendar extends AAbstraction[...]
     * class LocalDateAsCalendarWrapper extends AWrapper<LocalDate>[...]
     * [...]
     * ACalendar calendar = Wrappers.of(LocalDate.now()).as(ACalendar.class);
     * [...]
     * LocalDate date = calendar.unwrap();
     * }
     * </pre>
     *
     * @param <T> the type of the platform-specific object
     * 
     * @see Wrappers#unwrap(AAbstraction) Wrappers#unwrap(AAbstraction) for a way to unwrap
     *      potentially null abstractions.
     */
    <T> T unwrap();

}
