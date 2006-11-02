/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.util.message.MessageList;

/**
 * An entry in an IPS object path.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPathEntry {
    
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IIPSOBJECTPATHENTRY-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related folder is missing.
     */
    public final static String MSGCODE_MISSING_FOLDER = MSGCODE_PREFIX + "MissingFolder"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the related project is missing.
     */
    public final static String MSGCODE_MISSING_PROJECT = MSGCODE_PREFIX + "MissingProject"; //$NON-NLS-1$
    
    /**
     * Type constant indicating a source folder entry.
     */
    public final static String TYPE_SRC_FOLDER = "src";  //$NON-NLS-1$
    
    /**
     * Type constant indicating a project reference entry.
     */
    public final static String TYPE_PROJECT_REFERENCE = "project"; //$NON-NLS-1$
    
    /**
     * Type constant indicating a jar file containing the model files.
     */
    public final static String TYPE_JAR = "jar";  //$NON-NLS-1$

    /**
     * Returns the object path this is an entry of.
     */
    public IIpsObjectPath getIpsObjectPath();
    
    /**
     * Returns the type of this entry as one of the type constant defined in this interface.
     */
    public String getType();

    /**
     * Validates the object path entry and returns the result as list of messages.
     */
    public MessageList validate() throws CoreException;
}
