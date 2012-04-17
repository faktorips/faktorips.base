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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;

/**
 * Default implementation of {@link IEnumValueSetProvider}. Supports the case where an
 * {@link IEnumValueSet} in the model (policy component type attribute) is configured by a product
 * component (config element).
 * 
 * @author Stefan Widmaier
 */
public class DefaultEnumValueSetProvider implements IEnumValueSetProvider {

    private final IConfigElement configElement;

    public DefaultEnumValueSetProvider(IConfigElement configElement) {
        this.configElement = configElement;
    }

    /**
     * Searches the {@link IPolicyCmptTypeAttribute} (of the {@link IConfigElement}) and returns its
     * IEnumValueSet or <code>null</code> if it does not define one. {@inheritDoc}
     */
    @Override
    public IEnumValueSet getSourceEnumValueSet() {
        try {
            IPolicyCmptTypeAttribute attribute = configElement.findPcTypeAttribute(configElement.getIpsProject());
            IEnumValueSet sourceSet = (IEnumValueSet)attribute.getValueSet();
            return sourceSet;
        } catch (CoreException e) {
            // FIXME throw CoreRuntimeException (FIPS 3.6)
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the {@link IConfigElement} this provider was created with. {@inheritDoc}
     */
    @Override
    public IConfigElement getTargetConfigElement() {
        return configElement;
    }

    @Override
    public String getSourceLabel() {
        return Messages.DefaultsAndRangesEditDialog_additionalValuesDefinedInModel;
    }

    @Override
    public String getTargetLabel() {
        return Messages.DefaultsAndRangesEditDialog_valueDefinedInProductCmpt;
    }

}
