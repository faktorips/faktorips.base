/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypePart;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Element;

/**
 * Abstract base class for all IPS object parts belonging to policy component types or product
 * component types.
 * 
 * @author Alexander Weickmann
 */
public abstract class TypePart extends BaseIpsObjectPart implements ITypePart {

    private Modifier modifier = Modifier.PUBLISHED;

    private String category = ""; //$NON-NLS-1$
    private int categoryPosition = -1;

    protected TypePart(IType parent, String id) {
        super(parent, id);
    }

    @Override
    public IType getType() {
        return (IType)getParent();
    }

    @Override
    public boolean isOfType(QualifiedNameType qualifiedNameType) {
        return qualifiedNameType.equals(getType().getQualifiedNameType());
    }

    @Override
    public Modifier getModifier() {
        return modifier;
    }

    @Override
    public void setModifier(Modifier modifier) {
        Modifier oldModifier = this.modifier;
        this.modifier = modifier;
        valueChanged(oldModifier, modifier);
    }

    /**
     * Implementation of {@link IProductCmptProperty#setCategory(String)}.
     * <p>
     * This method is implemented in {@link TypePart} because - as we cannot use multiple
     * inheritance - we are not able to add a base implementation for product component properties
     * to our class hierarchy. Type parts that implement the {@link IProductCmptProperty} interface
     * immediately gain this implementation.
     * 
     * @see IProductCmptProperty
     */
    public void setCategory(String category) {
        String oldValue = this.category;
        this.category = category;
        valueChanged(oldValue, category, PROPERTY_CATEGORY);
    }

    /**
     * Implementation of {@link IProductCmptProperty#getCategory()}.
     * <p>
     * This method is implemented in {@link TypePart} because - as we cannot use multiple
     * inheritance - we are not able to add a base implementation for product component properties
     * to our class hierarchy. Type parts that implement the {@link IProductCmptProperty} interface
     * immediately gain this implementation.
     * 
     * @see IProductCmptProperty
     */
    public String getCategory() {
        return category;
    }

    /**
     * This method is implemented in {@link TypePart} because - as we cannot use multiple
     * inheritance - we are not able to add a base implementation for product component properties
     * to our class hierarchy. Type parts that implement the {@link IProductCmptProperty} interface
     * immediately gain this implementation.
     */
    public boolean hasCategory() {
        return !category.isEmpty();
    }

    public int getCategoryPosition() {
        return categoryPosition;
    }

    public void setCategoryPosition(int categoryPosition) {
        int oldValue = this.categoryPosition;
        this.categoryPosition = categoryPosition;
        valueChanged(oldValue, categoryPosition, PROPERTY_CATEGORY_POSITION);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        initModifierFromXml(element);
        initCategoryFromXml(element);
    }

    private void initModifierFromXml(Element element) {
        modifier = Modifier.getModifier(element.getAttribute(PROPERTY_MODIFIER));
        if (modifier == null) {
            modifier = Modifier.PUBLISHED;
        }
    }

    private void initCategoryFromXml(Element element) {
        String categoryAttribute = element.getAttribute(PROPERTY_CATEGORY);
        category = categoryAttribute != null ? categoryAttribute : ""; //$NON-NLS-1$
        String categoryPositionAttribute = element.getAttribute(PROPERTY_CATEGORY_POSITION);
        categoryPosition = IpsStringUtils.isNotBlank(categoryPositionAttribute)
                ? Integer.parseInt(categoryPositionAttribute)
                : -1;
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        if (IpsStringUtils.isNotEmpty(category)) {
            element.setAttribute(PROPERTY_CATEGORY, category);
        }
        if (categoryPosition > 0) {
            element.setAttribute(PROPERTY_CATEGORY_POSITION, Integer.toString(categoryPosition));
        }
    }
}
