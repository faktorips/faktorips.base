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
    private List<IEnumType> markerDefinitions;
    private List<ValueDatatype> enumDatatypes;

    public MarkerEnumUtil(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        markerDefinitions = getMarkerEnumsFromProject();
        enumDatatypes = new ArrayList<>();

        if (markerDefinitions != null) {
            for (IEnumType markerDefinition : markerDefinitions) {
                ValueDatatype datatype = ipsProject.findValueDatatype(markerDefinition.getQualifiedName());
                enumDatatypes.add(datatype);
            }
        }

    }

    /**
     * Returns a list of {@link IEnumType marker enum}. defined in the .ipsproject file.
     */
    public List<IEnumType> getMarkerEnumsFromProject() {
        List<IIpsSrcFile> enumSrcFiles = new ArrayList<>(ipsProject.getMarkerEnums());

        if (!enumSrcFiles.isEmpty()) {
            List<IEnumType> markerEnums = new ArrayList<>();
            for (IIpsSrcFile enumSrcFile : enumSrcFiles) {
                IEnumType enumType = (IEnumType)enumSrcFile.getIpsObject();
                markerEnums.add(enumType);
            }
            return markerEnums;
        }
        return Collections.emptyList();
    }

    /**
     * Returns all marker ids that are defined in the {@link IEnumType marker enum}.
     * 
     * @param markerDefinitions List of {@link IEnumType}.
     * @return The set of defined marker ids.
     */
    public Set<String> getDefinedMarkerIds(List<IEnumType> markerDefinitions) {
        Set<String> definedMarkerIds = new LinkedHashSet<>();

        for (IEnumType markerDefinition : markerDefinitions) {
            List<IEnumValue> allDefinedMarkerIds = getAllMarkers(markerDefinition);
            for (IEnumValue marker : allDefinedMarkerIds) {
                definedMarkerIds.add(getIdFor(marker, markerDefinition));
            }
        }

        return definedMarkerIds;
    }

    /**
     * Returns all marker ids that are defined in the {@link IEnumType marker enum}.
     * 
     * @return The set of defined marker ids.
     */
    public Set<String> getDefinedMarkerIds() {
        return getDefinedMarkerIds(markerDefinitions);
    }

    /**
     * Returns a list of all defined markers in the {@link IEnumType marker enum} that is used to
     * define markers.
     * 
     * @param markerDefinition {@link IEnumType}.
     * @return The list of all defined markers.
     */
    private List<IEnumValue> getAllMarkers(IEnumType markerDefinition) {
        if (hasAvailableMarkers(markerDefinition)) {
            return markerDefinition.getEnumValues();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Checks if {@link IEnumType marker enums} exists.
     * 
     * @param markerDefinition {@link IEnumType}
     */
    public boolean hasAvailableMarkers(IEnumType markerDefinition) {
        return !markerDefinitions.isEmpty();
    }

    /**
     * Checks if {@link IEnumType marker enums} exists.
     */
    public boolean hasAvailableMarkers() {
        return !markerDefinitions.isEmpty();
    }

    private String getIdFor(IEnumValue enumValue, IEnumType markerDefinition) {
        return enumValue.getEnumAttributeValue(findIdAttribute(markerDefinition)).getStringValue();
    }

    private IEnumAttribute findIdAttribute(IEnumType markerDefinition) {
        return markerDefinition.findIdentiferAttribute(ipsProject);
    }

    public List<IEnumType> getMarkerEnums() {
        if (hasAvailableMarkers()) {
            return markerDefinitions;
        }
        return Collections.emptyList();
    }

    /**
     * Returns the {@link IEnumType marker enum} names.
     */
    public List<String> getMarkerEnumTypeNames() {
        List<String> enumTypeNames = new ArrayList<>();
        for (IEnumType markerDefinition : markerDefinitions) {
            enumTypeNames.add(markerDefinition.getQualifiedName());
        }
        return enumTypeNames;
    }

    /**
     * Returns the {@link IEnumType marker enum} name.
     */
    public String getMarkerEnumTypeName() {
        if (!markerDefinitions.isEmpty()) {
            return markerDefinitions.get(0).getQualifiedName();
        } else {
            return Messages.MarkerEnumUtil_invalidMarkerEnum;
        }
    }

    /**
     * Return the {@link ValueDatatype} with the corresponding id of the {@link IEnumType marker
     * enum}.
     */
    public ValueDatatype getEnumDatatype(String id) {
        return enumDatatypes.stream().filter(e -> e.isParsable(id)).findFirst().orElse(null);
    }

}
