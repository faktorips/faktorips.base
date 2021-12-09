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

import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IDatatypeFormatter;

/**
 * Encapsulates accesses to the IpsPlugin
 * 
 * @author dicker
 */
public interface IPluginResourceFacade {

    public IpsObjectType[] getDefaultIpsObjectTypes();

    public void log(IStatus status);

    public String getIpsPluginPluginId();

    public IDatatypeFormatter getDatatypeFormatter();

    public Properties getMessageProperties(String resourceName) throws CoreRuntimeException;

}