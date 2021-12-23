/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.builder.IpsBuilder;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

/**
 * Interface for the extension point <code>org.faktorips.plugin.artefactbuilderset</code>, provided
 * by this plug-in.
 * <p>
 * Only one implementation of this interface can be registered to this extension point. If more than
 * one extension is declared only the first one will be registered and the others will be ignored.
 * <p>
 * An <code>IIpsArtefactBuilderSet</code> collects a list of <code>IIpsArtefactBuilders</code> and
 * makes them available to the build system.
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSet {

    /** The XML element name. */
    public static final String XML_ELEMENT = "IpsArtefactBuilderSet"; //$NON-NLS-1$

    /**
     * Returns the <code>IIpsArtefactBuilderSetConfig</code> that configures this builder set. The
     * configuration is available after the initialize method has been called.
     * 
     * @since 2.1
     */
    public IIpsArtefactBuilderSetConfig getConfig();

    /**
     * Returns all <code>IIpsArtefactBuilders</code> of this set.
     * <p>
     * An empty array has to be returned, if the set is empty.
     */
    public IIpsArtefactBuilder[] getArtefactBuilders();

    /**
     * Returns <code>true</code> if the builder set supports table access functions, otherwise
     * false.
     */
    public boolean isSupportTableAccess();

    /**
     * Returns <code>true</code> if the builder set supports a formula language identifier resolver.
     */
    public boolean isSupportFlIdentifierResolver();

    /**
     * Returns <code>true</code> if this builder set requires role names in plural form, even for
     * relations with a max cardinality of 1.
     */
    public boolean isRoleNamePluralRequiredForTo1Relations();

    /**
     * Returns <code>true</code> if this artefact builder set requires that master-to-detail
     * compositions contain a reference to an inverse detail-to-master relation.
     * <p>
     * The standard Faktor-IPS generator doesn't need this link. See the artikel on modeling
     * relations for further details.
     */
    public boolean isInverseRelationLinkRequiredFor2WayCompositions();

    /**
     * @return <code>true</code> if the validations for the Table as Enum-Datatype is required (as
     *         by the standard Faktor-IPS generator, <code>false</code> otherwise.
     */
    public boolean isTableBasedEnumValidationRequired();

    /**
     * If there is any persistence provider configured for this builder set, this method returns it.
     * May return <code>null</code>if there is no configured persistence provider or the build set
     * does not support persistence providers.
     * 
     * @return The currently configured persistence provider.
     * @see IPersistenceProvider
     */
    public IPersistenceProvider getPersistenceProvider();

    /**
     * Returns a compilation result that gives access to a table via the indicated function. Returns
     * <code>null</code> if this builder set does not support table access.
     * 
     * @param fct The table access function code should be generated for.
     * @param argResults Compilation Results for the function's arguments.
     * 
     * @throws CoreRuntimeException if an error occurs while generating the code.
     */
    public CompilationResult<JavaCodeFragment> getTableAccessCode(String tableContentsQualifiedName,
            ITableAccessFunction fct,
            CompilationResult<JavaCodeFragment>[] argResults) throws CoreRuntimeException;

    /**
     * Creates an<code>IdentifierResolver</code> used to resolve identifiers in the given formula.
     * Returns <code>null</code> if this builder set doesn't support an formula language identifier
     * resolver.
     * 
     * @param exprCompiler can be used by the {@link IdentifierResolver} to ask for properties or
     *            services that are necessary to be able to resolve an identifier properly
     */
    public IdentifierResolver<JavaCodeFragment> createFlIdentifierResolver(IExpression expression,
            ExprCompiler<JavaCodeFragment> exprCompiler) throws CoreRuntimeException;

    /**
     * Returns the file that contain the runtime repository toc file. Note that the file might not
     * exists.
     */
    public AFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) throws CoreRuntimeException;

    /**
     * Returns the name of the resource containing the root's table of contents at runtime. E.g.
     * "org.faktorips.sample.internal.sample-toc.xml". This returned path can be used to create a
     * ClassloaderRuntimeRepository. Returns <code>null</code> if this builder does not generate
     * TOCs or this root is not a root based on a source folder. Returns <code>null</code> if the
     * root is <code>null</code>.
     * 
     * @see org.faktorips.runtime.ClassloaderRuntimeRepository#create(String)
     */
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root);

    /**
     * Returns the locale of the language that is used by the generator to generate source code and
     * documentation.
     */
    public Locale getLanguageUsedInGeneratedSourceCode();

    /**
     * When the builder set is loaded by the Faktor-IPS plug-in the extension id is set by means of
     * this method. This method is called before initialization.
     * 
     * @param id the extension id
     */
    public void setId(String id);

    /**
     * When the builder set is loaded by the Faktor-IPS plug-in the extension description label is
     * set by means of this method. This method is called before initialization.
     * 
     * @param label the extension description label
     */
    public void setLabel(String label);

    /**
     * Returns the extension id declared in the plug-in descriptor.
     */
    public String getId();

    /**
     * Returns the extension description label declared in the plug-in descriptor.
     */
    public String getLabel();

    /**
     * This version indicates the version of the generated code. That means changes to the generator
     * that do not change the generated code a not reflected in this version identifier.
     */
    public String getVersion();

    /**
     * Initializes this set. Creation of IpsArtefactBuilders has to go here instead of the
     * constructor of the set implementation.
     * 
     * @param config the configuration for this builder set instance. The configuration for a
     *            builder set instance is defined in the .ipsproject file of an IPS project.
     * 
     * @throws NullPointerException if <code>config</code> is <code>null</code>.
     * 
     * @see IIpsArtefactBuilderSetConfig class description
     */
    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreRuntimeException;

    /**
     * Subclasses should re-implement this method if an aggregate root builder is contained
     * 
     * @return <code>true</code> if this builder set contains an builder which requires to be called
     *         for the aggregate root object if any child (regardless of the relations depth) has
     *         been modified. Only composite-relations are allowed for this dependency scan.
     *         <code>false</code> if only builder are contained which need only a dependency scan of
     *         depth one.
     */
    public boolean containsAggregateRootBuilder();

    /**
     * The IpsProject for which this builder set is registered.
     */
    public void setIpsProject(IIpsProject ipsProject);

    /**
     * Returns the IpsProject for which this builder set is registered.
     */
    public IIpsProject getIpsProject();

    /**
     * This method is called when the build process starts for this builder set. This method is
     * called before the <code>beforeBuildProcess(IIpsProject, int)</code> method of the registered
     * IpsArtefactBuilders will be called.
     * 
     * @param buildKind One of the build kinds defined in
     *            <code>org.eclipse.core.resources.IncrementalProjectBuilder</code>
     * 
     * @throws CoreRuntimeException implementations can throw or delegate rising CoreExceptions.
     *             Throwing a CoreException or RuntimeException will interrupt the build cycle
     */
    public void beforeBuildProcess(ABuildKind buildKind) throws CoreRuntimeException;

    /**
     * This method is called when the build process is finished for this builder set. It is called
     * after the <code>afterBuildProcess(IIpsProject, int)</code> methods on the registered
     * <code>IIpsArtefactBuilders</code> were called.
     * 
     * @param buildKind One of the build kinds defined in
     *            <code>org.eclipse.core.resources.IncrementalProjectBuilder</code>
     * 
     * @throws CoreRuntimeException implementations can throw or delegate rising CoreExceptions.
     */
    public void afterBuildProcess(ABuildKind buildKind) throws CoreRuntimeException;

    /**
     * Returns an array of builders which are sub types of or from the same type as the provided
     * builder class.
     * 
     * @param builderClass The class of the builders you are searching for.
     * 
     * @see #getBuilderById(IBuilderKindId)
     * @see #getBuilderById(IBuilderKindId, Class)
     */
    public <T extends IIpsArtefactBuilder> List<T> getBuildersByClass(Class<T> builderClass);

    /**
     * Returns the builder specified by the builder kind id.
     * 
     * @see #getBuilderById(IBuilderKindId, Class)
     * 
     * @param kindId The kind id of the builder you want to have
     * 
     * @return The builder registered by the specified kindId.
     * @throws RuntimeException if there is no builder for the specified kind ID
     */
    public IIpsArtefactBuilder getBuilderById(IBuilderKindId kindId);

    /**
     * Returns the builder specified by the builder kind id. The builder must be of the specified
     * class. If it is not this method throws a runtime exception.
     * 
     * @see #getBuilderById(IBuilderKindId)
     * 
     * @param kindId The kind id of the builder you want to have
     * @param builderClass The class of the builder you need
     * 
     * @return The builder registered by the specified kindId.
     * @throws RuntimeException if there is no builder for the specified kind ID matching the
     *             specified class.
     */
    public <T extends IIpsArtefactBuilder> T getBuilderById(IBuilderKindId kindId, Class<T> builderClass);

    /**
     * Getting true if none mergeable resources should be marked as derived or not.
     * 
     * @return True to mark the files and folders as derived
     */
    public boolean isMarkNoneMergableResourcesAsDerived();

    /**
     * Called by the {@link IpsBuilder} when {@link IpsBuilder#clean(IProgressMonitor)} is called
     * giving the builder set the opportunity to do additional clearing.
     * 
     * @see IncrementalProjectBuilder#clean(IProgressMonitor)
     * 
     */
    @SuppressWarnings("javadoc")
    public void clean(IProgressMonitor monitor);

    /**
     * Returns the data type helper for the given data type. Returns {@code null} if there is no
     * helper for the given data type in this builder set.
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype);
}
