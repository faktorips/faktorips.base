/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.XmlParseException;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class IpsObjectGeneration extends IpsObjectPart implements IIpsObjectGeneration {

    private GregorianCalendar validFrom;

    public IpsObjectGeneration(ITimedIpsObject ipsObject, String id) {
        super(ipsObject, id);
    }

    protected IpsObjectGeneration() {
        // Provides default constructor to sub classes.
    }

    @Override
    public ITimedIpsObject getTimedIpsObject() {
        return (ITimedIpsObject)getIpsObject();
    }

    @Override
    public int getGenerationNo() {
        IIpsObjectGeneration[] generations = ((ITimedIpsObject)getIpsObject()).getGenerationsOrderedByValidDate();
        for (int i = 0; i < generations.length; i++) {
            if (generations[i] == this) {
                return i + 1;
            }
        }
        throw new RuntimeException("Coulnd't find the generation " + this + " in it's parent " + getIpsObject() + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Returns the valid from formatted with the default <code>DataFormat</code> instance.
     */
    @Override
    public String getName() {
        if (validFrom == null) {
            return ""; //$NON-NLS-1$
        }
        DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
        return format.format(validFrom.getTime());
    }

    @Override
    public GregorianCalendar getValidFrom() {
        return validFrom;
    }

    @Override
    public void setValidFrom(GregorianCalendar validFrom) {
        GregorianCalendar oldValue = this.validFrom;
        setValidFromInternal(validFrom);
        valueChanged(oldValue, validFrom);
    }

    /**
     * Sets the valid from without triggering a content changed event.
     */
    protected void setValidFromInternal(GregorianCalendar validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public Boolean isValidFromInPast() {
        if (validFrom == null) {
            return null;
        }
        GregorianCalendar now = new GregorianCalendar();
        /*
         * because now contains the current time incliding hour, minute and second, but validFrom
         * does not, we have to set the fields for hour, minute, second and millisecond to 0 to get
         * an editable generation which is valid from today. The field AM_PM has to be set to AM,
         * too.
         */
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.AM_PM, Calendar.AM);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        return Boolean.valueOf(validFrom.before(now));
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        try {
            validFrom = XmlUtil.parseGregorianCalendar(element.getAttribute(PROPERTY_VALID_FROM));
        } catch (XmlParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_VALID_FROM, XmlUtil.gregorianCalendarToXmlDateString(validFrom));
    }

    @Override
    public void initFromGeneration(IIpsObjectGeneration source, GregorianCalendar validFrom) {
        String id = getId();
        Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
        this.initFromXml(source.toXml(doc), id);
        this.validFrom = validFrom;
        // note: do not call event triggering methods here (e.g. partWasEdited, objectHasChanged)
    }

    @Override
    public GregorianCalendar getValidTo() {
        IIpsObjectGeneration[] generations = getTimedIpsObject().getGenerationsOrderedByValidDate();

        GregorianCalendar parentValidTo = getTimedIpsObject().getValidTo();

        GregorianCalendar validTo = null;

        for (int i = 0; i < generations.length && validTo == null; i++) {
            if (generations[i].getGenerationNo() == getGenerationNo() + 1) {
                GregorianCalendar date = generations[i].getValidFrom();
                if (date != null) {
                    // make a copy to not modify the valid-from date of the generation
                    date = (GregorianCalendar)date.clone();

                    /*
                     * reduce the valid-from date of the follow-up generation by one millisecond to
                     * avoid that two generations are valid at the same time. This generation is not
                     * valid at the time the follow-up generation is valid from.
                     */
                    date.setTimeInMillis(date.getTimeInMillis() - 1);
                }
                validTo = date;
            }
        }

        if (parentValidTo == null) {
            // no restriction given by parent, so we can return the default value
            return validTo;
        }

        if (validTo == null || validTo.after(parentValidTo)) {
            // a restriction given by the parent exists, so we have to apply
            return parentValidTo;
        } else {
            return validTo;
        }

    }

    @Override
    public IIpsObjectGeneration getNextByValidDate() {
        IIpsObjectGeneration[] generations = getTimedIpsObject().getGenerationsOrderedByValidDate();
        int genIndex = getGenerationNo();

        if (generations.length > genIndex) {
            return generations[genIndex];
        }
        return null;
    }

    @Override
    public IIpsObjectGeneration getPreviousByValidDate() {
        IIpsObjectGeneration[] generations = getTimedIpsObject().getGenerationsOrderedByValidDate();
        int genIndex = getGenerationNo() - 2;

        if (genIndex >= 0) {
            return generations[genIndex];
        }
        return null;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        GregorianCalendar parentValidTo = getTimedIpsObject().getValidTo();

        if (parentValidTo != null && getValidFrom() != null && getValidFrom().after(parentValidTo)) {
            IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
            String[] params = new String[3];
            params[0] = prefs.getChangesOverTimeNamingConvention().getGenerationConceptNameSingular();
            DateFormat format = prefs.getDateFormat();
            params[1] = format.format(getValidFrom().getTime());
            params[2] = format.format(parentValidTo.getTime());
            String msg = NLS.bind(Messages.IpsObjectGeneration_msgInvalidFromDate, params);

            list.add(new Message(MSGCODE_INVALID_VALID_FROM, msg, Message.ERROR, this, PROPERTY_VALID_FROM));
        }
    }

}
