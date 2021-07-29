/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;

/**
 * This action goes over all {@link IIpsObjectPartContainer}s of an {@link IIpsProject} and ensures
 * that each one has an {@link ILabel} and an {@link IDescription} for each
 * {@link ISupportedLanguage}.
 * <p>
 * If a container has too many labels or descriptions, the ones that are too much are deleted. If a
 * container has too few labels or descriptions, the missing ones are created.
 * <p>
 * This action is only a temporary solution that is needed in case supported languages are added or
 * removed from an IPS project. It will be replaced at a later time by preference pages that allow
 * to edit the properties of an IPS project.
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.1
 */
public class CleanUpTranslationsAction extends IpsAction implements IObjectActionDelegate {

    private IWorkbenchWindow workbenchWindow;

    private ISelection delegateSelection;

    private IWorkbenchPart delegateActivePart;

    public CleanUpTranslationsAction() {
        super(null);
    }

    public CleanUpTranslationsAction(ISelectionProvider selectionProvider, IWorkbenchWindow workbenchWindow) {
        super(selectionProvider);
        this.workbenchWindow = workbenchWindow;
        setText(Messages.CleanUpTranslationsAction_text);
    }

    @Override
    public void run(IStructuredSelection selection) {
        TypedSelection<IAdaptable> typedSelection = TypedSelection.create(IAdaptable.class, selection, 1,
                TypedSelection.INFINITY);
        if (!(typedSelection.isValid())) {
            return;
        }

        boolean editorsSaved = IpsUIPlugin.getDefault().saveAllEditors();
        if (!(editorsSaved)) {
            return;
        }

        /*
         * Collect the potential different IPS projects of the selected elements as we want to
         * batch-execute the action on all projects.
         */
        Set<IIpsProject> ipsProjects = new HashSet<>();
        for (IAdaptable adaptable : typedSelection.getElements()) {
            IIpsProject ipsProject = null;
            if (adaptable instanceof IIpsElement) {
                ipsProject = ((IIpsElement)adaptable).getIpsProject();
            } else if (adaptable instanceof IResource) {
                ipsProject = IIpsModel.get()
                        .getIpsProject(Wrappers.wrap(((IResource)adaptable).getProject()).as(AProject.class));
            } else if (adaptable instanceof IJavaElement) {
                ipsProject = IIpsModel.get()
                        .getIpsProject(Wrappers.wrap(((IJavaElement)adaptable).getJavaProject().getProject())
                                .as(AProject.class));
            }
            if (!(ipsProjects.contains(ipsProject)) && ipsProject != null) {
                ipsProjects.add(ipsProject);
            }
        }

        Shell shell = delegateActivePart == null ? workbenchWindow.getShell() : delegateActivePart.getSite().getShell();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        try {
            dialog.run(true, true, new CleanUpTranslationsRunnableWithProgress(ipsProjects));
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        delegateActivePart = targetPart;
    }

    @Override
    public void run(IAction action) {
        run((IStructuredSelection)delegateSelection);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        delegateSelection = selection;
    }

    private static class CleanUpTranslationsRunnableWithProgress implements IRunnableWithProgress {

        private final Collection<IIpsProject> ipsProjects;

        public CleanUpTranslationsRunnableWithProgress(Collection<IIpsProject> ipsProjects) {
            this.ipsProjects = ipsProjects;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            try {
                ResourcesPlugin.getWorkspace().run(new CleanUpTranslationsWorkspaceRunnable(), monitor);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        private class CleanUpTranslationsWorkspaceRunnable implements ICoreRunnable {

            @Override
            public void run(IProgressMonitor monitor) {
                for (IIpsProject ipsProject : ipsProjects) {
                    List<IIpsSrcFile> ipsSrcFiles = new ArrayList<>();
                    IIpsPackageFragmentRoot[] fragmentRoots = ipsProject.getIpsPackageFragmentRoots();
                    for (IIpsPackageFragmentRoot root : fragmentRoots) {
                        for (IIpsPackageFragment fragment : root.getIpsPackageFragments()) {
                            ipsSrcFiles.addAll(Arrays.asList(fragment.getIpsSrcFiles()));
                        }
                    }

                    Set<Locale> supportedLocales = getSupportedLocales(ipsProject);

                    int totalWork = ipsSrcFiles.size();
                    monitor.beginTask(NLS.bind(Messages.CleanUpTranslationsAction_progressTask, ipsProject), totalWork);
                    for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
                        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
                        cleanUp(ipsObject, supportedLocales);
                        ipsSrcFile.save(null);
                        monitor.worked(1);
                    }
                    monitor.done();
                }
            }

            private Set<Locale> getSupportedLocales(IIpsProject ipsProject) {
                Set<ISupportedLanguage> supportedLanguages = ipsProject.getReadOnlyProperties().getSupportedLanguages();
                Set<Locale> supportedLocales = new HashSet<>(supportedLanguages.size());
                for (ISupportedLanguage language : supportedLanguages) {
                    Locale locale = language.getLocale();
                    if (locale != null) {
                        supportedLocales.add(locale);
                    }
                }
                return supportedLocales;
            }

            private void cleanUp(IIpsObjectPartContainer ipsObjectPartContainer, Set<Locale> supportedLocales) {
                cleanUpChildren(ipsObjectPartContainer, supportedLocales);

                if (ipsObjectPartContainer instanceof IDescribedElement) {
                    IDescribedElement describedElement = (IDescribedElement)ipsObjectPartContainer;
                    deleteObsoleteDescriptions(describedElement, supportedLocales);
                    addMissingDescriptions(describedElement, supportedLocales);
                }

                if (ipsObjectPartContainer instanceof ILabeledElement) {
                    ILabeledElement labeledElement = (ILabeledElement)ipsObjectPartContainer;
                    deleteObsoleteLabels(labeledElement, supportedLocales);
                    addMissingLabels(labeledElement, supportedLocales);
                }
            }

            private void cleanUpChildren(IIpsObjectPartContainer ipsObjectPartContainer, Set<Locale> supportedLocales) {
                IIpsElement[] children = ipsObjectPartContainer.getChildren();
                for (IIpsElement child : children) {
                    if (child instanceof IIpsObjectPartContainer) {
                        cleanUp((IIpsObjectPartContainer)child, supportedLocales);
                    }
                }
            }

            private void addMissingDescriptions(IDescribedElement describedElement, Set<Locale> supportedLocales) {
                for (Locale locale : supportedLocales) {
                    if (describedElement.getDescription(locale) == null) {
                        IDescription newDescription = describedElement.newDescription();
                        newDescription.setLocale(locale);
                    }
                }
            }

            private void addMissingLabels(ILabeledElement labeledElement, Set<Locale> supportedLocales) {
                for (Locale locale : supportedLocales) {
                    if (labeledElement.getLabel(locale) == null) {
                        ILabel newLabel = labeledElement.newLabel();
                        newLabel.setLocale(locale);
                    }
                }
            }

            private void deleteObsoleteDescriptions(IDescribedElement describedElement, Set<Locale> supportedLocales) {
                List<IDescription> descriptionList = describedElement.getDescriptions();
                // Transformation to array to avoid concurrent modification
                IDescription[] descriptionArray = descriptionList.toArray(new IDescription[descriptionList.size()]);
                for (IDescription description : descriptionArray) {
                    Locale locale = description.getLocale();
                    if (locale != null) {
                        if (!(supportedLocales.contains(locale))) {
                            description.delete();
                        }
                    }
                }
            }

            private void deleteObsoleteLabels(ILabeledElement labeledElement, Set<Locale> supportedLocales) {
                List<ILabel> labelList = labeledElement.getLabels();
                // Transformation to array to avoid concurrent modification
                ILabel[] labelArray = labelList.toArray(new ILabel[labelList.size()]);
                for (ILabel label : labelArray) {
                    Locale locale = label.getLocale();
                    if (locale != null) {
                        if (!(supportedLocales.contains(locale))) {
                            label.delete();
                        }
                    }
                }
            }

        }

    }

}
