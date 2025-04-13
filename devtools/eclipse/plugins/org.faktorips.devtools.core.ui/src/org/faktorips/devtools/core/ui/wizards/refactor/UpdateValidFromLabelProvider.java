/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.deepcopy.Messages;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IMultiLanguageSupport;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.util.StringUtil;

public class UpdateValidFromLabelProvider extends StyledCellLabelProvider {

    private final UpdateValidfromPresentationModel presentationModel;

    private DateFormat dateFormat;

    public UpdateValidFromLabelProvider(UpdateValidfromPresentationModel presentationModel) {
        this.presentationModel = Objects.requireNonNull(presentationModel);
        dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
    }

    @Override
    public void update(ViewerCell cell) {
        // This can be used to override default behavior if needed
        super.update(cell);
    }

    /**
     * Returns the generation ID label for the given element.
     */
    protected String getGenerationID(Object element) {
        if (!(element instanceof IProductCmptStructureReference reference)
                || !presentationModel.getTreeStatus().isChecked(reference)) {
            return StringUtils.EMPTY;
        }

        if (reference instanceof IProductCmptStructureTblUsageReference tblRef) {
            return getTableGenerationId(tblRef);
        }

        return getProductCmptGenerationId(reference);
    }

    /**
     * Returns a styled suffix showing the change from old valid-from to new valid-from. The text is
     * styled in gray and small font.
     *
     * @param element The element to extract dates from.
     * @return A styled string in the format [old → new] or empty if not applicable.
     */
    public StyledString getValidFromSuffix(Object element) {
        if (!(element instanceof IProductCmptStructureReference ref)) {
            return new StyledString();
        }

        UpdateValidfromPresentationModel model = getPresentationModel();

        if (!model.getTreeStatus().isChecked(ref)) {
            return new StyledString();
        }

        IProductCmpt productCmpt = getProductCmpt(ref.getWrappedIpsObject());
        if (productCmpt == null || productCmpt.getValidFrom() == null) {
            return new StyledString();
        }

        GregorianCalendar oldDate = productCmpt.getValidFrom();
        GregorianCalendar newDate = model.getNewValidFrom();

        if (newDate == null) {
            return new StyledString();
        }

        String formattedOld = dateFormat.format(oldDate.getTime());
        String formattedNew = dateFormat.format(newDate.getTime());

        if (formattedOld.equals(formattedNew)) {
            return new StyledString();
        }

        String suffix = "  [" + formattedOld + " → " + formattedNew + "]";
        return new StyledString(suffix, StyledString.DECORATIONS_STYLER);
    }

    /**
     * Returns the adjusted generation ID for a table reference.
     */
    private String getTableGenerationId(IProductCmptStructureTblUsageReference tblRef) {
        String originalName = tblRef.getTableContentUsage().getTableContentName();
        String unqualified = StringUtil.unqualifiedName(originalName);

        if (!presentationModel.isChangeGenerationId()) {
            return unqualified;
        }

        String newVersionId = presentationModel.getNewVersionId();
        if (StringUtils.isBlank(newVersionId)) {
            return unqualified;
        }

        return replaceVersionSuffix(unqualified, newVersionId);
    }

    private IProductCmpt getProductCmpt(IIpsObject partOfProductCmpt) {
        if (partOfProductCmpt == null) {
            return null;
        }

        IIpsElement parent = partOfProductCmpt.getIpsObject();
        if (parent instanceof IProductCmpt) {
            return (IProductCmpt)parent;
        }
        return null;
    }

    /**
     * Returns the adjusted generation ID for a product component reference.
     */
    private String getProductCmptGenerationId(IProductCmptStructureReference reference) {
        IProductCmpt productCmpt = getProductCmpt(reference.getWrappedIpsObject());
        if (productCmpt == null) {
            return StringUtils.EMPTY;
        }

        if (presentationModel.isChangeGenerationId()) {
            String newVersionId = presentationModel.getNewVersionId();
            if (StringUtils.isNotBlank(newVersionId)) {
                IProductCmptNamingStrategy namingStrategy = presentationModel.getIpsProject()
                        .getProductCmptNamingStrategy();

                if (namingStrategy != null && namingStrategy.supportsVersionId()) {
                    String kindId = namingStrategy.getKindId(productCmpt.getName());
                    return namingStrategy.getProductCmptName(kindId, newVersionId);
                }
            }
        }

        return getOldName(reference);
    }

