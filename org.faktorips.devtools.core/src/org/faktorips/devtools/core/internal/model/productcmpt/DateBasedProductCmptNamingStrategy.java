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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A naming strategy for product components that uses dates (or part of them) as version ids. The
 * next version is determined by the current working date.
 * 
 * @author Jan Ortmann
 */
public class DateBasedProductCmptNamingStrategy extends AbstractProductCmptNamingStrategy {

    public final static String EXTENSION_ID = "org.faktorips.devtools.core.DateBasedProductCmptNamingStrategy"; //$NON-NLS-1$

    public final static String XML_TAG_NAME = "DateBasedProductCmptNamingStrategy"; //$NON-NLS-1$

    private String dateFormatPattern; // the pattern has to be kept in order to save the state to
    // xml
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExtensionId() {
        return EXTENSION_ID;
    }

    /**
     * {@inheritDoc}
     */
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
     * 
     * @see java.textSimpleDateFormat
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

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageList validateVersionId(String versionId) {
        MessageList list = new MessageList();
        if (versionId.length() < dateFormatPattern.length()) {
            list.add(newInvalidVersionIdMsg(versionId));
            return list;
        }
        if (postfixAllowed) {
            versionId = versionId.substring(0, dateFormatPattern.length());
        }
        try {
            dateFormat.parse(versionId);
            if (versionId.length() != dateFormatPattern.length()) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            list.add(newInvalidVersionIdMsg(versionId));
        }
        return list;
    }

    private Message newInvalidVersionIdMsg(String versionId) {
        String versionConcept = getIpsProject().getChangesInTimeNamingConventionForGeneratedCode()
                .getVersionConceptNameSingular();
        String text = NLS.bind(Messages.DateBasedProductCmptNamingStrategy_msgWrongFormat, versionConcept,
                dateFormatPattern);
        return Message.newError(MSGCODE_ILLEGAL_VERSION_ID, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextVersionId(IProductCmpt pc) {
        GregorianCalendar date = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        return dateFormat.format(date.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSubclassFromXml(Element el) {
        setDateFormatPattern(el.getAttribute("dateFormatPattern")); //$NON-NLS-1$
        postfixAllowed = Boolean.valueOf(el.getAttribute("postfixAllowed")).booleanValue(); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element toXmlSubclass(Document doc) {
        Element el = doc.createElement(XML_TAG_NAME);
        el.setAttribute("dateFormatPattern", dateFormatPattern); //$NON-NLS-1$
        el.setAttribute("postfixAllowed", "" + postfixAllowed); //$NON-NLS-1$ //$NON-NLS-2$
        return el;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUniqueRuntimeId(IIpsProject project, String productCmptName) throws CoreException {
        String id = project.getRuntimeIdPrefix() + productCmptName;
        String uniqueId = id;

        String[] parts = uniqueId.split(getVersionIdSeparator(), 2);

        int i = 1;
        while (project.findProductCmptByRuntimeId(uniqueId) != null) {
            uniqueId = parts[0] + i + getVersionIdSeparator() + parts[1];
            i++;
        }

        return uniqueId;
    }
}
