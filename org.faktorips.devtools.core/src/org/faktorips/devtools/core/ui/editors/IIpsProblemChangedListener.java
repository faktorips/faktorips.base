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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.resources.IResource;

/**
 * Can be added to a IpsProblemMarkerManager to get notified about ips problem
 * marker changes. Used to update error ticks.
 * 
 * @author Joerg Ortmann
 */
public interface IIpsProblemChangedListener {
    /**
     * Called when problems changed. This call is posted in an aynch exec, therefore passed
     * resources must not exist.
     * @param changedResources  A set with elements of type <code>IResource</code> that
     * describe the resources that had an problem change.
     */
    void problemsChanged(IResource[] changedResources);
}
