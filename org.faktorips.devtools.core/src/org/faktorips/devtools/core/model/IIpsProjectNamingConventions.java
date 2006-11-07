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

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.util.message.MessageList;

/**
 * Naming conventions for the various ips elements. This is a separate class as it is sometimes
 * neccessary to check if a name is valid before an object is created for example in wizards to a create a new object. 
 * Therefore we can't use a method of the destinated class. The naming conventions are defined per project, so
 * that they can be configured in the project.
 * 
 * @author Jan Ortmann
 */
public interface IIpsProjectNamingConventions {

    public static final String DISCOURAGED_NAME = "DISCOURAGED_NAME";
    public static final String INVALID_NAME = "INVALID_NAME";
    
    /**
     * Validates if the given name is a valid for ips packages.
     * 
     * @throws CoreException if an error occurs while validating the name.
     */
    public MessageList validateIpsPackageName(String name) throws CoreException;
}
