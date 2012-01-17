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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.contentproposal.ContentProposalLabelProvider;
import org.faktorips.devtools.core.ui.controls.contentproposal.IpsSrcFileContentProposalProvider;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.SingleTypeSelectIpsObjectContext;
import org.faktorips.devtools.core.ui.wizards.tablecontents.AddNewTableContentHandler;
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

        try {
            filter = new IpsSrcFileFilter(tableUsage.findTableStructureUsage(project));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        proposalProvider.setFilter(filter);
        toolkit.attachContentProposalAdapter(getTextControl(), proposalProvider, new ContentProposalLabelProvider());
    }

    @Override
    protected Control createSecondControl(UIToolkit toolkit) {
        buttonComposite = toolkit.createGridComposite(this, 2, false, false);
        browseButton = toolkit.createButton(buttonComposite, Messages.IpsObjectRefControl_title);
        newButton = toolkit.createButton(buttonComposite, Messages.TableContentsUsageRefControl_button_new);
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

    @Override
    public void dispose() {
        super.dispose();
        proposalProvider.dispose();
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
        AddNewTableContentHandler addNewTableContentHandler = new AddNewTableContentHandler();
        addNewTableContentHandler.initWizard(tableUsage, getShell(), false);
    }

    private static class IpsSrcFileFilter extends ViewerFilter implements IFilter {

        private String[] tableStructures;

        public IpsSrcFileFilter(ITableStructureUsage usage) {
            tableStructures = usage.getTableStructures();
        }

        @Override
        public boolean select(Object object) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)object;
            for (String structure : tableStructures) {
                String tableStructure;
                try {
                    tableStructure = ipsSrcFile.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
                    if (tableStructure != null && tableStructure.equals(structure)) {
                        return true;
                    }
                } catch (CoreException e) {
                    return false;
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
