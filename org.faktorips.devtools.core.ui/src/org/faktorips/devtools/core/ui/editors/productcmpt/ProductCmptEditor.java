/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.TimedIpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation.ProductCmptDeltaDialog;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.values.DateUtil;

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
     * Setting key for the working date used in the editor. This might differ from the one defined
     * in the preferences.
     */
    private final static String SETTING_WORKING_DATE = "workingDate"; //$NON-NLS-1$

    /**
     * Setting key for user's decision not to choose a new product component type, because the old
     * can't be found.
     */
    private final static String SETTING_ACTIVE_GENERATION_MANUALLY_SET = "activeGenerationManuallySet"; //$NON-NLS-1$

    private GenerationPropertiesPage generationPropertiesPage;

    private boolean ignoreHandlingOfWorkingDateMissmatch = false;
    private boolean isHandlingWorkingDateMismatch = false;

    /**
     * Creates a new editor for product components.
     */
    public ProductCmptEditor() {
        super();
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        // if the editor was opened by using the generation
        // then no generation mismatch dialog should be displayed
        if (input instanceof ProductCmptEditorInput) {
            ignoreHandlingOfWorkingDateMissmatch = ((ProductCmptEditorInput)input).isIgnoreWorkingDateMissmatch();
        }
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        generationPropertiesPage = new GenerationPropertiesPage(this);
        addPage(generationPropertiesPage);
        addPage(new ProductCmptPropertiesPage(this));
        addPage(new DescriptionPage(this));
        IIpsObjectGeneration gen = getGenerationEffectiveOnCurrentEffectiveDate();
        if (gen == null) {
            gen = getProductCmpt().getGenerationsOrderedByValidDate()[getProductCmpt().getNumOfGenerations() - 1];
        }
        setActiveGeneration(gen, false);
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
    IProductCmpt getProductCmpt() {
        try {
            return (IProductCmpt)getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    private GregorianCalendar getWorkingDateUsedInEditor() {
        String s = getSettings().get(getIpsSrcFile(), SETTING_WORKING_DATE);
        try {
            return DateUtil.parseIsoDateStringToGregorianCalendar(s);
        } catch (IllegalArgumentException e) {
            IpsPlugin.log(e); // if it can't be parsed we use null.
            return null;
        }
    }

    private void setWorkingDateUsedInEditor(GregorianCalendar date) {
        getSettings().put(getIpsSrcFile(), SETTING_WORKING_DATE, DateUtil.gregorianCalendarToIsoDateString(date));
    }

    @Override
    public void editorActivated() {
        if (TRACE) {
            logMethodStarted("editorActivated()"); //$NON-NLS-1$
        }
        updateChosenActiveGeneration();
        super.editorActivated();
        if (TRACE) {
            logMethodFinished("editorActivated()"); //$NON-NLS-1$
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
        if (getProductCmpt().findProductCmptType(getIpsProject()) == null
                && couldDateBeChangedIfProductCmptTypeWasntMissing() && !IpsPlugin.getDefault().isTestMode()
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
        return Messages.ProductCmptEditor_productComponent + getProductCmpt().getName();
    }

    /**
     * Checks if the currently active generations valid-from-date matches exactly the currently set
     * working date. If not so, a search for a matching generation is started. If nothing is found,
     * the user is asked to create a new one.
     */
    private void updateChosenActiveGeneration() {
        try {
            if (!getIpsSrcFile().isContentParsable()) {
                return;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return;
        }

        if (ignoreHandlingOfWorkingDateMissmatch) {
            return;
        }
        ignoreHandlingOfWorkingDateMissmatch = true;

        IProductCmpt prod = getProductCmpt();
        GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        IProductCmptGeneration generation = (IProductCmptGeneration)prod.getGenerationByEffectiveDate(workingDate);

        if (generation != null) {
            setWorkingDateUsedInEditor(workingDate);
            if (!generation.equals(getActiveGeneration())) {
                // we found a generation matching the working date, but the found one is not active,
                // so make it active.
                this.setActiveGeneration(generation, false);
            }
            return;
        }
        // no generation for the _exact_ current working date.
        if (workingDate.equals(getWorkingDateUsedInEditor())) {
            // check happned before and user decided not to create a new generation - dont bother
            // the user with repeating questions.
            return;
        }
        IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
        if (prefs.isWorkingModeBrowse()) {
            // just browsing - show the generation valid at working date
            if (!getSettings().getBoolean(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET)) {
                showGenerationEffectiveOn(prefs.getWorkingDate());
            }
            return;
        }
        if (!IpsUIPlugin.isEditable(getIpsSrcFile())) {
            // no check of working date mismatch
            // because product component is read only
            return;
        }
        handleWorkingDateMissmatch();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!isActive()) {
            super.propertyChange(event);
            return;
        }
        String property = event.getProperty();
        if (property.equals(IpsPreferences.WORKING_DATE)) {
            getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, false);
            updateChosenActiveGeneration();
        } else if (property.equals(IpsPreferences.EDIT_RECENT_GENERATION)) {
            refresh();
        } else if (event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
            getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, false);
            // refresh is done in superclass
        } else if (event.getProperty().equals(IpsPreferences.RANGE_EDIT_FIELDS_IN_ONE_ROW)) {
            refreshInclStructuralChanges();
            refresh();
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
        if (!couldDateBeChangedIfProductCmptTypeWasntMissing()) {
            return false;
        }
        try {
            return getProductCmpt().findProductCmptType(getIpsProject()) != null;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    boolean couldDateBeChangedIfProductCmptTypeWasntMissing() {
        return super.computeDataChangeableState();
    }

    /**
     * Returns <code>true</code> if the active generation is editable, otherwise <code>false</code>.
     */
    public boolean isActiveGenerationEditable() {
        return isGenerationEditable((IProductCmptGeneration)getActiveGeneration());
    }

    /**
     * Returns <code>true</code> if the given generation is editable, otherwise <code>false</code>.
     */
    public boolean isGenerationEditable(IProductCmptGeneration gen) {
        if (gen == null) {
            return false;
        }
        // if generation is not effective in the current effective date, no editing is possible
        if (!gen.equals(getGenerationEffectiveOnCurrentEffectiveDate())) {
            return false;
        }
        if (!IpsUIPlugin.isEditable(gen.getIpsSrcFile())) {
            return false;
        }
        if (gen.isValidFromInPast() != null && gen.isValidFromInPast().booleanValue()) {
            IpsPreferences pref = IpsPlugin.getDefault().getIpsPreferences();
            return pref.canEditRecentGeneration();
        }
        return true;
    }

    /**
     * Shows the generation wich is effective on the given date
     */
    public void showGenerationEffectiveOn(GregorianCalendar date) {
        IIpsObjectGeneration generation = getProductCmpt().findGenerationEffectiveOn(date);
        if (generation == null) {
            generation = getProductCmpt().getFirstGeneration();
        }
        setActiveGeneration(generation, false);
    }

    private void handleWorkingDateMissmatch() {
        IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();

        // following if statement is there as closing the dialog triggers a window activated event
        // and handling the event calls this method.
        if (isHandlingWorkingDateMismatch) {
            return;
        }
        isHandlingWorkingDateMismatch = true;
        IProductCmpt cmpt = getProductCmpt();

        GenerationSelectionDialog dialog = new GenerationSelectionDialog(getContainer().getShell(), cmpt);

        dialog.open(); // closing the dialog triggers an window activation event

        isHandlingWorkingDateMismatch = false;
        int choice = -1;
        if (IpsPlugin.getDefault().isTestMode()) {
            choice = IpsPlugin.getDefault().getTestAnswerProvider().getIntAnswer();
        } else {
            if (dialog.getReturnCode() == Window.OK) {
                choice = dialog.getChoice();
                prefs.setEditRecentGeneration(dialog.isCanEditRecentGenerations());
            }
        }
        GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        setWorkingDateUsedInEditor(workingDate);
        switch (choice) {
            case GenerationSelectionDialog.CHOICE_BROWSE:
                setActiveGeneration(dialog.getSelectedGeneration(), false);
                break;
            case GenerationSelectionDialog.CHOICE_CREATE:
                setActiveGeneration(cmpt.newGeneration(workingDate), false);
                break;
            case GenerationSelectionDialog.CHOICE_SWITCH:
                setActiveGeneration(dialog.getSelectedGeneration(), true);
                prefs.setWorkingDate(dialog.getSelectedGeneration().getValidFrom());
                break;
            default:
                // show generation valid on current working date or if there is no valid generation,
                // show the first generation
                IIpsObjectGeneration currGeneration = cmpt.findGenerationEffectiveOn(workingDate);
                if (currGeneration == null) {
                    currGeneration = cmpt.getFirstGeneration();
                }
                setActiveGeneration(currGeneration, false);
                break;
        }
    }

    @Override
    protected Dialog createDialogToFixDifferencesToModel() throws CoreException {
        IIpsObjectGeneration[] gen = getProductCmpt().getGenerationsOrderedByValidDate();
        IProductCmptGeneration[] generations = new IProductCmptGeneration[gen.length];
        for (int i = 0; i < generations.length; i++) {
            generations[i] = (IProductCmptGeneration)gen[i];
        }
        IGenerationToTypeDelta[] deltas = new IGenerationToTypeDelta[generations.length];
        for (int i = 0; i < generations.length; i++) {
            deltas[i] = (generations[i]).computeDeltaToModel(getIpsProject());
        }

        return new ProductCmptDeltaDialog(generations, deltas, getSite().getShell());
    }

    @Override
    public void contentsChanged(final ContentChangeEvent event) {
        if (event.getIpsSrcFile().equals(getIpsSrcFile())) {
            if (event.getEventType() == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
                setWorkingDateUsedInEditor(null);
                getSettings().put(getIpsSrcFile(), SETTING_ACTIVE_GENERATION_MANUALLY_SET, false);
            }
        }
        super.contentsChanged(event);
    }

    @Override
    protected void refreshInclStructuralChanges() {
        try {
            getIpsSrcFile().getIpsObject();
            updateChosenActiveGeneration();
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
        return new ProductCmptModelDescriptionPage(this);
    }

    /**
     * Set if a mismatch of a working date will be handled or ignored.
     */
    public void setIgnoreHandlingOfWorkingDateMissmatch(boolean ignoreHandlingOfWorkingDateMissmatch) {
        this.ignoreHandlingOfWorkingDateMissmatch = ignoreHandlingOfWorkingDateMissmatch;
    }

}
