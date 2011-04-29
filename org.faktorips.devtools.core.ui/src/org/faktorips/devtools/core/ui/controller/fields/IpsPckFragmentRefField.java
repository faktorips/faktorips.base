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
        return fragmentRefControl.getIpsPackageFragment();
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
