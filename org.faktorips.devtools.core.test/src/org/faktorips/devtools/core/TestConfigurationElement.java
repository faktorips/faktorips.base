/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.faktorips.util.ArgumentCheck;

/**
 * A test implementation of the IConfigurationElement to test extension point loading code without interfering with definitions
 * in plugin descriptors within the workspace. Not all methods of this class are implemented. Not implemented methods throw a
 * RuntimeException.
 * methods 
 * 
 * @author Peter Erzberger
 */
public class TestConfigurationElement implements IConfigurationElement {

    private String name;
    private Map attributes;
    private String value;
    private Map executableExtensionMap = new HashMap();
    private IConfigurationElement[] children;

    
    public TestConfigurationElement(String name, Map attributes, String value, IConfigurationElement[] children){
        this(name, attributes, value, children, new HashMap());
    }
    
    /**
     * @param name
     * @param attributes
     * @param children
     */
    public TestConfigurationElement(String name, Map attributes, String value, IConfigurationElement[] children, Map executableExtensionMap) {
        ArgumentCheck.notNull(name, this);
        ArgumentCheck.notNull(attributes, this);
        ArgumentCheck.notNull(children, this);
        this.name = name;
        this.attributes = attributes;
        this.children = children;
        this.value = value;
        this.executableExtensionMap = executableExtensionMap;
    }

    public Object createExecutableExtension(String propertyName) throws CoreException {
        return executableExtensionMap.get(propertyName);
    }

    public String getAttribute(String name) throws InvalidRegistryObjectException {
        return (String)attributes.get(name);
    }

    public String getAttributeAsIs(String name) throws InvalidRegistryObjectException {
        return getAttribute(name);
    }

    public String[] getAttributeNames() throws InvalidRegistryObjectException {
        Set nameSet = attributes.keySet();
        return (String[])attributes.keySet().toArray(new String[nameSet.size()]);
    }

    public IConfigurationElement[] getChildren() throws InvalidRegistryObjectException {
        return children;
    }

    public IConfigurationElement[] getChildren(String name) throws InvalidRegistryObjectException {
        ArrayList result = new ArrayList();
        for (int i = 0; i < children.length; i++) {
            if(children[i].getName().equals(name)){
                result.add(children[i]);
            }
        }
        return (IConfigurationElement[])result.toArray(new IConfigurationElement[result.size()]);
    }

    public IContributor getContributor() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    public IExtension getDeclaringExtension() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    public String getName() throws InvalidRegistryObjectException {
        return name;
    }

    public String getNamespace() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    public Object getParent() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    public String getValue() throws InvalidRegistryObjectException {
        return value;
    }

    public String getValueAsIs() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    public boolean isValid() {
        throw new RuntimeException("Not implemented.");
    }

}
