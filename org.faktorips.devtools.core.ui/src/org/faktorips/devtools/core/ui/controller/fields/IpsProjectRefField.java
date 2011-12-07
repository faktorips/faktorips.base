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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;

/**
 * Field for {@link IpsProjectRefControl project reference controls}.
 * 
 * @author dirmeier
 */
public class IpsProjectRefField extends DefaultEditField<IIpsProject> {

    private IpsProjectRefControl ipsProjectRefControl;

    public IpsProjectRefField(IpsProjectRefControl refControl) {
        ipsProjectRefControl = refControl;
        setSupportsNullStringRepresentation(false);
    }

    @Override
    protected void addListenerToControl() {
        ipsProjectRefControl.getTextControl().addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(IpsProjectRefField.this));
            }
        });
    }

    @Override
    protected IIpsProject parseContent() throws Exception {
        IIpsProject ipsProject = ipsProjectRefControl.getIpsProject();
        if (ipsProject != null) {
            return ipsProject;
        } else {
            throw new IllegalArgumentException("Invalild package fragment"); //$NON-NLS-1$
        }
    }

    @Override
    public Control getControl() {
        return ipsProjectRefControl;
    }

    @Override
    public String getText() {
        return ipsProjectRefControl.getText();
    }

    @Override
    public void insertText(String text) {
        ipsProjectRefControl.setText(text);
    }

    @Override
    public void selectAll() {
        ipsProjectRefControl.getTextControl().selectAll();
    }

    @Override
    public void setText(String newText) {
        ipsProjectRefControl.setText(newText);
    }

    @Override
    public void setValue(IIpsProject newValue) {
        ipsProjectRefControl.setIpsProject(newValue);
    }

}
