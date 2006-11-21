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

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Default implementation of the project naming conventions.
 * 
 * @author Daniel Hohenberger
 */
public class DefaultIpsProjectNamingConventions implements IIpsProjectNamingConventions {


    /**
     * {@inheritDoc}
     */
    public MessageList validateQualifiedIpsObjectName(IpsObjectType type, String name) throws CoreException {
        // TODO implement
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validateUnqualifiedIpsObjectName(IpsObjectType type, String name) throws CoreException {
        // TODO implement and use in NewXyzWizards and RenamePage
        return null;
    }

    /**
     * A valid IPS package fragment name is either the empty String for the default package fragment or a valid
     * package package fragment name according to <code>JavaConventions.validatePackageName</code>.
     *
     * {@inheritDoc}
     */
    public MessageList validateIpsPackageName(String name) throws CoreException {
        MessageList ml = new MessageList();
        if(name.equals("")){ //$NON-NLS-1$
            return ml;
        }
        IStatus status = JavaConventions.validatePackageName(name);
        if(status.getSeverity()==IStatus.ERROR){
            ml.add(new Message(INVALID_NAME, NLS.bind(Messages.DefaultIpsProjectNamingConventions_error, name, status.getMessage()), Message.ERROR));
            return ml;
        }
        if(status.getSeverity()==IStatus.WARNING){
            ml.add(new Message(DISCOURAGED_NAME, NLS.bind(Messages.DefaultIpsProjectNamingConventions_warning, name, status.getMessage()), Message.WARNING));
            return ml;
        }
        return ml;
    }

}
