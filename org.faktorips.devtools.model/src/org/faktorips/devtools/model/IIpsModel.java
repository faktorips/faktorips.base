/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * The IPS model is the top of the IPS element hierarchy (like the Java model is the top of the Java
 * element hierarchy). One model instance exists per workspace. The model instance can be retrieved
 * via the plugin's <code>getIpsModel()</code> method.
 */
public interface IIpsModel extends IIpsElement {

    /** Prefix for all message codes of this class. */
    String MSGCODE_PREFIX = "IPSMODEL-"; //$NON-NLS-1$

    /**
     * Returns the {@link IIpsModel} singleton instance.
     */
    static IIpsModel get() {
        return IpsModel.get();
    }

    /** Returns the workspace. */
    AWorkspace getWorkspace();

    /**
     * Returns the object that gives access to custom model extensions.
     */
    ICustomModelExtensions getCustomModelExtensions();

    /**
     * Runs the given runnable/action as an atomic workspace operation like the <code>run</code>
     * method in IWorkspace. All IPS source file change events are queued until the action is
     * finished and then broadcasted. If an IPS source file is changed more than once, only one
     * change event is sent.
     * 
     * Note: To get a busy indicator and progress dialog better call
     * IpsUIPlugin#runWorkspaceModification
     * 
     * @see IWorkspace#run(org.eclipse.core.runtime.ICoreRunnable,
     *          org.eclipse.core.runtime.IProgressMonitor)
     */
    void runAndQueueChangeEvents(ICoreRunnable action, IProgressMonitor monitor);

    /**
     * Creates an ID for a new {@link IIpsObjectPart} in an {@link IIpsObjectPartContainer}. The
     * given parentPart is the parent of the newly created part. For example, if you want to create
     * a {@link IValueSet} as part of a {@link IConfiguredDefault} you call this method with the
     * {@link IConfiguredDefault} as parameter to get the ID for the new {@link IValueSet}.
     * 
     * @param parentPart The parent part of the new part for which we need the ID
     * @return the new unique ID that can be used for a new part
     */
    String getNextPartId(IIpsObjectPartContainer parentPart);

    /**
     * Creates an IpsProject for the given project by adding the IPS nature and creating (an empty)
     * .ipsproject file.
     * 
     * @throws NullPointerException if project is <code>null</code>.
     * @throws IpsException if an error occurs while creating the IPS project.
     */
    IIpsProject createIpsProject(AProject project) throws IpsException;

    /** Returns all IPS projects opened in the workspace or an empty array if none. */
    IIpsProject[] getIpsProjects();

    /**
     * Returns all IPS projects opened in the workspace that contain a model definition or an empty
     * array if none.
     */
    IIpsProject[] getIpsModelProjects() throws IpsException;

    /**
     * Returns all IPS projects opened in the workspace that contain a product definition or an
     * empty array if none.
     */
    IIpsProject[] getIpsProductDefinitionProjects() throws IpsException;

    /** Returns the IPS project with the indicated name. */
    IIpsProject getIpsProject(String name);

    /**
     * Returns the IpsProject that belongs to the indicated platform project.
     * 
     * @throws NullPointerException if project is null.
     */
    IIpsProject getIpsProject(AProject project);

    /**
     * Returns all {@link AProject projects} in the workspace, that do not have an IpsProject
     * nature. Ignores closed projects.
     * 
     * @throws IpsException if an exception occurs while accessing project-natures.
     */
    Set<AProject> getNonIpsProjects() throws IpsException;

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
     * &rarr; Returns an IIpsSrcFile handle. (Whether the underlying Resource exists or not).
     * <p>
     * Resource: HomeInsurance/model/pack1/readme.txt<br>
     * &rarr; Returns <code>null</code> as readme.txt is not a file containing an IpsObject.
     * <p>
     * Resource: HomeInsurance/root/pack1/Policy.ipspolicycmpttype<br>
     * &rarr; Returns <code>null</code> as "root" is not a source folder representing an
     * IpsPackageFragmentRoot in the project HomeInsurance!
     * <p>
     * Resource: HomeInsurance/root &rarr; Returns <code>null</code> as "root" is not a source
     * folder representing an IpsPackageFragmentRoot in the project HomeInsurance!
     */
    IIpsElement getIpsElement(AResource resource);

    /**
     * Returns the IpsElement that corresponds to the indicated resource, if such a valid element
     * exists for that resource. Returns <code>null</code> if no corresponding element is found or
     * the resource is not inside a valid context.
     */
    IIpsElement findIpsElement(AResource resource);

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
    void addChangeListener(ContentsChangeListener listener);

    /**
     * Removes the change listener.
     */
    void removeChangeListener(ContentsChangeListener listener);

