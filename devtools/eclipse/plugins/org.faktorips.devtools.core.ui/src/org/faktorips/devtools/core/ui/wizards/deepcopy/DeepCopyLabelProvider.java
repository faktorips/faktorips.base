/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.StringUtil;

public abstract class DeepCopyLabelProvider extends StyledCellLabelProvider {

    private final ResourceManager resourceManager;
    private ImageDescriptor overlay = IIpsDecorators.getImageHandling().getSharedImageDescriptor(
            "overlays/LinkOverlay.gif", //$NON-NLS-1$
            true);
    private final DeepCopyPreview deepCopyPreview;
    private final int segmentsToIgnore;

    public DeepCopyLabelProvider(DeepCopyPreview deepCopyPreview) {
        this.deepCopyPreview = deepCopyPreview;
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        segmentsToIgnore = deepCopyPreview.getSegmentsToIgnore(deepCopyPreview.getPresentationModel()
                .getAllCopyElements(false));
    }

    @Override
    public abstract void update(ViewerCell cell);

    protected Image getImage(Object element) {
        if (element instanceof IProductCmptStructureReference reference) {
            boolean enabled = deepCopyPreview.getPresentationModel().getTreeStatus().isEnabled(reference);
            return switch (element) {
                case IProductCmptReference productCmptReference -> IpsUIPlugin.getImageHandling()
                        .getImage(productCmptReference.getProductCmpt(), enabled);
                case IProductCmptTypeAssociationReference productCmptTypeAssociationReference -> IpsUIPlugin
                        .getImageHandling().getImage(productCmptTypeAssociationReference.getAssociation(), enabled);
                case IProductCmptStructureTblUsageReference productCmptStructureTblUsageReference -> IpsUIPlugin
                        .getImageHandling()
                        .getImage(productCmptStructureTblUsageReference.getTableContentUsage(), enabled);
                default -> null;
            };
        }
        return null;
    }

    protected String getNewName(Object element) {
        if (element instanceof IProductCmptStructureReference reference) {
            if (deepCopyPreview.getPresentationModel().getTreeStatus().isEnabled(reference)
                    && deepCopyPreview.getPresentationModel().getTreeStatus()
                            .getCopyOrLink(reference) == CopyOrLink.COPY) {
                return deepCopyPreview.getNewName(reference.getWrappedIpsObject());
            }
        }
        return null;
    }

    protected String getOldName(Object element) {
        return switch (element) {
            case IProductCmptReference productCmptReference -> productCmptReference.getProductCmpt().getName();
            case IProductCmptTypeAssociationReference productCmptTypeAssociationReference -> {
                IAssociation association = productCmptTypeAssociationReference.getAssociation();
                if (association.is1ToMany()) {
                    yield IIpsModel.get().getMultiLanguageSupport().getLocalizedPluralLabel(association);
                } else {
                    yield IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(association);
                }
            }
            case IProductCmptStructureTblUsageReference productCmptStructureTblUsageReference -> StringUtil
                    .unqualifiedName(
                            productCmptStructureTblUsageReference.getTableContentUsage().getTableContentName());
            default -> Messages.DeepCopyLabelProvider_textUndefined;
        };
    }

    protected String getNewOrOldName(Object element) {
        if (element instanceof IProductCmptTypeAssociationReference) {
            return getOldName(element);
        }
        if (isLinkedElement((IProductCmptStructureReference)element)) {
            return getOldName(element);
        } else {
            return getNewName(element);
        }
    }

    protected Image getErrorImage(Object element) {
        if (getErrorText(element) != null) {
            return IpsUIPlugin.getImageHandling().getSharedImage("error_tsk.gif", true); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    protected Image getObjectImage(Object element) {
        if ((element instanceof IProductCmptStructureReference)
                && isLinkedElement((IProductCmptStructureReference)element)) {
            Image image = getImage(element);
            return resourceManager.get(new DecorationOverlayIcon(image, overlay, IDecoration.BOTTOM_RIGHT));
        } else {
            return getImage(element);
        }
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
        super.dispose();
    }

    protected void addStyledSuffix(ViewerCell cell) {
        Object element = cell.getElement();
        String suffix = getSuffixFor(element);
        StyleRange styledPath = new StyleRange();
        String name = cell.getText();
        styledPath.start = name.length();
        styledPath.length = suffix.length();
        styledPath.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
        styledPath.fontStyle = SWT.NORMAL;
        cell.setText(name + suffix);
        cell.setStyleRanges(new StyleRange[] { styledPath });
    }

    private String getSuffixFor(Object item) {
        return switch (item) {
            case IProductCmptReference productCmptReference -> {
                String packageName = ""; //$NON-NLS-1$
                if (deepCopyPreview.getPresentationModel().getTreeStatus()
                        .getCopyOrLink(productCmptReference) == CopyOrLink.COPY) {
                    packageName = deepCopyPreview.buildTargetPackageName(deepCopyPreview.getPresentationModel()
                            .getTargetPackage(), productCmptReference.getWrappedIpsObject(), segmentsToIgnore);
                } else {
                    packageName = productCmptReference.getProductCmpt().getIpsPackageFragment().getName();
                }
                if (IpsStringUtils.isEmpty(packageName)) {
                    packageName = ""; //$NON-NLS-1$
                }
                yield " - " + packageName; //$NON-NLS-1$
            }
            case IProductCmptStructureTblUsageReference productCmptStructureTblUsageReference -> {
                String packageName = deepCopyPreview.buildTargetPackageName(
                        deepCopyPreview.getPresentationModel().getTargetPackage(),
                        productCmptStructureTblUsageReference.getWrappedIpsObject(),
                        segmentsToIgnore);
                yield " - " + packageName; //$NON-NLS-1$
            }
            default -> IpsStringUtils.EMPTY;
        };
    }

    @Override
    public String getToolTipText(Object element) {
        return getErrorText(element);
    }

    public String getErrorText(Object element) {
        return deepCopyPreview.getErrorElements().get(element);
    }

    private boolean isLinkedElement(IProductCmptStructureReference element) {
        return deepCopyPreview.getPresentationModel().getTreeStatus().getCopyOrLink(element) == CopyOrLink.LINK;
    }

}
