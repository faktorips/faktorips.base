/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A naming strategy for product components that uses dates as version ids.
 * The next version is determined by the current working date.
 * 
 * @author Jan Ortmann
 */
public class DateBasedProductCmptNamingStrategy extends
		AbstractProductCmptNamingStrategy {

	public final static String EXTENSION_ID = "org.faktorips.devtools.core.DateBasedProductCmptNamingStrategy";
	
	public final static String XML_TAG_NAME = "DateBasedProductCmptNamingStrategy";
	
	private String dateFormatPattern; // the pattern has to be kept in order to save the state to xml
    private DateFormat dateFormat;
    private boolean postfixAllowed;
	
    /**
     * Default constructor needed to use it as extension.
     */
    public DateBasedProductCmptNamingStrategy() {
    	super();
    }
    
	public DateBasedProductCmptNamingStrategy(
			String separator,
			String dateFormatPattern,
			boolean allowPostfix) {

		super(separator);
		setDateFormatPattern(dateFormatPattern);
		this.postfixAllowed = allowPostfix;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getExtensionId() {
		return EXTENSION_ID;
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
	 * Sets if the version id allows a postfix after the date part or not.
	 * E.g. if set to <code>true</code> 2006-10b is a valid version id
	 * (with the date pattern YYYY-mm).
	 */
	public void setPostfixAllowed(boolean flag) {
		postfixAllowed = flag;
	}
	
	/**
	 * Returns <code>true</code> if a postifx is allowed after the date part of
	 * the version id, otherwise <code>false</code>.
	 */
	public boolean isPostfixAllowed() {
		return postfixAllowed;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MessageList validateVersionId(String versionId) {
		MessageList list = new MessageList();
		if (versionId.length() < dateFormatPattern.length()) {
			list.add(Message.newError(MSGCODE_ILLEGAL_VERSION_ID, "The version identification has the wrong format! Exptected format: " + dateFormatPattern));			
			return list;
		}
		if (postfixAllowed) {
			versionId = versionId.substring(0, dateFormatPattern.length());
		}
		try {
			dateFormat.parse(versionId);
			if (versionId.length()!=dateFormatPattern.length()) {
				throw new RuntimeException();
			}
		} catch (Exception e) {
			list.add(Message.newError(MSGCODE_ILLEGAL_VERSION_ID, "The version identification has the wrong format! Exptected format: " + dateFormatPattern));
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNextVersionId(IProductCmpt pc) {
		GregorianCalendar date = IpsPreferences.getWorkingDate();
		return dateFormat.format(date.getTime());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void initSubclassFromXml(Element el) {
		setDateFormatPattern(el.getAttribute("dateFormatPattern"));
		postfixAllowed = Boolean.valueOf(el.getAttribute("postfixAllowed")).booleanValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public Element toXmlSubclass(Document doc) {
		Element el = doc.createElement(XML_TAG_NAME);
		el.setAttribute("dateFormatPattern", dateFormatPattern);
		el.setAttribute("postfixAllowed", "" + postfixAllowed);
		return el;
	}
	
}
