/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;

public class IpsPckFragmentRootRefField extends DefaultEditField<IIpsPackageFragmentRoot> {

    private IpsPckFragmentRootRefControl fragmentRootRefControl;

    public IpsPckFragmentRootRefField(IpsPckFragmentRootRefControl refControl) {
        fragmentRootRefControl = refControl;
    }

    @Override
    protected void addListenerToControl() {
        fragmentRootRefControl.getTextControl().addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(IpsPckFragmentRootRefField.this));
            }
        });
    }

    @Override
    protected IIpsPackageFragmentRoot parseContent() throws Exception {
        return fragmentRootRefControl.getIpsPackageFragmentRoot();
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
