/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.testsupport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/**
 * Utility class to wait in increasing intervals up to a set maximum duration for some condition to
 * be {@code true}. Throws an {@link AssertionError} if the condition is still {@code false} after
 * the time has elapsed.
 */
public class Wait {
    private Duration duration;

    private Wait(Duration duration) {
        this.duration = duration;
    }

    /**
     * Creates a new {@link Wait} with the given {@link Duration}
     */
    public static Wait atMost(Duration duration) {
        return new Wait(duration);
    }

    /**
     * Evaluates the given check repeatedly until the wait time has elapsed. If the condition is not
     * met afterwards, an {@link AssertionError} with the given message is thrown to fail a JUnit
     * test.
     * 
     * @throws AssertionError if the condition is still {@code false} after the time has elapsed.
     */
    public void until(BooleanSupplier check, String failureMessage) {
        long max = duration.getSeconds() * 1000L + duration.getNano() / 1_000_000L;
        for (long l = 1; l < max && !check.getAsBoolean(); l *= 5) {
            try {
                Thread.sleep(l);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        assertThat(failureMessage, check.getAsBoolean(), is(true));
    }
}