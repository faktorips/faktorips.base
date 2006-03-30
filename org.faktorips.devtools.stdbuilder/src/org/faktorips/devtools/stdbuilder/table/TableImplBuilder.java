/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.table;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.core.builder.SimpleJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.runtime.RuntimeRepository;
import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree;
import org.faktorips.runtime.internal.TableImpl;
import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree.TwoColumnKey;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Important: This builder expects from the <code>IJavaPackageStructure</code> the qualified class
 * name for a table row class. It uses the kindId="tablerow" to get this name. The output of this
 * builder depends on the output of the
 * <code>org.faktorips.std.builder.table.TableRowGenerator</code>.
 * 
 * @author Peter Erzberger
 */
public class TableImplBuilder extends SimpleJavaSourceFileBuilder {

    private final static String CLASS_JAVADOC = "TABLE_IMPL_BUILDER_CLASS_JAVADOC";
    private final static String GET_ALL_ROWS_JAVADOC = "TABLE_IMPL_BUILDER_GET_ALL_ROWS_JAVADOC";
    private final static String GET_INSTANCE_JAVADOC = "TABLE_IMPL_BUILDER_GET_INSTANCE_JAVADOC";
    private final static String KEY_CLASS_EQUALS_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_EQUALS_JAVADOC";
    private final static String KEY_CLASS_HASHCODE_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_HASHCODE_JAVADOC";
    private final static String FIND_JAVADOC = "TABLE_IMPL_BUILDER_FIND_JAVADOC";
    private final static String KEY_CLASS_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_JAVADOC";
    private final static String KEY_CLASS_CONSTRUCTOR_JAVADOC = "TABLE_IMPL_BUILDER_KEY_CLASS_CONSTRUCTOR_JAVADOC";
    private final static String ADD_ROW_JAVADOC = "TABLE_IMPL_BUILDER_ADD_ROW_JAVADOC";
    private final static String INIT_KEY_MAPS_JAVADOC = "TABLE_IMPL_BUILDER_INIT_KEY_MAPS_JAVADOC";

    private TableRowBuilder tableRowBuilder;
    
    private String[] fKeyVariableNames;
    private String[] fKeyClassNames;
    private List fKeyClassParameterNames;
    private List fAllItemNamesAsParameters;
    private List fKeyClassParameterTypes;
    private List fAllItemParameterTypes;
    private Map fDatatypes;
    private String qualifiedTableRowName;
    private Map fRanges;

