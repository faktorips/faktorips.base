/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isQualified() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType findTargetProductCmptType(IIpsProject ipsProject) throws CoreException {
        return (IProductCmptType)ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE_V2, getTarget());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException {
        return findMatchingPolicyCmptTypeAssociation(ipsProject)!=null;
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAssociation findMatchingPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptType policyCmptType = getProductCmptType().findPolicyCmptType(ipsProject);
        if (policyCmptType==null) {
            return null;
        }
        IProductCmptType targetType = findTargetProductCmptType(ipsProject);
        if (targetType==null) {
            return null;
        }
        IPolicyCmptType targetPolicyCmptType = targetType.findPolicyCmptType(ipsProject);
        if (targetPolicyCmptType==null) {
            return null;
        }
        IPolicyCmptTypeAssociation[] policyAss = getRelationsFor(policyCmptType, targetPolicyCmptType); 
        if (policyAss.length==0) {
            return null;
        }
        // Assume that both PolicyCmptTypeAssociations and ProductCmptTypeAssociations are listed in the same order.
        int index = getAssociationIndexPerTargetType();
        if (index>=policyAss.length) {
            return null;
        }
        return policyAss[index];
    }
    
    private IPolicyCmptTypeAssociation[] getRelationsFor(IPolicyCmptType from, IPolicyCmptType target) {
        List result = new ArrayList();
        String targetQName = target.getQualifiedName();
        IPolicyCmptTypeAssociation[] policyRelations = from.getPolicyCmptTypeAssociations();
        for (int i=0; i<policyRelations.length; i++) {
            if (targetQName.equals(policyRelations[i].getTarget())) {
                result.add(policyRelations[i]);
            }
        }
        return (IPolicyCmptTypeAssociation[])result.toArray(new IPolicyCmptTypeAssociation[result.size()]);
    }
    
    /**
     * Returns the index of the {@link IPolicyCmptTypeAssociation} within the list of associations
     * sharing the same target type.
     * 
     * <p>
     * Note that inverse compositions are ignored when compiling the list of associations sharing
     * the same target type.
     */
    private int getAssociationIndexPerTargetType() {
        List allAssociationsForTheTargetType = new ArrayList();
        IAssociation[] assoc = getType().getAssociations();
        for (int i = 0; i < assoc.length; i++) {
            if (getTarget().equals(assoc[i].getTarget())
                    && !AssociationType.COMPOSITION_DETAIL_TO_MASTER.equals(assoc[i].getAssociationType())) {
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

    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage(getAssociationType().getImageName());
    }

    
}
