/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;

/**
 * Presentation model object that can be used to associate a UI control with the
 * {@link IProductCmptCategory} an {@link IProductCmptProperty} is assigned to.
 * <p>
 * Using {@link BindingContext} to bind {@link IProductCmptProperty#PROPERTY_CATEGORY} to the
 * control is not sufficient as the getProperty() is of type {@link String} and does not consider
 * implicit assignments to default categories.
 * 
 * @since 3.6
 * 
 * @see IProductCmptCategory
 */
public class CategoryPmo extends IpsObjectPartPmo {

    public static final String PROPERTY_CATEGORY = "category"; //$NON-NLS-1$

    private final List<IProductCmptCategory> categories;

    public CategoryPmo(IProductCmptProperty property) {
        super(property);
        categories = findCategories();
    }

    public IProductCmptProperty getProperty() {
        return (IProductCmptProperty)getIpsObjectPartContainer();
    }

    private List<IProductCmptCategory> findCategories() {
        List<IProductCmptCategory> categoriesList = new ArrayList<>();

        IProductCmptType productCmptType = null;
        try {
            productCmptType = getProperty().findProductCmptType(getProperty().getIpsProject());
        } catch (IpsException e) {
            // Recover by not displaying any categories
            IpsPlugin.log(e);
        }

        if (productCmptType != null) {
            try {
                categoriesList.addAll(productCmptType.findCategories(getProperty().getIpsProject()));
            } catch (IpsException e) {
                // Recover by not displaying any categories
                IpsPlugin.log(e);
            }
        }

        return categoriesList;
    }

    /**
     * Returns the {@link IProductCmptCategory} the {@link IProductCmptProperty} of this
     * {@link CategoryPmo} is assigned to.
     * <p>
     * This method returns the default {@link IProductCmptCategory} corresponding to the
     * {@link IProductCmptProperty} if no specific {@link IProductCmptCategory} could be found.
     * <p>
     * Returns null if no default {@link IProductCmptCategory} could be found as well.
     * 
     * @return the {@link IProductCmptCategory} the {@link IProductCmptProperty} of this
     *         {@link CategoryPmo} is assigned to or null if neither a specific
     *         {@link IProductCmptCategory} nor the corresponding default
     *         {@link IProductCmptCategory} could be found
     */
    public IProductCmptCategory getCategory() {
        IProductCmptCategory correspondingDefaultCategory = null;
        for (IProductCmptCategory category : categories) {
            if (correspondingDefaultCategory == null && category.isDefaultFor(getProperty())) {
                correspondingDefaultCategory = category;
            }
            if (category.getName().equals(getProperty().getCategory())) {
                return category;
            }
        }
        return correspondingDefaultCategory;
    }

    /**
     * Returns the categories of the {@link IProductCmptType} the {@link IProductCmptProperty} of
     * this {@link CategoryPmo} belongs to.
     * <p>
     * This method includes categories defined in the supertype hierarchy. The categories are
     * determined at the construction of the PMO and cached.
     */
    public List<IProductCmptCategory> getCategories() {
        return new ArrayList<>(categories);
    }

    /**
     * Assigns the {@link IProductCmptProperty} of this {@link CategoryPmo} to the indicated
     * {@link IProductCmptCategory}.
     */
    public void setCategory(IProductCmptCategory category) {
        getProperty().setCategory(category.getName());
    }

}