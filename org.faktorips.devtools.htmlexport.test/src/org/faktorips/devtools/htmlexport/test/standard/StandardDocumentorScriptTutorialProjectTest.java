package org.faktorips.devtools.htmlexport.test.standard;


import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.test.XmlAbstractTestCase;
import org.faktorips.devtools.htmlexport.Documentor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.helper.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;
import org.faktorips.devtools.htmlexport.test.documentor.AbstractFipsDocTest;

public class StandardDocumentorScriptTutorialProjectTest extends XmlAbstractTestCase {

	protected String zielpfad;
	protected IIpsProject ipsProject;
	protected DocumentorConfiguration documentorConfig;
	protected Documentor documentor;

    @Override
    protected void setUp() throws Exception {
    	IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IpsPlugin.getDefault().reinitModel(); // also starts the listening process
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
        
		IProject project = workspace.getRoot().getProject("org.faktorips.tutorial.de.Hausratmodell");

		IpsModel model = new IpsModel();
		ipsProject = model.getIpsProject(project);

		documentorConfig = new DocumentorConfiguration();
		
        // So werden Dateien im Projekt gespeichert
		IPath location = ipsProject.getProject().getLocation();
		
		documentorConfig.setPath(location + File.separator + "html");
		documentorConfig.setIpsProject(ipsProject);
		documentorConfig.setLayouter(new HtmlLayouter(".resource"));

		documentor = new Documentor(documentorConfig);
        
        documentorConfig.addDocumentorScript(new StandardDocumentorScript());
        documentorConfig.setLinkedIpsObjectClasses(documentorConfig.getIpsProject().getIpsModel().getIpsObjectTypes());
    }

    public void testWriteWithoutException()  {
        deletePreviousGeneratedFiles();
        
        documentor.run();
    }

	protected void deletePreviousGeneratedFiles() {
		File file = new File(documentorConfig.getPath());
		if (file.exists()) {
			file.delete();
		}
	}
}