    public TableImplBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(TableImplBuilder.class));
    }

    public void setTableRowBuilder(TableRowBuilder tableRowBuilder) {
        this.tableRowBuilder = tableRowBuilder;
    }

    /**
     * Returns the table structure during generation and null otherwise.
     */
    protected ITableStructure getTableStructure(){
        return (ITableStructure)getIpsObject();
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.builder.SimpleJavaSourceFileBuilder#beforeBuildInternal(org.faktorips.devtools.core.model.IIpsSrcFile)
     */
    public void beforeBuildInternal(IIpsSrcFile ipsSrcFile)
            throws CoreException {
        qualifiedTableRowName = tableRowBuilder.getQualifiedClassName(ipsSrcFile);
        buildKeyVariableNames();
        initClassVariables();
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.builder.SimpleJavaSourceFileBuilder#afterBuildInternal(org.faktorips.devtools.core.model.IIpsSrcFile)
     */
    public void afterBuildInternal(IIpsSrcFile ipsSrcFile) throws CoreException {
        fKeyVariableNames = null;
        fKeyClassNames = null;
        fKeyClassParameterNames = null;
        fAllItemNamesAsParameters = null;
        fKeyClassParameterTypes = null;
        fAllItemParameterTypes = null;
        fDatatypes = null;
        fRanges = null;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.TABLE_STRUCTURE);
    }

    private Set getIndicesForKeysWithSameDatatypeSequence() throws CoreException {
        IUniqueKey[] keys = getUniqueKeys();
        Set positions = new HashSet();
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
        Set keysWithSameDatatypeSequence = getIndicesForKeysWithSameDatatypeSequence();
        for (int i = 0; i < keys.length; i++) {
            if (keysWithSameDatatypeSequence.contains(new Integer(i))) {
                StringBuffer keyName = new StringBuffer();
                String[] keyItems = keys[i].getKeyItemNames();
                keyName.append("By");
                for (int j = 0; j < keyItems.length; j++) {

                    if (getTableStructure().hasColumn(keyItems[j])) {
                        keyName.append(StringUtils.capitalise(keyItems[j]));
                        continue;
                    }

                    IColumnRange range = getTableStructure().getRange(keyItems[j]);
                    keyName.append(StringUtils.capitalise(range.getParameterName()));
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
        fKeyClassParameterNames = new ArrayList(keys.length);
        fKeyClassParameterTypes = new ArrayList(keys.length);
        fAllItemNamesAsParameters = new ArrayList(keys.length);
        fAllItemParameterTypes = new ArrayList(keys.length);
        fRanges = new HashMap(keys.length);
        for (int i = 0; i < keys.length; i++) {
            StringBuffer keyClassName = new StringBuffer();
            String[] keyItems = keys[i].getKeyItemNames();
            ArrayList parameters = new ArrayList();
            ArrayList allParameterTypes = new ArrayList();
            ArrayList keyClassParameterTypes = new ArrayList();
            ArrayList keyClassParameterNames = new ArrayList();
            boolean isColumn = false;
            for (int j = 0; j < keyItems.length; j++) {

                allParameterTypes.add(getJavaClassName(keyItems[j]));

                if (getTableStructure().hasColumn(keyItems[j])) {
                    keyClassName.append(StringUtils.capitalise(keyItems[j]));
                    keyClassParameterNames.add(keyItems[j]);
                    keyClassParameterTypes.add(getJavaClassName(keyItems[j]));
                    parameters.add(keyItems[j]);
                    isColumn = true;
                    continue;
                }

                IColumnRange range = getTableStructure().getRange(keyItems[j]);
                parameters.add(range.getParameterName());
                fRanges.put(range.getParameterName(), range);
            }

            if (isColumn) {
                keyClassName.append("Key");
                fKeyClassNames[i] = keyClassName.toString();
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

    private boolean checkColumnValidity() throws CoreException {
        IColumn[] columns = getTableStructure().getColumns();
        for (int i = 0; i < columns.length; i++) {
            if (!columns[i].validate().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkUniqueKeyValidity() throws CoreException {
        IUniqueKey[] keys = getTableStructure().getUniqueKeys();
        for (int i = 0; i < keys.length; i++) {
            if (!keys[i].validate().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.builder.SimpleJavaSourceFileBuilder#generateInternal()
     */
    protected void generateInternal() throws CoreException {
        if (!checkColumnValidity()) {
            return;
        }
        getJavaCodeFragementBuilder().javaDoc(getLocalizedText(getIpsObject(), CLASS_JAVADOC));
        getJavaCodeFragementBuilder().classBegin(Modifier.PUBLIC, getTableStructure().getName(),
                TableImpl.class, new Class[0]);
        createFields();
        createAddRowMethod();
        createInitKeyMapsMethod();
        if (getTableStructure().isMultipleContentsAllowed()) {
            createGetInstanceMethodForMultipleContents();
        } else {
            createGetInstanceMethodForSingleContent();
        }
        createAllRowsMethod();
        createFindMethods();
        createHashKeyClasses();
        getJavaCodeFragementBuilder().classEnd();
    }

    private void createAllRowsMethod() throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.appendClassName(qualifiedTableRowName);
        methodBody.append("[]");
        methodBody.append("rowsArray");
        methodBody.append(" = ");
        methodBody.append("new ");
        methodBody.appendClassName(qualifiedTableRowName);
        methodBody.append("[rows.size()];");
        methodBody.appendln();
        methodBody.append("rows.toArray(rowsArray);");
        methodBody.appendln();
        methodBody.append("return rowsArray;");
        getJavaCodeFragementBuilder().method(Modifier.PUBLIC, qualifiedTableRowName + "[]",
                "getAllRows", new String[0], new String[0], methodBody,
                getLocalizedText(getIpsObject(), GET_ALL_ROWS_JAVADOC));
    }

    private void createGetInstanceMethodForSingleContent() throws CoreException {
        String qualifiedClassName = getQualifiedClassName(getTableStructure().getIpsSrcFile());
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return (");
        methodBody.appendClassName(qualifiedClassName);
        methodBody.append(")");
        methodBody.append("repository.getTable(");
        methodBody.appendClassName(qualifiedClassName);
        methodBody.append(".class");
        methodBody.append(");");

        getJavaCodeFragementBuilder().method(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL,
                qualifiedClassName, "getInstance", new String[] { "repository" },
                new String[] { RuntimeRepository.class.getName() }, methodBody,
                getLocalizedText(getIpsObject(), GET_INSTANCE_JAVADOC));
    }

    private void createGetInstanceMethodForMultipleContents() throws CoreException {
        String qualifiedClassName = getQualifiedClassName(getTableStructure().getIpsSrcFile());
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return (");
        methodBody.appendClassName(qualifiedClassName);
        methodBody.append(")");
        methodBody.append("repository.getTable(qualifiedTableName);");

        getJavaCodeFragementBuilder().method(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL,
                qualifiedClassName, "getInstance", 
                new String[] { "repository", "qualifiedTableName" },
                new String[] { RuntimeRepository.class.getName(), String.class.getName() }, 
                methodBody,
                getLocalizedText(getIpsObject(), GET_INSTANCE_JAVADOC));
    }

    private void createAddRowMethod() throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        IColumn[] columns = getTableStructure().getColumns();
        for (int i = 0; i < columns.length; i++) {
            IColumn column = columns[i];
            Datatype columnDatatype = findDatatype(column.getDatatype());
            DatatypeHelper helper = getTableStructure().getIpsProject().getDatatypeHelper(
                    columnDatatype);
            if (i == 0) {
                methodBody.appendClassName(Element.class);
                methodBody.append(' ');
            }
            methodBody.append("valueElement = (");
            methodBody.appendClassName(Element.class);
            methodBody.append(") valueElements.item(");
            methodBody.append(i);
            methodBody.append(");");
            methodBody.appendln();

            if (i == 0) {
                methodBody.appendClassName(Text.class);
                methodBody.append(' ');
            }
            methodBody.append("text = ");
            methodBody.appendClassName(XmlUtil.class);
            methodBody.append(".getTextNode(valueElement);");

            methodBody.appendClassName(columnDatatype.getJavaClassName());
            methodBody.append(" ");
            methodBody.append(StringUtils.uncapitalise(column.getName()));
            methodBody.append(" = isNull(valueElement) ? ");
            methodBody.append(helper.nullExpression());
            methodBody.append(" : ");
            if (columnDatatype instanceof StringDatatype) {
                methodBody.append("text == null ? \"\" : text.getData()");
            } else {
                methodBody.append(helper.newInstanceFromExpression("text.getData()"));
            }
            methodBody.append(';');
        }
        methodBody.append("rows.add(new ");
        methodBody.appendClassName(qualifiedTableRowName);
        methodBody.append("(");
        for (int i = 0; i < columns.length; i++) {

            if (i > 0) {
                methodBody.append(", ");
            }
            methodBody.append(StringUtils.uncapitalise(columns[i].getName()));
        }

        methodBody.append("));");

        getJavaCodeFragementBuilder().method(Modifier.PROTECTED, Void.TYPE, "addRow",
                new String[] { "valueElements" }, new Class[] { NodeList.class }, methodBody,
                getLocalizedText(getIpsObject(), ADD_ROW_JAVADOC));
    }

    private void createFindMethods() throws CoreException {
        if (!checkUniqueKeyValidity()) {
            return;
        }
        IUniqueKey[] keys = getUniqueKeys();
        String[] findMethodNames = buildFindMethodNamesArray();
        for (int i = 0; i < keys.length; i++) {
            StringBuffer methodName = new StringBuffer();
            methodName.append("findRow");
            methodName.append(findMethodNames[i]);
            createFindMethod(methodName.toString(), qualifiedTableRowName,
                    (String[])fAllItemParameterTypes.get(i), (String[])fAllItemNamesAsParameters
                            .get(i), (String[])fKeyClassParameterNames.get(i),
                    fKeyVariableNames[i], fKeyClassNames[i], keys[i]);
        }
    }

    private IUniqueKey[] getUniqueKeys() {
        return getTableStructure().getUniqueKeys();
    }

    private void createFields() throws CoreException {
        if (!checkUniqueKeyValidity()) {
            return;
        }

        for (int i = 0; i < fKeyVariableNames.length; i++) {
            StringBuffer keyMapFieldName = new StringBuffer();
            String fieldClassName;
            keyMapFieldName.append(StringUtils.uncapitalise(fKeyVariableNames[i]));
            if (fKeyClassParameterNames.get(i) != null) {
                keyMapFieldName.append("Map");
                fieldClassName = Map.class.getName();
            } else {
                keyMapFieldName.append("Tree");
                fieldClassName = ReadOnlyBinaryRangeTree.class.getName();
            }
            getJavaCodeFragementBuilder().varDeclaration(Modifier.PRIVATE, fieldClassName,
                    keyMapFieldName.toString());
        }
    }

    private JavaCodeFragment buildAddKeyFragment(String combinedKeyName,
            String keyClassName,
            String[] keyItems) throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append(StringUtils.uncapitalise(combinedKeyName));
        methodBody.append("Map.put(new ");
        methodBody.append(keyClassName);
        methodBody.append("(");
        for (int i = 0; i < keyItems.length; i++) {
            if (i != 0) {
                methodBody.append(", ");
            }
            methodBody.append("row.get");
            methodBody.append(StringUtils.capitalise(keyItems[i]));
            methodBody.append("()");
        }
        methodBody.append("), row);");
        return methodBody;
    }

    private void createInitKeyMapsMethod() throws CoreException {
        if (!checkUniqueKeyValidity()) {
            return;
        }

        IUniqueKey[] keys = getUniqueKeys();
        JavaCodeFragment methodBody = new JavaCodeFragment();
        if(keys.length != 0){
            methodBody = createInitKeyMapsMethodBody(keys);
        }
        getJavaCodeFragementBuilder().method(Modifier.PROTECTED, Void.TYPE, "initKeyMaps",
                new String[0], new Class[0], methodBody,
                getLocalizedText(getIpsObject(), INIT_KEY_MAPS_JAVADOC));
    }

    private JavaCodeFragment createInitKeyMapsMethodBody(IUniqueKey[] keys) throws CoreException{
        JavaCodeFragment methodBody = new JavaCodeFragment();
        for (int i = 0; i < keys.length; i++) {
            methodBody.append(createInitKeyMapsVariables(keys[i], fKeyVariableNames[i] + "Map"));
            methodBody.appendln();
        }
        methodBody.append("for (");
        methodBody.appendClassName(Iterator.class);
        methodBody.append(" it = rows.iterator(); it.hasNext();)");
        methodBody.appendOpenBracket();
        methodBody.appendClassName(qualifiedTableRowName);
        methodBody.append(" row = (");
        methodBody.appendClassName(qualifiedTableRowName);
        methodBody.append(") it.next();");
        methodBody.appendln();

        for (int i = 0; i < keys.length; i++) {
            if (keys[i].containsRanges()) {
                String tempName = StringUtils.uncapitalise(fKeyVariableNames[i]) + "MapTemp";
                String[] parameterNames = (String[])fAllItemNamesAsParameters.get(i);
                String[] keyClassParameterNames = (String[])fKeyClassParameterNames.get(i);
                String[] rangeParameterNames;
                if (keyClassParameterNames == null) {
                    rangeParameterNames = parameterNames;
                } else {
                    rangeParameterNames = new String[parameterNames.length
                            - keyClassParameterNames.length];
                    System.arraycopy(parameterNames, keyClassParameterNames.length,
                            rangeParameterNames, 0, rangeParameterNames.length);
                }
                ArrayList getMapFirstParameter = new ArrayList();
                ArrayList getMapSecondParameter = new ArrayList();
                ArrayList getMapThirdParameter = new ArrayList();
                getMapFirstParameter.add(tempName);
                if (keys[i].containsColumns()) {
                    getMapFirstParameter.add(StringUtils.uncapitalise(rangeParameterNames[0])
                            + "Map");
                    getMapSecondParameter.add(createKeyInstantiation(
                            fKeyClassNames[i],
                            createInitKeyMapsKeyClassParameters((String[])fKeyClassParameterNames
                                    .get(i))).toString());
                    getMapThirdParameter.add(null);
                }
                for (int j = 0; j < rangeParameterNames.length; j++) {
                    if (j != 0) {
                        getMapFirstParameter.add(StringUtils.uncapitalise(rangeParameterNames[j])
                                + "Map");
                    }
                    IColumnRange range = (IColumnRange) fRanges.get(rangeParameterNames[j]);
                    if (range != null && range.getColumnRangeType().isTwoColumn()) {
                        getMapSecondParameter.add("row.get"
                                + StringUtils.capitalise(range.getFromColumn()) + "()");
                        getMapThirdParameter.add("row.get"
                                + StringUtils.capitalise(range.getToColumn()) + "()");
                    } else {
                        String paramName = range == null ? rangeParameterNames[j] :
                                range.getColumnRangeType().isOneColumnFrom() ? range.getFromColumn() :
                                    range.getToColumn();
                        getMapSecondParameter.add("row.get" 
                                + StringUtils.capitalise(paramName) + "()");
                        getMapThirdParameter.add(null);
                    }
                }
                for (int j = 1; j < getMapFirstParameter.size(); j++) {
                    methodBody.append(createInitKeyMapsMapAssignment((String)getMapFirstParameter
                            .get(j), (String)getMapFirstParameter.get(j - 1),
                            (String)getMapSecondParameter.get(j - 1),
                            (String)getMapThirdParameter.get(j - 1)));
                    methodBody.appendln();
                }
                methodBody.append(createInitKeyMapsPutStatement(
                        (String)getMapFirstParameter.get(getMapFirstParameter.size() - 1),
                        (String)getMapSecondParameter.get(getMapSecondParameter.size() - 1),
                        (String)getMapThirdParameter.get(getMapThirdParameter.size() - 1)));
                methodBody.appendln();
                continue;
            }
            methodBody.append(buildAddKeyFragment(fKeyVariableNames[i], fKeyClassNames[i],
                    (String[])fKeyClassParameterNames.get(i)));
        }

        methodBody.appendCloseBracket();
        methodBody.append(createInitKeyMapsFieldAssignments(keys));
        return methodBody;
    }
    
    private JavaCodeFragment createInitKeyMapsPutStatement(String mapName, String key,
            String secondKey) {
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
            fragment.append(StringUtils.uncapitalise(fKeyVariableNames[i]));
            fragment.append(keys[i].containsColumns() ? "Map" : "Tree");
            fragment.append(" = ");
            fragment.append(keys[i].containsColumns() ? "convert" : "generateTree");
            fragment.append("(");
            fragment.append(StringUtils.uncapitalise(fKeyVariableNames[i]));
            fragment.append("MapTemp, new ");
            fragment.appendClassName(Integer.TYPE);
            fragment.append("[] {");
            String[] keyItemNames = keys[i].getKeyItemNames();
            boolean firstEntry = true;
            for (int j = 0; j < keyItemNames.length; j++) {
                IColumnRange range = getTableStructure().getRange(keyItemNames[j]);
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
                    fragment.append("KEY_IS_LOWER_BOUND_EQUAL"); // TODO mï¿½glicherweise ï¿½ber
                    // reflection
                }
                if (range.getColumnRangeType().isOneColumnTo()) {
                    fragment.append("KEY_IS_UPPER_BOUND_EQUAL"); // TODO mï¿½glicherweise ï¿½ber
                    // reflection
                }
            }
            fragment.append("});");
        }
        return fragment;
    }

    private JavaCodeFragment createInitKeyMapsVariables(IUniqueKey key, String fieldName) {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        if (key.containsRanges()) {
            methodBody.appendClassName(HashMap.class);
            methodBody.append(' ');
            methodBody.append(StringUtils.uncapitalise(fieldName));
            methodBody.append("Temp");
        } else {
            methodBody.append(StringUtils.uncapitalise(fieldName));
        }
        methodBody.append(" = new ");
        methodBody.appendClassName(HashMap.class);
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
            returnValue[i] = "row.get" + StringUtils.capitalise(rangeParameterNames[i]) + "()";
        }
        return returnValue;
    }

    private void createHashKeyClasses() throws CoreException {
        if (!checkUniqueKeyValidity()) {
            return;
        }
        IUniqueKey[] keys = getUniqueKeys();
        for (int i = 0; i < keys.length; i++) {
            String[] keyClassParameterTypes = (String[])fKeyClassParameterTypes.get(i);
            if (keyClassParameterTypes != null) {
                createHashKeyClass(fKeyClassNames[i], (String[])fKeyClassParameterNames.get(i),
                        keyClassParameterTypes);
            }
        }
    }

    private void createHashKeyClass(String hashKeyClassName,
            String[] keyNames,
            String[] keyItemTypes) throws CoreException {
        
        getJavaCodeFragementBuilder().javaDoc(getLocalizedText(getIpsObject(), KEY_CLASS_JAVADOC));
        getJavaCodeFragementBuilder().classBegin(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
                hashKeyClassName);
        for (int i = 0; i < keyNames.length; i++) {
            getJavaCodeFragementBuilder().varDeclaration(Modifier.PRIVATE, keyItemTypes[i],
                    keyNames[i]);
        }
        // create hashCode field
        getJavaCodeFragementBuilder().varDeclaration(Modifier.PRIVATE, Integer.TYPE, "hashCode");

        JavaCodeFragment constructorBody = new JavaCodeFragment();
        for (int i = 0; i < keyNames.length; i++) {
            constructorBody.append("this.");
            constructorBody.append(keyNames[i]);
            constructorBody.append(" = ");
            constructorBody.append(keyNames[i]);
            constructorBody.append(';');
            constructorBody.appendln();
        }
        constructorBody.appendln("hashCode = calculateHashCode();");
        // constructor
        getJavaCodeFragementBuilder()
                .method(Modifier.PRIVATE, null, hashKeyClassName, keyNames, keyItemTypes,
                        constructorBody, getLocalizedText(getIpsObject(), KEY_CLASS_CONSTRUCTOR_JAVADOC));
        createKeyClassCalHashCodeMethod(keyNames);
        createKeyClassEqualsMethod(hashKeyClassName, keyNames);
        createKeyClassHashCodeMethod(keyNames);
        getJavaCodeFragementBuilder().classEnd();
    }

    private void createKeyClassCalHashCodeMethod(String[] keyNames) throws CoreException {
        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.appendln("int result = 17;");
        for (int i = 0; i < keyNames.length; i++) {
            methodBody.append("result = 37 * result + ");
            methodBody.append(keyNames[i]);
            methodBody.appendln(".hashCode();");
        }
        methodBody.appendln("return result;");

        getJavaCodeFragementBuilder().methodBegin(Modifier.PRIVATE, Integer.TYPE,
                "calculateHashCode", new String[0], new Class[0]);
        getJavaCodeFragementBuilder().append(methodBody);
        getJavaCodeFragementBuilder().methodEnd();
    }

    private void createKeyClassEqualsMethod(String keyClass, String[] combinedKeyNames)
            throws CoreException {

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

        getJavaCodeFragementBuilder().method(Modifier.PUBLIC, Boolean.TYPE, "equals",
                new String[] { "o" }, new Class[] { Object.class }, methodBody,
                getLocalizedText(getIpsObject(), KEY_CLASS_EQUALS_JAVADOC));
    }

    private void createKeyClassHashCodeMethod(String[] combineKeyNames) throws CoreException {

        JavaCodeFragment methodBody = new JavaCodeFragment();
        methodBody.append("return hashCode;");

        getJavaCodeFragementBuilder().method(Modifier.PUBLIC, Integer.TYPE, "hashCode",
                new String[0], new Class[0], methodBody,
                getLocalizedText(getIpsObject(), KEY_CLASS_HASHCODE_JAVADOC));
    }

    private void createFindMethod(String methodName,
            String returnTypeName,
            String[] parameterTypes,
            String[] parameterNames,
            String[] keyClassParameterNames,
            String combinedKeyName,
            String keyClassName,
            IUniqueKey key) throws JavaModelException {
        JavaCodeFragment methodBody = new JavaCodeFragment();

        for (int i = 0; i < parameterNames.length; i++) {
            methodBody.appendClassName(ArgumentCheck.class);
            methodBody.append(".notNull(");
            methodBody.append(parameterNames[i]);
            methodBody.append(");");
            methodBody.appendln();
        }

        String mapName = StringUtils.uncapitalise(combinedKeyName) + "Map";
        String treeName = StringUtils.uncapitalise(combinedKeyName) + "Tree";
        if (key.containsColumns()) {
            if (key.containsRanges()) {
                String[] rangeParameterNames = new String[parameterNames.length
                        - keyClassParameterNames.length];
                System.arraycopy(parameterNames, keyClassParameterNames.length,
                        rangeParameterNames, 0, rangeParameterNames.length);
                methodBody.append(createFindMethodReturnFrag(returnTypeName));
                methodBody.append(createFindMethodGetValueFrag(false, mapName, keyClassName,
                        keyClassParameterNames, rangeParameterNames));
            } else {
                methodBody.append(createFindMethodReturnFrag(returnTypeName));
                methodBody.append(createFindMethodGetMapEntryFrag(mapName, keyClassName,
                        keyClassParameterNames));
            }
        } else {
            methodBody.append(createFindMethodReturnFrag(returnTypeName));
            methodBody.append(createFindMethodGetValueFrag(true, treeName, keyClassName,
                    keyClassParameterNames, parameterNames));
        }
        methodBody.append(';');

        getJavaCodeFragementBuilder().method(Modifier.PUBLIC, returnTypeName, methodName,
                parameterNames, parameterTypes, methodBody, getLocalizedText(getIpsObject(), FIND_JAVADOC));
    }

    private JavaCodeFragment createFindMethodReturnFrag(String returnTypeName) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("return (");
        fragment.appendClassName(returnTypeName);
        fragment.append(") ");
        return fragment;
    }

    private JavaCodeFragment createFindMethodGetValueFrag(boolean rangesOnly,
            String fieldName,
            String keyClassName,
            String[] keyClassParameterNames,
            String[] parameterNames) {
        JavaCodeFragment fragment = new JavaCodeFragment();
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
            String[] parameterNames) {
        JavaCodeFragment fragment = new JavaCodeFragment();
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
        throw new RuntimeException("Unable to find column or columnRange for keyItemName: "
                + keyName);
    }

    private String getJavaClassName(String keyName) throws CoreException {
        return getDatatypeForKeyName(keyName).getJavaClassName();
    }

    private Datatype findDatatype(String name) throws CoreException {

        if (fDatatypes == null) {

            Datatype[] datatypes = getTableStructure().getIpsProject().findDatatypes(false, true);
            fDatatypes = new HashMap(datatypes.length);
            for (int i = 0; i < datatypes.length; i++) {
                fDatatypes.put(datatypes[i].getQualifiedName(), datatypes[i]);
            }
        }
        return (Datatype)fDatatypes.get(name);
    }
}
