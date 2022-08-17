/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.runtime.util.StringBuilderJoiner;

/**
 * Utility class for parameters of the {@link ExportHtmlTask}.
 */
class IpsObjectTypesParser {

    private IpsObjectTypesParser() {
        // do not instantiate
    }

    static IpsObjectType[] getIpsObjectTypes(String ipsObjectTypes, IpsObjectType[] allIpsObjectTypes) {
        if (ipsObjectTypes == null || ipsObjectTypes.trim().isEmpty()
                || "All".equalsIgnoreCase(ipsObjectTypes.trim())) {
            return allIpsObjectTypes;
        }

        Set<IpsObjectType> modelTypes = new HashSet<>();
        Set<IpsObjectType> productTypes = new HashSet<>();
        for (IpsObjectType ipsObjectType : allIpsObjectTypes) {
            if (ipsObjectType.isProductDefinitionType()) {
                productTypes.add(ipsObjectType);
            } else {
                modelTypes.add(ipsObjectType);
            }
        }
        Map<Parameter, ExportType> exportTypes = parseIpsObjectTypes(ipsObjectTypes);
        Set<IpsObjectType> selectedIpsObjectTypes = new HashSet<>();

        if (ExportType.INCLUDE == exportTypes.remove(Parameter.ALL)) {
            selectedIpsObjectTypes.addAll(Arrays.asList(allIpsObjectTypes));
        }
        selectAll(modelTypes, exportTypes, selectedIpsObjectTypes, Parameter.MODEL);
        selectAll(productTypes, exportTypes, selectedIpsObjectTypes, Parameter.PRODUCT);
        for (IpsObjectType ipsObjectType : allIpsObjectTypes) {
            select(ipsObjectType, exportTypes, selectedIpsObjectTypes);
        }
        if (!exportTypes.isEmpty()) {
            throwUnsupportedIpsObjectTypesException(exportTypes);
        }
        return selectedIpsObjectTypes.toArray(new IpsObjectType[0]);
    }

    private static void throwUnsupportedIpsObjectTypesException(Map<Parameter, ExportType> exportTypes) {
        StringBuilder message = new StringBuilder("Unknown IpsObjectType(s): ");
        StringBuilderJoiner.join(message, exportTypes.keySet());
        throw new BuildException(message.toString());
    }

    private static void select(IpsObjectType ipsObjectType,
            Map<Parameter, ExportType> exportTypes,
            Set<IpsObjectType> selectedIpsObjectTypes) {
        Parameter parameter = new Parameter(ipsObjectType.getId());
        switch (exportType(exportTypes, parameter)) {
            case INCLUDE:
                selectedIpsObjectTypes.add(ipsObjectType);
                break;
            case EXCLUDE:
                selectedIpsObjectTypes.remove(ipsObjectType);
                break;
            case DEFAULT:
                break;
        }
    }

    private static void selectAll(Set<IpsObjectType> ipsObjectTypes,
            Map<Parameter, ExportType> exportTypes,
            Set<IpsObjectType> selectedIpsObjectTypes,
            Parameter parameter) {
        switch (exportType(exportTypes, parameter)) {
            case INCLUDE:
                selectedIpsObjectTypes.addAll(ipsObjectTypes);
                break;
            case EXCLUDE:
                selectedIpsObjectTypes.removeAll(ipsObjectTypes);
                break;
            case DEFAULT:
                break;
        }
    }

    private static ExportType exportType(Map<Parameter, ExportType> exportTypes, Parameter parameter) {
        return exportTypes.containsKey(parameter) ? exportTypes.remove(parameter) : ExportType.DEFAULT;
    }

    private static Map<Parameter, ExportType> parseIpsObjectTypes(String ipsObjectTypes) {
        Map<Parameter, ExportType> exportTypes = new HashMap<>();
        String[] split = ipsObjectTypes.split(",");
        for (String substring : split) {
            Parameter parameter = new Parameter(substring);
            exportTypes.put(parameter, parameter.inverted ? ExportType.EXCLUDE : ExportType.INCLUDE);
        }
        return exportTypes;
    }

    private static final class Parameter {

        public static final Parameter ALL = new Parameter("ALL");
        public static final Parameter MODEL = new Parameter("MODEL");
        public static final Parameter PRODUCT = new Parameter("PRODUCT");

        private String originalString;
        private String key;
        private boolean inverted;

        public Parameter(String originalString) {
            this.originalString = originalString;
            key = originalString.trim().toUpperCase();
            if (key.startsWith("!")) {
                inverted = true;
                key = key.substring(1).trim();
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || !(obj instanceof Parameter)) {
                return false;
            }
            Parameter other = (Parameter)obj;
            return Objects.equals(key, other.key);
        }

        @Override
        public String toString() {
            return originalString;
        }

    }

    private enum ExportType {
        INCLUDE,
        EXCLUDE,
        DEFAULT
    }
}
