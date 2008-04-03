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
import org.faktorips.devtools.stdbuilder.policycmpttype.BasePolicyCmptTypeBuilder;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class GenAssociationTo1 extends GenAssociation {

    public GenAssociationTo1(IPolicyCmptTypeAssociation association, BasePolicyCmptTypeBuilder builder,
            LocalizedStringsSet stringsSet, boolean generateImplementation) throws CoreException {
        super(association, builder, stringsSet, generateImplementation);
    }

    /**
     * {@inheritDoc}
     */
    protected String computeFieldName() {
        return getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular());
    }

    /**
     * {@inheritDoc}
     */
    public String getMethodNameAddOrSetObject() {
        return getMethodNameSetObject();
    }

    /**
     * Returns the name of the method setting the referenced object.
     * e.g. setCoverage(ICoverage newObject)
     */
    public String getMethodNameSetObject() {
        return getLocalizedText("METHOD_SET_OBJECT_NAME", association.getTargetRoleSingular());
    }
}
