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

package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype2.AggregationKind;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeAssociation;

/**
 * An association is a directed relationship from one type (the source) to another (the target).
 * The association is stored as part of the source type.
 *  
 * @author Jan Ortmann
 */
public interface IAssociation extends IIpsObjectPart {

    public static final int CARDINALITY_ONE = 1;
    public static final int CARDINALITY_MANY = Integer.MAX_VALUE;
    
    // String constants for the relation class' properties according
    // to the Java beans standard.
    public final static String PROPERTY_AGGREGATION_KIND = "aggregationKind"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_SINGULAR = "targetRoleSingular"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_PLURAL = "targetRolePlural"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_READ_ONLY_CONTAINER = "readOnlyContainer"; //$NON-NLS-1$
    public final static String PROPERTY_IMPLEMENTED_CONTAINER_RELATION = "implementedContainerRelation"; //$NON-NLS-1$
    
    /**
     * Returns the type this association belongs to. Never returns <code>null</code>.
     */
    public IType getType();
    
    /**
     * Returns the kind of aggregation. The method never returns <code>null</code>.
     */
    public AggregationKind getAggregationKind();
    
    /**
     * Sets the kind of aggregation.
     * 
     * @throws NullPointerException if newKind is <code>null</code>.
     */
    public void setAggregationKind(AggregationKind newKind);
    
    /**
     * Returns <code>true</code> if this is a derived association, otherwise <code>false</code>.
     */
    public boolean isDerived();
    
    /**
     * Returns the qualified name of the target product component type.
     */
    public String getTarget();
    
    /**
     * Returns the target product component type or <code>null</code> if either this relation hasn't got a target
     * or the target does not exists.
     * 
     * @param project The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public IProductCmptType findTarget(IIpsProject project) throws CoreException;
    
    /**
     * Sets the qualified name of the target product component type.
     */
    public void setTarget(String newTarget);
    
    /**
     * Returns the role of the target in this relation.
     */
    public String getTargetRoleSingular();
    
    /**
     * Returns the default for the target role singular.
     */
    public String getDefaultTargetRoleSingular();
    
    /**
     * Sets the role of the target in this relation. The role is specified in singular form, e.g. policy and not
     * policies. The distinction is more relevant in other languages than English, where you can't derive the 
     * plural from the singular form.
     */
    public void setTargetRoleSingular(String newRole);
    
    /**
     * Returns the role of the target in this relation. The role is specified in plural form.
     */
    public String getTargetRolePlural();
    
    /**
     * Returns the default for the target role plural.
     */
    public String getDefaultTargetRolePlural();

    /**
     * Sets the new role in plural form of the target in this relation.
     */
    public void setTargetRolePlural(String newRole);
    
    /**
     * Returns if the target role plural is required (or not) based on the relation's max cardinality
     * and the aretfact builderset's information if it needs the plural form for to 1 relations.
     */
    public boolean isTargetRolePluralRequired();
    
    /**
     * Returns <code>true</code> if this is an abstract, read-only container relation. 
     * otherwise false.
     */
    public boolean isReadOnlyContainer();
    
    /**
     * Sets the information if this is an abstract read-only container relation or not.
     */
    public void setReadOnlyContainer(boolean flag);
    
    /**
     * Returns the minmum number of target instances required in this relation.   
     */
    public int getMinCardinality();
    
    /**
     * Sets the minmum number of target instances required in this relation.   
     */
    public void setMinCardinality(int newValue);
    
    /**
     * Returns the maxmium number of target instances allowed in this relation.
     * If the number is not limited, CARDINALITY_MANY is returned. 
     */
    public int getMaxCardinality();
    
    /**
     * Returns true if this is a 1 (or 0) to many relation. This is the case if
     * the max cardinality is greater than 1.
     */
    public boolean is1ToMany();
    
    /**
     * Returns true if this is a 1 (or 0) to 1 relation. This is the case if
     * the max cardinality is 1.
     */
    public boolean is1To1();
    
    /**
     * Sets the maxmium number of target instances allowed in this relation.
     * An unlimited number is represented by CARDINALITY_MANY.
     */
    public void setMaxCardinality(int newValue);
    
    /**
     * Sets the container relation that is implemented by this relation. 
     */
    public void setImplementedContainerRelation(String containerRelation);
    
    /**
     * Returns the name of the container relation this one implements.
     * <p>
     * Example:
     * <br>
     * A <code>Policy</code> class has a 1-many relation to it's <code>PolicyPart</code>s (PolicyPartRelation).
     * Derived from <code>Policy</code> is a <code>MotorPolicy</code>. Derived from <code>PolicyPart</code>
     * is a <code>MotorCollisionPart</code>. There exists a 1-1 relation between
     * <code>MotorPolicy</code> and <code>MotorCollisionPart</code>.
     * To express that the a motor policy instance returns the collision part
     * when all it's parts (PolicyPartRelation) are requested, the policy part relation
     * has to be defined as container relation. This relation between the motor policy
     * and the motor collision part then implements the PolicyPartRelation.
     */
    public String getImplementedContainerRelation();     

    /**
     * Returns <code>true</code> if this relation is based on a container relation.
     */
    public boolean isContainerRelationImplementation();
    
    /**
     * Returns <code>true</code> if this relation implements the given container relation, 
     * <code>false</code> otherwise. Returns <code>false</code> if containerRelation is <code>null</code>.
     * This method does not check if the given container relation is *really* a container relation.
     * 
     * @param project The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the container relation this ones implements.
     */
    public boolean isContainerRelationImplementation(IProductCmptTypeAssociation containerRelation, IIpsProject project) throws CoreException;
    
    /**
     * Searches the container relation object and returns it, if it exists. Returns <code>null</code> if the container
     * relation does not exists.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IProductCmptTypeAssociation findImplementedContainerRelation(IIpsProject project) throws CoreException;
}
