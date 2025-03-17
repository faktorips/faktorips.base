/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.devtools.model.productcmpt.template.TemplateHierarchyVisitor;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

/**
 * A TemplatePropertyFinder finds the value that is <em>defined</em> in the closest parent-template
 * of the product component (or template) a given value is part of. When the given value does not
 * belong to a product component (or a template) based on a parent-template, no value is found. If
 * none of the parent-templates defines a value, no value is found.
 */
public class TemplateValueFinder<V extends ITemplatedValue, C extends ITemplatedValueContainer>
        extends TemplateHierarchyVisitor<C> {

    private final V originalValue;
    private final Class<V> valueClass;
    private V resultingValue;
    private boolean knownInTemplate;
    private final ITemplatedValueIdentifier valueIdentifier;

    public TemplateValueFinder(V originalPropertyValue, Class<V> valueClass, ITemplatedValueIdentifier valueIdentifier,
            IIpsProject ipsProject) {
        super(ipsProject);
        originalValue = originalPropertyValue;
        this.valueClass = valueClass;
        this.valueIdentifier = valueIdentifier;
    }

    @Override
    protected boolean visit(C currentValueContainer) {
        if (originalValue.getTemplatedValueContainer() == currentValueContainer) {
            // Ignore container on which the search was started and continue searching.
            return true;
        }

        V currentValue = valueClass.cast(valueIdentifier.getValueFrom(currentValueContainer));
        if (currentValue == null || isInherited(currentValue)) {
            return true;
        } else if (isDefined(currentValue)) {
            resultingValue = currentValue;
            knownInTemplate = true;
        } else if (isUndefined(currentValue)) {
            knownInTemplate = true;
        }
        return false;
    }

    private boolean isDefined(V value) {
        return value.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    private boolean isUndefined(V value) {
        return value.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED;
    }

    private boolean isInherited(V value) {
        return value.getTemplateValueStatus() == TemplateValueStatus.INHERITED;
    }

    public V getTemplateValue() {
        return resultingValue;
    }

    /**
     * Returns whether any template in the visited hierarchy has a value (even if it is
     * {@link TemplateValueStatus#UNDEFINED}) for the property.
     */
    public boolean isKnownInTemplate() {
        return knownInTemplate;
    }

    public static <U extends ITemplatedValue> U findTemplateValue(U originalValue, Class<U> valueClass) {
        TemplateValueFinder<U, ITemplatedValueContainer> finder = new TemplateValueFinder<>(
                originalValue, valueClass, originalValue.getIdentifier(), originalValue.getIpsProject());
        finder.start(originalValue.getTemplatedValueContainer());
        return finder.getTemplateValue();
    }

    public static <U extends ITemplatedValue> boolean hasTemplateForValue(U originalValue, Class<U> valueClass) {
        TemplateValueFinder<U, ITemplatedValueContainer> finder = new TemplateValueFinder<>(
                originalValue, valueClass, originalValue.getIdentifier(), originalValue.getIpsProject());
        finder.start(originalValue.getTemplatedValueContainer());
        return finder.isKnownInTemplate();
    }

}
