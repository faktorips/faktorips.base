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

package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;

/**
 * An Interface for factories that are capable of creating an {@link IIpsArtefactBuilder}. It is
 * used for the extension point "org.faktorips.devtools.stdbuilder.artefactBuilderFactory", that
 * allows to register custom factories and thus custom artifact builders with the FaktorIPS standard
 * builder set.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public interface IIpsArtefactBuilderFactory {
    /**
     * Creates an {@link IIpsArtefactBuilder} for the given builder set.
     * 
     * @param builderSet the new builder's builder set
     * @return a custom IPS artifact builder.
     */
    public IIpsArtefactBuilder createBuilder(StandardBuilderSet builderSet);
}
