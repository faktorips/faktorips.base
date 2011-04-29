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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.values.DateUtil;

/**
 * Dialog with several radio buttons to show a particular generation in read-only modus, switch to a
 * new working date, or create a new generation depending on:
 * <ul>
 * <li>the given product cmpt generations
 * <li>working date
 * <li>option: canEditRecentGenerations and editWorkingMode
 * </ul>
 * 
 * @author Thorsten Guenther
 */
public class GenerationSelectionDialog extends TitleAreaDialog {

    private static final String STORED_CHOICE_ID = IpsPlugin.PLUGIN_ID + ".generationSelectionDialogChoice"; //$NON-NLS-1$

    private UIToolkit toolkit = new UIToolkit(null);
    private BindingContext bindingContext = new BindingContext();

    private IProductCmpt cmpt;

    /**
     * Cache the choice (view, set working date or create new generation) because if the choice is
     * needed, this dialog is already disposed and the information about the choice can not be
     * requested from the buttons, because these buttons are disposed, too.
     */
    private int choice;

    /**
     * Cache the selected generation (see above, <code>choice</code>).
     */
    private int generationIndex;

    private Map<Integer, Button> allButtons = new HashMap<Integer, Button>(3);

    private Hashtable<Button, Integer> choices = new Hashtable<Button, Integer>(3);

    /** User's choice was to browse the generation effective at the current effective date. */
    public static final int CHOICE_BROWSE = 0;

    /**
     * User's choice was to switch the effective date to the effective from of one of the
     * generations.
     */
    public static final int CHOICE_SWITCH = 1;

    /** User's choice was to create a new generation */
    public static final int CHOICE_CREATE = 2;

    private String formatedWorkingDate;
    private GregorianCalendar workingDate;
    private boolean canEditRecentGenerations;
    private boolean editWorkingMode;

    private String generationConceptNameSingular;
    private String generationConceptNameSingularInsideSentence;
    private String generationConceptNamePlural;
    private String generationConceptNamePluralInsideSentence;

    public class WorkingDatePmo extends PresentationModelObject {
        public static final String CAN_EDIT_RECENT_GENERATION = "editRecentGeneration"; //$NON-NLS-1$

        public boolean isEditRecentGeneration() {
            return canEditRecentGenerations;
        }

        public void setEditRecentGeneration(boolean editRecentGeneration) {
            canEditRecentGenerations = editRecentGeneration;
            validate();
        }
    }

    public GenerationSelectionDialog(Shell parentShell, IProductCmpt cmpt, String formatedWorkingDate,
            GregorianCalendar workingDate, String generationConceptNameSingular,
            String generationConceptNameSingularInsideSentence, String generationConceptNamePlural,
            String generationConceptNamePluralInsideSentence, boolean canEditRecentGenerations, boolean editWorkingMode) {

        super(parentShell);

        init(cmpt, formatedWorkingDate, workingDate, generationConceptNameSingular,
                generationConceptNameSingularInsideSentence, generationConceptNamePlural,
                generationConceptNamePluralInsideSentence, canEditRecentGenerations, editWorkingMode);
    }

    public GenerationSelectionDialog(Shell parentShell, IProductCmpt cmpt) {
        super(parentShell);

        IpsPreferences prefs = IpsPlugin.getDefault().getIpsPreferences();
        String generationConceptNameSingular = prefs.getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular();
        String generationConceptNameSingularInsideSentence = prefs.getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular(true);
        String generationConceptNamePlural = prefs.getChangesOverTimeNamingConvention()
                .getGenerationConceptNamePlural();
        String generationConceptNamePluralInsideSentence = prefs.getChangesOverTimeNamingConvention()
                .getGenerationConceptNamePlural(true);

        String formatedWorkingDate = prefs.getFormattedWorkingDate();
        GregorianCalendar workingDate = prefs.getWorkingDate();
        boolean canEditRecentGenerations = prefs.canEditRecentGeneration();
        boolean editWorkingMode = prefs.isWorkingModeEdit();

        init(cmpt, formatedWorkingDate, workingDate, generationConceptNameSingular,
                generationConceptNameSingularInsideSentence, generationConceptNamePlural,
                generationConceptNamePluralInsideSentence, canEditRecentGenerations, editWorkingMode);
    }

