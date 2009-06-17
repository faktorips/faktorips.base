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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Migration from Faktor-IPS Version 2.2.1.test to 2.3.0.rc1.
 * 
 * @author Alexander Weickmann
 */
public class Migration_2_2_1_test extends AbstractIpsProjectMigrationOperation {

    /** Creates <code>Migration_2_2_1_test</code>. */
    public Migration_2_2_1_test(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "New ips object types" + " have been added for modeling enumerations. All table structures"
                + " that represent enum types will be changed to abstract EnumType objects. The referencing"
                + " table contents will also be replaced with EnumType objects containing the enum values.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTargetVersion() {
        return "2.3.0.rc1"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

        if (monitor != null) {
            monitor.beginTask("Migration", 1);
        }

        Migration2_2_to2_3.migrate(getIpsProject());

        if (monitor != null) {
            monitor.worked(1);
            monitor.done();
        }

        return new MessageList();
    }

}
