/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;

public class IpsPckFragmentRootRefField extends DefaultEditField<IIpsPackageFragmentRoot> {

    private IpsPckFragmentRootRefControl fragmentRootRefControl;

    public IpsPckFragmentRootRefField(IpsPckFragmentRootRefControl refControl) {
        fragmentRootRefControl = refControl;
        setSupportsNullStringRepresentation(false);
    }

    @Override
    protected void addListenerToControl() {
        fragmentRootRefControl.getTextControl().addModifyListener(
                $ -> notifyChangeListeners(new FieldValueChangedEvent(IpsPckFragmentRootRefField.this)));
    }

    @Override
    protected IIpsPackageFragmentRoot parseContent() throws Exception {
        IIpsPackageFragmentRoot ipsPackageFragmentRoot = fragmentRootRefControl.getIpsPackageFragmentRoot();
        if (ipsPackageFragmentRoot != null) {
            return ipsPackageFragmentRoot;
        } else {
            throw new IllegalArgumentException("Invalild package fragment root"); //$NON-NLS-1$
        }
    }

    @Override
    public Control getControl() {
        return fragmentRootRefControl;
    }

    @Override
    public String getText() {
        return fragmentRootRefControl.getText();
    }

    @Override
    public void insertText(String text) {
        fragmentRootRefControl.setText(text);
    }

    @Override
    public void selectAll() {
        fragmentRootRefControl.getTextControl().selectAll();
    }

    @Override
    public void setText(String newText) {
        fragmentRootRefControl.setText(newText);
    }

    @Override
    public void setValue(IIpsPackageFragmentRoot newValue) {
        fragmentRootRefControl.setIpsPackageFragmentRoot(newValue);
    }

}
