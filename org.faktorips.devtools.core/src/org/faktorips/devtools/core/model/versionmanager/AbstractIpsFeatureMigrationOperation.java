/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.versionmanager;

import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;

/**
 * Abstract class for operations migrating the content of an IPS project created with an older
 * version of Faktor-IPS to match the needs of the current installed version of Faktor-IPS.
 * <p>
 * This class is a container for implementations of <code>AbstractMigrationOperation</code>s.
 * 
 * @author Thorsten Guenther
 */
public abstract class AbstractIpsFeatureMigrationOperation extends WorkspaceModifyOperation {

    /**
     * Returns the IPS project the operation migrates.
     */
    public abstract IIpsProject getIpsProject();

    /**
     * Returns the description of all the steps done by this operation - to be displayed to the
     * user.
     */
    public abstract String getDescription();

    /**
     * Returns <code>true</code> if no changes will be done by this operation, <code>false</code>
     * otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Returns a list of messages describing any problems occurred during migration. If this list is
     * empty, migration was either not executed or executed successfully. If this list contains a
     * message with severity error, it was not.
     */
    public abstract MessageList getMessageList();

}
