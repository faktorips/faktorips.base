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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
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

    private JavaPackageStructureAdapter javaPackageStructureAdaptor = new JavaPackageStructureAdapter();

    @Override
    public String getInternalPackage(final String basePackName, final String subPackageFragment) {
        return JavaClassNaming.getInternalPackage(basePackName, subPackageFragment);
    }

    @Override
    public String getTocFilePackageName(IIpsPackageFragmentRoot root) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        String basePackName = entry.getBasePackageNameForDerivedJavaClasses();
        return getInternalPackage(basePackName, StringUtils.EMPTY);
    }

    @Override
    public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) {
        if (root == null) {
            return null;
        }
        if (!root.isBasedOnSourceFolder()) {
            return null;
        }
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        String basePackInternal = getTocFilePackageName(root);
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
    public String getPackage(IIpsArtefactBuilder builder, IIpsSrcFile ipsSrcFile) throws CoreException {
        javaPackageStructureAdaptor = new JavaPackageStructureAdapter();
        return javaPackageStructureAdaptor.getPackage(builder, ipsSrcFile);
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

    @Override
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IExpression formula, ExprCompiler exprCompiler)
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
