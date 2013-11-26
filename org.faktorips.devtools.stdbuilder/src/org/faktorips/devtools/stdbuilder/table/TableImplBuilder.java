/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.MultiMap;
import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree;
import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree.KeyType;
import org.faktorips.runtime.internal.indexstructure.TwoColumnKey;
import org.faktorips.runtime.internal.Table;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Important: This builder expects from the <code>IJavaPackageStructure</code> the qualified class
 * name for a table row class. It uses the kindId="tablerow" to get this name.
 * 
 * @author Peter Erzberger
 */
public class TableImplBuilder extends DefaultJavaSourceFileBuilder {

    private static final String GET_ALL_ROWS_JAVADOC = "TABLE_IMPL_BUILDER_GET_ALL_ROWS_JAVADOC";
    private static final String GET_INSTANCE_JAVADOC = "TABLE_IMPL_BUILDER_GET_INSTANCE_JAVADOC";
    private static final String KEY_CLASS_EQUALS_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_EQUALS_JAVADOC";
    private static final String KEY_CLASS_HASHCODE_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_HASHCODE_JAVADOC";
    private static final String FIND_JAVADOC = "TABLE_IMPL_BUILDER_FIND_JAVADOC";
    private static final String FIND_RETURN_NULL_ROW_JAVADOC = "TABLE_IMPL_BUILDER_FIND_RETURN_NULL_ROW_JAVADOC";
    private static final String KEY_CLASS_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_JAVADOC";
    private static final String KEY_CLASS_CONSTRUCTOR_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_CONSTRUCTOR_JAVADOC";
    private static final String ADD_ROW_JAVADOC = "TABLE_IMPL_BUILDER_ADD_ROW_JAVADOC";
    private static final String INIT_KEY_MAPS_JAVADOC = "TABLE_IMPL_BUILDER_INIT_KEY_MAPS_JAVADOC";
    private static final String FIND_EXISTING_ROW_EXCEPTION_MESSAGE = "FIND_EXISTING_ROW_EXCEPTION_MESSAGE";
    private static final String FIND_EXISTING_ROW_JAVADOC = "TABLE_IMPL_BUILDER_FIND_EXISTING_ROW_JAVADOC";
    private static final String METHOD_NAME_SUFFIX_FOR_NULL_VALUE_ROW = "NullRowReturnedForEmtpyResult";
    private static final String METHOD_NAME_FIND = "find";
    private static final String METHOD_NAME_EXISTING = "Existing";
    private static final String METHOD_NAME_ROW = "Row";
    private static final String METHOD_NAME_FIND_ROW = METHOD_NAME_FIND + METHOD_NAME_ROW;

    private static final String INIT_KEY_MAPS = "initKeyMaps";

    private TableRowBuilder tableRowBuilder;

    private String qualifiedTableRowName;

    private IndexCodePartMap indexCodeParts;

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
        indexCodeParts = new IndexCodePartMap();
        initClassVariables();
        buildKeyVariableNames();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
        super.afterBuild(ipsSrcFile);
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

    private Set<IIndex> getIndicesForKeysWithSameDatatypeSequence() throws CoreException {
        List<IIndex> keys = getIndices();
        Set<IIndex> sameDatatype = new HashSet<IIndex>();
        int i = 0;
        for (IIndex key : keys) {
            for (int j = i + 1; j < keys.size(); j++) {
                IIndex key2 = keys.get(j);
                if (compareByDatatypeOnly(key, key2)) {
                    sameDatatype.add(key);
                    sameDatatype.add(key2);
                }
            }
            i++;
        }
        return sameDatatype;
    }

    private void buildKeyVariableNames() {
        List<IIndex> keys = getIndices();
        for (int i = 0; i < keys.size(); i++) {
            IIndex index = keys.get(i);
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            String keyVariableName = "Key" + i;
            indexCodePart.keyMapFieldName = StringUtils.uncapitalize(keyVariableName);
            if (index.containsColumns()) {
                indexCodePart.keyMapFieldName += "Map";
                indexCodePart.keyMapTempName = indexCodePart.keyMapFieldName;
                if (index.containsRanges()) {
                    indexCodePart.keyMapTempName += "Temp";
                }
                if (index.isUniqueKey() || index.containsRanges()) {
                    indexCodePart.keyMapFieldClassName = Map.class.getName();
                } else {
                    indexCodePart.keyMapFieldClassName = MultiMap.class.getName();
                }
            } else {
                indexCodePart.keyMapTempName = indexCodePart.keyMapFieldName + "MapTemp";
                indexCodePart.keyMapFieldName += "Tree";
                indexCodePart.keyMapFieldClassName = ReadOnlyBinaryRangeTree.class.getName();
            }
            if (index.isUniqueKey() || index.containsRanges()) {
                indexCodePart.keyMapTempClassName = Map.class.getName();
            } else {
                indexCodePart.keyMapTempClassName = MultiMap.class.getName();
            }
            String generics = getGenerics(index);
            indexCodePart.keyMapFieldClassName += generics;
            indexCodePart.keyMapTempClassName += generics;
        }
    }

