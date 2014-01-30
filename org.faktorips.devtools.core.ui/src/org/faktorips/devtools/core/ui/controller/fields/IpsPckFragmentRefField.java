/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;

public class IpsPckFragmentRefField extends DefaultEditField<IIpsPackageFragment> {

    private IpsPckFragmentRefControl fragmentRefControl;

    public IpsPckFragmentRefField(IpsPckFragmentRefControl refControl) {
        fragmentRefControl = refControl;
        setSupportsNullStringRepresentation(false);
    }

    @Override
    protected void addListenerToControl() {
        fragmentRefControl.getTextControl().addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(IpsPckFragmentRefField.this));
            }
        });
    }

    @Override
    protected IIpsPackageFragment parseContent() throws Exception {
        IIpsPackageFragment ipsPackageFragment = fragmentRefControl.getIpsPackageFragment();
        if (ipsPackageFragment != null) {
            return ipsPackageFragment;
        } else {
            throw new IllegalArgumentException("Invalild package fragment"); //$NON-NLS-1$
        }
    }

    @Override
    public Control getControl() {
        return fragmentRefControl;
    }

    @Override
    public String getText() {
        return fragmentRefControl.getText();
    }

    @Override
    public void insertText(String text) {
        fragmentRefControl.setText(text);
    }

    @Override
    public void selectAll() {
        fragmentRefControl.getTextControl().selectAll();
    }

    @Override
    public void setText(String newText) {
        fragmentRefControl.setText(newText);
    }

    @Override
    public void setValue(IIpsPackageFragment newValue) {
        fragmentRefControl.setIpsPackageFragment(newValue);
    }

}
