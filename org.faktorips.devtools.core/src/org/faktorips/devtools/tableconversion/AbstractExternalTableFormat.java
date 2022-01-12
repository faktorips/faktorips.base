/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * @author Thorsten Guenther
 */
public abstract class AbstractExternalTableFormat implements ITableFormat {

    /**
     * Table specific properties like text/field delimiter chars for CSV, ...
     */
    public Map<String, String> properties = new HashMap<>();

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
    private List<IValueConverter> converters = new ArrayList<>();

    /**
     * Returns the given double as string. If the double has no decimal places then the double will
     * be returned as integer string. Example: "1.0" will be returned as "1" but "1.2" will be
     * returned as "1.2".
     */
    public static String doubleToStringWithoutDecimalPlaces(Double externalDataValue) {
        /*
         * Workaround: If the external data is double without decimal places (n.0), e.g. if the
         * external data represents the numeric id of an enumeration value set, then it is important
         * to import the value as integer value ("1.0" will be "1"), otherwise the id couldn't be
         * mapped correctly to the enumeration identifier. The external data interprets a "1" always
         * as "1.0", even if the column or cell is formatted as text.
         */
        int intValue = externalDataValue.intValue();
        if (Double.valueOf("" + intValue).equals(externalDataValue)) { //$NON-NLS-1$
            return "" + intValue; //$NON-NLS-1$
        }
        return externalDataValue.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;

        if (this.name == null) {
            this.name = ""; //$NON-NLS-1$
        }
    }

    @Override
    public void setDefaultExtension(String extension) {
        defaultExtension = extension;

        if (defaultExtension == null) {
            defaultExtension = ""; //$NON-NLS-1$
        }
    }

    @Override
    public String getDefaultExtension() {
        return defaultExtension;
    }

    @Override
    public String getDefaultExtensionWildcard() {
        return "*" + defaultExtension; //$NON-NLS-1$
    }

    @Override
    public void addValueConverter(IValueConverter converter) {
        converters.add(converter);
        converter.setTableFormat(this);
    }

    @Override
    public String getIpsValue(Object externalValue, Datatype datatype, MessageList messageList) {
        MessageList msgList = new MessageList();
        String ipsValue = getConverter(datatype).getIpsValue(externalValue, msgList);
        if (msgList.containsErrorMsg()) {
            ipsValue = Messages.AbstractExternalTableFormat_Error + msgList.getFirstMessage(Message.ERROR).getText();
        }
        messageList.add(msgList);
        return ipsValue;
    }

    @Override
    public Object getExternalValue(String ipsValue, Datatype datatype, MessageList messageList) {
        return getConverter(datatype).getExternalDataValue(ipsValue, messageList);
    }

    // TODO RG: Cache converters in a map instead of for-loop.
    private IValueConverter getConverter(Datatype datatype) {
        for (IValueConverter converter : converters) {
            if (converter.getSupportedDatatype().equals(datatype)) {
                return converter;
            }
        }
        return defaultValueConverter;
    }

    @Override
    public String getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        if (propertyName != null) {
            properties.put(propertyName, propertyValue);
        }
    }

    protected Datatype[] getDatatypes(ITableStructure structure) {
        IColumn[] columns = structure.getColumns();
        Datatype[] datatypes = new Datatype[columns.length];
        for (int i = 0; i < columns.length; i++) {
            datatypes[i] = structure.getIpsProject().findDatatype(columns[i].getDatatype());
        }
        return datatypes;
    }

    protected Datatype[] getDatatypes(IEnumType structure) throws CoreRuntimeException {
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
     */
    private class DefaultValueConverter extends AbstractValueConverter {

        @Override
        public Datatype getSupportedDatatype() {
            return null;
        }

        @Override
        public String getIpsValue(Object externalDataValue, MessageList messageList) {
            if (externalDataValue instanceof Double) {
                return doubleToStringWithoutDecimalPlaces((Double)externalDataValue);
            }
            return externalDataValue.toString();
        }

        @Override
        public Object getExternalDataValue(String ipsValue, MessageList messageList) {
            return ipsValue;
        }

    }

}
