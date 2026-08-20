/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.productcmpt.IExpressionDependencyDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class ExpressionTest {


    @Mock
    private ExpressionDependencyCollector dependencyCollector;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Expression expression;

    private MockitoSession mockito;

    @BeforeEach
    void setUp() {
        mockito = createMocks(this);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }



    @Test
    public void testDependsOn() throws Exception {
        doReturn(dependencyCollector).when(expression).createDependencyCollector();
        Map<IDependency, IExpressionDependencyDetail> depMap = new HashMap<>();
        when(dependencyCollector.collectDependencies()).thenReturn(depMap);

        Map<IDependency, IExpressionDependencyDetail> dependsOn = expression.dependsOn();

        assertSame(depMap, dependsOn);
    }

}
