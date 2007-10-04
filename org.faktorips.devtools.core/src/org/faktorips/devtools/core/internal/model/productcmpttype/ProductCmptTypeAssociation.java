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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IRelation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociation extends AtomicIpsObjectPart implements IProductCmptTypeAssociation {

    final static String TAG_NAME = "Association"; //$NON-NLS-1$

    private AggregationKind aggregationKind = AggregationKind.NONE;
    private String target = ""; //$NON-NLS-1$
    private String targetRoleSingular = ""; //$NON-NLS-1$
    private String targetRolePlural = ""; //$NON-NLS-1$
    private int minCardinality = 0;
    private int maxCardinality = Integer.MAX_VALUE; 
    private String subsettedDerivedUnion = ""; //$NON-NLS-1$
    private boolean derivedUnion = false;
    
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
    public String getName() {
        return targetRoleSingular;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDerived() {
        return isDerivedUnion();
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
    public boolean isDerivedUnion() {
        return derivedUnion;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDerivedUnion(boolean flag) {
        boolean oldValue = derivedUnion;
        this.derivedUnion = flag;
        valueChanged(oldValue, derivedUnion);
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptType findTarget(IIpsProject project) throws CoreException {
        if (StringUtils.isEmpty(target)) {
            return null;
        }
        return (IProductCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE_V2, target);
    }
    
    /** 
     * {@inheritDoc}
     */
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, newTarget);
    }

    /** 
     * {@inheritDoc}
     */
    public String getTargetRoleSingular() {
        return targetRoleSingular;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultTargetRoleSingular() {
        return StringUtils.capitalise(QNameUtil.getUnqualifiedName(target));
    }

    /** 
     * {@inheritDoc}
     */
    public void setTargetRoleSingular(String newRole) {
        String oldRole = targetRoleSingular;
        targetRoleSingular = newRole;
        valueChanged(oldRole, newRole);
    }
    

    /**
     * {@inheritDoc}
     */
    public String getTargetRolePlural() {
        return targetRolePlural;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultTargetRolePlural() {
        return targetRoleSingular;
    }

    /**
     * {@inheritDoc}
     */
    public void setTargetRolePlural(String newRole) {
        String oldRole = targetRolePlural;
        targetRolePlural = newRole;
        valueChanged(oldRole, newRole);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isTargetRolePluralRequired() {
        return is1ToMany() || getIpsProject().getIpsArtefactBuilderSet().isRoleNamePluralRequiredForTo1Relations();
    }

    /** 
     * {@inheritDoc}
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /** 
     * {@inheritDoc}
     */
    public void setMinCardinality(int newValue) {
        int oldValue = minCardinality;
        minCardinality = newValue;
        valueChanged(oldValue, newValue);
        
    }

    /** 
     * {@inheritDoc}
     */
    public int getMaxCardinality() {
        return maxCardinality;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean is1ToMany() {
        return maxCardinality > 1;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean is1To1() {
        return maxCardinality == 1;
    }

    /**
     * {@inheritDoc}
     */
    public void setMaxCardinality(int newValue) {
        int oldValue = maxCardinality;
        maxCardinality = newValue;
        valueChanged(oldValue, newValue);
    }

    /** 
     * {@inheritDoc}
     */
    public void setSubsettedDerivedUnion(String newRelation) {
        String oldValue = subsettedDerivedUnion;
        subsettedDerivedUnion = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getSubsettedDerivedUnion() {
        return subsettedDerivedUnion;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isSubsetOfADerivedUnion() {
        return StringUtils.isNotEmpty(subsettedDerivedUnion);
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAssociation findSubsettedDerivedUnion(IIpsProject project) throws CoreException {
        return getProductCmptType().findAssociation(subsettedDerivedUnion, project);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isSubsetOfDerivedUnion(IProductCmptTypeAssociation containerRelation, IIpsProject project) throws CoreException {
        if (containerRelation==null) {
            return false;
        }
        return containerRelation.equals(findSubsettedDerivedUnion(project));
    }
    
    /**
     * {@inheritDoc}
     */

    public org.faktorips.devtools.core.model.pctype.IRelation findMatchingPolicyCmptTypeRelation(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptType policyCmptType = getProductCmptType().findPolicyCmptType(true, ipsProject);
        if (policyCmptType==null) {
            return null;
        }
        IProductCmptType targetType = findTarget(ipsProject);
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
        IProductCmptTypeAssociation[] ass = getProductCmptType().getAssociations();
        for (int i = 0; i < ass.length; i++) {
            if (target.equals(ass[i].getTarget())) {
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
        return IpsPlugin.getDefault().getImage("Relation.gif"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        aggregationKind = AggregationKind.getKind(element.getAttribute(PROPERTY_AGGREGATION_KIND));
        if (aggregationKind==null) {
            aggregationKind = AggregationKind.NONE;
        }
        target = element.getAttribute(PROPERTY_TARGET);
        targetRoleSingular = element.getAttribute(PROPERTY_TARGET_ROLE_SINGULAR);
        targetRolePlural = element.getAttribute(PROPERTY_TARGET_ROLE_PLURAL);
        try {
            minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        } catch (NumberFormatException e) {
            minCardinality = 0;
        }
        String max = element.getAttribute(PROPERTY_MAX_CARDINALITY);
        if (max.equals("*")) { //$NON-NLS-1$
            maxCardinality = CARDINALITY_MANY;
        } else {
            try {
                maxCardinality = Integer.parseInt(max);
            } catch (NumberFormatException e) {
                maxCardinality = 0;
            }
        }
        derivedUnion = Boolean.valueOf(element.getAttribute(PROPERTY_DERIVED_UNION)).booleanValue();
        subsettedDerivedUnion = element.getAttribute(PROPERTY_SUBSETTED_DERIVED_UNION);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_AGGREGATION_KIND, aggregationKind.getId());
        newElement.setAttribute(PROPERTY_TARGET, target);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_SINGULAR, targetRoleSingular);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_PLURAL, targetRolePlural);
        newElement.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality); //$NON-NLS-1$
        
        if (maxCardinality == CARDINALITY_MANY) {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
        } else {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "" + maxCardinality); //$NON-NLS-1$
        }
        
        newElement.setAttribute(PROPERTY_DERIVED_UNION, "" + derivedUnion); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_SUBSETTED_DERIVED_UNION, subsettedDerivedUnion); 
    }
    
}
