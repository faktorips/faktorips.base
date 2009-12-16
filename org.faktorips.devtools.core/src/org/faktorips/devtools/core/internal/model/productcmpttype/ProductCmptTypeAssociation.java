/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IRelation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociation extends Association implements IProductCmptTypeAssociation {

    final static String TAG_NAME = "Association"; //$NON-NLS-1$

    public ProductCmptTypeAssociation(IIpsObject parent, int id) {
        super(parent, id);
        type = AssociationType.AGGREGATION;
    }

    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    public boolean isQualified() {
        return false;
    }

    public IProductCmptType findTargetProductCmptType(IIpsProject ipsProject) throws CoreException {
        return (IProductCmptType)ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, getTarget());
    }

    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException {
        return findMatchingPolicyCmptTypeAssociation(ipsProject) != null;
    }

    public IPolicyCmptTypeAssociation findMatchingPolicyCmptTypeAssociation(IIpsProject ipsProject)
            throws CoreException {
        IPolicyCmptType policyCmptType = getProductCmptType().findPolicyCmptType(ipsProject);
        if (policyCmptType == null) {
            return null;
        }
        IProductCmptType targetType = findTargetProductCmptType(ipsProject);
        if (targetType == null) {
            return null;
        }
        IPolicyCmptType targetPolicyCmptType = targetType.findPolicyCmptType(ipsProject);
        if (targetPolicyCmptType == null) {
            return null;
        }
        IPolicyCmptTypeAssociation[] policyAssoc = getAssociationsFor(policyCmptType, targetPolicyCmptType);
        if (policyAssoc.length == 0) {
            return null;
        }
        // Assume that both PolicyCmptTypeAssociations and ProductCmptTypeAssociations are listed in
        // the same order.
        int index = getAssociationIndex();
        if (index >= policyAssoc.length) {
            return null;
        }
        return policyAssoc[index];
    }

    /**
     * Returns all {@code IPolicyCmptTypeAssociation}s which have the specified source and target
     * policy component type, but ignoring associations of type COMPOSITION_DETAIL_TO_MASTER.
     */
    private IPolicyCmptTypeAssociation[] getAssociationsFor(IPolicyCmptType from, IPolicyCmptType target) {
        List result = new ArrayList();
        String targetQName = target.getQualifiedName();
        IPolicyCmptTypeAssociation[] policyAssociations = from.getPolicyCmptTypeAssociations();
        for (int i = 0; i < policyAssociations.length; i++) {
            if (targetQName.equals(policyAssociations[i].getTarget())) {
                if (!AssociationType.COMPOSITION_DETAIL_TO_MASTER.equals(policyAssociations[i].getAssociationType())) {
                    result.add(policyAssociations[i]);
                }
            }
        }
        return (IPolicyCmptTypeAssociation[])result.toArray(new IPolicyCmptTypeAssociation[result.size()]);
    }

    private int getAssociationIndex() {
        List allAssociationsForTheTargetType = new ArrayList();
        IAssociation[] assoc = getType().getAssociations();
        for (int i = 0; i < assoc.length; i++) {
            if (getTarget().equals(assoc[i].getTarget())) {
                allAssociationsForTheTargetType.add(assoc[i]);
            }
        }
        int index = 0;
        for (Iterator it = allAssociationsForTheTargetType.iterator(); it.hasNext(); index++) {
            if (it.next() == this) {
                return index;
            }
        }
        throw new RuntimeException("Can't get index of association " + this); //$NON-NLS-1$
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    public Image getImage() {
        return IpsPlugin.getDefault().getImage(getAssociationType().getImageName());
    }

}
