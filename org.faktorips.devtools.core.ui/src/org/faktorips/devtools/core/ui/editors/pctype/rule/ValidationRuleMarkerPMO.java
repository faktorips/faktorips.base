/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.pctype.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.pctype.MarkerEnumUtil;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

/**
 * Presentation model object for {@link ValidationRuleMarkerUI} to activate or deactivate markers
 * for a {@link IValidationRule}.
 */
public class ValidationRuleMarkerPMO extends PresentationModelObject {

    private IValidationRule rule;
    private List<MarkerViewItem> items;
    private MarkerEnumUtil markerUtil;

    public ValidationRuleMarkerPMO(IValidationRule validationRule, MarkerEnumUtil markerUtil) {
        this.rule = validationRule;
        this.markerUtil = markerUtil;
    }

    /**
     * Sets the validation rule this PMO is based on. If <code>null</code> is set, no markers will
     * be displayed as active.
     * 
     * @param validationRule the rule to be used for retrieving/writing the active markers.
     */
    public void setRule(IValidationRule validationRule) {
        this.rule = validationRule;
        initItems();
    }

    protected void initItems() {
        Set<String> idSet = new HashSet<String>(getActiveMarkers());
        items = new ArrayList<MarkerViewItem>();
        for (String id : markerUtil.getDefinedMarkerIds()) {
            items.add(new MarkerViewItem(this, id, idSet.contains(id)));
        }
    }

    /**
     * Returns a list of all markers that are applied to the {@link IValidationRule}.
     */
    private List<String> getActiveMarkers() {
        if (hasValidationRule()) {
            return rule.getMarkers();
        } else {
            return Collections.emptyList();
        }
    }

    public ValueDatatype getEnumDatatype() {
        return markerUtil.getEnumDatatype();
    }

    public boolean hasAvailableMarkers() {
        return markerUtil.hasAvailableMarkers();
    }

    private boolean hasValidationRule() {
        return rule != null;
    }

    /**
     * Returns a list of all {@link MarkerViewItem}s that are shown at the UI.
     */
    public List<MarkerViewItem> getItems() {
        return items;
    }

    public void updateActiveMarkers() {
        List<String> ids = new ArrayList<String>();
        for (MarkerViewItem item : items) {
            if (item.isChecked()) {
                ids.add(item.getId());
            }
        }
        rule.setMarkers(ids);
    }

    public static ValidationRuleMarkerPMO createFor(IIpsProject ipsProject, IValidationRule vRule) {
        ValidationRuleMarkerPMO pmo = new ValidationRuleMarkerPMO(vRule, new MarkerEnumUtil(ipsProject));
        pmo.initItems();
        return pmo;
    }

    /**
     * Represents an item with a checkbox in the UI.
     */
    public static class MarkerViewItem {
        private final ValidationRuleMarkerPMO pmo;
        private final String id;
        private final String label;

        private boolean checked;

        public MarkerViewItem(ValidationRuleMarkerPMO pmo, String id, boolean initialCheckedState) {
            super();
            this.pmo = pmo;
            this.id = id;
            this.label = initLabel();
            this.checked = initialCheckedState;
        }

        private String initLabel() {
            if (pmo.hasAvailableMarkers()) {
                return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(pmo.getEnumDatatype(), getId());
            } else {
                return StringUtils.EMPTY;
            }
        }

        public boolean isChecked() {
            return checked;
        }

        public void updateCheckedState() {
            checked = !checked;
            pmo.updateActiveMarkers();
        }

        public String getLabel() {
            return label;
        }

        public String getId() {
            return id;
        }

    }

}
