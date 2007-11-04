/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.model.versionmanager;

import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;

/**
 * Abstract class for operations migrating the content of an IpsProject created with an older
 * version of FaktorIps to match the needs of the current installed version of FaktorIps.
 * 
 * 
 * This class is a container for implementations of <code>AbstractMigrationOperation</code>s.
 * 
 * @author Thorsten Guenther
 */
public abstract class AbstractIpsFeatureMigrationOperation extends WorkspaceModifyOperation {

    /**
     * Returns the ips project the operation migrates.
     */
    public abstract IIpsProject getIpsProject();
    
    /**
     * @return The description of all the steps done by this operation - to be displayed to the
     *         user.
     */
    public abstract String getDescription();

    /**
     * @return <code>true</code> if no changes will be done by this operation, <code>false</code>
     *         otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * @return A list of messages describing any problems occured during migration. If this list is
     *         empty, migration was either not executed or executed successfully. If this list
     *         contains a message with severity error, it was not.
     */
    public abstract MessageList getMessageList();

}