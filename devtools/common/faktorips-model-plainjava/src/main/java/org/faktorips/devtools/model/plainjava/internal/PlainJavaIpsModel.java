package org.faktorips.devtools.model.plainjava.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.GenericValueDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaFile;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaImplementation;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaProject;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaResource;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaResourceChange;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaResourceChange.Type;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaResourceDelta;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.builder.EmptyBuilderSet;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plainjava.internal.fl.PlainJavaIdentifierResolver;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.fl.DefaultDatatypeHelperProvider;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

public class PlainJavaIpsModel extends IpsModel {

    private Consumer<PlainJavaResourceChange> resourceChangeListener;

    public PlainJavaIpsModel() {
        super();
        resourceChangeListener = this::resourceChanged;
        PlainJavaImplementation.getResourceChanges().addListener(resourceChangeListener);
        setFallbackBuilderSetProvider(PlainJavaBuilderSet::new);
    }

    @Override
    public void startListeningToResourceChanges() {
        PlainJavaImplementation.getResourceChanges().addListener(resourceChangeListener);
    }

    @Override
    public void stopListeningToResourceChanges() {
        PlainJavaImplementation.getResourceChanges().removeListener(resourceChangeListener);
    }

    private void resourceChanged(PlainJavaResourceChange change) {
        PlainJavaResource resource = change.getChangedResource();
        if (resource instanceof PlainJavaProject) {
            AProject project = (AProject)resource;
            projectChanged(project);
        } else if (resource instanceof PlainJavaFile) {
            AProject project = resource.getProject();
            if (project != null && project.isIpsProject()) {
                IIpsProject ipsProject = getIpsProject(project);
                if (IpsProject.PROPERTY_FILE_EXTENSION_INCL_DOT.equals(resource.getName())) {
                    cleanValidationCache(ipsProject);
                } else {
                    String path = PathUtil.toPortableString(resource.getProjectRelativePath());
                    if (QualifiedNameType.representsQualifiedNameType(path)) {
                        IIpsSrcFile ipsSrcFile = findIpsSrcFile(resource, ipsProject, path);
                        if (ipsSrcFile != null) {
                            ipsSrcFileChanged(ipsSrcFile, change);
                        }
                    }
                }
            }
        }
    }

    private void ipsSrcFileChanged(IIpsSrcFile ipsSrcFile, PlainJavaResourceChange change) {
        forEachIpsSrcFilesChangeListener(listener -> listener.ipsSrcFilesChanged(
                new IpsSrcFilesChangedEvent(Map.of(ipsSrcFile, new PlainJavaResourceDelta(change)))));
        if (Type.REMOVED == change.getType()) {
            removeIpsSrcFileContent(ipsSrcFile);
        } else {
            IpsSrcFileContent content = getIpsSrcFileContent(ipsSrcFile);
            boolean isInSync = isInSync(ipsSrcFile, content);
            if (!isInSync) {
                ipsSrcFileContentHasChanged(ContentChangeEvent.newWholeContentChangedEvent(ipsSrcFile));
            }
        }
    }

    private void projectChanged(AProject project) {
        if (project.isIpsProject()) {
            IIpsProject ipsProject = getIpsProject(project);
            forceReloadOfCachedIpsSrcFileContents(ipsProject);
        }
    }

    private IIpsSrcFile findIpsSrcFile(PlainJavaResource resource, IIpsProject ipsProject, String path) {
        IIpsSrcFile ipsSrcFile = ipsProject.findIpsSrcFile(QualifiedNameType.newQualifedNameType(path));
        if (ipsSrcFile == null) {
            ipsSrcFile = findIpsSrcFileInIpsModel(resource);
        }
        return ipsSrcFile;
    }

    private IIpsSrcFile findIpsSrcFileInIpsModel(PlainJavaResource resource) {
        return getIpsSrcFilesInternal().parallelStream().filter(i -> resource.equals(i.getCorrespondingResource()))
                .findFirst().orElse(null);
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

    /**
     * Forces to reload the the cached IPS source file contents of a single project or the whole
     * workspace. This is done by setting {@value #INVALID_MOD_STAMP} as modification stamp in each
     * content object.
     */
    private synchronized void forceReloadOfCachedIpsSrcFileContents(IIpsProject ipsProject) {
        HashSet<IIpsSrcFile> copyKeys = new HashSet<>(getIpsSrcFilesInternal());
        for (IIpsSrcFile srcFile : copyKeys) {
            if (!srcFile.isDirty() && srcFile.getIpsProject().equals(ipsProject)) {
                releaseInCache(srcFile);
                getValidationResultCache().removeStaleData(srcFile);
            }
        }
    }

    /**
     * Forces to reload the the cached IPS source file contents of a single project or the whole
     * workspace. This is done by setting {@value #INVALID_MOD_STAMP} as modification stamp in each
     * content object.
     */
    private synchronized void cleanValidationCache(IIpsProject ipsProject) {
        HashSet<IIpsSrcFile> copyKeys = new HashSet<>(getIpsSrcFilesInternal());
        for (IIpsSrcFile srcFile : copyKeys) {
            if (srcFile.getIpsProject().equals(ipsProject)) {
                getValidationResultCache().removeStaleData(srcFile);
            }
        }
    }

    /**
     * An {@link IIpsArtefactBuilderSet} implementation that is used in a {@link PlainJavaIpsModel}.
     * It extends the {@link EmptyBuilderSet} and overwrites the
     * {@link #getDatatypeHelper(Datatype)} method.
     */
    private static class PlainJavaBuilderSet extends EmptyBuilderSet {

        private Map<Datatype, DatatypeHelper> datatypeHelperRegistry;

        public PlainJavaBuilderSet(IIpsProject project) {
            initialize(project);
        }

        private void initialize(IIpsProject project) {
            datatypeHelperRegistry = PlainJavaIpsModelExtensions.get().getDatatypeHelperRegistry().get();

            List<Datatype> definedDatatypes = project.getProperties().getDefinedDatatypes();
            for (Datatype datatype : definedDatatypes) {
                if (datatype instanceof GenericValueDatatype valueDatatype) {
                    datatypeHelperRegistry.put(valueDatatype, new GenericValueDatatypeHelper(valueDatatype));
                }
            }
        }

        @Override
        public DatatypeHelper getDatatypeHelper(Datatype datatype) {
            DatatypeHelper datatypeHelper = datatypeHelperRegistry.get(datatype);
            return (datatypeHelper != null) ? datatypeHelper
                    : new DefaultDatatypeHelperProvider().getDatatypeHelper(datatype);
        }

        @Override
        public IdentifierResolver<JavaCodeFragment> createFlIdentifierResolver(IExpression formula,
                ExprCompiler<JavaCodeFragment> exprCompiler) {
            return new PlainJavaIdentifierResolver(formula, exprCompiler);
        }
    }
}