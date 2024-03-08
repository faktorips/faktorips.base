/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;

import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class XTypeTest {

    @Mock
    private GeneratorModelContext context;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private IType type;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private XType xType;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private XType xSuperType;

    @Mock
    private IIpsProject ipsProject;

    @Before
    public void mockContext() {
        // addImport should always return the input parameter
        Answer<String> inputAnswer = invocation -> invocation.getArguments()[0].toString();
        when(context.addImport(anyString())).thenAnswer(inputAnswer);
        doReturn(generatorConfig).when(xType).getGeneratorConfig();
    }

    @Before
    public void createXType() {
        when(xType.getIpsObjectPartContainer()).thenReturn(type);
        doReturn(ipsProject).when(xType).getIpsProject();
        when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(true);
        doReturn("myInterface").when(xType).getInterfaceName();
    }

    @Before
    public void createXSuperType() {
        when(xSuperType.getContext()).thenReturn(context);
    }

    @Test
    public void testGetImplementedInterfaces_nothingImplemented() throws Exception {
        LinkedHashSet<String> implementedInterfaces = xType.getImplementedInterfaces();

        assertThat(implementedInterfaces, hasItem("myInterface"));
    }

    @Test
    public void testGetImplementedInterfaces_withSuperclass() throws Exception {

        LinkedHashSet<String> implementedInterfaces = xType.getImplementedInterfaces();

        assertThat(implementedInterfaces, hasItem("myInterface"));
    }

    @Test
    public void testGetImplementedInterfaces_withSuperclassNoPublishedInterfaces() throws Exception {
        when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(false);

        LinkedHashSet<String> implementedInterfaces = xType.getImplementedInterfaces();

        assertTrue(implementedInterfaces.isEmpty());
    }

    @Test
    public void testGetSuperclassName_withSuperclassNoPublishedInterfaces() throws Exception {
        doReturn(true).when(type).hasSupertype();
        doReturn(xSuperType).when(xType).getSupertype();
        doReturn("superClassName").when(xSuperType).getQualifiedName(BuilderAspect.IMPLEMENTATION);

        String superclassName = xType.getSuperclassName();

        assertEquals("superClassName", superclassName);
    }

}
