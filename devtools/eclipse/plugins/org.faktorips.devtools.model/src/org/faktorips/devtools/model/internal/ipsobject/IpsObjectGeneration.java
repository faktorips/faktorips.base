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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.XmlParseException;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
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
     * Returns the valid from formatted with the default {@link DateFormat} instance.
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
    public String getCaption(Locale locale) {
        return IIpsModelExtensions.get().getModelPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular();
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
    public GregorianCalendar getValidTo() {
        GregorianCalendar parentValidTo = getTimedIpsObject().getValidTo();

        IIpsObjectGeneration nextGeneration = getNextByValidDate();
        if (nextGeneration == null) {
            return parentValidTo;
        }

        GregorianCalendar nextValidFrom = nextGeneration.getValidFrom();
        if (nextValidFrom == null) {
            return parentValidTo;
        }
        GregorianCalendar validTo = (GregorianCalendar)GregorianCalendar.getInstance(nextValidFrom.getTimeZone());
        /*
         * reduce the valid-from date of the follow-up generation by one millisecond to avoid that
         * two generations are valid at the same time. This generation is not valid at the time the
         * follow-up generation is valid from.
         */
        validTo.setTimeInMillis(nextValidFrom.getTimeInMillis() - 1);

        if (parentValidTo == null) {
            // no restriction given by parent, so we can return the default value
            return validTo;
        }

        if (validTo.after(parentValidTo)) {
            // a restriction given by the parent exists, so we have to apply
            return parentValidTo;
        } else {
            return validTo;
        }

    }

    @Override
    public IIpsObjectGeneration getNextByValidDate() {
        IIpsObjectGeneration[] generations = getTimedIpsObject().getGenerationsOrderedByValidDate();

        for (int i = 0; i < generations.length - 1; i++) {
            if (generations[i] == this) {
                return generations[i + 1];
            }
        }
        return null;
    }

    @Override
    public IIpsObjectGeneration getPreviousByValidDate() {
        IIpsObjectGeneration[] generations = getTimedIpsObject().getGenerationsOrderedByValidDate();

        for (int i = generations.length - 1; i > 0; i--) {
            if (generations[i] == this) {
                return generations[i - 1];
            }
        }
        return null;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        validateValidFromFormat(list, this);
        if (list.getMessageByCode(MSGCODE_INVALID_FORMAT_VALID_FROM) == null) {
            validateValidFrom(list);
        }
    }

    /**
     * Validates whether the valid from of this generation is given in correct date format.
     *
     * @param list the message list error messages are added to
     * @param invalidObject the object that should be rendered invalid in case of an error
     */
    public void validateValidFromFormat(MessageList list, Object invalidObject) {
        if (getValidFrom() == null) {
            list.add(Message.newError(MSGCODE_INVALID_FORMAT_VALID_FROM,
                    Messages.IpsObjectGeneration_msgInvalidFormatFromDate + TimedIpsObject.getDefaultDateFormat(),
                    invalidObject,
                    PROPERTY_VALID_FROM));
        }
    }

    private void validateValidFrom(MessageList list) {
        GregorianCalendar parentValidTo = getTimedIpsObject().getValidTo();
        if (parentValidTo != null && getValidFrom().after(parentValidTo)) {
            Object[] params = new Object[3];
            params[0] = IIpsModelExtensions.get().getModelPreferences().getChangesOverTimeNamingConvention()
                    .getGenerationConceptNameSingular();
            DateFormat format = IIpsModelExtensions.get().getModelPreferences().getDateFormat();
            params[1] = format.format(getValidFrom().getTime());
            params[2] = format.format(parentValidTo.getTime());
            String msg = MessageFormat.format(Messages.IpsObjectGeneration_msgInvalidFromDate, params);

            list.add(Message.newError(MSGCODE_INVALID_VALID_FROM, msg, this, PROPERTY_VALID_FROM));
        }

        IIpsObjectGeneration duplicateGeneration = getTimedIpsObject().getGenerationByEffectiveDate(getValidFrom());
        if (duplicateGeneration != this) {
            String msg = MessageFormat.format(Messages.IpsObjectGeneration_msgDuplicateGeneration,
                    IIpsModelExtensions.get()
                            .getModelPreferences().getChangesOverTimeNamingConvention()
                            .getGenerationConceptNameSingular());
            list.add(Message.newError(MSGCODE_INVALID_VALID_FROM_DUPLICATE_GENERATION, msg, this, PROPERTY_VALID_FROM));
        }
    }
}
