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

package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
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
    public final static String CONFIG_PROPERTY_PUBLISHED_INTERFACES = "generatePublishedInterfaces"; //$NON-NLS-1$

    private JavaPackageStructure javaPackageStructure = new JavaPackageStructure();

    @Override
    public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) {
        if (root == null) {
            return null;
        }
        if (!root.isBasedOnSourceFolder()) {
            return null;
        }
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        String basePackInternal = javaPackageStructure.getBasePackageName(entry, false, false);
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
    public String getPackageName(IIpsSrcFile ipsSrcFile, boolean publishedArtifacts, boolean mergableArtifacts) {
        return javaPackageStructure.getPackageName(ipsSrcFile, publishedArtifacts, mergableArtifacts);
    }

    @Override
    public String getBasePackageName(IIpsSrcFolderEntry entry, boolean publishedArtifact, boolean mergableArtifacts) {
        return javaPackageStructure.getBasePackageName(entry, publishedArtifact, mergableArtifacts);
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
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException {

        return null;
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return false;
    }

    @Override
    public IdentifierResolver createFlIdentifierResolver(IExpression formula, ExprCompiler exprCompiler)
            throws CoreException {
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

}
