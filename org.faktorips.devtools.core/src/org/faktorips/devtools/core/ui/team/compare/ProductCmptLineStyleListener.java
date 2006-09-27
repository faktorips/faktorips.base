/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

/**
 * LineStyleListener for the IpsObjectCompareViewer. Colors keywords (tokens) in the text representation
 * of a productcomponent. 
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptLineStyleListener implements LineStyleListener{

    private Color foreground;
    private Color productHighlight;
    private Color generationHighlight;
    private Color dateHighlight;
    private Pattern genNumberPattern;
    private Pattern genSeparatorPattern;
        
    public ProductCmptLineStyleListener(SourceViewer viewer) {
        foreground = viewer.getTextWidget().getForeground();
        
        // green similar to ProductCmpt Icon
        productHighlight= new Color(viewer.getTextWidget().getDisplay(), 35, 120, 60);
        generationHighlight= new Color(viewer.getTextWidget().getDisplay(), 0, 0, 125);
        dateHighlight= new Color(viewer.getTextWidget().getDisplay(), 200, 200, 200);
        
        // regex-patterns for recognizing dates at linestart; only matches SimpleDateFormat.MEDIUM
        genNumberPattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+");
        genSeparatorPattern = Pattern.compile("-");
    }

    /**
     * Creates none, one or two StyleRanges for the given line. A style is created if a date or dash is found at 
     * the start of the current line. This style displays the date (or dash) in a light gray.
     * <p>
     * If a token is found in the line a style for the rest of the line (not including the date) is created. 
     * This range is colored dependant on the token and the font is set to bold.
     * {@inheritDoc}
     */
    public void lineGetStyle(LineStyleEvent event) {
        String lineText= event.lineText;

        // search for generation validFrom date at linestart and make it invisible (paint in backgroundColor)
        StyleRange numberStyle= null;
        int numberLength= 0;
        Matcher genNumberMatcher= genNumberPattern.matcher(lineText);
        Matcher genSeparatorMatcher= genSeparatorPattern.matcher(lineText);
        if(genNumberMatcher.find()){
            // number is always printed at beginning of line
            // genNumberMatcher.end() returns the end-index of the first match in lineText
            numberLength= genNumberMatcher.end();
            numberStyle= new StyleRange(event.lineOffset, numberLength, dateHighlight, null, SWT.NORMAL);
        }else if(genSeparatorMatcher.find()){
            numberStyle= new StyleRange(event.lineOffset, lineText.length(), dateHighlight, null, SWT.NORMAL);
        }
        
        // search for tokens
        StyleRange tokenStyle= getStyleRangeForToken(lineText, event.lineOffset+numberLength, event.lineText.length());
        
        if(numberStyle!=null && tokenStyle!=null){
            event.styles = new StyleRange[]{numberStyle, tokenStyle};
        }else if(numberStyle!=null){
            event.styles = new StyleRange[]{numberStyle};
        }else if(tokenStyle!=null){
            event.styles = new StyleRange[]{tokenStyle};
        }
    }

    /**
     * Returns a StyleRange for the given lineText if a known token is found.
     * <p>
     * The background color for the created range is not changed (null), so the background can be set by the TextViewer.
     */
    private StyleRange getStyleRangeForToken(String linetext, int offset, int length) {
        if(linetext.indexOf(Messages.ProductCmptCompareItem_ProductComponent)!=-1){
            return new StyleRange(offset, length, productHighlight, null, SWT.BOLD);
        }else if(linetext.indexOf(Messages.ProductCmptCompareItem_Generation)!=-1){
            return new StyleRange(offset, length, generationHighlight, null, SWT.BOLD);
        }else if(linetext.indexOf(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_attribute)!=-1){
            return new StyleRange(offset, length, foreground, null, SWT.BOLD);
        }else if(linetext.indexOf(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.PropertiesPage_relations)!=-1){
            return new StyleRange(offset, length, foreground, null, SWT.BOLD);
        }else if(linetext.indexOf(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.GenerationEditDialog_labelValidFrom)!=-1){
            return new StyleRange(offset, length, foreground, null, SWT.BOLD);
        }else{
            return null;
        }
    }
}
