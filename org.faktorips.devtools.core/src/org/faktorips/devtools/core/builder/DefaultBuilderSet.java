/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

/**
 * A default implementation that extends the AbstractBuilderSet and implements the
 * IJavaPackageStructure interface. The getPackage() method provides package names for the kind
 * constants defined in this DefaultBuilderSet. This implementation uses the base package name for
 * generated java classes as the root of the package structure. The base package name can be
 * configure for an IPS project within the ipsproject.xml file. On top of the base package name it
 * adds the IPS package fragment name of the IpsSrcFile in question. Internal packages are
 * distinguished from packages that contain published interfaces and classes. It depends on the kind
 * constant if an internal or published package name is returned.
 * 
 * @author Peter Erzberger
 */
public abstract class DefaultBuilderSet extends AbstractBuilderSet implements IJavaPackageStructure {

    /**
     * Name of the configuration property that indicates whether to generate public interfaces or
     * not.
     * <p>
     * Although this property is defined in this abstraction it needs to be configured in the
     * extension point of every specific builder. If it is not specified as a configuration
     * definition of any builder, the default value is <code>true</code>.
     */
    public static final String CONFIG_PROPERTY_PUBLISHED_INTERFACES = "generatePublishedInterfaces"; //$NON-NLS-1$

    /**
     * Configuration property that defines the @SupressWarning tag that is generated above all
     * generated methods of {@link PolicyCmptType}, {@link ProductCmptType}, {@link EnumType} ,
     * {@link TableStructure} and {@link TableContents}
     */
    public static final String CONFIG_PROPERTY_SUPPRESS_WARNING_ANNOTATION = "suppressWarningAnnotation"; //$NON-NLS-1$

    private static final String PARENTHESIS_CHARACTER = "("; //$NON-NLS-1$

    private static final String SEMI_COLON_CHARACTER = ";"; //$NON-NLS-1$

    private JavaPackageStructure javaPackageStructure = new JavaPackageStructure();

    private List<String> additionalAnnotations;

    private List<String> additionalImports;

    @Override
    public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) {
        if (root == null) {
            return null;
        }
        if (!root.isBasedOnSourceFolder()) {
            return null;
        }
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        String basePackInternal = javaPackageStructure.getBasePackageName(entry, true, false);
        IPath path = QNameUtil.toPath(basePackInternal);
        path = path.append(entry.getBasePackageRelativeTocPath());
        IFolder tocFileLocation = getTocFileLocation(root);
        if (tocFileLocation == null) {
            return null;
        }
        return tocFileLocation.getFile(path);
    }

    private IFolder getTocFileLocation(IIpsPackageFragmentRoot root) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getOutputFolderForDerivedJavaFiles();
    }

    @Override
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) {
        IFile tocFile = getRuntimeRepositoryTocFile(root);
        if (tocFile == null) {
            return null;
        }
        IFolder tocFileLocation = getTocFileLocation(root);
        return tocFile.getFullPath().removeFirstSegments(tocFileLocation.getFullPath().segmentCount()).toString();
    }

    @Override
    public String getPackageName(IIpsSrcFile ipsSrcFile, boolean internalArtifacts, boolean mergableArtifacts) {
        return javaPackageStructure.getPackageName(ipsSrcFile, internalArtifacts, mergableArtifacts);
    }

    @Override
    public String getBasePackageName(IIpsSrcFolderEntry entry, boolean internalArtifacts, boolean mergableArtifacts) {
        return javaPackageStructure.getBasePackageName(entry, internalArtifacts, mergableArtifacts);
    }

    public boolean isGeneratePublishedInterfaces() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(CONFIG_PROPERTY_PUBLISHED_INTERFACES);
        return propertyValueAsBoolean == null ? true : propertyValueAsBoolean.booleanValue();
    }

    @Override
    public boolean isSupportTableAccess() {
        return false;
    }

    /**
     * Empty implementation. Might be overridden by subclasses that support the formula language.
     */
    @Override
    public CompilationResult<JavaCodeFragment> getTableAccessCode(String tableContentsQualifiedName,
            ITableAccessFunction fct,
            CompilationResult<JavaCodeFragment>[] argResults) throws CoreException {

        return null;
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return false;
    }

    @Override
    public IdentifierResolver<JavaCodeFragment> createFlIdentifierResolver(IExpression formula,
            ExprCompiler<JavaCodeFragment> exprCompiler) throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>null</code>. This method is supposed to be overridden by subclasses.
     */
    @Override
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns an empty string. This method is supposed to be overridden by subclasses.
     */
    @Override
    public String getVersion() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public IPersistenceProvider getPersistenceProvider() {
        return null;
    }

    @Override
    public void beforeBuildProcess(int buildKind) throws CoreException {
        super.beforeBuildProcess(buildKind);
        initAdditionalImports();
        initAdditionalAnnotations();
    }

    private void initAdditionalImports() {
        additionalImports = new ArrayList<String>();
        List<String> splitInput = splitString(getConfiguredSuppressWarningAnnotation());
        for (String splitString : splitInput) {
            if (!splitString.equals(QNameUtil.getUnqualifiedName(splitString))) {
                additionalImports.add(removeParenthesis(splitString));
            }
        }
    }

    private List<String> splitString(String input) {
        List<String> splitInput = new ArrayList<String>();
        String[] split = input.split(SEMI_COLON_CHARACTER);
        for (String string : split) {
            splitInput.add(string.trim());
        }
        return splitInput;
    }

    /* private */String getConfiguredSuppressWarningAnnotation() {
        String propertyValueAsString = getConfig()
                .getPropertyValueAsString(CONFIG_PROPERTY_SUPPRESS_WARNING_ANNOTATION);
        return propertyValueAsString == null ? StringUtils.EMPTY : propertyValueAsString;
    }

    private String removeParenthesis(String importWithParenthesis) {
        if (importWithParenthesis.contains(PARENTHESIS_CHARACTER)) {
            int bracket = importWithParenthesis.indexOf(PARENTHESIS_CHARACTER);
            return importWithParenthesis.substring(0, bracket);
        }
        return importWithParenthesis;
    }

    private void initAdditionalAnnotations() {
        additionalAnnotations = new ArrayList<String>();
        List<String> splitInput = splitString(getConfiguredSuppressWarningAnnotation());
        for (String splitString : splitInput) {
            String unqualifiedName = QNameUtil.getUnqualifiedName(splitString);
            additionalAnnotations.add(unqualifiedName);
        }
    }

    public List<String> getAdditionalImports() {
        return additionalImports;
    }

    public List<String> getAdditionalAnnotations() {
        return additionalAnnotations;
    }
}
