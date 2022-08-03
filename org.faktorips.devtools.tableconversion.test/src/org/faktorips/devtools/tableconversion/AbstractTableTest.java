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

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.MessageList;

/**
 * Base class for all table / enumeration import and export test cases. Contains factory methods to
 * create valid and invalid table contents.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractTableTest extends AbstractIpsPluginTest {

    private ITableStructure structure;

    private final String[] datatypes = { Datatype.BOOLEAN.getQualifiedName(),
            Datatype.DECIMAL.getQualifiedName(), Datatype.DOUBLE.getQualifiedName(),
            Datatype.GREGORIAN_CALENDAR.getQualifiedName(), Datatype.INTEGER.getQualifiedName(),
            Datatype.LONG.getQualifiedName(), Datatype.MONEY.getQualifiedName(), Datatype.STRING.getQualifiedName() };

    public ITableStructure getStructure() {
        return structure;
    }

    public String[] getColumnDatatypes() {
        return datatypes;
    }

    /**
     * Creates valid table contents.
     */
    protected ITableContents createValidTableContents(IIpsProject ipsProject) {
        ITableContents contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS,
                "ExportSource");
        ITableRows exportSource = createExportSource(ipsProject, contents);

        IRow row1 = exportSource.newRow();
        row1.setValue(0, "true");
        row1.setValue(1, "12.3");
        row1.setValue(2, "" + 1.79769313486231E308);
        row1.setValue(3, "2001-04-26");
        row1.setValue(4, "" + Integer.MAX_VALUE);
        row1.setValue(5, "" + new BigDecimal(922337203685477000.0));
        row1.setValue(6, "10.23EUR");
        row1.setValue(7, "simple text");

        IRow row2 = exportSource.newRow();
        row2.setValue(0, "false");
        row2.setValue(1, "12.3");
        row2.setValue(2, "" + Double.MIN_VALUE);
        row2.setValue(3, "2001-04-26");
        row2.setValue(4, "" + Integer.MIN_VALUE);
        row2.setValue(5, "" + new BigDecimal(-922337203685477000.0));
        row2.setValue(6, "1 EUR");
        row2.setValue(7, "�������{[]}");

        IRow row3 = exportSource.newRow();
        row3.setValue(0, null);
        row3.setValue(1, null);
        row3.setValue(2, null);
        row3.setValue(3, null);
        row3.setValue(4, null);
        row3.setValue(5, null);
        row3.setValue(6, null);
        row3.setValue(7, null);

        exportSource.getIpsObject().getIpsSrcFile().save(null);

        return contents;
    }

    /**
     * Creates an invalid source for export.
     */
    protected ITableContents createInvalidTableContents(IIpsProject ipsProject) {
        ITableContents contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS,
                "ExportSource");
        ITableRows exportSource = createExportSource(ipsProject, contents);

        IRow row1 = exportSource.newRow();
        row1.setValue(0, "INVALID"); // BOOLEAN
        row1.setValue(1, "INVALID"); // DECIMAL
        row1.setValue(2, "INVALID"); // DOUBLE
        row1.setValue(3, "INVALID"); // GREGORIAN_CALENDAR_DATE
        row1.setValue(4, "INVALID"); // INTEGER
        row1.setValue(5, "INVALID"); // LONG
        row1.setValue(6, "INVALID"); // MONEY
        row1.setValue(7, "invalid is impossible"); // STRING

        exportSource.getIpsObject().getIpsSrcFile().save(null);

        return contents;
    }

    /**
     * Creates a test table structure based on the datatypes which are returned with a call to
     * {@link #getColumnDatatypes()}.
     * 
     * @param ipsProject The IPS project to create the table structure for.
     * @return The generated table structure.
     * @throws IpsException If the ipsProject is invalid or if this method is called more than once
     *             per test fixture.
     */
    public ITableStructure createTableStructure(IIpsProject ipsProject) {
        ITableStructure structure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
                "TestStructure");

        String[] datatypes = getColumnDatatypes();
        for (int i = 0; i < datatypes.length; i++) {
            IColumn col = structure.newColumn();
            col.setName("col" + i);
            col.setDatatype(datatypes[i]);
        }
        structure.getIpsSrcFile().save(null);

        return structure;
    }

    /**
     * Creates a valid table in the given table format stored on the filesystem. The export
     * operation is used to create this file.
     * 
     * @param ipsProject The IPS project.
     * @param format The external table format used for export.
     * @param exportColumnHeaderRow Flag to indicate whether to create a header line in the
     *            generated file.
     */
    public void createValidExternalTable(IIpsProject ipsProject, ITableFormat format, boolean exportColumnHeaderRow) {

        ITableContents contents = createValidTableContents(ipsProject);
        format.executeTableExport(contents, new Path("table" + format.getDefaultExtension()), "NULL",
                exportColumnHeaderRow, new MessageList());
    }

    /**
     * Creates an invalid table in the given table format stored on the filesystem. The export
     * operation is used to create this file.
     * 
     * @param ipsProject The IPS project.
     * @param format The external table format used for export.
     * @param exportColumnHeaderRow Flag to indicate whether to create a header line in the
     *            generated file.
     */
    public void createInvalidExternalTable(IIpsProject ipsProject, ITableFormat format, boolean exportColumnHeaderRow) {

        ITableContents contents = createInvalidTableContents(ipsProject);
        format.executeTableExport(contents, new Path("table" + format.getDefaultExtension()), "NULL",
                exportColumnHeaderRow, new MessageList());
    }

    private ITableRows createExportSource(IIpsProject ipsProject, ITableContents contents) {

        structure = createTableStructure(ipsProject);
        contents.setTableStructure(structure.getQualifiedName());

        contents.newColumn(null, "col0");
        contents.newColumn(null, "col1");
        contents.newColumn(null, "col2");
        contents.newColumn(null, "col3");
        contents.newColumn(null, "col4");
        contents.newColumn(null, "col5");
        contents.newColumn(null, "col6");
        contents.newColumn(null, "col7");
        return contents.newTableRows();
    }

    /**
     * Creates valid table contents.
     */
    protected IEnumType createValidEnumTypeWithValues(IIpsProject ipsProject) {
        IEnumType enumType = (IEnumType)newIpsObject(ipsProject, IpsObjectType.ENUM_TYPE, "EnumExportSource");
        enumType.setAbstract(false);
        enumType.setExtensible(false);
        enumType.newEnumLiteralNameAttribute();

        // create attributes (structure)
        for (int i = 0; i < datatypes.length; i++) {
            IEnumAttribute enumAttribute = enumType.newEnumAttribute();
            enumAttribute.setName("id" + i);
            enumAttribute.setDatatype(datatypes[i]);
        }
        IEnumAttribute idAttribute = enumType.getEnumAttributes(false).get(enumType.getEnumAttributesCount(false) - 1);
        idAttribute.setUnique(true);
        idAttribute.setIdentifier(true); // satisfy validation rules
        idAttribute.setUsedAsNameInFaktorIpsUi(true); // satisfy validation rules

        // create values inside the enumeration type
        IEnumValue enumValueRow1 = enumType.newEnumValue();
        List<IEnumAttributeValue> enumAttributeValues = enumValueRow1.getEnumAttributeValues();
        enumAttributeValues.get(0).setValue(ValueFactory.createStringValue("SIMPLE_TEXT"));
        enumAttributeValues.get(1).setValue(ValueFactory.createStringValue("true"));
        enumAttributeValues.get(2).setValue(ValueFactory.createStringValue("12.3"));
        enumAttributeValues.get(3).setValue(ValueFactory.createStringValue("" + 1.79769313486231E308));
        enumAttributeValues.get(4).setValue(ValueFactory.createStringValue("2001-04-26"));
        enumAttributeValues.get(5).setValue(ValueFactory.createStringValue("" + Integer.MAX_VALUE));
        // As Double tend to cause floating Point problems, Big Decimal is used
        enumAttributeValues.get(6).setValue(ValueFactory.createStringValue("" + new BigDecimal(922337203685477000.0)));
        enumAttributeValues.get(7).setValue(ValueFactory.createStringValue("10.23EUR"));
        enumAttributeValues.get(8).setValue(ValueFactory.createStringValue("SimpleText"));

        IEnumValue enumValueRow2 = enumType.newEnumValue();
        enumAttributeValues = enumValueRow2.getEnumAttributeValues();
        enumAttributeValues.get(0).setValue(ValueFactory.createStringValue("_VALID_JAVA_IDENTIFIER"));
        enumAttributeValues.get(1).setValue(ValueFactory.createStringValue("false"));
        enumAttributeValues.get(2).setValue(ValueFactory.createStringValue("12.3"));
        enumAttributeValues.get(3).setValue(ValueFactory.createStringValue("" + Double.MIN_VALUE));
        enumAttributeValues.get(4).setValue(ValueFactory.createStringValue("2001-04-26"));
        enumAttributeValues.get(5).setValue(ValueFactory.createStringValue("" + Integer.MIN_VALUE));
        // As Double tend to cause floating Point problems, Big Decimal is used
        enumAttributeValues.get(6).setValue(ValueFactory.createStringValue("" + new BigDecimal(-922337203685477000.0)));
        enumAttributeValues.get(7).setValue(ValueFactory.createStringValue("1 EUR"));
        enumAttributeValues.get(8).setValue(ValueFactory.createStringValue("_ValidJavaIdentifier"));

        IEnumValue enumValueRow3 = enumType.newEnumValue();
        enumAttributeValues = enumValueRow3.getEnumAttributeValues();
        enumAttributeValues.get(0).setValue(ValueFactory.createStringValue("_UNIQUE_KEY"));
        enumAttributeValues.get(1).setValue(ValueFactory.createStringValue(null));
        enumAttributeValues.get(2).setValue(ValueFactory.createStringValue(null));
        enumAttributeValues.get(3).setValue(ValueFactory.createStringValue(null));
        enumAttributeValues.get(4).setValue(ValueFactory.createStringValue(null));
        enumAttributeValues.get(5).setValue(ValueFactory.createStringValue(null));
        enumAttributeValues.get(6).setValue(ValueFactory.createStringValue(null));
        enumAttributeValues.get(7).setValue(ValueFactory.createStringValue(null));
        enumAttributeValues.get(8).setValue(ValueFactory.createStringValue("_UniqueKey"));

        // enumType.getIpsSrcFile().save(true, null);

        return enumType;
    }

    /**
     * Creates an invalid enumeration for export.
     */
    protected IEnumType createInvalidEnumTypeWithValues(IIpsProject ipsProject) {
        IEnumType enumType = (IEnumType)newIpsObject(ipsProject, IpsObjectType.ENUM_TYPE, "EnumExportSource");
        enumType.setAbstract(false);
        enumType.setExtensible(false);
        enumType.newEnumLiteralNameAttribute();

        // create attributes (structure)
        for (int i = 0; i < datatypes.length; i++) {
            IEnumAttribute enumAttribute = enumType.newEnumAttribute();
            enumAttribute.setName("id" + i);
            enumAttribute.setDatatype(datatypes[i]);
        }
        IEnumAttribute idAttribute = enumType.getEnumAttributes(false).get(enumType.getEnumAttributesCount(false) - 1);
        idAttribute.setUnique(true); // unique id must be set if literal name set
        idAttribute.setIdentifier(true); // satisfy validation rules
        idAttribute.setUsedAsNameInFaktorIpsUi(true); // satisfy validation rules

        IEnumValue row1 = enumType.newEnumValue();

        row1.getEnumAttributeValues().get(0)
                .setValue(ValueFactory.createStringValue("INVALID_BECAUSE_NOT_A_VALID_JAVA_IDENTIFIER_ $$% "));
        row1.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("INVALID")); // BOOLEAN
        row1.getEnumAttributeValues().get(2).setValue(ValueFactory.createStringValue("INVALID")); // DECIMAL
        row1.getEnumAttributeValues().get(3).setValue(ValueFactory.createStringValue("INVALID")); // DOUBLE
        row1.getEnumAttributeValues().get(4).setValue(ValueFactory.createStringValue("INVALID")); // GREGORIAN_CALENDAR_DATE
        row1.getEnumAttributeValues().get(5).setValue(ValueFactory.createStringValue("INVALID")); // INTEGER
        row1.getEnumAttributeValues().get(6).setValue(ValueFactory.createStringValue("INVALID")); // LONG
        row1.getEnumAttributeValues().get(7).setValue(ValueFactory.createStringValue("INVALID")); // MONEY
        row1.getEnumAttributeValues().get(8).setValue(ValueFactory.createStringValue("INVALID")); // STRING

        // enumType.getIpsSrcFile().save(true, null);

        return enumType;
    }

    /**
     * Creates a valid enumeration in the given table format stored on the file system. The export
     * operation is used to create this file.
     * 
     * @param ipsProject The IPS project.
     * @param format The external table format used for export.
     * @param exportColumnHeaderRow Flag to indicate whether to create a header line in the
     *            generated file.
     */
    public void createValidExternalEnumType(IIpsProject ipsProject, ITableFormat format, boolean exportColumnHeaderRow)
            throws Exception {

        IEnumType enumType = createValidEnumTypeWithValues(ipsProject);

        format.executeEnumExport(enumType, new Path("enum" + format.getDefaultExtension()), "NULL",
                exportColumnHeaderRow, new MessageList());
    }

    protected void assertRow(String[] stringRow, IRow row) {
        for (int i = 0; i < stringRow.length; i++) {
            assertEquals("column " + i + " mismatched", stringRow[i], row.getValue(i));
        }
    }

}
