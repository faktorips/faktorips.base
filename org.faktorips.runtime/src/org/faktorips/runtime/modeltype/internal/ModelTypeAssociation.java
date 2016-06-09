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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsInverseAssociation;
import org.faktorips.runtime.model.annotation.IpsMatchingAssociation;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeAssociation extends ModelPart implements IModelTypeAssociation {

    private final IpsAssociation annotation;

    private final Method getter;

    public ModelTypeAssociation(ModelType modelType, Method getterMethod) {
        super(getAssociationAnnotation(getterMethod).name(), modelType, getterMethod
                .getAnnotation(IpsExtensionProperties.class));
        this.annotation = getAssociationAnnotation(getterMethod);
        getter = getterMethod;
    }

    private static IpsAssociation getAssociationAnnotation(Method getterMethod) {
        return getterMethod.getAnnotation(IpsAssociation.class);
    }

    @Override
    public String getLabelForPlural(Locale locale) {
        return getDocumentation(locale, DocumentationType.PLURAL_LABEL, getNamePlural());
    }

    @Override
    public AssociationType getAssociationType() {
        return annotation.type();
    }

    @Override
    public int getMinCardinality() {
        return annotation.min();
    }

    @Override
    public int getMaxCardinality() {
        return annotation.max();
    }

    @Override
    public String getNamePlural() {
        return annotation.pluralName();
    }

    @Override
    public IModelType getTarget() {
        return Models.getModelType(annotation.targetClass());
    }

    @Override
    public List<IModelObject> getTargetObjects(IModelObject source) {
        List<IModelObject> targets = new ArrayList<IModelObject>();
        try {
            Object object = getter.invoke(source);
            if (object instanceof Iterable<?>) {
                for (Object target : (Iterable<?>)object) {
                    targets.add((IModelObject)target);
                }
            } else if (object instanceof IModelObject) {
                targets.add((IModelObject)object);
            }
        } catch (IllegalAccessException e) {
            handleGetterError(source, e);
        } catch (InvocationTargetException e) {
            handleGetterError(source, e);
        }
        return targets;
    }

    private void handleGetterError(IModelObject source, Exception e) {
        throw new IllegalArgumentException(String.format("Could not get target %s on source object %s.", getUsedName(),
                source), e);
    }

    @Override
    public String getUsedName() {
        return isTargetRolePluralRequired() ? getNamePlural() : getName();
    }

    private boolean isTargetRolePluralRequired() {
        return Iterable.class.isAssignableFrom(getter.getReturnType());
    }

    @Override
    public boolean isDerivedUnion() {
        return getter.isAnnotationPresent(IpsDerivedUnion.class);
    }

    @Override
    public boolean isSubsetOfADerivedUnion() {
        return getter.isAnnotationPresent(IpsSubsetOfDerivedUnion.class);
    }

    @Override
    public String getInverseAssociation() {
        if (getter.isAnnotationPresent(IpsInverseAssociation.class)) {
            return getter.getAnnotation(IpsInverseAssociation.class).value();
        } else {
            return null;
        }
    }

    /**
     * @deprecated Since 3.18, use isMatchingAssociationPresent
     */
    @Deprecated
    @Override
    public boolean isProductRelevant() {
        return isMatchingAssociationPresent();
    }

    @Override
    public boolean isMatchingAssociationPresent() {
        return getter.isAnnotationPresent(IpsMatchingAssociation.class);
    }

    @Override
    public String getMatchingAssociationName() {
        if (getter.isAnnotationPresent(IpsMatchingAssociation.class)) {
            return getter.getAnnotation(IpsMatchingAssociation.class).name();
        } else {
            return null;
        }
    }

    @Override
    public String getMatchingAssociationSource() {
        IModelType matchingAssociationSource = getMatchingAssociationSourceType();
        if (matchingAssociationSource != null) {
            return matchingAssociationSource.getName();
        } else {
            return null;
        }
    }

    @Override
    public IModelType getMatchingAssociationSourceType() {
        if (getter.isAnnotationPresent(IpsMatchingAssociation.class)) {
            return Models.getModelType(getter.getAnnotation(IpsMatchingAssociation.class).source());
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getUsedName());
        sb.append(": ");
        sb.append(getTarget().getName());
        sb.append('(');
        sb.append(getAssociationType());
        sb.append(' ');
        if (isDerivedUnion()) {
            sb.append(", Derived Union ");
        }
        if (isSubsetOfADerivedUnion()) {
            sb.append(", Subset of a Derived Union ");
        }
        sb.append(getMinCardinality());
        sb.append("..");
        sb.append(getMaxCardinality() == Integer.MAX_VALUE ? "*" : getMaxCardinality());
        if (isProductRelevant()) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

}
