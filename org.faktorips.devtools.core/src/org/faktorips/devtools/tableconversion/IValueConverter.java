/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;

/**
 * Interface for converters able to transform a string representation of a value to an instance of a
 * specific type. For example the string "29.06.2006" which represents a date in German format can
 * be transformed to <tt>java.util.Date</tt>, but it can also be transformed to another type which
 * is defined by any other vendor.
 * <p>
 * A converter should always return the same type in
 * <tt>getExternalDataValue(String, MessageList)</tt>, but has to be able to deal with different
 * types in <tt>getIpsValue(Object, MessageList)</tt>. So no simple cast to a specific type should
 * be done in this method.
 * 
 * @author Thorsten Guenther
 */
public interface IValueConverter {

    public Datatype getSupportedDatatype();

    /**
     * Returns the value of the given object as string-representation. If the given value can not be
     * converted into an ipsValue, <code>toString()</code> is called on the external value and the
     * result is returned. This is useful for the user because as less as possible information is
     * lost.
     * 
     * @param externalDataValue The value to convert to a string. This value is NOT ensured to be of
     *            the same instance as returned by this class getExternalDataValue() and also can
     *            vary from call to call.
     * @param messageList A list for messages to add if anything happens that should be reported to
     *            the user. If this list does not contains an error-message before you call this
     *            method and do contain an error-message after the call, the conversion failed.
     */
    public String getIpsValue(Object externalDataValue, MessageList messageList);

    /**
     * Returns an instance of the class this is an converter for representing the given string or
     * the string itself if it cannot be converted. This is useful for the user because as less as
     * possible information is lost.
     * 
     * @param ipsValue The string-representation of a value.
     * @param messageList A list for messages to add if anything happens that should be reported to
     *            the user. If this list does not contains an error-message before you call this
     *            method and do contain an error-message after the call, the conversion failed.
     */
    public Object getExternalDataValue(String ipsValue, MessageList messageList);

    /**
     * Sets the table format this converter belongs to. A converter can be assigned to a table
     * format only once. This method should only be is called by
     * {@link ITableFormat#addValueConverter(IValueConverter)}. It is published API, as new
     * ValueConverts must implement the method to function correctly.
     * 
     * @throws NullPointerException if format is <code>null</code>.
     * @throws RuntimeException if the format has already been set and the new format is a different
     *             one.
     */
    public void setTableFormat(ITableFormat format);

    /**
     * Returns the table format this converter belongs to.
     */
    public ITableFormat getTableFormat();

}
