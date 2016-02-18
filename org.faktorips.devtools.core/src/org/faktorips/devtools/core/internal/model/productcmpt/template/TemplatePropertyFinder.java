/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.template;

import static com.google.common.collect.Iterables.find;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValue;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValueContainer;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;

public class TemplatePropertyFinder<P extends ITemplatedValue, C extends ITemplatedValueContainer> extends
        TemplateHierarchyVisitor<C> {

    private final P originalPropertyValue;
    private P resultingPropertyValue;
    private final Function<C, P> propertyFinder;

    public TemplatePropertyFinder(P originalPropertyValue, Function<C, P> propertyFinder, IIpsProject ipsProject) {
        super(ipsProject);
        this.originalPropertyValue = originalPropertyValue;
        this.propertyFinder = propertyFinder;
    }

    @Override
    protected boolean visit(C currentValueContainer) {
        if (originalPropertyValue.getTemplatedValueContainer() == currentValueContainer) {
            /*
             * Ignore property value on which the search was started and continue searching.
             */
            return true;
        }
        P currentValue = propertyFinder.apply(currentValueContainer);
        if (currentValue == null || isInherited(currentValue)) {
            return true;
        } else if (isDefined(currentValue)) {
            this.resultingPropertyValue = currentValue;
        }
        return false;
    }

    private boolean isDefined(P value) {
        return value.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    private boolean isInherited(P value) {
        return value.getTemplateValueStatus() == TemplateValueStatus.INHERITED;
    }

    public P getPropertyValue() {
        return resultingPropertyValue;
    }

    public static <U extends IPropertyValue> U findTemplatePropertyValue(U originalValue, Class<U> propertyType) {
        String propertyName = originalValue.getPropertyName();
        PropertyValueFinder<U> propertyValueFinder = new PropertyValueFinder<U>(propertyName, propertyType);
        TemplatePropertyFinder<U, IPropertyValueContainer> finder = new TemplatePropertyFinder<U, IPropertyValueContainer>(
                originalValue, propertyValueFinder, originalValue.getIpsProject());
        finder.start(originalValue.getPropertyValueContainer());
        return finder.getPropertyValue();
    }

    public static IProductCmptLink findTemplateLink(IProductCmptLink originalLink) {
        LinkFinder propertyValueFinder = new LinkFinder(originalLink.getAssociation(), originalLink.getTarget());
        TemplatePropertyFinder<IProductCmptLink, IProductCmptLinkContainer> finder = new TemplatePropertyFinder<IProductCmptLink, IProductCmptLinkContainer>(
                originalLink, propertyValueFinder, originalLink.getIpsProject());
        finder.start(originalLink.getProductCmptLinkContainer());
        return finder.getPropertyValue();
    }

    static final class PropertyValueFinder<T extends IPropertyValue> implements Function<IPropertyValueContainer, T> {

        private final String propertyName;
        private final Class<T> propertyType;

        public PropertyValueFinder(String propertyName, Class<T> propertyType) {
            this.propertyName = propertyName;
            this.propertyType = propertyType;
        }

        @Override
        public T apply(IPropertyValueContainer currentValueContainer) {
            if (currentValueContainer != null) {
                return currentValueContainer.getPropertyValue(propertyName, propertyType);
            } else {
                return null;
            }
        }

    }

    static final class LinkFinder implements Function<IProductCmptLinkContainer, IProductCmptLink> {

        private final String association;
        private final String target;

        public LinkFinder(String association, String target) {
            this.association = association;
            this.target = target;
        }

        @Override
        public IProductCmptLink apply(IProductCmptLinkContainer currentValueContainer) {
            if (currentValueContainer != null) {
                List<IProductCmptLink> linksAsList = currentValueContainer.getLinksAsList(association);
                return find(linksAsList, sameTarget(), null);
            } else {
                return null;
            }
        }

        protected Predicate<IProductCmptLink> sameTarget() {
            return new Predicate<IProductCmptLink>() {

                @Override
                public boolean apply(IProductCmptLink link) {
                    if (link == null || link.getTarget() == null) {
                        return false;
                    } else {
                        return link.getTarget().equals(target);
                    }
                }
            };
        }
    }

}
