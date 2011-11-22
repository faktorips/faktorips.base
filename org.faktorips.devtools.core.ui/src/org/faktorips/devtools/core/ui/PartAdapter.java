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

package org.faktorips.devtools.core.ui;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * Provides default implementations for the methods described in {@link IPartListener2}.
 * <p>
 * Subclasses must override only those methods they are interested in.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IPartListener2
 */
public class PartAdapter implements IPartListener2 {

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
        // Empty default implementation
    }

}
