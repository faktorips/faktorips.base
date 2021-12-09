/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import org.eclipse.core.runtime.Platform;

/**
 * A factory that adapts objects of one type to another.
 */
// TODO klären ob wir dieses Konzept außerhalb von Eclipse wirklich benötigen. Wenn nein, dann erst
// mal wieder entfernen.
public class AAdaptersFactory {

    private AAdaptersFactory() {
        // util
    }

    public static <T> T getAdapter(Object adaptable, Class<T> adapterType) {
        if (Abstractions.isEclipseRunning()) {
            // TODO müssen wir hier ggf. zum Wrapper den passenden adapterType finden, damit Eclipse
            // aufrufen und dann wrappen?
            return Platform.getAdapterManager().getAdapter(adaptable, adapterType);
        } else {
            // TODO
            throw new UnsupportedOperationException();
        }
    }
}
