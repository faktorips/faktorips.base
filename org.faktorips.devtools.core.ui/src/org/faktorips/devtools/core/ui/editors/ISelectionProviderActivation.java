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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Implementations of this interface can be registered with an
 * <code>ISelectionProviderDispatcher</code>. An ISelectionProviderActivation provides the
 * information if the ISelectionProvider which can be retrieved by it is the currently active one.
 * An active ISelectionProvider is the one where the correlated wigdet or one of its controls holds
 * the focus or the cursor or there is another indicator that identifies that it is active.
 * 
 * @author Peter Erzberger
 */
public interface ISelectionProviderActivation {

    /**
     * Returns the selection provider of this ISelectionProviderActivation.
     */
    public ISelectionProvider getSelectionProvider();

    /**
     * Returns if the selection provider of this activation is active.
     */
    public boolean isActivated();

    /**
     * Returns true if the control that is displayed for the selection provider of this activation
     * is disposed.
     */
    public boolean isDisposed();
}
