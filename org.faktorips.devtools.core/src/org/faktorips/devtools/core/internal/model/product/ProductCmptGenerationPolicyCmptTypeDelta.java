/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class ProductCmptGenerationPolicyCmptTypeDelta implements
        IProductCmptGenerationPolicyCmptTypeDelta {
    
    private IProductCmptGeneration generation;
    private IPolicyCmptType pcType;
    private IAttribute[] attributesWithMissingConfigElements;
    private IConfigElement[] elementsWithMissingAttributes;
    private IConfigElement[] elementsWithTypeMismatch;
    private IConfigElement[] elementsWithValueSetMismatch; 
    private IProductCmptRelation[] relationsWithMissingPcTypeRelations;
    
    public ProductCmptGenerationPolicyCmptTypeDelta(
            IProductCmptGeneration generation, 
            IPolicyCmptType type) throws CoreException {
        ArgumentCheck.notNull(generation);
        ArgumentCheck.notNull(type);
        this.generation = generation;
        this.pcType = type;
        ITypeHierarchy hierarchy = pcType.getSupertypeHierarchy();
        computeAttributesWithMissingConfigElements(hierarchy);
        computeElementsWithMissingAttributes(hierarchy);
        computeElementsWithTypeOrValueSetMismatch(hierarchy);
        computeRelationsWithMissingPcTypeRelations(hierarchy);
    }
    
    private void computeAttributesWithMissingConfigElements(ITypeHierarchy hierarchy) throws CoreException {
        List missing = new ArrayList();
        IAttribute[] attributes = hierarchy.getAllAttributesRespectingOverride(pcType);
        for (int i=0; i<attributes.length; i++) {
            if (attributes[i].isProductRelevant()) {
                if (generation.getConfigElement(attributes[i].getName())==null) {
                    missing.add(attributes[i]);
                }
            }
        }
        attributesWithMissingConfigElements = (IAttribute[])missing.toArray(new IAttribute[missing.size()]);
    }

    private void computeElementsWithMissingAttributes(ITypeHierarchy hierarchy) {
        List missing = new ArrayList();
        IConfigElement[] elements = generation.getConfigElements();
        for (int i=0; i<elements.length; i++) {
            IAttribute attribute = hierarchy.findAttribute(pcType, elements[i].getPcTypeAttribute());
            if (attribute==null || !attribute.isProductRelevant()) {
                missing.add(elements[i]);
            }
        }
        elementsWithMissingAttributes = (IConfigElement[])missing.toArray(new IConfigElement[missing.size()]);
    }
    
    private void computeElementsWithTypeOrValueSetMismatch(ITypeHierarchy hierarchy) {
        List typeMismatchs = new ArrayList();
        List valueSetMismatchs = new ArrayList();
        IConfigElement[] elements = generation.getConfigElements();
        for (int i=0; i<elements.length; i++) {
            IAttribute attribute =hierarchy.findAttribute(pcType, elements[i].getPcTypeAttribute()); 
            if (attribute!=null && attribute.isProductRelevant()) {
                if (attribute.getConfigElementType()!=elements[i].getType()) {
                    typeMismatchs.add(elements[i]);
                }

                /*
				 * if the config element has an all values valueset and the
				 * valueset of the underlying attribute is not an all values
				 * valuese, the valueset has to be changed to a copy of the
				 * underlying attribute valueset. This is because all value
				 * valuesets only apply on datatypes, not on other valuesets.
				 */
                if (!(attribute.getValueSet().getValueSetType() == ValueSetType.ALL_VALUES) && (elements[i].getValueSet().getValueSetType() == ValueSetType.ALL_VALUES)) {
                	valueSetMismatchs.add(elements[i]);
                }
                
                if (!(attribute.getValueSet().getValueSetType() == ValueSetType.ALL_VALUES) && !(elements[i].getValueSet().getValueSetType() == ValueSetType.ALL_VALUES)) {
                    if (!attribute.getValueSet().getValueSetType().equals(elements[i].getValueSet().getValueSetType())) {
                        valueSetMismatchs.add(elements[i]);
                    }
                    else if (!attribute.getValueSet().containsValueSet(elements[i].getValueSet())) {
                        valueSetMismatchs.add(elements[i]);
                    }
                }
            }
        }
        elementsWithTypeMismatch = (IConfigElement[])typeMismatchs.toArray(new IConfigElement[typeMismatchs.size()]);
        elementsWithValueSetMismatch = (IConfigElement[])valueSetMismatchs.toArray(new IConfigElement[valueSetMismatchs.size()]);
    }
    
    private void computeRelationsWithMissingPcTypeRelations(ITypeHierarchy hierarchy) throws CoreException {
        List result = new ArrayList();
        IProductCmptRelation[] relations = generation.getRelations();
        for (int i=0; i<relations.length; i++) {
            if (hierarchy.findRelationOnProductSide(pcType, relations[i].getProductCmptTypeRelation())==null) {
                result.add(relations[i]);
            }
        }
        relationsWithMissingPcTypeRelations = (IProductCmptRelation[])result.toArray(new IProductCmptRelation[result.size()]);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta#getProductCmpt()
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return generation;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta#getPolicyCmptType()
     */
    public IPolicyCmptType getPolicyCmptType() {
        return pcType;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta#isEmpty()
     */
    public boolean isEmpty() {
        return attributesWithMissingConfigElements.length==0
        	&& elementsWithMissingAttributes.length == 0
        	&& elementsWithTypeMismatch.length == 0
        	&& elementsWithValueSetMismatch.length == 0
        	&& relationsWithMissingPcTypeRelations.length == 0;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta#getAttributesWithMissingConfigElements()
     */
    public IAttribute[] getAttributesWithMissingConfigElements() {
        return attributesWithMissingConfigElements;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta#getTypeMismatchElements()
     */
    public IConfigElement[] getTypeMismatchElements() {
        return elementsWithTypeMismatch;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta#getConfigElementsWithMissingAttributes()
     */
    public IConfigElement[] getConfigElementsWithMissingAttributes() {
        return elementsWithMissingAttributes;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta#getRelationsWithMissingPcTypeRelations()
     */
    public IProductCmptRelation[] getRelationsWithMissingPcTypeRelations() {
        return relationsWithMissingPcTypeRelations;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta#getElementsWithValueSetMismatch()
     */
    public IConfigElement[] getElementsWithValueSetMismatch() {
        return elementsWithValueSetMismatch;
    }

}
