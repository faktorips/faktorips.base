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

import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;

/**
 * Base class for all table / enumeration import and export test cases. Contains factory methods to
 * create valid and invalid table contents.
 * 
 * @author Roman Grutza
 */
public abstract class AbstractTableTest extends AbstractIpsPluginTest {

    private ITableStructure structure;

    private final String[] datatypes = new String[] { Datatype.BOOLEAN.getQualifiedName(),
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
    protected ITableContents createValidTableContents(IIpsProject ipsProject) throws CoreException {
        ITableContents contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "ExportSource");
        ITableContentsGeneration exportSource = createExportSource(ipsProject, contents);

        IRow row1 = exportSource.newRow();
        row1.setValue(0, "true");
        row1.setValue(1, "12.3");
        row1.setValue(2, "" + Double.MAX_VALUE);
        row1.setValue(3, "2001-04-26");
        row1.setValue(4, "" + Integer.MAX_VALUE);
        row1.setValue(5, "" + Long.MAX_VALUE);
        row1.setValue(6, "10.23EUR");
        row1.setValue(7, "simple text");

        IRow row2 = exportSource.newRow();
        row2.setValue(0, "false");
        row2.setValue(1, "12.3");
        row2.setValue(2, "" + Double.MIN_VALUE);
        row2.setValue(3, "2001-04-26");
        row2.setValue(4, "" + Integer.MIN_VALUE);
        row2.setValue(5, "" + Long.MIN_VALUE);
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

        exportSource.getTimedIpsObject().getIpsSrcFile().save(true, null);

        return contents;
    }

    /**
     * Creates an invalid source for export.
     */
    protected ITableContents createInvalidTableContents(IIpsProject ipsProject) throws CoreException {
        ITableContents contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "ExportSource");
        ITableContentsGeneration exportSource = createExportSource(ipsProject, contents);

        IRow row1 = exportSource.newRow();
        row1.setValue(0, "INVALID"); // BOOLEAN
        row1.setValue(1, "INVALID"); // DECIMAL
        row1.setValue(2, "INVALID"); // DOUBLE
        row1.setValue(3, "INVALID"); // GREGORIAN_CALENDAR_DATE
        row1.setValue(4, "INVALID"); // INTEGER
        row1.setValue(5, "INVALID"); // LONG
        row1.setValue(6, "INVALID"); // MONEY
        row1.setValue(7, "invalid is impossible"); // STRING

        exportSource.getTimedIpsObject().getIpsSrcFile().save(true, null);

