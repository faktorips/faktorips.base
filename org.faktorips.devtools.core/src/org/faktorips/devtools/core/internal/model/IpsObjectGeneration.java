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

package org.faktorips.devtools.core.internal.model;

import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

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
    
    public final static String TAG_NAME = "Generation"; //$NON-NLS-1$
    
    private GregorianCalendar validFrom;

    public IpsObjectGeneration(ITimedIpsObject ipsObject, int id) {
        super(ipsObject, id);
    }

    protected IpsObjectGeneration() {
    }
    
    /**
     * {@inheritDoc}
     */
    public ITimedIpsObject getTimedIpsObject() {
        return (ITimedIpsObject)getIpsObject();
    }
    
    /**
     * {@inheritDoc}
     */
    public int getGenerationNo() {
        IIpsObjectGeneration[] generations = ((ITimedIpsObject)getIpsObject()).getGenerations();
        for (int i=0; i<generations.length; i++) {
            if (generations[i]==this) {
                return i+1;
            }
        }
        throw new RuntimeException("Coulnd't find the generation " + this + " in it's parent " + getIpsObject() + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
            return ""; //$NON-NLS-1$
        }
        DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
        return format.format(validFrom.getTime());
    }
    
    /**
     * {@inheritDoc}
     */
    public GregorianCalendar getValidFrom() {
        return validFrom;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setValidFrom(GregorianCalendar validFrom) {
        GregorianCalendar oldValue = this.validFrom;
        this.validFrom = validFrom;
        valueChanged(oldValue, validFrom);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public Image getImage() {
    	return IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptImage(Locale.getDefault());
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
        validFrom = XmlUtil.parseXmlDateStringToGregorianCalendar(element.getAttribute(PROPERTY_VALID_FROM));
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_VALID_FROM, XmlUtil.gregorianCalendarToXmlDateString(validFrom));
    }

    /**
     * {@inheritDoc}
     */
	public void initFromGeneration(IIpsObjectGeneration source) {
		int id = this.getId();
		Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
		this.initFromXml(source.toXml(doc), new Integer(id));
		updateSrcFile();
		
	}

	/**
     * {@inheritDoc}
     */
	public GregorianCalendar getValidTo() {
		IIpsObjectGeneration[] generations = this.getTimedIpsObject().getGenerations();
		
		for (int i = 0; i < generations.length; i++) {
			if (generations[i].getGenerationNo() == this.getGenerationNo()+1) {
				GregorianCalendar date = generations[i].getValidFrom();
				if (date != null) {
					// make a copy to not modify the validfrom-date of the generation
					date = (GregorianCalendar)date.clone();
					
					// reduce the valid-from date of the follow-up generation
					// by one millisecond to avoid that two generations are valid
					// at the same time. This generation is not valid at the time 
					// the follow-up generation is valid from.
					date.setTimeInMillis(date.getTimeInMillis() - 1);
				}
				return date;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectGeneration getNext() {
		IIpsObjectGeneration[] generations = getTimedIpsObject().getGenerations();
		int genIndex = getGenerationNo();
		
		if (generations.length > genIndex) {
			return generations[genIndex];
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectGeneration getPrevious() {
		IIpsObjectGeneration[] generations = getTimedIpsObject().getGenerations();
		int genIndex = getGenerationNo()-2;
		
		if (genIndex >= 0) {
			return generations[genIndex];
		}
		return null;
	}
}
