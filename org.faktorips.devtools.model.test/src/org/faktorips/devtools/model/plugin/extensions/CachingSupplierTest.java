/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin.extensions;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class CachingSupplierTest {

    @Test
    public void testInitializesOnlyOnce() {
        AtomicInteger initializations = new AtomicInteger(0);
        CachingSupplier<Integer> cachingSupplier = new CachingSupplier<>() {

            @Override
            protected Integer initializeValue() {
                return initializations.incrementAndGet();
            }
        };

        assertEquals(1, (int)cachingSupplier.get());
        assertEquals(1, (int)cachingSupplier.get());
        assertEquals(1, (int)cachingSupplier.get());
        assertEquals(1, initializations.get());
    }

}
