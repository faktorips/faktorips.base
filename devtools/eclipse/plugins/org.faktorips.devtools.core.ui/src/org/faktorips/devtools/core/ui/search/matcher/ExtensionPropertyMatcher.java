/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyAccess;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;

/**
 * ExtensionPropertyMatcher is a matcher for Extension Properties.
 * <p>
 * The ExtensionPropertyMatcher uses a {@link WildcardMatcher} to check, whether the value of an
 * ExtensionProperty matches a given pattern. If there is no ExtensionProperty, the element does not
 * match.
 * 
 * @author dicker
 */
public class ExtensionPropertyMatcher implements IMatcher<IIpsElement> {

    private final WildcardMatcher wildcardMatcher;

    private final Map<Class<? extends IExtensionPropertyAccess>, Collection<IExtensionPropertyDefinition>> extensionProperties = new HashMap<>();

    public ExtensionPropertyMatcher(WildcardMatcher wildcardMatcher) {
        this.wildcardMatcher = wildcardMatcher;
    }

    @Override
    public boolean isMatching(IIpsElement element) {
        if (!(element instanceof IExtensionPropertyAccess access)) {
            return false;
        }

        for (IExtensionPropertyDefinition extensionPropertyDefinition : getExtensionProperties(access)) {
            if (isMatchingExtensionProperty(access, extensionPropertyDefinition)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMatchingExtensionProperty(IExtensionPropertyAccess access,
            IExtensionPropertyDefinition extensionPropertyDefinition) {
        if (access.isExtPropertyDefinitionAvailable(extensionPropertyDefinition.getPropertyId())) {
            Object propertyValue = access.getExtPropertyValue(extensionPropertyDefinition.getPropertyId());
            if (propertyValue != null && wildcardMatcher.isMatching(propertyValue.toString())) {
                return true;
            }
        }
        return false;
    }

    private Collection<IExtensionPropertyDefinition> getExtensionProperties(IExtensionPropertyAccess element) {
        Class<? extends IExtensionPropertyAccess> clazz = element.getClass();
        if (!extensionProperties.containsKey(clazz)) {
            extensionProperties.put(clazz, element.getExtensionPropertyDefinitions());
        }
        return extensionProperties.get(clazz);
    }
}
