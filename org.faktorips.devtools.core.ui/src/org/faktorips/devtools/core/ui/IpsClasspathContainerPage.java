/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsClasspathContainerInitializer;
import org.faktorips.devtools.core.IpsPlugin;

public class IpsClasspathContainerPage extends NewElementWizardPage implements IClasspathContainerPage,
        IClasspathContainerPageExtension {

    private IClasspathEntry entry;
    private IJavaProject javaProject;

    public IpsClasspathContainerPage() {
        super("Faktor-IPS Library"); //$NON-NLS-1$
        setTitle("Faktor-IPS Library"); //$NON-NLS-1$
        setDescription(Messages.IpsClasspathContainerPage_0);
        setImageDescriptor(JavaPluginImages.DESC_WIZBAN_ADD_LIBRARY); // access restricted, but why?
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setFont(composite.getFont());
        label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1));
        label.setText(Messages.IpsClasspathContainerPage_1);

        setControl(composite);
    }

    public static IJavaProject getPlaceholderProject() {
        String name = "####internal"; //$NON-NLS-1$
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        while (true) {
            IProject project = root.getProject(name);
            if (!project.exists()) {
                return JavaCore.create(project);
            }
            name += '1';
        }
    }

    @Override
    public boolean finish() {
        try {
            IClasspathContainer[] containers = { null };
            IPath entryPath;
            if (entry == null) {
                entryPath = IpsClasspathContainerInitializer.ENTRY_PATH;
                entry = JavaCore.newContainerEntry(entryPath);
            } else {
                entryPath = entry.getPath();
            }
            // I expected that it would work to pass the "current" java project in the
            // setClasspathContainer(..) method,
            // however when you do this, Eclipse marks the classpath entry as unbound (you can see
            // it in the build path dialog).
            // When you take the placeholder project, it works. No idea why!?! The JDT JUnit support
            // does it the same way!
            IJavaProject[] javaProjects = new IJavaProject[] { getPlaceholderProject() };
            JavaCore.setClasspathContainer(entryPath, javaProjects, containers, null);
        } catch (JavaModelException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }

    @Override
    public IClasspathEntry getSelection() {
        return entry;
    }

    @Override
    public void setSelection(IClasspathEntry containerEntry) {
        this.entry = containerEntry;
    }

    @Override
    public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
        this.javaProject = project;
    }

    public IJavaProject getJavaProject() {
        return javaProject;
    }

}
