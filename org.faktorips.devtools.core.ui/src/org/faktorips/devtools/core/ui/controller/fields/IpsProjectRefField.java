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
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

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
        ipsProjectRefControl.getTextControl()
                .addModifyListener($ -> notifyChangeListeners(new FieldValueChangedEvent(IpsProjectRefField.this)));
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
