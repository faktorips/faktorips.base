/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.util;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.faktorips.annotation.UtilityClass;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentLink;

@UtilityClass
public class ProductComponentLinks {

    private ProductComponentLinks() {
        // do not instantiate
    }

    /**
     * Returns value of type IProductComponent at the indicated index.
     *
     * @throws IndexOutOfBoundsException if the given index is out of range
     */
    public static <T extends IProductComponent> T getTarget(int index,
            Map<String, IProductComponentLink<T>> componentLink) {
        Iterator<? extends IProductComponentLink<T>> it = componentLink.values().iterator();
        try {
            for (int i = 0; i < index; i++) {
                it.next();
            }
            return it.next().getTarget();
        } catch (NoSuchElementException e) {
            IndexOutOfBoundsException indexOutOfBoundsException = new IndexOutOfBoundsException();
            indexOutOfBoundsException.initCause(e);
            throw indexOutOfBoundsException;
        }
    }
}
