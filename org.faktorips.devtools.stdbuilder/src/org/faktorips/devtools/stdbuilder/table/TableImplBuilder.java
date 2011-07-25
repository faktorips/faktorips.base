/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.table;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree;
import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree.TwoColumnKey;
import org.faktorips.runtime.internal.Table;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Important: This builder expects from the <code>IJavaPackageStructure</code> the qualified class
 * name for a table row class. It uses the kindId="tablerow" to get this name.
 * 
 * @author Peter Erzberger
 */
public class TableImplBuilder extends DefaultJavaSourceFileBuilder {

    private final static String GET_ALL_ROWS_JAVADOC = "TABLE_IMPL_BUILDER_GET_ALL_ROWS_JAVADOC";
    private final static String GET_INSTANCE_JAVADOC = "TABLE_IMPL_BUILDER_GET_INSTANCE_JAVADOC";
    private final static String KEY_CLASS_EQUALS_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_EQUALS_JAVADOC";
    private final static String KEY_CLASS_HASHCODE_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_HASHCODE_JAVADOC";
    private final static String FIND_JAVADOC = "TABLE_IMPL_BUILDER_FIND_JAVADOC";
    private final static String FIND_RETURN_NULL_ROW_JAVADOC = "TABLE_IMPL_BUILDER_FIND_RETURN_NULL_ROW_JAVADOC";
    private final static String KEY_CLASS_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_JAVADOC";
    private final static String KEY_CLASS_CONSTRUCTOR_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_CONSTRUCTOR_JAVADOC";
    private final static String ADD_ROW_JAVADOC = "TABLE_IMPL_BUILDER_ADD_ROW_JAVADOC";
    private final static String INIT_KEY_MAPS_JAVADOC = "TABLE_IMPL_BUILDER_INIT_KEY_MAPS_JAVADOC";

    private final static String INIT_KEY_MAPS = "initKeyMaps";

    private TableRowBuilder tableRowBuilder;

    private String[] fKeyVariableNames;
    private String[] fKeyClassNames;
    private List<String[]> fKeyClassParameterNames;
    private List<String[]> fAllItemNamesAsParameters;
    private List<String[]> fKeyClassParameterTypes;
    private List<String[]> fAllItemParameterTypes;
    private Map<String, Datatype> fDatatypes;
    private String qualifiedTableRowName;
    private Map<String, IColumnRange> fRanges;

