package org.faktorips.devtools.core.internal.model.businessfct;

import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.businessfct.BusinessFunction;
import org.w3c.dom.Element;


/**
 *
 */
public class BusinessFunctionImpl extends IpsObject implements
        BusinessFunction {

    public BusinessFunctionImpl(IIpsSrcFile file) {
        super(file);
    }

    // TODO remove temporay constructor
    public BusinessFunctionImpl(String name) {
        this.name = name;
    }
    

    public BusinessFunctionImpl() {
        super();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObject#getIpsObjectType()
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.BUSINESS_FUNCTION;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#getChildren()
     */
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element newElement) {
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element) {
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#reinitPartCollections()
     */
    protected void reinitPartCollections() {
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#reAddPart(org.faktorips.devtools.core.model.IIpsObjectPart)
     */
    protected void reAddPart(IIpsObjectPart part) {
    }

    /**
     * Overridden IMethod. 
     * 
     * BusinessFunctions don't have any part, so this method should never be called.
     * 
     * @throws RuntimeException if the method is called.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#newPart(java.lang.String, int)
     */
    protected IIpsObjectPart newPart(String xmlTagName, int id) {
        throw new RuntimeException("newPart() not supported.");
    }

    /**
     * {@inheritDoc}
     */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType);
	}
}
