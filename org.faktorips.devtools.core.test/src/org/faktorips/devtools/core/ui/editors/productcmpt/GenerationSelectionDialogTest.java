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

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.values.DateUtil;

public class GenerationSelectionDialogTest extends AbstractIpsPluginTest {
    private IIpsProject project;
    private IProductCmpt productCmpt;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = super.newIpsProject("TestProject");
        productCmpt = newProductCmpt(project, "TestProduct");
    }

    public void testCreatedChoiseBtns() throws CoreException{
        GenerationSelectionDialog dialog = null;
        IIpsObjectGeneration[] generations = new IIpsObjectGeneration[10];
        
        // ProductCmpt has no generations => no choise available
        assertContainsChoiseButtons(createDialogWithCanChangeRecentGenerations("2200-01-01"), false, false, true);
        
        // a) recent generations could be changed
        // b) ProductCmpt has one generations in past (2001-01-01)
        // c) working date is in future
        // -> show generation with valid from [2001-01-01]
        // -> switch to working date [2001-01-01]
        // -> create new generation
        generations[0] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2001-01-01"));
        dialog = createDialogWithCanChangeRecentGenerations("2200-01-01");
        assertContainsChoiseButtons(dialog, true, true, true);
        assertContentOfDropDownSwitchWorkingDate(dialog, new String[]{generations[0].getName()});
        assertContentOfDropDownShowGenerationValidFrom(dialog, new String[]{generations[0].getName()});
        deleteGenerations(generations);
        
        // a) recent generations could be changed
        // b) ProductCmpt has two generations in past (2001-01-01, 2002-02-02)
        // c) working date is in future
        // -> show generation with valid from [2001-01-01, 2002-02-02]
        // -> switch to working date [2001-01-01, 2002-02-02]
        // -> create new generation
        generations[0] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2001-01-01"));
        generations[1] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2002-02-02"));
        dialog = createDialogWithCanChangeRecentGenerations("2200-01-01");
        assertContainsChoiseButtons(dialog, true, true, true);
        assertContentOfDropDownShowGenerationValidFrom(dialog, new String[]{generations[0].getName(), generations[1].getName()}); 
        assertContentOfDropDownSwitchWorkingDate(dialog, new String[]{generations[0].getName(), generations[1].getName()});
        deleteGenerations(generations);
        
        // a) recent generations could be changed
        // b) ProductCmpt has two generations in past (2001-01-01, 2002-02-02) and one in future (2200-12-01)
        // c) working date is in future
        // -> show generation with valid from [2001-01-01, 2002-02-02, 2200-12-01]
        // -> switch to working date [2001-01-01, 2002-02-02, 2200-12-01]
        // -> create new generation
        generations[0] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2001-01-01"));
        generations[1] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2002-02-02"));
        generations[2] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2200-12-01"));
        dialog = createDialogWithCanChangeRecentGenerations("2200-01-01");
        assertContainsChoiseButtons(dialog, true, true, true);
        assertContentOfDropDownShowGenerationValidFrom(dialog, new String[]{generations[0].getName(), generations[1].getName(), generations[2].getName()}); 
        assertContentOfDropDownSwitchWorkingDate(dialog, new String[]{generations[0].getName(), generations[1].getName(), generations[2].getName()});
        deleteGenerations(generations); 
        
        // a) recent generations could not be changed
        // b) ProductCmpt has two generations in past (2001-01-01, 2002-02-02) and one in future (2200-12-01)
        // c) working date is in future
        // -> show generation with valid from [2001-01-01, 2002-02-02, 2200-12-01]
        // -> switch to working date [2200-12-01]
        // -> create new generation
        generations[0] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2001-01-01"));
        generations[1] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2002-02-02"));
        generations[2] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2200-12-01"));
        dialog = createDialogWithNotChangeRecentGenerations("2200-01-01");
        assertContainsChoiseButtons(dialog, true, true, true);
        assertContentOfDropDownShowGenerationValidFrom(dialog, new String[]{generations[0].getName(), generations[1].getName(), generations[2].getName()}); 
        assertContentOfDropDownSwitchWorkingDate(dialog, new String[]{generations[2].getName()});
        deleteGenerations(generations);
        
        // a) recent generations could not be changed
        // b) ProductCmpt has two generations in past (2001-01-01, 2002-02-02) and one in future (2200-12-01)
        // c) working date is in past
        // -> show generation with valid from [2001-01-01, 2002-02-02, 2200-12-01]
        // -> switch to working date [2200-12-01]
        generations[0] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2001-01-01"));
        generations[1] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2002-02-02"));
        generations[2] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2200-12-01"));
        dialog = createDialogWithNotChangeRecentGenerations("2000-01-01");
        assertContainsChoiseButtons(dialog, true, true, false);
        assertContentOfDropDownShowGenerationValidFrom(dialog, new String[]{generations[0].getName(), generations[1].getName(), generations[2].getName()}); 
        assertContentOfDropDownSwitchWorkingDate(dialog, new String[]{generations[2].getName()});
        deleteGenerations(generations);
        
        // a) recent generations could not be changed
        // b) ProductCmpt has two generations in past (2001-01-01, 2002-02-02) and one in future (2200-12-01)
        // c) working date is the valid from date of the generation in future 2200-12-01
        // -> show generation with valid from [2001-01-01, 2002-02-02] (not 2200-12-01 because this generation will not be opend as read only)
        // -> switch to working date [2200-12-01]
        generations[0] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2001-01-01"));
        generations[1] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2002-02-02"));
        generations[2] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2200-12-01"));
        dialog = createDialogWithNotChangeRecentGenerations("2200-12-01");
        assertContainsChoiseButtons(dialog, true, true, false);
        assertContentOfDropDownShowGenerationValidFrom(dialog, new String[]{generations[0].getName(), generations[1].getName()}); 
        assertContentOfDropDownSwitchWorkingDate(dialog, new String[]{generations[2].getName()});
        deleteGenerations(generations);
        
        // a) recent generations could not be changed
        // b) ProductCmpt has one generation in past
        // c) working date is today
        // -> show generation with valid from ...
        // -> create new generation
        String today = DateUtil.gregorianCalendarToIsoDateString(new GregorianCalendar());
        generations[0] = productCmpt.newGeneration(DateUtil.parseIsoDateStringToGregorianCalendar("2001-01-01"));
        dialog = createDialogWithNotChangeRecentGenerations(today);
        assertContainsChoiseButtons(dialog, true, false, true);
        assertContentOfDropDownShowGenerationValidFrom(dialog, new String[]{generations[0].getName()}); 
        deleteGenerations(generations);
    }

    private void deleteGenerations(IIpsObjectGeneration[] generations) {
        for (int i = 0; i < generations.length; i++) {
            if (generations[i] != null){
                generations[i].delete();
                generations[i] = null;
            }
        }
    }

    private void assertContentOfDropDownShowGenerationValidFrom(GenerationSelectionDialog dialog, String[] generations) {
        assertEqualLists(Arrays.asList(generations), dialog.getRelevantGenerations(true));
    }

    private void assertEqualLists(List expectedDates, List relevantGenerations) {
        assertEquals("Size of generations in drop down", expectedDates.size(), relevantGenerations.size());
        for (Iterator iter = expectedDates.iterator(); iter.hasNext();) {
            String element = (String)iter.next();
            assertTrue("not in drop down list: " + element, relevantGenerations.contains(element));
        }
    }

    private void assertContentOfDropDownSwitchWorkingDate(GenerationSelectionDialog dialog, String[] dates) {
        assertEqualLists(Arrays.asList(dates), dialog.getRelevantGenerations(false));
    }

    private void assertContainsChoiseButtons(GenerationSelectionDialog gsDialog,
            boolean browse,
            boolean switchWorkingDate,
            boolean newGeneration) {
        List createdButtons = gsDialog.getAllButtons();
        assertEquals((browse ? "must" : "must not") + " contain browse button", browse, createdButtons.contains(new Integer(GenerationSelectionDialog.CHOICE_BROWSE)));
        assertEquals((switchWorkingDate ? "must" : "must not") + " contain switch button", switchWorkingDate, createdButtons.contains(new Integer(GenerationSelectionDialog.CHOICE_SWITCH)));
        assertEquals((newGeneration ? "must" : "must not") + " contain create button", newGeneration, createdButtons.contains(new Integer(GenerationSelectionDialog.CHOICE_CREATE)));
    }
    
    private GenerationSelectionDialog createDialogWithCanChangeRecentGenerations(String workingDate){
        return createDialog(workingDate, true);
    }
    
    private GenerationSelectionDialog createDialogWithNotChangeRecentGenerations(String workingDate){
        return createDialog(workingDate, false);
    }
    
    private GenerationSelectionDialog createDialog(String workingDate, boolean canEditRecentGenerations){
        GenerationSelectionDialog dialog = new GenerationSelectionDialog(null, productCmpt, 
                workingDate, getWorkingDateAsCalender(workingDate), "Generation", "Generations", canEditRecentGenerations, true);
        dialog.createChoiseControls(Display.getDefault().getActiveShell());
        return dialog;
    }
    private GregorianCalendar getWorkingDateAsCalender(String workingDate){
        return DateUtil.parseIsoDateStringToGregorianCalendar(workingDate);
    }
}