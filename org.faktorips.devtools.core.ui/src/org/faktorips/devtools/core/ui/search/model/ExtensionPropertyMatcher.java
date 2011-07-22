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

package org.faktorips.devtools.core.ui.search.model;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;

/**
 * Matcher for Extension Properties
 * 
 * @author dicker
 */
public class ExtensionPropertyMatcher {
    private final WildcardMatcher wildcardMatcher;
    private final Map<Class<? extends IExtensionPropertyAccess>, IExtensionPropertyDefinition[]> extensionProperties = new HashMap<Class<? extends IExtensionPropertyAccess>, IExtensionPropertyDefinition[]>();
    private final IIpsModel ipsModel;

    public ExtensionPropertyMatcher(WildcardMatcher wildcardMatcher, IIpsModel ipsModel) {
        this.wildcardMatcher = wildcardMatcher;
        this.ipsModel = ipsModel;
    }

    public boolean isMatchingElement(IIpsElement element) {
        if (!(element instanceof IExtensionPropertyAccess)) {
            return false;
        }

        IExtensionPropertyAccess access = (IExtensionPropertyAccess)element;

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

    private IExtensionPropertyDefinition[] getExtensionProperties(IExtensionPropertyAccess element) {
        Class<? extends IExtensionPropertyAccess> clazz = element.getClass();
        if (!extensionProperties.containsKey(clazz)) {
            extensionProperties.put(clazz, ipsModel.getExtensionPropertyDefinitions(clazz, true));
        }
        return extensionProperties.get(clazz);
    }

}
