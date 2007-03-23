/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.StringUtil;


/**
 *
 */
public class IpsObjectCompletionProcessor extends AbstractCompletionProcessor {
    
    private IpsObjectRefControl control;
    private IpsObjectType ipsObjectType;

    public IpsObjectCompletionProcessor(IpsObjectRefControl control) {
        this.control = control;
        ipsObjectType = null;
    }
    
    public IpsObjectCompletionProcessor(IpsObjectType type) {
        ipsObjectType = type;
        control = null;
    }
    
    /**
	 * Returns whether the given qualified name will match the given package and
	 * name prefixes.
	 * 
	 * @param pack
	 *            The package the given qualified name must match. Can be empty,
	 *            but must not be <code>null</code>.
	 * @param prefix
	 *            The prefix the unqualifed name must match. Can be emtpy, but
	 *            must not be <code>null</code>.
	 * @param qualifiedName
	 *            The qulified name to match against the given package and name
	 *            prefix.
	 * 
	 * @return <code>true</code> if the given qualified name matches,
	 *         <code>false</code> otherwise.
	 */
	private boolean match(String pack, String prefix, String qualifiedName) {
		String toMatchPack = StringUtil.getPackageName(qualifiedName).toLowerCase();
		String toMatchName = StringUtil.unqualifiedName(qualifiedName).toLowerCase();

		return (StringUtils.isEmpty(pack) || toMatchPack.startsWith(pack.toLowerCase()))
				&& (StringUtils.isEmpty(prefix) || toMatchName.startsWith(prefix.toLowerCase()));
	}

    public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
    	if (ipsProject == null && control != null) {
    		ipsProject = control.getIpsProject();
    	}
    	return super.computeCompletionProposals(contentAssistSubjectControl, documentOffset);
    }

	
	protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        if (control==null && ipsProject==null) {
            setErrorMessage(Messages.IpsObjectCompletionProcessor_msgNoProject);
            return;
        }
        String match = prefix.toLowerCase();
        
        String matchPack = StringUtil.getPackageName(match);
        String matchName = StringUtil.unqualifiedName(match);
        
        try {
            IIpsObject[] objects;
            if (control != null) {
                objects = control.getIpsObjects();    
            } else {
                objects = ipsProject.findIpsObjects(ipsObjectType);
            }
            for (int i = 0; i < objects.length; i++) {
                if (match(matchPack, matchName, objects[i].getQualifiedName())) {
                    String qName = objects[i].getQualifiedName();
                    String displayText = objects[i].getName()
                            + " - " + mapDefaultPackageName(objects[i].getParent().getParent().getName()); //$NON-NLS-1$
                    CompletionProposal proposal = new CompletionProposal(qName, 0, documentOffset, qName.length(),
                            objects[i].getImage(), displayText, null, objects[i].getDescription());
                    result.add(proposal);
                }
            }

            IIpsProject prj = ipsProject;
            if (prj == null && control != null) {
                prj = control.getIpsProject();
            }
            if (prj == null) {
                return;
            }

            // find packages of the project this completion processor was created in
            IIpsPackageFragmentRoot[] roots = prj.getIpsPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++) {
                matchPackages(roots[i].getIpsPackageFragments(), prefix, documentOffset, result);
            }

            // find packages of projects, the project of this compeltion processor refers to...
            IIpsProject[] projects = prj.getIpsObjectPath().getReferencedIpsProjects();
            for (int i = 0; i < projects.length; i++) {
                roots = projects[i].getIpsPackageFragmentRoots();
                for (int j = 0; j < projects.length; j++) {
                    matchPackages(roots[j].getIpsPackageFragments(), prefix, documentOffset, result);
                }
            }
        } catch (Exception e) {
            setErrorMessage(Messages.IpsObjectCompletionProcessor_msgInternalError);
            IpsPlugin.log(e);
            return;
        }
	}
}
