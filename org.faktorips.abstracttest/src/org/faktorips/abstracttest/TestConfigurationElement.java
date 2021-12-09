/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

/**
 * A test implementation of the IConfigurationElement to test extension point loading code without
 * interfering with definitions in plugin descriptors within the workspace. Not all methods of this
 * class are implemented. Not implemented methods throw a RuntimeException. methods
 * 
 * @author Peter Erzberger
 */
public class TestConfigurationElement
        implements IConfigurationElement, IConfigurationElementCompatibilityToNewerEclipseVersions {

    public static final IContributor CONTRIBUTOR = mock(IContributor.class);

    private String name;
    private Map<String, String> attributes;
    private String value;
    private Map<String, Object> executableExtensionMap = new HashMap<>();
    private IConfigurationElement[] children;

    private IExtension extension;

    public TestConfigurationElement(String name, Map<String, String> attributes, String value,
            IConfigurationElement[] children) {
        this(name, attributes, value, children, new HashMap<String, Object>());
    }

    public TestConfigurationElement(String name, Map<String, String> attributes, String value,
            IConfigurationElement[] children, Map<String, Object> executableExtensionMap) {

        this.name = name;
        this.attributes = attributes;
        this.children = children;
        this.value = value;
        this.executableExtensionMap = executableExtensionMap;
    }

    @Override
    public Object createExecutableExtension(String propertyName) throws CoreRuntimeException {
        return executableExtensionMap.get(propertyName);
    }

    @Override
    public String getAttribute(String name) throws InvalidRegistryObjectException {
        return attributes.get(name);
    }

    @Override
    public String getAttributeAsIs(String name) throws InvalidRegistryObjectException {
        return getAttribute(name);
    }

    @Override
    public String[] getAttributeNames() throws InvalidRegistryObjectException {
        Set<String> nameSet = attributes.keySet();
        return attributes.keySet().toArray(new String[nameSet.size()]);
    }

    @Override
    public IConfigurationElement[] getChildren() throws InvalidRegistryObjectException {
        return children;
    }

    @Override
    public IConfigurationElement[] getChildren(String name) throws InvalidRegistryObjectException {
        ArrayList<IConfigurationElement> result = new ArrayList<>();
        for (IConfigurationElement element : children) {
            if (element.getName().equals(name)) {
                result.add(element);
            }
        }
        return result.toArray(new IConfigurationElement[result.size()]);
    }

    @Override
    public IContributor getContributor() throws InvalidRegistryObjectException {
        return CONTRIBUTOR;
    }

    @Override
    public IExtension getDeclaringExtension() throws InvalidRegistryObjectException {
        if (extension != null) {
            return extension;
        }
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public String getName() throws InvalidRegistryObjectException {
        return name;
    }

    @Override
    public String getNamespace() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public Object getParent() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public String getValue() throws InvalidRegistryObjectException {
        return value;
    }

    @Override
    public String getValueAsIs() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public boolean isValid() {
        throw new RuntimeException("Not implemented.");
    }

    // @since Eclipse 3.6 (Helios)
    @Override
    public String getAttribute(String attrName, String locale) throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    // @since Eclipse 3.6 (Helios)
    @Override
    public String getValue(String locale) throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public int getHandleId() {
        return 0;
    }

    public void setExtension(IExtension extension) {
        this.extension = extension;
    }

}