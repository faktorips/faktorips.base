package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.w3c.dom.Element;


/**
 *
 */
public abstract class TimedIpsObject extends IpsObject implements ITimedIpsObject {
    
    private List generations = new ArrayList(0);

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
    
    public IIpsObjectGeneration newGeneration() {
        IpsObjectGeneration generation = newGenerationInternal(getNextPartId());
        updateSrcFile();
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
        updateSrcFile();
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
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        // nothing else to do so far
    }
    
    /**
     * Overridden.
     */
    protected final IIpsObjectPart newPart(String xmlTagName, int id) {
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
        // nothing else to do so far
    }
    
    /**
     * Overridden.
     */
    protected final void reAddPart(IIpsObjectPart part) {
        if (part instanceof IIpsObjectGeneration) {
            generations.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass());
    }
    
    /**
     * Overridden.
     */
    protected final void reinitPartCollections() {
        generations.clear();
    }
}
