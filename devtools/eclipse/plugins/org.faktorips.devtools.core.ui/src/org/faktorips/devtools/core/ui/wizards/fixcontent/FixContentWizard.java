/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixcontent;

import java.util.List;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.tablestructure.IColumn;

/**
 * This wizard is available through the {@code ContentEditor} if the {@code  Content} to edit does
 * not refer to a valid {@code  ContentType}, if the number of referenced
 * {@code ContentAttribute ContentAttributes} that is stored in the {@code Content} does not
 * correspond to the number of {@code ContentAttribute ContentAttribute ContentAttributes} defined
 * in the referenced {@code ContentType} or if the ordering of the
 * {@code ContentAttributeValue ContentAttributeValues} needs to be changed because the ordering of
 * the referenced {@code ContentAttribute ContentAttribute ContentAttributes} has changed.
 * <p>
 * On the first page the wizard asks the user to select a valid {@code ContentType} to refer to. The
 * second page provides comfortable assignment of
 * {@code ContentAttribute ContentAttribute ContentAttributes} to existing
 * {@code ContentAttributeValue ContentAttributeValues}.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class FixContentWizard<T extends IIpsObject, E extends ILabeledElement> extends Wizard {

    /** The UI toolkit to create new UI elements with. */
    private UIToolkit uiToolkit;

    /** The wizard page to choose a new {@code ContentType}. */
    private ChooseContentTypePage<T, E> chooseContentTypePage;

    /**
     * The wizard page to assign {@link IEnumAttribute IEnumAttributes} or {@link IColumn IColumns}.
     */
    private AssignContentAttributesPage<T, E> assignContentAttributesPage;

    private FixValueTypePage convertContentTypePage;

    private TabularContentStrategy<T, E> contentStrategy;

    private DeltaFixWizardStrategy<T, E> wizardStrategy;

    /**
     * Creates a new {@link FixContentWizard}.
     * 
     * @param contentStrategy matching {@link TabularContentStrategy} depending on the content to
     *            fix.
     */
    public FixContentWizard(IIpsMetaObject content, TabularContentStrategy<T, E> contentStrategy) {
        uiToolkit = new UIToolkit(null);
        String title = NLS.bind(Messages.FixContentWizard_title, content.getUnqualifiedName());
        setWindowTitle(title);
        setNeedsProgressMonitor(false);
        this.contentStrategy = contentStrategy;
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(contentStrategy.getImage()));
        wizardStrategy = contentStrategy.createContentPageStrategy();
    }

    @Override
    public void addPages() {
        assignContentAttributesPage = new AssignContentAttributesPage<>(uiToolkit, wizardStrategy);
        chooseContentTypePage = new ChooseContentTypePage<>(uiToolkit, wizardStrategy,
                assignContentAttributesPage);
        convertContentTypePage = new FixValueTypePage(contentStrategy);

        addPage(chooseContentTypePage);
        addPage(assignContentAttributesPage);
        if (convertContentTypePage.isPageNecessary()) {
            addPage(convertContentTypePage);
        }
    }

    @Override
    public boolean performFinish() {
        // If not all columns have been assigned the user must confirm deletion.
        boolean confirmed = true;
        int numberNotAssignedColumns = assignContentAttributesPage.getCurrentlyNotAssignedColumns().size();
        if (numberNotAssignedColumns > 0) {
            String message = (numberNotAssignedColumns > 1)
                    ? NLS.bind(Messages.FixContentWizard_assignColumnsDeleteColumnsConfirmationMessagePlural,
                            numberNotAssignedColumns)
                    : NLS.bind(Messages.FixContentWizard_assignColumnsDeleteColumnsConfirmationMessageSingular,
                            numberNotAssignedColumns);
            confirmed = MessageDialog.openConfirm(getShell(),
                    Messages.FixContentWizard_assignColumnsDeleteColumnsConfirmationTitle, message);
        }

        if (confirmed) {
            ICoreRunnable workspaceRunnable = $ -> {
                deleteObsoleteContentAttributeValues();
                createNewContentAttributeValues();
                if (contentStrategy.getContentValuesCount() > 0) {
                    moveAttributeValues();
                }
                T contentType = wizardStrategy.findContentType(contentStrategy.getIpsProject(), null);
                contentStrategy.setContentType((contentType).getQualifiedName());
                contentStrategy.fixAllContentAttributeValues();
            };
            IIpsModel.get().runAndQueueChangeEvents(workspaceRunnable, null);
        }
        return confirmed;
    }

    /**
     * Deletes all existing{@code ContentAttributeValue ContentAttributeValues} that are no longer
     * needed according to the not assigned columns of the {@link AssignContentAttributesPage} from
     * every {@code ContentValue} of this {@code Content}.
     */
    private void deleteObsoleteContentAttributeValues() {
        contentStrategy.deleteObsoleteContentAttributeValues(assignContentAttributesPage);
    }

    /**
     * Creates new{@code ContentAttributeValue ContentAttributeValues} on every {@code ContentValue}
     * of the {@code Content} for every new column that has been added according to the column order
     * of the {@code AssignColumnsPage}.
     */
    private void createNewContentAttributeValues() {
        contentStrategy.createNewContentAttributeValues(assignContentAttributesPage);
    }

    /**
     * Each number in the column order array will be decremented by 1 for each number before that is
     * contained in the currently not assigned column numbers.
     * <p>
     * Example column order: 1, 3, 4, 7<br />
     * Not assigned columns: 2, 5, 6<br />
     * Decremented column order: 1, 2, 3, 4
     */
    private int[] computeDecrementedColumnOrder() {
        int[] columnOrder = assignContentAttributesPage.getColumnOrder();
        List<Integer> notAssignedColumns = assignContentAttributesPage.getCurrentlyNotAssignedColumns();

        for (int i = 0; i < columnOrder.length; i++) {
            int numberSmallerColumnsNotAssigned = 0;
            for (Integer currentNotAssignedColumn : notAssignedColumns) {
                int currentNotAssignedColumnNumber = currentNotAssignedColumn.intValue();
                if (currentNotAssignedColumnNumber < columnOrder[i]) {
                    numberSmallerColumnsNotAssigned++;
                } else if (currentNotAssignedColumnNumber > columnOrder[i]) {
                    break;
                }
            }
            columnOrder[i] = columnOrder[i] - numberSmallerColumnsNotAssigned;
        }

        return columnOrder;
    }

    /**
     * Moves{@code ContentAttributeValue ContentAttributeValues} in each {@code ContentValue}
     * according to the column order of the {@link AssignContentAttributesPage}.
     * <p>
     */
    private void moveAttributeValues() {
        /*
         * The column order as requested by the user and decremented where necessary (due to deleted
         * ContentAttributeValues).
         */
        int[] decrementedColumnOrder = computeDecrementedColumnOrder();

        int[] correctColumnOrdering = exchangeZerosInColumnWithIndex(decrementedColumnOrder);

        // Move the ContentAttributeValues of all ContentValues of the Content.
        contentStrategy.moveAttributeValues(correctColumnOrdering);
    }

    /**
     * Creates a new Array out of the two given Arrays.<br>
     * order before sorting: 1 2 0 0 0 <br>
     * order after sorting: 0 1 0 2 0 <br>
     * without zeros: 3 1 4 2 5
     * 
     */
    private int[] exchangeZerosInColumnWithIndex(int[] toBe) {
        int[] correctColumnOrdering = new int[toBe.length];

        int maxIndex = 0;
        for (int value : toBe) {
            if (value > maxIndex) {
                maxIndex = value;
            }
        }
        for (int i = 0; i < correctColumnOrdering.length; i++) {
            if (toBe[i] == 0) {
                correctColumnOrdering[i] = ++maxIndex;
            } else {
                correctColumnOrdering[i] = toBe[i];
            }
        }
        return correctColumnOrdering;
    }

}
