/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptClassBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class XpandBuilderTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IIpsProject ipsProject;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StandardBuilderSet builderSet;

    @Mock
    private GeneratorModelContext generatorModelContext;

    @Mock
    private ClassLoader myUselessClassLoader;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * For productive use, the debug switch needs to be disabled!
     */
    @Test
    public void testDebugDisabled() {
        assertFalse(XpandBuilder.DEBUG);
    }

    @Test
    public void testBeforeBuildProcess() throws Exception {
        OptimizedResourceManager resourceManager = new OptimizedResourceManager();
        when(generatorModelContext.getResourceManager()).thenReturn(resourceManager);
        // Using PolicyCmptImlClassBuilder as concrete instance knowing that this also tests this
        // other class
        PolicyCmptClassBuilder policyCmptClassBuilder = new PolicyCmptClassBuilder(false, builderSet,
                generatorModelContext, null);
        policyCmptClassBuilder.beforeBuildProcess(ipsProject, 0);
        assertNotNull(policyCmptClassBuilder.getOut());
        assertNotNull(policyCmptClassBuilder.getTemplateDefinition());
    }

    /**
     * FIPS-2227
     */
    @Test
    public void testBuild_correctContextClassloader() throws Exception {
        when(generatorModelContext.getResourceManager()).thenReturn(new OptimizedResourceManager());
        PolicyCmptClassBuilder policyCmptClassBuilder = new PolicyCmptClassBuilder(false, builderSet,
                generatorModelContext, null);
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(myUselessClassLoader);

        policyCmptClassBuilder.beforeBuildProcess(ipsProject, 0);
        policyCmptClassBuilder.afterBuildProcess(ipsProject, 0);

        assertNotNull(policyCmptClassBuilder.getTemplateDefinition());
        assertSame(myUselessClassLoader, Thread.currentThread().getContextClassLoader());

        Thread.currentThread().setContextClassLoader(oldClassLoader);
    }

}
