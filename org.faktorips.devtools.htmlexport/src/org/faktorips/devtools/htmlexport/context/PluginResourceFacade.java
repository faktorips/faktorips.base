/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.htmlexport.helper.FileHandler;

public class PluginResourceFacade implements IPluginResourceFacade {

    @Override
    public IpsObjectType[] getDefaultIpsObjectTypes() {
        return IpsPlugin.getDefault().getIpsModel().getIpsObjectTypes();
    }

    @Override
    public void log(IStatus status) {
        IpsPlugin.log(status);
    }

    @Override
    public String getIpsPluginPluginId() {
        return IpsPlugin.PLUGIN_ID;
    }

    @Override
    public UIDatatypeFormatter getDatatypeFormatter() {
        return IpsUIPlugin.getDefault().getDatatypeFormatter();
    }

    @Override
    public Properties getMessageProperties(String resourceName) throws CoreException {
        Properties messages = new Properties();

        try {
            byte[] bs = new FileHandler().readFile(resourceName);
            messages.load(new ByteArrayInputStream(bs));
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(IStatus.WARNING,
                    "Messages " + resourceName + " not be loaded found", e)); //$NON-NLS-1$ //$NON-NLS-2$        } 
        }

        return messages;
    }
}
