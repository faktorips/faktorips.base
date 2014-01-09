/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ExtensionFunctionResolversCache;
import org.faktorips.devtools.core.internal.model.SingleEventModification;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition2;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.valueset.IValueSet;

/**
 * The IPS model is the top of the IPS element hierarchy (like the Java model is the top of the Java
 * element hierarchy). One model instance exists per workspace. The model instance can be retrieved
 * via the plugin's <tt>getIpsModel()</tt> method.
 */
public interface IIpsModel extends IIpsElement {

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "IPSMODEL-"; //$NON-NLS-1$

    /** Returns the workspace. */
    public IWorkspace getWorkspace();

    /**
     * Returns the object that gives access to custom model extensions.
     */
    public ICustomModelExtensions getCustomModelExtensions();

    /**
     * Runs the given runnable/action as an atomic workspace operation like the <code>run</code>
     * method in IWorkspace. All IPS source file change events are queued until the action is
     * finished and then broadcasted. If an IPS source file is changed more than once, only one
     * change event is sent.
     * 
     * @see IWorkspace#run(org.eclipse.core.resources.IWorkspaceRunnable,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void runAndQueueChangeEvents(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException;

    /**
     * Runs the given runnable/action as an atomic workspace operation like the <code>run</code>
     * method in IWorkspace. All IPS source file change events are queued until the action is
     * finished and then broadcasted. If an IPS source file is change more than one, only one change
     * event is sent.
     * 
     * @see IWorkspace#run(org.eclipse.core.resources.IWorkspaceRunnable,
     *      org.eclipse.core.runtime.jobs.ISchedulingRule, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void runAndQueueChangeEvents(IWorkspaceRunnable action,
            ISchedulingRule rule,
            int flags,
            IProgressMonitor monitor) throws CoreException;

    /**
     * Creates an ID for a new {@link IIpsObjectPart} in an {@link IIpsObjectPartContainer}. The
     * given parentPart is the parent of the newly created part. For example, if you want to create
     * a {@link IValueSet} as part of a {@link IConfigElement} you call this method with the
     * {@link IConfigElement} as parameter to get the ID for the new {@link IValueSet}.
     * 
     * @param parentPart The parent part of the new part for which we need the ID
     * @return the new unique ID that can be used for a new part
     */
    public String getNextPartId(IIpsObjectPartContainer parentPart);

    /**
     * Creates an IpsProject for the given Java project by adding the IPS nature and creating (an
     * empty) .ipsproject file.
     * 
     * @throws NullPointerException if javaProjecct is <code>null</code>.
     * @throws CoreException if an error occurs while creating the IPS project.
     */
    public IIpsProject createIpsProject(IJavaProject javaProject) throws CoreException;

    /** Returns all IPS projects opened in the workspace or an empty array if none. */
    public IIpsProject[] getIpsProjects() throws CoreException;

    /**
     * Returns all IPS projects opened in the workspace that contain a model definition or an empty
     * array if none.
     */
    public IIpsProject[] getIpsModelProjects() throws CoreException;

    /**
     * Returns all IPS projects opened in the workspace that contain a product definition or an
     * empty array if none.
     */
    public IIpsProject[] getIpsProductDefinitionProjects() throws CoreException;

    /** Returns the IPS project with the indicated name. */
    public IIpsProject getIpsProject(String name);

    /**
     * Returns the IpsProject that belongs to the indicated platform project.
     * 
     * @throws NullPointerException if project is null.
     */
    public IIpsProject getIpsProject(IProject project);

    /**
     * Returns all <code>IProject</code>s in the workspace, that do not have an IpsProject nature.
     * Ignores closed projects.
     * 
     * @throws CoreException if an exception occurs while accessing project-natures.
     */
    public IResource[] getNonIpsProjects() throws CoreException;

    /**
     * Returns the IpsElement that corresponds to the indicated resource. The IpsElement may not be
     * valid or exist inside a IPS project's source folder / IpsPackageFragmentRoot.
     * <p>
     * If the resource is contained in a project that exists and is also an IpsProject, the method
     * returns IPS element only for those resources that are contained in a source folder
     * representing an IPS package fragment root.
     * <p>
     * Examples:
     * <p>
     * Given is the IpsProject "HomeInsurance" with the source folder "model"
     * (IpsPackageFragmentRoot).
     * <p>
     * Resource: HomeInsurance/model/pack1/Policy.ipspolicycmpttype<br>
     * => Returns an IIpsSrcFile handle. (Whether the underlying Resource exists or not).
     * <p>
     * Resource: HomeInsurance/model/pack1/readme.txt<br>
     * => Returns <code>null</code> as readme.txt is not a file containing an IpsObject.
     * <p>
     * Resource: HomeInsurance/root/pack1/Policy.ipspolicycmpttype<br>
     * => Returns <code>null</code> as "root" is not a source folder representing an
     * IpsPackageFragmentRoot in the project HomeInsurance!
     * <p>
     * Resource: HomeInsurance/root => Returns <code>null</code> as "root" is not a source folder
     * representing an IpsPackageFragmentRoot in the project HomeInsurance!
     */
    public IIpsElement getIpsElement(IResource resource);

