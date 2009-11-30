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

package org.faktorips.devtools.stdbuilder.productcmpttype.tableusage;

import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.type.GenTypePart;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class GenTableStructureUsage extends GenTypePart {

    private final static LocalizedStringsSet LOCALIZED_STRINGS = new LocalizedStringsSet(GenTableStructureUsage.class);

    private ITableStructureUsage tableStructureUsage;

    public GenTableStructureUsage(GenProductCmptType genProductCmptType, ITableStructureUsage tsu) throws CoreException {
        super(genProductCmptType, tsu, LOCALIZED_STRINGS);
        tableStructureUsage = tsu;
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        // nothing to do
    }

    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {

        if (generatesInterface) {
            return;
        }
        appendLocalizedJavaDoc("FIELD_TABLE_USAGE", tableStructureUsage.getRoleName(), builder);
        JavaCodeFragment expression = new JavaCodeFragment("null");
        builder.varDeclaration(Modifier.PROTECTED, String.class, getMemberVarName(), expression);
    }

    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {

        if (!generatesInterface) {
            generateMethodGetTable(builder);
            generateMethodSetTableName(builder);
        }

    }

    /**
     * Generates the method to return the table content which is related to the specific role.<br>
     * Example:
     * 
     * <pre>
     * public FtTable getRatePlan() {
     *     if (ratePlanName == null) {
     *         return null;
     *     }
     *     return (FtTable)getRepository().getTable(ratePlanName);
     * }
     * </pre>
     */
    private void generateMethodGetTable(JavaCodeFragmentBuilder codeBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_GET_TABLE", tableStructureUsage.getRoleName(), codeBuilder);

        String methodName = getMethodNameGetTableUsage();
        String tableStructureClassName = getReturnTypeOfMethodGetTableUsage();
        String roleName = getMemberVarName();
        codeBuilder.signature(Modifier.PUBLIC, tableStructureClassName, methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
        codeBuilder.openBracket();
        codeBuilder.append("if (");
        codeBuilder.append(roleName);
        codeBuilder.appendln(" == null){");
        codeBuilder.appendln("return null;");
        codeBuilder.appendln("}");
        codeBuilder.append("return ");
        codeBuilder.append("(");
        codeBuilder.appendClassName(tableStructureClassName);
        codeBuilder.append(")");
        codeBuilder.append(MethodNames.GET_REPOSITORY);
        codeBuilder.append("().getTable(");
        codeBuilder.append(roleName);
        codeBuilder.appendln(");");
        codeBuilder.closeBracket();
    }

    /**
     * Get the class name of the instance which will be returned, if the usage contains only one
     * table structure then the returned class will be the generated class of this table structure,
     * otherwise the return class will be the ITable interface class
     */
    public String getReturnTypeOfMethodGetTableUsage() throws CoreException {
        String[] tss = tableStructureUsage.getTableStructures();
        if (tss.length == 1) {
            IIpsSrcFile tsuFile = getGenType().getBuilderSet().getIpsProject().findIpsSrcFile(
                    IpsObjectType.TABLE_STRUCTURE, tss[0]);
            if (tsuFile == null) {
                return "";
            }
            return TableImplBuilder.getQualifiedClassName(tsuFile, getGenType().getBuilderSet());
        }
        return ITable.class.getName();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setRateTable(String tableName) {
     *     if (getRepository()!=null &amp;&amp; !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     this.rateTableName = tableName;
     * }
     * </pre>
     */
    private void generateMethodSetTableName(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_SET_TABLE_NAME", StringUtils.capitalize(getMemberVarName()), methodsBuilder);
        String methodName = getMethodNameSetUsedTableName();
        String[] paramNames = new String[] { "tableName" };
        String[] paramTypes = new String[] { String.class.getName() };
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, paramNames, paramTypes);
        methodsBuilder.openBracket();
        methodsBuilder.append(((GenProductCmptType)getGenType()).generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append("this." + getMemberVarName());
        methodsBuilder.appendln(" = tableName;");
        methodsBuilder.closeBracket();
    }

    public String getMethodNameGetTableUsage() {
        return "get" + StringUtils.capitalize(tableStructureUsage.getRoleName());
    }

    public String getMethodNameSetUsedTableName() {
        return "set" + StringUtils.capitalize(getMemberVarName());
    }

    public String getMemberVarName() {
        return StringUtils.uncapitalize(tableStructureUsage.getRoleName()) + "Name";
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        // TODO AW: Not implemented yet.
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        // TODO AW: Not implemented yet.
    }

}
