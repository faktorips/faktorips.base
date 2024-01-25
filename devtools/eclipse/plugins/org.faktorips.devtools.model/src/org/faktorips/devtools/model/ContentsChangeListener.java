/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.function.Consumer;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

/**
 * A listener that listens to changes in source files.
 *
 * @author Jan Ortmann
 */
@FunctionalInterface
public interface ContentsChangeListener {

    /**
     * Notifies the listener that an object has changed.
     */
    void contentsChanged(ContentChangeEvent event);

    /**
     * Creates a new {@link ContentsChangeListener} that invokes the given
     * {@link ContentChangeEvent}-{@link Consumer} whenever it receives a change event that
     * {@linkplain ContentChangeEvent#isAffected(IIpsObjectPartContainer) affects} the given
     * {@link IIpsObjectPartContainer}.
     *
     * @param partContainer the {@link IIpsObjectPartContainer} for which the listener will be
     *            filtering events
     * @param onChange the code that should be called for every change event regarding the part
     *            container
     */
    static ContentsChangeListener forEventsAffecting(IIpsObjectPartContainer partContainer,
            Consumer<ContentChangeEvent> onChange) {
        return event -> {
            if (event.isAffected(partContainer)) {
                onChange.accept(event);
            }
        };
    }

}