    private String getGenerics(IIndex index) {
        IndexCodePart indexCodePart = indexCodeParts.get(index);
        String generics = "<" + indexCodePart.keyClassName + ", ";
        IKeyItem[] keyItems = index.getKeyItems();
        int nestedGeneric = 1;
        // starting with index 1 because first keyItem already added
        for (int i = 1; i < keyItems.length; i++) {
            IKeyItem keyItem = keyItems[i];
            if (!getTableStructure().hasColumn(keyItem.getName())) {
                generics += ReadOnlyBinaryRangeTree.class.getName() + "<" + getJavaClassName(keyItem.getName()) + ", ";
                nestedGeneric++;
            }
        }
        if (!index.isUniqueKey() && index.containsRanges()) {
            generics += Set.class.getName() + "<";
            nestedGeneric++;
        }
        generics += qualifiedTableRowName;
        for (int i = 0; i < nestedGeneric; i++) {
            generics += ">";
        }
        return generics;
    }

    private void buildFindMethodNames() throws CoreException {
        List<IIndex> keys = getIndices();
        Set<IIndex> keysWithSameDatatypeSequence = getIndicesForKeysWithSameDatatypeSequence();
        for (IIndex index : keys) {
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            if (keysWithSameDatatypeSequence.contains(index)) {
                StringBuffer keyName = new StringBuffer();
                String[] keyItems = index.getKeyItemNames();
                keyName.append("By");
                for (String keyItem : keyItems) {
                    if (getTableStructure().hasColumn(keyItem)) {
                        keyName.append(StringUtils.capitalize(keyItem));
                    } else {
                        IColumnRange range = getTableStructure().getRange(keyItem);
                        keyName.append(StringUtils.capitalize(range.getParameterName()));
                    }
                }
                indexCodePart.findMethodNameSuffix = keyName.toString();
            } else {
                indexCodePart.findMethodNameSuffix = "";
            }
        }
    }

    private boolean compareByDatatypeOnly(IIndex first, IIndex second) throws CoreException {

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
        if (!checkIndexValidity()) {
            return;
        }
        List<IIndex> keys = getIndices();
        int keySize = keys.size();
        fRanges = new HashMap<String, IColumnRange>(keySize);
        for (int i = 0; i < keySize; i++) {
            IIndex index = keys.get(i);
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            String[] keyItems = index.getKeyItemNames();
            List<String> parameters = new ArrayList<String>();
            List<String> allParameterTypes = new ArrayList<String>();
            indexCodePart.keyClassParameterNames = new ArrayList<String>();
            indexCodePart.keyClassParameterTypes = new ArrayList<String>();
            boolean keyHasColumn = index.containsColumns();
            if (keyHasColumn) {
                indexCodePart.keyClassName = "Index" + i;
            } else {
                indexCodePart.keyClassName = getJavaClassName(keyItems[0]);
            }
            for (String keyItem : keyItems) {
                allParameterTypes.add(getJavaClassName(keyItem));

                if (getTableStructure().hasColumn(keyItem)) {
                    indexCodePart.keyClassParameterNames.add(StringUtils.uncapitalize(keyItem));
                    indexCodePart.keyClassParameterTypes.add(getJavaClassName(keyItem));
                    parameters.add(StringUtils.uncapitalize(keyItem));
                } else {
                    IColumnRange range = getTableStructure().getRange(keyItem);
                    parameters.add(range.getParameterName());
                    if (!keyHasColumn && range.getColumnRangeType().isTwoColumn()) {
                        indexCodePart.keyClassName = TwoColumnKey.class.getName() + "<" + getJavaClassName(keyItem)
                                + ">";
                    }
                    fRanges.put(range.getParameterName(), range);
                }
            }

            indexCodePart.allItemParameterTypes = allParameterTypes;
            indexCodePart.allItemParameterNames = parameters;
        }
    }

