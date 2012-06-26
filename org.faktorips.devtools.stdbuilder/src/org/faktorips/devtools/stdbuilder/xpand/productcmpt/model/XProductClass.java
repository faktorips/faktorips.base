/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;

public abstract class XProductClass extends XClass {

    public XProductClass(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);
    }

    /**
     * Getting the list of associations defined in this type. With the parameter
     * changableAssociations you could specify whether you want the associations that are changeable
     * over time or not changeable (sometimes called static) associations.
     * <p>
     * This method does not return any derived union association.
     * <p>
     * This method needs to be final because it may be called in constructor
     * 
     * @see #getProductDerivedUnionAssociations(boolean)
     * 
     * @param changableAssociations true if you want only associations changeable over time, false
     *            to get only not changeable over time associations
     * @return The list of associations without derived unions
     */
    protected final Set<IProductCmptTypeAssociation> getProductAssociations(boolean changableAssociations) {
        return getProductAssociations(false, changableAssociations);
    }

    /**
     * This method returns the derived union associations defined in this type. With the parameter
     * changableAssociations you could specify whether you want the associations that are changeable
     * over time or not changeable (sometimes called static) associations.
     * <p>
     * If you want to have not derived union associations, @see #getProductAssociations(boolean)
     * <p>
     * This method needs to be final because it may be called in constructor
     * 
     * @param changableAssociations true if you want only associations changeable over time, false
     *            to get only not changeable over time associations
     * @return The list of derived union associations
     */
    protected final Set<IProductCmptTypeAssociation> getProductDerivedUnionAssociations(boolean changableAssociations) {
        Set<IProductCmptTypeAssociation> resultingAssociations = new LinkedHashSet<IProductCmptTypeAssociation>();
        Set<IProductCmptTypeAssociation> notDerivedUnionAssociations = getProductAssociations(changableAssociations);
        for (IProductCmptTypeAssociation association : notDerivedUnionAssociations) {
            try {
                if (association.isSubsetOfADerivedUnion()) {
                    IProductCmptTypeAssociation subsettedDerivedUnion = (IProductCmptTypeAssociation)association
                            .findSubsettedDerivedUnion(getIpsProject());
                    resultingAssociations.add(subsettedDerivedUnion);
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return resultingAssociations;
    }

    private Set<IProductCmptTypeAssociation> getProductAssociations(boolean derivedUnion, boolean changableAssociations) {
        Set<IProductCmptTypeAssociation> resultingAssociations = new LinkedHashSet<IProductCmptTypeAssociation>();
        List<IProductCmptTypeAssociation> allAssociations = getProductCmptType().getProductCmptTypeAssociations();
        for (IProductCmptTypeAssociation assoc : allAssociations) {
            // TODO FIPS-989
            if (assoc.isDerivedUnion() == derivedUnion && changableAssociations) {
                resultingAssociations.add(assoc);
            }
        }
        return resultingAssociations;
    }

    /**
     * Returns the list of attributes. With the parameter you could specify whether you want the
     * attributes that change over time or attributes not changing over time.
     * <p>
     * This method needs to be final because it may be called in constructor
     * 
     * @param changableAttributes True to get attributes that change over time, false to get all
     *            other attributes
     * @return the list of attributes defined in this type
     */
    protected final Set<IProductCmptTypeAttribute> getProductAttributes(boolean changableAttributes) {
        Set<IProductCmptTypeAttribute> resultingAttributes = new LinkedHashSet<IProductCmptTypeAttribute>();
        List<IProductCmptTypeAttribute> allAttributes = getProductCmptType().getProductCmptTypeAttributes();
        for (IProductCmptTypeAttribute attr : allAttributes) {
            if (changableAttributes == attr.isChangingOverTime()) {
                resultingAttributes.add(attr);
            }
        }
        return resultingAttributes;
    }

    private IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    @Override
    public IProductCmptType getIpsObjectPartContainer() {
        return (IProductCmptType)super.getIpsObjectPartContainer();
    }

    @Override
    public abstract Set<XProductAttribute> getAttributes();

    @Override
    public abstract Set<XProductAssociation> getAssociations();

}