    /**
     * Returns the IpsElement that corresponds to the indicated resource, if such a valid element
     * exists for that resource. Returns <code>null</code> if no corresponding element is found or
     * the resource is not inside a valid context.
     */
    public IIpsElement findIpsElement(IResource resource);

    /**
     * <p>
     * Adds a listener that is notified when something in the model was changed. The notifications
     * are made in the UI thread.
     * <p>
     * <strong>Important</strong>: Do not forget to <strong>remove the listener again</strong> if no
     * longer needed to prevent memory leaks.
     * 
     * @see #removeChangeListener(ContentsChangeListener)
     */
    public void addChangeListener(ContentsChangeListener listener);

    /**
     * Removes the change listener.
     */
    public void removeChangeListener(ContentsChangeListener listener);

    /**
     * <p>
     * Adds the modification status change listener.
     * <p>
     * <strong>Important</strong>: Do not forget to <strong>remove the listener again</strong> if no
     * longer needed to prevent memory leaks.
     * 
     * @see #removeModificationStatusChangeListener(IModificationStatusChangeListener)
     */
    public void addModifcationStatusChangeListener(IModificationStatusChangeListener listener);

    /**
     * Removes the modification status change listener.
     */
    public void removeModificationStatusChangeListener(IModificationStatusChangeListener listener);

    /**
     * Returns all package fragment roots containing source files or an empty array if none is
     * found.
     */
    public IIpsPackageFragmentRoot[] getSourcePackageFragmentRoots() throws CoreException;

    /**
     * Returns the extension properties for the given type. Returns an empty array if no extension
     * property is defined.
     * <p>
     * Note: This method simply returns every {@link IExtensionPropertyDefinition} that is
     * registered for the given type. It does not respect whether it is applicable for the given
     * {@link IIpsObjectPartContainer} by calling
     * {@link IExtensionPropertyDefinition2#isApplicableFor(IIpsObjectPartContainer)}. Better use
     * {@link #getExtensionPropertyDefinitions(IIpsObjectPartContainer)} instead.
     * 
     * 
     * @param type The published interface of the IPS object or part e.g.
     *            <code>org.faktorips.plugin.model.pctype.IAttribute</code>
     * @param includeSupertypesAndInterfaces <code>true</code> if not only the extension properties
     *            defined for for the type itself should be returned, but also the ones registered
     *            for it's super type(s) and it's interfaces.
     * @see #getExtensionPropertyDefinitions(IIpsObjectPartContainer)
     * @deprecated Since 3.10 the scope of an extension property could be limited to an instance of
     *             {@link IIpsObjectPartContainer}. Hence we need the instance to decide whether a
     *             extension property is applicable or not. Use
     *             {@link #getExtensionPropertyDefinitions(IIpsObjectPartContainer)} instead.
     */
    @Deprecated
    public IExtensionPropertyDefinition[] getExtensionPropertyDefinitions(Class<?> type,
            boolean includeSupertypesAndInterfaces);

    /**
     * Returns the extension property with the given id that belongs to the given type. Returns
     * <code>null</code> if no such extension property is defined.
     * <p>
     * Note: This method simply returns every {@link IExtensionPropertyDefinition} that is
     * registered for the given type. It does not respect whether it is applicable for the given
     * {@link IIpsObjectPartContainer} by calling
     * {@link IExtensionPropertyDefinition2#isApplicableFor(IIpsObjectPartContainer)}. Better use
     * {@link #getExtensionPropertyDefinitions(IIpsObjectPartContainer)} instead.
     * 
     * @param type The published interface of the IPS object or part e.g.
     *            <code>or.faktorips.plugin.model.pctype.Attribute</code>
     * @param propertyId the extension property id
     * @param includeSupertypesAndInterfaces <code>true</code> if not only the extension properties
     *            defined for for the type itself should be returned, but also the ones registered
     *            for it's super type(s) and it's interfaces.
     * @see #getExtensionPropertyDefinitions(IIpsObjectPartContainer)
     * 
     * @deprecated Since 3.10 the scope of an extension property could be limited to an instance of
     *             {@link IIpsObjectPartContainer}. Hence we need the instance to decide whether a
     *             extension property is applicable or not. Use
     *             {@link #getExtensionPropertyDefinitions(IIpsObjectPartContainer)} instead.
     */
    @Deprecated
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(Class<?> type,
            String propertyId,
            boolean includeSupertypesAndInterfaces);

