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
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

/**
 * Presentation model object for {@link ValidationRuleMarkerUI} to activate or deactivate markers
 * for a {@link IValidationRule}.
 */
public class ValidationRuleMarkerPMO extends PresentationModelObject {

    private final IEnumType markerDefinition;
    private IValidationRule rule;
    private List<MarkerViewItem> items;

    public ValidationRuleMarkerPMO(IValidationRule validationRule, IEnumType markerDefinition) {
        this.markerDefinition = markerDefinition;
        setRule(validationRule);
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

    private void initItems() {
        Set<String> idSet = new HashSet<String>(getActiveMarkers());
        List<IEnumValue> allMarkers = getAllMarkers();
        items = new ArrayList<MarkerViewItem>();
        for (IEnumValue marker : allMarkers) {
            String id = getIdFor(marker);
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

    /**
     * Returns a list of all defined markers in the {@link IEnumType} that is used to define
     * markers.
     */
    private List<IEnumValue> getAllMarkers() {
        if (hasAvailableMarkers()) {
            return markerDefinition.getEnumValues();
        } else {
            return Collections.emptyList();
        }
    }

    public boolean hasAvailableMarkers() {
        return markerDefinition != null;
    }

    private ValueDatatype getEnumType() {
        return markerDefinition.getIpsProject().findValueDatatype(markerDefinition.getQualifiedName());
    }

    private String getIdFor(IEnumValue enumValue) {
        return enumValue.getEnumAttributeValue(findIdAttribute()).getStringValue();
    }

    private IEnumAttribute findIdAttribute() {
        return markerDefinition.findIdentiferAttribute(markerDefinition.getIpsProject());
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
        return new ValidationRuleMarkerPMO(vRule, getMarkerEnumFromProject(ipsProject));
    }

    private static IEnumType getMarkerEnumFromProject(IIpsProject ipsProject) {
        List<IIpsSrcFile> enumSrcFiles = new ArrayList<IIpsSrcFile>(ipsProject.getMarkerEnums());
        enumSrcFiles.removeAll(Collections.singleton(null));
        if (!enumSrcFiles.isEmpty()) {
            IEnumType enumType = (IEnumType)enumSrcFiles.get(0).getIpsObject();
            return enumType;
        } else {
            return null;
        }
    }

    /**
     * Represents an item with a checkbox at the UI.
     */
    public static class MarkerViewItem {
        private ValidationRuleMarkerPMO pmo;
        private boolean checked;
        private String id;

        public MarkerViewItem(ValidationRuleMarkerPMO pmo, String id, boolean checked) {
            super();
            this.pmo = pmo;
            this.id = id;
            this.checked = checked;
        }

        public boolean isChecked() {
            return checked;
        }

        public void updateCheckedState() {
            checked = !checked;
            pmo.updateActiveMarkers();
        }

        public String getLabel() {
            if (pmo.hasAvailableMarkers()) {
                return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(pmo.getEnumType(), getId());
            } else {
                return StringUtils.EMPTY;
            }
        }

        public String getId() {
            return id;
        }

    }

}
