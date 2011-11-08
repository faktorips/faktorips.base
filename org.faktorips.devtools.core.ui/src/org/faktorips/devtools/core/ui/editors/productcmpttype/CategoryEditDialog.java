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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

/**
 * TODO AW
 * 
 * @author Alexander Weickmann
 */
public class CategoryEditDialog extends IpsPartEditDialog2 {

    public CategoryEditDialog(IProductCmptCategory category, Shell parentShell) {
        super(category, parentShell, Messages.CategoryEditDialog_title, true);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder tabFolder = (TabFolder)parent;

        TabItem generalPage = new TabItem(tabFolder, SWT.NONE);
        generalPage.setText(Messages.CategoryEditDialog_generalTabTitle);
        generalPage.setControl(createGeneralPage(tabFolder));

        return tabFolder;
    }

    private Control createGeneralPage(TabFolder tabFolder) {
        Composite page = createTabItemComposite(tabFolder, 1, false);

        Composite nameComposite = getToolkit().createLabelEditColumnComposite(page);
        getToolkit().createLabel(nameComposite, "Name:");
        getToolkit().createText(nameComposite);

        getToolkit().createVerticalSpacer(page, 10);

        Group defaultsGroup = getToolkit().createGridGroup(page, "Defaults", 1, false);
        getToolkit().createLabel(
                defaultsGroup,
                "Properties without a designated category are automatically assigned "
                        + "to the corresponding default category.");
        Composite defaultsComposite = getToolkit().createGridComposite(defaultsGroup, 1, false, true);
        getToolkit().createCheckbox(defaultsComposite, "Product Component Type Attributes");
        getToolkit().createCheckbox(defaultsComposite, "Formula Signature Definitions");
        getToolkit().createCheckbox(defaultsComposite, "Table Structure Usages");
        getToolkit().createCheckbox(defaultsComposite, "Policy Component Type Attributes (changeable & configurable)");
        getToolkit().createCheckbox(defaultsComposite, "Validation Rules (configurable)");

        return page;
    }
}
