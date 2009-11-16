/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 *
 */
public abstract class TimedIpsObject extends IpsObject implements ITimedIpsObject {

    private List<IIpsObjectGeneration> generations = new ArrayList<IIpsObjectGeneration>();
    private GregorianCalendar validTo;

    public TimedIpsObject(IIpsSrcFile file) {
        super(file);
    }

    public TimedIpsObject() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public boolean changesOn(GregorianCalendar date) {
        for (Iterator<IIpsObjectGeneration> it = generations.iterator(); it.hasNext();) {
            IIpsObjectGeneration gen = it.next();
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
        if (generations.size() > 0) {
            return getGenerationsOrderedByValidDate()[0];
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectGeneration getGeneration(int index) {
        return generations.get(index);
    }

    public List<IIpsObjectGeneration> getGenerations() {
        return new ArrayList<IIpsObjectGeneration>(generations);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectGeneration[] getGenerationsOrderedByValidDate() {
        IIpsObjectGeneration[] gens = generations.toArray(new IIpsObjectGeneration[generations.size()]);
        Arrays.sort(gens, new Comparator<IIpsObjectGeneration>() {

            public int compare(IIpsObjectGeneration gen1, IIpsObjectGeneration gen2) {
                if (gen1.getValidFrom() == null) {
                    return gen2.getValidFrom() == null ? 0 : -1;
                }
                return gen1.getValidFrom().after(gen2.getValidFrom()) ? 1 : -1;
            }

        });
        return gens;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectGeneration findGenerationEffectiveOn(GregorianCalendar date) {
        if (date == null) {
            return null;
        }
        IIpsObjectGeneration generation = null;
        for (Iterator<IIpsObjectGeneration> it = generations.iterator(); it.hasNext();) {
            IIpsObjectGeneration each = it.next();
            if (!each.getValidFrom().after(date)) {
                if (generation == null) {
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
     * {@inheritDoc}
     */
    public IIpsObjectGeneration getGenerationByEffectiveDate(GregorianCalendar date) {
        if (date == null) {
            return null;
        }
        for (Iterator<IIpsObjectGeneration> it = generations.iterator(); it.hasNext();) {
            IIpsObjectGeneration each = it.next();
            if (date.equals(each.getValidFrom())) {
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
            generation.initFromGeneration(source, validFrom);
        } else {
            generation.setValidFromInternal(validFrom);
        }
        partWasAdded(generation);
        return generation;
    }

    public int getNumOfGenerations() {
        return generations.size();
    }

    protected IpsObjectGeneration newGenerationInternal(int id) {
        IpsObjectGeneration generation = createNewGeneration(id);
        generations.add(generation);
        return generation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IIpsObjectGeneration) {
            generations.remove(part);
        }
    }

    /**
     * Creates a new generation instance. Subclass have to override to and return an instance of the
     * correct subclass of IpsObjectGenerationImpl.
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
    @Override
    public IIpsElement[] getChildren() {
        return getGenerationsOrderedByValidDate();
    }

    /**
     * Overridden.
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        validTo = XmlUtil.parseXmlDateStringToGregorianCalendar(ValueToXmlHelper.getValueFromElement(element,
                PROPERTY_VALID_TO));
    }

    /**
     * Overridden.
     */
    @Override
    protected final IIpsObjectPart newPart(Element xmlTag, int id) {
        String xmlTagName = xmlTag.getNodeName();

        if (xmlTagName.equals(IIpsObjectGeneration.TAG_NAME)) {
            return newGenerationInternal(id);
        }
        return null;
    }

    /**
     * Overridden.
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        if (validTo == null) {
            ValueToXmlHelper.addValueToElement(null, element, PROPERTY_VALID_TO);
        } else {
            ValueToXmlHelper.addValueToElement(XmlUtil.gregorianCalendarToXmlDateString(validTo), element,
                    PROPERTY_VALID_TO);
        }
    }

    /**
     * Overridden.
     */
    @Override
    protected final void addPart(IIpsObjectPart part) {
        if (part instanceof IIpsObjectGeneration) {
            generations.add((IIpsObjectGeneration)part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    @Override
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
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        GregorianCalendar validTo = getValidTo();

        if (validTo == null) {
            // empty validTo - valid forever.
            return;
        }

        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (int i = 0; i < generations.length; i++) {
            if (generations[i].getValidFrom().after(validTo)) {
                IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
                String params[] = new String[4];
                params[0] = prefs.getDateFormat().format(validTo.getTime());
                params[1] = prefs.getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
                params[2] = "" + generations[i].getGenerationNo(); //$NON-NLS-1$
                params[3] = prefs.getDateFormat().format(generations[i].getValidFrom().getTime());
                String msg = NLS.bind(Messages.TimedIpsObject_msgIvalidValidToDate, params);
                list.add(new Message(MSGCODE_INVALID_VALID_TO, msg, Message.ERROR, this, PROPERTY_VALID_TO));
            }
        }
    }

}
