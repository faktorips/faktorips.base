/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsexport;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.TableFormatConfigurationCompositeFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.runtime.MessageList;

/**
 * Wizard page to configure a table format's properties like a date format or field/text delimiters.
 * 
 * @author Roman Grutza, Alexander Weickmann
 */
public class TableFormatPropertiesPage extends WizardPage implements ValueChangeListener {

    private static final String PAGE_NAME = "TableFormatPropertiesPage"; //$NON-NLS-1$

    private Composite pageControl;

    private TableFormatConfigurationCompositeFactory formatCompositeFactory;

    private UIToolkit toolkit = new UIToolkit(null);

    private ITableFormat tableFormat;

    public TableFormatPropertiesPage(ITableFormat tableFormat) {
        super(PAGE_NAME);
        setTitle(Messages.TableFormatPropertiesPage_title);
        this.tableFormat = tableFormat;
        setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        this.pageControl = toolkit.createGridComposite(parent, 1, false, false);
        setControl(pageControl);

        Group configurationGroup = toolkit
                .createGroup(pageControl, Messages.TableFormatPropertiesPage_configGroupLabel);
        Object layoutData = configurationGroup.getLayoutData();
        if (layoutData instanceof GridData) {
            ((GridData)layoutData).grabExcessVerticalSpace = true;
        }

        formatCompositeFactory = IpsUIPlugin.getDefault().getTableFormatPropertiesControlFactory(tableFormat);
        if (formatCompositeFactory != null) {
            formatCompositeFactory.createPropertyComposite(configurationGroup, toolkit);
            formatCompositeFactory.addValueChangedListener(this);
        }

        pageControl.layout();
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        validatePage();
    }

    private void validatePage() {
        if (getErrorMessage() != null) {
            setErrorMessage(null);
        }
        MessageList list = formatCompositeFactory.validate();
        if (list.containsErrorMsg()) {
            setErrorMessage(list.getMessage(0).getText());
        }
        updatePageComplete();
    }

    private void updatePageComplete() {
        setPageComplete((getErrorMessage() != null) ? false : true);
    }

}
