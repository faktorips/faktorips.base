/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;

public class SimpleOpenIpsObjectPartAction extends OpenEditorAction {

    public SimpleOpenIpsObjectPartAction(IIpsObjectPartContainer part, String caption) {
        super(new SimpleSelectionProvider(part));
        setImageDescriptor(IpsUIPlugin.getImageHandling().getImageDescriptor(part.getIpsObject()));
        setText(caption);
        setToolTipText(caption);
    }

    private static class SimpleSelectionProvider implements ISelectionProvider {

        private IIpsObjectPartContainer part;

        public SimpleSelectionProvider(IIpsObjectPartContainer part) {
            this.part = part;
        }

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            // nothing to do
        }

        @Override
        public ISelection getSelection() {
            return new StructuredSelection(part);
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            // nothing to do
        }

        @Override
        public void setSelection(ISelection selection) {
            // nothing to do
        }

    }

}
