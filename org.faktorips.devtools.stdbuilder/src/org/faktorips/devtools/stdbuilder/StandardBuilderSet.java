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

package org.faktorips.devtools.stdbuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.internal.model.TableContentsEnumDatatypeAdapter;
import org.faktorips.devtools.core.internal.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
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
import org.faktorips.devtools.stdbuilder.bf.BusinessFunctionBuilder;
import org.faktorips.devtools.stdbuilder.enums.EnumClassesBuilder;
import org.faktorips.devtools.stdbuilder.enums.EnumTypeInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
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
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.internal.MethodNames;

/**
 * An <code>IpsArtefactBuilderSet</code> implementation that assembles the standard Faktor-IPS
 * artefact builders.
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
     * Configuration property that enables/disables the use of typesafe collections, if supported by
     * the target java version.
     */
    public final static String CONFIG_PROPERTY_USE_TYPESAFE_COLLECTIONS = "useTypesafeCollections";

    /**
     * Configuration property that enables/disables the use of enums, if supported by the target
     * java version.
     */
    public final static String CONFIG_PROPERTY_USE_ENUMS = "useJavaEnumTypes";

    /**
     * Configuration property that enables/disables the generation of JAXB support.
     */
    public final static String CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT = "generateJaxbSupport";

    private TableImplBuilder tableImplBuilder;
    private TableRowBuilder tableRowBuilder;
    private PolicyCmptInterfaceBuilder policyCmptInterfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;
    private EnumClassesBuilder enumClassesBuilder;
    private EnumTypeBuilder enumTypeBuilder;
    private String version;
    private final Map<IType, GenType> ipsObjectTypeGenerators = new HashMap<IType, GenType>(1000);

    public StandardBuilderSet() {
        initVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterBuildProcess(int buildKind) throws CoreException {
        clearGenerators();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeBuildProcess(int buildKind) throws CoreException {
        clearGenerators();
    }

    private void initVersion() {
        version = "2.2.0";
        // Version versionObj =
        //Version.parseVersion((String)StdBuilderPlugin.getDefault().getBundle().getHeaders().get(org
        // .osgi.framework.Constants.BUNDLE_VERSION));
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
    @Override
    public boolean isSupportTableAccess() {
        return true;
    }

    private void clearGenerators() {
        ipsObjectTypeGenerators.clear();
    }

    public GenType getGenerator(IType type) throws CoreException {
        if (type == null) {
            return null;
        }

        if (type instanceof IPolicyCmptType) {
            return getGenerator((IPolicyCmptType)type);
        }
        if (type instanceof IProductCmptType) {
            return getGenerator((IProductCmptType)type);
        }

        throw new CoreException(new IpsStatus("Unkown subclass " + type.getClass()));
    }

    public GenPolicyCmptType getGenerator(IPolicyCmptType policyCmptType) throws CoreException {
        if (policyCmptType == null) {
            return null;
        }

        GenPolicyCmptType generator = (GenPolicyCmptType)ipsObjectTypeGenerators.get(policyCmptType);
        if (generator == null) {
            generator = new GenPolicyCmptType(policyCmptType, this);
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
            generator = new GenProductCmptType(productCmptType, this);
            ipsObjectTypeGenerators.put(productCmptType, generator);
        }

        return generator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException {

        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        JavaCodeFragment code = new JavaCodeFragment();
        ITableStructure tableStructure = fct.getTableStructure();

        CompilationResultImpl result = new CompilationResultImpl(code, returnType);
        result.addAllIdentifierUsed(argResults);
        code.appendClassName(tableImplBuilder.getQualifiedClassName(tableStructure.getIpsSrcFile()));
        // create get instance method by using the qualified name of the table content
        code.append(".getInstance(" + MethodNames.GET_REPOSITORY + "(), \"" + tableContents.getQualifiedName() //$NON-NLS-1$ //$NON-NLS-2$
                + "\").findRowNullRowReturnedForEmtpyResult(");

        // TODO pk: findRow is not correct in general
        for (int i = 0; i < argResults.length; i++) {
            if (i > 0) {
                code.append(", ");
            }
            code.append(argResults[i].getCodeFragment());
            result.addMessages(argResults[i].getMessages());
        }
        code.append(").get");
        code.append(StringUtils.capitalize(fct.findAccessedColumn().getName()));
        code.append("()");

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IFormula formula) throws CoreException {
        return new AbstractParameterIdentifierResolver(formula) {

            /**
             * {@inheritDoc}
             */
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
            @Override
            protected CompilationResult compile(IParameter param, String attributeName, Locale locale) {
                CompilationResult compile = super.compile(param, attributeName, locale);
                try {
                    Datatype datatype = param.findDatatype(getIpsProject());
                    if (datatype instanceof IType) {
                        /*
                         * instead of using the types getter method to get the value for an
                         * identifier, the given datatype plus the attribute will be used as new
                         * parameter identifier, this parameter identifier will also be used as
                         * parameter inside the formula method which uses this code fragment
                         */
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
    @Override
    public String getPackage(String kind, IIpsSrcFile ipsSrcFile) throws CoreException {
        String returnValue = super.getPackage(kind, ipsSrcFile);
        if (returnValue != null) {
            return returnValue;
        }

        IpsObjectType objectType = ipsSrcFile.getIpsObjectType();
        if (IpsObjectType.TABLE_CONTENTS.equals(objectType)
                && EnumClassesBuilder.PACKAGE_STRUCTURE_KIND_ID.equals(kind)) {
            return getPackageName(ipsSrcFile);
        }
        if (IpsObjectType.TABLE_STRUCTURE.equals(objectType)
                && EnumTypeInterfaceBuilder.PACKAGE_STRUCTURE_KIND_ID.equals(kind)) {
            return getPackageName(ipsSrcFile);
        }
        if (BusinessFunctionIpsObjectType.getInstance().equals(objectType)) {
            return getPackageName(ipsSrcFile);
        }
        if (IpsObjectType.ENUM_TYPE.equals(objectType)) {
            return getPackageName(ipsSrcFile);
        }

        throw new IllegalArgumentException("Unexpected kind id " + kind + " for the IpsObjectType: " + objectType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupportFlIdentifierResolver() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsArtefactBuilder[] createBuilders() throws CoreException {
        // create policy component type builders
        PolicyCmptImplClassBuilder policyCmptImplClassBuilder = new PolicyCmptImplClassBuilder(this,
                KIND_POLICY_CMPT_IMPL);
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

        BusinessFunctionBuilder businessFunctionBuilder = new BusinessFunctionBuilder(this,
                BusinessFunctionBuilder.PACKAGE_STRUCTURE_KIND_ID);
        //
        // wire up the builders
        //

        // policy component type builders

        // New enum type builder
        enumTypeBuilder = new EnumTypeBuilder(this);

        IIpsArtefactBuilder enumContentBuilder = new XmlContentFileCopyBuilder(IpsObjectType.ENUM_CONTENT, this, KIND_ENUM_CONTENT);

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
        tocFileBuilder.setPolicyCmptImplClassBuilder(policyCmptImplClassBuilder);
        tocFileBuilder.setProductCmptTypeImplClassBuilder(productCmptImplClassBuilder);
        tocFileBuilder.setProductCmptBuilder(productCmptGenerationImplBuilder);
        tocFileBuilder.setProductCmptGenImplClassBuilder(productCmptGenImplClassBuilder);
        tocFileBuilder.setTableImplBuilder(tableImplBuilder);
        tocFileBuilder.setTestCaseTypeClassBuilder(testCaseTypeClassBuilder);
        tocFileBuilder.setTestCaseBuilder(testCaseBuilder);
        tocFileBuilder.setFormulaTestBuilder(formulaTestBuilder);
        tocFileBuilder.setEnumTypeBuilder(enumTypeBuilder);

        if (ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())) {
            ModelTypeXmlBuilder policyModelTypeBuilder = new ModelTypeXmlBuilder(IpsObjectType.POLICY_CMPT_TYPE, this,
                    KIND_MODEL_TYPE);
            ModelTypeXmlBuilder productModelTypeBuilder = new ModelTypeXmlBuilder(IpsObjectType.PRODUCT_CMPT_TYPE,
                    this, KIND_MODEL_TYPE);
            tocFileBuilder.setPolicyModelTypeXmlBuilder(policyModelTypeBuilder);
            tocFileBuilder.setProductModelTypeXmlBuilder(productModelTypeBuilder);
            tocFileBuilder.setGenerateEntriesForModelTypes(true);
            return new IIpsArtefactBuilder[] { tableImplBuilder, tableRowBuilder, productCmptGenInterfaceBuilder,
                    productCmptGenImplClassBuilder, productCmptInterfaceBuilder, productCmptImplClassBuilder,
                    policyCmptImplClassBuilder, policyCmptInterfaceBuilder, productCmptGenerationImplBuilder,
                    tableContentCopyBuilder, productCmptContentCopyBuilder, testCaseTypeClassBuilder, testCaseBuilder,
                    formulaTestBuilder, enumClassesBuilder, enumTypeInterfaceBuilder, tocFileBuilder,
                    policyModelTypeBuilder, productModelTypeBuilder, businessFunctionBuilder, enumTypeBuilder, enumContentBuilder};
        } else {
            tocFileBuilder.setGenerateEntriesForModelTypes(false);
            return new IIpsArtefactBuilder[] { tableImplBuilder, tableRowBuilder, productCmptGenInterfaceBuilder,
                    productCmptGenImplClassBuilder, productCmptInterfaceBuilder, productCmptImplClassBuilder,
                    policyCmptImplClassBuilder, policyCmptInterfaceBuilder, productCmptGenerationImplBuilder,
                    tableContentCopyBuilder, productCmptContentCopyBuilder, testCaseTypeClassBuilder, testCaseBuilder,
                    formulaTestBuilder, enumClassesBuilder, enumTypeInterfaceBuilder, tocFileBuilder,
                    businessFunctionBuilder, enumTypeBuilder, enumContentBuilder};
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatatypeHelper getDatatypeHelperForTableBasedEnum(TableContentsEnumDatatypeAdapter datatype) {
        return new TableContentsEnumDatatypeHelper(datatype, enumClassesBuilder);
    }

    /**
     * {@inheritDoc}
     */
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return new EnumTypeDatatypeHelper(enumTypeBuilder, datatypeAdapter);
    }
    
    /**
     * Returns the standard builder plugin version in the format [major.minor.micro]. The version
     * qualifier is not included in the version string.
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Returns whether Java5 enums shall be used in the code generated by this builder.
     */
    public boolean isUseEnums() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_USE_ENUMS).booleanValue();
    }

    /**
     * Returns if Java 5 typesafe collections shall be used in the code generated by this builder.
     */
    public boolean isUseTypesafeCollections() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_USE_TYPESAFE_COLLECTIONS)
                .booleanValue();
    }

    /**
     * Returns whether JAXB support is to be generated by this builder.
     */
    public boolean isGenerateJaxbSupport() {
        return getConfig().getPropertyValueAsBoolean(CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT);
    }

    public String getJavaClassName(Datatype datatype) throws CoreException {
        if (datatype instanceof IPolicyCmptType) {
            return getGenerator((IPolicyCmptType)datatype).getQualifiedName(true);
        }

        if (datatype instanceof IProductCmptType) {
            return getGenerator((IProductCmptType)datatype).getQualifiedName(true);
        }

        return datatype.getJavaClassName();
    }

}
