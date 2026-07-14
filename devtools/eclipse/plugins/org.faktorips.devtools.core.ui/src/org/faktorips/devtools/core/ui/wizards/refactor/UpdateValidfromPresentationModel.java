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

import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyTreeStatus;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IMultiLanguageSupport;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.util.StringUtil;

/**
 * Presentation model for the "Update Valid From" wizard page.
 */
public class UpdateValidfromPresentationModel extends PresentationModelObject {

    public static final String NEW_VALID_FROM = "newValidFrom"; //$NON-NLS-1$
    public static final String NEW_VERSION_ID = "newVersionId"; //$NON-NLS-1$
    public static final String CHANGE_GENERATION_ID = "changeGenerationId"; //$NON-NLS-1$
    public static final String CHANGE_ATTRIBUTES = "changeAttributes"; //$NON-NLS-1$
    public static final String STRUCTURE = "structure"; //$NON-NLS-1$

    public static final String MSG_CODE_EMPTY_NEW_VALID_FROM = "empty_" + NEW_VALID_FROM; //$NON-NLS-1$
    public static final String MSG_CODE_EMPTY_NEW_VERSION_ID = "empty_" + NEW_VERSION_ID; //$NON-NLS-1$
    public static final String MSG_CODE_VALID_FROM_MOVED_PAST_NEXT_GENERATION = "validFromMovedPastNextGeneration"; //$NON-NLS-1$

    private final IProductCmpt productCmpt;
    private final LocalResourceManager resourceManager;
    private final DateFormat dateFormat;
    private final Map<IProductCmpt, SequencedMap<GregorianCalendar, IGenerationChange>> generationsAfterChangeCache = new IdentityHashMap<>();

    private IProductCmptTreeStructure structure;

    private DeepCopyTreeStatus treeStatus;

    private GregorianCalendar newValidFrom;
    private String newVersionId = ""; //$NON-NLS-1$

    private boolean changeGenerationId = true;
    private boolean changeAttributes = false;

    public UpdateValidfromPresentationModel(IProductCmpt productCmpt) {
        treeStatus = new DeepCopyTreeStatus();
        this.productCmpt = productCmpt;
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
        initialiseOldValidFrom();
    }

    public DeepCopyTreeStatus getTreeStatus() {
        return treeStatus;
    }

    public IProductCmpt getProductCmpt() {
        return productCmpt;
    }

    /**
     * Initializes the internal structure based on the current valid-from of the root product.
     */
    private void initialiseOldValidFrom() {
        try {
            IProductCmptTreeStructure newStructure = productCmpt.getStructure(productCmpt.getValidFrom(),
                    productCmpt.getIpsProject());
            initialize(newStructure);
        } catch (CycleInProductStructureException e) {
            return;
        }
    }

    private void initialize(IProductCmptTreeStructure structure) {
        this.structure = structure;
        treeStatus.initialize(structure);
    }

    public GregorianCalendar getNewValidFrom() {
        return newValidFrom;
    }

    public void setNewValidFrom(GregorianCalendar newValue) {
        GregorianCalendar oldValue = newValidFrom;
        newValidFrom = newValue;
        generationsAfterChangeCache.clear();
        notifyListeners(new PropertyChangeEvent(this, NEW_VALID_FROM, oldValue, newValue));
    }

    public String getNewVersionId() {
        return newVersionId;
    }

    public void setNewVersionId(String newValue) {
        String oldValue = newVersionId;
        newVersionId = newValue;
        notifyListeners(new PropertyChangeEvent(this, NEW_VERSION_ID, oldValue, newValue));
    }

    public boolean isChangeGenerationId() {
        return changeGenerationId;
    }

    public void setChangeGenerationId(boolean newValue) {
        boolean oldValue = changeGenerationId;
        changeGenerationId = newValue;
        notifyListeners(new PropertyChangeEvent(this, CHANGE_GENERATION_ID, oldValue, newValue));
    }

    public boolean isChangeAttributes() {
        return changeAttributes;
    }

    public void setChangeAttributes(boolean newValue) {
        boolean oldValue = changeAttributes;
        changeAttributes = newValue;
        notifyListeners(new PropertyChangeEvent(this, CHANGE_ATTRIBUTES, oldValue, newValue));
    }

    public IProductCmptTreeStructure getStructure() {
        return structure;
    }

    public IIpsProject getIpsProject() {
        return structure.getRoot().getProductCmpt().getIpsProject();
    }

