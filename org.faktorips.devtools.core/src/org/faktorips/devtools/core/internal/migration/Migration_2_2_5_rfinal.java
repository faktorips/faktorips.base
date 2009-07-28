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

package org.faktorips.devtools.core.internal.migration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Migration from Faktor-IPS Version 2.2.5.rfinal to 2.3.0.rc1.
 * 
 * @author Alexander Weickmann
 */
public class Migration_2_2_5_rfinal extends AbstractIpsProjectMigrationOperation {

    /** Creates <code>Migration_2_2_5_rfinal</code>. */
    public Migration_2_2_5_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "New Faktor-IPS object types"
                + " have been added for modeling enumerations."
                + " Abstract enumeration type objects will be created for each table structure that represents an enumeration type"
                + " (the old table structures must be deleted manually). The referencing"
                + " table contents will also be replaced with new enumeration type objects containing the enumeration values.";
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "2.3.0.rc1"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inhSeritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException {
        Migration2_2_to2_3.migrate(getIpsProject(), monitor);
        return new MessageList();
    }
}
