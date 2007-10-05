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

package org.faktorips.devtools.core.internal.model.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.QNameUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAssociation.
 * 
 * @author Jan Ortmann
 */
public abstract class Association extends AtomicIpsObjectPart implements IAssociation {

    final static String TAG_NAME = "Association"; //$NON-NLS-1$

    private String target = ""; //$NON-NLS-1$
    private String targetRoleSingular = ""; //$NON-NLS-1$
    private String targetRolePlural = ""; //$NON-NLS-1$
    private int minCardinality = 0;
    private int maxCardinality = Integer.MAX_VALUE; 
    private String subsettedDerivedUnion = ""; //$NON-NLS-1$
    private boolean derivedUnion = false;
    
    public Association(IIpsObject parent, int id) {
        super(parent, id);
    }
    
    /**
     * {@inheritDoc}
     */
    public IType getType() {
        return (IType)getParent();
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
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, newTarget);
    }

    /**
     * {@inheritDoc}
     */
    public IType findTarget(IIpsProject ipsProject) throws CoreException {
        return (IType)ipsProject.findIpsObject(getIpsObject().getIpsObjectType(), target);
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
    public IAssociation findSubsettedDerivedUnion(IIpsProject project) throws CoreException {
        return getType().findAssociation(subsettedDerivedUnion, project);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isSubsetOfDerivedUnion(IAssociation derivedUnion, IIpsProject project) throws CoreException {
        if (derivedUnion==null) {
            return false;
        }
        return derivedUnion.equals(findSubsettedDerivedUnion(project));
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
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
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
