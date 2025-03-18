/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 *
 * This LinkCandidateFilter filters, whether an {@link IIpsSrcFile} can be linked to the
 * {@link IProductCmptGeneration} represented by an {@link IProductCmptStructureReference}.
 * <p>
 * It is recommended, that the {@link #filter(IIpsSrcFile)} is called immediately after
 * instantiation.
 *
 * @author dicker
 */
public class LinkCandidateFilter {

    private boolean canAnyLinkBeAdded;
    private List<IProductCmptTypeAssociation> associations;

    private IProductCmptGeneration generation;

    /**
     * Constructs a new filter for the given {@link IProductCmptStructureReference}.
     * <p>
     * This filter always returns false, if the given <code>workingModeBrowse</code> is set to
     * <code>true</code>. workingModeBrowse could be read by calling {@link IpsPlugin}
     * .getDefault().getIpsPreferences() .isWorkingModeBrowse()}.
     */
    public LinkCandidateFilter(IProductCmptStructureReference structureReference, boolean workingModeBrowse) {
        Assert.isNotNull(structureReference);
        IProductCmpt productCmpt = getProductCmpt(structureReference);

        if (productCmpt == null) {
            initializeNotEditable();
        } else {
            GregorianCalendar validAt = structureReference.getStructure().getValidAt();
            generation = productCmpt.getGenerationEffectiveOn(validAt);

            initialize(workingModeBrowse, structureReference);
        }
    }

    private void initialize(boolean workingModeBrowse, IProductCmptStructureReference structureReference) {
        if (generation == null) {
            initializeNotEditable();
        } else {
            initAssociations(structureReference);
            initCanAnyLinkBeAdded(workingModeBrowse);
        }
    }

    private void initializeNotEditable() {
        associations = Collections.emptyList();
        generation = null;
        canAnyLinkBeAdded = false;
    }

    private void initCanAnyLinkBeAdded(boolean workingModeBrowse) {
        canAnyLinkBeAdded = !(associations.isEmpty() || workingModeBrowse || generation.getIpsSrcFile().isReadOnly());
    }

    private List<IProductCmptTypeAssociation> getUncheckedAssociations(
            IProductCmptStructureReference structureReference) {
        return switch (structureReference) {
            case IProductCmptReference productCmptReference -> getUncheckedAssociations(productCmptReference);
            case IProductCmptTypeAssociationReference associationReference -> Arrays
                    .asList(associationReference.getAssociation());
            default -> Collections.emptyList();
        };
    }

    private List<IProductCmptTypeAssociation> getUncheckedAssociations(IProductCmptReference productCmptReference) {
        List<IProductCmptTypeAssociation> uncheckedAssociations;
        IProductCmptTypeAssociationReference[] childProductCmptTypeAssociationReferences = productCmptReference
                .getStructure().getChildProductCmptTypeAssociationReferences(productCmptReference);
        uncheckedAssociations = new ArrayList<>();
        for (IProductCmptTypeAssociationReference associationReference : childProductCmptTypeAssociationReferences) {
            uncheckedAssociations.add(associationReference.getAssociation());
        }
        return uncheckedAssociations;
    }

    private IProductCmpt getProductCmpt(IProductCmptStructureReference structureReference) {
        return switch (structureReference) {
            case IProductCmptReference productCmptReference -> productCmptReference.getProductCmpt();
            case IProductCmptTypeAssociationReference associationReference -> getProductCmpt(
                    associationReference.getParent());
            default -> null;
        };
    }

    private void initAssociations(IProductCmptStructureReference structureReference) {
        List<IProductCmptTypeAssociation> checkedAssociations = new ArrayList<>();

        List<IProductCmptTypeAssociation> uncheckedAssociations = getUncheckedAssociations(structureReference);

        for (IProductCmptTypeAssociation association : uncheckedAssociations) {
            String name = association.getName();
            IProductCmptLink[] links = generation.getLinks(name);
            if (links.length < association.getMaxCardinality()) {
                checkedAssociations.add(association);
            }
        }

        associations = checkedAssociations;
    }

    public boolean filter(IIpsSrcFile srcFile) {
        if (!canAnyLinkBeAdded || !srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)
                || isOutsideReferencedProjects(srcFile) || isWrongType(srcFile)) {
            return false;
        }
        if (isAlreadyLinked(srcFile)) {
            return false;
        }
        return true;
    }

    private boolean isAlreadyLinked(IIpsSrcFile srcFile) {
        String qualifiedName = srcFile.getQualifiedNameType().getName();
        List<IProductCmptLink> linksAsList = generation.getLinksAsList();
        for (IProductCmptLink link : linksAsList) {
            if (link.getTarget().equals(qualifiedName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWrongType(IIpsSrcFile srcFile) {
        String typeName = srcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);

        IProductCmptType productCmptType = getIpsProject().findProductCmptType(typeName);

        for (IProductCmptTypeAssociation association : associations) {

            IProductCmptType targetType = association.findTargetProductCmptType(getIpsProject());
            boolean subtypeOrSameType = productCmptType.isSubtypeOrSameType(targetType, getIpsProject());

            if (subtypeOrSameType) {
                return false;
            }
        }

        return true;
    }

    private IIpsProject getIpsProject() {
        return generation.getIpsProject();
    }

    private boolean isOutsideReferencedProjects(IIpsSrcFile srcFile) {
        IIpsProject ipsProject = getIpsProject();
        IIpsProject ipsProject2 = srcFile.getIpsProject();

        if (ipsProject.equals(ipsProject2)) {
            return false;
        }
        return !ipsProject.isReferencing(ipsProject2);
    }
}
