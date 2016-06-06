/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeAssociation extends AbstractModelElement implements IModelTypeAssociation {

    private final Map<Locale, String> pluralLabelsByLocale = new HashMap<Locale, String>();

    private ModelType modelType;
    private AssociationType associationType = AssociationType.Association;
    private int minCardinality = 0;
    private int maxCardinality = Integer.MAX_VALUE;
    private String namePlural = null;
    private String targetJavaClassName = null;
    private boolean isProductRelevant = false;
    private boolean isDerivedUnion = false;
    private boolean isSubsetOfADerivedUnion = false;
    private Boolean isTargetRolePluralRequired = false;
    private String inverseAssociation;
    private String matchingAssociationName;
    private String matchingAssociationSource;

    private String getterName;

    public ModelTypeAssociation(String name, ModelType modelType) {
        super(name);
        this.modelType = modelType;
    }

    @Override
    public String getLabelForPlural(Locale locale) {
        String label = pluralLabelsByLocale.get(locale);
        return IpsStringUtils.isEmpty(label) ? getNamePlural() : label;
    }

    @Override
    public IModelType getModelType() {
        return modelType;
    }

    @Override
    public AssociationType getAssociationType() {
        return associationType;
    }

    @Override
    public int getMaxCardinality() {
        return maxCardinality;
    }

    @Override
    public int getMinCardinality() {
        return minCardinality;
    }

    @Override
    public String getNamePlural() {
        return namePlural;
    }

    @Override
    public IModelType getTarget() throws ClassNotFoundException {
        return Models.getModelType(targetJavaClassName);
    }

    @Override
    public List<IModelObject> getTargetObjects(IModelObject source) {
        List<IModelObject> targets = new ArrayList<IModelObject>();
        try {
            Object object = getGetter(source).invoke(source);
            if (object instanceof Iterable<?>) {
                for (Object target : (Iterable<?>)object) {
                    targets.add((IModelObject)target);
                }
            } else if (object instanceof IModelObject) {
                targets.add((IModelObject)object);
            }
        } catch (IntrospectionException e) {
            handleGetterError(source, e);
        } catch (IllegalArgumentException e) {
            handleGetterError(source, e);
        } catch (IllegalAccessException e) {
            handleGetterError(source, e);
        } catch (InvocationTargetException e) {
            handleGetterError(source, e);
        }
        return targets;
    }

    private Method getGetter(IModelObject source) throws IntrospectionException {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(getUsedName(), source.getClass(),
                getGetterName(), null);
        return propertyDescriptor.getReadMethod();
    }

    private String getGetterName() {
        if (getterName == null) {
            getterName = "get" + getUsedName().substring(0, 1).toUpperCase() + getUsedName().substring(1);
        }
        return getterName;
    }

    private void handleGetterError(IModelObject source, Exception e) {
        throw new IllegalArgumentException(String.format("Could not get target %s on source object %s.", getUsedName(),
                source), e);
    }

    @Override
    public boolean isProductRelevant() {
        return isProductRelevant;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getUsedName());
        sb.append(": ");
        sb.append(targetJavaClassName);
        sb.append('(');
        sb.append(associationType);
        sb.append(' ');
        if (isDerivedUnion) {
            sb.append(", Derived Union ");
        }
        if (isSubsetOfADerivedUnion) {
            sb.append(", Subset of a Derived Union ");
        }
        sb.append(minCardinality);
        sb.append("..");
        sb.append(maxCardinality == Integer.MAX_VALUE ? "*" : maxCardinality);
        if (isProductRelevant) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String getUsedName() {
        return isTargetRolePluralRequired ? getNamePlural() : getName();
    }

    @Override
    public boolean isDerivedUnion() {
        return isDerivedUnion;
    }

    @Override
    public boolean isSubsetOfADerivedUnion() {
        return isSubsetOfADerivedUnion;
    }

    @Override
    public String getInverseAssociation() {
        return inverseAssociation;
    }

    @Override
    public String getMatchingAssociationName() {
        return matchingAssociationName;
    }

    @Override
    public String getMatchingAssociationSource() {
        return matchingAssociationSource;
    }
}
