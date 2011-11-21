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

package org.faktorips.devtools.core.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.memento.Memento;

/**
 * Allows to wrap the opening of a {@link Dialog} into a {@link Memento}.
 * <p>
 * This means that the original state of the {@link IIpsObjectPartContainer} is restored if the user
 * presses the 'Cancel' button, even if the {@link IIpsObjectPartContainer} was changed by means of
 * the {@link Dialog}.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public abstract class DialogMementoHelper {

    /**
     * Opens the {@link Dialog} provided by {@link #createDialog()} and takes care of resetting the
     * state of the provided {@link IIpsObjectPartContainer} to the state as it was before opening
     * the dialog if the user presses the cancel button.
     * 
     * @param ipsObjectPartContainer the {@link IIpsObjectPartContainer} that a {@link Memento} is
     *            created for
     * 
     * @return the return code of {@link Dialog#open()}
     */
    public int openDialogWithMemento(IIpsObjectPartContainer ipsObjectPartContainer) {
        boolean wasDirty = ipsObjectPartContainer.getIpsSrcFile().isDirty();
        Memento memento = ipsObjectPartContainer.newMemento();

        Dialog dialog = createDialog();
        if (dialog == null) {
            return Window.CANCEL;
        }

        dialog.open();
        if (dialog.getReturnCode() == Window.CANCEL && ipsObjectPartContainer.getIpsSrcFile().isMutable()) {
            ipsObjectPartContainer.setState(memento);
            if (!wasDirty) {
                ipsObjectPartContainer.getIpsSrcFile().markAsClean();
            }
        }

        return dialog.getReturnCode();
    }

    /**
     * Subclass implementation responsible for providing the {@link Dialog} to be opened.
     */
    protected abstract Dialog createDialog();

}
