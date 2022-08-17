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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.pctype.Messages;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A collection of utility methods for handling {@link IEnumType marker enums}.
 */
public class MarkerEnumUtil {

    private IIpsProject ipsProject;
    private IEnumType markerDefinition;
    private ValueDatatype enumDatatype;

    public MarkerEnumUtil(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        markerDefinition = getMarkerEnumFromProject();
        if (markerDefinition != null) {
            enumDatatype = ipsProject.findValueDatatype(markerDefinition.getQualifiedName());
        }
    }

    private IEnumType getMarkerEnumFromProject() {
        List<IIpsSrcFile> enumSrcFiles = new ArrayList<>(ipsProject.getMarkerEnums());
        if (!enumSrcFiles.isEmpty()) {
            return (IEnumType)enumSrcFiles.get(0).getIpsObject();
        } else {
            return null;
        }
    }

    /**
     * Returns all marker ids that are defined in the {@link IEnumType marker enum}.
     */
    public Set<String> getDefinedMarkerIds() {
        List<IEnumValue> allDefinedMarkerIds = getAllMarkers();
        Set<String> definedMarkerIds = new LinkedHashSet<>();
        for (IEnumValue marker : allDefinedMarkerIds) {
            definedMarkerIds.add(getIdFor(marker));
        }
        return definedMarkerIds;
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

    /**
     * Checks if a {@link IEnumType marker enum} exists.
     */
    public boolean hasAvailableMarkers() {
        return markerDefinition != null;
    }

    private String getIdFor(IEnumValue enumValue) {
        return enumValue.getEnumAttributeValue(findIdAttribute()).getStringValue();
    }

    private IEnumAttribute findIdAttribute() {
        return markerDefinition.findIdentiferAttribute(ipsProject);
    }

    /**
     * Returns the {@link IEnumType marker enum}.
     */
    public IEnumType getMarkerEnumType() {
        return markerDefinition;
    }

    /**
     * Returns the {@link IEnumType marker enum}.
     */
    public String getMarkerEnumTypeName() {
        if (markerDefinition != null) {
            return markerDefinition.getQualifiedName();
        } else {
            return Messages.MarkerEnumUtil_invalidMarkerEnum;
        }
    }

    /**
     * Return the {@link ValueDatatype} of the {@link IEnumType marker enum}.
     */
    public ValueDatatype getEnumDatatype() {
        return enumDatatype;
    }

}
