/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung -
 * Version 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist
 * und auch unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *
 *  * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation -
 * http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITableStructureUsage;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

/**
 * 
 */
public class ProductCmptGeneration extends IpsObjectGeneration implements IProductCmptGeneration {

    private List configElements = new ArrayList(0);

    private ArrayList relations = new ArrayList(0);

    private List tableContentUsages = new ArrayList(0);

    public ProductCmptGeneration(ITimedIpsObject ipsObject, int id) {
        super(ipsObject, id);
    }

    public ProductCmptGeneration() {
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        int numOfChildren = getNumOfConfigElements() + getNumOfRelations() + getTableContentUsages().length;
        IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
        List childrenList = new ArrayList(numOfChildren);
        childrenList.addAll(configElements);
        childrenList.addAll(relations);
        childrenList.addAll(tableContentUsages);
        childrenList.toArray(childrenArray);
        return childrenArray;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptGenerationPolicyCmptTypeDelta computeDeltaToPolicyCmptType() throws CoreException {
        IPolicyCmptType pcType = getProductCmpt().findPolicyCmptType();
        if (pcType != null) {
            return new ProductCmptGenerationPolicyCmptTypeDelta(this, pcType);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void fixDifferences(IProductCmptGenerationPolicyCmptTypeDelta delta) throws CoreException {
        if (delta == null) {
            return;
        }
        IAttribute[] attributes = delta.getAttributesWithMissingConfigElements();
        for (int i = 0; i < attributes.length; i++) {
            IConfigElement element = newConfigElement();
            element.setPcTypeAttribute(attributes[i].getName());
            element.setType(attributes[i].getConfigElementType());
            element.setValue(attributes[i].getDefaultValue());
            if (element.getType() != ConfigElementType.PRODUCT_ATTRIBUTE) {
                element.setValueSetCopy(attributes[i].getValueSet());
            }
        }
        IConfigElement[] elements = delta.getConfigElementsWithMissingAttributes();
        for (int i = 0; i < elements.length; i++) {
            elements[i].delete();
        }
        elements = delta.getTypeMismatchElements();
        for (int i = 0; i < elements.length; i++) {
            IAttribute a = elements[i].findPcTypeAttribute();
            if (elements[i].getType() == ConfigElementType.FORMULA) {
                elements[i].setValue(""); //$NON-NLS-1$
                // if the config element is no longer a formula delete all existing formula test
                // cases
                IFormulaTestCase[] formulaTest = elements[i].getFormulaTestCases();
                for (int j = 0; j < formulaTest.length; j++) {
                    formulaTest[j].delete();
                }
            }
            elements[i].setType(a.getConfigElementType());
        }
        elements = delta.getElementsWithValueSetMismatch();
        for (int i = 0; i < elements.length; i++) {
            IAttribute a = elements[i].findPcTypeAttribute();
            elements[i].setValueSetCopy(a.getValueSet());
        }

        IProductCmptRelation[] relations = delta.getRelationsWithMissingPcTypeRelations();
        for (int i = 0; i < relations.length; i++) {
            relations[i].delete();
        }

        ITableStructureUsage[] tsus = delta.getTableStructureUsagesWithMissingContentUsages();
        for (int i = 0; i < tsus.length; i++) {
            ITableContentUsage tcu = newTableContentUsage();
            tcu.setStructureUsage(tsus[i].getRoleName());
        }

        ITableContentUsage[] tcus = delta.getTableContentUsagesWithMissingStructureUsages();
        for (int i = 0; i < tcus.length; i++) {
            tcus[i].delete();
        }
    }

    /**
     * {@inheritDoc}
     */
    public IConfigElement[] getConfigElements() {
        return (IConfigElement[])configElements.toArray(new IConfigElement[configElements.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IConfigElement getConfigElement(String attributeName) {
        for (Iterator it = configElements.iterator(); it.hasNext();) {
            IConfigElement each = (IConfigElement)it.next();
            if (each.getPcTypeAttribute().equals(attributeName)) {
                return each;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IConfigElement[] getConfigElements(ConfigElementType type) {
        List result = new ArrayList(configElements.size());
        for (Iterator it = configElements.iterator(); it.hasNext();) {
            IConfigElement configEl = (IConfigElement)it.next();
            if (configEl.getType().equals(type)) {
                result.add(configEl);
            }
        }
        return (IConfigElement[])result.toArray(new IConfigElement[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfConfigElements() {
        return configElements.size();
    }

    /**
     * {@inheritDoc}
     */
    public IConfigElement newConfigElement() {
        IConfigElement newElement = newConfigElementInternal(getNextPartId());
        objectHasChanged();
        return newElement;
    }

    /*
     * Creates a new attribute without updating the src file.
     */
    private ConfigElement newConfigElementInternal(int id) {
        ConfigElement e = new ConfigElement(this, id);
        configElements.add(e);
        return e;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptRelation[] getRelations() {
        return (IProductCmptRelation[])relations.toArray(new ProductCmptRelation[relations.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptRelation[] getRelations(String typeRelation) {
        List result = new ArrayList();
        for (Iterator it = relations.iterator(); it.hasNext();) {
            IProductCmptRelation relation = (IProductCmptRelation)it.next();
            if (relation.getProductCmptTypeRelation().equals(typeRelation)) {
                result.add(relation);
            }
        }
        return (IProductCmptRelation[])result.toArray(new ProductCmptRelation[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfRelations() {
        return relations.size();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#newRelation(java.lang.String)
     */
    public IProductCmptRelation newRelation(String pcTypeRelation) {
        ProductCmptRelation newRelation = newRelationInternal(getNextPartId());
        newRelation.setProductCmptTypeRelation(pcTypeRelation);
        objectHasChanged();
        return newRelation;
    }

    public IProductCmptRelation newRelation(String pcTypeRelation, IProductCmptRelation insertBefore) {
        ProductCmptRelation newRelation = newRelationInternal(getNextPartId(), insertBefore);
        newRelation.setProductCmptTypeRelation(pcTypeRelation);
        objectHasChanged();
        return newRelation;
    }

    public IProductCmptRelation newRelation() {
        return newRelationInternal(getNextPartId());
    }

    /**
     * {@inheritDoc}
     */
    public boolean canCreateValidRelation(IProductCmpt target, String relation) throws CoreException {
        if (relation == null || target == null) {
            return false;
        }
        IProductCmptType type = getProductCmpt().findProductCmptType();
        if (type == null) {
            return false;
        }
        IProductCmptTypeRelation relationType = type.findRelationInHierarchy(relation);
        if (relationType == null) {
            return false;
        }
        // it is not valid to create more than one relation with the same type and target.
        if (!isFirstRelationOfThisType(relationType, target)) {
            return false;
        }
        return this.getRelations(relation).length < relationType.getMaxCardinality()
                && ProductCmptRelation.willBeValid(target, relationType);
    }

    private boolean isFirstRelationOfThisType(IProductCmptTypeRelation relation, IProductCmpt target)
            throws CoreException {
        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            IProductCmptRelation rel = (IProductCmptRelation)iter.next();
            if (rel.findProductCmptTypeRelation().equals(relation) && rel.getTarget().equals(target.getQualifiedName())) {
                return false;
            }
        }
        return true;
    }

    private ProductCmptRelation newRelationInternal(int id, IProductCmptRelation insertBefore) {
        ProductCmptRelation newRelation = new ProductCmptRelation(this, id);
        if (insertBefore == null) {
            relations.add(newRelation);
        }
        else {
            int index = relations.indexOf(insertBefore);
            if (index == -1) {
                relations.add(newRelation);
            }
            else {
                relations.add(index, newRelation);
            }
        }
        return newRelation;
    }

    private ProductCmptRelation newRelationInternal(int id) {
        return newRelationInternal(id, null);
    }

    /**
     * {@inheritDoc}
     */
    public void moveRelation(IProductCmptRelation toMove, IProductCmptRelation moveBefore) {
        relations.remove(toMove);
        int index = relations.indexOf(moveBefore);
        if (index == -1) {
            relations.add(toMove);
        }
        else {
            relations.add(index, toMove);
        }
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public ITableContentUsage newTableContentUsage() {
        ITableContentUsage retValue = newTableContentUsageInternal(getNextPartId());
        objectHasChanged();
        return retValue;
    }

    public ITableContentUsage[] getTableContentUsages() {
        return (ITableContentUsage[])tableContentUsages.toArray(new ITableContentUsage[tableContentUsages.size()]);
    }

    private ITableContentUsage newTableContentUsageInternal(int id) {
        ITableContentUsage retValue = new TableContentUsage(this, id);
        tableContentUsages.add(retValue);
        return retValue;
    }

    /*
     * Returns true if the generation contains a formula config element, otherwise false.
     */
    boolean containsFormula() {
        for (Iterator it = configElements.iterator(); it.hasNext();) {
            IConfigElement element = (IConfigElement)it.next();
            if (element.getType().equals(ConfigElementType.FORMULA)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(ConfigElement.TAG_NAME)) {
            return newConfigElementInternal(id);
        }
        else if (xmlTagName.equals(ProductCmptRelation.TAG_NAME)) {
            return newRelationInternal(id);
        }
        else if (xmlTagName.equals(ITableContentUsage.TAG_NAME)) {
            return newTableContentUsageInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IConfigElement) {
            configElements.add(part);
            return;
        }
        else if (part instanceof IProductCmptRelation) {
            relations.add(part);
            return;
        }
        else if (part instanceof ITableContentUsage) {
            tableContentUsages.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IConfigElement) {
            configElements.remove(part);
            return;
        }
        else if (part instanceof IProductCmptRelation) {
            relations.remove(part);
            return;
        }
        else if (part instanceof ITableContentUsage) {
            tableContentUsages.remove(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        configElements.clear();
        relations.clear();
        tableContentUsages.clear();
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        IProductCmptType type = getProductCmpt().findProductCmptType();
        // no type information available, so no further validation possible
        if (type == null) {
            list.add(new Message(MSGCODE_NO_TEMPLATE, Messages.ProductCmptGeneration_msgTemplateNotFound,
                    Message.ERROR, this));
            return;
        }

        IProductCmptGenerationPolicyCmptTypeDelta delta = computeDeltaToPolicyCmptType();
        IAttribute[] attributesWithMissingConfigElements = delta.getAttributesWithMissingConfigElements();
        for (int i = 0; i < attributesWithMissingConfigElements.length; i++) {
            String text = NLS.bind(Messages.ProductCmptGeneration_msgAttributeWithMissingConfigElement, attributesWithMissingConfigElements[i].getName());
            list.add(new Message(MSGCODE_ATTRIBUTE_WITH_MISSING_CONFIG_ELEMENT, text, Message.WARNING, this)); //$NON-NLS-1$
        }

        IProductCmptTypeRelation[] relationTypes = type.getRelations();
        for (int i = 0; i < relationTypes.length; i++) {
            IProductCmptRelation[] relations = getRelations(relationTypes[i].getTargetRoleSingular());

            // get all messages for the relation types and add them
            MessageList relMessages = relationTypes[i].validate();
            if (!relMessages.isEmpty()) {
                list.add(relMessages, new ObjectProperty(relationTypes[i].getTargetRoleSingular(), null), true);
            }

            if (relationTypes[i].getMinCardinality() > relations.length) {
                Object[] params = { new Integer(relations.length), relationTypes[i].getTargetRoleSingular(),
                        new Integer(relationTypes[i].getMinCardinality()) };
                String msg = NLS.bind(Messages.ProductCmptGeneration_msgNotEnoughRelations, params);
                ObjectProperty prop1 = new ObjectProperty(this, null);
                ObjectProperty prop2 = new ObjectProperty(relationTypes[i].getTargetRoleSingular(), null);
                list.add(new Message(MSGCODE_NOT_ENOUGH_RELATIONS, msg, Message.ERROR, new ObjectProperty[] { prop1,
                        prop2 }));
            }

            int maxCardinality = relationTypes[i].getMaxCardinality();
            if (maxCardinality < relations.length) {
                Object[] params = { new Integer(relations.length),
                        "" + maxCardinality, relationTypes[i].getTargetRoleSingular() }; //$NON-NLS-1$
                String msg = NLS.bind(Messages.ProductCmptGeneration_msgTooManyRelations, params);
                ObjectProperty prop1 = new ObjectProperty(this, null);
                ObjectProperty prop2 = new ObjectProperty(relationTypes[i].getTargetRoleSingular(), null);
                list.add(new Message(MSGCODE_TOO_MANY_RELATIONS, msg, Message.ERROR, new ObjectProperty[] { prop1,
                        prop2 }));
            }

            Map targets = new Hashtable();
            String msg = null;
            for (int j = 0; j < relations.length; j++) {
                String target = relations[j].getTarget();
                if (targets.get(target) != null) {
                    if (msg == null) {
                        msg = NLS.bind(Messages.ProductCmptGeneration_msgDuplicateTarget, relationTypes[i]
                                .getTargetRoleSingular(), target);
                    }
                    list.add(new Message(MSGCODE_DUPLICATE_RELATION_TARGET, msg, Message.ERROR, relationTypes[i]
                            .getTargetRoleSingular()));
                }
                else {
                    targets.put(target, target);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        if (partType.equals(IConfigElement.class)) {
            return newConfigElement();
        }
        else if (partType.equals(IRelation.class)) {
            return newRelation();
        }
        else if (partType.equals(ITableContentUsage.class)) {
            return newTableContentUsage();
        }

        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public ITableContentUsage getTableContentUsage(String rolename) {
        for (Iterator iter = this.tableContentUsages.iterator(); iter.hasNext();) {
            ITableContentUsage element = (ITableContentUsage)iter.next();
            if (element.getStructureUsage().equals(rolename)) {
                return element;
            }
        }
        return null;
    }

}
