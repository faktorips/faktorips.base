/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.HtmlExportPlugin;

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
    public DatatypeFormatter getDatatypeFormatter() {
        return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter();
    }

    @Override
    public Properties getMessageProperties(String resourceName) throws CoreException {
        Properties messages = new Properties();

        final InputStream inputStream = HtmlExportPlugin.class.getClassLoader().getResourceAsStream(resourceName);

        if (inputStream == null) {
            throw new CoreException(new IpsStatus(IStatus.WARNING, "Messages " + resourceName + " not found")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        try {
            messages.load(inputStream);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(IStatus.WARNING, "Messages " + resourceName + " couldn't be loaded")); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // nothing to do
            }
        }

        return messages;
    }
}
