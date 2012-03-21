/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation.ProductCmptDeltaDialog;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.ProductCmptTypeDescriptionPage;

/**
 * Editor to a edit a product component.
 * 
 * @author Jan Ortmann
 * @author Thorsten Guenther
 */
public class ProductCmptEditor extends TimedIpsObjectEditor implements IModelDescriptionSupport {

    /**
     * Setting key for user's decision not to choose a new product component type, because the old
     * can't be found.
     */
    private final static String SETTING_WORK_WITH_MISSING_TYPE = "workWithMissingType"; //$NON-NLS-1$

    /**
     * Setting key for user's decision not to choose a new product component type, because the old
     * can't be found.
     */
    private final static String SETTING_ACTIVE_GENERATION_MANUALLY_SET = "activeGenerationManuallySet"; //$NON-NLS-1$

    private GenerationPropertiesPage generationPropertiesPage;

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        generationPropertiesPage = new GenerationPropertiesPage(this);
        addPage(generationPropertiesPage);
        addPage(new ProductCmptPropertiesPage(this));
        setActiveGeneration(getProductCmpt().getLatestGeneration(), false);
    }

    private GenerationPropertiesPage getGenerationPropertiesPage() {
        if (generationPropertiesPage.getPartControl() == null || generationPropertiesPage.getPartControl().isDisposed()) {
            return null;
        }
        return generationPropertiesPage;
    }

    /**
     * Returns the product component for the source file edited with this editor.
     */
    public IProductCmpt getProductCmpt() {
        try {
            return (IProductCmpt)getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    // The method is overridden, to enable access from classes in same package.
    @Override
    protected void checkForInconsistenciesToModel() {
        if (TRACE) {
            logMethodStarted("checkForInconsistenciesToModel"); //$NON-NLS-1$
        }
        try {
            checkMissingType();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        super.checkForInconsistenciesToModel();
        if (TRACE) {
            logMethodFinished("checkForInconsistenciesToModel"); //$NON-NLS-1$
        }
    }

    private void checkMissingType() throws CoreException {
        // open the select template dialog if the template is missing and the data is changeable
        if (getProductCmpt().findProductCmptType(getIpsProject()) == null && super.computeDataChangeableState()
                && !IpsPlugin.getDefault().isTestMode()
                && !getSettings().getBoolean(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE)) {
            String msg = NLS
                    .bind(Messages.ProductCmptEditor_msgTemplateNotFound, getProductCmpt().getProductCmptType());
            SetTemplateDialog d = new SetTemplateDialog(getProductCmpt(), getSite().getShell(), msg);
            int rc = d.open();
            if (rc == Window.CANCEL) {
                getSettings().put(getIpsSrcFile(), SETTING_WORK_WITH_MISSING_TYPE, true);
            }
        }
    }

    @Override
    protected String getUniformPageTitle() {
        if (!isSrcFileUsable()) {
            String filename = getIpsSrcFile() == null ? "null" : getIpsSrcFile().getName(); //$NON-NLS-1$
            return NLS.bind(Messages.ProductCmptEditor_msgFileOutOfSync, filename);
        }
        String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getProductCmpt());
        return localizedCaption + ": " + getProductCmpt().getName(); //$NON-NLS-1$
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!isActive()) {
            super.propertyChange(event);
            return;
        }
        if (event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
            getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, false);
            // refresh is done in superclass
        }
        super.propertyChange(event);
    }

    public void setActiveGeneration(IIpsObjectGeneration generation, boolean manuallySet) {
        if (generation == null) {
            return;
        }
        if (generation != getActiveGeneration()) {
            super.setActiveGeneration(generation);
            if (getGenerationPropertiesPage() != null) {
                generationPropertiesPage.rebuildInclStructuralChanges();
            }
            refresh();
        }
        getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, manuallySet);
    }

    @Override
    protected boolean computeDataChangeableState() {
        if (!super.computeDataChangeableState()) {
            return false;
        }
        try {
            return getProductCmpt().findProductCmptType(getIpsProject()) != null;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
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
        setActiveGeneration(generation, false);
    }

    @Override
    protected Dialog createDialogToFixDifferencesToModel() throws CoreException {
        IIpsObjectGeneration[] gen = getProductCmpt().getGenerationsOrderedByValidDate();
        IProductCmptGeneration[] generations = new IProductCmptGeneration[gen.length];
        for (int i = 0; i < generations.length; i++) {
            generations[i] = (IProductCmptGeneration)gen[i];
        }

        IFixDifferencesComposite deltas = getProductCmpt().computeDeltaToModel(getIpsProject());

        return new ProductCmptDeltaDialog(deltas, getSite().getShell());
    }

    @Override
    public void contentsChanged(final ContentChangeEvent event) {
        if (event.getIpsSrcFile().equals(getIpsSrcFile())) {
            if (event.getEventType() == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
                getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, false);
            }
        }
        super.contentsChanged(event);
    }

    @Override
    protected void refreshIncludingStructuralChanges() {
        try {
            getIpsSrcFile().getIpsObject(); // Updates cache
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        if (getGenerationPropertiesPage() != null) {
            generationPropertiesPage.rebuildInclStructuralChanges();
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
                + ", IpsSrcFile=" + file + ", Thread=" + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    private String getLogPrefix() {
        return "ProductCmptEditor"; //$NON-NLS-1$
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        IProductCmptType cmptType = getProductCmpt().findProductCmptType(getIpsProject());
        if (cmptType != null) {
            return new ProductCmptTypeDescriptionPage(cmptType);
        } else {
            return null;
        }
    }

}
