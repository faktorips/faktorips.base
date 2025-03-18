/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGenerationToTypeDelta;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainerToTypeDelta;

/**
 * Content provider for differences between the model and the matching {@link ProductCmpt product
 * component}.
 * <p>
 * The content consists of the product itself, all differences in its generations, and differences
 * in static attributes. If two or more generations have the same differences, they are not listed
 * individually in the tree, but are joined under a heading detailing which generations are
 * affected. Usually, generations have the same differences, so, most of the time, only one
 * generation heading will be provided.
 * <p>
 * The differences themselves are grouped by category: removed elements, elements not existing in
 * the model, and so on.
 */
public class DeltaContentProvider implements ITreeContentProvider {
    /**
     * Root note of the differences.
     */
    private IFixDifferencesComposite deltaComposite;

    /**
     * Maps the differences to their {@link IIpsElement}.
     */
    private Map<IIpsElement, IFixDifferencesComposite> objectToDifferencesMap;

    /**
     * Builds the {@link IIpsElement} to {@link IFixDifferencesComposite} map by recursively adding
     * all children of the root {@code IFixDifferencesComposite}.
     *
     * @param composites children of the parent {@code IFixDifferencesComposite}.
     */
    private void mapIIpsElementToDifferenceComposite(List<IFixDifferencesComposite> composites) {
        if (composites == null || composites.isEmpty()) {
            return;
        }
        for (IFixDifferencesComposite c : composites) {
            objectToDifferencesMap.put(c.getCorrespondingIpsElement(), c);
            mapIIpsElementToDifferenceComposite(c.getChildren());
        }
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        deltaComposite = (IFixDifferencesComposite)newInput;
        objectToDifferencesMap = new HashMap<>();
        if (deltaComposite != null) {
            objectToDifferencesMap.put(deltaComposite.getCorrespondingIpsElement(), deltaComposite);
            mapIIpsElementToDifferenceComposite(deltaComposite.getChildren());
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return new Object[] { deltaComposite.getCorrespondingIpsElement() };
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof IProductCmpt || element instanceof IPropertyValueContainerToTypeDelta
                || element instanceof DeltaTypeWrapper || element instanceof ProductCmptGenerationsDeltaViewItem;
    }

    /**
     * Returns the children of an {@link IPropertyValueContainerToTypeDelta}. These are all
     * {@link DeltaTypeWrapper DeltaTypeWrappers}, which consist of the {@link DeltaType} and its
     * parent. The parent (i.e. the provided {@code IPropertyValueContainerToTypeDelta} is necessary
     * to find the correct entries for a given {@code DeltaType}.
     *
     * @param delta parent node, containing deltas.
     * @return the children of the node.
     */
    private Object[] getPropertyChildren(IPropertyValueContainerToTypeDelta delta) {
        List<Object> elements = new ArrayList<>();
        for (DeltaType element : DeltaType.values()) {
            if (delta.getEntries(element).length > 0) {
                elements.add(new DeltaTypeWrapper(element, delta));
            }
        }
        return elements.toArray();
    }

    /**
     * Helper method to compare two {@link IFixDifferencesComposite IFixDifferencesComposites}. They
     * are considered equal (at least for the purpose in this tree), if they are both
     * {@link IPropertyValueContainerToTypeDelta IPropertyValueContainerToTypeDeltas} and the string
     * representation of all their entries are equal.
     *
     * @param a first object to test for equality.
     * @param b second object to test for equality.
     * @return true if, and only if, a and b are instances of
     *             {@code IPropertyValueContainerToTypeDelta} and they have the same entries
     *             (entries whose string representation is equal).
     */
    private boolean areDifferenceCompositeEntriesEqual(Object a, IFixDifferencesComposite b) {
        if (a instanceof ProductCmptGenerationsDeltaViewItem) {
            a = ((ProductCmptGenerationsDeltaViewItem)a).getDelta();
        }
        if (!(a instanceof IPropertyValueContainerToTypeDelta aDelta)
                || !(b instanceof IPropertyValueContainerToTypeDelta bDelta)) {
            return false;
        }
        IDeltaEntry[] aDeltaEntries = aDelta.getEntries();
        IDeltaEntry[] bDeltaEntries = bDelta.getEntries();
        if (aDeltaEntries == null) {
            return bDeltaEntries == null;
        } else if (bDeltaEntries == null) {
            return false;
        }
        if (aDeltaEntries.length != bDeltaEntries.length) {
            return false;
        }
        for (int i = 0, n = aDeltaEntries.length; i < n; i++) {
            if (!(aDeltaEntries[i].toString().equals(bDeltaEntries[i].toString()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns all children of an {@link IProductCmpt}. This method lists all differences and
     * combines them for several generations if they are equal. Most of the time, differences affect
     * all generations, hence only one generation difference is created. Furthermore, this method
     * also returns the differences on the product component itself (static attributes).
     *
     * @param parent {@code IProductCmpt} of which children are retrieved.
     * @return all children of the product component.
     */
    private Object[] getChildrenOfIProductCmpt(IProductCmpt parent) {
        List<Object> kids = new ArrayList<>();
        IFixDifferencesComposite fixDifferenceComposite = objectToDifferencesMap.get(parent);
        if (fixDifferenceComposite != null) {
            for (IFixDifferencesComposite composite : objectToDifferencesMap.get(parent).getChildren()) {
                if (!(composite instanceof ProductCmptGenerationToTypeDelta)) {
                    kids.add(composite);
                    continue;
                }
                if (((ProductCmptGenerationToTypeDelta)composite).getEntries().length == 0) {
                    continue;
                }
                boolean isContained = false;
                for (Object o : kids) {
                    if (areDifferenceCompositeEntriesEqual(o, composite)) {
                        isContained = true;
                        ((ProductCmptGenerationsDeltaViewItem)o).addDate((ProductCmptGenerationToTypeDelta)composite);
                        break;
                    }
                }
                if (!isContained) {
                    kids.add(new ProductCmptGenerationsDeltaViewItem((ProductCmptGenerationToTypeDelta)composite));
                }
            }
            if (fixDifferenceComposite instanceof IPropertyValueContainerToTypeDelta) {
                for (DeltaType element : DeltaType.values()) {
                    if (((IPropertyValueContainerToTypeDelta)fixDifferenceComposite).getEntries(element).length > 0) {
                        kids.add(new DeltaTypeWrapper(element,
                                (IPropertyValueContainerToTypeDelta)fixDifferenceComposite));
                    }
                }

            }
        }
        return kids.toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return switch (parentElement) {
            case IProductCmpt productCmpt -> getChildrenOfIProductCmpt(productCmpt);
            case DeltaTypeWrapper delta -> delta.getDelta().getEntries(delta.getDeltaType());
            case IPropertyValueContainerToTypeDelta delta -> getPropertyChildren(delta);
            case ProductCmptGenerationsDeltaViewItem viewItem -> getPropertyChildren(viewItem.getDelta());
            default -> new Object[0];
        };
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IDeltaEntry) {
            return ((IDeltaEntry)element).getDeltaType();
        }
        return null;
    }

    @Override
    public void dispose() {
        // Nothing to do.
    }
}