        return contents;
    }

    /**
     * Creates a test table structure based on the datatypes which are returned with a call to
     * {@link #getColumnDatatypes()}.
     * 
     * @param ipsProject The IPS project to create the table structure for.
     * @return The generated table structure.
     * @throws CoreException If the ipsProject is invalid or if this method is called more than once
     *             per test fixture.
     */
    public ITableStructure createTableStructure(IIpsProject ipsProject) throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
                "TestStructure");

        String[] datatypes = getColumnDatatypes();
        for (int i = 0; i < datatypes.length; i++) {
            IColumn col = structure.newColumn();
            col.setName("col" + i);
            col.setDatatype(datatypes[i]);
        }
        structure.getIpsSrcFile().save(true, null);

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
    public void createValidExternalTable(IIpsProject ipsProject, ITableFormat format, boolean exportColumnHeaderRow)
            throws CoreException {

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
    public void createInvalidExternalTable(IIpsProject ipsProject, ITableFormat format, boolean exportColumnHeaderRow)
            throws CoreException {

        ITableContents contents = createInvalidTableContents(ipsProject);
        format.executeTableExport(contents, new Path("table" + format.getDefaultExtension()), "NULL",
                exportColumnHeaderRow, new MessageList());
    }

    private ITableContentsGeneration createExportSource(IIpsProject ipsProject, ITableContents contents)
            throws CoreException {

        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);

        structure = createTableStructure(ipsProject);
        contents.setTableStructure(structure.getQualifiedName());

        return (ITableContentsGeneration)contents.newGeneration(new GregorianCalendar());
    }

    /**
     * Creates valid table contents.
     */
    protected IEnumType createValidEnumTypeWithValues(IIpsProject ipsProject) throws CoreException {
        IEnumType enumType = (IEnumType)newIpsObject(ipsProject, IpsObjectType.ENUM_TYPE, "EnumExportSource");
        enumType.setAbstract(false);
        enumType.setContainingValues(true);
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
        enumAttributeValues.get(0).setValue("SIMPLE_TEXT");
        enumAttributeValues.get(1).setValue("true");
        enumAttributeValues.get(2).setValue("12.3");
        enumAttributeValues.get(3).setValue("" + Double.MAX_VALUE);
        enumAttributeValues.get(4).setValue("2001-04-26");
        enumAttributeValues.get(5).setValue("" + Integer.MAX_VALUE);
        enumAttributeValues.get(6).setValue("" + Long.MAX_VALUE);
        enumAttributeValues.get(7).setValue("10.23EUR");
        enumAttributeValues.get(8).setValue("SimpleText");

        IEnumValue enumValueRow2 = enumType.newEnumValue();
        enumAttributeValues = enumValueRow2.getEnumAttributeValues();
        enumAttributeValues.get(0).setValue("_VALID_JAVA_IDENTIFIER");
        enumAttributeValues.get(1).setValue("false");
        enumAttributeValues.get(2).setValue("12.3");
        enumAttributeValues.get(3).setValue("" + Double.MIN_VALUE);
        enumAttributeValues.get(4).setValue("2001-04-26");
        enumAttributeValues.get(5).setValue("" + Integer.MIN_VALUE);
        enumAttributeValues.get(6).setValue("" + Long.MIN_VALUE);
        enumAttributeValues.get(7).setValue("1 EUR");
        enumAttributeValues.get(8).setValue("_ValidJavaIdentifier");

        IEnumValue enumValueRow3 = enumType.newEnumValue();
        enumAttributeValues = enumValueRow3.getEnumAttributeValues();
        enumAttributeValues.get(0).setValue("_UNIQUE_KEY");
        enumAttributeValues.get(1).setValue(null);
        enumAttributeValues.get(2).setValue(null);
        enumAttributeValues.get(3).setValue(null);
        enumAttributeValues.get(4).setValue(null);
        enumAttributeValues.get(5).setValue(null);
        enumAttributeValues.get(6).setValue(null);
        enumAttributeValues.get(7).setValue(null);
        enumAttributeValues.get(8).setValue("_UniqueKey");

        // enumType.getIpsSrcFile().save(true, null);

        return enumType;
    }

    /**
     * Creates an invalid enumeration for export.
     */
    protected IEnumType createInvalidEnumTypeWithValues(IIpsProject ipsProject) throws CoreException {
        IEnumType enumType = (IEnumType)newIpsObject(ipsProject, IpsObjectType.ENUM_TYPE, "EnumExportSource");
        enumType.setAbstract(false);
        enumType.setContainingValues(true);
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

        row1.getEnumAttributeValues().get(0).setValue("INVALID_BECAUSE_NOT_A_VALID_JAVA_IDENTIFIER_ $$% ");
        row1.getEnumAttributeValues().get(1).setValue("INVALID"); // BOOLEAN
        row1.getEnumAttributeValues().get(2).setValue("INVALID"); // DECIMAL
        row1.getEnumAttributeValues().get(3).setValue("INVALID"); // DOUBLE
        row1.getEnumAttributeValues().get(4).setValue("INVALID"); // GREGORIAN_CALENDAR_DATE
        row1.getEnumAttributeValues().get(5).setValue("INVALID"); // INTEGER
        row1.getEnumAttributeValues().get(6).setValue("INVALID"); // LONG
        row1.getEnumAttributeValues().get(7).setValue("INVALID"); // MONEY
        row1.getEnumAttributeValues().get(8).setValue("INVALID"); // STRING

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

}
