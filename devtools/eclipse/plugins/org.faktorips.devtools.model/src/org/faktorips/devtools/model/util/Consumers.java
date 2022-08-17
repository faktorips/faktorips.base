/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.util.function.Consumer;

/**
 * Utility methods for dealing with {@link Consumer} implementations.
 */
public final class Consumers {

    private Consumers() {
        // no instances
    }

    /**
     * Creates an empty {@link Consumer} that ignores it's argument.
     */
    public static <C> Consumer<C> ignore() {
        return $ -> {
            /* ignore */ };
    }

}
