/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

/**
 * Presentation model object for ValidationRuleMarkerEditDialog to apply or remove markers to an
 * {@link IValidationRule}.
 */
public class ValidationRuleMarkerPMO extends PresentationModelObject {

    private IEnumType markerDefinition;
    private IValidationRule rule;
    private List<MarkerViewItem> items;

    public ValidationRuleMarkerPMO(IValidationRule validationRule, IEnumType markerDefinition) {
        rule = validationRule;
        this.markerDefinition = markerDefinition;
        createItems();
    }

    private void createItems() {
        Set<String> idSet = new HashSet<String>(getAppliedMarkers());
        List<IEnumValue> allMarkers = getAllMarkers();
        items = new ArrayList<MarkerViewItem>();
        for (IEnumValue marker : allMarkers) {
            items.add(new MarkerViewItem(this, marker, idSet.contains(marker.getId())));
        }
    }

    /**
     * Returns a list of all defined markers in the {@link IEnumType} that is used to define
     * markers.
     */
    public List<IEnumValue> getAllMarkers() {
        return markerDefinition.getEnumValues();
    }

    /**
     * Returns a list of all markers that are applied to the {@link IValidationRule}.
     */
    public List<String> getAppliedMarkers() {
        return rule.getMarkers();
    }

    /**
     * Returns a list of all {@link MarkerViewItem}s that are shown at the UI.
     */
    public List<MarkerViewItem> getItems() {
        return items;
    }

    public void updateCheckedState() {
        List<String> ids = new ArrayList<String>();
        for (MarkerViewItem item : items) {
            if (item.isChecked()) {
                ids.add(item.getId());
            }
        }
        rule.setMarkers(ids);
    }

    /**
     * Represents an item with a checkbox at the UI.
     */
    public static class MarkerViewItem {
        private IEnumValue enumValue;
        private ValidationRuleMarkerPMO pmo;
        private boolean checked;

        public MarkerViewItem(ValidationRuleMarkerPMO pmo, IEnumValue enumValue, boolean checked) {
            super();
            this.pmo = pmo;
            this.enumValue = enumValue;
            this.checked = checked;
        }

        public boolean isChecked() {
            return checked;
        }

        public void updateCheckedState() {
            checked = !checked;
            pmo.updateCheckedState();
        }

        public String getLabel() {
            return enumValue.getName();
        }

        public String getId() {
            return enumValue.getId();
        }
    }

}
