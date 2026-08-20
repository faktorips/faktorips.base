/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import org.faktorips.annotation.UtilityClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

/**
 * Helper class for the use of Mockito with JUnit 6, as the MockitoExtension still uses JUnit 5. See
 * https://github.com/mockito/mockito/pull/3781.
 */
@UtilityClass
public final class MockUtil {

    private MockUtil() {
        // util
    }

    /**
     * Creates a {@link MockitoSession} for the given test class instance. This method should be
     * called in a {@link BeforeEach} block. The created session should be kept in a field and its
     * {@link MockitoSession#finishMocking()} method must be called in a matching {@link AfterEach}
     * block:
     *
     * <pre class="code">
     * <code class="java">
     *   private MockitoSession mockito;
     *
     *   &#64;BeforeEach
     *   public void setUp() {
     *     mockito = createMocks(this);
     *
     *     // configure mocks
     *   }
     *
     *   &#64;AfterEach
     *   public void tearDown() throws Exception {
     *     mockito.finishMocking();
     *   }
     * </code>
     * </pre>
     */
    public static MockitoSession createMocks(Object testClassInstance) {
        Strictness strictness = Strictness.STRICT_STUBS;
        return createMocks(testClassInstance, strictness);
    }

    /**
     * Creates a {@link MockitoSession} with the given {@link Strictness} for the given test class
     * instance. This method should be called in a {@link BeforeEach} block. The created session
     * should be kept in a field and its {@link MockitoSession#finishMocking()} method must be
     * called in a matching {@link AfterEach} block:
     *
     * <pre class="code">
     * <code class="java">
     *   private MockitoSession mockito;
     *
     *   &#64;BeforeEach
     *   public void setUp() {
     *     mockito = createMocks(this, Strictness.LENIENT);
     *
     *     // configure mocks
     *   }
     *
     *   &#64;AfterEach
     *   public void tearDown() throws Exception {
     *     mockito.finishMocking();
     *   }
     * </code>
     * </pre>
     */
    public static MockitoSession createMocks(Object testClassInstance, Strictness strictness) {
        return Mockito.mockitoSession()
                .initMocks(testClassInstance)
                .strictness(strictness)
                .startMocking();
    }

}
