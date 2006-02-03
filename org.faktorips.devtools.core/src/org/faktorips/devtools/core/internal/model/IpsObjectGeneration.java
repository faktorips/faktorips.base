package org.faktorips.devtools.core.internal.model;

import java.text.DateFormat;
import java.util.GregorianCalendar;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public abstract class IpsObjectGeneration extends IpsObjectPart implements IIpsObjectGeneration {
    
    public final static String TAG_NAME = "Generation";
    
    private GregorianCalendar validFrom;

    public IpsObjectGeneration(ITimedIpsObject ipsObject, int id) {
        super(ipsObject, id);
    }

    protected IpsObjectGeneration() {
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectGeneration#getIpsObject()
     */
    public ITimedIpsObject getTimedIpsObject() {
        return (ITimedIpsObject)getIpsObject();
    }
    
    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectGeneration#getGenerationNo()
     */
    public int getGenerationNo() {
        IIpsObjectGeneration[] generations = ((ITimedIpsObject)getIpsObject()).getGenerations();
        for (int i=0; i<generations.length; i++) {
            if (generations[i]==this) {
                return i+1;
            }
        }
        throw new RuntimeException("Coulnd't find the generation " + this + " in it's parent " + getIpsObject() + "!");
    }
    
    /**
     * Returns the valid from formatted with the default <code>DataFormat</code>
     * instance.
     *  
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName() {
        if (validFrom==null) {
            return "";
        }
        DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
        return format.format(validFrom.getTime());
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectGeneration#getValidFrom()
     */
    public GregorianCalendar getValidFrom() {
        return validFrom;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectGeneration#setValidFrom(java.util.GregorianCalendar)
     */
    public void setValidFrom(GregorianCalendar validFrom) {
        GregorianCalendar oldValue = this.validFrom;
        this.validFrom = validFrom;
        valueChanged(oldValue, validFrom);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        ((TimedIpsObject)getTimedIpsObject()).removeGeneration(this);
        parent = null;
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("Generation.gif");
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#createElement(org.w3c.dom.Document)
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        validFrom = XmlUtil.parseXmlDateStringToGregorianCalendar(element.getAttribute(PROPERTY_VALID_FROM));
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_VALID_FROM, XmlUtil.gregorianCalendarToXmlDateString(validFrom));
    }
}
