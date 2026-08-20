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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;

import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.MockitoSession;

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

    private MockitoSession mockito;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        // addImport should always return the input parameter
        Answer<String> inputAnswer = invocation -> invocation.getArguments()[0].toString();
        lenient().when(context.addImport(anyString())).thenAnswer(inputAnswer);
        lenient().doReturn(generatorConfig).when(xType).getGeneratorConfig();
        lenient().when(xType.getIpsObjectPartContainer()).thenReturn(type);
        lenient().doReturn(ipsProject).when(xType).getIpsProject();
        lenient().when(generatorConfig.isGeneratePublishedInterfaces(any(IIpsProject.class))).thenReturn(true);
        lenient().doReturn("myInterface").when(xType).getInterfaceName();
        lenient().when(xSuperType.getContext()).thenReturn(context);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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
