/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.model.Described;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * A section to edit a description.
 * 
 * @author Jan Ortmann
 */
public class DescriptionSection extends IpsSection {

    private Text descriptionText;
    private Described describedObj;

    public DescriptionSection(Described described, Composite parent, UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        this.describedObj = described;
        initControls();
        setText(Messages.DescriptionSection_description);
    }

    public DescriptionSection(Described described, Composite parent, int style, UIToolkit toolkit) {
        super(parent, style, GridData.FILL_BOTH, toolkit);
        this.describedObj = described;
        initControls();
        setText(Messages.DescriptionSection_description);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.ui.forms.IpsSection#initClientComposite(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 1;
        client.setLayout(layout);
        descriptionText = toolkit.getFormToolkit().createText(client,
                "", SWT.WRAP | SWT.MULTI | SWT.H_SCROLL | SWT.FLAT); //$NON-NLS-1$
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 100;
        descriptionText.setLayoutData(data);
        // following line forces the paint listener to draw a light grey border around
        // the text control. Can only be understood by looking at the FormToolkit.PaintBorder class.
        descriptionText.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        descriptionText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (isRefreshing()) {
                    return;
                }
                if (!ObjectUtils.equals(describedObj.getDescription(), descriptionText.getText())) {
                    describedObj.setDescription(descriptionText.getText());
                }
            }

        });
    }

    @Override
    protected void performRefresh() {
        if (describedObj == null) {
            return;
        }
        if (ObjectUtils.equals(descriptionText.getText(), describedObj.getDescription())) {
            return;
        }
        descriptionText.setText(describedObj.getDescription());
    }

    public void setDescribedObject(Described object) {
        describedObj = object;
        performRefresh();
    }

}
