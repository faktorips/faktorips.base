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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;


/**
 *
 */
public class IpsObjectCompletionProcessor implements IContentAssistProcessor, ISubjectControlContentAssistProcessor {
    
    private IIpsProject pdProject;
    private IpsObjectRefControl control;
    private IpsObjectType pdObjectType;
    private char[] proposalAutoActivationSet;
    private String errorMessage;
    private boolean computeProposalForEmptyPrefix = false;

    public IpsObjectCompletionProcessor(IpsObjectRefControl control) {
        this.control = control;
        pdObjectType = null;
    }
    
    public IpsObjectCompletionProcessor(IpsObjectType type) {
        pdObjectType = type;
        control = null;
    }
    
    public void setPdProject(IIpsProject project) {
        pdProject = project;
    }
    
    /**
     * If true, the processor proposes all objects if the user has provided
     * no prefix to start with. If false, the processor won't generate a
     * prososal.
     */
    public void setComputeProposalForEmptyPrefix(boolean value) {
        computeProposalForEmptyPrefix = value;
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        throw new RuntimeException("ITextViewer not supported."); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        throw new RuntimeException("ITextViewer not supported."); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return proposalAutoActivationSet;
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator() {
		return null; //no context
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor#computeContextInformation(org.eclipse.jface.contentassist.IContentAssistSubjectControl, int)
     */
    public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
        return null;
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.contentassist.IContentAssistSubjectControl, int)
     */
    public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		if (documentOffset == 0 && !computeProposalForEmptyPrefix) {
			return null;
		}
        if (control==null && pdProject==null) {
            errorMessage = Messages.IpsObjectCompletionProcessor_msgNoProject;
            return null;
        }
		String input= contentAssistSubjectControl.getDocument().get();
        String match = input.substring(0, documentOffset).toLowerCase();
        
        List result = new ArrayList(100);
        try {
            IIpsObject[] objects;
            if (control != null) {
                objects = control.getPdObjects();    
            } else {
                objects = pdProject.findIpsObjects(pdObjectType);
            }
            for (int i=0; i<objects.length; i++) {
                if (objects[i].getName().toLowerCase().startsWith(match)) {
                    String qName = objects[i].getQualifiedName();
                    String displayText = objects[i].getName() + " - " + objects[i].getParent().getParent().getName(); //$NON-NLS-1$
                    CompletionProposal proposal = new CompletionProposal(
                            qName, 0, documentOffset, qName.length(),  
                            objects[i].getImage(), displayText, null, objects[i].getDescription());
                    result.add(proposal);
                }
            }
        } catch (Exception e) {
            errorMessage = Messages.IpsObjectCompletionProcessor_msgInternalError;
            IpsPlugin.log(e);
            return null;
        }
        
		ICompletionProposal[] proposals = new ICompletionProposal[result.size()];
		result.toArray(proposals);
		return proposals;
    }


}
