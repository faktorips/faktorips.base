package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.MutableClRuntimeRepositoryToc;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.runtime.TocEntry;
import org.faktorips.util.ArgumentCheck;

/**
 * A base class for the runtime repository toc file update builders of this plugin. It provides the
 * base functionality to add and remove entries of the toc file. Subclasses have to implement the
 * creteTocEntry method to provide an ips object specific entry.
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractTocFileUpdateBuilder implements IIpsArtefactBuilder {

    private IJavaPackageStructure packageStructure;
    protected String kind;
    
    /**
     * Subclasses need to provide the funtionality to return a TocEntry for the provided IpsObject.
     */
    protected abstract TocEntry createTocEntry(IIpsObject object) throws CoreException;

    /**
     * @param logger cannot be null
     * @param structure cannot be null
     */
    public AbstractTocFileUpdateBuilder(IJavaPackageStructure structure, String kind) {
        super();
        ArgumentCheck.notNull(structure, this);
        ArgumentCheck.notNull(kind, this);
        packageStructure = structure;
        this.kind = kind;
    }

    /**
     * Returns the IJavaPackageStructure that has been assigned to it upon creation.
     */
    protected final IJavaPackageStructure getPackageStructure() {
        return packageStructure;
    }

    public void beforeBuildProcess(int buildKind) throws CoreException {
    }

    public void afterBuildProcess(int buildKind) throws CoreException {
    }

    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
    }

    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
    }

    protected abstract void checkIfDependOnBuildersSet() throws IllegalStateException;

    /**
     * Adds or replaces a toc entry of the runtime repository toc file.
     *
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#build(org.faktorips.devtools.core.model.IIpsSrcFile)
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        checkIfDependOnBuildersSet();
        IIpsObject object = null;
        try {
            object = ipsSrcFile.getIpsObject();
            TocEntry entry = createTocEntry(object);
            if (entry == null) {
                return;
            }
            getMutableClRuntimeRepositoryToc(ipsSrcFile).addOrReplaceTocEntry(entry);
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(
                    "Unable to update the runtime repository toc file with the entry for: "
                            + object.getQualifiedName(), e));
        }
    }

    /**
     * Removes the according TocEntry from the runtime repository toc file.
     *
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#delete(org.faktorips.devtools.core.model.IIpsSrcFile)
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        // TODO kann zur Zeit nicht vernünftig umgesetzt werden. Man muss das
        // MutableClRuntimeRepository
        // neu schreiben. Grundsätzlich lohn es sich an dieser Stelle auch noch mal über
        // das "IpsObject as handle" Problem nachzudenken. (bei ProductCmpt und TableContent
        // könnten die String Referenz auf den ProductCmptType bzw. TableStructure auch noch
        // im handle gehalten werden.
    }

    protected MutableClRuntimeRepositoryToc getMutableClRuntimeRepositoryToc(IIpsSrcFile ipsSrcFile)
            throws CoreException {
        IpsPackageFragmentRoot packRoot = (IpsPackageFragmentRoot)ipsSrcFile.getIpsObject()
                .getIpsPackageFragment().getRoot();
        return packRoot.getRuntimeRepositoryToc();
    }
}
