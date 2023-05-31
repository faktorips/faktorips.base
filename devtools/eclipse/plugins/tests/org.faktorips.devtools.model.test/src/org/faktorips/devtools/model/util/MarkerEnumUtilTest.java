/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.value.ValueFactory;
import org.junit.Before;
import org.junit.Test;

public class MarkerEnumUtilTest extends AbstractIpsPluginTest {

    private IEnumType markerDefinition1;
    private IEnumType markerDefinition2;
    private List<IEnumType> markerDefinitions;

    private IIpsProject ipsProject;
    private MarkerEnumUtil markerEnumUtil;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        markerEnumUtil = new MarkerEnumUtil(ipsProject);
    }

    private void setupMarkerDefinition() {
        markerDefinitions = new ArrayList<>();

        //Create first marker Enum
        String qNameEnum1 = "qualified.markerEnum";
        markerDefinition1 = newEnumType(ipsProject, qNameEnum1);
        IEnumAttribute idAttribute = markerDefinition1.newEnumAttribute();
        idAttribute.setIdentifier(true);
        IEnumAttribute labelAttribute = markerDefinition1.newEnumAttribute();
        labelAttribute.setUsedAsNameInFaktorIpsUi(true);

        IEnumValue value1 = markerDefinition1.newEnumValue();
        IEnumValue value2 = markerDefinition1.newEnumValue();
        value1.setEnumAttributeValue(idAttribute, ValueFactory.createStringValue("id1"));
        value1.setEnumAttributeValue(labelAttribute, ValueFactory.createStringValue("label1"));
        value2.setEnumAttributeValue(idAttribute, ValueFactory.createStringValue("id2"));
        value2.setEnumAttributeValue(labelAttribute, ValueFactory.createStringValue("label2"));

        //Create second marker Enum
        String qNameEnum2 = "qualified.markerEnum2";
        markerDefinition2 = newEnumType(ipsProject, qNameEnum2);
        IEnumAttribute idAttribute2 = markerDefinition2.newEnumAttribute();
        idAttribute2.setIdentifier(true);
        IEnumAttribute labelAttribute2 = markerDefinition2.newEnumAttribute();
        labelAttribute2.setUsedAsNameInFaktorIpsUi(true);

        IEnumValue value3 = markerDefinition2.newEnumValue();
        IEnumValue value4 = markerDefinition2.newEnumValue();
        value3.setEnumAttributeValue(idAttribute2, ValueFactory.createStringValue("id3"));
        value3.setEnumAttributeValue(labelAttribute2, ValueFactory.createStringValue("label3"));
        value4.setEnumAttributeValue(idAttribute2, ValueFactory.createStringValue("id4"));
        value4.setEnumAttributeValue(labelAttribute2, ValueFactory.createStringValue("label4"));

        //Add MarkerEnums to ipsProject
        markerDefinitions.add(markerDefinition1);
        markerDefinitions.add(markerDefinition2);

        initMarkerEnumInProjectSettings(qNameEnum1);
        initMarkerEnumInProjectSettings(qNameEnum2);   
    }

    private void initMarkerEnumInProjectSettings(String markerEnumName) {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addMarkerEnum(markerEnumName);
        ipsProject.setProperties(properties);
    }

    @Test
    public void testGetDefinedMarkerIds_noMarkerEnumDefined() {
        assertTrue(markerEnumUtil.getMarkerEnums().isEmpty());
        assertTrue(markerEnumUtil.getDefinedMarkerIds().isEmpty());
    }

    @Test
    public void testGetDefinedMarkerIds_defaultValues() {
        setupMarkerDefinition();
        markerEnumUtil = new MarkerEnumUtil(ipsProject);

        Set<String> markerIds = markerEnumUtil.getDefinedMarkerIds();

        assertEquals(4, markerIds.size());
        assertThat(markerIds, hasItem("id1"));
        assertThat(markerIds, hasItem("id2"));
        assertThat(markerIds, hasItem("id3"));
        assertThat(markerIds, hasItem("id4"));
    }

    @Test
    public void testGetMarkerEnumsFromProject() {
        setupMarkerDefinition();
        assertEquals(2, markerEnumUtil.getMarkerEnumsFromProject().size());
    }

    @Test
    public void testGetMarkerEnumsFromProject_noMarkerEnumDefined() {
        assertTrue(markerEnumUtil.getMarkerEnumsFromProject().isEmpty());
    }

    @Test
    public void testGetEnumDatatype_validId() {
        setupMarkerDefinition();
        markerEnumUtil = new MarkerEnumUtil(ipsProject);

        ValueDatatype enumDatatype1 = markerEnumUtil.getEnumDatatype("id1");
        assertNotNull(enumDatatype1);
        assertEquals("markerEnum", enumDatatype1.getName());

        ValueDatatype enumDatatype2 = markerEnumUtil.getEnumDatatype("id3");
        assertNotNull(enumDatatype2);
        assertEquals("markerEnum2", enumDatatype2.getName());
    }

    @Test
    public void testGetEnumDatatype_inValidId() {
        setupMarkerDefinition();
        markerEnumUtil = new MarkerEnumUtil(ipsProject);

        ValueDatatype enumDatatype2 = markerEnumUtil.getEnumDatatype("invalidId");
        assertNull(enumDatatype2);
    }


}
