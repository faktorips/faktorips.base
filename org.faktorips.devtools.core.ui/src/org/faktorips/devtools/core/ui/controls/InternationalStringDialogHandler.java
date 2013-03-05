/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.ui.dialogs.InternationalStringDialog;
import org.faktorips.values.LocalizedString;

/**
 * This handler is used to open a {@link InternationalStringDialog} to edit some
 * {@link LocalizedString}. The {@link IIpsObjectPart} is used to bind the dialog to this part and
 * look or update for any changes.
 * <p>
 * This class is an abstract class and you need to implement the {@link #getInternationalString()}
 * for your own. This is important because in some environments the {@link IInternationalString}
 * object that should be modified may change after the handler was instantiated. For example if the
 * part is an {@link IAttributeValue}, the internal {@link IInternationalString} may change when
 * anybody reset the editor (for example by discard any changes) but the handler instance would
 * still be the same.
 * 
 * @author dirmeier
 */
public abstract class InternationalStringDialogHandler {
    private final Shell shell;
    private final IIpsObjectPart part;

    /**
     * The constructor needs to know the shell to instantiate the dialog and the part that should be
     * provided to the dialog to bind on for any changes.
     * 
     */
    public InternationalStringDialogHandler(Shell shell, IIpsObjectPart part) {
        this.shell = shell;
        this.part = part;
    }

    public IIpsProject getIpsProject() {
        return part.getIpsProject();
    }

    /**
     * This method should be called by the action or button that triggers this handler.
     */
    public void run() {
        openMultilingualValueDialog();
    }

    protected void openMultilingualValueDialog() {
        InternationalStringDialog internationalStringDialog = new InternationalStringDialog(shell, part,
                getInternationalString());
        internationalStringDialog.open();
    }

    /**
     * Returns the international string that should be manipulated by the
     * {@link InternationalStringDialog} opened by this handler.
     */
    protected abstract IInternationalString getInternationalString();

}