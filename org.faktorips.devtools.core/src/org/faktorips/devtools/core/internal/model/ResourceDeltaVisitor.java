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

package org.faktorips.devtools.core.internal.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * ResourceDeltaVisitor to generate IPS model change events.
 */
class ResourceDeltaVisitor implements IResourceDeltaVisitor {

    private final IpsModel ipsModel;
    private Set<String> fileExtensionsOfInterest = new HashSet<String>(20);

    public ResourceDeltaVisitor(IpsModel ipsModel) {
        this.ipsModel = ipsModel;
        IpsObjectType[] types = ipsModel.getIpsObjectTypes();
        for (IpsObjectType type : types) {
            getFileExtensionsOfInterest().add(type.getFileExtension());
        }
        getFileExtensionsOfInterest().add(IpsProject.PROPERTY_FILE_EXTENSION);
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
        if (isNotRelatedFile(resource)) {
            return false;
        }
        if (ipsProjectPropertiesChanged(resource) || manifestChanged(resource)) {
            IIpsProject ipsProject = ipsModel.getIpsProject(resource.getProject());
            ipsModel.clearProjectSpecificCaches(ipsProject);
            ipsModel.getValidationResultCache().clear();
            return false;
        }

        if (delta.getKind() == IResourceDelta.REMOVED) {
            IIpsElement ipsElement = ipsModel.getIpsElement(resource);
            if (ipsElement instanceof IIpsSrcFile) {
                ipsModel.removeIpsSrcFileContent((IIpsSrcFile)ipsElement);
                return false;
            }
        }

        final IIpsElement element = ipsModel.findIpsElement(resource);
        if (!(element instanceof IIpsSrcFile)) {
            return true;
        }
        IpsSrcFile srcFile = (IpsSrcFile)element;
        IpsSrcFileContent content = ipsModel.getIpsSrcFileContent(srcFile);
        boolean isInSync = isInSync(srcFile, content);
        traceModelResourceVisited(resource, srcFile, isInSync);
        handleNotSyncResource(srcFile, isInSync);
        return true;
    }

    private boolean ipsProjectPropertiesChanged(IResource resource) {
        IIpsProject ipsProject = ipsModel.getIpsProject(resource.getProject());
        return resource.equals(ipsProject.getIpsProjectPropertiesFile());
    }

    private boolean manifestChanged(IResource resource) {
        return resource.getProjectRelativePath().equals(new Path(IpsBundleManifest.MANIFEST_NAME));
    }

    private void handleNotSyncResource(IpsSrcFile srcFile, boolean isInSync) {
        if (!isInSync) {
            ipsModel.ipsSrcFileContentHasChanged(ContentChangeEvent.newWholeContentChangedEvent(srcFile));
        }
    }

    private void traceModelResourceVisited(IResource resource, IpsSrcFile srcFile, boolean isInSync) {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out
                    .println("IpsModel.ResourceDeltaVisitor.visit(): Received notification of IpsSrcFile change/delete on disk with modStamp " //$NON-NLS-1$
                            + resource.getModificationStamp() + ", Sync status=" + isInSync + ", " //$NON-NLS-1$ //$NON-NLS-2$
                            + srcFile + " Thread: " + Thread.currentThread().getName()); //$NON-NLS-1$
        }
    }

    private boolean isNotRelatedFile(IResource resource) {
        IFile file = (IFile)resource;
        if (getFileExtensionsOfInterest().contains(file.getFileExtension())) {
            return false;
        }
        if (IpsBundleManifest.MANIFEST_NAME.equals(file.getProjectRelativePath().toString())) {
            return false;
        }
        return true;
    }

    private boolean isInSync(IpsSrcFile srcFile, IpsSrcFileContent content) {
        return content == null
                || content.wasModStampCreatedBySave(srcFile.getEnclosingResource().getModificationStamp());
    }

    Set<String> getFileExtensionsOfInterest() {
        return fileExtensionsOfInterest;
    }
}