/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.pctype.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.devtools.core.ui.editors.pctype.rule.ValidationRuleMarkerPMO.MarkerViewItem;
import org.junit.Before;
import org.junit.Test;

public class ValidationRuleMarkerPMOTest extends AbstractIpsPluginTest {

    private ValidationRuleMarkerPMO markerPMO;
    private PolicyCmptType pcType;
    private IValidationRule rule;
    private IEnumType markerDefinition;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        ipsProject = newIpsProject();

        pcType = newPolicyCmptType(ipsProject, "pcType");
        rule = pcType.newRule();

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
    }

    private void initMarkerPMO() {
        markerPMO = ValidationRuleMarkerPMO.createFor(ipsProject, rule);
    }

    private void initMarkerEnumInProjectSettings() throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addMarkerEnum("qualified.markerEnum");
        ipsProject.setProperties(properties);
    }

    @Test
    public void testGetItems_noMarkerEnumDefined() {
        initMarkerPMO();
        List<MarkerViewItem> items = markerPMO.getItems();

        assertTrue(items.isEmpty());
    }

    @Test
    public void testGetItems_defaultValues() throws CoreException {
        initMarkerEnumInProjectSettings();
        initMarkerPMO();

        List<MarkerViewItem> items = markerPMO.getItems();

        assertEquals(2, items.size());
        assertEquals("label1 (id1)", items.get(0).getLabel());
        assertEquals("id1", items.get(0).getId());
        assertEquals(false, items.get(0).isChecked());

        assertEquals("label2 (id2)", items.get(1).getLabel());
        assertEquals("id2", items.get(1).getId());
        assertEquals(false, items.get(1).isChecked());
    }

    @Test
    public void testGetItems() throws CoreException {
        initMarkerEnumInProjectSettings();
        rule.setMarkers(Collections.singletonList("id1"));
        initMarkerPMO();

        List<MarkerViewItem> items = markerPMO.getItems();

        assertEquals(2, items.size());
        assertTrue(items.get(0).isChecked());
        assertFalse(items.get(1).isChecked());
    }

    @Test
    public void testUpdateActiveMarkers() throws CoreException {
        initMarkerEnumInProjectSettings();
        rule.setMarkers(Collections.singletonList("id1"));
        initMarkerPMO();

        List<MarkerViewItem> items = markerPMO.getItems();
        items.get(1).updateCheckedState();

        List<String> markers = rule.getMarkers();
        assertEquals(2, markers.size());
        assertEquals("id1", markers.get(0));
        assertEquals("id2", markers.get(1));
    }

    @Test
    public void testIllegalID() throws CoreException {
        initMarkerEnumInProjectSettings();
        rule.setMarkers(Collections.singletonList("illegalID"));
        initMarkerPMO();

        List<MarkerViewItem> items = markerPMO.getItems();

        assertEquals(3, items.size());
        MarkerViewItem item1 = items.get(0);
        assertEquals("id1", item1.getId());
        assertFalse(item1.isIllegal());

        MarkerViewItem item2 = items.get(1);
        assertEquals("id2", item2.getId());
        assertFalse(item2.isIllegal());

        MarkerViewItem illegalItem = items.get(2);
        assertEquals("illegalID", illegalItem.getId());
        assertEquals(NLS.bind(Messages.ValidationRuleMarkerPMO_Label_illegalEntry, "illegalID"), illegalItem.getLabel());
        assertTrue(illegalItem.isIllegal());
    }
}
