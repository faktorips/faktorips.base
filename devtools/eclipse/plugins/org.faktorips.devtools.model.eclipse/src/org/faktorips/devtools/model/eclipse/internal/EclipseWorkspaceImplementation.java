package org.faktorips.devtools.model.eclipse.internal;

import java.io.File;
import java.util.function.Consumer;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions.AWorkspaceAbstractionsImplementation;
import org.faktorips.devtools.model.eclipse.internal.ipsproject.jdtcontainer.IpsContainer4JdtClasspathContainer;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.util.IpsProjectUtil;
import org.osgi.service.component.annotations.Component;

@Component(service = AWorkspaceAbstractionsImplementation.class, property = "type=eclipse")
public class EclipseWorkspaceImplementation implements AWorkspaceAbstractionsImplementation {

    @Override
    public IpsModel createIpsModel() {
        return new EclipseIpsModel();
    }

    @Override
    public void addRequiredEntriesToIpsObjectPath(IpsObjectPath ipsObjectPath,
            Consumer<IIpsObjectPathEntry> entryAdder) {
        IpsContainer4JdtClasspathContainer.addRequiredEntriesToIpsObjectPath(ipsObjectPath, entryAdder);
    }

    @Override
    public IpsProject createIpsProject(IIpsModel ipsModel, String name) {
        return new IpsProject.EclipseIpsProject(ipsModel, name);
    }

    @Override
    public void initializeProject(IIpsProject ipsProject) {
        addIpsNature(ipsProject);
        AWorkspaceAbstractionsImplementation.super.initializeProject(ipsProject);
    }

    private void addIpsNature(IIpsProject ipsProject) {
        IProject eclipseProject = ipsProject.getProject().unwrap();
        try {
            IpsProjectUtil.addNature(eclipseProject, IIpsProject.NATURE_ID);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    @Override
    public void initializeProperties(IIpsProjectProperties props) {
        IIpsArtefactBuilderSetInfo[] infos = IIpsModel.get().getIpsArtefactBuilderSetInfos();
        if (infos.length > 0) {
            props.setBuilderSetId(infos[0].getBuilderSetId());
        }
    }

    @Override
    public File getFileFromArchivePath(IIpsArchive ipsArchive) {
        // accessing the file on local file system is tricky in eclipse. At least we have to
        // refresh
        AResource resource = ipsArchive.getCorrespondingResource();
        if (resource != null) {
            try {
                resource.refreshLocal(AResourceTreeTraversalDepth.RESOURCE_ONLY, null);
                // this part is copied from
                // org.eclipse.jdt.internal.core.util.Util.toLocalFile(URI,
                // IProgressMonitor)
                IFileStore fileStore = EFS.getStore(resource.getLocation().toUri());
                File localFile = fileStore.toLocalFile(EFS.NONE, null);
                if (localFile == null) {
                    // non local file system
                    localFile = fileStore.toLocalFile(EFS.CACHE, null);
                }
                return localFile;
            } catch (CoreException e) {
                IpsLog.log(e);
            }
        }
        return AWorkspaceAbstractionsImplementation.super.getFileFromArchivePath(ipsArchive);
    }

    @Override
    public IIpsModelExtensions getIpsModelExtensions() {
        return IpsModelExtensionsViaEclipsePlugins.get();
    }
}
