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

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IResourceDelta;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * This listener is notified if at least one {@link IIpsSrcFile} is modified in a resource change
 * event. For every resource change event you get exactly one {@link IpsSrcFilesChangedEvent}
 * containing every changed {@link IIpsSrcFile} and the corresponding {@link IResourceDelta}.
 * 
 * @author dirmeier
 */
public interface IIpsSrcFilesChangeListener {

    /**
     * This method is called by the model if at least one {@link IIpsSrcFile} have changed in a
     * resource change event
     * 
     * @param event the {@link IpsSrcFilesChangedEvent} containing all {@link IIpsSrcFile source
     *            files} that have changed
     */
    void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event);

}
