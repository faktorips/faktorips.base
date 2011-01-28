/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.context.IPluginResourceFacade;

public class FakePluginResourcesFacade implements IPluginResourceFacade {

    private Map<String, Properties> messagesMap = new HashMap<String, Properties>();

    private static final IpsObjectType[] ALL_IPS_OBJECT_TYPES = new IpsObjectType[] { IpsObjectType.ENUM_CONTENT,
            IpsObjectType.ENUM_TYPE, IpsObjectType.BUSINESS_FUNCTION, IpsObjectType.POLICY_CMPT_TYPE,
            IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.TABLE_STRUCTURE, IpsObjectType.PRODUCT_CMPT,
            IpsObjectType.TABLE_CONTENTS, IpsObjectType.TEST_CASE_TYPE, IpsObjectType.TEST_CASE,
            IpsObjectType.IPS_SOURCE_FILE };

    private IpsObjectType[] defaultIpsObjectTypes;

    public FakePluginResourcesFacade() {
        this(ALL_IPS_OBJECT_TYPES);
    }

    public FakePluginResourcesFacade(IpsObjectType[] defaultIpsObjectTypes) {
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
    public DatatypeFormatter getDatatypeFormatter() {
        IpsPreferences ipsPreferences = mock(IpsPreferences.class);
        DatatypeFormatter datatypeFormatter = new DatatypeFormatter(ipsPreferences);
        return datatypeFormatter;
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