    public void init(IProductCmpt cmpt,
            String formatedWorkingDate,
            GregorianCalendar workingDate,
            String generationConceptNameSingular,
            String generationConceptNameSingularInsideSentence,
            String generationConceptNamePlural,
            String generationConceptNamePluralInsideSentence,
            boolean canEditRecentGenerations,
            boolean editWorkingMode) {

        this.cmpt = cmpt;
        this.formatedWorkingDate = formatedWorkingDate;
        this.workingDate = workingDate;
        this.generationConceptNameSingular = generationConceptNameSingular;
        this.generationConceptNamePlural = generationConceptNamePlural;
        this.generationConceptNameSingularInsideSentence = generationConceptNameSingularInsideSentence;
        this.generationConceptNamePluralInsideSentence = generationConceptNamePluralInsideSentence;
        this.canEditRecentGenerations = canEditRecentGenerations;
        this.editWorkingMode = editWorkingMode;

        setShellStyle(getShellStyle() | SWT.RESIZE);
        setHelpAvailable(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite workArea = (Composite)super.createDialogArea(parent);
        workArea.setLayout(new GridLayout(1, false));

        // init title
        setTitle(Messages.GenerationSelectionDialog_title);

        String winTitle = NLS.bind(Messages.ProductCmptEditor_title_GenerationMissmatch, cmpt.getName(),
                generationConceptNameSingular);
        getShell().setText(winTitle);

        // create choice buttons
        Composite selectPane = new Composite(workArea, SWT.None);
        selectPane.setLayout(new GridLayout(3, false));

        Label label = toolkit.createLabel(selectPane, NLS.bind(Messages.GenerationSelectionDialog_description,
                formatedWorkingDate, generationConceptNameSingularInsideSentence));
        GridData gd = (GridData)label.getLayoutData();
        gd.horizontalSpan = 3;

        createChoiceControls(selectPane);

        getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                validate();
            }
        });

        bindingContext.updateUI();

        return workArea;
    }

    public void createChoiceControls(Composite selectPane) {
        createChoiceForShowGenerationReadOnly(selectPane);
        createChoiceForSwitchWorkingDate(selectPane);
        createChoiceForCreateNewGeneration(selectPane);

        createHoricontalSpace(selectPane);

        Label label = toolkit.createHorizonzalLine(selectPane);
        GridData ld = (GridData)label.getLayoutData();
        ld.horizontalSpan = 3;
        createCheckBoxEditRecentGenerations(selectPane);

        createHoricontalSpace(selectPane);

        initSelectionFromPreferences();
    }

    private void createCheckBoxEditRecentGenerations(Composite selectPane) {
        Composite composite = toolkit.createComposite(selectPane);
        GridLayout gl = toolkit.createNoMarginGridLayout(1, true);
        composite.setLayout(gl);
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        composite.setLayoutData(gd);

        String labelText = NLS.bind(Messages.GenerationSelectionDialog_checkboxCanEditRecentGenerations,
                generationConceptNamePluralInsideSentence);
        Checkbox checkbox = toolkit.createCheckbox(composite, labelText);

        bindingContext.bindContent(checkbox, new WorkingDatePmo(), WorkingDatePmo.CAN_EDIT_RECENT_GENERATION);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
    }

    private void createChoiceForSwitchWorkingDate(Composite selectPane) {
        List<String> relevantGenerations = getGenerationsDropDownContent();

        Button switchButton = new Button(selectPane, SWT.RADIO);
        allButtons.put(new Integer(CHOICE_SWITCH), switchButton);
        choices.put(switchButton, new Integer(CHOICE_SWITCH));

        Label l3 = new Label(selectPane, SWT.NONE);
        l3.setText(NLS.bind(Messages.GenerationSelectionDialog_labelSwitch, generationConceptNameSingular));
        l3.addMouseListener(new ActivateButtonOnClickListener(switchButton));

        createGenerationsDropDown(selectPane, switchButton, relevantGenerations, false);
    }

    public List<String> getGenerationsDropDownContent() {
        IIpsObjectGeneration[] generations = cmpt.getGenerationsOrderedByValidDate();
        List<String> relevantGenerations = new ArrayList<String>(generations.length);
        for (IIpsObjectGeneration generation : generations) {
            String valueToAdd = generation.getName();
            relevantGenerations.add(valueToAdd);
        }
        return relevantGenerations;
    }

    private void createGenerationsDropDown(Composite switchPane,
            Button button,
            List<String> generations,
            boolean forReadOnlyCombo) {

        Combo validFromDates = new Combo(switchPane, SWT.DROP_DOWN | SWT.READ_ONLY);
        SelectionListenerForCombo listenerForCombo = new SelectionListenerForCombo(validFromDates, button);
        validFromDates.addSelectionListener(listenerForCombo);
        validFromDates.addFocusListener(listenerForCombo);
        for (String string : generations) {
            validFromDates.add(string);
        }

        validFromDates.select(forReadOnlyCombo ? 0 : generations.size() - 1);
        for (Button btn : allButtons.values()) {
            btn.addSelectionListener(new SelectionListenerForButton(validFromDates));
        }

        if (!forReadOnlyCombo) {
            validFromDates.select(generations.size());
        } else {
            IIpsObjectGeneration generation = cmpt.findGenerationEffectiveOn(workingDate);
            int idx = 0;
            if (generation != null) {
                for (String generationName : generations) {
                    if (generationName.equals(generation.getName())) {
                        break;
                    }
                    idx++;
                }
            }
            if (idx >= generations.size()) {
                idx = 0;
            }
            validFromDates.select(idx);
        }
    }

    private void createChoiceForShowGenerationReadOnly(Composite selectPane) {
        List<String> relevantGenerations = getGenerationsDropDownContent();

        // necessary to get the same space between create- and browse-line
        // and browse- and switch-line.
        createHoricontalSpace(selectPane);

        Button browseButton = new Button(selectPane, SWT.RADIO);
        allButtons.put(new Integer(CHOICE_BROWSE), browseButton);
        choices.put(browseButton, new Integer(CHOICE_BROWSE));

        Label l2 = new Label(selectPane, SWT.NONE);
        l2.setText(NLS.bind(Messages.GenerationSelectionDialog_labelShowReadOnlyGeneration,
                generationConceptNameSingularInsideSentence));
        l2.addMouseListener(new ActivateButtonOnClickListener(browseButton));

        createGenerationsDropDown(selectPane, browseButton, relevantGenerations, true);
    }

    private void createHoricontalSpace(Composite selectPane) {
        new Composite(selectPane, SWT.NONE).setLayoutData(new GridData(1, 3));
        new Composite(selectPane, SWT.NONE).setLayoutData(new GridData(1, 3));
        new Composite(selectPane, SWT.NONE).setLayoutData(new GridData(1, 3));
    }

    private void createChoiceForCreateNewGeneration(Composite selectPane) {
        GregorianCalendar now = getToday();

        createHoricontalSpace(selectPane);

        Button createButton = new Button(selectPane, SWT.RADIO);
        allButtons.put(new Integer(CHOICE_CREATE), createButton);
        choices.put(createButton, new Integer(CHOICE_CREATE));
        Label l1 = new Label(selectPane, SWT.NONE);
        l1.setText(NLS.bind(Messages.GenerationSelectionDialog_labelCreate,
                generationConceptNameSingularInsideSentence, formatedWorkingDate));

        // if control is enabled add mouse listener
        l1.addMouseListener(new ActivateButtonOnClickListener(createButton));

        // fill grid composite
        new Composite(selectPane, SWT.NONE).setLayoutData(new GridData(1, 3));

        // if one genearations valid to date is equal the working date then the new button should
        // not displayed
        IIpsObjectGeneration[] generations = cmpt.getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            if (workingDate.equals(generation.getValidFrom())) {
                return;
            }
        }

        if (!IpsUIPlugin.isEditable(cmpt.getIpsSrcFile())) {
            return;
        }
        if (!canEditRecentGenerations && workingDate.before(now)) {
            return;
        }
        if (!canEditRecentGenerations && cmpt.getValidTo() != null && cmpt.getValidTo().after(workingDate)) {
            return;
        }
    }

    private GregorianCalendar getToday() {
        return DateUtil.parseIsoDateStringToGregorianCalendar(DateUtil
                .gregorianCalendarToIsoDateString(new GregorianCalendar()));
    }

    private void initSelectionFromPreferences() {
        choice = IpsPlugin.getDefault().getPreferenceStore().getInt(STORED_CHOICE_ID);
        for (Integer key : allButtons.keySet()) {
            Button btn = allButtons.get(key);
            if (key.intValue() == choice) {
                btn.setSelection(true);
            } else {
                btn.setSelection(false);
            }
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == Window.OK) {
            IpsPlugin.getDefault().getPluginPreferences().setValue(STORED_CHOICE_ID, choice);
        }
        super.buttonPressed(buttonId);
    }

    /**
     * Returns the choice of the user. The returned value is one of the CHOICE-constants of this
     * class.
     */
    public int getChoice() {
        return choice;
    }

    /**
     * Returns the currently selected generation or null, if no generation was selected.
     */
    public IProductCmptGeneration getSelectedGeneration() {
        if (generationIndex == -1) {
            return null;
        }
        return (IProductCmptGeneration)cmpt.getGenerationsOrderedByValidDate()[generationIndex];
    }

    private void updateCurrentChoice(Button choosen) {
        choice = (choices.get(choosen)).intValue();
        validate();
    }

    private class SelectionListenerForButton implements SelectionListener {
        private Combo data;

        public SelectionListenerForButton(Combo data) {
            this.data = data;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            // get the current selected button, without using the event
            // because maybe the event doesn't match the visible selection
            updateCurrentChoice(getCurrentSelectedButton());
            generationIndex = data.getSelectionIndex();
            validate();
        }

        /**
         * Returns the current selected radio button
         */
        private Button getCurrentSelectedButton() {
            for (int i = 0; i < 3; i++) {
                Button button = allButtons.get(new Integer(i));
                if (button.getSelection()) {
                    return button;
                }
            }
            throw new RuntimeException("Radiobutton has no selection."); //$NON-NLS-1$
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }

    }

    private class SelectionListenerForCombo implements SelectionListener, FocusListener {

        private Combo combo;
        private Button button;

        public SelectionListenerForCombo(Combo combo, Button button) {
            this.combo = combo;
            this.button = button;
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            handleButtonStates();
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            handleButtonStates();
        }

        @Override
        public void focusGained(FocusEvent e) {
            handleButtonStates();
        }

        @Override
        public void focusLost(FocusEvent e) {
            // Nothing to do
        }

        private void handleButtonStates() {
            for (Button btn : allButtons.values()) {
                if (btn != button) {
                    btn.setSelection(false);
                } else {
                    btn.setSelection(true);
                }
            }
            generationIndex = combo.getSelectionIndex();
            updateCurrentChoice(button);
        }
    }

    private class ActivateButtonOnClickListener implements MouseListener {
        private Button toSelect;

        public ActivateButtonOnClickListener(Button toSelect) {
            this.toSelect = toSelect;
        }

        @Override
        public void mouseDoubleClick(MouseEvent e) {
            // nothing to do
        }

        @Override
        public void mouseDown(MouseEvent e) {
            // nothing to do
        }

        @Override
        public void mouseUp(MouseEvent e) {
            for (Button btn : allButtons.values()) {
                btn.setSelection(false);
            }

            toSelect.setSelection(true);
            updateCurrentChoice(toSelect);
            validate();
        }
    }

    private boolean cannotEditRecentGeneration() {
        IProductCmptGeneration selectedGeneration = getSelectedGeneration();
        return !canEditRecentGenerations && new GregorianCalendar().after(selectedGeneration.getValidFrom());
    }

    /**
     * @return Returns the canEditRecentGenerations.
     */
    public boolean isCanEditRecentGenerations() {
        return canEditRecentGenerations;
    }

    private void validate() {
        if (getShell() == null || getShell().isDisposed()) {
            return;
        }
        String message = null;
        int messageType = IMessageProvider.NONE;

        if (!editWorkingMode) {
            message = ""; //$NON-NLS-1$
            messageType = IMessageProvider.INFORMATION;
        }

        if ((choice == CHOICE_SWITCH || choice == CHOICE_CREATE) && !editWorkingMode) {
            message = Messages.GenerationSelectionDialog_infoNoChangesInCurrentWorkingMode;
            messageType = choice == CHOICE_SWITCH ? IMessageProvider.WARNING : IMessageProvider.ERROR;
        }

        if (messageType == IMessageProvider.NONE && choice == CHOICE_SWITCH) {
            if (cannotEditRecentGeneration()) {
                if (editWorkingMode) {
                    message = NLS.bind(Messages.GenerationSelectionDialog_infoGenerationsCouldntChangeInfo,
                            generationConceptNamePlural);
                    messageType = IMessageProvider.WARNING;
                }
            }
        } else if (messageType == IMessageProvider.NONE && choice == CHOICE_CREATE) {
            if (!canEditRecentGenerations && new GregorianCalendar().after(workingDate)) {
                message = NLS.bind(Messages.GenerationSelectionDialog_infoGenerationsCouldntChange,
                        generationConceptNamePlural);
                messageType = IMessageProvider.ERROR;
            }
        }

        setMessage(null, IMessageProvider.WARNING);
        setErrorMessage(null);
        if (messageType == IMessageProvider.WARNING) {
            setMessage(message, messageType);
        } else if (messageType == IMessageProvider.ERROR) {
            setErrorMessage(message);
        }

        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null && !button.isDisposed()) {
            button.setEnabled(messageType != IMessageProvider.ERROR);
        }
    }

    public List<Integer> getAllButtons() {
        List<Integer> createdChoices = new ArrayList<Integer>(4);
        createdChoices.addAll(allButtons.keySet());
        return createdChoices;
    }
}
