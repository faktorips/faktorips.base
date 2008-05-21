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

package org.faktorips.devtools.stdbuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.internal.model.TableContentsEnumDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.controller.fields.EnumTypeTargetJavaVersion;
import org.faktorips.devtools.stdbuilder.enums.EnumClassesBuilder;
import org.faktorips.devtools.stdbuilder.enums.EnumTypeInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.formulatest.FormulaTestBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.table.TableContentBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.table.TableRowBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.TableFunctionExecution;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A IpsArtefactBuilderSet implementation that assembles the standard FaktorIPS artefact builders.
 * 
 * @author Peter Erzberger
 */
public class StandardBuilderSet extends DefaultBuilderSet {

    /**
     * Configuration property that enables/disables the generation of a copy method.
     * 
     * @see ICopySupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_COPY_SUPPORT = "generateCopySupport";

    /**
     * Configuration property that enables/disables the generation of delta computation.
     * 
     * @see IDeltaSupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT = "generateDeltaSupport";

    /**
     * Configuration property that enables/disables the generation of the visitor support.
     * 
     * @see IDeltaSupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT = "generateVisitorSupport";

    /**
     * Configuration property that is supposed to be used to read a configuration value from the
     * IIpsArtefactBuilderSetConfig object provided by the initialize method of an
     * IIpsArtefactBuilderSet instance.
     */
    public final static String CONFIG_PROPERTY_GENERATE_CHANGELISTENER = "generateChangeListener";

    /**
     * Configuration property that declares for which java version sourcecode will be generated.
     */
    public final static String CONFIG_PROPERTY_TARGET_JAVA_VERSION = "targetJavaVersion";

    /**
     * Configuration property that enanbles/disables the use of typesafe collections, if supported
     * by the target java version.
     */
    public final static String CONFIG_PROPERTY_USE_TYPESAFE_COLLECTIONS = "useTypesafeCollections";

    /**
     * Configuration property that enanbles/disables the use of enums, if supported by the target
     * java version.
     */
    public final static String CONFIG_PROPERTY_USE_ENUMS = "useEnums";

    private TableImplBuilder tableImplBuilder;
    private TableRowBuilder tableRowBuilder;
    private PolicyCmptInterfaceBuilder policyCmptInterfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    private EnumClassesBuilder enumClassesBuilder;
    private String version;
    private Map ipsObjectTypeGenerators = new HashMap(1000);

    public StandardBuilderSet() {
        initVersion();
    }

    public void afterBuildProcess(int buildKind) throws CoreException {
        clearGenerators();
    }

    public void beforeBuildProcess(int buildKind) throws CoreException {
        clearGenerators();
    }

    private void initVersion() {
        version = "2.0.0";
        // Version versionObj =
        // Version.parseVersion((String)StdBuilderPlugin.getDefault().getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION));
        // StringBuffer buf = new StringBuffer();
        // buf.append(versionObj.getMajor());
        // buf.append('.');
        // buf.append(versionObj.getMinor());
        // buf.append('.');
        // buf.append(versionObj.getMicro());
        // version = buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSupportTableAccess() {
        return true;
    }

    private void clearGenerators() {
        ipsObjectTypeGenerators.clear();
    }

    public GenPolicyCmptType getGenerator(IPolicyCmptType policyCmptType) throws CoreException {
        if (policyCmptType == null) {
            return null;
        }
        GenPolicyCmptType generator = (GenPolicyCmptType)ipsObjectTypeGenerators.get(policyCmptType);
        if (generator == null) {
            generator = new GenPolicyCmptType(policyCmptType, this, new LocalizedStringsSet(GenPolicyCmptType.class));
            ipsObjectTypeGenerators.put(policyCmptType, generator);
        }
        return generator;
    }

