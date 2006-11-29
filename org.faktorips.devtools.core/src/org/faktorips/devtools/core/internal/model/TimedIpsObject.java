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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public abstract class TimedIpsObject extends IpsObject implements ITimedIpsObject {
    
    private List generations = new ArrayList(0);
    private GregorianCalendar validTo; 

    public TimedIpsObject(IIpsSrcFile file) {
        super(file);
    }

    public TimedIpsObject() {
        super();
    }

    /** 
     * Overridden.
     */
    public boolean changesOn(GregorianCalendar date) {
        for (Iterator it=generations.iterator(); it.hasNext();) {
            IIpsObjectGeneration gen = (IIpsObjectGeneration)it.next();
            if (gen.getValidFrom().equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectGeneration getFirstGeneration() {
    	if (this.generations.size() > 0) {
    	return this.getGenerations()[0];
    	}    	
    	return null;
    }

    /** 
     * Overridden.
     */
    public IIpsObjectGeneration[] getGenerations() {
        IIpsObjectGeneration[] gens = (IIpsObjectGeneration[])generations.toArray(new IIpsObjectGeneration[generations.size()]);
        Arrays.sort(gens, new Comparator() {

            public int compare(Object o1, Object o2) {
                IIpsObjectGeneration gen1 = (IIpsObjectGeneration)o1;
                IIpsObjectGeneration gen2 = (IIpsObjectGeneration)o2;
                if (gen1.getValidFrom()==null) {
                    return gen2.getValidFrom()==null ? 0 : -1;
                }
                return gen1.getValidFrom().after(gen2.getValidFrom()) ? 1 : -1;
            }
            
        });
        return gens;
    }
    
    /**
     * Overridden.
     */
    public IIpsObjectGeneration findGenerationEffectiveOn(GregorianCalendar date) {
        if (date==null) {
            return null;
        }
        IIpsObjectGeneration generation = null;
        for (Iterator it=generations.iterator(); it.hasNext();) {
            IIpsObjectGeneration each = (IIpsObjectGeneration)it.next();
            if (!each.getValidFrom().after(date)) {
                if (generation==null) {
                    generation = each;
                } else {
                    if (each.getValidFrom().after(generation.getValidFrom())) {
                        generation = each;
                    }
                }
            }
        }
        
        // exclude an (invalid) generation which has a valid-from date after the valid-to date 
        // of this IpsObject.
        if (generation != null && getValidTo() != null && generation.getValidFrom().after(getValidTo())) {
            return null;
        }
        
        return generation;
    }

    /**
     * Overridden.
     */
    public IIpsObjectGeneration getGenerationByEffectiveDate(GregorianCalendar date) {
        for (Iterator it=generations.iterator(); it.hasNext();) {
            IIpsObjectGeneration each = (IIpsObjectGeneration)it.next();
            if (each.getValidFrom().equals(date)) {
                return each;
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsObjectGeneration newGeneration() {
        IpsObjectGeneration generation = newGenerationInternal(getNextPartId());
        objectHasChanged();
        return generation;
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsObjectGeneration newGeneration(GregorianCalendar validFrom) {
        IIpsObjectGeneration oldGen = findGenerationEffectiveOn(validFrom);
        return newGeneration(oldGen, validFrom);
    }

    public IIpsObjectGeneration newGeneration(IIpsObjectGeneration source, GregorianCalendar validFrom) {
    	int newId = getNextPartId();
        IpsObjectGeneration generation = newGenerationInternal(newId);

        if (source != null) {
        	generation.initFromGeneration(source);
        }

        generation.setValidFrom(validFrom);
        
        objectHasChanged();
        return generation;
    }

    public int getNumOfGenerations() {
        return generations.size();
    }
    
    IpsObjectGeneration newGenerationInternal(int id) {
        IpsObjectGeneration generation = createNewGeneration(id);
        generations.add(generation);
        return generation;
    }
    
    void removeGeneration(IIpsObjectGeneration generation) {
        generations.remove(generation);
    }
    
    /**
     * Creates a new generation instance. Subclass have to override to
     * and return an instance of the correct subclass of IpsObjectGenerationImpl.
     * 
     * @param id the unique id for the new generation.
     */
    protected abstract IpsObjectGeneration createNewGeneration(int id);

    /** 
     * Returns the object's generations. 
     * 
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public IIpsElement[] getChildren() {
        return getGenerations();
    }
    
    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        validTo = XmlUtil.parseXmlDateStringToGregorianCalendar(ValueToXmlHelper.getValueFromElement(element, PROPERTY_VALID_TO));
    }
    
    /**
     * Overridden.
     */
    protected final IIpsObjectPart newPart(Element xmlTag, int id) {
    	String xmlTagName = xmlTag.getNodeName();

        if (xmlTagName.equals(IpsObjectGeneration.TAG_NAME)) {
            return newGenerationInternal(id);
        }
        return null;
    }
    
    /**
     * Overridden.
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        if (validTo == null) {
            ValueToXmlHelper.addValueToElement(null, element, PROPERTY_VALID_TO);
        }
        else {
            ValueToXmlHelper.addValueToElement(XmlUtil.gregorianCalendarToXmlDateString(validTo), element, PROPERTY_VALID_TO);
        }
    }
    
    /**
     * Overridden.
     */
    protected final void reAddPart(IIpsObjectPart part) {
        if (part instanceof IIpsObjectGeneration) {
            generations.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
     * Overridden.
     */
    protected final void reinitPartCollections() {
        generations.clear();
    }

    /**
     * {@inheritDoc}
     */
    public GregorianCalendar getValidTo() {
        return validTo;
    }

    /**
     * {@inheritDoc}
     */
    public void setValidTo(GregorianCalendar validTo) {
        GregorianCalendar oldId = this.validTo;
        this.validTo = validTo;
        valueChanged(oldId, validTo);
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        GregorianCalendar validTo = getValidTo();
        
        if (validTo == null) {
            // empty validTo - valid forever.
            return;
        }
        
        IIpsObjectGeneration[] generations = getGenerations();
        for (int i = 0; i < generations.length; i++) {
            if (generations[i].getValidFrom().after(validTo)) {
                IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
                String params[] = new String[4];
                params[0] = prefs.getValidFromFormat().format(validTo.getTime());
                params[1] = prefs.getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
                params[2] = "" + generations[i].getGenerationNo(); //$NON-NLS-1$
                params[3] = prefs.getValidFromFormat().format(generations[i].getValidFrom().getTime());
                String msg = NLS.bind(org.faktorips.devtools.core.internal.model.Messages.TimedIpsObject_msgIvalidValidToDate, params);
                list.add(new Message(MSGCODE_INVALID_VALID_TO, msg, Message.ERROR, this, PROPERTY_VALID_TO));
            }
        }
    }
    
    
}
