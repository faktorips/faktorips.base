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

package org.faktorips.devtools.stdbuilder.type;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.util.LocalizedStringsSet;

/**
 * This class combines common implementation for {@link IAssociation}. As this class is introduced
 * late after {@link GenAssociation} and {@link GenProdAssociation} already exists, there are many
 * aspects that are not generalized yet.
 * 
 * @author dirmeier
 */
public abstract class GenAbstractAssociation extends GenTypePart {

    private Boolean lazyNeedSuperCallForDerivedUnion;

    public GenAbstractAssociation(GenType genType, IIpsObjectPartContainer ipsObjectPartContainer,
            LocalizedStringsSet stringsSet) {
        super(genType, ipsObjectPartContainer, stringsSet);
    }

    protected abstract IAssociation getAssociation();

    /**
     * This method check if a call of the method in superclass is needed. For Java5 we only generate
     * the override annotation if such a method exists.
     */
    protected boolean needSuperCallForDerivedUnion() throws CoreException {
        if (lazyNeedSuperCallForDerivedUnion != null) {
            return lazyNeedSuperCallForDerivedUnion;
        }

        IType type = getGenType().getType();
        if (type.equals(getAssociation().getType())) {
            // the derived union is defined in this generated class
            lazyNeedSuperCallForDerivedUnion = false;
        } else {
            IType supertype = type.findSupertype(getIpsProject());
            FindSubsetOfDerivedUnion findSubsetOfDerivedUnionVisitor = new FindSubsetOfDerivedUnion(getIpsProject(),
                    getAssociation());
            findSubsetOfDerivedUnionVisitor.start(supertype);
            lazyNeedSuperCallForDerivedUnion = findSubsetOfDerivedUnionVisitor.foundSubset;
        }
        return lazyNeedSuperCallForDerivedUnion;
    }

    private static class FindSubsetOfDerivedUnion extends TypeHierarchyVisitor<IType> {

        private final IAssociation derivedUnion;

        private boolean foundSubset = false;

        public FindSubsetOfDerivedUnion(IIpsProject ipsProject, IAssociation derivedUnion) {
            super(ipsProject);
            this.derivedUnion = derivedUnion;
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            List<IAssociation> associations = currentType.getAssociations();
            for (IAssociation aAssociation : associations) {
                if (aAssociation.isSubsetOfDerivedUnion(derivedUnion, ipsProject)) {
                    foundSubset = true;
                    return false;
                }
            }
            if (currentType.equals(derivedUnion.getType())) {
                return false;
            }
            return true;
        }

    }

}