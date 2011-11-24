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

package org.faktorips.devtools.core.internal.model.type;

import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypePart;
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
     * Implementation of {@link IProductCmptProperty#hasCategory()}.
     * <p>
     * This method is implemented in {@link TypePart} because - as we cannot use multiple
     * inheritance - we are not able to add a base implementation for product component properties
     * to our class hierarchy. Type parts that implement the {@link IProductCmptProperty} interface
     * immediately gain this implementation.
     * 
     * @see IProductCmptProperty
     */
    public boolean hasCategory() {
        return !category.isEmpty();
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
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        element.setAttribute(PROPERTY_CATEGORY, category);
    }

}
