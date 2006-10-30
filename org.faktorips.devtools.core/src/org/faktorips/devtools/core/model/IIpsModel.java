/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.util.message.MessageList;

/**
 * The IPS model is the top of the IpsElement hierarchy (like the Java model is the top of the Java
 * element hierarchy). One model instance exists per workspace. The model instance can be retrievedd
 * via the plugin's <code>getIpsModel()</code> method.
 */
public interface IIpsModel extends IIpsElement {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IPSMODEL-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exist two runtime ids which collide.
     */
    public final static String MSGCODE_RUNTIME_ID_COLLISION = MSGCODE_PREFIX + "RuntimeIdCollision"; //$NON-NLS-1$

    /**
     * Returns the workspace.
     */
    public IWorkspace getWorkspace();

    /**
     * Runs the given runnable/action as an atomic workspace operation like the <code>run</code>
     * method in IWorkspace. All ips source file change events are queued until the action is
     * finished and then broadcastet. If an ips source file is change more than one, only one change
     * event is sent.
     * 
     * @throws CoreException
     * 
     * @see IWorkspace#run(org.eclipse.core.resources.IWorkspaceRunnable,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void runAndQueueChangeEvents(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException;

    /**
     * Runs the given runnable/action as an atomic workspace operation like the <code>run</code>
     * method in IWorkspace. All ips source file change events are queued until the action is
     * finished and then broadcastet. If an ips source file is change more than one, only one change
     * event is sent.
     * 
     * @throws CoreException
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
     * Creates an IpsProject for the given Java project by adding the IPS nature and creating (an
     * empty) .ipsproject file.
     * 
     * @throws NullPointerException if javaProjecct is <code>null</code>.
     * @throws CoreException if an error occurs while creating the ips project.
     */
    public IIpsProject createIpsProject(IJavaProject javaProject) throws CoreException;

    /**
     * Returns all IPS projects opened in the workspace or an empty array if none.
     */
    public IIpsProject[] getIpsProjects() throws CoreException;

    /**
     * Returns the IpsProject with the indicated name.
     */
    public IIpsProject getIpsProject(String name);

    /**
     * Returns the IpsProject that belongs to the indicated platform project.
     * 
     * @throws NullPointerException if project is null.
     */
    public IIpsProject getIpsProject(IProject project);

    /**
     * Returns all <code>IProject</code>s in the workspace, that do not have an IpsProject
     * nature. Ignores closed projects.
     * 
     * @throws CoreException if an exception occurs while accessing project-natures.
     */
    public IResource[] getNonIpsProjects() throws CoreException;

    /**
     * Returns the IpsElement that corresponds to the indicated resource. The IpsElement may not be
     * valid or existing inside a IPS project's source folder.
     */
    public IIpsElement getIpsElement(IResource resource);

    /**
     * Returns the IpsElement that corresponds to the indicated resource, if such a valid element
     * exists for that resource. Returns <code>null</code> if no corresponding element is found or
     * the resource is not inside a valid context.
     */
    public IIpsElement findIpsElement(IResource resource);

    /**
     * Adds a listener that is notified when something in the model was changed. The notifications
     * are made in the ui-thread.
     * 
     * @throws IllegalArgumentException if listener is null.
     */
    public void addChangeListener(ContentsChangeListener listener);

    /**
     * Removes the change listener.
     * 
     * @throws IllegalArgumentException if listener is null.
     */
    public void removeChangeListener(ContentsChangeListener listener);

    /**
     * Returns all package fragment roots containing source files or an empty array if none is
     * found.
     * 
     * @throws CoreException
     */
    public IIpsPackageFragmentRoot[] getSourcePackageFragmentRoots() throws CoreException;

    /**
     * Returns the extension properties for the given type. Returns an empty array if no extension
     * property is defined.
     * 
     * @param type The published interface of the ips object or part e.g.
     *            <code>org.faktorips.plugin.model.pctype.IAttribute</code>
     * @param <code>true</code> if not only the extension properties defined for for the type
     *            itself should be returned, but also the ones registered for it's supertype(s) and
     *            it's interfaces.
     */
    public IExtensionPropertyDefinition[] getExtensionPropertyDefinitions(Class type,
            boolean includeSupertypesAndInterfaces);

    /**
     * Returns the extension property with the given id that belongs to the given type. Returns
     * <code>null</code> if no such extension property is defined.
     * 
     * @param type The published interface of the ips object or part e.g.
     *            <code>or.faktorips.plugin.model.pctype.Attribute</code>
     * @parma propertyId the extension property id
     * @param <code>true</code> if not only the extension properties defined for for the type
     *            itself should be returned, but also the ones registered for it's supertype(s) and
     *            it's interfaces.
     */
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(Class type,
            String propertyId,
            boolean includeSupertypesAndInterfaces);

    /**
     * Returns the predefines value datatypes like String, Integer etc.
     */
    public ValueDatatype[] getPredefinedValueDatatypes();

    /**
     * Returns <code>true</code> if the datatype is predefined (via the datatypeDefinition
     * extension point), otherwise <code>false</code>. Returns <code>false</code> if datatypeId
     * is <code>null</code>.
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
     * Searches all product components of all projects of this model for duplicate runtime ids.
     * 
     * @return A list of messages. For each combination of two product components with duplicate
     *         runtime id a new message is created. This message has two invalid object properties,
     *         each containing one of the two product components.
     * 
     * @throws CoreException if an error occurs during search.
     */
    public MessageList checkForDuplicateRuntimeIds() throws CoreException;

    /**
     * Checks all given product components against all product components of all projects of this
     * model for duplicate runtime ids.
     * 
     * @return A list of messages. For each combination of two product components with duplicate
     *         runtime id a new message is created. This message has only one invalid object
     *         property, containing the product component given to this method.
     * 
     * @throws CoreException if an error occurs during search.
     */
    public MessageList checkForDuplicateRuntimeIds(IProductCmpt[] cmptsToCheck) throws CoreException;

    /**
     * Clears the validation results for all elements in the workspace.
     */
    public void clearValidationCache();
}
