package org.faktorips.devtools.core.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page to edit the FaktorIPS specific project properties.
 * 
 * @author Jan Ortmann
 */
public class IpsProjectPropertyPage extends PropertyPage {

    public IpsProjectPropertyPage() {
        super();
    }

    /**
     * Overridden IMethod.
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Label l = new Label(parent, SWT.NONE);
        l.setText("Hello World!");
        return l;
    }

}
