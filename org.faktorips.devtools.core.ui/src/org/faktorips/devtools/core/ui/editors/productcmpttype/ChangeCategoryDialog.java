/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;

/**
 * A dialog that allows the user to select one {@link IProductCmptCategory} out of all categories of
 * an {@link IProductCmptType}.
 * 
 * @since 3.6
 */
public class ChangeCategoryDialog extends ElementListSelectionDialog {

    private final List<IProductCmptCategory> categories = new ArrayList<>();

    private final IProductCmptType productCmptType;

    private final IProductCmptProperty property;

    private final IProductCmptCategory initialCategory;

    public ChangeCategoryDialog(IProductCmptType productCmptType, IProductCmptProperty property,
            IProductCmptCategory initialCategory, Shell shell) {

        super(shell, new CategoryLabelProvider());

        this.productCmptType = productCmptType;
        this.property = property;
        this.initialCategory = initialCategory;

        setTitle(Messages.ChangeCategoryDialog_title);
        setMessage(Messages.ChangeCategoryDialog_message);

        setElements();
    }

    private void setElements() {
        try {
            categories.addAll(productCmptType.findCategories(productCmptType.getIpsProject()));
        } catch (IpsException e) {
            // Recover by not showing any categories, just log the exception
            IpsPlugin.log(e);
        }
        setElements(categories.toArray());
    }

    // Overridden to set the initial selection
    @Override
    protected Control createDialogArea(Composite parent) {
        Control dialogArea = super.createDialogArea(parent);
        setSelection(new IProductCmptCategory[] { initialCategory });
        return dialogArea;
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        productCmptType.changeCategoryAndDeferPolicyChange(property, getSelectedCategory().getName());
    }

    /**
     * Returns the {@link IProductCmptCategory} the user has selected.
     * <p>
     * Only available after the dialog has been closed by the user pressing the OK button.
     */
    public IProductCmptCategory getSelectedCategory() {
        return (IProductCmptCategory)getFirstResult();
    }

    private static class CategoryLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel((IProductCmptCategory)element);
        }

    }

}
