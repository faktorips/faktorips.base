/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @deprecated
 */
@Deprecated
public class TwoColumnComposite extends Composite implements ModifyListener, SelectionListener {

    private FormToolkit toolkit;
    private ModifyListener modifyListener;
    private SelectionListener selectionListener;

    public TwoColumnComposite(Composite parent, ModifyListener modifyListener, SelectionListener selectionListener) {
        this(parent, SWT.NONE, null, modifyListener, selectionListener);
    }

    public TwoColumnComposite(Composite parent, int style, ModifyListener modifyListener,
            SelectionListener selectionListener) {

        this(parent, style, null, modifyListener, selectionListener);
    }

    public TwoColumnComposite(Composite parent, FormToolkit toolkit, ModifyListener modifyListener,
            SelectionListener selectionListener) {

        this(parent, SWT.NONE, toolkit, modifyListener, selectionListener);
    }

    public TwoColumnComposite(Composite parent, int style, FormToolkit toolkit, ModifyListener modifyListener,
            SelectionListener selectionListener) {

        super(parent, style);
        this.toolkit = toolkit;
        this.modifyListener = modifyListener;
        this.selectionListener = selectionListener;
        setBackground(parent.getBackground());
        if (parent.getLayout() instanceof GridLayout) {
            setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
        GridLayout layout = new GridLayout(2, false);
        if (toolkit != null) {
            layout.marginHeight = 2;
            layout.marginWidth = 2;
            layout.verticalSpacing = 8;
        } else {
            layout.marginHeight = 0;
            layout.marginWidth = 0;
        }
        setLayout(layout);
    }

    public Label newLabel(String text) {
        Label newLabel;
        if (toolkit != null) {
            newLabel = toolkit.createLabel(this, text);
        } else {
            newLabel = new Label(this, SWT.NONE);
            newLabel.setBackground(this.getBackground());
            newLabel.setText(text);
        }
        setLayoutData4Label(newLabel);
        return newLabel;
    }

    public Text newText(String text) {
        Text newText;
        if (toolkit != null) {
            newText = toolkit.createText(this, text);
        } else {
            newText = new Text(this, SWT.SINGLE | SWT.BORDER);
            newText.setText(text);
        }
        setLayoutData4EditControl(newText);
        newText.addModifyListener(this);
        return newText;
    }

    public Button newCheckbox(boolean value) {
        Composite composite;
        if (toolkit != null) {
            composite = toolkit.createComposite(this);
        } else {
            composite = new Composite(this, SWT.NONE);
        }
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 20;
        composite.setLayoutData(data);
        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginHeight = 4;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        Button newButton;
        if (toolkit != null) {
            newButton = toolkit.createButton(this, null, SWT.CHECK);
        } else {
            newButton = new Button(composite, SWT.CHECK);
        }
        newButton.setSelection(value);
        setLayoutData4EditControl(newButton);
        newButton.addSelectionListener(this);
        return newButton;
    }

    public void setLayoutData4Label(Control label) {
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END);
        label.setLayoutData(data);
    }

    public void setLayoutData4EditControl(Control editControl) {
        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
        editControl.setLayoutData(data);
    }

    @Override
    public void modifyText(ModifyEvent e) {
        if (modifyListener != null) {
            modifyListener.modifyText(e);
        }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (selectionListener != null) {
            selectionListener.widgetSelected(e);
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        if (selectionListener != null) {
            selectionListener.widgetDefaultSelected(e);
        }
    }

}
