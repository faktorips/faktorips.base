/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
