/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.internal.xpand2.model.XpandDefinition;
import org.eclipse.xpand2.XpandExecutionContextImpl;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptClassBuilder;
import org.faktorips.devtools.stdbuilder.xpand.stringout.StringOutlet;
import org.faktorips.devtools.stdbuilder.xpand.stringout.StringOutput;
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

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private XpandBuilder<XClass> xpandBuilder;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsSrcFile ipsSrcFile;

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

    @Test
    public void testGenerate_outletCleared() throws Exception {
        StringOutlet outlet = setupBuilderStub();

        xpandBuilder.generate();

        verify(outlet).clear();
    }

    private StringOutlet setupBuilderStub() {
        when(xpandBuilder.getIpsObject()).thenReturn(ipsObject);
        when(xpandBuilder.getIpsSrcFile()).thenReturn(ipsSrcFile);
        XClass root = mock(XClass.class);
        doReturn(root).when(xpandBuilder).getGeneratorModelRoot(ipsObject);
        when(root.isValidForCodeGeneration()).thenReturn(true);
        StringOutput out = mock(StringOutput.class);
        doReturn(out).when(xpandBuilder).getOut();
        StringOutlet outlet = mock(StringOutlet.class);
        when(out.getOutlet(null)).thenReturn(outlet);
        XpandDefinition template = mock(XpandDefinition.class);
        doReturn(template).when(xpandBuilder).getTemplateDefinition();
        XpandExecutionContextImpl context = mock(XpandExecutionContextImpl.class);
        doReturn(context).when(xpandBuilder).getXpandContext();
        doReturn(null).when(xpandBuilder).getRelativeJavaFile(ipsSrcFile);
        return outlet;
    }
}
