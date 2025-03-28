/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import java.io.InputStream;

import org.eclipse.compare.HistoryItem;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileImmutable;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Creates a structure/tree of AbstractCompareItems that is used for comparing IpsObjects.
 * <p>
 * For each product component (local, remote and a common ancestor (also remote)) a structure is
 * created based on their contents. The <code>StrucureMergeViewer</code> (by default
 * <code>StructureDiffViewer</code>) calls the <code>Differencer</code>, which compares the created
 * structures. As a result a tree of <code>DiffNode</code>s is created and displayed in the
 * <code>StructureDiffViewer</code> (topviewer in the compare window). Each <code>DiffNode</code> in
 * the result structure represents a difference/change between local and remote ipsobject. By
 * doubleclicking such a node, a text representation of the ipsobject is displayed in the content
 * mergeviewer (parallel scrollable textviewers at the bottom of the compare window).
 *
 * @author Stefan Widmaier
 */
public abstract class AbstractCompareItemCreator implements IStructureCreator {

    /**
     * Returns a tree of <code>AbstractCompareItem</code>s for the given input. This tree is created
     * on the basis of an <code>IIpsSrcFile</code> that might point to a local file or a remote file
     * (<code>IpsSrcFileImmutable</code>).
     * <p>
     * If the given input is a <code>ResourceNode</code>, an <code>IIpsSrcFile</code> is created on
     * the contained resource (local file). If the given input is a
     * <code>ISynchronizeModelElement</code>, an <code>IIpsSrcFile</code> is created on the
     * contained resource (local file). If the given Input is a <code>BufferedContent</code>,
     * <code>IEncodedStreamContentAccessor</code> and <code>ITypedElement</code>, an
     * <code>IpsSrcFileImmutable</code> (<code>FilteredBufferedResourceNode</code>) is created
     * reading remote contents via an input stream. {@inheritDoc}
     */
    // CSOFF: CyclomaticComplexity
    @Override
    public IStructureComparator getStructure(Object input) {
        if (input instanceof IAdaptable adaptableInput) {
            IResource resource = adaptableInput.getAdapter(IResource.class);
            if (resource != null) {
                IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(resource).as(AResource.class));
                if (element instanceof IIpsSrcFile) {
                    return getStructureForIpsSrcFile((IIpsSrcFile)element);
                }
            }
        }
        if (input instanceof ResourceNode resourceNode) {
            IResource file = resourceNode.getResource();
            IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(file).as(AResource.class));
            if (element instanceof IIpsSrcFile) {
                return getStructureForIpsSrcFile((IIpsSrcFile)element);
            }
        } else if (input instanceof IEncodedStreamContentAccessor remoteContent
                && input instanceof ITypedElement typedElement) {
            try {
                InputStream is = remoteContent.getContents();
                String name = typedElement.getName();
                IpsSrcFileImmutable srcFile = new IpsSrcFileImmutable(name, is);
                return getStructureForIpsSrcFile(srcFile);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        } else if (input instanceof ISynchronizeModelElement modelElement) {
            IResource res = modelElement.getResource();
            if (res instanceof IFile) {
                IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(res).as(AResource.class));
                if (element instanceof IIpsSrcFile) {
                    return getStructureForIpsSrcFile((IIpsSrcFile)element);
                }
            }
        } else if (input instanceof HistoryItem historyItem) {
            IResource res = historyItem.getResource();
            IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(res).as(AResource.class));
            if (element instanceof IIpsSrcFile ipsSrcFile) {
                return getStructureForIpsSrcFile(ipsSrcFile);
            }
        }
        return null;
    }

    // CSON: CyclomaticComplexity

    /**
     * Returns a tree of <code>AbstractCompareItem</code>s. Each <code>AbstractCompareItem</code>
     * represents a structural entity: the IpsSrcFile, the contained IpsObject, and its
     * IpsObjectParts.
     */
    protected abstract IStructureComparator getStructureForIpsSrcFile(IIpsSrcFile file);

    /**
     * No implementation needed. Returns null. {@inheritDoc}
     */
    @Override
    public IStructureComparator locate(Object path, Object input) {
        return null;
    }

    /**
     * Returns null if node is not an <code>AbstractCompareItem</code>. Otherwise a
     * string-representation of the given <code>AbstractCompareItem</code> is returned.
     * <p>
     * This method is called by the compareframework for byte-wise comparison of CompareItems (@see
     * org.eclipse.compare.structuremergeviewer.Differencer). It ist NOT used for text comparision
     * (RangeDifferencing) in <code>TextMergeViewer</code>, where the document contained in
     * compareItems is used.
     *
     * @see AbstractCompareItem#getContentString()
     * @see AbstractCompareItem#getContentStringWithoutWhiteSpace()
     *
     */
    @Override
    public String getContents(Object node, boolean ignoreWhitespace) {
        if (node instanceof AbstractCompareItem) {
            if (ignoreWhitespace) {
                return ((AbstractCompareItem)node).getContentStringWithoutWhiteSpace();
            }
            return ((AbstractCompareItem)node).getContentString();
        }
        return null;
    }

    /**
     * Empty implementation. Nothing to be saved. {@inheritDoc}
     */
    @Override
    public void save(IStructureComparator node, Object input) {
        // Empty implementation. Nothing to be saved.
    }

}