    /**
     * Returns a map {@link IExtensionPropertyDefinition} identified by their IDs, that are defined
     * and activated for the given {@link IIpsObjectPartContainer}.
     * 
     * @param object The {@link IIpsObjectPartContainer} for which you want to get the
     *            {@link IExtensionPropertyDefinition}
     * @return A map of {@link IExtensionPropertyDefinition} that are identified by their IDs
     * @since 3.10
     */
    public Map<String, IExtensionPropertyDefinition> getExtensionPropertyDefinitions(IIpsObjectPartContainer object);

    /**
     * Returns the predefines value data types like String, Integer etc.
     */
    public ValueDatatype[] getPredefinedValueDatatypes();

    /**
     * Returns <code>true</code> if the data type is predefined (via the datatypeDefinition
     * extension point), otherwise <code>false</code>. Returns <code>false</code> if datatypeId is
     * <code>null</code>.
     */
    public boolean isPredefinedValueDatatype(String datatypeId);

    /**
     * Returns the available changes over time naming conventions.
     */
    public IChangesOverTimeNamingConvention[] getChangesOverTimeNamingConvention();

    /**
     * Returns the changes in time naming convention identified by the given id. If the id does not
     * identify a naming convention, the VAA naming convention is returned per default.
     */
    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention(String id);

    /**
     * Removes the given object from the model.
     */
    public void delete(IIpsElement toDelete);

    /**
     * Clears the validation results for all elements in the workspace.
     */
    public void clearValidationCache();

    /**
     * Returns the type of ips objects supported.
     */
    public IpsObjectType[] getIpsObjectTypes();

    /**
     * Returns the type identified by the name or <code>null</code> if no such type exists.
     */
    public IpsObjectType getIpsObjectType(String name);

    /**
     * Returns the type identified by the given file extension or <code>null</code> if no such type
     * exists.
     */
    public IpsObjectType getIpsObjectTypeByFileExtension(String fileExtension);

    /**
     * Returns all registered IpsArtefactBuilderSetInfo objects.
     */
    public IIpsArtefactBuilderSetInfo[] getIpsArtefactBuilderSetInfos();

    /**
     * Returns the IpsArtefactBuilderSetInfo object for the specified id. If none is found
     * <code>null</code> will be returned.
     */
    public IIpsArtefactBuilderSetInfo getIpsArtefactBuilderSetInfo(String id);

    /**
     * Searches for all test cases referring to the given product component. The project of the
     * given product component and all projects referring to this one are searched.
     * 
     * @param cmpt The product component test cases have to refer to.
     * @return All test cases referring the product component with the given name or an empty list,
     *         of none is found.
     * @throws CoreException if any exceptions occurs during search.
     */
    public List<ITestCase> searchReferencingTestCases(IProductCmpt cmpt) throws CoreException;

    /**
     * Returns the container identified by the given type ID and optional path for the given IPS
     * project. Returns <code>null</code> if no such container exists.
     * 
     * @param ipsProject The IPS project
     * @param containerTypeId The unique ID of the container type.
     * @param optionalPath The optional path info.
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public IIpsObjectPathContainer getIpsObjectPathContainer(IIpsProject ipsProject,
            String containerTypeId,
            String optionalPath);

    /**
     * Adding a {@link IIpsSrcFilesChangeListener} to the list of listeners. If the listener already
     * exists this method does nothing.
     * 
     * 
     * @param listener the new listener
     * @return true if this set did not already contain the specified element
     */
    boolean addIpsSrcFilesChangedListener(IIpsSrcFilesChangeListener listener);

    /**
     * Removes the {@link IIpsSrcFilesChangeListener} from the list of listeners.
     * 
     * @param listener the listener to remove
     * @return true if the listener was removed
     */
    boolean removeIpsSrcFilesChangedListener(IIpsSrcFilesChangeListener listener);

    /**
     * This method executes the logic that is implemented in the provided
     * {@link SingleEventModification} and makes sure that only the {@link ContentChangeEvent} that
     * is provided by the {@link SingleEventModification} is fired. No events are fired during the
     * method execution.
     * 
     * @throws CoreException delegates the exceptions from the execute() method of the
     *             {@link SingleEventModification}
     */
    <T> T executeModificationsWithSingleEvent(SingleEventModification<T> modifications) throws CoreException;

    /**
     * Returns the function resolver cache for the given IPS project.
     * 
     * @see ExtensionFunctionResolversCache
     */
    public ExtensionFunctionResolversCache getExtensionFunctionResolverCache(IIpsProject ipsProject);

}
