/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;

import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class XTypeTest {

    @Mock
    private GeneratorModelContext context;

    @Mock
    private IType type;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private XType xType;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private XType xSuperType;

    @Before
    public void mockContext() {
        // addImport should always return the input parameter
        Answer<String> inputAnswer = new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0].toString();
            }
        };
        when(context.addImport(anyString())).thenAnswer(inputAnswer);
    }

    @Before
    public void createXType() {
        when(xType.getContext()).thenReturn(context);
        when(xType.getIpsObjectPartContainer()).thenReturn(type);
        when(xType.isGeneratePublishedInterfaces()).thenReturn(true);
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
        when(xType.hasSupertype()).thenReturn(true);
        doReturn(xSuperType).when(xType).getSupertype();
        doReturn("superInterfaceName").when(xSuperType).getQualifiedName(BuilderAspect.INTERFACE);

        LinkedHashSet<String> implementedInterfaces = xType.getImplementedInterfaces();

        assertThat(implementedInterfaces, hasItem("myInterface"));
    }

    @Test
    public void testGetImplementedInterfaces_withSuperclassNoPublishedInterfaces() throws Exception {
        when(xType.isGeneratePublishedInterfaces()).thenReturn(false);
        when(xType.hasSupertype()).thenReturn(true);
        doReturn(xSuperType).when(xType).getSupertype();
        doReturn("superInterfaceName").when(xSuperType).getQualifiedName(BuilderAspect.INTERFACE);

        LinkedHashSet<String> implementedInterfaces = xType.getImplementedInterfaces();

        assertTrue(implementedInterfaces.isEmpty());
    }

    @Test
    public void testGetSuperclassName_withSuperclassNoPublishedInterfaces() throws Exception {
        when(xType.isGeneratePublishedInterfaces()).thenReturn(false);
        when(xType.hasSupertype()).thenReturn(true);
        doReturn(xSuperType).when(xType).getSupertype();
        doReturn("superClassName").when(xSuperType).getQualifiedName(BuilderAspect.IMPLEMENTATION);

        String superclassName = xType.getSuperclassName();

        assertEquals("superClassName", superclassName);
    }

}
