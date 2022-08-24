/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.eclipse.internal;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.AResourceDelta.AResourceDeltaKind;
import org.faktorips.devtools.abstraction.AResourceDeltaVisitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.eclipse.util.IpsProjectUtil;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ResourceDeltaVisitor;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class EclipseIpsModel extends IpsModel implements IResourceChangeListener {

    /**
     * Resource delta visitor used to generate IPS source file contents changed events and trigger a
     * build after changes to the IPS project properties file.
     */
    private ResourceDeltaVisitor resourceDeltaVisitor;

    public EclipseIpsModel() {
        super();
        // has to be done after the IPS object types are initialized!
        resourceDeltaVisitor = new ResourceDeltaVisitor(this);
    }

    @Override
    public void startListeningToResourceChanges() {
        ((IWorkspace)getWorkspace().unwrap()).addResourceChangeListener(this,
                IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE
                        | IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_REFRESH);
    }

    @Override
    public void stopListeningToResourceChanges() {
        ((IWorkspace)getWorkspace().unwrap()).removeResourceChangeListener(this);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_REFRESH) {
            if (event.getResource() == null || event.getResource() instanceof IProject) {
                forceReloadOfCachedIpsSrcFileContents((IProject)event.getResource());
            }
        } else {
            AResourceDelta delta = wrap(event.getDelta()).as(AResourceDelta.class);
            if (delta != null) {
                try {
                    delta.accept(resourceDeltaVisitor);
                    IpsSrcFileChangeVisitor visitor = new IpsSrcFileChangeVisitor();
                    delta.accept(visitor);
                    if (!visitor.changedIpsSrcFiles.isEmpty()) {
                        notifyIpsSrcFileChangedListeners(visitor.changedIpsSrcFiles);
                    }
                    // CSOFF: IllegalCatch
                } catch (Exception e) {
                    IpsLog.log(new IpsStatus("Error updating model objects in resurce changed event.", //$NON-NLS-1$
                            e));
                }
                // CSON: IllegalCatch
            }
        }
    }

    /**
     * Forces to reload the the cached IPS source file contents of a single project or the whole
     * workspace. This is done by setting {@value #INVALID_MOD_STAMP} as modification stamp in each
     * content object.
     * 
     * @param project The project that should considered or <code>null</code> if the whole workspace
     *            should be considered.
     */
    private synchronized void forceReloadOfCachedIpsSrcFileContents(IProject project) {
        HashSet<IIpsSrcFile> copyKeys = new HashSet<>(getIpsSrcFilesInternal());
        for (IIpsSrcFile srcFile : copyKeys) {
            if (!srcFile.isDirty()
                    && (project == null || srcFile.getIpsProject().getProject().unwrap().equals(project))) {
                releaseInCache(srcFile);
            }
        }
    }

    private void notifyIpsSrcFileChangedListeners(final Map<IIpsSrcFile, AResourceDelta> changedIpsSrcFiles) {
        forEachIpsSrcFilesChangeListener(
                listener -> listener.ipsSrcFilesChanged(new IpsSrcFilesChangedEvent(changedIpsSrcFiles)));
    }

    @Override
    protected void runSafe(ICoreRunnable action, IProgressMonitor monitor, Set<IIpsSrcFile> modifiedSrcFiles) {
        super.runSafe(action, monitor, modifiedSrcFiles);
    }

    @Override
    public IIpsProject createIpsProject(AProject project) {
        try {
            IProject eclipseProject = project.unwrap();
            if (eclipseProject.getNature(IIpsProject.NATURE_ID) != null) {
                return getIpsProject(project);
            }
            IIpsProject ipsProject = getIpsProject(project);
            IpsProjectUtil.addNature(eclipseProject, IIpsProject.NATURE_ID);

            IIpsArtefactBuilderSetInfo[] infos = getIpsArtefactBuilderSetInfos();
            if (infos.length > 0) {
                IIpsProjectProperties props = ipsProject.getProperties();
                props.setBuilderSetId(infos[0].getBuilderSetId());
                ipsProject.setProperties(props);
            }

            return ipsProject;
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    private class IpsSrcFileChangeVisitor implements AResourceDeltaVisitor {

        private Map<IIpsSrcFile, AResourceDelta> changedIpsSrcFiles = new HashMap<>(5);
        private Set<String> fileExtensionsOfInterest;

        public IpsSrcFileChangeVisitor() {
            fileExtensionsOfInterest = resourceDeltaVisitor.getFileExtensionsOfInterest();
        }

        @Override
        public boolean visit(final AResourceDelta delta) {
            AResource resource = delta.getResource();
            if (resource == null || resource.getType() != AResourceType.FILE) {
                return true;
            }
            if (fileExtensionsOfInterest.contains(((AFile)resource).getExtension())) {
                if (delta.getKind() == AResourceDeltaKind.REMOVED) {
                    IIpsElement ipsElement = getIpsElement(resource);
                    if (ipsElement instanceof IIpsSrcFile && ((IIpsSrcFile)ipsElement).isContainedInIpsRoot()) {
                        changedIpsSrcFiles.put((IIpsSrcFile)ipsElement, delta);
                    }
                } else {
                    final IIpsElement ipsElement = findIpsElement(resource);
                    if (ipsElement instanceof IIpsSrcFile && ((IIpsSrcFile)ipsElement).isContainedInIpsRoot()) {
                        IpsSrcFile srcFile = (IpsSrcFile)ipsElement;
                        changedIpsSrcFiles.put(srcFile, delta);
                    }
                }
            }
            return false;
        }
    }

}