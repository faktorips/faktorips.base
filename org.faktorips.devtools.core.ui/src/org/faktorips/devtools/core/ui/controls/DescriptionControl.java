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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.Described;

public class DescriptionControl extends Composite implements ModifyListener {

    private Described describedObject;
    private Text descriptionText;

    public DescriptionControl(Described describedObject, Composite parent) {
        super(parent, SWT.NONE);
        this.describedObject = describedObject;
        if (parent.getLayout() instanceof GridLayout) {
            setLayoutData(new GridData(GridData.FILL_BOTH));
        }
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        Group group = new Group(this, SWT.NONE);
        group.setText(Messages.DescriptionControl_title);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setLayout(new GridLayout(1, false));
        descriptionText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        GridData data = new GridData(SWT.LEFT, SWT.TOP, false, true);
        data.heightHint = 100;
        data.widthHint = 150;
        descriptionText.setLayoutData(data);
        descriptionText.setText(describedObject.getDescription());
        descriptionText.addModifyListener(this);
    }

    @Override
    public void modifyText(ModifyEvent e) {
        describedObject.setDescription(descriptionText.getText());
    }

}
