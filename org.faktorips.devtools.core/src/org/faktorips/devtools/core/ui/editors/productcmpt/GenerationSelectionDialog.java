/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.values.DateUtil;

/**
 * Dialog with several radio buttons to show a particular generation in read-only modus, switch to a
 * new working date, or create a new generation depending on:
 * <ul>
 * <li> the given product cmpt generations
 * <li> working date
 * <li> option: canEditRecentGenerations and editWorkingMode
 * </ul>
 * 
 * @author Thorsten Guenther
 */
public class GenerationSelectionDialog extends TitleAreaDialog {
    private static final String STORED_CHOICE_ID = IpsPlugin.PLUGIN_ID + ".generationSelectionDialogChoice"; //$NON-NLS-1$
    
	private IProductCmpt cmpt;
    
    /**
     * Cache the choice (view, set working date or create new generation) because
     * if the choice is needed, this dialog is already disposed and the information 
     * about the choice can not be requested from the buttons, because these buttons 
     * are disposed, too.
     */
	private int choice;
    
    /**
     * Cache the selected generation (see above, <code>choice</code>).
     */
	private int generationIndex;
    
	
	private Map allButtons = new HashMap(3);
	private Hashtable choices = new Hashtable(3);
    /* User's choice was to create a new generation */
	public static final int CHOICE_CREATE = 0;
    /* User's choice was to browse the generation effective at the current effective date. */
	public static final int CHOICE_BROWSE = 1;
    /* User's choice was to switch the effective date to the effective from of one of the generations. */
	public static final int CHOICE_SWITCH = 2;

	private String formatedWorkingDate;
	private GregorianCalendar workingDate;
    private boolean canEditRecentGenerations;
    private boolean editWorkingMode;

    private String generationConceptNameSingular;
    private String generationConceptNamePlural;
    
