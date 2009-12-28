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

package org.faktorips.devtools.tableconversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * @author Thorsten Guenther
 */
public abstract class AbstractExternalTableFormat implements ITableFormat {

    /**
     * The human readable name of this external table format.
     */
    private String name;

    /**
     * The extension of the files this external table format usually uses. Can be the empty string,
     * but not <code>null</code>.
     */
    private String defaultExtension;

    /**
     * Converter to be used if no other matches
     */
    private final IValueConverter defaultValueConverter = new DefaultValueConverter();

    /**
     * List of all converters this external table format is configured with.
     */
    private List<IValueConverter> converters = new ArrayList<IValueConverter>();

    /**
     * Table specific properties like text/field delimiter chars for CSV, ...
     */
    public Map<String, String> properties = new HashMap<String, String>();

    /**
     * Returns the given Double as string. If the Double has no decimal places then the Double will
     * be returned as intg string. Example: "1.0" will be returned as "1" but "1.2" will be returned
     * as "1.2"
     */
    public static String doubleToStringWithoutDecimalPlaces(Double externalDataValue) {
        // Workaround: if the external data is double without decimal places (n.0),
        // e.g. if the external data represents the numeric id of an enum value set,
        // then it is important to import the value as int value ("1.0" will be "1"),
        // otherwise the id couldn't be mapped correctly to the enum identifier.
        // The extenal data interprets a "1" always as "1.0",
        // even if the column or cell is formatted as text
        int intValue = externalDataValue.intValue();
        if (Double.valueOf("" + intValue).equals(externalDataValue)) { //$NON-NLS-1$
            return "" + intValue; //$NON-NLS-1$
        }
        return externalDataValue.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        this.name = name;

        if (this.name == null) {
            this.name = ""; //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultExtension(String extension) {
        defaultExtension = extension;

        if (defaultExtension == null) {
            defaultExtension = ""; //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultExtension() {
        return defaultExtension;
    }

    /**
     * {@inheritDoc}
     */
    public void addValueConverter(IValueConverter converter) {
        converters.add(converter);
    }

    /**
     * {@inheritDoc}
     */
    public String getIpsValue(Object externalValue, Datatype datatype, MessageList messageList) {
        MessageList msgList = new MessageList();
        String ipsValue = getConverter(datatype).getIpsValue(externalValue, msgList);
        if (msgList.containsErrorMsg()) {
            ipsValue = Messages.AbstractExternalTableFormat_Error + msgList.getFirstMessage(Message.ERROR).getText();
        }
        messageList.add(msgList);
        return ipsValue;
    }

    /**
     * {@inheritDoc}
     */
    public Object getExternalValue(String ipsValue, Datatype datatype, MessageList messageList) {
        return getConverter(datatype).getExternalDataValue(ipsValue, messageList);
    }

    // TODO rg: cache converters in a map instead of for-loop
    // add testcase for GregorianCalendarAsDateDatatype
    private IValueConverter getConverter(Datatype datatype) {
        for (IValueConverter converter : converters) {
            IValueConverter valueConverter = converter;

            if (valueConverter.getSupportedDatatype().equals(datatype)) {
                return valueConverter;
            }
            // Manually handle GregorianCalendarAsDateDatatype
            if (datatype instanceof GregorianCalendarAsDateDatatype) {
                return getConverter(Datatype.GREGORIAN_CALENDAR_DATE);
            }
        }
        return defaultValueConverter;
    }

    /**
     * {@inheritDoc}
     */
    public String getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    public void setProperty(String propertyName, String propertyValue) {
        if (propertyName != null) {
            properties.put(propertyName, propertyValue);
        }
    }

    protected Datatype[] getDatatypes(ITableStructure structure) throws CoreException {
        IColumn[] columns = structure.getColumns();
        Datatype[] datatypes = new Datatype[columns.length];
        for (int i = 0; i < columns.length; i++) {
            datatypes[i] = structure.getIpsProject().findDatatype(columns[i].getDatatype());
        }
        return datatypes;
    }

    protected Datatype[] getDatatypes(IEnumType structure) throws CoreException {
        List<IEnumAttribute> enumAttributes = structure.getEnumAttributesIncludeSupertypeCopies(true);
        Datatype[] datatypes = new Datatype[enumAttributes.size()];
        for (int i = 0; i < datatypes.length; i++) {
            IEnumAttribute enumAttribute = enumAttributes.get(i);
            datatypes[i] = enumAttribute.findDatatype(enumAttribute.getIpsProject());
        }
        return datatypes;
    }

    /**
     * Implementation of IValueConverter to be used if no other converter was found.
     * 
     * @author Thorsten Guenther
     */
    private class DefaultValueConverter implements IValueConverter {

        private ITableFormat tableFormat;

        /**
         * {@inheritDoc}
         */
        public Class<Object> getSupportedClass() {
            return Object.class;
        }

        /**
         * {@inheritDoc}
         */
        public Datatype getSupportedDatatype() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getIpsValue(Object externalDataValue, MessageList messageList) {
            if (externalDataValue instanceof Double) {
                return doubleToStringWithoutDecimalPlaces((Double)externalDataValue);
            }
            return externalDataValue.toString();
        }

        /**
         * {@inheritDoc}
         */
        public Object getExternalDataValue(String ipsValue, MessageList messageList) {
            return ipsValue;
        }

        public void setTableFormat(ITableFormat tableFormat) {
            this.tableFormat = tableFormat;
        }

        public ITableFormat getTableFormat() {
            return tableFormat;
        }

    }

}
