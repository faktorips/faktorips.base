/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;

/**
 * Encapsulates access to the IpsPlugin
 * 
 * @author dicker
 */
public interface IPluginResourceFacade {

    public IpsObjectType[] getDefaultIpsObjectTypes();

    public void log(IStatus status);

    public String getIpsPluginPluginId();

    public UIDatatypeFormatter getDatatypeFormatter();

    public Properties getMessageProperties(String resourceName) throws CoreException;

}