/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.model.DefaultVersion;
import org.faktorips.devtools.core.internal.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer;
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
        when(modelContext.addImport(anyString())).thenReturn("dummyImportStatement");
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

    @Test
    public void testHasSinceVersion_true() throws Exception {
        IVersionControlledElement versionControlledElement = mock(IVersionControlledElement.class);
        doReturn(new DefaultVersion("1.2.3")).when(versionControlledElement).getSinceVersion();
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(versionControlledElement, null, null);

        boolean hasSinceVersion = modelNode.hasSinceVersion();

        assertTrue(hasSinceVersion);
    }

    @Test
    public void testHasSinceVersion_noVersion() throws Exception {
        IVersionControlledElement versionControlledElement = mock(IVersionControlledElement.class);
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(versionControlledElement, null, null);

        boolean hasSinceVersion = modelNode.hasSinceVersion();

        assertFalse(hasSinceVersion);
    }

    @Test
    public void testHasSinceVersion_noVersionControlledElement() throws Exception {
        IpsObjectPartContainer part = mock(IpsObjectPartContainer.class);
        doReturn(new DefaultVersion("1.2.3")).when(part).getSinceVersion();
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(part, null, null);

        boolean hasSinceVersion = modelNode.hasSinceVersion();

        assertFalse(hasSinceVersion);
    }

    @Test
    public void testGetSinceVersion() throws Exception {
        IVersionControlledElement versionControlledElement = mock(IVersionControlledElement.class);
        doReturn(new DefaultVersion("1.2.3")).when(versionControlledElement).getSinceVersion();
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(versionControlledElement, null, null);

        String sinceVersion = modelNode.getSinceVersion();

        assertEquals("1.2.3", sinceVersion);
    }

    @Test
    public void testGetSinceVersion_noVersion() throws Exception {
        IVersionControlledElement versionControlledElement = mock(IVersionControlledElement.class);
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(versionControlledElement, null, null);

        String sinceVersion = modelNode.getSinceVersion();

        assertNull(sinceVersion);
    }

    @Test
    public void testGetSinceVersion_noVersionControlledElement() throws Exception {
        IpsObjectPartContainer part = mock(IpsObjectPartContainer.class);
        doReturn(new DefaultVersion("1.2.3")).when(part).getSinceVersion();
        AbstractGeneratorModelNode modelNode = new GenericGeneratorModelNode(part, null, null);

        String sinceVersion = modelNode.getSinceVersion();

        assertNull(sinceVersion);
    }

}
