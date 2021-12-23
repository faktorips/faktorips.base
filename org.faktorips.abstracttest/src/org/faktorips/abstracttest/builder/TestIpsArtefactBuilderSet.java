/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.builder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.builder.GenericBuilderKindId;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.builder.flidentifier.AbstractIdentifierResolver;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGenerator;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

public class TestIpsArtefactBuilderSet extends DefaultBuilderSet {

    public static final String ID = "testbuilderset";

    private boolean inverseRelationLinkRequiredFor2WayCompositions;

    private boolean roleNamePluralRequiredForTo1Relations;

    private boolean isAggregateRootBuilder;

    private final LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> ipsArtefactBuilders;

    /**
     * You can put any object for any key into this map. Some of the test methods in this test
     * builder set use this map to return test results instead of null. Just put the expected method
     * parameter into this map and the method would return the corresponding value.
     * 
     */
    public Map<Object, Object> testObjectsMap = new HashMap<>();

    public TestIpsArtefactBuilderSet() throws CoreRuntimeException {
        this(new IIpsArtefactBuilder[0]);
    }

    public TestIpsArtefactBuilderSet(IIpsArtefactBuilder[] builders) throws CoreRuntimeException {
        super();
        ipsArtefactBuilders = new LinkedHashMap<>();
        for (IIpsArtefactBuilder ipsArtefactBuilder : builders) {
            ipsArtefactBuilders.put(new GenericBuilderKindId(), ipsArtefactBuilder);
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_PROPERTY_GENERATOR_LOCALE, Locale.GERMAN.getLanguage());
        TestBuilderSetConfig config = new TestBuilderSetConfig(properties);
        initialize(config);
    }

    @Override
    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreRuntimeException {
        if (getConfig() == null) {
            super.initialize(config);
        }
    }

    @Override
    public TestBuilderSetConfig getConfig() {
        return (TestBuilderSetConfig)super.getConfig();
    }

    @Override
    protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() throws CoreRuntimeException {
        return ipsArtefactBuilders;
    }

    public void setAggregateRootBuilder(boolean enable) {
        isAggregateRootBuilder = enable;
    }

    @Override
    public boolean containsAggregateRootBuilder() {
        return isAggregateRootBuilder;
    }

    @Override
    public boolean isInverseRelationLinkRequiredFor2WayCompositions() {
        return inverseRelationLinkRequiredFor2WayCompositions;
    }

    public void setInverseRelationLinkRequiredFor2WayCompositions(
            boolean inverseRelationLinkRequiredFor2WayCompositions) {
        this.inverseRelationLinkRequiredFor2WayCompositions = inverseRelationLinkRequiredFor2WayCompositions;
    }

    @Override
    public boolean isRoleNamePluralRequiredForTo1Relations() {
        return roleNamePluralRequiredForTo1Relations;
    }

    public void setRoleNamePluralRequiredForTo1Relations(boolean roleNamePluralRequiredForTo1Relations) {
        this.roleNamePluralRequiredForTo1Relations = roleNamePluralRequiredForTo1Relations;
    }

    @Override
    public boolean isGeneratePublishedInterfaces() {
        return true;
    }

    @Override
    protected String getConfiguredAdditionalAnnotations() {
        return StringUtils.EMPTY;
    }

    @Override
    public void setId(String id) {

    }

    @Override
    public void setLabel(String label) {

    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getLabel() {
        return getId();
    }

    @Override
    public boolean isSupportTableAccess() {
        return false;
    }

    @Override
    public CompilationResult<JavaCodeFragment> getTableAccessCode(String tableContentsQualifiedName,
            ITableAccessFunction fct,
            CompilationResult<JavaCodeFragment>[] argResults) throws CoreRuntimeException {
        return null;
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return false;
    }

    @Override
    public IdentifierResolver<JavaCodeFragment> createFlIdentifierResolver(IExpression formula,
            ExprCompiler<JavaCodeFragment> exprCompiler) throws CoreRuntimeException {
        return new TestParameterIdentifierResolver(formula, exprCompiler);
    }

    @Override
    public AFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) {
        return (AFile)testObjectsMap.get(root);
    }

    public String getTocFilePackageName(IIpsPackageFragmentRoot root) {
        return (String)testObjectsMap.get(root);
    }

    @Override
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) {
        return (String)testObjectsMap.get(root);
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        return (DatatypeHelper)testObjectsMap.get(datatype);
    }

    private final class TestParameterIdentifierResolver extends AbstractIdentifierResolver<JavaCodeFragment> {
        private TestParameterIdentifierResolver(IExpression formula2, ExprCompiler<JavaCodeFragment> exprCompiler) {
            super(formula2, exprCompiler);
        }

        @Override
        protected IdentifierNodeGeneratorFactory<JavaCodeFragment> getGeneratorFactory() {
            return new IdentifierNodeGeneratorFactory<>() {

                @Override
                public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForParameterNode() {
                    return new DummyIdentifierNodeGenerator(this);
                }

                @Override
                public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForAssociationNode() {
                    return new DummyIdentifierNodeGenerator(this);
                }

                @Override
                public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForAttributeNode() {
                    return new DummyIdentifierNodeGenerator(this);
                }

                @Override
                public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForEnumClassNode() {
                    return new DummyIdentifierNodeGenerator(this);
                }

                @Override
                public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForEnumValueNode() {
                    return new DummyIdentifierNodeGenerator(this);
                }

                @Override
                public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForIndexBasedAssociationNode() {
                    return new DummyIdentifierNodeGenerator(this);
                }

                @Override
                public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForQualifiedAssociationNode() {
                    return new DummyIdentifierNodeGenerator(this);
                }

                @Override
                public IdentifierNodeGenerator<JavaCodeFragment> getGeneratorForInvalidNode() {
                    return new DummyIdentifierNodeGenerator(this);
                }
            };
        }

        @Override
        protected CompilationResult<JavaCodeFragment> getStartingCompilationResult() {
            return new CompilationResultImpl("this", getExpression().findProductCmptType(getIpsProject())); //$NON-NLS-1$
        }
    }

    private static class DummyIdentifierNodeGenerator extends IdentifierNodeGenerator<JavaCodeFragment> {

        public DummyIdentifierNodeGenerator(IdentifierNodeGeneratorFactory<JavaCodeFragment> factory) {
            super(factory);
        }

        @Override
        protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
                CompilationResult<JavaCodeFragment> contextCompilationResult) {
            return contextCompilationResult;
        }

    }

}
