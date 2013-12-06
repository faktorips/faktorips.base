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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.internal.tableindex.KeyStructure;
import org.faktorips.runtime.internal.tableindex.RangeStructure;
import org.faktorips.runtime.internal.tableindex.RangeType;
import org.faktorips.runtime.internal.tableindex.ResultStructure;
import org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure;
import org.faktorips.runtime.internal.tableindex.UniqueResultStructure;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.values.ObjectUtil;

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
            indexCodePart.keyStructureFieldName = StringUtils.uncapitalize(keyVariableName) + "SearchStructure";
            indexCodePart.keyStructureFieldClassName = getKeyStructureFieldClass(Arrays.asList(index.getKeyItems()),
                    indexCodePart.indexClassName, index.isUniqueKey());
        }
    }

    /* private */String getKeyStructureFieldClass(List<IKeyItem> keyItemList, String keyClassName, boolean unique) {
        StringBuffer className = new StringBuffer();
        int nestedGeneric = 0;
        boolean firstItem = true;
        for (IKeyItem keyItem : keyItemList) {
            if (firstItem || keyItem.isRange()) {
                String structureType;
                structureType = getStructureType(keyItem, unique);
                className.append(structureType);
                if (keyItem.isRange()) {
                    className.append("<").append(getJavaClassName(keyItem)).append(", ");
                } else {
                    className.append("<").append(keyClassName).append(", ");
                }
                nestedGeneric++;
                firstItem = false;
            }
        }
        className.append(getResultStructureClassName(unique));
        className.append("<").append(qualifiedTableRowName).append(">");
        for (int i = 0; i < nestedGeneric; i++) {
            className.append(", ").append(qualifiedTableRowName);
            className.append(">");
        }
        return className.toString();
    }

    private String getResultStructureClassName(boolean unique) {
        if (unique) {
            return UniqueResultStructure.class.getName();
        } else {
            return ResultStructure.class.getName();
        }
    }

    private String getStructureType(IKeyItem keyItem, boolean unique) {
        String structureType;
        if (keyItem == null) {
            structureType = getResultStructureClassName(unique);
        } else if (keyItem.isRange()) {
            if (((IColumnRange)keyItem).getColumnRangeType().isTwoColumn()) {
                structureType = TwoColumnRangeStructure.class.getName();
            } else {
                structureType = RangeStructure.class.getName();
            }
        } else {
            structureType = KeyStructure.class.getName();
        }
        return structureType;
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
            indexCodePart.indexClassParameterNames = new ArrayList<String>();
            indexCodePart.indexClassParameterTypes = new ArrayList<String>();
            boolean keyHasColumn = index.containsColumns();
            if (keyHasColumn) {
                indexCodePart.indexClassName = "Index" + i;
            } else {
                indexCodePart.indexClassName = getJavaClassName(keyItems[0]);
            }
            for (String keyItem : keyItems) {
                allParameterTypes.add(getJavaClassName(keyItem));

                if (getTableStructure().hasColumn(keyItem)) {
                    indexCodePart.indexClassParameterNames.add(StringUtils.uncapitalize(keyItem));
                    indexCodePart.indexClassParameterTypes.add(getJavaClassName(keyItem));
                    parameters.add(StringUtils.uncapitalize(keyItem));
                } else {
                    IColumnRange range = getTableStructure().getRange(keyItem);
                    parameters.add(range.getParameterName());
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
        mainSection.setSuperClass(Table.class.getName() + "<" + qualifiedTableRowName + ">");
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
                if (enumHelper.getEnumType().isExtensible()) {
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
            createFindMethodRegular(indexCodePart.findMethodNameSuffix, index, codeBuilder);
            if (index.isUniqueKey()) {
                createFindMethodWithNullValueRow(indexCodePart.findMethodNameSuffix, index, codeBuilder);
                createFindMethodExistingRow(indexCodePart.findMethodNameSuffix, index, codeBuilder);
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
            codeBuilder.varDeclaration(Modifier.PRIVATE, indexCodePart.keyStructureFieldClassName,
                    indexCodePart.keyStructureFieldName);
        }
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
        codeBuilder.methodBegin(Modifier.PROTECTED, Void.TYPE, INIT_KEY_MAPS, new String[0], new Class[0]);
        codeBuilder.append(methodBody);
        codeBuilder.methodEnd();
    }

    private JavaCodeFragment createInitKeyMapsMethodBody(List<IIndex> keys) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        createKeyMapInitialization(keys, methodBody);
        createForLoop(keys, methodBody);
        return methodBody;
    }

    private void createKeyMapInitialization(List<IIndex> keys, JavaCodeFragment methodBody) {
        for (IIndex index : keys) {
            methodBody.append(createInitKeyMapsVariables(index));
        }
    }

    private JavaCodeFragment createInitKeyMapsVariables(IIndex index) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        IndexCodePart indexCodePart = indexCodeParts.get(index);
        methodBody.append(indexCodePart.keyStructureFieldName).append(" = ");
        IKeyItem keyItem = index.getKeyItemAt(0);
        methodBody.append(codeForCreateStructure(keyItem, index.isUniqueKey()));
        methodBody.appendln(';');
        return methodBody;
    }

    private void createForLoop(List<IIndex> keys, JavaCodeFragment methodBody) {
        methodBody.append("for (").appendClassName(qualifiedTableRowName).append(" row : rows)");
        methodBody.appendOpenBracket();
        createForLoopBody(keys, methodBody);
        methodBody.appendCloseBracket();
    }

    private void createForLoopBody(List<IIndex> keys, JavaCodeFragment methodBody) {
        int i = 0;
        for (IIndex index : keys) {
            JavaCodeFragment resultStructureCode = codeForCreateStructure(null, index.isUniqueKey(), "row");
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            JavaCodeFragment previousStructure = resultStructureCode;
            List<IKeyItem> keyItemList = Arrays.asList(index.getKeyItems());
            ArrayList<IKeyItem> processedKeys = new ArrayList<IKeyItem>();
            for (ListIterator<IKeyItem> iterator = keyItemList.listIterator(keyItemList.size()); iterator.hasPrevious();) {
                IKeyItem keyItem = iterator.previous();
                processedKeys.add(0, keyItem);
                String[] putParameter = getPutParameter(index, keyItem, previousStructure.getSourcecode());
                if (iterator.hasPrevious() && keyItem.isRange()) {
                    String keyStructureFieldClass = getKeyStructureFieldClass(processedKeys,
                            indexCodePart.indexClassName, index.isUniqueKey());
                    methodBody.appendClassName(keyStructureFieldClass).append(" ");
                    previousStructure = new JavaCodeFragment(((IColumnRange)keyItem).getParameterName()
                            + "SearchStructure" + i);
                    methodBody.append(previousStructure).append(" = ");
                    methodBody.append(codeForCreateStructure(keyItem, index.isUniqueKey(), putParameter));
                    methodBody.appendln(';');
                } else {
                    previousStructure = new JavaCodeFragment(indexCodePart.keyStructureFieldName);
                    appendPutIntoPreviousStructure(methodBody, previousStructure, putParameter);
                    break;
                }
            }
            i++;
        }
    }

    /* private */void appendPutIntoPreviousStructure(JavaCodeFragment methodBody,
            JavaCodeFragment previousStructure,
            String[] putParameter) {
        methodBody.append(previousStructure).append(".put(");
        for (String param : putParameter) {
            methodBody.append(param);
            if (!putParameter[putParameter.length - 1].equals(param)) {
                methodBody.append(", ");
            }
        }
        methodBody.appendln(");");
    }

    private String[] getPutParameter(IIndex index, IKeyItem keyItem, String result) {
        if (keyItem.isRange()) {
            IColumnRange range = (IColumnRange)keyItem;
            if (range.getColumnRangeType().isTwoColumn()) {
                return new String[] { "row.get" + StringUtils.capitalize(range.getFromColumn()) + "()",
                        "row.get" + StringUtils.capitalize(range.getToColumn()) + "()", result };
            } else {
                String paramName = range.getColumnRangeType().isOneColumnFrom() ? range.getFromColumn() : range
                        .getToColumn();
                return new String[] { "row.get" + StringUtils.capitalize(paramName) + "()", result };
            }
        } else {
            IndexCodePart indexCodePart = indexCodeParts.get(index);
            final String keyInstance = createKeyInstantiation(indexCodePart.indexClassName,
                    createInitKeyMapsKeyClassParameters(indexCodePart.indexClassParameterNames)).getSourcecode();
            return new String[] { keyInstance, result };
        }
    }

    private JavaCodeFragment codeForCreateStructure(IKeyItem keyItem, boolean unique, String... createArguments) {
        JavaCodeFragment codeFragment = new JavaCodeFragment();
        codeFragment.appendClassName(getStructureType(keyItem, unique));
        if (createArguments.length > 0) {
            codeFragment.append(".createWith(");
        } else {
            codeFragment.append(".create(");
        }
        boolean addColon = false;
        if (keyItem != null && keyItem.isRange()) {
            ColumnRangeType columnRangeType = ((IColumnRange)keyItem).getColumnRangeType();
            if (columnRangeType.isOneColumnFrom()) {
                codeFragment.appendClassName(RangeType.class).append('.').append(RangeType.LOWER_BOUND_EQUAL.name());
                addColon = true;
            } else if (columnRangeType.isOneColumnTo()) {
                codeFragment.appendClassName(RangeType.class).append('.').append(RangeType.UPPER_BOUND_EQUAL.name());
                addColon = true;
            }
        }
        for (String argument : createArguments) {
            if (addColon) {
                codeFragment.append(", ");
            }
            addColon = true;
            codeFragment.append(argument);
        }
        codeFragment.append(")");
        return codeFragment;
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
            if (!indexCodePart.indexClassParameterTypes.isEmpty()) {
                createHashKeyClass(indexCodePart.indexClassName, indexCodePart.indexClassParameterNames,
                        indexCodePart.indexClassParameterTypes);
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
            methodBody.append("((").append(keyName).append(" == null) ? 0 : ").append(keyName)
                    .appendln(".hashCode());");
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
            methodBody.appendClassName(ObjectUtil.class).append(".equals(");
            methodBody.append(keyClassParameterName);
            methodBody.append(", other.");
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

    private void createFindMethodRegular(String methodNameSuffix, IIndex index, JavaCodeFragmentBuilder codeBuilder)
            throws CoreException {
        IndexCodePart indexCodePart = indexCodeParts.get(index);
        String methodName = getMethodNameFindRow(methodNameSuffix, index.isUniqueKey());
        JavaCodeFragment methodBody = createFindMethodBody(methodName, index, "null");
        String javaDoc = getLocalizedText(getIpsObject(), FIND_JAVADOC);
        codeBuilder.method(Modifier.PUBLIC, getFinderMethodReturnType(index), methodName,
                toArray(indexCodePart.allItemParameterNames), toArray(indexCodePart.allItemParameterTypes), methodBody,
                javaDoc, ANNOTATION_GENERATED);
    }

    private String getMethodNameFindRow(String methodNameSuffix, boolean unique) {
        return METHOD_NAME_FIND_ROW + (unique ? "" : "s") + methodNameSuffix;
    }

    private void createFindMethodWithNullValueRow(String methodNameSuffix,
            IIndex index,
            JavaCodeFragmentBuilder codeBuilder) throws CoreException {
        IndexCodePart indexCodePart = indexCodeParts.get(index);
        String methodName = getMethodNameFindRowNullRowReturned(methodNameSuffix);
        JavaCodeFragment methodBody = createFindMethodBody(
                methodName,
                index,
                tableRowBuilder.getUnqualifiedClassName(getIpsSrcFile()) + "."
                        + tableRowBuilder.getFieldNameForNullRow());
        String javaDoc = getLocalizedText(getIpsObject(), FIND_RETURN_NULL_ROW_JAVADOC);
        codeBuilder.method(Modifier.PUBLIC, getFinderMethodReturnType(index), methodName,
                toArray(indexCodePart.allItemParameterNames), toArray(indexCodePart.allItemParameterTypes), methodBody,
                javaDoc, ANNOTATION_GENERATED);
    }

    private String getMethodNameFindRowNullRowReturned(String methodNameSuffix) {
        return METHOD_NAME_FIND_ROW + methodNameSuffix + METHOD_NAME_SUFFIX_FOR_NULL_VALUE_ROW;
    }

    private void createFindMethodExistingRow(String methodNameSuffix, IIndex index, JavaCodeFragmentBuilder codeBuilder)
            throws CoreException {
        IndexCodePart indexCodePart = indexCodeParts.get(index);
        JavaCodeFragment methodBody = createMethodBodyForFindMethodExistingRow(index, methodNameSuffix);
        String javaDoc = getLocalizedText(getIpsProject(), FIND_EXISTING_ROW_JAVADOC);
        codeBuilder.method(Modifier.PUBLIC, getFinderMethodReturnType(index),
                getMethodNameFindExistingRow(methodNameSuffix), toArray(indexCodePart.allItemParameterNames),
                toArray(indexCodePart.allItemParameterTypes), methodBody, javaDoc, ANNOTATION_GENERATED);
    }

    private JavaCodeFragment createMethodBodyForFindMethodExistingRow(IIndex index, String methodNameSuffix)
            throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("try").appendOpenBracket();
        methodBody.append(createFindMethodBody(getMethodNameFindExistingRow(methodNameSuffix), index, ""));
        methodBody.appendCloseBracket().appendln("catch (").appendClassName(NoSuchElementException.class).append(" e)")
                .appendOpenBracket();
        methodBody.append("throw new ");
        methodBody.appendClassName(IllegalArgumentException.class);
        methodBody.append("(\"");
        methodBody.append(getExceptionMessage());
        methodBody.append(" ");
        List<String> parameterNames = indexCodeParts.get(index).allItemParameterNames;
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
        return methodBody;
    }

    private String getExceptionMessage() throws CoreException {
        return NLS.bind(getLocalizedText(getIpsProject(), FIND_EXISTING_ROW_EXCEPTION_MESSAGE),
                getUnqualifiedClassName());
    }

    private String getMethodNameFindExistingRow(String methodNameSuffix) {
        return METHOD_NAME_FIND + METHOD_NAME_EXISTING + METHOD_NAME_ROW + methodNameSuffix;
    }

    private String getFinderMethodReturnType(IIndex index) {
        if (index.isUniqueKey()) {
            return qualifiedTableRowName;
        } else {
            return Set.class.getName() + "<" + qualifiedTableRowName + ">";
        }
    }

    private JavaCodeFragment createFindMethodBody(String methodName, IIndex index, String defaultValue)
            throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        IndexCodePart indexCodePart = indexCodeParts.get(index);

        // logging
        generateMethodEnteringLoggingStmt(methodBody, indexCodePart.indexClassName, methodName,
                toArray(indexCodePart.allItemParameterNames));
        methodBody.append("return ");
        methodBody.append(indexCodePart.keyStructureFieldName);
        int i = 0;
        for (IKeyItem keyItem : index.getKeyItems()) {
            if (i == 0 && !keyItem.isRange()) {
                methodBody
                        .append(".get(")
                        .append(createKeyInstantiation(indexCodePart.indexClassName,
                                indexCodePart.indexClassParameterNames)).append(")");
            } else if (keyItem.isRange()) {
                methodBody.append(".get(").append(indexCodePart.allItemParameterNames.get(i)).append(")");
            }
            i++;
        }
        if (index.isUniqueKey()) {
            methodBody.append(".getUnique(").append(defaultValue).append(")");
        } else {
            methodBody.append(".get()");
        }
        methodBody.appendln(';');
        return methodBody;
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

    private String getJavaClassName(IKeyItem keyItem) {
        Datatype datatypeForKeyName;
        try {
            datatypeForKeyName = findDatatype(keyItem.getDatatype(), getIpsProject());
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

        /**
         * The class name of the field holding the index structure
         */
        private String keyStructureFieldClassName;

        /**
         * The name of the field holding the index structure
         */
        private String keyStructureFieldName;

        /**
         * The class name of the key, for example Index0 or if there are only ranges the datatype of
         * the fist range.
         */
        private String indexClassName;

        /**
         * The parameters of the key class constructor, hence the name of all columns that builds
         * the index without ranges!
         */
        private List<String> indexClassParameterNames;

        /**
         * The class names of the parameters of the key class constructor in the same order as
         * {@link #indexClassParameterNames}
         */
        private List<String> indexClassParameterTypes;

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
         * methods {@link TableImplBuilder#getMethodNameFindRow(String, boolean)}
         * {@link TableImplBuilder#getMethodNameFindExistingRow(String)} and
         * {@link TableImplBuilder#getMethodNameFindRowNullRowReturned(String)}. This suffix is
         * provided to the methods. It is only filled if there are two keys with the same datatypes
         * and hence same finder method signatures.
         * <p>
         * For example for a key that contains two columns, gender and age, this suffix may be
         * "ByGenderAge". The resulting finder method name is "findRowByGenderAge"
         */
        private String findMethodNameSuffix;

    }

    private static class IndexCodePartMap {

        private Map<IIndex, IndexCodePart> internalMap = new HashMap<IIndex, TableImplBuilder.IndexCodePart>();

        public IndexCodePart get(IIndex index) {
            if (internalMap.containsKey(index)) {
                return internalMap.get(index);
            } else {
                IndexCodePart codePart = new IndexCodePart();
                internalMap.put(index, codePart);
                return codePart;
            }
        }

    }

}
