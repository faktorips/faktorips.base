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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.productcmpt.IExpressionDependencyDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionTest {

    @Mock
    private ExpressionDependencyCollector dependencyCollector;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Expression expression;

    @Test
    public void testDependsOn() throws Exception {
        doReturn(dependencyCollector).when(expression).createDependencyCollector();
        Map<IDependency, IExpressionDependencyDetail> depMap = new HashMap<>();
        when(dependencyCollector.collectDependencies()).thenReturn(depMap);

        Map<IDependency, IExpressionDependencyDetail> dependsOn = expression.dependsOn();

        assertSame(depMap, dependsOn);
    }

}
