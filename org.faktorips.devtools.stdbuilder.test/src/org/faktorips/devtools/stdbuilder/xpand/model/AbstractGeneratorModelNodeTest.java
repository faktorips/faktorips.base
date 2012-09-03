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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractGeneratorModelNodeTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private IPolicyCmptType type;

    private XClass xClass;

    @Before
    public void setUp() throws Exception {
        xClass = new XPolicyCmptClass(type, modelContext, modelService);
        ImportStatement importStatement = mock(ImportStatement.class);
        when(modelContext.addImport(anyString())).thenReturn(importStatement);
    }

    @Test
    public void addTemplateImport() {
        xClass.addImport("java::util::Map");
        verify(modelContext).addImport("java.util.Map");
    }

    @Test
    public void addNormalImport() {
        xClass.addImport("java.util.Map");
        xClass.addImport("package.subpackage.ClassName");
        verify(modelContext).addImport("java.util.Map");
        verify(modelContext).addImport("package.subpackage.ClassName");
    }

    @Test
    public void testGetLocalizedComent() {
        xClass = spy(xClass);
        doReturn("GenericComment").when(xClass).getLocalizedText("key");

        String localizedComment = xClass.localizedComment("key");
        assertEquals("// GenericComment", localizedComment);
    }
}
