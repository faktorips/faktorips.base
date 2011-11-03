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
import java.util.Arrays;
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
import org.faktorips.devtools.core.ui.IpsUIPlugin;

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
        super.okPressed();
        changeCategory();
    }

    private void changeCategory() {
        if (initialCategory.equals(getSelectedCategory())) {
            return;
        }

        if (property.isPolicyCmptTypeProperty()) {
            changeCategoryOfPolicyCmptTypeProperty();
        } else {
            changeCategoryOfProductCmptTypeProperty();
        }
    }

    private void changeCategoryOfPolicyCmptTypeProperty() {
        /*
         * If the property originates from a policy component type, dirty editors editing the policy
         * component type must be saved.
         */
        boolean saved = IpsUIPlugin.getDefault().saveEditors(Arrays.asList(property.getIpsSrcFile()));
        if (!saved) {
            return;
        }

        property.setCategory(getSelectedCategory().getName());
        try {
            property.getIpsSrcFile().save(true, null);
        } catch (CoreException e) {
            // The category change could not be saved, so restore the old category
            IpsPlugin.logAndShowErrorDialog(e);
            property.setCategory(initialCategory.getName());
            property.getIpsSrcFile().markAsClean();
        }
    }

    private void changeCategoryOfProductCmptTypeProperty() {
        property.setCategory(getSelectedCategory().getName());
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
            return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel((IProductCmptCategory)element);
        }

    }

}
