/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IDatatypeFormatter;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class PluginResourceFacade implements IPluginResourceFacade {

    @Override
    public IpsObjectType[] getDefaultIpsObjectTypes() {
        return IIpsModel.get().getIpsObjectTypes();
    }

    @Override
    public void log(IStatus status) {
        IpsModelActivator.getLog().log(status);
    }

    @Override
    public String getIpsPluginPluginId() {
        return IpsModelActivator.PLUGIN_ID;
    }

    @Override
    public IDatatypeFormatter getDatatypeFormatter() {
        return IIpsModelExtensions.get().getModelPreferences().getDatatypeFormatter();
    }

    @Override
    public Properties getMessageProperties(String resourceName) throws CoreRuntimeException {
        Properties messages = new Properties();

        try {
            byte[] bs = new FileHandler().readFile(resourceName);
            messages.load(new ByteArrayInputStream(bs));
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(IStatus.WARNING,
                    "Messages " + resourceName + " not be loaded found", e)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return messages;
    }
}