    /**
     * <p>
     * Adds the modification status change listener.
     * <p>
     * <strong>Important</strong>: Do not forget to <strong>remove the listener again</strong> if no
     * longer needed to prevent memory leaks.
     * 
     * @see #removeModificationStatusChangeListener(IModificationStatusChangeListener)
     */
    void addModifcationStatusChangeListener(IModificationStatusChangeListener listener);

    /**
     * Removes the modification status change listener.
     */
    void removeModificationStatusChangeListener(IModificationStatusChangeListener listener);

    /**
     * Returns all package fragment roots containing source files or an empty array if none is
     * found.
     */
    IIpsPackageFragmentRoot[] getSourcePackageFragmentRoots() throws IpsException;

    /**
     * Returns a view on the extension properties for the given class.
     * <p>
     * <em><strong>Important: This operation must only be used if it is of no relevance whether the
     * retrieved extension properties are applicable to a given
     * {@link IIpsObjectPartContainer}.</strong></em>
     * <p>
     * This operation simply returns every {@link IExtensionPropertyDefinition} that is registered
     * for the given class. It does not respect whether these definitions are applicable for a given
     * {@link IIpsObjectPartContainer} by calling
     * {@link IExtensionPropertyDefinition#isApplicableFor(IIpsObjectPartContainer)}.
     * <p>
     * Clients are advised to use {@link #getExtensionPropertyDefinitions(IIpsObjectPartContainer)}
     * instead, except if they explicitly want the behavior provided by this operation.
     * 
     * 
     * @param type The published interface of the extended {@link IIpsObjectPartContainer}, e.g.
     *            {@link IAttribute}
     * @param includeSupertypesAndInterfaces <code>true</code> if not only the extension properties
     *            defined for for the type itself should be returned, but also the ones registered
     *            for it's super type(s) and interface(s).
     * 
     * @see #getExtensionPropertyDefinitions(IIpsObjectPartContainer)
     */
    Set<IExtensionPropertyDefinition> getExtensionPropertyDefinitionsForClass(Class<?> type,
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
    Map<String, IExtensionPropertyDefinition> getExtensionPropertyDefinitions(IIpsObjectPartContainer object);

    /**
     * Returns the predefines value data types like String, Integer etc.
     */
    ValueDatatype[] getPredefinedValueDatatypes();

    /**
     * Returns <code>true</code> if the data type is predefined (via the datatypeDefinition
     * extension point), otherwise <code>false</code>. Returns <code>false</code> if datatypeId is
     * <code>null</code>.
     */
    boolean isPredefinedValueDatatype(String datatypeId);

    /**
     * Returns the available changes over time naming conventions.
     */
    IChangesOverTimeNamingConvention[] getChangesOverTimeNamingConvention();

    /**
     * Returns the changes in time naming convention identified by the given id. If the id does not
     * identify a naming convention, the VAA naming convention is returned per default.
     */
    IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention(String id);

    /**
     * Removes the given object from the model.
     */
    void delete(IIpsElement toDelete);

    /**
     * Clears the validation results for all elements in the workspace.
     */
    void clearValidationCache();

    /**
     * Returns the type of IPS objects supported.
     */
    IpsObjectType[] getIpsObjectTypes();

    /**
     * Returns the type identified by the name or <code>null</code> if no such type exists.
     */
    IpsObjectType getIpsObjectType(String name);

    /**
     * Returns the type identified by the given file extension or <code>null</code> if no such type
     * exists.
     */
    IpsObjectType getIpsObjectTypeByFileExtension(String fileExtension);

    /**
     * Returns all registered IpsArtefactBuilderSetInfo objects.
     */
    IIpsArtefactBuilderSetInfo[] getIpsArtefactBuilderSetInfos();

    /**
     * Returns the IpsArtefactBuilderSetInfo object for the specified id. If none is found
     * <code>null</code> will be returned.
     */
    IIpsArtefactBuilderSetInfo getIpsArtefactBuilderSetInfo(String id);

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
    IIpsObjectPathContainer getIpsObjectPathContainer(IIpsProject ipsProject,
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
     * Returns the version provider according to {@link IIpsProject#getVersionProvider()}
     * 
     * @param ipsProject The {@link IIpsProject} for which you want to get the version provider.
     * @return The version provider that is configured for this {@link IIpsProject}.
     */
    IVersionProvider<?> getVersionProvider(IIpsProject ipsProject);

    IpsSrcFileContent getIpsSrcFileContent(IIpsSrcFile file);

    /**
     * Provides access to operations related to multi-language support.
     */
    IMultiLanguageSupport getMultiLanguageSupport();

}
