/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.AbstractProductCmptNamingStrategy;
import org.faktorips.devtools.model.internal.productcmpt.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A naming strategy for product components that uses dates (or part of them) as version ids. The
 * next version is determined by the current working date.
 * 
 * @author Jan Ortmann
 */
public class DateBasedProductCmptNamingStrategy extends AbstractProductCmptNamingStrategy {

    public static final String EXTENSION_ID = "org.faktorips.devtools.model.DateBasedProductCmptNamingStrategy"; //$NON-NLS-1$

    /**
     * XML Element name.
     */
    @SuppressWarnings("hiding")
    public static final String XML_TAG_NAME = "DateBasedProductCmptNamingStrategy"; //$NON-NLS-1$

    // the pattern has to be kept in order to save the state to
    private String dateFormatPattern;

    private DateFormat dateFormat;

    private boolean postfixAllowed;

    /**
     * Default constructor needed to use it as extension.
     */
    public DateBasedProductCmptNamingStrategy() {
        super();
    }

    public DateBasedProductCmptNamingStrategy(String separator, String dateFormatPattern, boolean allowPostfix) {
        super(separator);
        setDateFormatPattern(dateFormatPattern);
        this.postfixAllowed = allowPostfix;
    }

    @Override
    public String getExtensionId() {
        return EXTENSION_ID;
    }

    @Override
    public boolean supportsVersionId() {
        return true;
    }

    /**
     * Sets the date format pattern used for the version id.
     * 
     * @param pattern Format patter according to SimpleDateFormat.
     * @throws NullPointerException if pattern is null.
     * 
     * @see java.text.SimpleDateFormat
     */
    public void setDateFormatPattern(String pattern) {
        ArgumentCheck.notNull(pattern);
        dateFormatPattern = pattern;
        dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        dateFormat.setLenient(false);
    }

    /**
     * Returns the date format pattern used for the version id.
     * 
     * @return Format patter according to SimpleDateFormat.
     */
    public String getDateFormatPattern() {
        return dateFormatPattern;
    }

    /**
     * Sets if the version id allows a postfix after the date part or not. E.g. if set to
     * <code>true</code> 2006-10b is a valid version id (with the date pattern YYYY-mm).
     */
    public void setPostfixAllowed(boolean flag) {
        postfixAllowed = flag;
    }

    /**
     * Returns <code>true</code> if a postifx is allowed after the date part of the version id,
     * otherwise <code>false</code>.
     */
    public boolean isPostfixAllowed() {
        return postfixAllowed;
    }

    @Override
    public MessageList validateVersionId(String versionId) {
        MessageList list = new MessageList();
        String id = versionId;
        if (id.length() < dateFormatPattern.length()) {
            list.add(newInvalidVersionIdMsg());
            return list;
        }
        if (postfixAllowed) {
            id = id.substring(0, dateFormatPattern.length());
        }
        try {
            dateFormat.parse(id);
            if (id.length() != dateFormatPattern.length()) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            list.add(newInvalidVersionIdMsg());
        }
        return list;
    }

    private Message newInvalidVersionIdMsg() {
        String versionConcept = getIpsProject().getChangesInTimeNamingConventionForGeneratedCode()
                .getVersionConceptNameSingular();
        String text = MessageFormat.format(Messages.DateBasedProductCmptNamingStrategy_msgWrongFormat, versionConcept,
                dateFormatPattern);
        return Message.newError(MSGCODE_ILLEGAL_VERSION_ID, text);
    }

    /**
     * This implementation ignores the previous version and returns the validFrom in a format
     * defined by {@link DateBasedProductCmptNamingStrategy#setDateFormatPattern(String)}.
     * 
     * {@inheritDoc}
     */
    @Override
    public String getNextVersionId(IProductCmpt pc, GregorianCalendar validFrom) {
        return dateFormat.format(validFrom.getTime());
    }

    @Override
    public void initSubclassFromXml(Element el) {
        setDateFormatPattern(el.getAttribute("dateFormatPattern")); //$NON-NLS-1$
        postfixAllowed = Boolean.valueOf(el.getAttribute("postfixAllowed")).booleanValue(); //$NON-NLS-1$
    }

    @Override
    public Element toXmlSubclass(Document doc) {
        Element el = doc.createElement(XML_TAG_NAME);
        el.setAttribute("dateFormatPattern", dateFormatPattern); //$NON-NLS-1$
        el.setAttribute("postfixAllowed", "" + postfixAllowed); //$NON-NLS-1$ //$NON-NLS-2$
        return el;
    }

    @Override
    public String getUniqueRuntimeId(IIpsProject project, String productCmptName) throws CoreRuntimeException {
        String id = project.getRuntimeIdPrefix() + productCmptName;
        String uniqueId = id;

        String kindId = getKindId(uniqueId);
        String versionId = getVersionId(uniqueId);

        int i = 1;
        while (project.findProductCmptByRuntimeId(uniqueId) != null) {
            uniqueId = kindId + i + getVersionIdSeparator() + versionId;
            i++;
        }

        return uniqueId;
    }

}