    public GenProductCmptType getGenerator(IProductCmptType productCmptType) throws CoreException {
        if (productCmptType == null) {
            return null;
        }
        GenProductCmptType generator = (GenProductCmptType)ipsObjectTypeGenerators.get(productCmptType);
        if (generator == null) {
            generator = new GenProductCmptType(productCmptType, this, new LocalizedStringsSet(GenProductCmptType.class));
            ipsObjectTypeGenerators.put(productCmptType, generator);
        }
        return generator;
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException {
        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        DatatypeHelper returnTypeHelper = fct.getIpsProject().findDatatypeHelper(returnType.getQualifiedName());
        JavaCodeFragment code = new JavaCodeFragment();
        ITableStructure tableStructure = fct.getTableStructure();
        code.append("(("); //$NON-NLS-1$
        code.appendClassName(returnType.getJavaClassName());
        code.append(")"); //$NON-NLS-1$
        code.append("(new "); //$NON-NLS-1$
        code.appendClassName(TableFunctionExecution.class);
        code.append("()"); //$NON-NLS-1$
        code.appendOpenBracket();
        code.append("public Object execute()"); //$NON-NLS-1$
        code.appendOpenBracket();
        code.appendClassName(tableRowBuilder.getQualifiedClassName(tableStructure.getIpsSrcFile()));
        code.append(" row = "); //$NON-NLS-1$
        CompilationResultImpl result = new CompilationResultImpl(code, returnType);
        result.addAllIdentifierUsed(argResults);
        code.appendClassName(tableImplBuilder.getQualifiedClassName(tableStructure.getIpsSrcFile()));
        // create get instance method by using the qualified name of the table content
        code.append(".getInstance(" + MethodNames.GET_REPOSITORY + "(), \"" + tableContents.getQualifiedName() //$NON-NLS-1$ //$NON-NLS-2$
                + "\").findRow("); //$NON-NLS-1$
        // TODO pk: findRow is not correct in general
        for (int i = 0; i < argResults.length; i++) {
            if (i > 0) {
                code.append(", "); //$NON-NLS-1$
            }
            code.append(argResults[i].getCodeFragment());
            result.addMessages(argResults[i].getMessages());
        }
        code.append(");"); //$NON-NLS-1$
        code.appendln();
        code.append("if(row != null)"); //$NON-NLS-1$
        code.appendOpenBracket();
        code.append("return row.get"); //$NON-NLS-1$
        code.append(StringUtils.capitalize(fct.findAccessedColumn().getName()));
        code.append("();"); //$NON-NLS-1$
        code.appendCloseBracket();
        code.append("return "); //$NON-NLS-1$
        code.append(returnTypeHelper.nullExpression());
        code.append(';');
        code.appendCloseBracket();
        code.appendCloseBracket();
        code.append(").execute())"); //$NON-NLS-1$
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public IdentifierResolver createFlIdentifierResolver(IFormula formula) throws CoreException {
        return new AbstractParameterIdentifierResolver(formula) {

            protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
                try {
                    if (datatype instanceof IPolicyCmptType) {
                        return getGenerator((IPolicyCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }
                    if (datatype instanceof IProductCmptType) {
                        return getGenerator((IProductCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }
                } catch (CoreException e) {
                    return null;
                }
                return null;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IFormula formula) throws CoreException {
        return new AbstractParameterIdentifierResolver(formula) {

            protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
                try {
                    if (datatype instanceof IPolicyCmptType) {
                        return getGenerator((IPolicyCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }
                    if (datatype instanceof IProductCmptType) {
                        return getGenerator((IProductCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }
                } catch (CoreException e) {
                    return null;
                }
                return null;
            }

            /**
             * {@inheritDoc}
             */
            protected CompilationResult compile(IParameter param, String attributeName, Locale locale) {
                CompilationResult compile = super.compile(param, attributeName, locale);
                try {
                    Datatype datatype = param.findDatatype(getIpsProject());
                    if (datatype instanceof IType) {
                        // instead of using the types getter method to get the value for an
                        // identifier,
                        // the given datatype plus the attribute will be used as new parameter
                        // identifier,
                        // this parameter identifier will also be used as parameter inside the
                        // formula method
                        // which uses this code fragment
                        String code = param.getName() + "_" + attributeName;
                        return new CompilationResultImpl(code, compile.getDatatype());
                    }
                } catch (CoreException ignored) {
                    // the exception was already handled in the compile method of the super class
                }
                return compile;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public String getPackage(String kind, IIpsSrcFile ipsSrcFile) throws CoreException {
        String returnValue = super.getPackage(kind, ipsSrcFile);
        if (returnValue != null) {
            return returnValue;
        }
        if (IpsObjectType.TABLE_CONTENTS.equals(ipsSrcFile.getIpsObjectType())
                && EnumClassesBuilder.PACKAGE_STRUCTURE_KIND_ID.equals(kind)) {
            return getPackageName(ipsSrcFile);
        }

        if (IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType())
                && EnumTypeInterfaceBuilder.PACKAGE_STRUCTURE_KIND_ID.equals(kind)) {
            return getPackageName(ipsSrcFile);
        }

        throw new IllegalArgumentException("Unexpected kind id " + kind + " for the IpsObjectType: "
                + ipsSrcFile.getIpsObjectType());
    }

    public boolean isSupportFlIdentifierResolver() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsArtefactBuilder[] createBuilders() throws CoreException {
        // create policy component type builders
        PolicyCmptImplClassBuilder policyCmptImplClassBuilder = new PolicyCmptImplClassBuilder(this, KIND_POLICY_CMPT_IMPL);
        policyCmptInterfaceBuilder = new PolicyCmptInterfaceBuilder(this, KIND_POLICY_CMPT_INTERFACE);

        // create product component type builders
        ProductCmptInterfaceBuilder productCmptInterfaceBuilder = new ProductCmptInterfaceBuilder(this,
                KIND_PRODUCT_CMPT_INTERFACE);
        ProductCmptImplClassBuilder productCmptImplClassBuilder = new ProductCmptImplClassBuilder(this,
                KIND_PRODUCT_CMPT_IMPL);
        productCmptGenInterfaceBuilder = new ProductCmptGenInterfaceBuilder(this,
                DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION_INTERFACE);
        ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder = new ProductCmptGenImplClassBuilder(this,
                DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION_IMPL);

        // product component builders
        ProductCmptBuilder productCmptGenerationImplBuilder = new ProductCmptBuilder(this,
                KIND_PRODUCT_CMPT_GENERATION_IMPL);
        IIpsArtefactBuilder productCmptContentCopyBuilder = new ProductCmptXMLBuilder(IpsObjectType.PRODUCT_CMPT, this,
                KIND_PRODUCT_CMPT_CONTENT);

        // table structure builders
        tableImplBuilder = new TableImplBuilder(this, KIND_TABLE_IMPL);
        tableRowBuilder = new TableRowBuilder(this, KIND_TABLE_ROW);
        tableImplBuilder.setTableRowBuilder(tableRowBuilder);

        // table content builders
        IIpsArtefactBuilder tableContentCopyBuilder = new TableContentBuilder(this, KIND_TABLE_CONTENT);

        // test case type builders
        TestCaseTypeClassBuilder testCaseTypeClassBuilder = new TestCaseTypeClassBuilder(this,
                KIND_TEST_CASE_TYPE_CLASS);

        // test case builder
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder(this);

        // formula test builder
        FormulaTestBuilder formulaTestBuilder = new FormulaTestBuilder(this, KIND_FORMULA_TEST_CASE);

        // toc file builder
        TocFileBuilder tocFileBuilder = new TocFileBuilder(this);

        EnumTypeInterfaceBuilder enumTypeInterfaceBuilder = new EnumTypeInterfaceBuilder(this,
                EnumTypeInterfaceBuilder.PACKAGE_STRUCTURE_KIND_ID);
        enumClassesBuilder = new EnumClassesBuilder(this, EnumClassesBuilder.PACKAGE_STRUCTURE_KIND_ID,
                enumTypeInterfaceBuilder);
        //
        // wire up the builders
        //

        // policy component type builders

        // product component type builders
        productCmptGenImplClassBuilder.setTableImplBuilder(tableImplBuilder);

        // product component builders.
        productCmptGenerationImplBuilder.setProductCmptImplBuilder(productCmptImplClassBuilder);
        productCmptGenerationImplBuilder.setProductCmptGenImplBuilder(productCmptGenImplClassBuilder);

        // test case builder
        testCaseBuilder.setJavaSourceFileBuilder(policyCmptImplClassBuilder);

        // formula test builder
        formulaTestBuilder.setProductCmptInterfaceBuilder(productCmptInterfaceBuilder);
        formulaTestBuilder.setProductCmptBuilder(productCmptGenerationImplBuilder);
        formulaTestBuilder.setProductCmptGenImplClassBuilder(productCmptGenImplClassBuilder);

        // toc file builders
        tocFileBuilder.setProductCmptTypeImplClassBuilder(productCmptImplClassBuilder);
        tocFileBuilder.setProductCmptBuilder(productCmptGenerationImplBuilder);
        tocFileBuilder.setProductCmptGenImplClassBuilder(productCmptGenImplClassBuilder);
        tocFileBuilder.setTableImplBuilder(tableImplBuilder);
        tocFileBuilder.setTestCaseTypeClassBuilder(testCaseTypeClassBuilder);
        tocFileBuilder.setTestCaseBuilder(testCaseBuilder);
        tocFileBuilder.setFormulaTestBuilder(formulaTestBuilder);

        return new IIpsArtefactBuilder[] { tableImplBuilder, tableRowBuilder, productCmptGenInterfaceBuilder,
                productCmptGenImplClassBuilder, productCmptInterfaceBuilder, productCmptImplClassBuilder,
                policyCmptImplClassBuilder, policyCmptInterfaceBuilder, productCmptGenerationImplBuilder,
                tableContentCopyBuilder, productCmptContentCopyBuilder, testCaseTypeClassBuilder, testCaseBuilder,
                formulaTestBuilder, enumClassesBuilder, enumTypeInterfaceBuilder, tocFileBuilder };
    }

    public DatatypeHelper getDatatypeHelperForTableBasedEnum(TableContentsEnumDatatypeAdapter datatype) {
        return new TableContentsEnumDatatypeHelper(datatype, enumClassesBuilder);
    }

    /**
     * Returns the standard builder plugin version in the format [major.minor.mico]. The version
     * qualifier is not included in the version string.
     */
    public String getVersion() {
        return version;
    }

    public boolean isGenerateChangeListener() {
        return getConfig().getBooleanPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER, false);    }

    /**
     * {@inheritDoc}
     */
    public EnumTypeTargetJavaVersion getTargetJavaVersion() {
        return EnumTypeTargetJavaVersion.valueOf(getConfig().getPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_TARGET_JAVA_VERSION));
    }

    /**
     * Returns if Java 5 enums shall be used in the code generated by this builder.
     */
    public boolean isUseEnums() {
        return getTargetJavaVersion().isAtLeast(EnumTypeTargetJavaVersion.JAVA_5)
            && getConfig().getBooleanPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_USE_ENUMS, false);    
    }

    /**
     * Returns if Java 5 typesafe collections shall be used in the code generated by this builder.
     */
    public boolean isUseTypesafeCollections() {
        return getTargetJavaVersion().isAtLeast(EnumTypeTargetJavaVersion.JAVA_5)
            && getConfig().getBooleanPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_USE_TYPESAFE_COLLECTIONS, false);    
    }
}
