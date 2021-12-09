/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IGotoIpsObjectPart;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation.ProductCmptDeltaDialog;
import org.faktorips.devtools.core.ui.filter.IProductCmptPropertyFilter;
import org.faktorips.devtools.core.ui.filter.IPropertyVisibleController;
import org.faktorips.devtools.core.ui.internal.filter.PropertyVisibleController;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.ProductCmptTypeDescriptionPage;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;

/**
 * Editor to a edit a product component.
 * 
 * @author Jan Ortmann
 * @author Thorsten Guenther
 */
public class ProductCmptEditor extends TimedIpsObjectEditor implements IModelDescriptionSupport, IGotoIpsObjectPart {

    /**
     * Setting key for user's decision not to choose a new product component type, because the old
     * can't be found.
     */
    private static final String SETTING_WORK_WITH_MISSING_TYPE = "workWithMissingType"; //$NON-NLS-1$

    private GenerationPropertiesPage generationPropertiesPage;

    private final IPropertyVisibleController visibilityController = new PropertyVisibleController();

    public ProductCmptEditor() {
        initVisibilityController();
    }

    private void initVisibilityController() {
        List<IProductCmptPropertyFilter> filters = IpsUIPlugin.getDefault().getPropertyVisibilityFilters();
        visibilityController.addFilters(filters);
        visibilityController.setRefreshCallback(this::refreshIncludingStructuralChanges);
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException {
        generationPropertiesPage = new GenerationPropertiesPage(this);
        addPage(generationPropertiesPage);
        addPage(new ProductCmptPropertiesPage(this));

        if (getActiveGeneration() == null) {
            setActiveGeneration(getInitialGeneration());
        }
    }

    private IProductCmptGeneration getInitialGeneration() {
        IProductCmptGeneration initialGeneration = getProductCmpt().getLatestProductCmptGeneration();
        if (getEditorInput() instanceof ProductCmptEditorInput) {
            ProductCmptEditorInput productCmptEditorInput = (ProductCmptEditorInput)getEditorInput();
            IProductCmptGeneration inputGeneration = productCmptEditorInput.getProductCmptGeneration();
            if (inputGeneration != null && !inputGeneration.isDeleted()) {
                initialGeneration = inputGeneration;
            }
        }
        return initialGeneration;
    }

    private GenerationPropertiesPage getGenerationPropertiesPage() {
        if (generationPropertiesPage == null || generationPropertiesPage.getPartControl() == null
                || generationPropertiesPage.getPartControl().isDisposed()) {
            return null;
        }
        return generationPropertiesPage;
    }

    /**
     * Returns the product component for the source file edited with this editor.
     */
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getIpsObject();
    }

    // The method is overridden, to enable access from classes in same package.
    @Override
    protected void checkForInconsistenciesToModel() {
        if (TRACE) {
            logMethodStarted("checkForInconsistenciesToModel"); //$NON-NLS-1$
        }
        checkMissingType();
        super.checkForInconsistenciesToModel();
        if (TRACE) {
            logMethodFinished("checkForInconsistenciesToModel"); //$NON-NLS-1$
        }
    }

    private void checkMissingType() {
        // open the select type dialog if the type is missing and the data is changeable
        if (getProductCmpt().findProductCmptType(getIpsProject()) == null && super.computeDataChangeableState()
                && !getSettings().getBoolean(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE)) {
            String msg = NLS.bind(Messages.ProductCmptEditor_msgTypeNotFound, getProductCmpt().getProductCmptType());
            SetProductCmptTypeDialog d = new SetProductCmptTypeDialog(getProductCmpt(), getSite().getShell(), msg);
            int rc = d.open();
            if (rc == Window.CANCEL) {
                getSettings().put(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE, true);
            } else {
                refreshIncludingStructuralChanges();
            }
        }
    }

