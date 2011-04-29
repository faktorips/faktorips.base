/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.apache.commons.lang.StringUtils;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.util.StringUtil;

public abstract class DeepCopyLabelProvider extends StyledCellLabelProvider {

    private final ResourceManager resourceManager;
    private ImageDescriptor overlay = IpsUIPlugin.getImageHandling().getSharedImageDescriptor("LinkOverlay.gif", true); //$NON-NLS-1$
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
        if (element instanceof IProductCmptStructureReference) {
            IProductCmptStructureReference reference = (IProductCmptStructureReference)element;
            boolean enabled = deepCopyPreview.getPresentationModel().getTreeStatus().isEnabled(reference);
            if (element instanceof IProductCmptReference) {
                return IpsUIPlugin.getImageHandling().getImage(((IProductCmptReference)element).getProductCmpt(),
                        enabled);
            } else if (element instanceof IProductCmptTypeAssociationReference) {
                return IpsUIPlugin.getImageHandling().getImage(
                        ((IProductCmptTypeAssociationReference)element).getAssociation(), enabled);
            } else if (element instanceof IProductCmptStructureTblUsageReference) {
                return IpsUIPlugin.getImageHandling().getImage(
                        ((IProductCmptStructureTblUsageReference)element).getTableContentUsage(), enabled);
            }
        }
        return null;
    }

    protected String getNewName(Object element) {
        if (element instanceof IProductCmptStructureReference) {
            IProductCmptStructureReference reference = (IProductCmptStructureReference)element;
            if (deepCopyPreview.getPresentationModel().getTreeStatus().isEnabled(reference)
                    && deepCopyPreview.getPresentationModel().getTreeStatus().getCopyOrLink(reference) == CopyOrLink.COPY) {
                return deepCopyPreview.getNewName(reference.getWrappedIpsObject());
            }
        }
        return null;
    }

    protected String getOldName(Object element) {
        if (element instanceof IProductCmptReference) {
            IProductCmptReference productCmptReference = (IProductCmptReference)element;
            return productCmptReference.getProductCmpt().getName();
        } else if (element instanceof IProductCmptTypeAssociationReference) {
            IAssociation association = ((IProductCmptTypeAssociationReference)element).getAssociation();
            if (association.is1ToMany()) {
                return IpsPlugin.getMultiLanguageSupport().getLocalizedPluralLabel(association);
            } else {
                return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(association);
            }
        } else if (element instanceof IProductCmptStructureTblUsageReference) {
            return StringUtil.unqualifiedName(((IProductCmptStructureTblUsageReference)element).getTableContentUsage()
                    .getTableContentName());
        }
        return Messages.DeepCopyLabelProvider_textUndefined;
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
        if (element instanceof IProductCmptStructureReference) {
            if (isLinkedElement((IProductCmptStructureReference)element)) {
                Image image = getImage(element);
                return (Image)resourceManager.get(new DecorationOverlayIcon(image, overlay, IDecoration.BOTTOM_RIGHT));
            } else {
                return getImage(element);
            }
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
        if (item instanceof IProductCmptReference) {
            IProductCmptReference productCmptReference = (IProductCmptReference)item;
            String packageName = ""; //$NON-NLS-1$
            if (deepCopyPreview.getPresentationModel().getTreeStatus().getCopyOrLink(productCmptReference) == CopyOrLink.COPY) {
                packageName = deepCopyPreview.buildTargetPackageName(deepCopyPreview.getPresentationModel()
                        .getTargetPackage(), productCmptReference.getWrappedIpsObject(), segmentsToIgnore);
            } else {
                packageName = productCmptReference.getProductCmpt().getIpsPackageFragment().getName();
            }
            if (StringUtils.isEmpty(packageName)) {
                packageName = ""; //$NON-NLS-1$
            }
            return " - " + packageName; //$NON-NLS-1$
        } else if (item instanceof IProductCmptStructureTblUsageReference) {
            String packageName = deepCopyPreview.buildTargetPackageName(deepCopyPreview.getPresentationModel()
                    .getTargetPackage(), ((IProductCmptStructureTblUsageReference)item).getWrappedIpsObject(),
                    segmentsToIgnore);
            return " - " + packageName; //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
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
