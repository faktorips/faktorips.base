/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.util;

import com.google.common.base.Optional;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SwtUtil {

    private SwtUtil() {
        // Utility class that should not be instantiated
    }

    /**
     * Returns the (first) parent of the given control that is an instance of the given class.
     * Returns {@code Optional.absent()} if the given composite is {@code null} or if it does not
     * have a parent that is an instance of the given class.
     */
    public static <T extends Composite> Optional<T> getParent(Control c, Class<T> parentClass) {
        if (c == null) {
            return Optional.absent();
        }
        if (parentClass.isInstance(c)) {
            return Optional.of(parentClass.cast(c));
        }
        return getParent(c.getParent(), parentClass);
    }

}
