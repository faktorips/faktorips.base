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

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

/**
 * Presentation model object that can be used to associate a UI control with the
 * {@link IProductCmptCategory} an {@link IProductCmptProperty} is assigned to.
 * <p>
 * Using {@link BindingContext} to bind {@link IProductCmptProperty#PROPERTY_CATEGORY} to the
 * control is not sufficient as the property is of type {@link String} and does not consider
 * implicit assignments to default categories.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann, Faktor Zehn AG
 * 
 * @see IProductCmptCategory
 */
public class CategoryPmo extends PresentationModelObject {

    public static final String PROPERTY_CATEGORY = "category"; //$NON-NLS-1$

    private final IProductCmptProperty property;

    private final List<IProductCmptCategory> categories;

    public CategoryPmo(IProductCmptProperty property) {
        this.property = property;
        categories = findCategories();
    }

    private List<IProductCmptCategory> findCategories() {
        List<IProductCmptCategory> categories = new ArrayList<IProductCmptCategory>();

        IProductCmptType productCmptType = null;
        try {
            productCmptType = property.findProductCmptType(property.getIpsProject());
        } catch (CoreException e) {
            // Recover by not displaying any categories
            IpsPlugin.log(e);
        }

        if (productCmptType != null) {
            try {
                categories.addAll(productCmptType.findCategories(property.getIpsProject()));
            } catch (CoreException e) {
                // Recover by not displaying any categories
                IpsPlugin.log(e);
            }
        }

        return categories;
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
            if (correspondingDefaultCategory == null && category.isDefaultFor(property)) {
                correspondingDefaultCategory = category;
            }
            if (category.getName().equals(property.getCategory())) {
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
        return new ArrayList<IProductCmptCategory>(categories);
    }

    /**
     * Assigns the {@link IProductCmptProperty} of this {@link CategoryPmo} to the indicated
     * {@link IProductCmptCategory}.
     */
    public void setCategory(IProductCmptCategory category) {
        property.setCategory(category.getName());
    }

}