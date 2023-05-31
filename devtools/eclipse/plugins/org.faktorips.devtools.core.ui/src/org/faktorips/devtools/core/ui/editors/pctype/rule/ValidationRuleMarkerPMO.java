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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.util.MarkerEnumUtil;

/**
 * Presentation model object for {@link ValidationRuleMarkerUI} to activate or deactivate markers
 * for a {@link IValidationRule}.
 */
public class ValidationRuleMarkerPMO extends PresentationModelObject {

    private IValidationRule rule;
    private List<MarkerViewItem> items;
    private MarkerEnumUtil markerUtil;

    public ValidationRuleMarkerPMO(IValidationRule validationRule, MarkerEnumUtil markerUtil) {
        rule = validationRule;
        this.markerUtil = markerUtil;
    }

    /**
     * Sets the validation rule this PMO is based on. If <code>null</code> is set, no markers will
     * be displayed as active.
     * 
     * @param validationRule the rule to be used for retrieving/writing the active markers.
     */
    public void setRule(IValidationRule validationRule) {
        rule = validationRule;
        initItems();
    }

    protected void initItems() {
        Set<String> idSet = new HashSet<>(getActiveMarkers());
        items = new ArrayList<>();
        for (String id : markerUtil.getDefinedMarkerIds()) {
            boolean checked = idSet.remove(id);
            items.add(MarkerViewItem.validItem(this, id, checked));
        }
        for (String id : idSet) {
            items.add(MarkerViewItem.errorItem(this, id));
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

    public ValueDatatype getEnumDatatype(String id) {
        return markerUtil.getEnumDatatype(id);
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
        List<String> ids = new ArrayList<>();
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
        private boolean illegalEntry;

        public MarkerViewItem(ValidationRuleMarkerPMO pmo, String id, boolean initialCheckedState,
                boolean illegalEntry) {
            super();
            this.pmo = pmo;
            this.id = id;
            checked = initialCheckedState;
            this.illegalEntry = illegalEntry;
            label = initLabel();
        }

        public static MarkerViewItem validItem(ValidationRuleMarkerPMO pmo, String id, boolean checked) {
            return new MarkerViewItem(pmo, id, checked, false);
        }

        public static MarkerViewItem errorItem(ValidationRuleMarkerPMO pmo, String markerID) {
            return new MarkerViewItem(pmo, markerID, true, true);
        }

        private String initLabel() {
            if (illegalEntry) {
                return NLS.bind(Messages.ValidationRuleMarkerPMO_Label_illegalEntry, id);
            } else if (pmo.hasAvailableMarkers()) {
                return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(pmo.getEnumDatatype(id), getId());
            } else {
                return id;
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

        public String getLabelAndMarkerEnumName() {
            String markerEnumName = pmo.getEnumDatatype(id).getName();
            return label + " - " + markerEnumName;
        }

        public String getId() {
            return id;
        }

        public boolean isIllegal() {
            return illegalEntry;
        }

    }

}
