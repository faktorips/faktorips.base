/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.ui.internal.text.HTMLTextPresenter;

/**
 * Collection of utility for content assist and auto completion. As of Eclipse 3.2 most the
 * ContentAssistHandler is replaced by JFace field assist support. So this class encapsulate the
 * calls to the deprecated API.
 */
@SuppressWarnings({ "deprecation", "javadoc" })
public class CompletionUtil {

    /** Preference key for content assist auto activation. */
    private static final String AUTOACTIVATION = PreferenceConstants.CODEASSIST_AUTOACTIVATION;

    /** Preference key for content assist auto activation delay. */
    private static final String AUTOACTIVATION_DELAY = PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY;

    /** Preference key for content assist proposal color. */
    private static final String PROPOSALS_FOREGROUND = JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR;

    /** Preference key for content assist proposal color. */
    private static final String PROPOSALS_BACKGROUND = JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR;

    /** Preference key for content assist parameters color. */
    private static final String PARAMETERS_FOREGROUND = PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND;

    /** Preference key for content assist parameters color. */
    private static final String PARAMETERS_BACKGROUND = PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND;

    /** Preference key for content assist auto insert. */
    private static final String AUTOINSERT = PreferenceConstants.CODEASSIST_AUTOINSERT;

    /** Preference key for prefix completion. */
    private static final String PREFIX_COMPLETION = PreferenceConstants.CODEASSIST_PREFIX_COMPLETION;

    private static JavaTextTools textTools;

    /**
     * Encapsulate the deprecated call to
     * {@link ContentAssistHandler#createHandlerForText(Text, SubjectControlContentAssistant)} to
     * avoid the deprecated warning in a lot of source sections.
     */
    public static ContentAssistHandler createHandlerForText(Text text, SubjectControlContentAssistant contentAssistant) {
        return ContentAssistHandler.createHandlerForText(text, contentAssistant);
    }

    /**
     * Encapsulate the deprecated call to
     * {@link ContentAssistHandler#createHandlerForText(Text, SubjectControlContentAssistant)} to
     * avoid the deprecated warning in a lot of source sections.
     */
    public static ContentAssistHandler createHandlerForText(Text text, IContentAssistProcessor processor) {
        return ContentAssistHandler.createHandlerForText(text, createContentAssistant(processor));
    }

    /**
     * Encapsulate the deprecated call to
     * {@link ContentAssistHandler#createHandlerForCombo(Combo, SubjectControlContentAssistant)}
     * avoid the deprecated warning in a lot of source sections.
     */
    public static ContentAssistHandler createHandlerForCombo(Combo combo, IContentAssistProcessor processor) {
        return ContentAssistHandler.createHandlerForCombo(combo, createContentAssistant(processor));
    }

    /**
     * Encapsulate the deprecated call to
     * {@link ContentAssistHandler#createHandlerForCombo(Combo, SubjectControlContentAssistant)}
     * avoid the deprecated warning in a lot of source sections.
     */
    public static ContentAssistHandler createHandlerForCombo(Combo combo,
            SubjectControlContentAssistant contentAssistant) {
        return ContentAssistHandler.createHandlerForCombo(combo, contentAssistant);
    }

    public static SubjectControlContentAssistant createContentAssistant(IContentAssistProcessor processor) {
        final SubjectControlContentAssistant contentAssistant = new SubjectControlContentAssistant();
        contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
        configure(contentAssistant, PreferenceConstants.getPreferenceStore());
        contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
        contentAssistant.setInformationControlCreator(new IInformationControlCreator() {
            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, new HTMLTextPresenter(true));
            }
        });
        return contentAssistant;
    }

    /**
     * Configure the given content assistant from the given store.
     */
    public static void configure(ContentAssistant assistant, IPreferenceStore store) {
        JavaTextTools tools = getJavaTextTools();
        IColorManager manager = tools.getColorManager();

        boolean enabled = store.getBoolean(AUTOACTIVATION);
        assistant.enableAutoActivation(enabled);

        int delay = store.getInt(AUTOACTIVATION_DELAY);
        assistant.setAutoActivationDelay(delay);

        Color c = getColor(store, PROPOSALS_FOREGROUND, manager);
        assistant.setProposalSelectorForeground(c);

        c = getColor(store, PROPOSALS_BACKGROUND, manager);
        assistant.setProposalSelectorBackground(c);

        c = getColor(store, PARAMETERS_FOREGROUND, manager);
        assistant.setContextInformationPopupForeground(c);
        assistant.setContextSelectorForeground(c);

        c = getColor(store, PARAMETERS_BACKGROUND, manager);
        assistant.setContextInformationPopupBackground(c);
        assistant.setContextSelectorBackground(c);

        enabled = store.getBoolean(AUTOINSERT);
        assistant.enableAutoInsert(enabled);

        enabled = store.getBoolean(PREFIX_COMPLETION);
        assistant.enablePrefixCompletion(enabled);
    }

    private static JavaTextTools getJavaTextTools() {
        if (textTools == null) {
            textTools = new JavaTextTools(PreferenceConstants.getPreferenceStore());
        }

        return textTools;
    }

    private static Color getColor(IPreferenceStore store, String key, IColorManager manager) {
        RGB rgb = PreferenceConverter.getColor(store, key);
        return manager.getColor(rgb);
    }

    private CompletionUtil() {
        // Utility class not to be instantiated.
    }

}