    /** Validates user inputs and sets error message if necessary. */
    MessageList validate() {
        var messageList = new MessageList();
        if (getNewValidFrom() == null) {
            messageList.newError(MSG_CODE_EMPTY_NEW_VALID_FROM,
                    Messages.UpdateValidFromSourcePage_emptyValidFomDateError,
                    new ObjectProperty(this, NEW_VALID_FROM));
            return messageList;
        }

        if (isChangeGenerationId() && StringUtils.isBlank(getNewVersionId())) {
            messageList.newError(MSG_CODE_EMPTY_NEW_VERSION_ID, Messages.UpdateValidFromSourcePage_emptyVersionIdError,
                    new ObjectProperty(this, NEW_VERSION_ID));
            return messageList;
        }

        if (messageList.containsErrorMsg()) {
            return messageList;
        }

        IProductCmptNamingStrategy namingStrategy = getNamingStrategy();
        if (namingStrategy != null && namingStrategy.supportsVersionId() && isChangeGenerationId()) {
            MessageList validation = namingStrategy.validateVersionId(getNewVersionId());
            if (validation.containsErrorMsg()) {
                messageList.add(validation);
                return messageList;
            }
        }

        validateAdjustments(messageList);
        return messageList;
    }

    private IProductCmptNamingStrategy getNamingStrategy() {
        return getIpsProject().getProductCmptNamingStrategy();
    }

    private static boolean beforeOrEqual(GregorianCalendar first, GregorianCalendar second) {
        return first.before(second) || first.equals(second);
    }

    private void validateAdjustments(MessageList messageList) {
        var selectedItems = getTreeStatus().getAllEnabledElements(
                CopyOrLink.COPY, getStructure(), false);
        selectedItems.stream().map(IProductCmptStructureReference::getWrappedIpsObject)
                .filter(IProductCmpt.class::isInstance).map(IProductCmpt.class::cast)
                .forEach(p -> validateAdjustments(p, messageList));
    }

    private void validateAdjustments(IProductCmpt product, MessageList messageList) {
        if (product.allowGenerations() && product.getNumOfGenerations() > 1) {
            GregorianCalendar secondGenValidFrom = product.getGenerationsOrderedByValidDate()[1].getValidFrom();
            if (secondGenValidFrom != null && beforeOrEqual(secondGenValidFrom, getNewValidFrom())) {
                var changesOverTimeNamingConvention = IpsPlugin.getDefault().getIpsPreferences()
                        .getChangesOverTimeNamingConvention();
                messageList.newWarning(MSG_CODE_VALID_FROM_MOVED_PAST_NEXT_GENERATION,
                        NLS.bind(Messages.UpdateValidFromSourcePage_ValidFromMovedPastNextGeneration,
                                new Object[] {
                                        dateFormat.format(newValidFrom.getTime()),
                                        changesOverTimeNamingConvention
                                                .getGenerationConceptNameSingular(true),
                                        dateFormat.format(secondGenValidFrom.getTime()),
                                        changesOverTimeNamingConvention
                                                .getGenerationConceptNamePlural(true)
                                }),
                        new ObjectProperty(this, NEW_VALID_FROM));
            }
        }
    }

    SequencedMap<GregorianCalendar, IGenerationChange> getGenerationsAfterChange(IProductCmpt productCmpt) {
        return generationsAfterChangeCache.computeIfAbsent(productCmpt, this::getGenerationsAfterChangeInternal);
    }

    private SequencedMap<GregorianCalendar, IGenerationChange> getGenerationsAfterChangeInternal(
            IProductCmpt productCmpt) {
        SequencedMap<GregorianCalendar, IGenerationChange> map = new LinkedHashMap<>();
        if (productCmpt.allowGenerations() && newValidFrom != null) {
            IIpsObjectGeneration[] generationsOrderedByValidDate = productCmpt.getGenerationsOrderedByValidDate();
            boolean first = true;
            for (int i = 0; i < generationsOrderedByValidDate.length; i++) {
                var generation = generationsOrderedByValidDate[i];
                if (i < generationsOrderedByValidDate.length - 1
                        && beforeOrEqual(generationsOrderedByValidDate[i + 1].getValidFrom(), newValidFrom)) {
                    map.put(generation.getValidFrom(), new Deleted());
                    continue;
                }
                if (first && !generation.getValidFrom().equals(newValidFrom)) {
                    map.put(generation.getValidFrom(), new MovedTo(newValidFrom));
                } else {
                    map.put(generation.getValidFrom(), new Unchanged());
                }
                first = false;
            }
        }
        return map;
    }

