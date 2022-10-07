/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsproject.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.Message;

/**
 * The default implementation of the {@link IIpsBuilderSetPropertyDef} interface. If no special
 * class is defined in the plugin descriptor for the property, instances of this class are created.
 */
public class IpsBuilderSetPropertyDef implements IIpsBuilderSetPropertyDef {

    private String name;
    private String label;
    private String description;
    private String type;
    private String defaultValue;
    private String disableValue;
    private List<String> supportedJdkVersions;
    private List<String> discretePropertyValues;

    @Override
    public String getDefaultValue(IIpsProject ipsProject) {
        return defaultValue;
    }

    /**
     * Returns the description specified in the plugin descriptor.
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getDisableValue(IIpsProject ipsProject) {
        return disableValue;
    }

    /**
     * Returns the name specified in the plugin descriptor.
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Returns the type specified in the plugin descriptor.
     */
    @Override
    public String getType() {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IStatus initialize(IIpsModel ipsModel, Map<String, Object> properties) {
        type = (String)properties.get("type"); //$NON-NLS-1$
        name = (String)properties.get("name"); //$NON-NLS-1$
        defaultValue = (String)properties.get("defaultValue"); //$NON-NLS-1$
        description = (String)properties.get("description"); //$NON-NLS-1$
        disableValue = (String)properties.get("disableValue"); //$NON-NLS-1$
        label = (String)properties.get("label"); //$NON-NLS-1$
        discretePropertyValues = (List<String>)properties.get("discreteValues"); //$NON-NLS-1$
        supportedJdkVersions = (List<String>)properties.get("jdkComplianceLevels"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        switch (type) {
            case "string": //$NON-NLS-1$
                return value;
            case "boolean": //$NON-NLS-1$
                return Boolean.valueOf(value);
            case "integer": //$NON-NLS-1$
                return Integer.valueOf(value);
            case "enum": //$NON-NLS-1$
            case "extensionPoint": //$NON-NLS-1$
                for (String discreteValue : discretePropertyValues) {
                    if (discreteValue.equals(value)) {
                        return value;
                    }
                }
                return disableValue;
            default:
                throw new IllegalArgumentException("The provided value \"" + value //$NON-NLS-1$
                        + "\" cannot be converted into an instance of the type " + type //$NON-NLS-1$
                        + " of this IpsBuilderSetPropertyDef."); //$NON-NLS-1$
        }
    }

    @Override
    public boolean isAvailable(IIpsProject ipsProject) {
        String optionValue = ipsProject.getJavaProject().getSourceVersion().toString();
        return supportedJdkVersions.isEmpty() || supportedJdkVersions.contains(optionValue);
    }

    @Override
    public Message validateValue(IIpsProject ipsProject, String value) {
        if (value == null) {
            return null;
        }
        boolean parsable = false;

        switch (type) {
            case "string": //$NON-NLS-1$
                parsable = Datatype.STRING.isParsable(value);
                break;
            case "boolean": //$NON-NLS-1$
                parsable = Datatype.BOOLEAN.isParsable(value);
                break;
            case "integer": //$NON-NLS-1$
                parsable = Datatype.INTEGER.isParsable(value);
                break;
            case "enum": //$NON-NLS-1$
            case "extensionPoint": //$NON-NLS-1$
                for (String discreteValue : discretePropertyValues) {
                    if (discreteValue.equals(value)) {
                        parsable = true;
                    }
                }
                break;
            default:
                break;
        }

        if (!parsable) {
            return getStandardValidationMessage(value);
        }
        return null;
    }

    protected Message getStandardValidationMessage(String value) {
        return new Message(MSGCODE_NON_PARSABLE_VALUE,
                Messages.bind(Messages.IpsBuilderSetPropertyDef_NonParsableValue, value, type, getName()),
                Message.ERROR);
    }

    @Override
    public String[] getDiscreteValues() {
        return discretePropertyValues.toArray(new String[discretePropertyValues.size()]);
    }

    @Override
    public boolean hasDiscreteValues() {
        return discretePropertyValues.size() != 0;
    }

    private static final void retrieveEnumValues(String type,
            List<String> discreteValues,
            IConfigurationElement element) {
        if (!IpsStringUtils.isEmpty(type) && "enum".equals(type) && "discreteValues".equals(element.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
            IConfigurationElement[] values = element.getChildren();
            for (IConfigurationElement value2 : values) {
                String value = value2.getAttribute("value"); //$NON-NLS-1$
                if (!IpsStringUtils.isEmpty(value)) {
                    discreteValues.add(value);
                }
            }
        }
    }

    private static final boolean retrieveReferencedExtensionPoint(String type,
            List<String> discreteValues,
            IExtensionRegistry registry,
            String builderSetId,
            Map<String, Object> properties,
            IConfigurationElement element,
            ALog logger) {

        if ("extensionPoint".equals(type)) { //$NON-NLS-1$

            String extensionPointId = element.getAttribute("extensionPointId"); //$NON-NLS-1$
            if (IpsStringUtils.isEmpty(extensionPointId)) {
                logger.log(new IpsStatus("If the type attribute of the builder set property " + element.getName() //$NON-NLS-1$
                        + " of the builder set " //$NON-NLS-1$
                        + builderSetId
                        + " has the value \"extensionPoint\" then the \"extensionPointId\" attribute has to have a value.")); //$NON-NLS-1$
                return false;
            }
            properties.put("extensionPointId", extensionPointId); //$NON-NLS-1$
            IExtensionPoint refExtPoint = registry.getExtensionPoint(extensionPointId);
            IExtension[] refExts = refExtPoint.getExtensions();
            for (IExtension refExt : refExts) {
                discreteValues.add(refExt.getUniqueIdentifier());
            }
        }
        return true;
    }

    private static final void retrieveJdkComplianceLevels(List<String> jdkComplianceLevelList,
            IConfigurationElement element) {
        if ("jdkComplianceLevels".equals(element.getName())) { //$NON-NLS-1$
            IConfigurationElement[] values = element.getChildren();
            for (IConfigurationElement value : values) {
                String level = value.getAttribute("value"); //$NON-NLS-1$
                if (!IpsStringUtils.isEmpty(level)) {
                    jdkComplianceLevelList.add(level);
                }
            }
        }
    }

    private static final boolean retrieveProperties(ALog logger,
            IExtensionRegistry registry,
            String builderSetId,
            IConfigurationElement element,
            Map<String, Object> properties,
            List<String> discreteValues) {

        String classValue = element.getAttribute("class"); //$NON-NLS-1$
        boolean classValueSpecified = !IpsStringUtils.isEmpty(classValue);

        String name = element.getAttribute("name"); //$NON-NLS-1$
        if (!classValueSpecified && IpsStringUtils.isEmpty(name)) {
            logger.log(new IpsStatus("The required attribute \"name\" of the builder set property " + element.getName() //$NON-NLS-1$
                    + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        properties.put("name", name); //$NON-NLS-1$
        String label = element.getAttribute("label"); //$NON-NLS-1$
        if (!classValueSpecified && IpsStringUtils.isEmpty(label)) {
            logger.log(new IpsStatus("The required attribute \"label\" of the builder set property " + element.getName() //$NON-NLS-1$
                    + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        properties.put("label", label); //$NON-NLS-1$
        String type = element.getAttribute("type"); //$NON-NLS-1$
        if (!classValueSpecified && IpsStringUtils.isEmpty(type)) {
            logger.log(new IpsStatus("The required attribute \"type\" of the builder set property " + element.getName() //$NON-NLS-1$
                    + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        properties.put("type", type); //$NON-NLS-1$

        String defaultValue = element.getAttribute("defaultValue"); //$NON-NLS-1$
        properties.put("defaultValue", defaultValue); //$NON-NLS-1$

        String disableValue = element.getAttribute("disableValue"); //$NON-NLS-1$
        properties.put("disableValue", disableValue); //$NON-NLS-1$

        String description = element.getAttribute("description"); //$NON-NLS-1$
        properties.put("description", description); //$NON-NLS-1$

        if (!retrieveReferencedExtensionPoint(type, discreteValues, registry, builderSetId, properties, element,
                logger)) {
            return false;
        }

        if (classValueSpecified) {
            try {
                properties.put("class", element.createExecutableExtension("class")); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (CoreException e) {
                logger.log(new IpsStatus(e));
            }
        }
        return true;
    }

    public static final IIpsBuilderSetPropertyDef loadExtensions(IConfigurationElement element,
            IExtensionRegistry registry,
            String builderSetId,
            ALog logger,
            IIpsModel ipsModel) {

        Map<String, Object> properties = new HashMap<>();
        List<String> discreteValues = new ArrayList<>();
        List<String> jdkComplianceLevelList = new ArrayList<>();

        if (!retrieveProperties(logger, registry, builderSetId, element, properties, discreteValues)) {
            return null;
        }

        String type = (String)properties.get("type"); //$NON-NLS-1$
        IConfigurationElement[] childs = element.getChildren();

        for (IConfigurationElement child : childs) {
            retrieveEnumValues(type, discreteValues, child);
            retrieveJdkComplianceLevels(jdkComplianceLevelList, child);
        }
        if ("enum".equals(type) && discreteValues.isEmpty()) { //$NON-NLS-1$
            logger.log(new IpsStatus("If the type attribute of the builder set property " + element.getName() //$NON-NLS-1$
                    + " of the builder set " + builderSetId //$NON-NLS-1$
                    + " has the value \"enum\" then discrete values have to be specified.")); //$NON-NLS-1$
        }
        properties.put("discreteValues", discreteValues); //$NON-NLS-1$
        properties.put("jdkComplianceLevels", jdkComplianceLevelList); //$NON-NLS-1$

        IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)properties.remove("class"); //$NON-NLS-1$
        if (propertyDef == null) {
            propertyDef = new IpsBuilderSetPropertyDef();
        }
        try {
            propertyDef.initialize(ipsModel, properties);
        } catch (Exception e) {
            logger.log(new IpsStatus(e));
            return null;
        }
        return propertyDef;
    }

    @Override
    public String toString() {
        return "BuilderSetPropertyDefinition: " + getName(); //$NON-NLS-1$
    }
}
