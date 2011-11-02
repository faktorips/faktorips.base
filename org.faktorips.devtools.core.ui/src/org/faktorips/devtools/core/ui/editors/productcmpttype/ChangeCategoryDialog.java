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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * A dialog that allows the user to select one {@link IProductCmptCategory} out of all
 * {@link IProductCmptCategory}s of an {@link IProductCmptType}.
 * 
 * @author Alexander Weickmann
 */
public class ChangeCategoryDialog extends ElementListSelectionDialog {

    private final List<IProductCmptCategory> categories = new ArrayList<IProductCmptCategory>();

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
            categories.addAll(productCmptType.findProductCmptCategories(productCmptType.getIpsProject()));
        } catch (CoreException e) {
            // Recover by not showing any categories, just log the exception
            IpsPlugin.log(e);
        }
        setElements(categories.toArray());
    }

    // Overridden to set initial selection
    @Override
    protected Control createDialogArea(Composite parent) {
        Control dialogArea = super.createDialogArea(parent);
        setSelection(new IProductCmptCategory[] { initialCategory });
        return dialogArea;
    }

    @Override
    protected void okPressed() {
        // TODO AW NPE
        if (!initialCategory.equals(getSelectedCategory())) {
            property.setCategory(getSelectedCategory().getName());
        }
        super.okPressed();
    }

    private IProductCmptCategory getSelectedCategory() {
        return (IProductCmptCategory)getFirstResult();
    }

    private static class CategoryLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel((IProductCmptCategory)element);
        }

    }

}
