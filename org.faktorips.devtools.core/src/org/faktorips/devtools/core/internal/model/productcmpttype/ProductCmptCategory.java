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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IProductCmptProperty}, please see the interface for more details.
 * 
 * @author Alexander Weickmann
 */
public final class ProductCmptCategory extends AtomicIpsObjectPart implements IProductCmptCategory {

    final static String TAG_NAME = "Category"; //$NON-NLS-1$

    private final List<IProductCmptProperty> assignedProperties = new ArrayList<IProductCmptProperty>(5);

    private boolean inherited;

    private boolean defaultForMethods;

    private boolean defaultForValidationRules;

    private boolean defaultForTableStructureUsages;

    private boolean defaultForPolicyCmptTypeAttributes;

    private boolean defaultForProductCmptTypeAttributes;

    public ProductCmptCategory(IProductCmptType parent, String id) {
        super(parent, id);
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public List<IProductCmptProperty> getAssignedProductCmptProperties() {
        return Collections.unmodifiableList(assignedProperties);
    }

    @Override
    public IProductCmptProperty getAssignedProductCmptProperty(String name) {
        for (IProductCmptProperty property : assignedProperties) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    @Override
    public List<IProductCmptProperty> findAllAssignedProductCmptProperties(IIpsProject ipsProject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IProductCmptProperty findAssignedProductCmptProperty(String name, IIpsProject ipsProject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean assignProductCmptProperty(IProductCmptProperty productCmptProperty) {
        if (assignedProperties.contains(productCmptProperty)) {
            return false;
        }
        return assignedProperties.add(productCmptProperty);
    }

    @Override
    public boolean removeProductCmptProperty(IProductCmptProperty productCmptProperty) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInherited() {
        return inherited;
    }

    @Override
    public void setInherited(boolean inherited) {
        boolean oldValue = this.inherited;
        this.inherited = inherited;
        valueChanged(oldValue, inherited);
    }

    @Override
    public boolean isDefaultForMethods() {
        return defaultForMethods;
    }

    @Override
    public void setDefaultForMethods(boolean defaultForMethods) {
        boolean oldValue = this.defaultForMethods;
        this.defaultForMethods = defaultForMethods;
        valueChanged(oldValue, defaultForMethods);
    }

    @Override
    public boolean isDefaultForPolicyCmptTypeAttributes() {
        return defaultForPolicyCmptTypeAttributes;
    }

    @Override
    public void setDefaultForPolicyCmptTypeAttributes(boolean defaultForPolicyCmptTypeAttributes) {
        boolean oldValue = this.defaultForPolicyCmptTypeAttributes;
        this.defaultForPolicyCmptTypeAttributes = defaultForPolicyCmptTypeAttributes;
        valueChanged(oldValue, defaultForPolicyCmptTypeAttributes);
    }

    @Override
    public boolean isDefaultForProductCmptTypeAttributes() {
        return defaultForProductCmptTypeAttributes;
    }

    @Override
    public void setDefaultForProductCmptTypeAttributes(boolean defaultForProductCmptTypeAttributes) {
        boolean oldValue = this.defaultForProductCmptTypeAttributes;
        this.defaultForProductCmptTypeAttributes = defaultForProductCmptTypeAttributes;
        valueChanged(oldValue, defaultForProductCmptTypeAttributes);
    }

    @Override
    public boolean isDefaultForTableStructureUsages() {
        return defaultForTableStructureUsages;
    }

    @Override
    public void setDefaultForTableStructureUsages(boolean defaultForTableStructureUsages) {
        boolean oldValue = this.defaultForTableStructureUsages;
        this.defaultForTableStructureUsages = defaultForTableStructureUsages;
        valueChanged(oldValue, defaultForTableStructureUsages);
    }

    @Override
    public boolean isDefaultForValidationRules() {
        return defaultForValidationRules;
    }

    @Override
    public void setDefaultForValidationRules(boolean defaultForValidationRules) {
        boolean oldValue = this.defaultForValidationRules;
        this.defaultForValidationRules = defaultForValidationRules;
        valueChanged(oldValue, defaultForValidationRules);
    }

    @Override
    public void setSide(Side side) {
        // TODO Auto-generated method stub

    }

    @Override
    public Side getSide() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAtLeftSide() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAtRightSide() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected Element createElement(Document doc) {
        // TODO Auto-generated method stub
        return null;
    }

}
