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

package org.faktorips.devtools.stdbuilder.enums;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.BooleanHelper;
import org.faktorips.codegen.dthelpers.IntegerHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.MessageList;

/**
 * A builder for enumeration classes. Generates enumeration classes according to the type save enum
 * pattern. The enumeration type is defined by an ips table structure and the enumeration values by
 * an ips table contents. Therefore this builder is a builder for the ips type tablestructure and
 * tablecontents.
 * 
 * @author Peter Erzberger
 */
// TODO AW: Is this builder still needed as we have the new enum concept now?
public class EnumClassesBuilder extends DefaultJavaSourceFileBuilder {

    public final static String PACKAGE_STRUCTURE_KIND_ID = "EnumClassesBuilder.enums.stdbuilder.devtools.faktorips.org"; //$NON-NLS-1$

    private final EnumTypeInterfaceBuilder enumTypeInterfaceBuilder;

    /**
     * See super class constructor.
     */
    public EnumClassesBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            EnumTypeInterfaceBuilder enumTypeInterfaceBuilder) {
        super(builderSet, kindId, new LocalizedStringsSet(EnumClassesBuilder.class));
        ArgumentCheck.notNull(enumTypeInterfaceBuilder);
        this.enumTypeInterfaceBuilder = enumTypeInterfaceBuilder;
        setMergeEnabled(true);
    }

    @Override
    protected String generate() throws CoreException {
        ITableStructure tableStructure = getTableContents().findTableStructure(getIpsProject());
        if (tableStructure == null || !tableStructure.isModelEnumType()) {
            return null;
        }
        return super.generate();
    }

    @Override
    protected void generateCodeForJavatype() throws CoreException {
        ITableStructure tableStructure = getTableContents().findTableStructure(getIpsProject());
        if (tableStructure == null) {
            return;
        }

        TypeSection mainSection = getMainTypeSection();
        mainSection.setUnqualifiedName(getTableContents().getName());
        if (isUseEnums()) {
            mainSection.setClassModifier(Modifier.PUBLIC);
            mainSection.setEnum(true);
            mainSection.setExtendedInterfaces(new String[] { enumTypeInterfaceBuilder
                    .getQualifiedClassName(tableStructure) });
        } else {
            mainSection.setClassModifier(Modifier.PUBLIC | Modifier.FINAL);
            mainSection.setClass(true);
            mainSection.setExtendedInterfaces(new String[] { Serializable.class.getName(),
                    enumTypeInterfaceBuilder.getQualifiedClassName(tableStructure) });
        }

        JavaCodeFragmentBuilder memberBuilder = getMainTypeSection().getMemberVarBuilder();
        JavaCodeFragmentBuilder methodBuilder = getMainTypeSection().getMethodBuilder();

        if (isUseEnums()) {
            appendLocalizedJavaDoc("ENUM_DESCRIPTION", getTableContents().getName(), getIpsObject().getDescription(),
                    getTableContents(), getMainTypeSection().getJavaDocForTypeBuilder());
        } else {
            appendLocalizedJavaDoc("CLASS_DESCRIPTION", getTableContents().getName(), getIpsObject().getDescription(),
                    getTableContents(), getMainTypeSection().getJavaDocForTypeBuilder());
        }

        generateCodeForColumns(memberBuilder, methodBuilder, tableStructure);
        generateConstantForSerialVersionNumber(getMainTypeSection().getConstantBuilder());

        EnumValueAttributesInfo info = createEnumValueAttributesInfo(getTableContents());
        if (!info.isValid) {
            return;
        }
        generateConstructor(getMainTypeSection().getConstructorBuilder(), tableStructure, info.idKeyItem,
                info.nameKeyItem);
        if (isUseEnums()) {
            generateEnumInitialization(getMainTypeSection().getEnumDefinitionBuilder(), tableStructure);
        } else {
            generateConstantsForEnumValues(getMainTypeSection().getConstantBuilder(), tableStructure);
            generateMethodGetAllEnumValues(methodBuilder);
            generateMethodReadResolve(methodBuilder, info.idKeyItem);
        }
        generateMethodGetEnumValue(methodBuilder, info.idKeyItem, info.idDatatype);
        generateMethodIsEnumValue(methodBuilder, info.idKeyItem, info.idDatatype);
        generateMethodToString(methodBuilder, info.nameKeyItem, info.idKeyItem);
    }

    private EnumValueAttributesInfo createEnumValueAttributesInfo(ITableContents tableContents) throws CoreException {
        ITableStructure tableStructure = tableContents.findTableStructure(getIpsProject());
        if (tableStructure == null) {
            return EnumValueAttributesInfo.INVALID_INFO;
        }
        IUniqueKey[] uniqueKeys = tableStructure.getUniqueKeys();
        // first key is the id of the enum
        // second key is the name of the enum
        if (uniqueKeys.length != 2) {
            return EnumValueAttributesInfo.INVALID_INFO;
        }

        for (int i = 0; i < uniqueKeys.length; i++) {
            if (!uniqueKeys[i].validate(getIpsProject()).isEmpty()) {
                return EnumValueAttributesInfo.INVALID_INFO;
            }
        }
        IUniqueKey idKey = uniqueKeys[0];
        IUniqueKey nameKey = uniqueKeys[1];

        IKeyItem[] idKeyItems = idKey.getKeyItems();
        if (idKeyItems.length != 1) {
            return EnumValueAttributesInfo.INVALID_INFO;
        }
        IKeyItem[] nameKeyItems = nameKey.getKeyItems();
        if (nameKeyItems.length != 1) {
            return EnumValueAttributesInfo.INVALID_INFO;
        }
        IIpsProject ipsProject = tableContents.getIpsProject();
        Datatype idKeyItemDatatype = ipsProject.findDatatype(idKeyItems[0].getDatatype());
        return new EnumValueAttributesInfo(idKeyItems[0], nameKeyItems[0], idKeyItemDatatype);
    }

    public final boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.TABLE_CONTENTS)) {
            return true;
        }
        return false;
    }

    private ITableContents getTableContents() {
        return (ITableContents)getIpsObject();
    }

    private void generateCodeForColumns(JavaCodeFragmentBuilder memberBuilder,
            JavaCodeFragmentBuilder methodBuilder,
            ITableStructure structure) throws CoreException {
        IColumn[] columns = structure.getColumns();
        for (int i = 0; i < columns.length; i++) {
            Datatype datatype = columns[i].findValueDatatype(getIpsProject());
            if (datatype == null) {
                continue;
            }
            generateField(memberBuilder, columns[i], datatype);
            generateMethodGetField(methodBuilder, columns[i], datatype);
        }
    }

    private String getFieldName(IColumn column) {
        return column.getName();
    }

    /*
     * Code sample: <pre> [Javadoc] private Integer id; </pre>
     */
    private void generateField(JavaCodeFragmentBuilder memberBuilder, IColumn column, Datatype datatype)
            throws CoreException {
        memberBuilder.javaDoc(null, ANNOTATION_GENERATED);
        memberBuilder.varDeclaration(Modifier.PRIVATE, datatype.getJavaClassName(), getFieldName(column));
    }

    /*
     * Code sample: <pre> [Javadoc] public Integer getId() { return id; } </pre>
     */
    private void generateMethodGetField(JavaCodeFragmentBuilder methodBuilder, IColumn column, Datatype datatype) {
        String methodName = getJavaNamingConvention().getGetterMethodName(column.getName(), datatype);
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return ");
        methodBody.append(getFieldName(column));
        methodBody.append(';');
        appendLocalizedJavaDoc("GET_FIELD_METHOD", getFieldName(column), column, methodBuilder);
        methodBuilder.method(Modifier.PUBLIC, datatype.getJavaClassName(), methodName, new String[0], new String[0],
                methodBody, null);
    }

    private boolean checkTableColumns(IColumn[] columns) throws CoreException {
        for (int i = 0; i < columns.length; i++) {
            if (!columns[i].validate(getIpsProject()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /*
     * Code sample: <pre> [Javadoc] private GeneratedGender(Integer id, String name, String
     * description) { this.id = id; this.name = name; this.description = description; } </pre>
     */
    private void generateConstructor(JavaCodeFragmentBuilder methodBuilder,
            ITableStructure tableStructure,
            IKeyItem idKeyItem,
            IKeyItem nameKeyItem) throws CoreException {

        IColumn[] columns = tableStructure.getColumns();
        if (!checkTableColumns(columns)) {
            return;
        }
        List<Datatype> columnsDatatypes = new ArrayList<Datatype>();
        for (int i = 0; i < columns.length; i++) {
            Datatype datatype = columns[i].findValueDatatype(getIpsProject());
            columnsDatatypes.add(datatype);
        }
        Datatype[] datatypes = columnsDatatypes.toArray(new Datatype[columnsDatatypes.size()]);

        JavaCodeFragment methodBody = new JavaCodeFragment();
        for (int i = 0; i < columns.length; i++) {
            methodBody.append("this.");
            methodBody.append(getFieldName(columns[i]));
            methodBody.append(" = ");
            methodBody.append(getFieldName(columns[i]));
            methodBody.appendln(";");
        }
        String[] parameterClasses = new String[columns.length];
        for (int i = 0; i < parameterClasses.length; i++) {
            parameterClasses[i] = datatypes[i].getJavaClassName();
        }
        String[] parameterNames = new String[columns.length];
        for (int i = 0; i < parameterNames.length; i++) {
            parameterNames[i] = columns[i].getName();
        }

        String className = getTableContents().getName();
        appendLocalizedJavaDoc("CONSTRUCTOR", new Object[] { className, idKeyItem.getName(), nameKeyItem.getName() },
                getTableContents(), methodBuilder);
        methodBuilder.method(Modifier.PRIVATE, null, className, parameterNames, parameterClasses, methodBody, null);

    }

    // TODO we have to force some constrains on the name rows[i].getValue(1)
    private String getEnumValueConstantName(IRow row) {
        return StringUtils.upperCase(row.getValue(1));
    }

    private IRow[] getValidTableContentRows() throws CoreException {
        ITableContentsGeneration generation = (ITableContentsGeneration)getTableContents().getFirstGeneration();
        List<IRow> validRows = new ArrayList<IRow>();
        IRow[] rows = generation.getRows();
        for (int i = 0; i < rows.length; i++) {
            MessageList msgList = rows[i].validate(getIpsProject());
            if (msgList.isEmpty()) {
                validRows.add(rows[i]);
            }
        }
        return validRows.toArray(new IRow[validRows.size()]);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * 
     * [Javadoc]
     * public static final GeneratedGender MALE = new GeneratedGender(new Integer(1), &quot;male&quot;, &quot;Male&quot;);
     * </pre>
     */
    private void generateConstantsForEnumValues(JavaCodeFragmentBuilder constantBuilder, ITableStructure structure)
            throws CoreException {

        String className = getTableContents().getName();
        IColumn[] columns = structure.getColumns();
        if (!checkTableColumns(columns)) {
            return;
        }
        // the number of columns retrieved from the table contents object can differ from the one
        // retrieved from the table structure. This inconsistency is allowed in the model and is
        // shown
        // to the user as an error. The builder still has to cope with this inconsistency.
        int numberOfColumns = Math.min(getTableContents().getNumOfColumns(), columns.length);
        List<DatatypeHelper> datatypHelpers = new ArrayList<DatatypeHelper>();
        for (int i = 0; i < numberOfColumns; i++) {
            Datatype datatype = columns[i].findValueDatatype(getIpsProject());
            IIpsProject project = getIpsObject().getIpsProject();
            datatypHelpers.add(project.findDatatypeHelper(datatype.getName()));
        }
        IRow[] rows = getValidTableContentRows();
        for (int i = 0; i < rows.length; i++) {
            JavaCodeFragment value = new JavaCodeFragment();
            value.append("new ");
            value.append(className);
            value.append("(");
            for (int j = 0; j < numberOfColumns; j++) {
                DatatypeHelper helper = datatypHelpers.get(j);
                value.append(helper.newInstance(rows[i].getValue(j)));
                if (j < numberOfColumns - 1) {
                    value.appendln(", ");
                }
            }
            value.append(")");
            appendLocalizedJavaDoc("ENUM_CONSTANT", rows[i], constantBuilder);
            constantBuilder.varDeclaration(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, className,
                    getEnumValueConstantName(rows[i]), value);
            // this line is necessary because it forces a new line between the generated constants
            constantBuilder.appendln(" ");
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * MALE(1, &quot;male&quot;, &quot;Male&quot;);
     * </pre>
     */
    private void generateEnumInitialization(JavaCodeFragmentBuilder enumDefinitionBuilder, ITableStructure structure)
            throws CoreException {

        IColumn[] columns = structure.getColumns();
        if (!checkTableColumns(columns)) {
            return;
        }
        // the number of columns retrieved from the table contents object can differ from the one
        // retrieved from the table structure. This inconsistency is allowed in the model and is
        // shown
        // to the user as an error. The builder still has to cope with this inconsistency.
        int numberOfColumns = Math.min(getTableContents().getNumOfColumns(), columns.length);
        List<DatatypeHelper> datatypeHelpers = getDatatypeHelpers(columns, numberOfColumns);
        IRow[] rows = getValidTableContentRows();
        if (rows.length == 0) {
            enumDefinitionBuilder.appendln(";");
        }
        for (int i = 0; i < rows.length; i++) {
            JavaCodeFragment value = new JavaCodeFragment();
            appendLocalizedJavaDoc("ENUM_VALUE", rows[i], enumDefinitionBuilder);
            value.append(getEnumValueConstantName(rows[i]));
            value.append("(");
            for (int j = 0; j < numberOfColumns; j++) {
                DatatypeHelper helper = datatypeHelpers.get(j);
                // use autoboxing if possible to reduce static code size
                if (helper instanceof IntegerHelper) {
                    helper = DatatypeHelper.PRIMITIVE_INTEGER;
                }
                if (helper instanceof BooleanHelper) {
                    helper = DatatypeHelper.PRIMITIVE_BOOLEAN;
                }
                value.append(helper.newInstance(rows[i].getValue(j)));
                if (j < numberOfColumns - 1) {
                    value.append(", ");
                }
            }
            value.append(")");
            if (i < rows.length - 1) {
                value.appendln(", ");
            } else {
                value.appendln(";");
            }
            value.appendln();
            enumDefinitionBuilder.append(value);
        }
    }

    private List<DatatypeHelper> getDatatypeHelpers(IColumn[] columns, int numberOfColumns) throws CoreException {
        List<DatatypeHelper> datatypeHelpers = new ArrayList<DatatypeHelper>();
        for (int i = 0; i < numberOfColumns; i++) {
            Datatype datatype = columns[i].findValueDatatype(getIpsProject());
            IIpsProject project = getIpsObject().getIpsProject();
            datatypeHelpers.add(project.findDatatypeHelper(datatype.getName()));
        }
        return datatypeHelpers;
    }

    /**
     * Code sample:
     * 
     * <pre>
     * 
     * [Javadoc]
     * public static final GeneratedGender[] getGeneratedGenders() {
     *      return new GeneratedGender[] { MALE, FEMALE };
     * }
     * </pre>
     * 
     * Not generated for Java5 enums, use .values() instead
     */
    private void generateMethodGetAllEnumValues(JavaCodeFragmentBuilder methodBuilder) throws CoreException {

        // TODO java naming conventions
        String methodName = "getAllValues";

        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return ");
        methodBody.append("new ");
        methodBody.append(getTableContents().getName());
        methodBody.append(" []{");
        IRow[] rows = getValidTableContentRows();
        for (int i = 0; i < rows.length; i++) {
            methodBody.append(getEnumValueConstantName(rows[i]));
            if (i < rows.length - 1) {
                methodBody.append(", ");
            }
        }
        methodBody.append("};");

        appendLocalizedJavaDoc("METHOD_GET_ALL_VALUES", getTableContents(), methodBuilder);
        methodBuilder.method(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, getTableContents().getName() + "[]",
                methodName, new String[0], new String[0], methodBody, null);

    }

    private String getMethodNameGetEnumValue(ITableContents tableContents) {
        // TODO java naming conventions
        return "get" + StringUtils.capitalize(tableContents.getName());
    }

    /**
     * This method expects a value that is either a constant or an expression of the regarding
     * datatype. The parameter valueIsExpression needs to be true when a constant value is provided
     * and false otherwise.
     */
    public JavaCodeFragment generateCallMethodGetEnumValue(ITableContents tableContents,
            String value,
            boolean valueIsExpression) throws CoreException {
        ArgumentCheck.notNull(tableContents);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(getPackage(tableContents.getIpsSrcFile()) + '.'
                + StringUtils.capitalize(tableContents.getName()));
        fragment.append('.');
        fragment.append(getMethodNameGetEnumValue(tableContents));
        fragment.append('(');
        EnumValueAttributesInfo info = createEnumValueAttributesInfo(tableContents);
        if (!info.isValid) {
            return fragment;
        }
        DatatypeHelper helper = tableContents.getIpsProject().findDatatypeHelper(info.idDatatype.getQualifiedName());
        if (helper == null) {
            return fragment;
        }
        if (valueIsExpression) {
            fragment.append(helper.newInstanceFromExpression(value));
        } else {
            fragment.append(helper.newInstance(value));
        }
        fragment.append(')');
        return fragment;
    }

    /**
     * Code sample:
     * 
     * <pre>
     *  [Javadoc]
     *  public static final GeneratedGender getGeneratedGender(Integer id) {
     *      if (MALE.id.equals(id)) {
     *          return MALE;
     *      }
     *      if (FEMALE.id.equals(id)) {
     *          return FEMALE;
     *      }
     *      return null;
     *  }
     * </pre>
     */
    private void generateMethodGetEnumValue(JavaCodeFragmentBuilder methodBuilder,
            IKeyItem idKeyItem,
            Datatype idDatatype) throws CoreException {

        JavaCodeFragment methodBody = new JavaCodeFragment();
        String[] parameterNames = new String[] { idKeyItem.getName() };
        String[] parameterClasses = new String[] { idDatatype.getJavaClassName() };
        IRow[] rows = getValidTableContentRows();
        for (int i = 0; i < rows.length; i++) {
            methodBody.append("if(");
            methodBody.append(getEnumValueConstantName(rows[i]));
            methodBody.append(".");
            // TODO better way to find the id column
            methodBody.append(idKeyItem.getName());
            methodBody.append(".equals(");
            methodBody.append(idKeyItem.getName());
            methodBody.append("))");
            methodBody.appendOpenBracket();
            methodBody.append("return ");
            methodBody.append(getEnumValueConstantName(rows[i]));
            methodBody.append(";");
            methodBody.appendCloseBracket();
        }
        methodBody.append("return null;");

        appendLocalizedJavaDoc("METHOD_GET_VALUE", idKeyItem.getName(), getTableContents(), methodBuilder);
        methodBuilder.method(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, getTableContents().getName(),
                getMethodNameGetEnumValue(getTableContents()), parameterNames, parameterClasses, methodBody, null);

    }

    /*
     * Code sample: <pre> [Javadoc] public final static boolean isGender(Integer id){ return
     * getGender(id) != null; } </pre>
     */
    private void generateMethodIsEnumValue(JavaCodeFragmentBuilder methodBuilder,
            IKeyItem idKeyItem,
            Datatype idDatatype) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return ");
        methodBody.append(getMethodNameGetEnumValue(getTableContents()));
        methodBody.append("(");
        methodBody.append(idKeyItem.getName());
        methodBody.append(")");
        methodBody.append(" != null;");

        // TODO java naming conventions
        String methodName = "is" + StringUtils.capitalize(getTableContents().getName());
        String[] parameterNames = new String[] { idKeyItem.getName() };
        String[] parameterClasses = new String[] { idDatatype.getJavaClassName() };

        appendLocalizedJavaDoc("METHOD_IS_VALUE", idKeyItem.getName(), getTableContents(), methodBuilder);
        methodBuilder.method(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, Boolean.TYPE.getName(), methodName,
                parameterNames, parameterClasses, methodBody, null);
    }

    /*
     * Code sample: <pre> [Javadoc] private Object readResolve() throws ObjectStreamException{
     * return getGender(id); } </pre>
     */
    private void generateMethodReadResolve(JavaCodeFragmentBuilder methodBuilder, IKeyItem idKeyItem) {

        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return ");
        methodBody.append(getMethodNameGetEnumValue(getTableContents()));
        methodBody.append("(");
        methodBody.append(idKeyItem.getName());
        methodBody.append(");");
        methodBuilder.javaDoc(null, ANNOTATION_GENERATED);
        methodBuilder.method(Modifier.PRIVATE, Object.class, "readResolve", new String[0], new Class[0],
                new Class[] { ObjectStreamException.class }, methodBody, null);

    }

    /*
     * Code sample: <pre> [Javadoc] public String toString(){ return "Gender: " + name; } </pre>
     */
    private void generateMethodToString(JavaCodeFragmentBuilder methodBuilder, IKeyItem nameKeyItem, IKeyItem idKeyItem) {

        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return ");
        methodBody.append("\"");
        methodBody.append(StringUtils.capitalize(getTableContents().getName()));
        methodBody.append(": \"");
        methodBody.append(" + ");
        methodBody.append(nameKeyItem.getName());
        methodBody.append(" + \"(\" + ");
        methodBody.append(idKeyItem.getName());
        methodBody.append(" + \")\";");

        methodBuilder.javaDoc("{@inheritDoc}", ANNOTATION_GENERATED);
        methodBuilder.method(Modifier.PUBLIC, String.class, "toString", new String[0], new Class[0], methodBody, null);
    }

    /*
     * Code sample: <pre> [Javadoc] private static final long serialVersionUID =
     * 7932454078331259392L; </pre>
     */
    private void generateConstantForSerialVersionNumber(JavaCodeFragmentBuilder constantBuilder) throws CoreException {
        String packageName = getBuilderSet().getPackage(PACKAGE_STRUCTURE_KIND_ID, getIpsSrcFile());
        int hashCode = 17;
        hashCode = 37 * hashCode + packageName.hashCode();
        hashCode = 37 * hashCode + getTableContents().getName().hashCode();
        appendLocalizedJavaDoc("SERIALVERSIONUID", getTableContents(), constantBuilder);
        constantBuilder.varDeclaration(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, Long.TYPE,
                "serialVersionUID", new JavaCodeFragment(String.valueOf(hashCode)));
    }

    private static class EnumValueAttributesInfo {

        private final static EnumValueAttributesInfo INVALID_INFO = new EnumValueAttributesInfo();

        public final IKeyItem idKeyItem;
        public final IKeyItem nameKeyItem;
        public final Datatype idDatatype;
        public final boolean isValid;

        private EnumValueAttributesInfo() {
            idKeyItem = null;
            nameKeyItem = null;
            idDatatype = null;
            isValid = false;
        }

        /**
         * @param idKeyItem
         * @param nameKeyItem
         * @param idDatatype
         */
        private EnumValueAttributesInfo(final IKeyItem idKeyItem, final IKeyItem nameKeyItem, final Datatype idDatatype) {
            this.idKeyItem = idKeyItem;
            this.nameKeyItem = nameKeyItem;
            this.idDatatype = idDatatype;
            isValid = true;
        }
    }

    /**
     * Returns if Java 5 enums shall be used in the code generated by this builder.
     */
    public boolean isUseEnums() {
        return ((StandardBuilderSet)getBuilderSet()).isUseEnums();
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer,
            boolean recursivelyIncludeChildren) {

        // TODO AW: Not implemented yet.
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        // TODO AW: Not implemented yet.
        throw new RuntimeException("Not implemented yet.");
    }
}
