/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * ResourceDeltaVisitor to generate IPS model change events.
 */
class ResourceDeltaVisitor implements IResourceDeltaVisitor {

    private final IpsModel ipsModel;
    private Set<String> fileExtensionsOfInterest = new HashSet<>(20);

    public ResourceDeltaVisitor(IpsModel ipsModel) {
        this.ipsModel = ipsModel;
        IpsObjectType[] types = ipsModel.getIpsObjectTypes();
        for (IpsObjectType type : types) {
            getFileExtensionsOfInterest().add(type.getFileExtension());
        }
        getFileExtensionsOfInterest().add(IIpsProject.PROPERTY_FILE_EXTENSION);
    }

    @Override
    public boolean visit(final IResourceDelta delta) {
        IResource resource = delta.getResource();
        return visitInternal(delta, resource);
    }

    boolean visitInternal(final IResourceDelta delta, IResource resource) {
        if (resource == null || resource.getType() != IResource.FILE) {
            return true;
        }
        if (isRelatedFile(resource)) {
            if (ipsProjectPropertiesChanged(resource) || manifestChanged(resource)) {
                handleUpdateProjectSettings(resource);
            } else if (delta.getKind() == IResourceDelta.REMOVED) {
                handleRemoved(resource);
            } else {
                handleOtherResourceChange(resource);
            }
        }
        return false;
    }

    private void handleUpdateProjectSettings(IResource resource) {
        IIpsProject ipsProject = ipsModel.getIpsProject(wrap(resource.getProject()).as(AProject.class));
        ipsModel.clearProjectSpecificCaches(ipsProject);
        ipsModel.getValidationResultCache().clear();
    }

    private void handleRemoved(IResource resource) {
        IIpsElement ipsElement = ipsModel.getIpsElement(wrap(resource).as(AResource.class));
        if (ipsElement instanceof IIpsSrcFile) {
            ipsModel.removeIpsSrcFileContent((IIpsSrcFile)ipsElement);
        }
    }

    private boolean handleOtherResourceChange(IResource resource) {
        final IIpsElement element = ipsModel.findIpsElement(wrap(resource).as(AResource.class));
        if (element instanceof IIpsSrcFile && ((IIpsSrcFile)element).isContainedInIpsRoot()) {
            IIpsSrcFile srcFile = (IIpsSrcFile)element;
            IpsSrcFileContent content = ipsModel.getIpsSrcFileContent(srcFile);
            boolean isInSync = isInSync(srcFile, content);
            traceModelResourceVisited(resource, srcFile, isInSync);
            if (!isInSync) {
                handleNotSyncResource(srcFile);
            }
            return true;
        } else {
            return true;
        }
    }

    private boolean ipsProjectPropertiesChanged(IResource resource) {
        IIpsProject ipsProject = ipsModel.getIpsProject(wrap(resource).as(AResource.class).getProject());
        return resource.equals(ipsProject.getIpsProjectPropertiesFile().unwrap());
    }

    private boolean manifestChanged(IResource resource) {
        return resource.getProjectRelativePath().equals(new Path(IpsBundleManifest.MANIFEST_NAME));
    }

    private void handleNotSyncResource(IIpsSrcFile srcFile) {
        ipsModel.ipsSrcFileContentHasChanged(ContentChangeEvent.newWholeContentChangedEvent(srcFile));
    }

    private void traceModelResourceVisited(IResource resource, IIpsSrcFile srcFile, boolean isInSync) {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println(
                    "IpsModel.ResourceDeltaVisitor.visit(): Received notification of IpsSrcFile change/delete on disk with modStamp " //$NON-NLS-1$
                            + resource.getModificationStamp() + ", Sync status=" + isInSync + ", " //$NON-NLS-1$ //$NON-NLS-2$
                            + srcFile + " Thread: " + Thread.currentThread().getName()); //$NON-NLS-1$
        }
    }

    private boolean isRelatedFile(IResource resource) {
        IFile file = (IFile)resource;
        if (getFileExtensionsOfInterest().contains(file.getFileExtension())) {
            return true;
        }
        if (IpsBundleManifest.MANIFEST_NAME.equals(file.getProjectRelativePath().toString())) {
            return true;
        }
        return false;
    }

    /**
     * This method checks whether the content was saved by a Faktor-IPS save or by an event outside
     * of Faktor-IPS. If it was saved by us it is still in sync because we have other mechanism to
     * trigger change events. These change events will be more detailed (for example it gives the
     * information about a specific part that was changed). If the resource change event was not
     * triggered by our own save operation we need to assume that the whole content may have
     * changed.
     */
    private boolean isInSync(IIpsSrcFile srcFile, IpsSrcFileContent content) {
        return content == null
                || content.wasModStampCreatedBySave(srcFile.getEnclosingResource().getModificationStamp());
    }

    Set<String> getFileExtensionsOfInterest() {
        return fileExtensionsOfInterest;
    }
}