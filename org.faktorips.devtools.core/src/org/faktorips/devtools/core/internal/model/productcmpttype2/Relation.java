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

package org.faktorips.devtools.core.internal.model.productcmpttype2;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IRelation;
import org.faktorips.devtools.core.util.QNameUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IRelation.
 * 
 * @author Jan Ortmann
 */
public class Relation extends AtomicIpsObjectPart implements IRelation {

    final static String TAG_NAME = "Relation"; //$NON-NLS-1$

    private String target = ""; //$NON-NLS-1$
    private String targetRoleSingular = ""; //$NON-NLS-1$
    private String targetRolePlural = ""; //$NON-NLS-1$
    private int minCardinality = 0;
    private int maxCardinality = Integer.MAX_VALUE; 
    private String implementedContainerRelation = ""; //$NON-NLS-1$
    private boolean readOnlyContainer = false;
    
    public Relation(IIpsObject parent, int id) {
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
    public boolean isReadOnlyContainer() {
        return readOnlyContainer;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setReadOnlyContainer(boolean flag) {
        boolean oldValue = readOnlyContainer;
        this.readOnlyContainer = flag;
        valueChanged(oldValue, readOnlyContainer);
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
        return (IProductCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE2, target);
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
    public void setImplementedContainerRelation(String newRelation) {
        String oldValue = implementedContainerRelation;
        implementedContainerRelation = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getImplementedContainerRelation() {
        return implementedContainerRelation;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isContainerRelationImplementation() {
        return StringUtils.isNotEmpty(implementedContainerRelation);
    }
    
    /**
     * {@inheritDoc}
     */
    public IRelation findImplementedContainerRelation(IIpsProject project) throws CoreException {
        return getProductCmptType().findRelationInSupertypeHierarchy(implementedContainerRelation, true, project);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isContainerRelationImplementation(IRelation containerRelation, IIpsProject project) throws CoreException {
        if (containerRelation==null) {
            return false;
        }
        return containerRelation.equals(findImplementedContainerRelation(project));
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
        readOnlyContainer = Boolean.valueOf(element.getAttribute(PROPERTY_READ_ONLY_CONTAINER)).booleanValue();
        implementedContainerRelation = element.getAttribute(PROPERTY_IMPLEMENTED_CONTAINER_RELATION);
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
        
        newElement.setAttribute(PROPERTY_READ_ONLY_CONTAINER, "" + readOnlyContainer); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_IMPLEMENTED_CONTAINER_RELATION, implementedContainerRelation); 
    }
    
}
