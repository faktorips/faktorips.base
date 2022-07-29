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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractIpsSrcFileContentProposalProvider;
import org.faktorips.devtools.core.ui.controls.contentproposal.IpsSrcFileContentProposalLabelProvider;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.core.ui.dialogs.StaticContentSelectIpsObjectContext;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.StringUtil;

/**
 * Control to edit a reference to an ips source file in a text control with an associated browse
 * button that allows to browse the available objects and an optional delete button if the reference
 * may be removed.
 * <p>
 * The referenced {@link IIpsSrcFile ips source files} should be within the given {@link IIpsProject
 * ips projects}.
 * <p>
 * An older version of this class was based on using only one {@link IIpsProject}, but has been
 * refitted for several ips projects. Therefore some method are deprecated and replaced by new
 * methods.
 * 
 */
public abstract class IpsObjectRefControl extends TextButtonControl {

    /**
     * The {@link IpsObjectType} of the currently selected object.
     */
    protected IpsObjectType objectType;

    private List<IIpsProject> ipsProjects;

    private String dialogTitle;
    private boolean enableDialogFilter = true;
    private String dialogMessage;

    private IContentProposalProvider proposalProvider;

    private Button deleteButton;

    public IpsObjectRefControl(IIpsProject project, Composite parent, UIToolkit toolkit, String dialogTitle,
            String dialogMessage) {
        this(Arrays.asList(project), parent, toolkit, dialogTitle, dialogMessage);
    }

    /**
     * Allows no empty ref.
     */
    public IpsObjectRefControl(List<IIpsProject> projects, Composite parent, UIToolkit toolkit, String dialogTitle,
            String dialogMessage) {
        this(projects, parent, toolkit, dialogTitle, dialogMessage, false);
    }

    /**
     * @param allowEmptyRef whether this control allows the object reference to be <code>null</code>
     *            , i.e. when there is no ref to another object. <code>true</code> to allow the ref
     *            to be set to <code>null</code> and display a delete button for that purpose,
     *            <code>false</code> to enforce a valid ref, hide the delete button (default).
     */
    public IpsObjectRefControl(List<IIpsProject> projects, Composite parent, UIToolkit toolkit, String dialogTitle,
            String dialogMessage, boolean allowEmptyRef) {
        super(parent, toolkit, Messages.IpsObjectRefControl_title);
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;

        proposalProvider = new AbstractIpsSrcFileContentProposalProvider() {

            @Override
            protected IIpsSrcFile[] getIpsSrcFiles() {
                return IpsObjectRefControl.this.getIpsSrcFiles();
            }
        };

        if (allowEmptyRef) {
            addDeleteButton();
        }

        UIToolkit.attachContentProposalAdapter(getTextControl(), proposalProvider,
                new IpsSrcFileContentProposalLabelProvider());

        setIpsProjects(projects);
    }

    /**
     * SW 23.11.2015 Introducing a listener hurts. We have to refactor all ips-object-ref fields to
     * use PMOs.
     */
    protected void addDeleteButton() {
        ((GridLayout)getLayout()).numColumns = 3;
        deleteButton = new Button(this, SWT.PUSH);
        deleteButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Delete_grey.png", true)); //$NON-NLS-1$
        deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setText(StringUtils.EMPTY);
            }
        });
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
        ipsProjects = new ArrayList<>();

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
        return new CopyOnWriteArrayList<>(ipsProjects);
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
        BusyIndicator.showWhile(getDisplay(), () -> {
            try {
                context.setElements(getIpsSrcFiles());
            } catch (IpsException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        });
        if (isDialogFilterEnabled()) {
            dialog.setFilter(getDefaultDialogFilterExpression());
        }
        if (dialog.open() == Window.OK) {
            if (dialog.getResult().length > 0) {
                List<IIpsSrcFile> srcFiles = new ArrayList<>();
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
        return StringUtil.unqualifiedName(getText());
    }

    /**
     * Called when the user closes the dialog by clicking OK.
     * 
     * @param ipsSrcFiles List of selected ips source files containing at least 1 element!
     */
    protected void updateTextControlAfterDialogOK(List<IIpsSrcFile> ipsSrcFiles) {
        if (ipsSrcFiles.isEmpty()) {
            updateSelection(null);
        } else {
            updateSelection(ipsSrcFiles.get(0).getQualifiedNameType());
        }
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
    protected abstract IIpsSrcFile[] getIpsSrcFiles() throws IpsException;

    /**
     * Returns an Array of {@link IIpsSrcFile}, that contains all {@link IIpsSrcFile source files}
     * of the given {@link IpsObjectType} within the {@link IIpsProject projects} of the ref
     * control.
     * <p>
     * This is a convenience method for subclasses to search the source files in all given projects.
     */
    protected final IIpsSrcFile[] findIpsSrcFilesByType(IpsObjectType type) {
        Set<IIpsSrcFile> srcFiles = new LinkedHashSet<>();
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
    protected final IIpsObject findIpsObject(IpsObjectType type) {
        for (IIpsProject project : getIpsProjects()) {
            IIpsObject object = project.findIpsObject(type, getText());
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    /**
     * Checks whether an object is clearly identifiable by the passed qualified name within the used
     * {@link #ipsProjects}.
     * 
     * @param qualifiedName The qualified name to be checked for uniqueness
     * @return True whether the qualified name is unique, else false
     * @throws IpsException If getting the required {@link IIpsSrcFile}s failed
     */
    public boolean checkIpsObjectUniqueness(String qualifiedName) {
        List<IIpsSrcFile> srcFiles = Arrays.asList(getIpsSrcFiles());
        if (srcFiles.isEmpty()) {
            return true;
        }
        long objectCount = srcFiles
                .stream()
                .filter(src -> src.getQualifiedNameType().getName().equals(qualifiedName))
                .count();
        return objectCount <= 1;

    }

    /**
     * Getter which provides the {@link #objectType} of the selected object.
     * 
     * @return The object type
     */
    public IpsObjectType getSelectedObjectType() {
        return objectType;
    }

    /**
     * Updates the user selection. This includes setting the {@link #objectType} and updating the
     * text field text.
     * <p>
     * Passing {@code null} will clear the selection.
     * 
     * @param qualifiedNameType The {@link QualifiedNameType} of the current selection
     */
    public void updateSelection(QualifiedNameType qualifiedNameType) {
        if (qualifiedNameType != null) {
            objectType = qualifiedNameType.getIpsObjectType();
            setText(qualifiedNameType.getName());
        } else {
            objectType = null;
            setText(StringUtils.EMPTY);
        }
    }

    @Override
    public void setButtonEnabled(boolean value) {
        super.setButtonEnabled(value);
        if (deleteButton != null) {
            deleteButton.setEnabled(value);
        }
    }
}
