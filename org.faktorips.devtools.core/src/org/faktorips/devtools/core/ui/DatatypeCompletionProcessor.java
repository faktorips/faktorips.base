package org.faktorips.devtools.core.ui;

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
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;


/**
 *
 */
public class DatatypeCompletionProcessor implements IContentAssistProcessor, ISubjectControlContentAssistProcessor {
    
    private IIpsProject ipsProject;
    private char[] proposalAutoActivationSet;
    private String errorMessage;
    private boolean includeVoid = false;
    private boolean valuetypesOnly = false;

    public DatatypeCompletionProcessor() {
    }
    
    public void setIpsProject(IIpsProject project) {
        ipsProject = project;
    }
    
    public void setIncludeVoid(boolean value) {
        includeVoid = value;
    }
    
    public boolean getIncludeVoid() {
        return includeVoid;
    }
    
    public void setValueDatatypesOnly(boolean value) {
        valuetypesOnly = value;
    }
    
    public boolean getValueDatatypesOnly() {
        return valuetypesOnly;
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        throw new RuntimeException("ITextViewer not supported.");
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        throw new RuntimeException("ITextViewer not supported.");
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
		String input= contentAssistSubjectControl.getDocument().get();
		if (documentOffset == 0) {
			return null;
		}
        if (ipsProject==null) {
            errorMessage = "No project context available.";
            return null;
        }
        List result = new ArrayList(100);
        String match = input.toLowerCase();
        try {
            Datatype[] types = ipsProject.findDatatypes(valuetypesOnly, includeVoid);
            for (int i=0; i<types.length; i++) {
                if (types[i].getName().toLowerCase().startsWith(match)) {
                    String qName = types[i].getQualifiedName();
                    String displayText = types[i].getName();
                    CompletionProposal proposal = new CompletionProposal(
                            qName, 0, documentOffset, qName.length(),  
                            null, displayText, null, null);
                    result.add(proposal);
                }
            }
        } catch (Exception e) {
            errorMessage = "An internal error occured while searching. See the log file for details.";
            IpsPlugin.log(e);
            return null;
        }
        
		ICompletionProposal[] proposals = new ICompletionProposal[result.size()];
		result.toArray(proposals);
		return proposals;
    }

}
