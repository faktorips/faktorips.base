/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.contentproposal.IpsSrcFileContentProposalLabelProvider;
import org.faktorips.devtools.core.ui.controls.contentproposal.IpsSrcFileContentProposalProvider;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.SingleTypeSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.wizards.tablecontents.AddNewTableContentsHandler;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.util.StringUtil;

/**
 * Control enter a reference to a table contents. Candidates for the reference are defined by a
 * given table structure usage.
 * 
 * @see ITableStructureUsage
 * 
 * @author Thorsten Guenther
 */
public class TableContentsUsageRefControl extends TextAndSecondControlComposite {

    private ITableContentUsage tableUsage;
    private Button browseButton;
    private Button newButton;
    private Composite buttonComposite;
    private final IIpsProject project;
    private IpsSrcFileFilter filter;
    private IpsSrcFileContentProposalProvider proposalProvider;

    public TableContentsUsageRefControl(IIpsProject project, Composite parent, UIToolkit toolkit,
            ITableContentUsage tableUsage) {
        super(parent, toolkit, false, -1, SWT.NONE);
        this.project = project;
        this.tableUsage = tableUsage;

        proposalProvider = new IpsSrcFileContentProposalProvider(project, IpsObjectType.TABLE_CONTENTS);

        filter = new IpsSrcFileFilter(tableUsage.findTableStructureUsage(project));
        proposalProvider.setFilter(filter);
        toolkit.attachContentProposalAdapter(getTextControl(), proposalProvider,
                new IpsSrcFileContentProposalLabelProvider());
    }

    @Override
    protected Control createSecondControl(UIToolkit toolkit) {
        buttonComposite = toolkit.createGridComposite(this, 2, false, false);
        browseButton = toolkit.createButton(buttonComposite, StringUtils.EMPTY);
        browseButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("browse.gif", true)); //$NON-NLS-1$
        newButton = toolkit.createButton(buttonComposite, StringUtils.EMPTY);
        newButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("NewTableContentsWizard.gif", true)); //$NON-NLS-1$
        return buttonComposite;
    }

    @Override
    protected Composite getSecondControl() {
        return buttonComposite;
    }

    @Override
    protected void addListeners() {
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                browseClicked();
            }
        });
        newButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                newClicked();
            }
        });
    }

    private void browseClicked() {
        SingleTypeSelectIpsObjectContext context = new SingleTypeSelectIpsObjectContext(project,
                IpsObjectType.TABLE_CONTENTS, filter);
        final OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(getShell(),
                Messages.TableContentsRefControl_title, context);
        dialog.setMessage(Messages.TableContentsRefControl_description);
        try {
            dialog.setFilter(StringUtil.unqualifiedName(getText()));
            if (dialog.open() == Window.OK) {
                if (dialog.getResult().length > 0) {
                    Object[] result = dialog.getResult();
                    updateTextControlAfterDialogOK((IIpsSrcFile)result[0]);
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private void updateTextControlAfterDialogOK(IIpsSrcFile srcFile) {
        setText(srcFile.getQualifiedNameType().getName());
    }

    private void newClicked() {
        tableUsage.getIpsSrcFile().markAsDirty();
        AddNewTableContentsHandler addNewTableContentHandler = new AddNewTableContentsHandler();
        addNewTableContentHandler.openDialog(tableUsage, getShell(), false);
    }

    private static class IpsSrcFileFilter extends ViewerFilter implements IFilter {

        private String[] tableStructures;

        public IpsSrcFileFilter(ITableStructureUsage usage) {
            if (usage != null) {
                tableStructures = usage.getTableStructures();
            } else {
                tableStructures = new String[0];
            }
        }

        @Override
        public boolean select(Object object) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)object;
            for (String structure : tableStructures) {
                String tableStructure;
                tableStructure = ipsSrcFile.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
                if (tableStructure != null && tableStructure.equals(structure)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            return select(element);
        }
    }

}
