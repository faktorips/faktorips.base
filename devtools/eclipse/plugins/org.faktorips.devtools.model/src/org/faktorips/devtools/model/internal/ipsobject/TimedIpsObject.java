/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.XmlParseException;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

public abstract class TimedIpsObject extends IpsObject implements ITimedIpsObject {

    private final List<IIpsObjectGeneration> generations = new ArrayList<>();

    private GregorianCalendar validFrom;
    private GregorianCalendar validTo;

    public TimedIpsObject(IIpsSrcFile file) {
        super(file);
    }

    public TimedIpsObject() {
        super();
    }

    @Override
    public boolean changesOn(GregorianCalendar date) {
        for (IIpsObjectGeneration gen : generations) {
            if (gen.getValidFrom().equals(date)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IIpsObjectGeneration getFirstGeneration() {
        if (generations.size() > 0) {
            return getGenerationsOrderedByValidDate()[0];
        }
        return null;
    }

    @Override
    public IIpsObjectGeneration getGeneration(int index) {
        return generations.get(index);
    }

    @Override
    public List<IIpsObjectGeneration> getGenerations() {
        return new ArrayList<>(generations);
    }

    @Override
    public IIpsObjectGeneration[] getGenerationsOrderedByValidDate() {
        IIpsObjectGeneration[] gens = generations.toArray(new IIpsObjectGeneration[generations.size()]);
        Arrays.sort(gens, (gen1, gen2) -> {
            if (gen1.getValidFrom() == null) {
                return gen2.getValidFrom() == null ? 0 : -1;
            } else if (gen2.getValidFrom() == null) {
                return 1;
            }
            return gen1.getValidFrom().after(gen2.getValidFrom()) ? 1 : -1;
        });
        return gens;
    }

    @Override
    public IIpsObjectGeneration getGenerationEffectiveOn(GregorianCalendar date) {
        if (date == null) {
            return null;
        }
        IIpsObjectGeneration generation = null;
        for (IIpsObjectGeneration each : generations) {
            if (!each.getValidFrom().after(date)) {
                if ((generation == null)
                        || each.getValidFrom().after(generation.getValidFrom())) {
                    generation = each;
                }
            }
        }

        // exclude an (invalid) generation which has a valid-from date after the valid-to date
        // of this IpsObject.
        if (generation != null && getValidTo() != null && date.after(getValidTo())) {
            return null;
        }

        return generation;
    }

    @Override
    public IIpsObjectGeneration getBestMatchingGenerationEffectiveOn(GregorianCalendar date) {
        IIpsObjectGeneration generationEffectiveOn = getGenerationEffectiveOn(date);
        if (generationEffectiveOn == null) {
            if (date.after(getValidTo())) {
                return getLatestGeneration();
            } else {
                return getFirstGeneration();
            }
        } else {
            return generationEffectiveOn;
        }
    }

    @Override
    public IIpsObjectGeneration getGenerationByEffectiveDate(GregorianCalendar date) {
        if (date == null) {
            return null;
        }
        for (IIpsObjectGeneration each : generations) {
            if (date.equals(each.getValidFrom())) {
                return each;
            }
        }
        return null;
    }

    @Override
    public IIpsObjectGeneration getLatestGeneration() {
        IIpsObjectGeneration[] generationsOrderedByValidDate = getGenerationsOrderedByValidDate();
        if (generationsOrderedByValidDate.length > 0) {
            return generationsOrderedByValidDate[generationsOrderedByValidDate.length - 1];
        }
        return null;
    }

    @Override
    public IIpsObjectGeneration newGeneration() {
        IpsObjectGeneration generation = newGenerationInternal(getNextPartId());
        partWasAdded(generation);
        return generation;
    }

    @Override
    public IIpsObjectGeneration newGeneration(GregorianCalendar validFrom) {
        ArgumentCheck.notNull(validFrom);
        IIpsObjectGeneration oldGen = getGenerationEffectiveOn(validFrom);
        return newGeneration(oldGen, validFrom);
    }

    public IIpsObjectGeneration newGeneration(IIpsObjectGeneration source, GregorianCalendar validFrom) {
        String newId = getNextPartId();
        IpsObjectGeneration generation = newGenerationInternal(newId);

        if (source != null) {
            generation.copyFrom(source);
        }
        generation.setValidFromInternal(validFrom);
        partWasAdded(generation);
        return generation;
    }

    @Override
    public int getNumOfGenerations() {
        return generations.size();
    }

    protected IpsObjectGeneration newGenerationInternal(String id) {
        IpsObjectGeneration generation = createNewGeneration(id);
        generations.add(generation);
        return generation;
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        List<IIpsElement> result = new ArrayList<>(Arrays.asList(getGenerationsOrderedByValidDate()));
        return result.toArray(new IIpsElement[result.size()]);
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IIpsObjectGeneration) {
            generations.add((IIpsObjectGeneration)part);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IIpsObjectGeneration) {
            generations.remove(part);
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(IIpsObjectGeneration.TAG_NAME)) {
            return newGenerationInternal(id);
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (IIpsObjectGeneration.class.isAssignableFrom(partType)) {
            return newGenerationInternal(getNextPartId());
        } else {
            return null;
        }
    }

    @Override
    protected void reinitPartCollectionsThis() {
        generations.clear();
    }

    /**
     * Creates a new generation instance. Subclass have to override to and return an instance of the
     * correct subclass of IpsObjectGenerationImpl.
     * 
     * @param id the unique id for the new generation.
     */
    protected abstract IpsObjectGeneration createNewGeneration(String id);

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        try {
            validTo = XmlUtil.parseGregorianCalendar(ValueToXmlHelper.getValueFromElement(element, PROPERTY_VALID_TO));
        } catch (XmlParseException e) {
            throw new RuntimeException(e);
        }
    }

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

    @Override
    public GregorianCalendar getValidFrom() {
        if (getFirstGeneration() == null) {
            return validFrom;
        }
        return getFirstGeneration().getValidFrom();
    }

    @Override
    public void setValidFrom(GregorianCalendar validFrom) {
        if (getFirstGeneration() == null) {
            this.validFrom = validFrom;
        } else {
            getFirstGeneration().setValidFrom(validFrom);
        }
    }

    @Override
    public GregorianCalendar getValidTo() {
        return validTo;
    }

    @Override
    public void setValidTo(GregorianCalendar validTo) {
        GregorianCalendar oldId = this.validTo;
        this.validTo = validTo;
        valueChanged(oldId, validTo);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        validateValidFrom(list);
        validateValidTo(list);
    }

    /**
     * This method ensures the validation of the valid from date.
     */
    private void validateValidFrom(MessageList list) {
        if (getValidFrom() == null) {
            list.add(Message.newError(MSGCODE_INVALID_FORMAT_VALID_FROM,
                    Messages.IpsObjectGeneration_msgInvalidFormatFromDate + getDefaultDateFormat(), this,
                    PROPERTY_VALID_FROM));
        }
    }

    public static String getDefaultDateFormat() {
        DateFormat dateFormat = IIpsModelExtensions.get().getModelPreferences().getDateFormat();
        String formatDescription = dateFormat.format(new GregorianCalendar().getTime());
        if (dateFormat instanceof SimpleDateFormat) {
            formatDescription = ((SimpleDateFormat)dateFormat).toPattern();
        }
        return formatDescription;
    }

    private void validateValidTo(MessageList list) {
        if (getValidTo() == null) {
            // empty validTo - valid forever
            return;
        }

        IIpsObjectGeneration[] generationsByValidDate = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generationsByValidDate) {
            if (generation.getValidFrom() != null && generation.getValidFrom().after(getValidTo())) {
                Object[] params = new Object[4];
                params[0] = IIpsModelExtensions.get().getModelPreferences().getDateFormat()
                        .format(getValidTo().getTime());
                params[1] = IIpsModelExtensions.get().getModelPreferences().getChangesOverTimeNamingConvention()
                        .getGenerationConceptNameSingular();
                params[2] = "" + generation.getGenerationNo(); //$NON-NLS-1$
                params[3] = IIpsModelExtensions.get().getModelPreferences().getDateFormat()
                        .format(generation.getValidFrom().getTime());
                String msg = MessageFormat.format(Messages.TimedIpsObject_msgIvalidValidToDate, params);
                list.add(new Message(MSGCODE_INVALID_VALID_TO, msg, Message.ERROR, this, PROPERTY_VALID_TO));
            }
        }
    }

    @Override
    public void reassignGenerations(GregorianCalendar newDate) {
        int generationIndex = 0;
        int newGenerationCount = 0;
        int oldGenerationCount = getNumOfGenerations();
        boolean hasMoreGenerations;

        for (IIpsObjectGeneration generation : getGenerationsOrderedByValidDate()) {
            hasMoreGenerations = generationIndex < oldGenerationCount - 1;
            if (newGenerationCount == 0) {
                if (hasMoreGenerations) {
                    // The generation expires before the new valid from date, hence do not add
                    // it to the target.
                    if (generation.getValidTo() != null && generation.getValidTo().before(newDate)) {
                        generationIndex++;
                        generation.delete();
                        continue;
                    }
                }
                boolean validBefore = generation.getValidFrom().before(newDate);
                boolean validAfter = generation.getValidFrom().after(newDate)
                        && (generation.getValidTo() == null || generation.getValidTo().after(newDate));
                if (validBefore || validAfter) {
                    // The first generation in the target must be valid from the new date.
                    generation.setValidFrom(newDate);
                }
            }
            generationIndex++;
            newGenerationCount++;
        }
        ensureGenerationExists(newDate);
    }

    private void ensureGenerationExists(GregorianCalendar newDate) {
        IIpsObjectGeneration generationEffectiveOn = getGenerationEffectiveOn(newDate);
        if (generationEffectiveOn == null) {
            generationEffectiveOn = getFirstGeneration();
        }
        if (generationEffectiveOn == null) {
            newGeneration(newDate);
        }
    }

    @Override
    public void retainOnlyGeneration(GregorianCalendar oldDate, GregorianCalendar newDate) {
        IIpsObjectGeneration generationEffectiveOn = getGenerationEffectiveOn(oldDate);
        if (generationEffectiveOn == null) {
            generationEffectiveOn = getFirstGeneration();
        }
        for (IIpsObjectGeneration generation : getGenerations()) {
            if (!generation.equals(generationEffectiveOn)) {
                generation.delete();
            }
        }
        if (generationEffectiveOn == null) {
            generationEffectiveOn = newGeneration(newDate);
        } else {
            generationEffectiveOn.setValidFrom(newDate);
        }
    }
}
