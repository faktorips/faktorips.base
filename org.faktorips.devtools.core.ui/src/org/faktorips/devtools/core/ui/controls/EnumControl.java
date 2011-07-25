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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * TODO
 * 
 * @author Alexander Weickmann
 */
public class EnumControl extends TextButtonControl {

    public EnumControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, "+", true, -1); //$NON-NLS-1$
        // toolkit.attachContentProposalAdapter(this, contentAdapter, proposalProvider,
        // labelProvider);
    }

    @Override
    protected void buttonClicked() {
        // TODO Auto-generated method stub

    }

}
