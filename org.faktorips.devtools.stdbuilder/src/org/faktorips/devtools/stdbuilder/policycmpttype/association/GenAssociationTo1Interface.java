/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class GenAssociationTo1Interface extends GenAssociationTo1 {

    public GenAssociationTo1Interface(IPolicyCmptTypeAssociation association, PolicyCmptInterfaceBuilder builder,
            LocalizedStringsSet stringsSet) throws CoreException {
        
        super(association, builder, stringsSet, false);
    }

    /**
     * {@inheritDoc}
     */
    public void generateMethods(JavaCodeFragmentBuilder builder) throws CoreException {
        if (!association.isDerivedUnion() && !association.getAssociationType().isCompositionDetailToMaster()) {
            // generate setter
            generateNewChildMethodsIfApplicable(builder);
        }
    }

}
