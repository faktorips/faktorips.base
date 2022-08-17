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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.value.ValueFactory;
import org.junit.Before;
import org.junit.Test;

public class MarkerEnumUtilTest extends AbstractIpsPluginTest {

    private IEnumType markerDefinition;
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
        markerDefinition = newEnumType(ipsProject, "qualified.markerEnum");
        IEnumAttribute idAttribute = markerDefinition.newEnumAttribute();
        idAttribute.setIdentifier(true);
        IEnumAttribute labelAttribute = markerDefinition.newEnumAttribute();
        labelAttribute.setUsedAsNameInFaktorIpsUi(true);

        IEnumValue value1 = markerDefinition.newEnumValue();
        IEnumValue value2 = markerDefinition.newEnumValue();
        value1.setEnumAttributeValue(idAttribute, ValueFactory.createStringValue("id1"));
        value1.setEnumAttributeValue(labelAttribute, ValueFactory.createStringValue("label1"));
        value2.setEnumAttributeValue(idAttribute, ValueFactory.createStringValue("id2"));
        value2.setEnumAttributeValue(labelAttribute, ValueFactory.createStringValue("label2"));

        initMarkerEnumInProjectSettings();
    }

    private void initMarkerEnumInProjectSettings() {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addMarkerEnum("qualified.markerEnum");
        ipsProject.setProperties(properties);
    }

    @Test
    public void testGetDefinedMarkerIds_noMarkerEnumDefined() {
        assertNull(markerEnumUtil.getMarkerEnumType());
        assertTrue(markerEnumUtil.getDefinedMarkerIds().isEmpty());
    }

    @Test
    public void testGetDefinedMarkerIds_defaultValues() {
        setupMarkerDefinition();
        markerEnumUtil = new MarkerEnumUtil(ipsProject);

        Set<String> markerIds = markerEnumUtil.getDefinedMarkerIds();

        assertEquals(2, markerIds.size());
        assertThat(markerIds, hasItem("id1"));
        assertThat(markerIds, hasItem("id2"));
    }

}
