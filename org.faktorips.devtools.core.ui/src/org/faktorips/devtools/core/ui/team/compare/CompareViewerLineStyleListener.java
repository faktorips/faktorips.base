/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.team.compare.productcmpt.Messages;

/**
 * LineStyleListener for the IpsObjectCompareViewer. Colors keywords (tokens) in the text
 * representation of a productcomponent.
 * 
 * @author Stefan Widmaier
 */
public class CompareViewerLineStyleListener implements LineStyleListener {

    protected final Color ipsObjectHighlight = new Color(IpsPlugin.getDefault().getWorkbench().getDisplay(), 35, 120,
            60);
    protected final Color generationHighlight = new Color(IpsPlugin.getDefault().getWorkbench().getDisplay(), 0, 0, 125);
    protected final Color dateHighlight = new Color(IpsPlugin.getDefault().getWorkbench().getDisplay(), 200, 200, 200);

    /**
     * Pattern for recognizing separators between generations (single line starting with "-")
     */
    private Pattern genSeparatorPattern = Pattern.compile("-"); //$NON-NLS-1$

    /**
     * List of patterns to be applied to lines in getStylesForRestOfLine().
     */
    protected List<Pattern> linePatternList = new ArrayList<Pattern>();
    /**
     * Maps patterns to specific highlight colors and thus defines a colour for specific tokens
     * should in which the should be displayed.
     */
    private Map<Pattern, Color> highlightColorMap = new HashMap<Pattern, Color>();

    public CompareViewerLineStyleListener() {
        // init patterns and map highlight colors for productCmpts
        Pattern productPattern = Pattern
                .compile(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductCmptEditor_productComponent);
        linePatternList.add(productPattern);
        highlightColorMap.put(productPattern, ipsObjectHighlight);
        Pattern attributesPattern = Pattern
                .compile(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_attribute
                        + AbstractCompareItem.COLON_BLANK);
        linePatternList.add(attributesPattern);
        Pattern propertiesPattern = Pattern
                .compile(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.PropertiesPage_relations
                        + AbstractCompareItem.COLON_BLANK);
        linePatternList.add(propertiesPattern);
        Pattern rulesPattern = Pattern
                .compile(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ValidationRuleSection_DefaultTitle
                        + AbstractCompareItem.COLON_BLANK);
        linePatternList.add(rulesPattern);
        Pattern typePattern = Pattern
                .compile(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_template
                        + AbstractCompareItem.COLON_BLANK);
        linePatternList.add(typePattern);
        Pattern runtimeIDPattern = Pattern
                .compile(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_labelRuntimeId
                        + AbstractCompareItem.COLON_BLANK);
        linePatternList.add(runtimeIDPattern);
        Pattern tableUsagesPattern = Pattern.compile(Messages.ProductCmptCompareItem_TableUsagesHeader
                + AbstractCompareItem.COLON_BLANK);
        linePatternList.add(tableUsagesPattern);

        // Patterns for TableContents Messages.TableContentsCompareItem_TableContents
        Pattern tablePattern = Pattern
                .compile(org.faktorips.devtools.core.ui.team.compare.tablecontents.Messages.TableContentsCompareItem_TableContents);
        linePatternList.add(tablePattern);
        highlightColorMap.put(tablePattern, ipsObjectHighlight);
        Pattern tableStructurePattern = Pattern
                .compile(org.faktorips.devtools.core.ui.team.compare.tablecontents.Messages.TableContentsCompareItem_TableStructure);
        linePatternList.add(tableStructurePattern);

        // patterns for all ipsObjects
        String generationString = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular();
        Pattern generationPattern = Pattern.compile(generationString);
        linePatternList.add(generationPattern);
        highlightColorMap.put(generationPattern, generationHighlight);
        Pattern validFromPattern = Pattern
                .compile(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.GenerationEditDialog_labelValidFrom);
        linePatternList.add(validFromPattern);
    }

    /**
     * Creates none, one or two StyleRanges for the given line. A style is created if a date or dash
     * is found at the start of the current line. This style displays the date (or dash) in a light
     * gray.
     * <p>
     * If a token is found in the line a style for the rest of the line (not including the date) is
     * created. This range is colored dependant on the token and the font is set to bold.
     * {@inheritDoc}
     */
    @Override
    public void lineGetStyle(LineStyleEvent event) {
        String lineText = event.lineText;
        int lineOffset = event.lineOffset;
        List<StyleRange> styleList = new ArrayList<StyleRange>();

        styleList.addAll(getStylesForLineStart(lineText, lineOffset));
        styleList.addAll(getStylesForRestOfLine(lineText, lineOffset));

        StyleRange[] styleArray = new StyleRange[styleList.size()];
        event.styles = styleList.toArray(styleArray);
    }

    /**
     * Returns a list one or no <code>StyleRange</code> for a token at linestart. This may either be
     * a date (in <code>SimpleDateFormat.MEDIUM</code>) or a dash. (Lines starting with "-" and a
     * date are not possible)
     * <p>
     * Only returns a style a if date/dash string is found, and only if such a token is found at
     * linestart to avoid faulty highlighting.
     */
    protected List<StyleRange> getStylesForLineStart(String lineText, int lineOffset) {
        List<StyleRange> styleList = new ArrayList<StyleRange>();
        ParsePosition pos = new ParsePosition(0);
        IpsPlugin.getDefault().getIpsPreferences().getDateFormat().parse(lineText, pos);
        int endIndex = pos.getIndex();
        if (endIndex > 8) {
            styleList.add(new StyleRange(lineOffset, endIndex, dateHighlight, null, SWT.NORMAL));
            // }
            // Matcher genDateMatcher = genDatePattern.matcher(lineText);
            // if (genDateMatcher.find()) {
            // if (genDateMatcher.start() == 0) {
            // styleList.add(new StyleRange(lineOffset, genDateMatcher.end(), dateHighlight, null,
            // SWT.NORMAL));
            // }
        } else {
            Matcher genSeparatorMatcher = genSeparatorPattern.matcher(lineText);
            if (genSeparatorMatcher.find()) {
                if (genSeparatorMatcher.start() == 0) {
                    styleList.add(new StyleRange(lineOffset, lineText.length(), dateHighlight, null, SWT.NORMAL));
                }
            }
        }
        return styleList;
    }

    /**
     * Returns a list of styles containing one or no <code>StyleRange</code> that hightlights the
     * found token and the following text in the given line. All token matches are highlighted with
     * bold font and an optional color.
     */
    protected List<StyleRange> getStylesForRestOfLine(String lineText, int lineOffset) {
        List<StyleRange> styleList = new ArrayList<StyleRange>();
        for (Pattern pattern : linePatternList) {
            Matcher matcher = pattern.matcher(lineText);
            if (matcher.find()) {
                int start = matcher.start();
                Color highlight = highlightColorMap.get(pattern); // if null, default foreground is
                // used
                styleList.add(new StyleRange(lineOffset + start, lineText.length() - start, highlight, null, SWT.BOLD));
                break;
            }
        }
        return styleList;
    }
}
