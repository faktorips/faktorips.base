/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.htmlexport.context.IPluginResourceFacade;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IDatatypeFormatter;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class MockPluginResourcesFacade implements IPluginResourceFacade {

    private Map<String, Properties> messagesMap = new HashMap<>();

    private static final IpsObjectType[] ALL_IPS_OBJECT_TYPES = new IpsObjectType[] { IpsObjectType.ENUM_CONTENT,
            IpsObjectType.ENUM_TYPE, IpsObjectType.POLICY_CMPT_TYPE,
            IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.TABLE_STRUCTURE, IpsObjectType.PRODUCT_CMPT,
            IpsObjectType.TABLE_CONTENTS, IpsObjectType.TEST_CASE_TYPE, IpsObjectType.TEST_CASE,
            IpsObjectType.IPS_SOURCE_FILE };

    private IpsObjectType[] defaultIpsObjectTypes;

    public MockPluginResourcesFacade() {
        this(ALL_IPS_OBJECT_TYPES);
    }

    public MockPluginResourcesFacade(IpsObjectType[] defaultIpsObjectTypes) {
        this.defaultIpsObjectTypes = defaultIpsObjectTypes;
    }

    public void putMessageProperties(String resource, Properties messages) {
        messagesMap.put(resource, messages);
    }

    @Override
    public IpsObjectType[] getDefaultIpsObjectTypes() {
        return defaultIpsObjectTypes;
    }

    @Override
    public void log(IStatus status) {
        // nothing to do
    }

    @Override
    public String getIpsPluginPluginId() {
        return "org.faktorips.devtools.core";
    }

    @Override
    public IDatatypeFormatter getDatatypeFormatter() {
        return new IDatatypeFormatter() {
        };
    }

    @Override
    public Properties getMessageProperties(String resourceName) throws CoreException {
        Properties properties = messagesMap.get(resourceName);
        if (properties == null) {
            throw new CoreException(new IpsStatus(IStatus.WARNING, "Messages " + resourceName + " not found")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return properties;
    }
}
