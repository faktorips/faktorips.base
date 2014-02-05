/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractIpsSrcFileContentProposalProvider;
import org.faktorips.devtools.core.ui.controls.contentproposal.IpsSrcFileContentProposalLabelProvider;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.StaticContentSelectIpsObjectContext;
import org.faktorips.util.StringUtil;

/**
 * Control to edit a reference to an ips source file in a text control with an associated browse
 * button that allows to browse the available objects.
 * <p>
 * The referenced {@link IIpsSrcFile ips source files} should be within the given
 * {@link IIpsProject ips projects}.
 * <p>
 * An older version of this class was based on using only one {@link IIpsProject}, but has been
 * refitted for several ips projects. Therefore some method are deprecated and replaced by new
 * methods.
 * 
 */
public abstract class IpsObjectRefControl extends TextButtonControl {

    private List<IIpsProject> ipsProjects;

    private String dialogTitle;
    private boolean enableDialogFilter = true;
    private String dialogMessage;

    private IContentProposalProvider proposalProvider;

    public IpsObjectRefControl(IIpsProject project, Composite parent, UIToolkit toolkit, String dialogTitle,
            String dialogMessage) {
        this(Arrays.asList(project), parent, toolkit, dialogTitle, dialogMessage);
    }

    public IpsObjectRefControl(List<IIpsProject> projects, Composite parent, UIToolkit toolkit, String dialogTitle,
            String dialogMessage) {
        super(parent, toolkit, Messages.IpsObjectRefControl_title);
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;

        proposalProvider = new AbstractIpsSrcFileContentProposalProvider() {

            @Override
            protected IIpsSrcFile[] getIpsSrcFiles() {
                try {
                    return IpsObjectRefControl.this.getIpsSrcFiles();
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
        };

        toolkit.attachContentProposalAdapter(getTextControl(), proposalProvider,
                new IpsSrcFileContentProposalLabelProvider());

        setIpsProjects(projects);
    }

    /**
     * 
     * @deprecated This method is deprecated because of the refitting of this class for several
     *             projects. Use {@link #setIpsProjects(List)} instead of this method.
     */
    @Deprecated
    public void setIpsProject(IIpsProject project) {
        setIpsProjects(Arrays.asList(project));
    }

    public void setIpsProjects(IIpsProject... projects) {
        setIpsProjects(Arrays.asList(projects));
    }

    public void setIpsProjects(List<IIpsProject> projects) {
        ipsProjects = new ArrayList<IIpsProject>();

        for (IIpsProject project : projects) {
            if (project != null && project.exists()) {
                ipsProjects.add(project);
            }
        }

        setButtonEnabled(!ipsProjects.isEmpty());
    }

    /**
     * If you want to use the {@link IIpsProject ips projects} to find the chosen {@link IIpsObject}
     * or the {@link IIpsSrcFile src files} of a type, consider calling
     * {@link #findIpsObject(IpsObjectType)} or {@link #findIpsSrcFilesByType(IpsObjectType)}.
     * 
     * @deprecated This method is deprecated because of the refitting of this class for several
     *             projects. This method is replaced by {@link #getIpsProjects()}.
     * 
     */
    @Deprecated
    public IIpsProject getIpsProject() {
        if (ipsProjects.isEmpty()) {
            return null;
        }
        return ipsProjects.get(0);
    }

    public List<IIpsProject> getIpsProjects() {
        return new CopyOnWriteArrayList<IIpsProject>(ipsProjects);
    }

    @Override
    protected void buttonClicked() {
        /*
         * using the StaticContentSelectIpsObjectContext is not the recommended way to use the
         * OpenIpsObjectSelecitonDialog. It is only used for older implementation If you have a
         * choice use your own implementation of SelectIpsObjectContext for better performance and
         * correct progress monitoring
         */
        final StaticContentSelectIpsObjectContext context = new StaticContentSelectIpsObjectContext();
        final OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(getShell(), dialogTitle, context);
        dialog.setMessage(dialogMessage);
        BusyIndicator.showWhile(getDisplay(), new Runnable() {
            @Override
            public void run() {
                try {
                    context.setElements(getIpsSrcFiles());
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });
        if (isDialogFilterEnabled()) {
            dialog.setFilter(getDefaultDialogFilterExpression());
        }
        if (dialog.open() == Window.OK) {
            if (dialog.getResult().length > 0) {
                List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();
                Object[] result = dialog.getResult();
                for (Object element : result) {
                    srcFiles.add((IIpsSrcFile)element);
                }
                updateTextControlAfterDialogOK(srcFiles);
            } else {
                setText(""); //$NON-NLS-1$
            }
        }
    }

    protected String getDefaultDialogFilterExpression() {
        return StringUtil.unqualifiedName(super.getText());
    }

    /**
     * Called when the user closes the dialog by clicking OK.
     * 
     * @param ipsSrcFiles List of selected ips source files containing at least 1 element!
     */
    protected void updateTextControlAfterDialogOK(List<IIpsSrcFile> ipsSrcFiles) {
        setText(ipsSrcFiles.get(0).getQualifiedNameType().getName());
    }

    public boolean isDialogFilterEnabled() {
        return enableDialogFilter;
    }

    public void setDialogFilterEnabled(boolean enable) {
        enableDialogFilter = enable;
    }

    /**
     * Returns all ips source files that can be chosen by the user.
     */
    protected abstract IIpsSrcFile[] getIpsSrcFiles() throws CoreException;

    /**
     * Returns an Array of {@link IIpsSrcFile}, that contains all {@link IIpsSrcFile source files}
     * of the given {@link IpsObjectType} within the {@link IIpsProject projects} of the ref
     * control.
     * <p>
     * This is a convenience method for subclasses to search the source files in all given projects.
     */
    protected final IIpsSrcFile[] findIpsSrcFilesByType(IpsObjectType type) throws CoreException {
        Set<IIpsSrcFile> srcFiles = new LinkedHashSet<IIpsSrcFile>();
        for (IIpsProject ipsProject : getIpsProjects()) {
            srcFiles.addAll(Arrays.asList(ipsProject.findIpsSrcFiles(type)));
        }
        return srcFiles.toArray(new IIpsSrcFile[srcFiles.size()]);
    }

    /**
     * Returns the {@link IIpsObject}, which is represented by the input in the text field. It will
     * be searched within all {@link IIpsProject projects} within this class. If no ips object is
     * found, <code>null</code> will be returned.
     * <p>
     * This is a convenience method for subclasses.
     */
    protected final IIpsObject findIpsObject(IpsObjectType type) throws CoreException {
        for (IIpsProject project : getIpsProjects()) {
            IIpsObject object = project.findIpsObject(type, getText());
            if (object != null) {
                return object;
            }
        }
        return null;
    }
}
