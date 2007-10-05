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
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IRelation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociation extends Association implements IProductCmptTypeAssociation {

    final static String TAG_NAME = "Association"; //$NON-NLS-1$

    private AggregationKind aggregationKind = AggregationKind.NONE;
    
    public ProductCmptTypeAssociation(IIpsObject parent, int id) {
        super(parent, id);
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
    public AggregationKind getAggregationKind() {
        return aggregationKind;
    }

    /**
     * {@inheritDoc}
     */
    public void setAggregationKind(AggregationKind newKind) {
        ArgumentCheck.notNull(newKind);
        AggregationKind oldKind = aggregationKind;
        aggregationKind = newKind;
        valueChanged(oldKind, newKind);
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
    public IRelation findMatchingPolicyCmptTypeRelation(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptType policyCmptType = getProductCmptType().findPolicyCmptType(true, ipsProject);
        if (policyCmptType==null) {
            return null;
        }
        IProductCmptType targetType = findTargetProductCmptType(ipsProject);
        if (targetType==null) {
            return null;
        }
        IPolicyCmptType targetPolicyCmptType = targetType.findPolicyCmptType(true, ipsProject);
        if (targetPolicyCmptType==null) {
            return null;
        }
        IRelation[] policyRelations = getRelationsFor(policyCmptType, targetPolicyCmptType); 
        if (policyRelations.length==0) {
            return null;
        }
        return policyRelations[getAssociationIndex()];
    }
    
    private IRelation[] getRelationsFor(IPolicyCmptType from, IPolicyCmptType target) {
        List result = new ArrayList();
        String targetQName = target.getQualifiedName();
        org.faktorips.devtools.core.model.pctype.IRelation[] policyRelations = from.getRelations();
        for (int i=0; i<policyRelations.length; i++) {
            if (targetQName.equals(policyRelations[i].getTarget())) {
                result.add(policyRelations[i]);
            }
        }
        return (IRelation[])result.toArray(new IRelation[result.size()]);
    }
    
    private int getAssociationIndex() {
        List allAssociationsForTheTargetType = new ArrayList();
        IAssociation[] ass = getType().getAssociations();
        for (int i = 0; i < ass.length; i++) {
            if (getTarget().equals(ass[i].getTarget())) {
                allAssociationsForTheTargetType.add(ass[i]);
            }
        }
        int index = 0;
        for (Iterator it=allAssociationsForTheTargetType.iterator(); it.hasNext(); index++) {
            if (it.next()==this) {
                return index;
            }
        }
        throw new RuntimeException("Can't get index of association " + this);
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
        return aggregationKind.getImage();
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        aggregationKind = AggregationKind.getKind(element.getAttribute(IAssociation.PROPERTY_AGGREGATION_KIND));
        if (aggregationKind==null) {
            aggregationKind = AggregationKind.NONE;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(IAssociation.PROPERTY_AGGREGATION_KIND, aggregationKind.getId());
    }
    
}
