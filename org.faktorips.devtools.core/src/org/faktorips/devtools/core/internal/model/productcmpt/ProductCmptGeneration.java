/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung -
 * Version 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist
 * und auch unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *
 *  * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation -
 * http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.model.Dependency;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.ConfigElementType;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

/**
 * 
 */
public class ProductCmptGeneration extends IpsObjectGeneration implements IProductCmptGeneration {

    private List attributeValues = new ArrayList(0);
    
    private List configElements = new ArrayList(0);

    private List links = new ArrayList(0);

    private List tableContentUsages = new ArrayList(0);
    
    private List formulas = new ArrayList(0);

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
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmpt().findProductCmptType(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        int numOfChildren = getNumOfAttributeValues() + getNumOfConfigElements() + getNumOfLinks() + getNumOfTableContentUsages() + getNumOfFormulas();
        List childrenList = new ArrayList(numOfChildren);
        childrenList.addAll(attributeValues);        
        childrenList.addAll(configElements);
        childrenList.addAll(tableContentUsages);
        childrenList.addAll(formulas);
        childrenList.addAll(links);
        return (IIpsElement[])childrenList.toArray(new IIpsElement[childrenList.size()]);
    }
    
    public void dependsOn(Set dependencies) throws CoreException {
        addRelatedProductCmptQualifiedNameTypes(dependencies);
        addRelatedTableContentsQualifiedNameTypes(dependencies);
    }
    
    /*
     * Add the qualified name types of all related table contents inside the given generation to the
     * given set
     */
    private void addRelatedTableContentsQualifiedNameTypes(Set qaTypes) {
        ITableContentUsage[] tableContentUsages = getTableContentUsages();
        for (int i = 0; i < tableContentUsages.length; i++) {
            qaTypes.add(Dependency.createReferenceDependency(getIpsObject().getQualifiedNameType(),
                    new QualifiedNameType(tableContentUsages[i].getTableContentName(), IpsObjectType.TABLE_CONTENTS)));
        }
    }

    /*
     * Add the qualified name types of all related product cmpt's inside the given generation to the given set
     */
    private void addRelatedProductCmptQualifiedNameTypes(Set qaTypes) {
        IProductCmptLink[] relations = getLinks();
        for (int j = 0; j < relations.length; j++) {
            qaTypes.add(Dependency.createReferenceDependency(getIpsObject().getQualifiedNameType(),
                    new QualifiedNameType(relations[j].getTarget(), IpsObjectType.PRODUCT_CMPT)));
        }
    }

    
    /**
     * {@inheritDoc}
     */
    public IPropertyValue getPropertyValue(IProdDefProperty property) {
        if (property==null) {
            return null;
        }
        ProdDefPropertyType type = property.getProdDefPropertyType();
        if (type.equals(ProdDefPropertyType.VALUE)) {
            return getAttributeValue(property.getPropertyName());
        }
        if (type.equals(ProdDefPropertyType.TABLE_CONTENT_USAGE)) {
            return getTableContentUsage(property.getPropertyName());
        }
        if (type.equals(ProdDefPropertyType.FORMULA)) {
            return getFormula(property.getPropertyName());
        }
        if (type.equals(ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET)) {
            return getConfigElement(property.getPropertyName());
        }
        throw new RuntimeException("Unknown property type " + type);
    }
    
    /**
     * {@inheritDoc}
     */
    public IPropertyValue getPropertyValue(String propertyName) {
        if (propertyName==null) {
            return null;
        }
        IPropertyValue value = getAttributeValue(propertyName);
        if (value!=null) {
            return value;
        }
        value = getTableContentUsage(propertyName);
        if (value!=null) {
            return value;
        }
        value = getFormula(propertyName);
        if (value!=null) {
            return value;
        }
        return getConfigElement(propertyName); 
    }

    /**
     * {@inheritDoc}
     */
    public IPropertyValue[] getPropertyValues(ProdDefPropertyType type) {
        if (type==null) {
            return new IPropertyValue[0];
        }
        if (ProdDefPropertyType.VALUE.equals(type)) {
            return (IPropertyValue[])attributeValues.toArray(new IPropertyValue[attributeValues.size()]);
        }
        if (ProdDefPropertyType.TABLE_CONTENT_USAGE.equals(type)) {
            return (IPropertyValue[])tableContentUsages.toArray(new IPropertyValue[tableContentUsages.size()]);
        }
        if (ProdDefPropertyType.FORMULA.equals(type)) {
            return (IPropertyValue[])formulas.toArray(new IPropertyValue[formulas.size()]);
        }
        if (ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(type)) {
            return (IPropertyValue[])configElements.toArray(new IPropertyValue[configElements.size()]);
        }
        throw new RuntimeException("Unknown type " + type);
    }
    
