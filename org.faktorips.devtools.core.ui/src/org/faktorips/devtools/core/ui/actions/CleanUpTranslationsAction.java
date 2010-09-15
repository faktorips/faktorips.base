/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.ui.util.TypedSelection;

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

        /*
         * Collect the potential different IPS projects of the selected elements as we want to
         * batch-execute the action on all projects.
         */
        Set<IIpsProject> ipsProjects = new HashSet<IIpsProject>();
        for (IAdaptable adaptable : typedSelection.getElements()) {
            IIpsProject ipsProject = null;
            if (adaptable instanceof IIpsElement) {
                ipsProject = ((IIpsElement)adaptable).getIpsProject();
            } else if (adaptable instanceof IResource) {
                ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(((IResource)adaptable).getProject());
            } else if (adaptable instanceof IJavaElement) {
                ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(
                        ((IJavaElement)adaptable).getJavaProject().getProject());
            }
            if (!(ipsProjects.contains(ipsProject)) && ipsProject != null) {
                ipsProjects.add(ipsProject);
            }
        }

        Shell shell = delegateActivePart == null ? workbenchWindow.getShell() : delegateActivePart.getSite().getShell();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        try {
            dialog.run(true, true, new CleanUpRunnable(ipsProjects));
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

    private static class CleanUpRunnable implements IRunnableWithProgress {

        private final Collection<IIpsProject> ipsProjects;

        public CleanUpRunnable(Collection<IIpsProject> ipsProjects) {
            this.ipsProjects = ipsProjects;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            for (IIpsProject ipsProject : ipsProjects) {
                List<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>();
                try {
                    IIpsPackageFragmentRoot[] fragmentRoots = ipsProject.getIpsPackageFragmentRoots();
                    for (IIpsPackageFragmentRoot root : fragmentRoots) {
                        for (IIpsPackageFragment fragment : root.getIpsPackageFragments()) {
                            ipsSrcFiles.addAll(Arrays.asList(fragment.getIpsSrcFiles()));
                        }
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }

                Set<Locale> supportedLocales = getSupportedLocales(ipsProject);

                int totalWork = ipsSrcFiles.size();
                monitor.beginTask(NLS.bind(Messages.CleanUpTranslationsAction_progressTask, ipsProject), totalWork);
                for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
                    try {
                        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
                        cleanUp(ipsObject, supportedLocales);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        ipsSrcFile.save(true, null);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                    monitor.worked(1);
                }
                monitor.done();
            }
        }

        private Set<Locale> getSupportedLocales(IIpsProject ipsProject) {
            Set<ISupportedLanguage> supportedLanguages = ipsProject.getProperties().getSupportedLanguages();
            Set<Locale> supportedLocales = new HashSet<Locale>(supportedLanguages.size());
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
            try {
                IIpsElement[] children = ipsObjectPartContainer.getChildren();
                for (IIpsElement child : children) {
                    if (child instanceof IIpsObjectPartContainer) {
                        cleanUp((IIpsObjectPartContainer)child, supportedLocales);
                    }
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
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
