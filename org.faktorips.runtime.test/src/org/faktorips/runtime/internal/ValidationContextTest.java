/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.faktorips.runtime.ValidationContext;
import org.faktorips.values.Money;
import org.junit.Test;

public class ValidationContextTest {

    @Test
    public void testGetLocale() {
        ValidationContext context = new ValidationContext();
        assertEquals(Locale.getDefault(), context.getLocale());

        context = new ValidationContext(Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, context.getLocale());
    }

    @Test
    public void testGetValue() {
        ValidationContext context = new ValidationContext();
        context.setValue("a", Money.euro(100));
        context.setValue("b", Integer.valueOf(1));
        assertEquals(Money.euro(100), context.getValue("a"));
    }

}