    @Override
    protected String getUniformPageTitle() {
        if (!isSrcFileUsable()) {
            String filename = getIpsSrcFile() == null ? "null" : getIpsSrcFile().getName(); //$NON-NLS-1$
            return NLS.bind(Messages.ProductCmptEditor_msgFileOutOfSync, filename);
        }
        String localizedCaption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(getProductCmpt());
        String name = getProductCmpt().getName();
        if (getProductCmpt().isProductTemplate()) {
            return NLS.bind(Messages.ProductCmptEditor_templateTitle, localizedCaption, name);
        } else {
            return NLS.bind(Messages.ProductCmptEditor_productCmptTitle, localizedCaption, name);
        }
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);
        if (isStructuralChangeEvent(event)) {
            Display display = PlatformUI.getWorkbench().getDisplay();
            display.syncExec(this::refreshIncludingStructuralChanges);
        }
    }

    protected boolean isStructuralChangeEvent(ContentChangeEvent event) {
        return event.isAffected(getIpsObject())
                && (isGenerationAdded(event) || event.isPropertyAffected(IProductCmptTypeAttribute.PROPERTY_VISIBLE)
                        || event.isPropertyAffected(IProductCmpt.PROPERTY_TEMPLATE));
    }

    private boolean isGenerationAdded(ContentChangeEvent event) {
        if (event.getEventType() == ContentChangeEvent.TYPE_PART_ADDED
                && event.getPart() instanceof IProductCmptGeneration) {
            IProductCmptGeneration cmptGeneration = (IProductCmptGeneration)event.getPart();
            return cmptGeneration.getProductCmpt().equals(getProductCmpt());
        } else {
            return false;
        }
    }

    @Override
    public void setActiveGeneration(IIpsObjectGeneration generation) {
        if (generation == null) {
            return;
        }
        if (generation != getActiveGeneration()) {
            super.setActiveGeneration(generation);
            if (getGenerationPropertiesPage() != null) {
                generationPropertiesPage.rebuildInclStructuralChanges();
            }
            // Refreshes the visible controller by application start
            getVisibilityController().updateUI(false);
            refresh();
        }
    }

    @Override
    protected boolean computeDataChangeableState() {
        if (!super.computeDataChangeableState()) {
            return false;
        }
        return getProductCmpt().findProductCmptType(getIpsProject()) != null;
    }

    /**
     * Returns <code>true</code> if the active generation is editable, otherwise <code>false</code>.
     */
    public boolean isActiveGenerationEditable() {
        return IpsUIPlugin.getDefault().isGenerationEditable((IProductCmptGeneration)getActiveGeneration());
    }

    /**
     * Shows the generation which is effective on the given date
     */
    public void showGenerationEffectiveOn(GregorianCalendar date) {
        IIpsObjectGeneration generation = getProductCmpt().getGenerationEffectiveOn(date);
        if (generation == null) {
            generation = getProductCmpt().getFirstGeneration();
        }
        setActiveGeneration(generation);
    }

    @Override
    protected Dialog createDialogToFixDifferencesToModel() throws CoreRuntimeException {
        IIpsObjectGeneration[] gen = getProductCmpt().getGenerationsOrderedByValidDate();
        IProductCmptGeneration[] generations = new IProductCmptGeneration[gen.length];
        for (int i = 0; i < generations.length; i++) {
            generations[i] = (IProductCmptGeneration)gen[i];
        }

        IFixDifferencesComposite deltas = getProductCmpt().computeDeltaToModel(getIpsProject());

        return new ProductCmptDeltaDialog(deltas, getSite().getShell());
    }

    @Override
    protected void refreshIncludingStructuralChanges() {
        getIpsSrcFile().getIpsObject();
        fixActiveGeneration();
        if (getGenerationPropertiesPage() != null) {
            generationPropertiesPage.rebuildInclStructuralChanges();
        }
    }

    /**
     * Current generation may be deleted in delta fix.
     */
    private void fixActiveGeneration() {
        IIpsObjectGeneration activeGeneration = getActiveGeneration();
        if (activeGeneration != null && activeGeneration.isDeleted()) {
            setActiveGeneration(getInitialGeneration());
        }
    }

    private void logMethodStarted(String msg) {
        logInternal("." + msg + " - started"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    private void logMethodFinished(String msg) {
        logInternal("." + msg + " - finished"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    private void logInternal(String msg) {
        String file = getIpsSrcFile() == null ? "null" : getIpsSrcFile().getName(); // $NON-NLS-1$ //$NON-NLS-1$
        System.out.println(getLogPrefix() + msg
                + ", IpsSrcFile=" + file + ", Thread=" + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$
                                                                                            // $NON-NLS-2$
    }

    private String getLogPrefix() {
        return "ProductCmptEditor"; //$NON-NLS-1$
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreRuntimeException {
        IProductCmptType cmptType = getProductCmpt().findProductCmptType(getIpsProject());
        if (cmptType != null) {
            return new ProductCmptTypeDescriptionPage(cmptType);
        } else {
            return null;
        }
    }

    @Override
    protected List<IMessage> getMessages() {
        List<IMessage> messages = super.getMessages();
        if (getGenerationPropertiesPage().showsNotLatestGeneration()) {
            messages.add(0, getGenerationPropertiesPage().getNotLatestGenerationMessage());
        }
        return messages;
    }

    @Override
    protected String createHeaderMessage(List<IMessage> messages, int messageType) {
        if (!getGenerationPropertiesPage().showsNotLatestGeneration()) {
            return super.createHeaderMessage(messages, messageType);
        }
        return getHeaderMessage(messages, messageType);
    }

    private String getHeaderMessage(List<IMessage> messages, int messageType) {
        String generationName = getGenerationPropertiesPage().getGenerationName(getActiveGeneration());
        if (messages.size() == 1) {
            return generationName;
        }
        List<IMessage> filteredList = messages.subList(1, messages.size());
        String headerMessage = super.createHeaderMessage(filteredList, messageType);
        if (StringUtils.isBlank(headerMessage)) {
            return generationName;
        }
        return generationName + System.lineSeparator() + headerMessage;
    }

    @Override
    public void gotoIpsObjectPart(IIpsObjectPart part) {
        setActiveGeneration(getGeneration(part));
        generationPropertiesPage.gotoIpsObjectPart(part);
    }

    private IIpsObjectGeneration getGeneration(IIpsObjectPart part) {
        IIpsObjectPartContainer partContainer = part;
        while (true) {
            if (partContainer instanceof IIpsObjectGeneration) {
                return (IIpsObjectGeneration)partContainer;
            }
            IIpsElement parent = partContainer.getParent();
            if (parent instanceof IIpsObjectPartContainer) {
                partContainer = (IIpsObjectPartContainer)parent;
            } else {
                return null;
            }
        }
    }

    public IPropertyVisibleController getVisibilityController() {
        return visibilityController;
    }

}
