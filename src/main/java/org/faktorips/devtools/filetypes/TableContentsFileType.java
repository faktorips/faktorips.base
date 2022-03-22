/*******************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.com
 *
 * All Rights Reserved - Alle Rechte vorbehalten.
 ******************************************************/
package org.faktorips.devtools.filetypes;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TableContentsFileType extends LanguageFileType {

    public static final LanguageFileType INSTANCE = new TableContentsFileType();
    private static final Icon ICON = IconLoader.getIcon("/icons/filetypes/TableContents.svg", TableContentsFileType.class);
    private static final String FILE_EXTENSION = "ipstablecontents";

    public TableContentsFileType() {
        super(XMLLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "TableContents";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Table Contents";
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