	/**
	 * @param parentShell
	 * @param generationConceptNamePlural 
	 */
	public GenerationSelectionDialog(Shell parentShell, IProductCmpt cmpt, String formatedWorkingDate, GregorianCalendar workingDate,
            String generationConceptNameSingular, String generationConceptNamePlural, boolean canEditRecentGenerations, boolean editWorkingMode) {
        super(parentShell);
        this.cmpt = cmpt;

        this.formatedWorkingDate = formatedWorkingDate;
        this.workingDate = workingDate;
        this.generationConceptNameSingular = generationConceptNameSingular;
        this.generationConceptNamePlural = generationConceptNamePlural;
        this.canEditRecentGenerations = canEditRecentGenerations;
        this.editWorkingMode = editWorkingMode;

        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

	/**
	 * {@inheritDoc}
	 */
	protected Control createDialogArea(Composite parent) {
		Composite workArea = (Composite) super.createDialogArea(parent);
        workArea.setLayout(new GridLayout(1, false));
        
        // init title
		setTitle (Messages.GenerationSelectionDialog_title);
        
        String winTitle = NLS.bind(Messages.ProductCmptEditor_title_GenerationMissmatch, cmpt.getName(),
                generationConceptNameSingular);
        getShell().setText(winTitle);

        setMessage(NLS.bind(Messages.GenerationSelectionDialog_description, formatedWorkingDate, generationConceptNameSingular));
        
        // create choise buttons
        Composite selectPane = new Composite(workArea, SWT.None);
        selectPane.setLayout(new GridLayout(3, false));
        
        createChoiseControls(selectPane);
		
		return workArea;
	}

    public void createChoiseControls(Composite selectPane) {
        createChoiseForShowGenerationReadOnly(selectPane);
        createChoiseForSwitchWorkingDate(selectPane);
        createChoiseForCreateNewGeneration(selectPane);
        createInfoMessageNoRecentCange(selectPane);
        
        initSelectionFromPreferences();
    }

    private void createInfoMessageNoRecentCange(Composite selectPane) {
        createHoricontalSpace(selectPane);
        
        Composite composite = new Composite(selectPane, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        composite.setLayoutData(gd);
        composite.setLayout(new GridLayout(1, true));
        
		if (! canEditRecentGenerations){
		    String message = NLS.bind(Messages.GenerationSelectionDialog_infoGenerationsCouldntChange, generationConceptNamePlural);
            Label descrition = new Label(composite, SWT.NONE);
		    descrition.setText(message);
		}
    }

    /**
     * {@inheritDoc}
     */
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }
    
    private void createChoiseForSwitchWorkingDate(Composite selectPane) {
        List relevantGenerations = getRelevantGenerations(false);
        if (relevantGenerations.size() == 0){
            return;
        }
        
        Button switchButton = new Button(selectPane, SWT.RADIO);
        allButtons.put(new Integer(CHOICE_SWITCH), switchButton);
        choices.put(switchButton, new Integer(CHOICE_SWITCH));
        
		Label l3 = new Label(selectPane, SWT.NONE);
		l3.setText(NLS.bind(Messages.GenerationSelectionDialog_labelSwitch, generationConceptNameSingular));
		l3.addMouseListener(new ActivateButtonOnClickListener(switchButton));
		
        createGenerationsDropDown(selectPane, switchButton, relevantGenerations, false);
    }
    
    public List getRelevantGenerations(boolean forReadOnlyCombo){
        IIpsObjectGeneration[] generations = cmpt.getGenerations();
        List relevantGenerations = new ArrayList(generations.length);
        for (int i = 0; i < generations.length; i++) {
            if (forReadOnlyCombo){
                if (editWorkingMode && workingDate.equals(generations[i].getValidFrom())){
                    // skip because: generation could be edit because working date is equal generations vaild from date
                    // will be added as option in the switch working date to drop down
                    continue;
                }
            } else {
                if (! editWorkingMode || 
                        (! canEditRecentGenerations && new GregorianCalendar().after(generations[i].getValidFrom()))){
                    // skip because: working date is before generations valid from date and recent generations couldn't be changed
                    // will be added as option in the shwow generation read only drop down
                    continue;
                }
            }
            relevantGenerations.add(generations[i].getName());
        }
        return relevantGenerations;
    }

    private void createGenerationsDropDown(Composite switchPane, Button button, List generations, boolean forReadOnlyCombo) {
        Combo validFromDates = new Combo(switchPane, SWT.DROP_DOWN | SWT.READ_ONLY);
        SelectionListenerForCombo listenerForCombo = new SelectionListenerForCombo(validFromDates, button);
        validFromDates.addSelectionListener(listenerForCombo);
        validFromDates.addFocusListener(listenerForCombo);
        for (Iterator iter = generations.iterator(); iter.hasNext();) {
            validFromDates.add((String)iter.next());
        }
        
        validFromDates.select(forReadOnlyCombo?0:generations.size()-1);
        for (Iterator iter = allButtons.values().iterator(); iter.hasNext();) {
            Button btn = (Button)iter.next();
            btn.addSelectionListener(new SelectionListenerForButton(validFromDates));
        }
        
        if (!forReadOnlyCombo){
            validFromDates.select(generations.size());
        } else {
            IIpsObjectGeneration generation = cmpt.findGenerationEffectiveOn(workingDate);
            int idx = 0;
            if (generation != null){
                for (Iterator iter = generations.iterator(); iter.hasNext();) {
                    String generationName = (String)iter.next();
                    if (generationName.equals(generation.getName())){
                        break;
                    }
                    idx++;
                }
            }
            if (idx >= generations.size()){
                idx =0;
            }
            validFromDates.select(idx);
        }
    }
    
    private void createChoiseForShowGenerationReadOnly(Composite selectPane) {
        List relevantGenerations = getRelevantGenerations(true);
        if (relevantGenerations.size() == 0){
            return;
        }

        // neccessary to get the same space between create- and browse-line
        // and browse- and switch-line.
        createHoricontalSpace(selectPane);

        Button browseButton = new Button(selectPane, SWT.RADIO);
        allButtons.put(new Integer(CHOICE_BROWSE), browseButton);
        choices.put(browseButton, new Integer(CHOICE_BROWSE));
        
        Label l2 = new Label(selectPane, SWT.NONE);
        l2.setText(NLS.bind(Messages.GenerationSelectionDialog_labelShowReadOnlyGeneration, generationConceptNameSingular));
        l2.addMouseListener(new ActivateButtonOnClickListener(browseButton));

        createGenerationsDropDown(selectPane, browseButton, relevantGenerations, true);
    }

    private void createHoricontalSpace(Composite selectPane) {
        new Composite(selectPane, SWT.NONE).setLayoutData(new GridData(1, 3));
        new Composite(selectPane, SWT.NONE).setLayoutData(new GridData(1, 3));
        new Composite(selectPane, SWT.NONE).setLayoutData(new GridData(1, 3));
    }
    
    private void createChoiseForCreateNewGeneration(Composite selectPane) {
        GregorianCalendar now = getToday();
        
        createHoricontalSpace(selectPane);
        
        if (!cmpt.getIpsSrcFile().isMutable()) {
            return;
        }
        if (!canEditRecentGenerations && workingDate.before(now)) {
            return;
        }
        if (!canEditRecentGenerations && cmpt.getValidTo() != null && cmpt.getValidTo().after(workingDate)) {
            return;
        }
        
        // if one genearations valid to date is equal the working date then the new button should not displayed 
        IIpsObjectGeneration[] generations = cmpt.getGenerations();
        for (int i = 0; i < generations.length; i++) {
            if (workingDate.equals(generations[i].getValidFrom())){
                return;
            }
        }

        Button createButton = new Button(selectPane, SWT.RADIO);
        allButtons.put(new Integer(CHOICE_CREATE), createButton);
        choices.put(createButton, new Integer(CHOICE_CREATE));
        Label l1 = new Label(selectPane, SWT.NONE);
        l1.setText(NLS.bind(Messages.GenerationSelectionDialog_labelCreate, generationConceptNameSingular, formatedWorkingDate));
        l1.addMouseListener(new ActivateButtonOnClickListener(createButton));
        
        // fill grid composite
        new Composite(selectPane, SWT.NONE).setLayoutData(new GridData(1, 3));
    }

    private GregorianCalendar getToday() {
        return DateUtil.parseIsoDateStringToGregorianCalendar(DateUtil.gregorianCalendarToIsoDateString(new GregorianCalendar()));
    }

	private void initSelectionFromPreferences() {
		choice = IpsPlugin.getDefault().getPreferenceStore().getInt(STORED_CHOICE_ID);
        for (Iterator iter = allButtons.keySet().iterator(); iter.hasNext();) {
            Integer key = (Integer)iter.next();
            Button btn = (Button) allButtons.get(key);
            if (key.intValue()== choice){
                btn.setSelection(true);
            } else {
                btn.setSelection(false);
            }
        }
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == TitleAreaDialog.OK) {
			IpsPlugin.getDefault().getPluginPreferences().setValue(STORED_CHOICE_ID, choice);
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Returns the choice of the user. The returned value is one of the CHOICE-constants 
	 * of this class.
	 */
	public int getChoice() {
		return choice;
	}

	/**
	 * Returns the currently selected generation or null, if no generation
	 * was selected.
	 */
	public IProductCmptGeneration getSelectedGeneration() {
        if (generationIndex == -1) {
			return null;
		}
		return (IProductCmptGeneration)cmpt.getGenerations()[generationIndex];
	}

	private void updateCurrentChoice(Button choosen) {
		choice = ((Integer)choices.get(choosen)).intValue();
	}
	
	private class SelectionListenerForButton implements SelectionListener {
		private Combo data;
		
		public SelectionListenerForButton(Combo data) {
			this.data = data;
		}
		
		public void widgetSelected(SelectionEvent e) {
			updateCurrentChoice((Button)e.widget);
			GenerationSelectionDialog.this.generationIndex = data.getSelectionIndex();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
		
	}
	
    private class SelectionListenerForCombo implements SelectionListener, FocusListener{
        private Combo combo;
        private Button button;
        
        public SelectionListenerForCombo(Combo combo, Button button){
            this.combo = combo;
            this.button = button;
        }
        
        public void widgetDefaultSelected(SelectionEvent e) {
            handleButtonStates();
        }
    
        public void widgetSelected(SelectionEvent e) {
            handleButtonStates();
        }

        public void focusGained(FocusEvent e) {
            handleButtonStates();
        }

        public void focusLost(FocusEvent e) {
        }

        private void handleButtonStates() {
            for (Iterator iter = allButtons.values().iterator(); iter.hasNext();) {
                Button btn = (Button)iter.next();
                if (btn != button){
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
		
		public void mouseDoubleClick(MouseEvent e) {
			// nothing to do
		}

		public void mouseDown(MouseEvent e) {
			// nothing to do
		}

		public void mouseUp(MouseEvent e) {
            for (Iterator iter = allButtons.values().iterator(); iter.hasNext();) {
                Button btn = (Button)iter.next();
                btn.setSelection(false);
            }
			
            toSelect.setSelection(true);
			updateCurrentChoice(toSelect);
		}
		
	}
	
	// test methods
    
    public List getAllButtons(){
        List createdChoises = new ArrayList(4);
        createdChoises.addAll(allButtons.keySet());
        return createdChoises;
    }
}