    static IProductCmpt getProductCmpt(IIpsObject partOfProductCmpt) {
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
     * Returns the label of the element as it originally exists.
     */
    static String getOldName(Object element) {
        return switch (element) {
            case IProductCmptReference ref -> ref.getProductCmpt().getName();
            case IProductCmptTypeAssociationReference assocRef -> getAssociationLabel(assocRef.getAssociation());
            case IProductCmptStructureTblUsageReference tblRef -> StringUtil
                    .unqualifiedName(tblRef.getTableContentUsage().getTableContentName());
            default -> org.faktorips.devtools.core.ui.wizards.deepcopy.Messages.DeepCopyLabelProvider_textUndefined;
        };
    }

    /**
     * Returns the localized label of an association.
     */
    private static String getAssociationLabel(IAssociation association) {
        IMultiLanguageSupport langSupport = IIpsModel.get().getMultiLanguageSupport();
        return association.is1ToMany()
                ? langSupport.getLocalizedPluralLabel(association)
                : langSupport.getLocalizedLabel(association);
    }

    /**
     * Returns the generation ID label for the given element.
     */
    String getGenerationID(Object element) {
        if (!(element instanceof IProductCmptStructureReference reference)
                || !getTreeStatus().isChecked(reference)) {
            return StringUtils.EMPTY;
        }

        if (reference instanceof IProductCmptStructureTblUsageReference tblRef) {
            return getTableGenerationId(tblRef);
        }

        return getProductCmptGenerationId(reference);
    }

    /**
     * Returns the adjusted generation ID for a table reference.
     */
    private String getTableGenerationId(IProductCmptStructureTblUsageReference tblRef) {
        String originalName = tblRef.getTableContentUsage().getTableContentName();
        String unqualified = StringUtil.unqualifiedName(originalName);

        if (!isChangeGenerationId() || StringUtils.isBlank(newVersionId)) {
            return unqualified;
        }

        return replaceVersionSuffix(unqualified, newVersionId);
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

    /**
     * Returns the adjusted generation ID for a product component reference.
     */
    private String getProductCmptGenerationId(IProductCmptStructureReference reference) {
        IProductCmpt product = getProductCmpt(reference.getWrappedIpsObject());
        if (product == null) {
            return StringUtils.EMPTY;
        }

        if (isChangeGenerationId()) {
            if (StringUtils.isNotBlank(newVersionId)) {
                IProductCmptNamingStrategy namingStrategy = getIpsProject()
                        .getProductCmptNamingStrategy();

                if (namingStrategy != null && namingStrategy.supportsVersionId()) {
                    String kindId = namingStrategy.getKindId(product.getName());
                    return namingStrategy.getProductCmptName(kindId, newVersionId);
                }
            }
        }

        return getOldName(reference);
    }

    /**
     * Returns the image to use, potentially decorated for links.
     *
     * @param element The element to get the image for
     * @param forceImageEnabled Whether to force the image to appear enabled
     * @return The image, or {@code null} if not applicable
     */
    Image getObjectImage(Object element, boolean forceImageEnabled) {
        if (!(element instanceof IProductCmptStructureReference)) {
            return getImage(element, forceImageEnabled);
        }

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
    private Image getImage(Object element, boolean forceImageEnabled) {
        if (!(element instanceof IProductCmptStructureReference ref)) {
            return null;
        }

        boolean enabled = forceImageEnabled || getTreeStatus().isEnabled(ref);

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
     * Returns the ValidFrom date string, or empty if the element is unchecked or invalid.
     */
    String getValidFrom(Object element, DateFormat dateFormat) {
        if (!(element instanceof IProductCmptStructureReference ref)
                || !getTreeStatus().isChecked(ref)) {
            return StringUtils.EMPTY;
        }

        var product = getProductCmpt(ref.getWrappedIpsObject());
        if (product == null || product.getValidFrom() == null) {
            return StringUtils.EMPTY;
        }

        return dateFormat.format(product.getValidFrom().getTime());
    }

    void dispose() {
        resourceManager.dispose();
    }

    sealed interface IGenerationChange {
        // no methods, we just want the typesafe hierarchy
    }

    static record Unchanged() implements IGenerationChange {

    }

    static record Deleted() implements IGenerationChange {

    }

    static record MovedTo(GregorianCalendar newValidFrom) implements IGenerationChange {

    }
}
