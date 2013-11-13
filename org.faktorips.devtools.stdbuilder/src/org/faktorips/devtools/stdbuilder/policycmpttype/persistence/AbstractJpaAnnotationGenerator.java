/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.persistence.IPersistenceProvider;

/**
 * Abstract base class for annotation generators.
 */
public abstract class AbstractJpaAnnotationGenerator extends AbstractAnnotationGenerator {

    @Override
    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        return (getPersistenceProvider(ipsElement) != null) && isGenerateAnnotationForInternal(ipsElement);
    }

    IPersistenceProvider getPersistenceProvider(IIpsElement ipsElement) {
        IIpsArtefactBuilderSet ipsArtefactBuilderSet = ipsElement.getIpsProject().getIpsArtefactBuilderSet();
        if (ipsArtefactBuilderSet instanceof StandardBuilderSet) {
            StandardBuilderSet standardBuilderSet = (StandardBuilderSet)ipsArtefactBuilderSet;
            IPersistenceProvider persistenceProviderImpl = standardBuilderSet.getPersistenceProviderImplementation();
            return persistenceProviderImpl;
        }
        return null;
    }

    protected abstract boolean isGenerateAnnotationForInternal(IIpsElement ipsElement);
}