    /**
     * {@inheritDoc}
     */
    public IPropertyValue newPropertyValue(IProdDefProperty property) {
        ProdDefPropertyType type = property.getProdDefPropertyType();
        if (ProdDefPropertyType.VALUE.equals(type)) {
            return newAttributeValue((IProductCmptTypeAttribute)property);
        }
        if (ProdDefPropertyType.TABLE_CONTENT_USAGE.equals(type)) {
            return newTableContentUsage((ITableStructureUsage)property);
        }
        if (ProdDefPropertyType.FORMULA.equals(type)) {
            return newFormula((IProductCmptTypeMethod)property);
        }
        if (ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(type)) {
            return newConfigElement((IPolicyCmptTypeAttribute)property);
        }
        throw new RuntimeException("Unknown type " + type);
    }

    /**
     * {@inheritDoc}
     */
    public IGenerationToTypeDelta computeDeltaToModel() throws CoreException {
        return new GenerationToTypeDelta(this);
    }

    /**
     * {@inheritDoc}
     */
    public IAttributeValue[] getAttributeValues() {
        return (IAttributeValue[])attributeValues.toArray(new IAttributeValue[attributeValues.size()]);    
    }
    
    /**
     * {@inheritDoc}
     */
    public IAttributeValue getAttributeValue(String attribute) {
        if (attribute==null) {
            return null;
        }
        for (Iterator it = attributeValues.iterator(); it.hasNext();) {
            IAttributeValue value= (IAttributeValue)it.next();
            if (attribute.equals(value.getAttribute())) {
                return value;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfAttributeValues() {
        return attributeValues.size();
    }

    /**
     * {@inheritDoc}
     */
    public IAttributeValue newAttributeValue() {
        return newAttributeValue(null);
    }

    /**
     * {@inheritDoc}
     */
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute) {
        return newAttributeValueInternal(getNextPartId(), attribute, attribute==null ? "" : attribute.getDefaultValue());
    }

    /**
     * {@inheritDoc}
     */
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute, String value) {
        IAttributeValue newValue = newAttributeValueInternal(getNextPartId(), attribute, value);
        objectHasChanged();
        return newValue;
    }
    
    /*
     * Creates a new attribute value without updating the src file.
     */
    private AttributeValue newAttributeValueInternal(int id) {
        AttributeValue av = new AttributeValue(this, id);
        attributeValues.add(av);
        return av;
    }

    /*
     * Creates a new attribute value without updating the src file.
     */
    private AttributeValue newAttributeValueInternal(int id, IProductCmptTypeAttribute attr, String value) {
        AttributeValue av = new AttributeValue(this, id, attr==null ? "" : attr.getName(), value);
        attributeValues.add(av);
        return av;
    }

    /**
     * {@inheritDoc
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
            if (each.getPolicyCmptTypeAttribute().equals(attributeName)) {
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
        return newConfigElement(null);
    }
    
    /**
     * {@inheritDoc}
     */
    public IConfigElement newConfigElement(IPolicyCmptTypeAttribute attribute) {
        IConfigElement newElement = newConfigElementInternal(getNextPartId(), attribute);
        objectHasChanged();
        return newElement;
    }

    /*
     * Creates a new attribute without updating the src file.
     */
    private ConfigElement newConfigElementInternal(int id, IPolicyCmptTypeAttribute attribute) {
        ConfigElement e = new ConfigElement(this, id);
        if (attribute!=null) {
            try {
                ((IpsModel)getIpsModel()).stopBroadcastingChangesMadeByCurrentThread();
                e.setPolicyCmptTypeAttribute(attribute.getName());
                e.setValue(attribute.getDefaultValue());
                e.setValueSetCopy(attribute.getValueSet());
            } finally {
                ((IpsModel)getIpsModel()).resumeBroadcastingChangesMadeByCurrentThread();
            }
        }
        configElements.add(e);
        return e;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptLink[] getLinks() {
        return (IProductCmptLink[])links.toArray(new ProductCmptLink[links.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptLink[] getLinks(String typeRelation) {
        List result = new ArrayList();
        for (Iterator it = links.iterator(); it.hasNext();) {
            IProductCmptLink relation = (IProductCmptLink)it.next();
            if (relation.getAssociation().equals(typeRelation)) {
                result.add(relation);
            }
        }
        return (IProductCmptLink[])result.toArray(new ProductCmptLink[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfLinks() {
        return links.size();
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptLink newLink(IProductCmptTypeAssociation association) {
        return newLink(association.getName());
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptLink newLink(String associationName) {
        ProductCmptLink newRelation = newLinkInternal(getNextPartId());
        newRelation.setProductCmptTypeRelation(associationName);
        objectHasChanged();
        return newRelation;
    }

    public IProductCmptLink newLink(String associationName, IProductCmptLink insertBefore) {
        ProductCmptLink newRelation = newRelationInternal(getNextPartId(), insertBefore);
        newRelation.setProductCmptTypeRelation(associationName);
        objectHasChanged();
        return newRelation;
    }

    public IProductCmptLink newLink() {
        return newLinkInternal(getNextPartId());
    }

    /**
     * {@inheritDoc}
     */
    public boolean canCreateValidLink(IProductCmpt target, String associationName, IIpsProject ipsProject) throws CoreException {
        if (associationName == null || target == null) {
            return false;
        }
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type == null) {
            return false;
        }
        IAssociation association = type.findAssociation(associationName, ipsProject);
        if (association == null) {
            return false;
        }
        // it is not valid to create more than one relation with the same type and target.
        if (!isFirstRelationOfThisType(association, target, ipsProject)) {
            return false;
        }
        return this.getLinks(associationName).length < association.getMaxCardinality()
                && ProductCmptLink.willBeValid(target, association, ipsProject);
    }

    private boolean isFirstRelationOfThisType(IAssociation association, IProductCmpt target, IIpsProject ipsProject)
            throws CoreException {
        for (Iterator iter = links.iterator(); iter.hasNext();) {
            IProductCmptLink link = (IProductCmptLink)iter.next();
            if (link.findAssociation(ipsProject).equals(association) && link.getTarget().equals(target.getQualifiedName())) {
                return false;
            }
        }
        return true;
    }

    private ProductCmptLink newRelationInternal(int id, IProductCmptLink insertBefore) {
        ProductCmptLink newRelation = new ProductCmptLink(this, id);
        if (insertBefore == null) {
            links.add(newRelation);
        }
        else {
            int index = links.indexOf(insertBefore);
            if (index == -1) {
                links.add(newRelation);
            }
            else {
                links.add(index, newRelation);
            }
        }
        return newRelation;
    }

    private ProductCmptLink newLinkInternal(int id) {
        return newRelationInternal(id, null);
    }

    /**
     * {@inheritDoc}
     */
    public void moveLink(IProductCmptLink toMove, IProductCmptLink moveBefore) {
        links.remove(toMove);
        int index = links.indexOf(moveBefore);
        if (index == -1) {
            links.add(toMove);
        }
        else {
            links.add(index, toMove);
        }
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public ITableContentUsage newTableContentUsage() {
        return newTableContentUsage(null);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITableContentUsage newTableContentUsage(ITableStructureUsage structureUsage) {
        ITableContentUsage newUsage = newTableContentUsageInternal(getNextPartId(), structureUsage);
        objectHasChanged();
        return newUsage;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfTableContentUsages() {
        return getTableContentUsages().length;
    }

    /**
     * {@inheritDoc}
     */
    public ITableContentUsage[] getTableContentUsages() {
        return (ITableContentUsage[])tableContentUsages.toArray(new ITableContentUsage[tableContentUsages.size()]);
    }

    private ITableContentUsage newTableContentUsageInternal(int id, ITableStructureUsage structureUsage) {
        ITableContentUsage retValue = new TableContentUsage(this, id, structureUsage == null ? "" : structureUsage.getRoleName());
        tableContentUsages.add(retValue);
        return retValue;
    }

    /**
     * Returns true if the generation contains a formula config element, otherwise false.
     */
    public boolean containsFormula() {
        return formulas.size() > 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumOfFormulas() {
        return formulas.size();
    }

    /**
     * {@inheritDoc}
     */
    public IFormula[] getFormulas() {
        return (IFormula[])formulas.toArray(new IFormula[formulas.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public IFormula getFormula(String formulaName) {
        if (formulaName==null) {
            return null;
        }
        for (Iterator it = formulas.iterator(); it.hasNext();) {
            IFormula formula = (IFormula)it.next();
            if (formulaName.equals(formula.getName())) {
                return formula;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IFormula newFormula() {
        return newFormula(null);
    }

    /**
     * {@inheritDoc}
     */
    public IFormula newFormula(IProductCmptTypeMethod signature) {
        IFormula newFormula = newFormulaInternal(getNextPartId(), signature);
        objectHasChanged();
        return newFormula;
    }

    private IFormula newFormulaInternal(int id, IProductCmptTypeMethod signature) {
        IFormula newFormula = new Formula(this, id, signature==null ? "" : signature.getFormulaName());
        formulas.add(newFormula);
        return newFormula;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        if (partType.equals(IAttributeValue.class)) {
            return newAttributeValue();
        }
        if (partType.equals(IConfigElement.class)) {
            return newConfigElement();
        }
        else if (partType.equals(IPolicyCmptTypeAssociation.class)) {
            return newLink();
        }
        else if (partType.equals(ITableContentUsage.class)) {
            return newTableContentUsage();
        }
        else if (partType.equals(IFormula.class)) {
            return newFormula();
        }
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(AttributeValue.TAG_NAME)) {
            return newAttributeValueInternal(id);
        } else if (xmlTagName.equals(ConfigElement.TAG_NAME)) {
            return newConfigElementInternal(id, null);
        } else if (xmlTagName.equals(ProductCmptLink.TAG_NAME)) {
            return newLinkInternal(id);
        } else if (xmlTagName.equals(ITableContentUsage.TAG_NAME)) {
            return newTableContentUsageInternal(id, null);
        } else if (xmlTagName.equals(Formula.TAG_NAME)) {
            return newFormulaInternal(id, null);
        }
        throw new RuntimeException("Could not create part for tag " + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IAttributeValue) {
            attributeValues.add(part);
            return;
        } else if (part instanceof IConfigElement) {
            configElements.add(part);
            return;
        } else if (part instanceof IProductCmptLink) {
            links.add(part);
            return;
        } else if (part instanceof ITableContentUsage) {
            tableContentUsages.add(part);
            return;
        } else if (part instanceof IFormula) {
            formulas.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IAttributeValue) {
            attributeValues.remove(part);
            return;
        } else if (part instanceof IConfigElement) {
            configElements.remove(part);
            return;
        } else if (part instanceof IProductCmptLink) {
            links.remove(part);
            return;
        } else if (part instanceof ITableContentUsage) {
            tableContentUsages.remove(part);
            return;
        } else if (part instanceof IFormula) {
            tableContentUsages.remove(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        attributeValues.clear();
        configElements.clear();
        links.clear();
        tableContentUsages.clear();
        formulas.clear();
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        IIpsProject ipsProject = getIpsProject();
        IProductCmptType type = getProductCmpt().findProductCmptType(ipsProject);
        // no type information available, so no further validation possible
        if (type == null) {
            list.add(new Message(MSGCODE_NO_TEMPLATE, Messages.ProductCmptGeneration_msgTemplateNotFound,
                    Message.ERROR, this));
            return;
        }

        IGenerationToTypeDelta delta = computeDeltaToModel();
        IDeltaEntry[] entries = delta.getEntries();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getDeltaType()==DeltaType.MISSING_PROPERTY_VALUE) {
                String text = NLS.bind(Messages.ProductCmptGeneration_msgAttributeWithMissingConfigElement, entries[i].getPropertyName());
                list.add(new Message(MSGCODE_ATTRIBUTE_WITH_MISSING_CONFIG_ELEMENT, text, Message.WARNING, this)); //$NON-NLS-1$
            }
        }

        IAssociation[] relationTypes = type.getAssociations();
        for (int i = 0; i < relationTypes.length; i++) {
            IProductCmptLink[] relations = getLinks(relationTypes[i].getTargetRoleSingular());

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
    public ITableContentUsage getTableContentUsage(String rolename) {
        if (rolename==null) {
            return null;
        }
        for (Iterator iter = this.tableContentUsages.iterator(); iter.hasNext();) {
            ITableContentUsage element = (ITableContentUsage)iter.next();
            if (rolename.equals(element.getStructureUsage())) {
                return element;
            }
        }
        return null;
    }

    
}
