package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.util.message.MessageList;

/**
 * 
 */
public class ProductCmptGeneration extends IpsObjectGeneration implements
        IProductCmptGeneration {

    private List configElements = new ArrayList(0);
    private List relations = new ArrayList(0);

    public ProductCmptGeneration(ITimedIpsObject ipsObject, int id) {
        super(ipsObject, id);
    }

    public ProductCmptGeneration() {
    }

    /**
     * Overridden.
     */
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent();
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public IIpsElement[] getChildren() {
        int numOfChildren = getNumOfConfigElements() + getNumOfRelations();
	    IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
	    List childrenList = new ArrayList(numOfChildren);
	    childrenList.addAll(configElements);
	    childrenList.addAll(relations);
	    childrenList.toArray(childrenArray);
	    return childrenArray;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmpt#computeDeltaToPolicyCmptType()
     */
    public IProductCmptGenerationPolicyCmptTypeDelta computeDeltaToPolicyCmptType()
            throws CoreException {
        IPolicyCmptType pcType = getProductCmpt().findPolicyCmptType();
        if (pcType != null) {
            return new ProductCmptGenerationPolicyCmptTypeDelta(this, pcType);
        }
        return null;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#fixDifferences(org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta)
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
        }
        IConfigElement[] elements = delta.getConfigElementsWithMissingAttributes();
        for (int i = 0; i < elements.length; i++) {
            elements[i].delete();
        }
        elements = delta.getTypeMismatchElements();
        for (int i = 0; i < elements.length; i++) {
            IAttribute a = elements[i].findPcTypeAttribute();
            if (elements[i].getType() == ConfigElementType.FORMULA) {
                elements[i].setValue("");
            }
            elements[i].setType(a.getConfigElementType());
        }
        elements = delta.getElementsWithValueSetMismatch();
        for (int i = 0; i < elements.length; i++) {
            IAttribute a = elements[i].findPcTypeAttribute();
            elements[i].setValueSet(a.getValueSet().copy());
        }
        IProductCmptRelation[] relations = delta.getRelationsWithMissingPcTypeRelations();
        for (int i = 0; i < relations.length; i++) {
            relations[i].delete();
        }
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getConfigElements()
     */
    public IConfigElement[] getConfigElements() {
        return (IConfigElement[])configElements.toArray(new IConfigElement[configElements.size()]);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getConfigElement(java.lang.String)
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
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getConfigElements(org.faktorips.devtools.core.model.product.ConfigElementType)
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
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getNumOfConfigElements()
     */
    public int getNumOfConfigElements() {
        return configElements.size();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#newConfigElement()
     */
    public IConfigElement newConfigElement() {
        IConfigElement newElement = newConfigElementInternal(getNextPartId());
        updateSrcFile();
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

    void removeConfigElement(ConfigElement element) {
        configElements.remove(element);
        updateSrcFile();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getRelations()
     */
    public IProductCmptRelation[] getRelations() {
        return (IProductCmptRelation[])relations.toArray(new ProductCmptRelation[relations
                .size()]);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getRelations(java.lang.String)
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
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getNumOfRelations()
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
        updateSrcFile();
        return newRelation;
    }
    
    public IProductCmptRelation newRelation() {
    	return newRelationInternal(getNextPartId());
    }

    private ProductCmptRelation newRelationInternal(int id) {
        ProductCmptRelation newRelation = new ProductCmptRelation(this, id);
        relations.add(newRelation);
        return newRelation;
    }

    void removeRelation(ProductCmptRelation relation) {
        relations.remove(relation);
        updateSrcFile();
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getJavaType(int)
     */
    public IType getJavaType(int kind) throws CoreException {
        if (kind != JAVA_IMPLEMENTATION_TYPE) {
            throw new IllegalArgumentException("Unkown kind " + kind);
        }
        if (containsFormula()) {
            IPackageFragment pack = getIpsObject().getIpsPackageFragment().getJavaPackageFragment(
                IIpsPackageFragment.JAVA_PACK_IMPLEMENTATION);
            String javaTypeName = StringUtils.capitalise(getProductCmpt().getName());
            ICompilationUnit cu = pack.getCompilationUnit(javaTypeName + ".java");
            return cu.getType(javaTypeName);
        }
        IPolicyCmptType pcType = getProductCmpt().findPolicyCmptType();
        if (pcType == null) {
            throw new CoreException(new IpsStatus(
                    "Can't find corresponding policy component type for product component generation "
                            + this));
        }
        return pcType.getJavaType(IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE);
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getAllJavaTypes()
     */
    public IType[] getAllJavaTypes() throws CoreException {
        return new IType[] { getJavaType(JAVA_IMPLEMENTATION_TYPE) };
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#newPart(java.lang.String, int)
     */
    protected IIpsObjectPart newPart(String xmlTagName, int id) {
        if (xmlTagName.equals(ConfigElement.TAG_NAME)) {
            return newConfigElementInternal(id);
        } else if (xmlTagName.equals(ProductCmptRelation.TAG_NAME)) {
            return newRelationInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#reAddPart(org.faktorips.devtools.core.model.IIpsObjectPart)
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IConfigElement) {
            configElements.add(part);
            return;
        } else if (part instanceof IProductCmptRelation) {
            relations.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass());
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#reinitPartCollections()
     */
    protected void reinitPartCollections() {
        configElements.clear();
        relations.clear();
    }

    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        IConfigElement[] configElements = getConfigElements();
        for (int i = 0; i < configElements.length; i++) {
            //TODO the validate(MessageList) method needs to be moved up to the interface
            ((ConfigElement)configElements[i]).validate(list);
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
			
		throw new IllegalArgumentException("Unknown part type" + partType);
	}
    
}
