/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype.tableusage;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptTypePart;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class GenTableStructureUsage extends GenProductCmptTypePart {

    private final static LocalizedStringsSet LOC_STRINGS = new LocalizedStringsSet(GenTableStructureUsage.class);  
    
    private ITableStructureUsage tableStructureUsage;
    
    public GenTableStructureUsage(GenProductCmptType genProductCmptType, ITableStructureUsage tsu) throws CoreException {
        super(genProductCmptType, tsu, LOC_STRINGS);
        this.tableStructureUsage = tsu;
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        
        if (!generatesInterface) {
            generateMethodGetTableStructure(builder);
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
    private void generateMethodGetTableStructure(JavaCodeFragmentBuilder codeBuilder)
            throws CoreException {
        // generate the method to return the corresponding table content
        String methodName = getMethodNameGetTableUsage();
        String tableStructureClassName = getReturnTypeOfMethodGetTableUsage();
        String javaDoc = getLocalizedText("GET_TABLE_USAGE_METHOD_JAVADOC", tableStructureUsage.getRoleName());
        String roleName = getMemberVarName();
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("if (");
        body.append(roleName);
        body.appendln(" == null){");
        body.appendln("return null;");
        body.appendln("}");
        body.append("return ");
        body.append("(");
        body.appendClassName(tableStructureClassName);
        body.append(")");
        body.append(MethodNames.GET_REPOSITORY);
        body.append("().getTable(");
        body.append(roleName);
        body.appendln(");");
        codeBuilder.method(Modifier.PUBLIC, tableStructureClassName, methodName, new String[0], new String[0], body,
                javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
    }
    
    /**
     * Get the class name of the instance which will be returned,
     * if the usage contains only one table structure then the returned class will be the
     * generated class of this table structure, otherwise the return class will be the ITable
     * interface class
     */
    public String getReturnTypeOfMethodGetTableUsage() throws CoreException {
        String[] tss = tableStructureUsage.getTableStructures();
        if (tss.length == 1) {
            IIpsSrcFile tsuFile = getGenProductCmptType().getBuilderSet().getIpsProject().findIpsSrcFile(IpsObjectType.TABLE_STRUCTURE, tss[0]);
            if (tsuFile == null) {
                return "";
            }
            return TableImplBuilder.getQualifiedClassName(tsuFile, getGenProductCmptType().getBuilderSet());
        } 
        return ITable.class.getName();
    }

    public String getMethodNameGetTableUsage() {
        return "get" + StringUtils.capitalize(tableStructureUsage.getRoleName());
    }

    public String getMemberVarName() {
        return StringUtils.uncapitalize(tableStructureUsage.getRoleName()) + "Name";
    }

 
}
