/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.IMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
    private EnumType enumType;

    @Mock
    private IIpsSrcFile enumTypeSrcFile;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private DatatypeHelper datahelper;

    private XValidationRule xValidationRule;

    private Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();

    private List<String> values = new ArrayList<String>();

    @Before
    public void createXValidationRule() throws Exception {
        Mockito.when(validationRule.getIpsProject()).thenReturn(ipsProject);
        Mockito.when(validationRule.getMarkers()).thenReturn(values);

        xValidationRule = new XValidationRule(validationRule, context, modelService);
    }

    @Test
    public void testConvertToJavaParameters() throws Exception {
        LinkedHashSet<String> parameters = new LinkedHashSet<String>();
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
    public void testGetMarkers() {
        values.add("id");
        Mockito.when(enumTypeSrcFile.getIpsObject()).thenReturn(enumType);
        srcFiles.add(enumTypeSrcFile);
        Mockito.when(ipsProject.getMarkerEnums()).thenReturn(srcFiles);
        Mockito.when(ipsProject.getDatatypeHelper(new EnumTypeDatatypeAdapter(enumType, null))).thenReturn(datahelper);
        Mockito.when(datahelper.newInstance("id")).thenReturn(new JavaCodeFragment("EnumType.VALUE"));

        List<String> markers = xValidationRule.getMarkers();

        assertEquals(1, markers.size());
        assertEquals(markers.get(0), "EnumType.VALUE");
    }
}