    public TableImplBuilder(DefaultBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(TableImplBuilder.class));
        setMergeEnabled(true);
    }

    public void setTableRowBuilder(TableRowBuilder tableRowBuilder) {
        this.tableRowBuilder = tableRowBuilder;
    }

    /**
     * Returns the table structure during generation and null otherwise.
     */
    protected ITableStructure getTableStructure() {
        return (ITableStructure)getIpsObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        qualifiedTableRowName = tableRowBuilder.getQualifiedClassName(ipsSrcFile);
        buildKeyVariableNames();
        initClassVariables();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
        super.afterBuild(ipsSrcFile);
        fKeyVariableNames = null;
        fKeyClassNames = null;
        fKeyClassParameterNames = null;
        fAllItemNamesAsParameters = null;
        fKeyClassParameterTypes = null;
        fAllItemParameterTypes = null;
        fDatatypes = null;
        fRanges = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.TABLE_STRUCTURE)) {
            return true;
        }
        return false;
    }

    private Set<Integer> getIndicesForKeysWithSameDatatypeSequence() throws CoreException {
        IUniqueKey[] keys = getUniqueKeys();
        Set<Integer> positions = new HashSet<Integer>();
        for (int i = 0; i < keys.length; i++) {
            IUniqueKey key1 = keys[i];
            for (int j = i + 1; j < keys.length; j++) {
                IUniqueKey key2 = keys[j];
                if (compareByDatatypeOnly(key1, key2)) {
                    positions.add(new Integer(i));
                    positions.add(new Integer(j));
                }
            }
        }
        return positions;
    }

    private void buildKeyVariableNames() {
        IUniqueKey[] keys = getUniqueKeys();
        fKeyVariableNames = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            fKeyVariableNames[i] = "Key" + i;
        }
    }

    private String[] buildFindMethodNamesArray() throws CoreException {
        IUniqueKey[] keys = getUniqueKeys();
        String[] findMethodNames = new String[keys.length];
        Set<Integer> keysWithSameDatatypeSequence = getIndicesForKeysWithSameDatatypeSequence();
        for (int i = 0; i < keys.length; i++) {
            if (keysWithSameDatatypeSequence.contains(new Integer(i))) {
                StringBuffer keyName = new StringBuffer();
                String[] keyItems = keys[i].getKeyItemNames();
                keyName.append("By");
                for (String keyItem : keyItems) {

                    if (getTableStructure().hasColumn(keyItem)) {
                        keyName.append(StringUtils.capitalize(keyItem));
                        continue;
                    }

                    IColumnRange range = getTableStructure().getRange(keyItem);
                    keyName.append(StringUtils.capitalize(range.getParameterName()));
                }
                findMethodNames[i] = keyName.toString();
                continue;
            }
            findMethodNames[i] = "";
        }
        return findMethodNames;
    }

    private boolean compareByDatatypeOnly(IUniqueKey first, IUniqueKey second) throws CoreException {

        String[] firstkeyItems = first.getKeyItemNames();
        String[] secondkeyItems = second.getKeyItemNames();

        if (firstkeyItems.length != secondkeyItems.length) {
            return false;
        }

        for (int i = 0; i < firstkeyItems.length; i++) {
            Datatype firstDatatype = getDatatypeForKeyName(firstkeyItems[i]);
            Datatype secondDatatype = getDatatypeForKeyName(secondkeyItems[i]);
            if (firstDatatype == null || secondDatatype == null) {
                // compare failed beacause of missing datatypes
                return false;
            }
            if (!firstDatatype.equals(secondDatatype)) {
                return false;
            }
        }
        return true;
    }

    private void initClassVariables() throws CoreException {
        if (!checkUniqueKeyValidity()) {
            return;
        }
        IUniqueKey[] keys = getUniqueKeys();
        fKeyClassNames = new String[keys.length];
        fKeyClassParameterNames = new ArrayList<String[]>(keys.length);
        fKeyClassParameterTypes = new ArrayList<String[]>(keys.length);
        fAllItemNamesAsParameters = new ArrayList<String[]>(keys.length);
        fAllItemParameterTypes = new ArrayList<String[]>(keys.length);
        fRanges = new HashMap<String, IColumnRange>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            String[] keyItems = keys[i].getKeyItemNames();
            List<String> parameters = new ArrayList<String>();
            List<String> allParameterTypes = new ArrayList<String>();
            List<String> keyClassParameterTypes = new ArrayList<String>();
            List<String> keyClassParameterNames = new ArrayList<String>();
            boolean isColumn = false;
            for (String keyItem : keyItems) {
                allParameterTypes.add(getJavaClassName(keyItem));

                if (getTableStructure().hasColumn(keyItem)) {
                    keyClassParameterNames.add(StringUtils.uncapitalize(keyItem));
                    keyClassParameterTypes.add(getJavaClassName(keyItem));
                    parameters.add(StringUtils.uncapitalize(keyItem));
                    isColumn = true;
                    continue;
                }

                IColumnRange range = getTableStructure().getRange(keyItem);
                parameters.add(range.getParameterName());
                if (!isColumn && range.getColumnRangeType().isTwoColumn()) {
                    fKeyClassNames[i] = TwoColumnKey.class.getName();
                }
                fRanges.put(range.getParameterName(), range);
            }

            if (isColumn) {
                fKeyClassNames[i] = "UniqueKey" + i;
                fKeyClassParameterNames.add(keyClassParameterNames.toArray(new String[0]));
                fKeyClassParameterTypes.add(keyClassParameterTypes.toArray(new String[0]));
            } else {
                fKeyClassParameterNames.add(null);
                fKeyClassParameterTypes.add(null);
            }
            fAllItemParameterTypes.add(allParameterTypes.toArray(new String[0]));
            fAllItemNamesAsParameters.add(parameters.toArray(new String[0]));
        }
    }

    private boolean checkUniqueKeyValidity() throws CoreException {
        IUniqueKey[] keys = getTableStructure().getUniqueKeys();
        for (int i = 0; i < keys.length; i++) {
            if (!keys[i].isValid(getIpsProject())) {
                return false;
            }
            // the key is valid
            // additional check if the column inside the keys are valid
            IKeyItem[] keyItems = keys[i].getKeyItems();
            for (int j = 0; j < keyItems.length; j++) {
                if (!checkColumnsValidity(keyItems[j].getColumns())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void generateCodeForJavatype() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setUnqualifiedName(getTableStructure().getName());
        mainSection.setSuperClass(Table.class.getName());
        mainSection.setClass(true);

        String description = getDescriptionInGeneratorLanguage(getIpsObject());
        appendLocalizedJavaDoc("CLASS_DESCRIPTION", getIpsObject(), description, mainSection.getJavaDocForTypeBuilder());
        createFields(mainSection.getMemberVarBuilder());
        generateConstructors(mainSection.getConstructorBuilder());
        createAddRowMethod(mainSection.getMethodBuilder());
        createInitKeyMapsMethod(mainSection.getMethodBuilder());
        // create single instance method only if no multi content is allowed
        if (!getTableStructure().isMultipleContentsAllowed()) {
            createGetInstanceMethodForSingleContent(mainSection.getMethodBuilder());
        }
        // create multi instance content method if multi or single content
        createGetInstanceMethodForMultipleContents(mainSection.getMethodBuilder());
        createAllRowsMethod(mainSection.getMethodBuilder());
        createFindMethods(mainSection.getMethodBuilder());
        createHashKeyClasses();
    }

    private void generateConstructors(JavaCodeFragmentBuilder code) throws CoreException {
        generateDefaultConstructor(code);
        generateConstructorWithRowsParameter(code);
    }

    private void generateDefaultConstructor(JavaCodeFragmentBuilder code) throws CoreException {
        appendLocalizedJavaDoc("CONSTRUCTOR_DEFAULT", getTableStructure(), code);
        code.appendJavaModifier(Modifier.PUBLIC);
        code.append(' ');
        code.append(getUnqualifiedClassName());
        code.appendln("() {");
        code.append("super();");
        code.append("}");
    }

    private void generateConstructorWithRowsParameter(JavaCodeFragmentBuilder code) throws CoreException {
        appendLocalizedJavaDoc("CONSTRUCTOR_WITH_ROWS", getTableStructure(), code);
        if (isUseTypesafeCollections()) {
            code.annotation(new String[] { ANNOTATION_SUPPRESS_WARNINGS_UNCHECKED });
        }
        code.appendJavaModifier(Modifier.PUBLIC);
        code.append(' ');
        code.append(getUnqualifiedClassName());
        code.appendln("(");
        code.appendClassName(List.class);
        if (isUseTypesafeCollections()) {
            code.append('<');
            code.appendClassName(qualifiedTableRowName);
            code.append('>');
        }
        code.append(" content) {");
        code.appendln("super();");
        code.append("rows = new ");
        code.appendClassName(ArrayList.class);
        code.appendln("(content.size());");
        code.appendln("rows.addAll(content);");
        code.append(INIT_KEY_MAPS);
        code.append("();");
        code.appendln("}");
    }

    private void createAllRowsMethod(JavaCodeFragmentBuilder codeBuilder) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        if (isUseTypesafeCollections()) {
            methodBody.append("return ");
            methodBody.appendClassName(Collections.class);
            methodBody.appendln(".unmodifiableList(rows);");
            codeBuilder.method(Modifier.PUBLIC, List.class.getName() + "<" + qualifiedTableRowName + ">", "getAllRows",
                    new String[0], new String[0], methodBody, getLocalizedText(getIpsObject(), GET_ALL_ROWS_JAVADOC),
                    ANNOTATION_GENERATED, new String[] { ANNOTATION_SUPPRESS_WARNINGS_UNCHECKED });
        } else {
            methodBody.appendClassName(qualifiedTableRowName);
            methodBody.append("[] rowsArray  =  new ");
            methodBody.appendClassName(qualifiedTableRowName);
            methodBody.appendln("[rows.size()];");
            methodBody.appendln("rows.toArray(rowsArray);");
            methodBody.append("return rowsArray;");
            codeBuilder.method(Modifier.PUBLIC, qualifiedTableRowName + "[]", "getAllRows", new String[0],
                    new String[0], methodBody, getLocalizedText(getIpsObject(), GET_ALL_ROWS_JAVADOC),
                    ANNOTATION_GENERATED, null);
        }
    }

    private void createGetInstanceMethodForSingleContent(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        String qualifiedClassName = getQualifiedClassName(getTableStructure().getIpsSrcFile());
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return (");
        methodBody.appendClassName(qualifiedClassName);
        methodBody.append(")");
        methodBody.append("repository.getTable(");
        methodBody.appendClassName(qualifiedClassName);
        methodBody.append(".class");
        methodBody.append(");");

        codeBuilder.method(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, qualifiedClassName, "getInstance",
                new String[] { "repository" }, new String[] { IRuntimeRepository.class.getName() }, methodBody,
                getLocalizedText(getIpsObject(), GET_INSTANCE_JAVADOC), ANNOTATION_GENERATED);
    }

    private void createGetInstanceMethodForMultipleContents(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        String qualifiedClassName = getQualifiedClassName(getTableStructure().getIpsSrcFile());
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return (");
        methodBody.appendClassName(qualifiedClassName);
        methodBody.append(")");
        methodBody.append("repository.getTable(qualifiedTableName);");

        codeBuilder.method(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, qualifiedClassName, "getInstance",
                new String[] { "repository", "qualifiedTableName" }, new String[] { IRuntimeRepository.class.getName(),
                        String.class.getName() }, methodBody, getLocalizedText(getIpsObject(), GET_INSTANCE_JAVADOC),
                ANNOTATION_GENERATED);
    }

    private boolean checkColumnsValidity(IColumn[] columns) throws CoreException {
        for (int i = 0; i < columns.length; i++) {
            if (!columns[i].isValid(getIpsProject())) {
                return false;
            }
        }
        return true;
    }

    private void createAddRowMethod(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        IColumn[] columns = getTableStructure().getColumns();
        if (!checkColumnsValidity(columns)) {
            return;
        }

        for (int i = 0; i < columns.length; i++) {
            String valueName = "columnValue";
            if (i == 0) {
                methodBody.appendClassName(String.class.getName());
                methodBody.append(" ");
            }
            methodBody.append(valueName);
            methodBody.append(" = (String)");
            methodBody.append("values.get(");
            methodBody.append(i);
            methodBody.append(");");
            IColumn column = columns[i];
            Datatype columnDatatype = findDatatype(column.getDatatype());
            DatatypeHelper helper = getTableStructure().getIpsProject().getDatatypeHelper(columnDatatype);

            methodBody.appendClassName(columnDatatype.getJavaClassName());
            methodBody.append(' ');
            methodBody.append(StringUtils.uncapitalize(column.getName()));
            methodBody.append(" = ");
            methodBody.append(valueName);
            methodBody.append(" == null ? ");
            methodBody.append(helper.nullExpression());
            methodBody.append(" : ");
            if (helper instanceof EnumTypeDatatypeHelper) {
                EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)helper;
                if (!enumHelper.getEnumType().isContainingValues()) {
                    methodBody.append(enumHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(
                            enumHelper.getEnumType(), valueName, new JavaCodeFragment("productRepository")));
                } else {
                    methodBody.append(helper.newInstanceFromExpression(valueName));
                }
            } else {
                methodBody.append(helper.newInstanceFromExpression(valueName));
            }
            methodBody.append(';');
            methodBody.appendln();
        }

        methodBody.append("rows.add(new ");
        methodBody.appendClassName(qualifiedTableRowName);
        methodBody.append("(");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                methodBody.append(", ");
            }
            methodBody.append(StringUtils.uncapitalize(columns[i].getName()));
        }
        methodBody.append("));");

        methodBody.addImport(List.class.getName());
        codeBuilder.javaDoc(getLocalizedText(getIpsObject(), ADD_ROW_JAVADOC), ANNOTATION_GENERATED);
        appendOverrideAnnotation(codeBuilder, false);
        if (isUseTypesafeCollections()) {
            codeBuilder.annotation(new String[] { ANNOTATION_SUPPRESS_WARNINGS_UNCHECKED });
        }
        codeBuilder.methodBegin(Modifier.PROTECTED, Void.TYPE, "addRow",
                new String[] { "values", "productRepository" }, new Class[] { List.class, IRuntimeRepository.class });
        codeBuilder.append(methodBody);
        codeBuilder.methodEnd();
    }

    private boolean isUseTypesafeCollections() {
        return ((StandardBuilderSet)getBuilderSet()).isUseTypesafeCollections();
    }

    private void createFindMethods(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        if (!checkUniqueKeyValidity()) {
            return;
        }
        IUniqueKey[] keys = getUniqueKeys();
        String[] findMethodNames = buildFindMethodNamesArray();
        for (int i = 0; i < keys.length; i++) {
            StringBuffer methodName = new StringBuffer();
            methodName.append("findRow");
            methodName.append(findMethodNames[i]);
            createFindMethodRegular(methodName.toString(), qualifiedTableRowName, fAllItemParameterTypes.get(i),
                    fAllItemNamesAsParameters.get(i), fKeyClassParameterNames.get(i), fKeyVariableNames[i],
                    fKeyClassNames[i], keys[i], codeBuilder);
            createFindMethodWithNullValueRow(methodName.toString(), qualifiedTableRowName,
                    fAllItemParameterTypes.get(i), fAllItemNamesAsParameters.get(i), fKeyClassParameterNames.get(i),
                    fKeyVariableNames[i], fKeyClassNames[i], keys[i], codeBuilder);
        }
    }

    private IUniqueKey[] getUniqueKeys() {
        return getTableStructure().getUniqueKeys();
    }

    private void createFields(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        if (!checkUniqueKeyValidity()) {
            return;
        }

        for (int i = 0; i < fKeyVariableNames.length; i++) {
            StringBuffer keyMapFieldName = new StringBuffer();
            String fieldClassName;
            keyMapFieldName.append(StringUtils.uncapitalize(fKeyVariableNames[i]));
            if (fKeyClassParameterNames.get(i) != null) {
                keyMapFieldName.append("Map");
                fieldClassName = Map.class.getName();
                if (isUseTypesafeCollections()) {
                    fieldClassName += "<" + fKeyClassNames[i] + ", " + qualifiedTableRowName + ">";
                }
            } else {
                keyMapFieldName.append("Tree");
                fieldClassName = ReadOnlyBinaryRangeTree.class.getName();
            }
            appendLocalizedJavaDoc("FIELD_KEY_MAP", getTableStructure(), codeBuilder);
            codeBuilder.varDeclaration(Modifier.PRIVATE, fieldClassName, keyMapFieldName.toString());
        }
    }

    private JavaCodeFragment buildAddKeyFragment(String combinedKeyName, String keyClassName, String[] keyItems) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append(StringUtils.uncapitalize(combinedKeyName));
        methodBody.append("Map.put(new ");
        methodBody.append(keyClassName);
        methodBody.append("(");
        for (int i = 0; i < keyItems.length; i++) {
            if (i != 0) {
                methodBody.append(", ");
            }
            methodBody.append("row.get");
            methodBody.append(StringUtils.capitalize(keyItems[i]));
            methodBody.append("()");
        }
        methodBody.append("), row);");
        return methodBody;
    }

    private void createInitKeyMapsMethod(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        if (!checkUniqueKeyValidity()) {
            return;
        }

        IUniqueKey[] keys = getUniqueKeys();
        JavaCodeFragment methodBody = new JavaCodeFragment();
        if (keys.length != 0) {
            methodBody = createInitKeyMapsMethodBody(keys);
        }
        codeBuilder.javaDoc(getLocalizedText(getIpsObject(), INIT_KEY_MAPS_JAVADOC), ANNOTATION_GENERATED);
        appendOverrideAnnotation(codeBuilder, false);
        // TODO pk 12-08-2009: why is here a check agains <???
        if (isUseTypesafeCollections() && methodBody.getSourcecode().indexOf("<") > 0) {
            codeBuilder.annotation(new String[] { ANNOTATION_SUPPRESS_WARNINGS_UNCHECKED });
        }
        codeBuilder.methodBegin(Modifier.PROTECTED, Void.TYPE, INIT_KEY_MAPS, new String[0], new Class[0]);
        codeBuilder.append(methodBody);
        codeBuilder.methodEnd();
    }

    private JavaCodeFragment createInitKeyMapsMethodBody(IUniqueKey[] keys) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        for (int i = 0; i < keys.length; i++) {
            String keyClassName = fKeyClassNames[i];
            if (keyClassName == null && keys[i].containsRanges()) {
                String[] keyClassParameterNames = fKeyClassParameterNames.get(i);
                String[] parameterNames = fAllItemNamesAsParameters.get(i);
                String[] rangeParameterNames;
                if (keyClassParameterNames == null) {
                    rangeParameterNames = parameterNames;
                } else {
                    rangeParameterNames = new String[parameterNames.length - keyClassParameterNames.length];
                    System.arraycopy(parameterNames, keyClassParameterNames.length, rangeParameterNames, 0,
                            rangeParameterNames.length);
                }
                for (String rangeParameterName : rangeParameterNames) {
                    IColumnRange range = fRanges.get(rangeParameterName);
                    if (range != null && range.getColumnRangeType().isTwoColumn()) {
                        keyClassName = TwoColumnKey.class.getName();
                    }
                }
            }
            methodBody.append(createInitKeyMapsVariables(keys[i], fKeyVariableNames[i] + "Map", keyClassName));
            methodBody.appendln();
        }
        methodBody.append("for (");
        methodBody.appendClassName(Iterator.class);
        if (isUseTypesafeCollections()) {
            methodBody.append("<");
            methodBody.appendClassName(qualifiedTableRowName);
            methodBody.append(">");
        }
        methodBody.append(" it = rows.iterator(); it.hasNext();)");
        methodBody.appendOpenBracket();
        methodBody.appendClassName(qualifiedTableRowName);
        if (isUseTypesafeCollections()) {
            methodBody.append(" row = it.next();");
        } else {
            methodBody.append(" row = (");
            methodBody.appendClassName(qualifiedTableRowName);
            methodBody.append(") it.next();");
        }
        methodBody.appendln();

        for (int i = 0; i < keys.length; i++) {
            if (keys[i].containsRanges()) {
                String tempName = StringUtils.uncapitalize(fKeyVariableNames[i]) + "MapTemp";
                String[] parameterNames = fAllItemNamesAsParameters.get(i);
                String[] keyClassParameterNames = fKeyClassParameterNames.get(i);
                String[] rangeParameterNames;
                if (keyClassParameterNames == null) {
                    rangeParameterNames = parameterNames;
                } else {
                    rangeParameterNames = new String[parameterNames.length - keyClassParameterNames.length];
                    System.arraycopy(parameterNames, keyClassParameterNames.length, rangeParameterNames, 0,
                            rangeParameterNames.length);
                }
                ArrayList<String> getMapFirstParameter = new ArrayList<String>();
                ArrayList<String> getMapSecondParameter = new ArrayList<String>();
                ArrayList<String> getMapThirdParameter = new ArrayList<String>();
                getMapFirstParameter.add(tempName);
                if (keys[i].containsColumns()) {
                    getMapFirstParameter.add(StringUtils.uncapitalize(rangeParameterNames[0]) + "Map");
                    getMapSecondParameter.add(createKeyInstantiation(fKeyClassNames[i],
                            createInitKeyMapsKeyClassParameters(fKeyClassParameterNames.get(i))).toString());
                    getMapThirdParameter.add(null);
                }
                for (int j = 0; j < rangeParameterNames.length; j++) {
                    if (j != 0) {
                        getMapFirstParameter.add(StringUtils.uncapitalize(rangeParameterNames[j]) + "Map");
                    }
                    IColumnRange range = fRanges.get(rangeParameterNames[j]);
                    if (range != null && range.getColumnRangeType().isTwoColumn()) {
                        getMapSecondParameter.add("row.get" + StringUtils.capitalize(range.getFromColumn()) + "()");
                        getMapThirdParameter.add("row.get" + StringUtils.capitalize(range.getToColumn()) + "()");
                    } else {
                        String paramName = range == null ? rangeParameterNames[j] : range.getColumnRangeType()
                                .isOneColumnFrom() ? range.getFromColumn() : range.getToColumn();
                        getMapSecondParameter.add("row.get" + StringUtils.capitalize(paramName) + "()");
                        getMapThirdParameter.add(null);
                    }
                }
                for (int j = 1; j < getMapFirstParameter.size(); j++) {
                    methodBody.append(createInitKeyMapsMapAssignment(getMapFirstParameter.get(j),
                            getMapFirstParameter.get(j - 1), getMapSecondParameter.get(j - 1),
                            getMapThirdParameter.get(j - 1)));
                    methodBody.appendln();
                }
                methodBody.append(createInitKeyMapsPutStatement(
                        getMapFirstParameter.get(getMapFirstParameter.size() - 1),
                        getMapSecondParameter.get(getMapSecondParameter.size() - 1),
                        getMapThirdParameter.get(getMapThirdParameter.size() - 1)));
                methodBody.appendln();
                continue;
            }
            methodBody.append(buildAddKeyFragment(fKeyVariableNames[i], fKeyClassNames[i],
                    fKeyClassParameterNames.get(i)));
        }

        methodBody.appendCloseBracket();
        methodBody.append(createInitKeyMapsFieldAssignments(keys));
        return methodBody;
    }

    private JavaCodeFragment createInitKeyMapsPutStatement(String mapName, String key, String secondKey) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(mapName);
        fragment.append(".put(");
        if (secondKey != null) {
            fragment.append("new ");
            fragment.appendInnerClassName(TwoColumnKey.class);
            fragment.append("(");
        }
        fragment.append(key);
        if (secondKey != null) {
            fragment.append(", ");
            fragment.append(secondKey);
            fragment.append(")");
        }
        fragment.append(", row);");
        return fragment;
    }

    private JavaCodeFragment createInitKeyMapsFieldAssignments(IUniqueKey[] keys) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        for (int i = 0; i < keys.length; i++) {
            if (!keys[i].containsRanges()) {
                continue;
            }
            fragment.append(StringUtils.uncapitalize(fKeyVariableNames[i]));
            fragment.append(keys[i].containsColumns() ? "Map" : "Tree");
            fragment.append(" = ");
            fragment.append(keys[i].containsColumns() ? "convert" : "generateTree");
            fragment.append("(");
            fragment.append(StringUtils.uncapitalize(fKeyVariableNames[i]));
            fragment.append("MapTemp, new ");
            if (isUseTypesafeCollections()) {
                fragment.appendClassName(Java5ClassNames.ReadOnlyBinaryRangeTreeKeyType_QualifiedName);
            } else {
                fragment.appendClassName(Integer.TYPE);
            }
            fragment.append("[] {");
            String[] keyItemNames = keys[i].getKeyItemNames();
            boolean firstEntry = true;
            for (String keyItemName : keyItemNames) {
                IColumnRange range = getTableStructure().getRange(keyItemName);
                if (range == null) {
                    continue;
                }
                if (!firstEntry) {
                    fragment.append(", ");
                }
                firstEntry = false;
                fragment.appendClassName(ReadOnlyBinaryRangeTree.class);
                fragment.append('.');
                if (range.getColumnRangeType().isTwoColumn()) {
                    fragment.append("KEY_IS_TWO_COLUMN_KEY");
                }
                if (range.getColumnRangeType().isOneColumnFrom()) {
                    fragment.append("KEY_IS_LOWER_BOUND_EQUAL");
                }
                if (range.getColumnRangeType().isOneColumnTo()) {
                    fragment.append("KEY_IS_UPPER_BOUND_EQUAL");
                }
            }
            fragment.append("});");
        }
        return fragment;
    }

    private JavaCodeFragment createInitKeyMapsVariables(IUniqueKey key, String fieldName, String keyClassName) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        if (key.containsRanges()) {
            methodBody.appendClassName(HashMap.class);
            if (isUseTypesafeCollections()) {
                methodBody.append("<");
                methodBody.appendClassName(keyClassName != null ? keyClassName : Integer.class.getName());
                methodBody.append(", ");
                methodBody.appendClassName(qualifiedTableRowName);
                methodBody.append(">");
            }
            methodBody.append(' ');
            methodBody.append(StringUtils.uncapitalize(fieldName));
            methodBody.append("Temp");
        } else {
            methodBody.append(StringUtils.uncapitalize(fieldName));
        }
        methodBody.append(" = new ");
        methodBody.appendClassName(HashMap.class);
        if (isUseTypesafeCollections()) {
            methodBody.append("<");
            methodBody.appendClassName(keyClassName != null ? keyClassName : Integer.class.getName());
            methodBody.append(", ");
            methodBody.appendClassName(qualifiedTableRowName);
            methodBody.append(">");
        }
        methodBody.append("(rows.size());");
        return methodBody;
    }

    private JavaCodeFragment createInitKeyMapsMapAssignment(String returnedMapName,
            String fromMapName,
            String keyName,
            String secondKeyName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Map.class);
        fragment.append(' ');
        fragment.append(returnedMapName);
        fragment.append(" = getMap(");
        fragment.append(fromMapName);
        fragment.append(", ");
        fragment.append(keyName);
        if (secondKeyName != null) {
            fragment.append(", ");
            fragment.append(secondKeyName);
        }
        fragment.append(");");
        return fragment;
    }

    private String[] createInitKeyMapsKeyClassParameters(String[] rangeParameterNames) {
        String[] returnValue = new String[rangeParameterNames.length];
        for (int i = 0; i < rangeParameterNames.length; i++) {
            returnValue[i] = "row.get" + StringUtils.capitalize(rangeParameterNames[i]) + "()";
        }
        return returnValue;
    }

    private void createHashKeyClasses() throws CoreException {
        if (!checkUniqueKeyValidity()) {
            return;
        }
        IUniqueKey[] keys = getUniqueKeys();
        for (int i = 0; i < keys.length; i++) {
            String[] keyClassParameterTypes = fKeyClassParameterTypes.get(i);
            if (keyClassParameterTypes != null) {
                createHashKeyClass(fKeyClassNames[i], fKeyClassParameterNames.get(i), keyClassParameterTypes);
            }
        }
    }

    private void createHashKeyClass(String hashKeyClassName, String[] keyNames, String[] keyItemTypes) {
        TypeSection innerClassBody = createInnerClassSection();
        innerClassBody.getJavaDocForTypeBuilder().javaDoc(getLocalizedText(getIpsObject(), KEY_CLASS_JAVADOC),
                ANNOTATION_GENERATED);
        innerClassBody.setClassModifier(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        innerClassBody.setUnqualifiedName(hashKeyClassName);
        for (int i = 0; i < keyNames.length; i++) {
            innerClassBody.getMemberVarBuilder().javaDoc("", ANNOTATION_GENERATED);
            innerClassBody.getMemberVarBuilder().varDeclaration(Modifier.PRIVATE, keyItemTypes[i], keyNames[i]);
        }
        // create hashCode field
        innerClassBody.getMemberVarBuilder().javaDoc("Cached hashcode.", ANNOTATION_GENERATED);
        innerClassBody.getMemberVarBuilder().varDeclaration(Modifier.PRIVATE, Integer.TYPE, "hashCode");

        JavaCodeFragment constructorBody = new JavaCodeFragment();
        for (String keyName : keyNames) {
            constructorBody.append("this.");
            constructorBody.append(keyName);
            constructorBody.append(" = ");
            constructorBody.append(keyName);
            constructorBody.append(';');
            constructorBody.appendln();
        }
        constructorBody.appendln("hashCode = calculateHashCode();");
        // constructor
        innerClassBody.getConstructorBuilder().method(Modifier.PRIVATE, null, hashKeyClassName, keyNames, keyItemTypes,
                constructorBody, getLocalizedText(getIpsObject(), KEY_CLASS_CONSTRUCTOR_JAVADOC), ANNOTATION_GENERATED);
        createKeyClassCalHashCodeMethod(keyNames, innerClassBody.getMethodBuilder());
        createKeyClassEqualsMethod(hashKeyClassName, keyNames, innerClassBody.getMethodBuilder());
        createKeyClassHashCodeMethod(innerClassBody.getMethodBuilder());
    }

    private void createKeyClassCalHashCodeMethod(String[] keyNames, JavaCodeFragmentBuilder codeBuilder) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.appendln("int result = 17;");
        for (String keyName : keyNames) {
            methodBody.append("result = 37 * result + ");
            methodBody.append(keyName);
            methodBody.appendln(".hashCode();");
        }
        methodBody.appendln("return result;");

        codeBuilder.methodBegin(Modifier.PRIVATE, Integer.TYPE, "calculateHashCode", new String[0], new Class[0], "",
                ANNOTATION_GENERATED);
        codeBuilder.append(methodBody);
        codeBuilder.methodEnd();
    }

    private void createKeyClassEqualsMethod(String keyClass,
            String[] combinedKeyNames,
            JavaCodeFragmentBuilder codeBuilder) {

        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("if (o instanceof ");
        methodBody.append(keyClass);
        methodBody.append(")");
        methodBody.appendOpenBracket();
        methodBody.append(keyClass);
        methodBody.append(" other = (");
        methodBody.append(keyClass);
        methodBody.append(") o;");
        methodBody.append("return ");
        for (int i = 0; i < combinedKeyNames.length; i++) {
            if (i != 0) {
                methodBody.append(" && ");
            }
            methodBody.append(combinedKeyNames[i]);
            methodBody.append(".equals(other.");
            methodBody.append(combinedKeyNames[i]);
            methodBody.append(")");
        }
        methodBody.append(";");
        methodBody.appendCloseBracket();
        methodBody.append("return false;");

        codeBuilder.javaDoc(getLocalizedText(getIpsObject(), KEY_CLASS_EQUALS_JAVADOC), ANNOTATION_GENERATED);
        appendOverrideAnnotation(codeBuilder, false);
        codeBuilder.methodBegin(Modifier.PUBLIC, Boolean.TYPE, "equals", new String[] { "o" },
                new Class[] { Object.class });
        codeBuilder.append(methodBody);
        codeBuilder.methodEnd();
    }

    private void createKeyClassHashCodeMethod(JavaCodeFragmentBuilder codeBuilder) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return hashCode;");

        codeBuilder.javaDoc(getLocalizedText(getIpsObject(), KEY_CLASS_HASHCODE_JAVADOC), ANNOTATION_GENERATED);
        appendOverrideAnnotation(codeBuilder, false);
        codeBuilder.methodBegin(Modifier.PUBLIC, Integer.TYPE, "hashCode", new String[0], new Class[0]);
        codeBuilder.append(methodBody);
        codeBuilder.methodEnd();
    }

    private void createFindMethodWithNullValueRow(String methodName,
            String returnTypeName,
            String[] parameterTypes,
            String[] parameterNames,
            String[] keyClassParameterNames,
            String combinedKeyName,
            String keyClassName,
            IUniqueKey key,
            JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        methodName = methodName + "NullRowReturnedForEmtpyResult";
        createFindMethod(methodName, returnTypeName, parameterTypes, parameterNames, keyClassParameterNames,
                combinedKeyName, keyClassName, key, codeBuilder, true);
    }

    private void createFindMethodRegular(String methodName,
            String returnTypeName,
            String[] parameterTypes,
            String[] parameterNames,
            String[] keyClassParameterNames,
            String combinedKeyName,
            String keyClassName,
            IUniqueKey key,
            JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        createFindMethod(methodName, returnTypeName, parameterTypes, parameterNames, keyClassParameterNames,
                combinedKeyName, keyClassName, key, codeBuilder, false);
    }

    private void generateFindMethodParameterCheckingBlock(JavaCodeFragment methodBody,
            String methodName,
            String[] parameterNames,
            String keyClassName,
            boolean useNullValueRow) throws CoreException {
        if (parameterNames.length > 0) {
            methodBody.appendln("if (");

            for (int i = 0; i < parameterNames.length; i++) {
                methodBody.append(parameterNames[i]);
                methodBody.append(" == null");
                if (i < parameterNames.length - 1) {
                    methodBody.append(" || ");
                }
            }
            methodBody.append(")");
            methodBody.appendOpenBracket();
            if (useNullValueRow) {
                generateMethodExitingLoggingStmt(
                        methodBody,
                        keyClassName,
                        methodName,
                        tableRowBuilder.getQualifiedClassName(getIpsSrcFile()) + '.'
                                + tableRowBuilder.getFieldNameForNullRow());
                methodBody.append("return ");
                methodBody.appendClassName(tableRowBuilder.getQualifiedClassName(getIpsSrcFile()));
                methodBody.append('.');
                methodBody.append(tableRowBuilder.getFieldNameForNullRow());
                methodBody.append(';');
                methodBody.appendln();
            } else {
                generateMethodExitingLoggingStmt(methodBody, keyClassName, methodName, "null");
                methodBody.appendln("return null;");
            }
            methodBody.appendCloseBracket();
        }
    }

    private void createFindMethod(String methodName,
            String returnTypeName,
            String[] parameterTypes,
            String[] parameterNames,
            String[] keyClassParameterNames,
            String combinedKeyName,
            String keyClassName,
            IUniqueKey key,
            JavaCodeFragmentBuilder codeBuilder,
            boolean useNullValueRow) throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();

        // logging
        generateMethodEnteringLoggingStmt(methodBody, keyClassName, methodName, parameterNames);

        generateFindMethodParameterCheckingBlock(methodBody, methodName, parameterNames, keyClassName, useNullValueRow);

        String mapName = StringUtils.uncapitalize(combinedKeyName) + "Map";
        String treeName = StringUtils.uncapitalize(combinedKeyName) + "Tree";
        String returnVariableName = "returnValue";
        if (key.containsColumns()) {
            if (key.containsRanges()) {
                String[] rangeParameterNames = new String[parameterNames.length - keyClassParameterNames.length];
                System.arraycopy(parameterNames, keyClassParameterNames.length, rangeParameterNames, 0,
                        rangeParameterNames.length);
                generateReturnFindMethodReturnStmt(
                        methodBody,
                        returnVariableName,
                        keyClassName,
                        methodName,
                        createFindMethodGetValueFrag(false, mapName, keyClassName, returnTypeName, returnVariableName,
                                keyClassParameterNames, rangeParameterNames), useNullValueRow);
            } else {
                generateReturnFindMethodReturnStmt(
                        methodBody,
                        returnVariableName,
                        keyClassName,
                        methodName,
                        createFindMethodGetMapEntryFrag(mapName, keyClassName, returnTypeName, returnVariableName,
                                keyClassParameterNames), useNullValueRow);
            }
        } else {
            generateReturnFindMethodReturnStmt(
                    methodBody,
                    returnVariableName,
                    keyClassName,
                    methodName,
                    createFindMethodGetValueFrag(true, treeName, keyClassName, returnTypeName, returnVariableName,
                            keyClassParameterNames, parameterNames), useNullValueRow);
        }
        methodBody.appendln(';');
        String javaDoc = getLocalizedText(getIpsObject(), useNullValueRow ? FIND_RETURN_NULL_ROW_JAVADOC : FIND_JAVADOC);
        codeBuilder.method(Modifier.PUBLIC, returnTypeName, methodName, parameterNames, parameterTypes, methodBody,
                javaDoc, ANNOTATION_GENERATED);
    }

    private void generateReturnFindMethodReturnStmt(JavaCodeFragment methodBody,
            String returnVariableName,
            String keyClassName,
            String methodName,
            JavaCodeFragment getValueFrag,
            boolean useNullValueRow) throws CoreException {

        methodBody.append(getValueFrag);
        methodBody.appendln(';');
        generateMethodExitingLoggingStmt(methodBody, keyClassName, methodName, returnVariableName);

        if (useNullValueRow) {
            methodBody.append("if(");
            methodBody.append(returnVariableName);
            methodBody.append(" == null)");
            methodBody.appendOpenBracket();
            methodBody.append("return ");
            methodBody.appendClassName(tableRowBuilder.getQualifiedClassName(getIpsSrcFile()));
            methodBody.append('.');
            methodBody.append(tableRowBuilder.getFieldNameForNullRow());
            methodBody.append(";");
            methodBody.appendln();
            methodBody.appendCloseBracket();
        }
        methodBody.append("return ");
        methodBody.append(returnVariableName);
        methodBody.appendln();
    }

    private JavaCodeFragment createFindMethodGetValueFrag(boolean rangesOnly,
            String fieldName,
            String keyClassName,
            String returnTypeName,
            String returnVariable,
            String[] keyClassParameterNames,
            String[] parameterNames) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(returnTypeName);
        fragment.append(' ');
        fragment.append(returnVariable);
        fragment.append(" = ");
        fragment.append(" (");
        fragment.appendClassName(returnTypeName);
        fragment.append(") ");
        fragment.append("getValue(");
        fragment.append(fieldName);
        if (!rangesOnly) {
            fragment.append(", ");
            fragment.append(createKeyInstantiation(keyClassName, keyClassParameterNames));
        }
        fragment.append(", new ");
        fragment.appendClassName(Comparable.class);
        fragment.append("[] {");
        for (int i = 0; i < parameterNames.length; i++) {
            if (i != 0) {
                fragment.append(", ");
            }
            fragment.append(parameterNames[i]);
        }
        fragment.append("})");
        return fragment;
    }

    private JavaCodeFragment createFindMethodGetMapEntryFrag(String mapName,
            String keyClassName,
            String returnTypeName,
            String returnVariable,
            String[] parameterNames) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(returnTypeName);
        fragment.append(' ');
        fragment.append(returnVariable);
        fragment.append(" = ");
        if (!isUseTypesafeCollections()) {
            fragment.append(" (");
            fragment.appendClassName(returnTypeName);
            fragment.append(") ");
        }

        fragment.append(mapName);
        fragment.append(".get(");
        fragment.append(createKeyInstantiation(keyClassName, parameterNames));
        fragment.append(")");
        return fragment;
    }

    // TODO this would be a method for the java code fragment builder
    private JavaCodeFragment createKeyInstantiation(String keyClassName, String[] parameterNames) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.append(keyClassName);
        fragment.append("(");
        for (int i = 0; i < parameterNames.length; i++) {
            if (i != 0) {
                fragment.append(", ");
            }
            fragment.append(parameterNames[i]);
        }
        fragment.append(")");
        return fragment;
    }

    private Datatype getDatatypeForKeyName(String keyName) throws CoreException {
        IColumn column = getTableStructure().getColumn(keyName);
        if (column != null) {
            return findDatatype(column.getDatatype());
        }
        IColumnRange range = getTableStructure().getRange(keyName);
        if (range != null) {
            return findDatatype(range.getDatatype());
        }
        throw new RuntimeException("Unable to find column or columnRange for keyItemName: " + keyName);
    }

    private String getJavaClassName(String keyName) throws CoreException {
        return getDatatypeForKeyName(keyName).getJavaClassName();
    }

    private Datatype findDatatype(String name) throws CoreException {
        if (fDatatypes == null) {
            Datatype[] datatypes = getTableStructure().getIpsProject().findDatatypes(false, true);
            fDatatypes = new HashMap<String, Datatype>(datatypes.length);
            for (Datatype datatype : datatypes) {
                fDatatypes.put(datatype.getQualifiedName(), datatype);
            }
        }
        return fDatatypes.get(name);
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {

    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

}
