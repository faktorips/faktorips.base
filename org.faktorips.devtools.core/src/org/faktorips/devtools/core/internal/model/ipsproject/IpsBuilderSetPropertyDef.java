/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The default implementation of the <code>org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef</code> interface.
 * If no special class is defined in the plugin descriptor for the IpsArtefactBuilderSetPropertyDef instances of this class are
 * created  
 * 
 * @author Peter Erzberger
 */
public class IpsBuilderSetPropertyDef implements IIpsBuilderSetPropertyDef{

    private String name;
    private String label;
    private String description;
    private String type;
    private String defaultValue;
    private String disableValue;
    private List supportedJdkVersions;
    private List discretePropertyValues;

    
    /**
     * This constructor is only public for test purposes. Regularly instances of this class are
     * created via the loadExtensions static method.
     */
    public IpsBuilderSetPropertyDef(String name, String label, String description, String type, String defaultValue,
            String disableValue, List discretePropertyValues, List supportedJdkVersions) {
        super();
        ArgumentCheck.notNull(name, this);
        this.name = name;
        this.label = label;
        this.description = description;
        this.type = type;
        this.discretePropertyValues = discretePropertyValues;
        this.defaultValue = defaultValue;
        this.disableValue = disableValue;
        this.supportedJdkVersions = supportedJdkVersions;
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue(IIpsProject ipsProject) {
        return defaultValue;
    }

    /**
     * Returns the descripton specified in the plugin descriptor.
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisableValue(IIpsProject ipsProject) {
        return disableValue;
    }

    /**
     * Returns the name specified in the plugin descriptor.
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel() {
        return label;
    }
    
    
    /**
     * Returns the type specified in the plugin descriptor.
     */
    public String getTypeInternal() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public IStatus initialize(IIpsModel ipsModel, Map properties) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object parseValue(String value) {
        
        if(type.equals("string")){
            return value;
        } else if(type.equals("boolean")){
            return Boolean.valueOf(value);
        } else if(type.equals("integer")){
            return Integer.valueOf(value);
        } else if(type.equals("enum") || type.equals("extensionPoint")){
            if(value.equals("null")){
                return null;
            }
            for (Iterator it = discretePropertyValues.iterator(); it.hasNext();) {
                String discreteValue = (String)it.next();
                if(discreteValue.equals(value)){
                    return value;
                }
            }
        }
        throw new IllegalArgumentException("The provided value \"" + value
                + "\" cannot be converted into an instance of the type " + type + " of this IpsBuilderSetPropertyDef.");
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAvailable(IIpsProject ipsProject) {
        String optionValue = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        return supportedJdkVersions.isEmpty() || supportedJdkVersions.contains(optionValue); 
    }

    /**
     * {@inheritDoc}
     */
    public Message validateValue(String value) {
        
        boolean parsable = false;
        if(type.equals("string")){
            parsable = Datatype.STRING.isParsable(value);
        } else if(type.equals("boolean")){
            parsable = Datatype.BOOLEAN.isParsable(value);
        } else if(type.equals("integer")){
            parsable = Datatype.INTEGER.isParsable(value);
        } else if(type.equals("enum") || type.equals("extensionPoint")){
            if(value.equals("null")){
                parsable = true;
            }
            for (Iterator it = discretePropertyValues.iterator(); it.hasNext();) {
                String discreteValue = (String)it.next();
                if(discreteValue.equals(value)){
                    parsable = true;
                }
            }
        }

        if(!parsable){
            return new Message("", "The value \"" + value + "\" is not supported for the type \"" + type
                    + "\" of this property \"" + getName() + "\"", Message.ERROR);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getDiscreteValues() {
        return discretePropertyValues.toArray(new String[discretePropertyValues.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getType() {
        if(getTypeInternal().equals("string")){
            return Datatype.STRING;
        }
        if (getTypeInternal().equals("boolean")){
            return Datatype.BOOLEAN;
        }
        if (getTypeInternal().equals("integer")){
            return Datatype.INTEGER;
        }
        if(getTypeInternal().equals("enum") || getTypeInternal().equals("extensionPoint")){
            return new PropertyDefEnumDatatype();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasDiscreteValues() {
        return discretePropertyValues.size() != 0;
    }

    private final static void retrieveEnumValues(String type,
            List discreteValues,
            IConfigurationElement element,
            ILog logger) {
        if (!StringUtils.isEmpty(type) && type.equals("enum") && element.getName().equals("discreteValues")) {
            IConfigurationElement[] values = element.getChildren();
            for (int j = 0; j < values.length; j++) {
                String value = values[j].getAttribute("value");
                if (!StringUtils.isEmpty(value)) {
                    discreteValues.add(value);
                }
            }
        }
    }
    
    private final static boolean retrieveReferencedExtensionPoint(String type,
            List discreteValues,
            IExtensionRegistry registry,
            String builderSetId,
            Map properties,            
            IConfigurationElement element,
            ILog logger) {
        if("extensionPoint".equals(type)){
            String pluginIdValue = element.getAttribute("pluginId"); //$NON-NLS-1$
            properties.put("pluginId", pluginIdValue);
            if (StringUtils.isEmpty(pluginIdValue)) {
                logger.log(new IpsStatus("If the type attribute of builder set property " + element.getName() + " of the builder set " + 
                        builderSetId + " has the value \"extensionPoint\" then the \"pluginId\" attribute has to have a value.")); //$NON-NLS-1$
                return false;
            }
            String extensionPointValue = element.getAttribute("extensionPoint"); //$NON-NLS-1$
            properties.put("extensionPoint", pluginIdValue);
            if (StringUtils.isEmpty(extensionPointValue)) {
                logger.log(new IpsStatus("If the type attribute of the builder set property " + element.getName() + " of the builder set " + 
                        builderSetId + " has the value \"extensionPoint\" then the \"extensionPoint\" attribute has to have a value.")); //$NON-NLS-1$
                return false;
            }
            IExtensionPoint refExtPoint = registry.getExtensionPoint(pluginIdValue, extensionPointValue);
            IExtension[] refExts  = refExtPoint.getExtensions();
            for (int j = 0; j < refExts.length; j++) {
                discreteValues.add(refExts[j].getSimpleIdentifier());
            }
        }
        return true;
    }
    
    private final static void retrieveJdkComplianceLevels(List jdkComplianceLevelList, IConfigurationElement element){
        if(element.getName().equals("jdkComplianceLevels")){
            IConfigurationElement[] values = element.getChildren();
            for (int j = 0; j < values.length; j++) {
                String level = values[j].getAttribute("value");
                if(!StringUtils.isEmpty(level)){
                    jdkComplianceLevelList.add(level);
                }
            }
        }
    }
    
    private final static boolean retrieveProperties(ILog logger,
            IExtensionRegistry registry,
            String builderSetId,
            IConfigurationElement element,
            Map properties,
            List discreteValues) {

        String classValue = element.getAttribute("class"); //$NON-NLS-1$
        boolean classValueSpecified = !StringUtils.isEmpty(classValue);

        String name = element.getAttribute("name"); //$NON-NLS-1$
        if (!classValueSpecified && StringUtils.isEmpty(name)) {
            logger
            .log(new IpsStatus(
                    "The required attribute \"name\" of the builder set property " + element.getName() + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$
            return false;
        }
        String label = element.getAttribute("label"); //$NON-NLS-1$
        if (!classValueSpecified && StringUtils.isEmpty(label)) {
            logger
            .log(new IpsStatus(
                    "The required attribute \"label\" of the builder set property " + element.getName() + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$
            return false;
        }
        String type = element.getAttribute("type"); //$NON-NLS-1$
        if (!classValueSpecified && StringUtils.isEmpty(type)) {
            logger
                    .log(new IpsStatus(
                            "The required attribute \"type\" of the builder set property " + element.getName() + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$
            return false;
        }
        String defaultValue = element.getAttribute("defaultValue"); //$NON-NLS-1$
        if (!classValueSpecified && StringUtils.isEmpty(defaultValue)) {
            logger
                    .log(new IpsStatus(
                            "The required attribute \"defaultValue\" of the builder set property " + element.getName() + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$
            return false;
        }
        String disableValue = element.getAttribute("disableValue"); //$NON-NLS-1$
        if (!classValueSpecified && StringUtils.isEmpty(disableValue)) {
            logger
                    .log(new IpsStatus(
                            "The required attribute \"disableValue\" of the builder set property " + element.getName() + " of the builder set " + builderSetId + " is missing.")); //$NON-NLS-1$
            return false;
        }
        String description = element.getAttribute("description"); //$NON-NLS-1$

        if (!retrieveReferencedExtensionPoint(type, discreteValues, registry, builderSetId, properties, element, logger)) {
            return false;
        }

        if (classValueSpecified) {
            try {
                properties.put("class", (IIpsBuilderSetPropertyDef)element.createExecutableExtension("class"));
            } catch (CoreException e) {
                logger.log(new IpsStatus(e));
            }
        }

        properties.put("name", name);
        properties.put("label", label);
        properties.put("type", type);
        properties.put("defaultValue", defaultValue);
        properties.put("disableValue", disableValue);
        properties.put("description", description);
        return true;
    }
    
    public final static IIpsBuilderSetPropertyDef loadExtensions(IConfigurationElement element,
            IExtensionRegistry registry,
            String builderSetId,
            ILog logger,
            IIpsModel ipsModel) {

        Map properties = new HashMap();
        ArrayList discreteValues = new ArrayList();
        ArrayList jdkComplianceLevelList = new ArrayList();

        if(!retrieveProperties(logger, registry, builderSetId, element, properties, discreteValues)){
            return null;
        }

        String type = (String)properties.get("type");
        IConfigurationElement[] childs = element.getChildren();
        
        for (int j = 0; j < childs.length; j++) {
            retrieveEnumValues(type, discreteValues, childs[j], logger);
            retrieveJdkComplianceLevels(jdkComplianceLevelList, childs[j]);
        }
        if ("enum".equals(type) && discreteValues.isEmpty()) {
            logger.log(new IpsStatus("If the type attribute of the builder set property " + element.getName()
                    + " of the builder set " + builderSetId
                    + " has the value \"enum\" then discrete values have to be specified."));
        }
        properties.put("discreteValues", discreteValues);
        properties.put("jdkComplianceLevels", jdkComplianceLevelList);

        IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)properties.remove("class");
        if (propertyDef != null) {
            try {
                propertyDef.initialize(ipsModel, properties);
            } catch (Exception e) {
                logger.log(new IpsStatus(e));
                return null;
            }
        } else {
            String name = (String)properties.get("name");
            String label = (String)properties.get("label");
            String description = (String)properties.get("description");
            String defaultValue = (String)properties.get("defaultValue");
            String disableValue = (String)properties.get("disableValue");
            propertyDef = new IpsBuilderSetPropertyDef(name, label, description, type, defaultValue,
                    disableValue, discreteValues, jdkComplianceLevelList);
        }
        return propertyDef;
    }

    
    /**
     * 
     * @author Roman Grutza
     */
    public final class PropertyDefEnumDatatype implements EnumDatatype {
        public String[] getAllValueIds(boolean includeNull) {
                return (String[]) getDiscreteValues();
        }

        public String getValueName(String id) {
            String[] discreteValues = (String[]) getDiscreteValues();
            
            for (int i = 0; i < discreteValues.length; i++) {
                if (discreteValues[i].equals(id)) {
                    return id;
                }
            }
            return null;
        }

        public boolean isSupportingNames() {
            return true;
        }

        public boolean areValuesEqual(String valueA, String valueB) {
            return valueA.equals(valueB);
        }

        public MessageList checkReadyToUse() {
            return null;
        }

        public int compare(String valueA, String valueB) throws UnsupportedOperationException {
            return valueA.compareTo(valueB);
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public ValueDatatype getWrapperType() {
            return null;
        }

        public boolean isNull(String value) {
            return (value == null);
        }

        public boolean isParsable(String value) {
            if (value==null) {
                return true;
            }
            String[] ids = (String[]) getDiscreteValues();
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].equals(value)) {
                    return true;
                }
            }
            return false;
        }

        public boolean supportsCompare() {
            return false;
        }

        public String getJavaClassName() {
            // FIXME: ???
            return "java.lang.String";
        }

        public String getName() {
            return name;
        }

        public String getQualifiedName() {
            return name;
        }

        public boolean hasNullObject() {
            return false;
        }

        public boolean isPrimitive() {
            return false;
        }

        public boolean isValueDatatype() {
            return true;
        }

        public boolean isVoid() {
            return false;
        }

        public int compareTo(Object arg0) {
            return 0;
        }
    }


}