    private boolean checkIndexValidity() throws CoreException {
        List<IIndex> keys = getTableStructure().getIndices();
        for (IIndex key : keys) {
            if (!key.isValid(getIpsProject())) {
                return false;
            }
            // the key is valid
            // additional check if the column inside the keys are valid
            IKeyItem[] keyItems = key.getKeyItems();
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
        mainSection.setSuperClass(isUseTypesafeCollections() ? Table.class.getName() + "<" + qualifiedTableRowName
                + ">" : Table.class.getName());
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
        if (isUseTypesafeCollections()) {
            code.append('<');
            code.appendClassName(qualifiedTableRowName);
            code.append('>');
        }
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
                    ANNOTATION_GENERATED, null);
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
        methodBody.append("return repository.getTable(");
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
            methodBody.append(" = ");
            methodBody.append("values.get(");
            methodBody.append(i);
            methodBody.append(");");
            IColumn column = columns[i];
            Datatype columnDatatype = findDatatype(column.getDatatype(), column.getIpsProject());
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
                    methodBody.append(helper.newInstanceFromExpression(valueName, false));
                }
            } else {
                methodBody.append(helper.newInstanceFromExpression(valueName, false));
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
        codeBuilder.methodBegin(Modifier.PROTECTED, Void.TYPE.getName(), "addRow", new String[] { "values",
                "productRepository" },
                new String[] {
                        isUseTypesafeCollections() ? List.class.getName() + "<" + String.class.getName() + ">"
                                : List.class.getName(), IRuntimeRepository.class.getName() });
        codeBuilder.append(methodBody);
        codeBuilder.methodEnd();
    }

    private boolean isUseTypesafeCollections() {
        return ((StandardBuilderSet)getBuilderSet()).isUseTypesafeCollections();
    }

    private void createFindMethods(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        if (!checkIndexValidity()) {
            return;
        }
        List<IIndex> keys = getIndices();
        buildFindMethodNames();
        for (IIndex index : keys) {
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            createFindMethodRegular(indexCodePart.findMethodNameSuffix, indexCodePart.allItemParameterTypes,
                    indexCodePart.allItemParameterNames, indexCodePart.keyClassParameterNames, index, codeBuilder);
            if (index.isUniqueKey()) {
                createFindMethodWithNullValueRow(indexCodePart.findMethodNameSuffix,
                        indexCodePart.allItemParameterTypes, indexCodePart.allItemParameterNames,
                        indexCodePart.keyClassParameterNames, index, codeBuilder);
                createFindMethodExistingRow(indexCodePart.findMethodNameSuffix, indexCodePart.allItemParameterTypes,
                        indexCodePart.allItemParameterNames, index, codeBuilder);
            }
        }
    }

    private List<IIndex> getIndices() {
        return getTableStructure().getIndices();
    }

    private void createFields(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        if (!checkIndexValidity()) {
            return;
        }

        for (IIndex index : getIndices()) {
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            appendLocalizedJavaDoc("FIELD_KEY_MAP", getTableStructure(), codeBuilder);
            codeBuilder.varDeclaration(Modifier.PRIVATE, indexCodePart.keyMapFieldClassName,
                    indexCodePart.keyMapFieldName.toString());
        }
    }

    private JavaCodeFragment buildAddKeyFragment(String keyMapFieldName, String keyClassName, List<String> keyItems) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append(keyMapFieldName);
        methodBody.append(".put(new ");
        methodBody.append(keyClassName);
        methodBody.append("(");
        for (int i = 0; i < keyItems.size(); i++) {
            if (i != 0) {
                methodBody.append(", ");
            }
            methodBody.append("row.get");
            methodBody.append(StringUtils.capitalize(keyItems.get(i)));
            methodBody.append("()");
        }
        methodBody.append("), row);");
        return methodBody;
    }

    private void createInitKeyMapsMethod(JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        if (!checkIndexValidity()) {
            return;
        }

        List<IIndex> keys = getIndices();
        JavaCodeFragment methodBody = new JavaCodeFragment();
        if (!keys.isEmpty()) {
            methodBody = createInitKeyMapsMethodBody(keys);
        }
        codeBuilder.javaDoc(getLocalizedText(getIpsObject(), INIT_KEY_MAPS_JAVADOC), ANNOTATION_GENERATED);
        appendOverrideAnnotation(codeBuilder, false);
        if (methodBody.getSourcecode().contains("getMap")) {
            codeBuilder.annotation(new String[] { ANNOTATION_SUPPRESS_WARNINGS_UNCHECKED });
        }
        codeBuilder.methodBegin(Modifier.PROTECTED, Void.TYPE, INIT_KEY_MAPS, new String[0], new Class[0]);
        codeBuilder.append(methodBody);
        codeBuilder.methodEnd();
    }

    private JavaCodeFragment createInitKeyMapsMethodBody(List<IIndex> keys) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        createKeyMapInitialization(keys, methodBody);
        createForLoop(keys, methodBody);
        createInitKeyMapsFieldAssignments(keys, methodBody);
        return methodBody;
    }

    private void createKeyMapInitialization(List<IIndex> keys, JavaCodeFragment methodBody) {
        for (IIndex index : keys) {
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            List<String> keyClassName = indexCodePart.getKeyClassNamesForMap();
            methodBody.append(createInitKeyMapsVariables(index, indexCodePart.keyMapTempName, keyClassName));
            methodBody.appendln();
        }
    }

    private void createForLoop(List<IIndex> keys, JavaCodeFragment methodBody) {
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
        createForLoopBody(keys, methodBody);
        methodBody.appendCloseBracket();
    }

    private void createForLoopBody(List<IIndex> keys, JavaCodeFragment methodBody) {
        for (IIndex index : keys) {
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            if (index.containsRanges()) {
                List<String> getMapFirstParameter = new ArrayList<String>();
                List<String> getMapSecondParameter = new ArrayList<String>();
                List<String> getMapThirdParameter = new ArrayList<String>();
                getMapFirstParameter.add(indexCodePart.keyMapTempName);
                if (index.containsColumns()) {
                    getMapFirstParameter.add(StringUtils.uncapitalize(indexCodePart.getRangeParameterNames().get(0))
                            + "Map");
                    getMapSecondParameter.add(createKeyInstantiation(indexCodePart.keyClassName,
                            createInitKeyMapsKeyClassParameters(indexCodePart.keyClassParameterNames)).toString());
                    getMapThirdParameter.add(null);
                }
                adaptNummberOfColumns(index, getMapFirstParameter, getMapSecondParameter, getMapThirdParameter);
                List<String> allKeyClassNamesForMap = indexCodePart.getKeyClassNamesForMap();
                for (int j = 1; j < getMapFirstParameter.size(); j++) {
                    String currentKeyClassName = allKeyClassNamesForMap.get(j - 1);
                    List<String> followingKeyClassNames = allKeyClassNamesForMap.subList(j,
                            allKeyClassNamesForMap.size());
                    methodBody.append(createInitKeyMapsMapAssignment(index, currentKeyClassName,
                            followingKeyClassNames, getMapFirstParameter.get(j), getMapFirstParameter.get(j - 1),
                            getMapSecondParameter.get(j - 1), getMapThirdParameter.get(j - 1)));
                    methodBody.appendln();
                }
                methodBody.append(createInitKeyMapsPutStatement(
                        getMapFirstParameter.get(getMapFirstParameter.size() - 1),
                        getMapSecondParameter.get(getMapSecondParameter.size() - 1),
                        getMapThirdParameter.get(getMapThirdParameter.size() - 1),
                        allKeyClassNamesForMap.get(allKeyClassNamesForMap.size() - 1)));
                methodBody.appendln();
                continue;
            }
            methodBody.append(buildAddKeyFragment(indexCodePart.keyMapFieldName, indexCodePart.keyClassName,
                    indexCodePart.keyClassParameterNames));
        }
    }

    private void adaptNummberOfColumns(IIndex index,
            List<String> mapFirstParameter,
            List<String> mapSecondParameter,
            List<String> mapThirdParameter) {
        boolean first = true;
        for (IKeyItem keyItem : index.getKeyItems()) {
            if (keyItem.isRange()) {
                IColumnRange range = (IColumnRange)keyItem;
                if (!first) {
                    mapFirstParameter.add(StringUtils.uncapitalize(range.getParameterName()) + "Map");
                } else {
                    first = false;
                }
                if (range.getColumnRangeType().isTwoColumn()) {
                    mapSecondParameter.add("row.get" + StringUtils.capitalize(range.getFromColumn()) + "()");
                    mapThirdParameter.add("row.get" + StringUtils.capitalize(range.getToColumn()) + "()");
                } else {
                    String paramName = range.getColumnRangeType().isOneColumnFrom() ? range.getFromColumn() : range
                            .getToColumn();
                    mapSecondParameter.add("row.get" + StringUtils.capitalize(paramName) + "()");
                    mapThirdParameter.add(null);
                }
            }
        }
    }

    private JavaCodeFragment createInitKeyMapsPutStatement(String mapName,
            String key,
            String secondKey,
            String keyClassName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(mapName);
        fragment.append(".put(");
        if (secondKey != null) {
            fragment.append("new ");
            fragment.appendClassName(keyClassName);
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

    private void createInitKeyMapsFieldAssignments(List<IIndex> keys, JavaCodeFragment methodBody) {
        for (IIndex index : keys) {
            if (index.containsRanges()) {
                IndexCodePart indexCodePart = indexCodeParts.get(index);
                methodBody.append(indexCodePart.keyMapFieldName);
                methodBody.append(" = ");
                methodBody.append(index.containsColumns() ? "convert" : "generateTree");
                methodBody.append("(");
                methodBody.append(indexCodePart.keyMapTempName);
                methodBody.append(", new ");
                methodBody.appendClassName(Java5ClassNames.ReadOnlyBinaryRangeTreeKeyType_QualifiedName);
                methodBody.append("[] {");
                String[] keyItemNames = index.getKeyItemNames();
                createTreeTypesParameter(methodBody, keyItemNames);
                methodBody.append("});");
            }
        }
    }

    private void createTreeTypesParameter(JavaCodeFragment fragment, String[] keyItemNames) {
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
            fragment.appendClassName(KeyType.class);
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
    }

    private JavaCodeFragment createInitKeyMapsVariables(IIndex index,
            String keyMapVariableName,
            List<String> keyClassNames) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        if (index.containsRanges()) {
            appendMapClassWithGenerics(keyClassNames, methodBody, false, index.isUniqueKey());
            methodBody.append(' ');
            methodBody.append(keyMapVariableName);
        } else {
            methodBody.append(keyMapVariableName);
        }
        methodBody.append(" = new ");
        appendMapClassWithGenerics(keyClassNames, methodBody, true, index.isUniqueKey());
        methodBody.append("(rows.size());");
        return methodBody;
    }

    void appendMapClassWithGenerics(List<String> keyClassNames,
            JavaCodeFragment methodBody,
            boolean implementation,
            boolean unique) {
        if (keyClassNames.isEmpty()) {
            methodBody.appendClassName(qualifiedTableRowName);
        } else {
            if (implementation) {
                if (keyClassNames.size() > 1) {
                    methodBody.appendClassName(Table.MapOfMaps.class);
                } else {
                    if (unique) {
                        methodBody.appendClassName(HashMap.class);
                    } else {
                        methodBody.appendClassName(MultiMap.class);
                    }
                }
            } else {
                if (unique || keyClassNames.size() > 1) {
                    methodBody.appendClassName(Map.class);
                } else {
                    methodBody.appendClassName(MultiMap.class);
                }
            }
            methodBody.append("<");
            methodBody.appendClassName(keyClassNames.get(0));
            methodBody.append(", ");
            if (implementation && keyClassNames.size() > 1) {
                methodBody.appendClassName(keyClassNames.get(1));
                methodBody.append(", ");
                appendMapClassWithGenerics(keyClassNames.subList(2, keyClassNames.size()), methodBody, false, unique);
            } else {
                appendMapClassWithGenerics(keyClassNames.subList(1, keyClassNames.size()), methodBody, false, unique);
            }
            methodBody.append(">");
        }
    }

    private JavaCodeFragment createInitKeyMapsMapAssignment(IIndex index,
            String currentKeyClassName,
            List<String> keyClassNames,
            String returnedMapName,
            String fromMapName,
            String keyName,
            String secondKeyName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        appendMapClassWithGenerics(keyClassNames, fragment, false, index.isUniqueKey());
        fragment.append(' ');
        fragment.append(returnedMapName);
        fragment.append(" = ");
        fragment.append(fromMapName);
        fragment.append(".get(");
        if (secondKeyName != null) {
            fragment.append("new ").appendClassName(currentKeyClassName).append("(");
            fragment.append(keyName);
            fragment.append(", ");
            fragment.append(secondKeyName);
            fragment.append(")");
        } else {
            fragment.append(keyName);
        }
        fragment.append(");");
        return fragment;
    }

    private List<String> createInitKeyMapsKeyClassParameters(List<String> keyClassParameterNames) {
        List<String> returnValue = new ArrayList<String>();
        for (String keyClassParameterName : keyClassParameterNames) {
            returnValue.add("row.get" + StringUtils.capitalize(keyClassParameterName) + "()");
        }
        return returnValue;
    }

    private void createHashKeyClasses() throws CoreException {
        if (!checkIndexValidity()) {
            return;
        }
        List<IIndex> keys = getIndices();
        for (IIndex index : keys) {
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            if (!indexCodePart.keyClassParameterTypes.isEmpty()) {
                createHashKeyClass(indexCodePart.keyClassName, indexCodePart.keyClassParameterNames,
                        indexCodePart.keyClassParameterTypes);
            }
        }
    }

    private void createHashKeyClass(String hashKeyClassName,
            List<String> keyClassParameterNames,
            List<String> keyClassParameterTypes) {
        TypeSection innerClassBody = createInnerClassSection();
        innerClassBody.getJavaDocForTypeBuilder().javaDoc(getLocalizedText(getIpsObject(), KEY_CLASS_JAVADOC),
                ANNOTATION_GENERATED);
        innerClassBody.setClassModifier(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        innerClassBody.setUnqualifiedName(hashKeyClassName);
        JavaCodeFragmentBuilder memberVarBuilder = innerClassBody.getMemberVarBuilder();
        for (int i = 0; i < keyClassParameterNames.size(); i++) {
            memberVarBuilder.javaDoc("", ANNOTATION_GENERATED);
            memberVarBuilder.varDeclaration(Modifier.PRIVATE | Modifier.FINAL, keyClassParameterTypes.get(i),
                    keyClassParameterNames.get(i));
            memberVarBuilder.appendln();
        }
        // create hashCode field
        memberVarBuilder.javaDoc("Cached hashcode.", ANNOTATION_GENERATED);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE | Modifier.FINAL, Integer.TYPE, "hashCode");
        memberVarBuilder.appendln();

        JavaCodeFragment constructorBody = new JavaCodeFragment();
        for (String keyName : keyClassParameterNames) {
            constructorBody.append("this.");
            constructorBody.append(keyName);
            constructorBody.append(" = ");
            constructorBody.append(keyName);
            constructorBody.append(';');
            constructorBody.appendln();
        }
        constructorBody.appendln("hashCode = calculateHashCode();");
        // constructor
        innerClassBody.getConstructorBuilder().method(Modifier.PRIVATE, null, hashKeyClassName,
                toArray(keyClassParameterNames), toArray(keyClassParameterTypes), constructorBody,
                getLocalizedText(getIpsObject(), KEY_CLASS_CONSTRUCTOR_JAVADOC), ANNOTATION_GENERATED);
        createKeyClassCalHashCodeMethod(keyClassParameterNames, innerClassBody.getMethodBuilder());
        createKeyClassEqualsMethod(hashKeyClassName, keyClassParameterNames, innerClassBody.getMethodBuilder());
        createKeyClassHashCodeMethod(innerClassBody.getMethodBuilder());
    }

    private void createKeyClassCalHashCodeMethod(List<String> keyClassParameterNames,
            JavaCodeFragmentBuilder codeBuilder) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.appendln("int result = 17;");
        for (String keyName : keyClassParameterNames) {
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
            List<String> keyClassParameterNames,
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
        boolean first = true;
        for (String keyClassParameterName : keyClassParameterNames) {
            if (!first) {
                methodBody.append(" && ");
            } else {
                first = false;
            }
            methodBody.append(keyClassParameterName);
            methodBody.append(".equals(other.");
            methodBody.append(keyClassParameterName);
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

    private void createFindMethodExistingRow(String methodNameSuffix,
            List<String> allItemParameterTypes,
            List<String> allItemNamesAsParameters,
            IIndex index,
            JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        JavaCodeFragment methodBody = createMethodBodyForFindMethodExistingRow(methodNameSuffix,
                allItemNamesAsParameters);
        String javaDoc = getLocalizedText(getIpsProject(), FIND_EXISTING_ROW_JAVADOC);
        codeBuilder.method(Modifier.PUBLIC, getFinderMethodReturnType(index),
                getMethodNameFindExistingRow(methodNameSuffix), toArray(allItemNamesAsParameters),
                toArray(allItemParameterTypes), methodBody, javaDoc, ANNOTATION_GENERATED);
    }

    private JavaCodeFragment createMethodBodyForFindMethodExistingRow(String methodNameSuffix,
            List<String> parameterNames) throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        createRowInitializationCode(methodNameSuffix, parameterNames, methodBody);
        createIfConditionCode(parameterNames, methodBody);
        createElseConditionCode(methodBody);
        return methodBody;
    }

    private void createRowInitializationCode(String methodNameSuffix,
            List<String> parameterNames,
            JavaCodeFragment methodBody) {
        String nullRowReturnedMethodName = getMethodNameFindRowNullRowReturned(methodNameSuffix);
        methodBody.appendClassName(qualifiedTableRowName);
        methodBody.append(" row = ");
        methodBody.append(nullRowReturnedMethodName + "(");
        for (int i = 0; i < parameterNames.size(); i++) {
            methodBody.append(parameterNames.get(i));
            if (i < parameterNames.size() - 1) {
                methodBody.append(", ");
            }
        }
        methodBody.append(");");
    }

    private void createIfConditionCode(List<String> parameterNames, JavaCodeFragment methodBody) throws CoreException {
        methodBody.appendln();
        methodBody.append("if(row ==");
        methodBody.appendClassName(qualifiedTableRowName);
        methodBody.append(".");
        methodBody.append(tableRowBuilder.getFieldNameForNullRow());
        methodBody.append(")");
        methodBody.appendOpenBracket();
        methodBody.append("throw new ");
        methodBody.appendClassName(IllegalArgumentException.class);
        methodBody.append("(\"");
        methodBody.append(getExceptionMessage());
        methodBody.append(" ");
        for (int i = 0; i < parameterNames.size(); i++) {
            methodBody.append(parameterNames.get(i));
            methodBody.append(" = \" + ");
            methodBody.append(parameterNames.get(i));
            if (i < parameterNames.size() - 1) {
                methodBody.append(" + ");
                methodBody.append("\", ");
            }
        }
        methodBody.append(");");
        methodBody.appendCloseBracket();
    }

    private String getExceptionMessage() throws CoreException {
        return NLS.bind(getLocalizedText(getIpsProject(), FIND_EXISTING_ROW_EXCEPTION_MESSAGE),
                getUnqualifiedClassName());
    }

    private void createElseConditionCode(JavaCodeFragment methodBody) {
        methodBody.append("else");
        methodBody.appendOpenBracket();
        methodBody.appendln("return row;");
        methodBody.appendCloseBracket();
    }

    private String getMethodNameFindRow(String methodNameSuffix) {
        return METHOD_NAME_FIND_ROW + methodNameSuffix;
    }

    private String getMethodNameFindRowNullRowReturned(String methodNameSuffix) {
        return METHOD_NAME_FIND_ROW + methodNameSuffix + METHOD_NAME_SUFFIX_FOR_NULL_VALUE_ROW;
    }

    private String getMethodNameFindExistingRow(String methodNameSuffix) {
        return METHOD_NAME_FIND + METHOD_NAME_EXISTING + METHOD_NAME_ROW + methodNameSuffix;
    }

    private void createFindMethodWithNullValueRow(String methodNameSuffix,
            List<String> allItemParameterTypes,
            List<String> allItemNamesAsParameters,
            List<String> keyClassParameterNames,
            IIndex index,
            JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        String methodName = getMethodNameFindRowNullRowReturned(methodNameSuffix);
        JavaCodeFragment methodBody = createFindMethodBody(methodName, allItemNamesAsParameters,
                keyClassParameterNames, index, true);
        String javaDoc = getLocalizedText(getIpsObject(), FIND_RETURN_NULL_ROW_JAVADOC);
        codeBuilder.method(Modifier.PUBLIC, getFinderMethodReturnType(index), methodName,
                toArray(allItemNamesAsParameters), toArray(allItemParameterTypes), methodBody, javaDoc,
                ANNOTATION_GENERATED);
    }

    private void createFindMethodRegular(String methodNameSuffix,
            List<String> parameterTypes,
            List<String> parameterNames,
            List<String> keyClassParameterNames,
            IIndex index,
            JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        String methodName = getMethodNameFindRow(methodNameSuffix);
        JavaCodeFragment methodBody = createFindMethodBody(methodName, parameterNames, keyClassParameterNames, index,
                false);
        String javaDoc = getLocalizedText(getIpsObject(), FIND_JAVADOC);
        codeBuilder.method(Modifier.PUBLIC, getFinderMethodReturnType(index), methodName, toArray(parameterNames),
                toArray(parameterTypes), methodBody, javaDoc, ANNOTATION_GENERATED);
    }

    private String getFinderMethodReturnType(IIndex index) {
        if (index.isUniqueKey()) {
            return qualifiedTableRowName;
        } else {
            return Set.class.getName() + "<" + qualifiedTableRowName + ">";
        }
    }

    private JavaCodeFragment createFindMethodBody(String methodName,
            List<String> parameterNames,
            List<String> keyClassParameterNames,
            IIndex index,
            boolean useNullValueRow) throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        IndexCodePart indexCodePart = indexCodeParts.get(index);

        // logging
        generateMethodEnteringLoggingStmt(methodBody, indexCodePart.keyClassName, methodName, toArray(parameterNames));

        generateFindMethodParameterCheckingBlock(methodBody, methodName, parameterNames, indexCodePart.keyClassName,
                useNullValueRow);

        String returnVariableName = "returnValue";
        String returnType = getFinderMethodReturnType(index);
        if (index.containsColumns()) {
            if (index.containsRanges()) {
                List<String> rangeParameterNames = indexCodePart.getRangeParameterNames();
                generateReturnFindMethodReturnStmt(
                        methodBody,
                        returnVariableName,
                        indexCodePart.keyClassName,
                        methodName,
                        createFindMethodGetValueFrag(false, indexCodePart.keyMapFieldName, indexCodePart.keyClassName,
                                returnType, returnVariableName, keyClassParameterNames, rangeParameterNames),
                        useNullValueRow);
            } else {
                generateReturnFindMethodReturnStmt(
                        methodBody,
                        returnVariableName,
                        indexCodePart.keyClassName,
                        methodName,
                        createFindMethodGetMapEntryFrag(indexCodePart.keyMapFieldName, indexCodePart.keyClassName,
                                returnType, returnVariableName, keyClassParameterNames), useNullValueRow);
            }
        } else {
            generateReturnFindMethodReturnStmt(
                    methodBody,
                    returnVariableName,
                    indexCodePart.keyClassName,
                    methodName,
                    createFindMethodGetValueFrag(true, indexCodePart.keyMapFieldName, indexCodePart.keyClassName,
                            returnType, returnVariableName, keyClassParameterNames, parameterNames), useNullValueRow);
        }
        methodBody.appendln(';');
        return methodBody;
    }

    private void generateFindMethodParameterCheckingBlock(JavaCodeFragment methodBody,
            String methodName,
            List<String> parameterNames,
            String keyClassName,
            boolean useNullValueRow) throws CoreException {
        if (parameterNames.size() > 0) {
            methodBody.appendln("if (");

            for (int i = 0; i < parameterNames.size(); i++) {
                methodBody.append(parameterNames.get(i));
                methodBody.append(" == null");
                if (i < parameterNames.size() - 1) {
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
            List<String> keyClassParameterNames,
            List<String> parameterNames) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(returnTypeName);
        fragment.append(' ');
        fragment.append(returnVariable);
        fragment.append(" = ");
        fragment.append(fieldName);
        if (!rangesOnly) {
            fragment.append(".get(");
            fragment.append(createKeyInstantiation(keyClassName, keyClassParameterNames));
            fragment.append(")");
        }
        for (int i = 0; i < parameterNames.size(); i++) {
            fragment.append(".getValue(");
            fragment.append(parameterNames.get(i));
            fragment.append(")");
        }
        return fragment;
    }

    private JavaCodeFragment createFindMethodGetMapEntryFrag(String mapName,
            String keyClassName,
            String returnTypeName,
            String returnVariable,
            List<String> parameterNames) {
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

    private JavaCodeFragment createKeyInstantiation(String keyClassName, List<String> parameterNames) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.append(keyClassName);
        fragment.append("(");
        for (int i = 0; i < parameterNames.size(); i++) {
            if (i != 0) {
                fragment.append(", ");
            }
            fragment.append(parameterNames.get(i));
        }
        fragment.append(")");
        return fragment;
    }

    private Datatype getDatatypeForKeyName(String keyName) throws CoreException {
        IColumn column = getTableStructure().getColumn(keyName);
        if (column != null) {
            return findDatatype(column.getDatatype(), column.getIpsProject());
        }
        IColumnRange range = getTableStructure().getRange(keyName);
        if (range != null) {
            return findDatatype(range.getDatatype(), range.getIpsProject());
        }
        throw new RuntimeException("Unable to find column or columnRange for keyItemName: " + keyName);
    }

    private String getJavaClassName(String keyName) {
        Datatype datatypeForKeyName;
        try {
            datatypeForKeyName = getDatatypeForKeyName(keyName);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        if (datatypeForKeyName != null) {
            return datatypeForKeyName.getJavaClassName();
        } else {
            return null;
        }
    }

    private static String getJavaClassName(IKeyItem keyItem) {
        Datatype datatypeForKeyName;
        try {
            datatypeForKeyName = findDatatype(keyItem.getDatatype(), keyItem.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        if (datatypeForKeyName != null) {
            return datatypeForKeyName.getJavaClassName();
        } else {
            return null;
        }
    }

    private static Datatype findDatatype(String name, IIpsProject ipsProject) throws CoreException {
        return ipsProject.findDatatype(name);
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {

        // not supported, yet.
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

    private String[] toArray(List<String> stringList) {
        return stringList.toArray(new String[stringList.size()]);
    }

    /**
     * For every index there exists this set of variable. Some variables may not be used depending
     * on the kind of index.
     * 
     */
    private static class IndexCodePart {

        private final IIndex index;

        /**
         * The class name of the field holding the map or tree
         */
        private String keyMapFieldClassName;

        /**
         * The name of the field holding the map or tree
         */
        private String keyMapFieldName;

        /**
         * The name of the temporary map for creating a tree. If the index contains no ranges, this
         * is the same as the field name because we directly create the field's map.
         */
        private String keyMapTempName;

        /**
         * The class name of the temporary map for creating a tree. If the index contains no ranges,
         * this is the same as the field name because we directly create the field's map.
         * <p>
         * If the index is unique the temporary map is of type {@link Map} if it is not unique it is
         * of type {@link MultiMap}
         */
        private String keyMapTempClassName;

        /**
         * The class name of the key, for example UniqueKey1 or Index4
         */
        private String keyClassName;

        /**
         * The parameters of the key class constructor, hence the name of all columns that builds
         * the index without ranges!
         */
        private List<String> keyClassParameterNames;

        /**
         * The class names of the parameters of the key class constructor in the same order as
         * {@link #keyClassParameterNames}
         */
        private List<String> keyClassParameterTypes;

        /**
         * The names of all parameters in the find method. This may differ from keyClassParameters
         * if the key contains ranges.
         */
        private List<String> allItemParameterNames;

        /**
         * The class names of all parameters in the find method in the same order as
         * {@link #allItemParameterNames}
         */
        private List<String> allItemParameterTypes;

        /**
         * The suffix of the find method. The method name for the finder methods are provided by the
         * methods {@link TableImplBuilder#getMethodNameFindRow(String)}
         * {@link TableImplBuilder#getMethodNameFindExistingRow(String)} and
         * {@link TableImplBuilder#getMethodNameFindRowNullRowReturned(String)}. This suffix is
         * provided to the methods. It is only filled if there are two keys with the same datatypes
         * and hence same finder method signatures.
         * <p>
         * For example for a key that contains two columns, gender and age, this suffix may be
         * "ByGenderAge". The resulting finder method name is "findRowByGenderAge"
         */
        private String findMethodNameSuffix;

        public IndexCodePart(IIndex index) {
            this.index = index;
        }

        public List<String> getRangeParameterNames() {
            List<String> rangeParameterNames = allItemParameterNames.subList(keyClassParameterNames.size(),
                    allItemParameterNames.size());
            return rangeParameterNames;
        }

        /**
         * Creates the list of key class names as they are used in the map. If the index contains at
         * least one column, the first key class is the name of the internal key object, for example
         * Index0. The following key classes are the types of the ranges which are part of the
         * index. Key classes for two-column-ranges are wrapped in {@link TwoColumnKey}, every other
         * type is used as it is.
         */
        public List<String> getKeyClassNamesForMap() {
            List<String> keyClassNames = new ArrayList<String>();
            if (index.containsColumns()) {
                keyClassNames.add(keyClassName);
            }
            IKeyItem[] keyItems = index.getKeyItems();
            for (IKeyItem keyItem : keyItems) {
                if (keyItem.isRange()) {
                    IColumnRange range = (IColumnRange)keyItem;
                    if (range.getColumnRangeType().isTwoColumn()) {
                        keyClassNames.add(TwoColumnKey.class.getName() + "<" + getJavaClassName(range) + ">");
                    } else {
                        keyClassNames.add(getJavaClassName(range));
                    }
                }
            }
            return keyClassNames;
        }
    }

    private static class IndexCodePartMap {

        private Map<IIndex, IndexCodePart> internalMap = new HashMap<IIndex, TableImplBuilder.IndexCodePart>();

        public IndexCodePart get(IIndex index) {
            if (internalMap.containsKey(index)) {
                return internalMap.get(index);
            } else {
                IndexCodePart codePart = new IndexCodePart(index);
                internalMap.put(index, codePart);
                return codePart;
            }
        }

    }

}
