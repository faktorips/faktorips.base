/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.tablestructure.TableStructureType;

/**
 * Shows a table structure's general properties and allows to edit them.
 * 
 * @author Thorsten Waertel
 */
public class GeneralInfoSection extends IpsSection {

    private final ExtensionPropertyControlFactory extFactory;

    private final ITableStructure tableStructure;

    public GeneralInfoSection(ITableStructure tableStructure, Composite parent, UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);

        this.tableStructure = tableStructure;
        extFactory = new ExtensionPropertyControlFactory(tableStructure);

        initControls();
        setText(Messages.GeneralInfoSection_labelGeneralInfoSection);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        toolkit.createFormLabel(composite, Messages.GeneralInfoSection_labelTableType);
        Combo combo = toolkit.createCombo(composite);

        getBindingContext().bindContent(combo, tableStructure, ITableStructure.PROPERTY_TYPE,
                new TableStructureType[] { TableStructureType.SINGLE_CONTENT, TableStructureType.MULTIPLE_CONTENTS });

        extFactory.createControls(composite, toolkit, tableStructure, IExtensionPropertyDefinition.POSITION_TOP);
        extFactory.bind(getBindingContext());
    }

}
