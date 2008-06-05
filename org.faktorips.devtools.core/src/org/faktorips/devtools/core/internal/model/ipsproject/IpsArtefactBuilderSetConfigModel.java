/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The default implementation of the IIpsArtefactBuilderSetConfig interface
 * 
 * @author Peter Erzberger
 */
public class IpsArtefactBuilderSetConfigModel implements IIpsArtefactBuilderSetConfigModel {

    private Map properties;
    
    /**
     * Creates an empty ips artefact builder set configuration instances.
     */
    public IpsArtefactBuilderSetConfigModel() {
        properties = new HashMap();
    }

    /**
     * This constructor is for test purposes only.
     */
    public IpsArtefactBuilderSetConfigModel(Map properties) {
        ArgumentCheck.notNull(properties);
        this.properties = properties;
    }

    /**
     * Creates and returns an ips artefact builder set configuration instance from the provided dom element.
     */
    public final void initFromXml(Element el){
        properties = new HashMap();
        NodeList nl = el.getElementsByTagName("Property"); //$NON-NLS-1$
        for (int i = 0; i < nl.getLength(); i++) {
            Element propertyEl = (Element)nl.item(i);
            String key = propertyEl.getAttribute("name"); //$NON-NLS-1$
            String value = propertyEl.getAttribute("value"); //$NON-NLS-1$
            properties.put(key, value);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getPropertyValue(String propertyName) {
        return (String)properties.get(propertyName);
    }

    /**
     * Sets the value of a property
     * 
     * @param propertyName the name of the property
     * @param value the value of the property
     */
    public void setPropertyValue(String propertyName, String value){
        ArgumentCheck.notNull(propertyName);
        ArgumentCheck.notNull(value);
        properties.put(propertyName, value);
    }
    
    /**
     * {@inheritDoc}
     */
    public final Element toXml(Document doc) {
        Element root = doc.createElement(XML_ELEMENT);
        Set keys = properties.keySet();
        for (Iterator iter = keys.iterator();iter.hasNext();) {
            String key = (String)iter.next();
            String value = (String)properties.get(key);
            Element prop = doc.createElement("Property"); //$NON-NLS-1$
            root.appendChild(prop);
            prop.setAttribute("name", key); //$NON-NLS-1$
            prop.setAttribute("value", value); //$NON-NLS-1$
        }
        return root;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getPropertyNames() {
        return (String[])properties.keySet().toArray(new String[properties.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArtefactBuilderSetConfig create(IIpsProject ipsProject, IIpsArtefactBuilderSetInfo builderSetInfo){
        HashMap properties = new HashMap();
        for (Iterator it = this.properties.keySet().iterator(); it.hasNext();) {
            String name = (String)it.next();
            IIpsBuilderSetPropertyDef propertyDef = builderSetInfo.getPropertyDefinition(name);
            if(propertyDef == null){
                throw new IllegalStateException("The property: " + name
                        + " of this builder set configuration is not defined in the provided for the builder set: "
                        + builderSetInfo.getBuilderSetId());
            }
            String valueAsString = (String)this.properties.get(name);
            if(!propertyDef.isAvailable(ipsProject)){
                properties.put(name, propertyDef.parseValue(propertyDef.getDisableValue(ipsProject)));
                continue;
            }
            Object value = propertyDef.parseValue(valueAsString);
            properties.put(name, value);
        }
        IIpsBuilderSetPropertyDef[] propertyDefs = builderSetInfo.getPropertyDefinitions();
        for (int i = 0; i < propertyDefs.length; i++) {
            Object value = properties.get(propertyDefs[i].getName());
            if(value == null){
                properties.put(propertyDefs[i].getName(), propertyDefs[i].parseValue(propertyDefs[i].getDisableValue(ipsProject)));
            }
        }
        return new IpsArtefactBuilderSetConfig(properties);
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validate(IIpsProject ipsProject, IpsArtefactBuilderSetInfo builderSetInfo){
        return builderSetInfo.validateIpsArtefactBuilderSetConfig(ipsProject, this);
    }
}
