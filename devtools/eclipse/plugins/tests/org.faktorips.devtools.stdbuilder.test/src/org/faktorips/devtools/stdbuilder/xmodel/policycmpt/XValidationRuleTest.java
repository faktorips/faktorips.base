/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.policycmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.runtime.IMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class XValidationRuleTest {

    @Mock
    private GeneratorModelContext context;

    @Mock
    private ModelService modelService;

    @Mock
    private IValidationRule validationRule;

    @Mock
    private IMarker marker;

    @Mock
    private IIpsProject ipsProject;

    private XValidationRule xValidationRule;

    @Before
    public void createXValidationRule() throws Exception {
        when(validationRule.getIpsProject()).thenReturn(ipsProject);

        xValidationRule = new XValidationRule(validationRule, context, modelService);
    }

    @Test
    public void testConvertToJavaParameters() throws Exception {
        LinkedHashSet<String> parameters = new LinkedHashSet<>();
        LinkedHashSet<String> javaParameters = xValidationRule.convertToJavaParameters(parameters);
        assertTrue(javaParameters.isEmpty());

        parameters.add("asd");
        javaParameters = xValidationRule.convertToJavaParameters(parameters);
        Iterator<String> iterator = javaParameters.iterator();
        assertTrue(javaParameters.size() == 1);
        assertEquals("asd", iterator.next());

        parameters.add("0");
        javaParameters = xValidationRule.convertToJavaParameters(parameters);
        iterator = javaParameters.iterator();
        assertTrue(javaParameters.size() == 2);
        assertEquals("asd", iterator.next());
        assertEquals("p0", iterator.next());

        parameters.add("p0");
        javaParameters = xValidationRule.convertToJavaParameters(parameters);
        iterator = javaParameters.iterator();
        assertTrue(javaParameters.size() == 3);
        assertEquals("asd", iterator.next());
        assertEquals("pp0", iterator.next());
        assertEquals("p0", iterator.next());
    }

    @Test
    public void testGetMarkerSourceCodes() {
        DatatypeHelper datahelper = mock(DatatypeHelper.class);
        IEnumType enumType = mock(IEnumType.class);
        IIpsSrcFile enumTypeSrcFile = mock(IIpsSrcFile.class);
        List<String> values = new ArrayList<>();
        LinkedHashSet<IIpsSrcFile> srcFiles = new LinkedHashSet<>();
        values.add("id");
        srcFiles.add(enumTypeSrcFile);

        when(validationRule.getMarkers()).thenReturn(values);
        when(enumTypeSrcFile.getIpsObject()).thenReturn(enumType);
        when(ipsProject.getMarkerEnums()).thenReturn(srcFiles);
        when(ipsProject.findDatatypeHelper(enumType.getQualifiedName())).thenReturn(datahelper);
        when(datahelper.newInstance("id")).thenReturn(new JavaCodeFragment("EnumType.VALUE"));

        List<String> markers = xValidationRule.getMarkers();

        assertEquals(1, markers.size());
        assertEquals(markers.get(0), "EnumType.VALUE");
    }
}