    /**
     * Returns the ValidFrom date string, or empty if the element is unchecked or invalid.
     */
    protected String getValidFrom(Object element) {
        if (!(element instanceof IProductCmptStructureReference ref)
                || !presentationModel.getTreeStatus().isChecked(ref)) {
            return StringUtils.EMPTY;
        }

        IProductCmpt productCmpt = getProductCmpt(ref.getWrappedIpsObject());
        if (productCmpt == null || productCmpt.getValidFrom() == null) {
            return StringUtils.EMPTY;
        }

        return dateFormat.format(productCmpt.getValidFrom().getTime());
    }

    /**
     * Returns the label of the element as it originally exists.
     */
    protected String getOldName(Object element) {
        return switch (element) {
            case IProductCmptReference ref -> ref.getProductCmpt().getName();
            case IProductCmptTypeAssociationReference assocRef -> getAssociationLabel(assocRef.getAssociation());
            case IProductCmptStructureTblUsageReference tblRef -> StringUtil
                    .unqualifiedName(tblRef.getTableContentUsage().getTableContentName());
            default -> Messages.DeepCopyLabelProvider_textUndefined;
        };
    }

    /**
     * Returns the localized label of an association.
     */
    private String getAssociationLabel(IAssociation association) {
        IMultiLanguageSupport langSupport = IIpsModel.get().getMultiLanguageSupport();
        return association.is1ToMany()
                ? langSupport.getLocalizedPluralLabel(association)
                : langSupport.getLocalizedLabel(association);
    }

    /**
     * Returns the image to use, potentially decorated for links.
     *
     * @param element The element to get the image for
     * @param forceImageEnabled Whether to force the image to appear enabled
     * @return The image, or {@code null} if not applicable
     */
    protected Image getObjectImage(Object element, boolean forceImageEnabled) {
        if (!(element instanceof IProductCmptStructureReference)) {
            return getImage(element, forceImageEnabled);
        }

        LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
        Image baseImage = getImage(element, forceImageEnabled);
        ImageDescriptor overlay = IIpsDecorators.getImageHandling()
                .getSharedImageDescriptor("overlays/LinkOverlay.gif", true); //$NON-NLS-1$

        return resourceManager.get(new DecorationOverlayIcon(baseImage, overlay, IDecoration.BOTTOM_RIGHT));
    }

    /**
     * Returns the image for the given element.
     *
     * @param element The element to get the image for
     * @param forceImageEnabled Whether to force the image to appear enabled
     * @return The image, or {@code null} if not applicable
     */
    public Image getImage(Object element, boolean forceImageEnabled) {
        if (!(element instanceof IProductCmptStructureReference ref)) {
            return null;
        }

        boolean enabled = forceImageEnabled || presentationModel.getTreeStatus().isEnabled(ref);

        return switch (ref) {
            case IProductCmptReference cmpRef -> IpsUIPlugin.getImageHandling().getImage(cmpRef.getProductCmpt(),
                    enabled);
            case IProductCmptTypeAssociationReference assocRef -> IpsUIPlugin.getImageHandling()
                    .getImage(assocRef.getAssociation(), enabled);
            case IProductCmptStructureTblUsageReference tblRef -> IpsUIPlugin.getImageHandling()
                    .getImage(tblRef.getTableContentUsage(), enabled);
            default -> null;
        };
    }

    /**
     * Replaces the old suffix (after last space) in the name with the new version ID.
     */
    private String replaceVersionSuffix(String name, String newVersionId) {
        int lastSpace = name.lastIndexOf(' ');
        return (lastSpace != -1)
                ? name.substring(0, lastSpace) + " " + newVersionId
                : name + " " + newVersionId;
    }

    public UpdateValidfromPresentationModel getPresentationModel() {
        return presentationModel;
    }
}