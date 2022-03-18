package org.faktorips.devtools.filetypes;

import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class PolicyCmptTypeFileType extends LanguageFileType {

    public static final LanguageFileType INSTANCE = new PolicyCmptTypeFileType();
    private static final Icon ICON = IconLoader.getIcon("/icons/filetypes/PolicyCmptType.png", PolicyCmptTypeFileType.class);
    private static final String FILE_EXTENSION = "ipspolicycmpttype";

    public PolicyCmptTypeFileType() {
        super(XMLLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "PolicyComponentType";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Policy Component Type";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public @Nullable Icon getIcon() {
        return ICON;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return getName();
    }
}
