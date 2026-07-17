/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.eclipse.emf.codegen.merge.java;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.emf.codegen.util.CodeGenUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Post-processes a Java source string after JMerger by inserting configurable start/end-tag
 * comments around {@code @generated} members (methods, fields, enum constants), and removing such
 * tags from members that carry {@code @generated NOT} or any other {@code @generated} variant with
 * trailing text.
 * <p>
 * Example output when start tag is {@code @START@} and end tag is {@code @END@}:
 *
 * <pre>
 * //@START@
 * /**
 *  * &#64;generated
 *  *&#47;
 * &#64;IpsGenerated
 * public void foo() { ... }
 * //@END@
 * </pre>
 *
 * The injection is idempotent: if the tags are already present around a member they are not
 * inserted again. Idempotency is determined by checking the AST comment list of the parsed source,
 * so it is robust against JMerger's ASTRewrite pass. A formatter-added blank line between a tag and
 * the member is tolerated — the tag is still recognised and not duplicated.
 * <p>
 * Tags around members with {@code @generated NOT} (or any other {@code @generated <text>} variant)
 * are removed. Only the tag line itself is deleted; adjacent blank lines are left untouched.
 *
 * @since 27.1
 */
public class GeneratedMemberMarkerInjector {

    // mirrors merge.java5.xml line 8: @generated followed only by optional "This field/method..."
    // text
    private static final Pattern GENERATED_PATTERN = Pattern
            .compile("@\\s*generated\\s*(?:This field/method[^\\r\\n]*)*(?:\\R| \\*/)"); //$NON-NLS-1$

    private static final Pattern ANY_GENERATED_PATTERN = Pattern.compile("@\\s*generated"); //$NON-NLS-1$

    private final String startTag;
    private final String endTag;
    private final Map<String, String> javaCoreOptions;

    /**
     * Creates a new injector. Both tags are wrapped as line comments ({@code // tag}).
     *
     * @param startTag tag text to insert before each {@code @generated} member; must not be null or
     *            empty
     * @param endTag tag text to insert after each {@code @generated} member; must not be null or
     *            empty
     * @param javaCoreOptions compiler options forwarded to the AST parser
     * @throws IllegalArgumentException if either tag is null or empty
     */
    public GeneratedMemberMarkerInjector(String startTag, String endTag, Map<String, String> javaCoreOptions) {
        if (startTag == null || startTag.isEmpty()) {
            throw new IllegalArgumentException("startTag must not be null or empty"); //$NON-NLS-1$
        }
        if (endTag == null || endTag.isEmpty()) {
            throw new IllegalArgumentException("endTag must not be null or empty"); //$NON-NLS-1$
        }
        this.startTag = toLineComment(startTag);
        this.endTag = toLineComment(endTag);
        this.javaCoreOptions = javaCoreOptions;
    }

    private static String toLineComment(String tag) {
        return "//" + commentContent(tag); //$NON-NLS-1$
    }

    /**
     * Post-processes the given Java source string by injecting start/end-tag line comments around
     * every {@code @generated} member and removing such tags from {@code @generated NOT} members.
     * The operation is idempotent.
     *
     * @param source the full Java source to process
     * @return the source with start/end-tag comments injected/removed; unchanged if no action is
     *             needed
     */
    public String inject(String source) {
        CompilationUnit cu = parse(source);
        List<ChangePoint> changes = collectChangePoints(cu, source);
        if (changes.isEmpty()) {
            return source;
        }
        changes.sort(Comparator.comparingInt(ChangePoint::sortKey).reversed());
        String lineSeparator = detectLineSeparator(source);
        StringBuilder sb = new StringBuilder(source);
        for (ChangePoint change : changes) {
            if (change instanceof RemovalPoint rp) {
                sb.delete(rp.start(), rp.end());
            } else if (change instanceof InsertionPoint ip) {
                if (ip.needsEnd()) {
                    sb.insert(ip.endInsert(), lineSeparator + ip.indent() + endTag);
                }
                if (ip.needsStart()) {
                    sb.insert(ip.startInsert(), ip.indent() + startTag + lineSeparator);
                }
            }
        }
        return sb.toString();
    }

    private CompilationUnit parse(String source) {
        ASTParser parser = CodeGenUtil.EclipseUtil.newASTParser();
        parser.setCompilerOptions(javaCoreOptions);
        parser.setSource(source.toCharArray());
        ASTNode ast = parser.createAST(null);
        return (CompilationUnit)ast;
    }

    @SuppressWarnings("unchecked")
    private List<ChangePoint> collectChangePoints(CompilationUnit cu, String source) {
        List<ChangePoint> changes = new ArrayList<>();
        Map<Integer, Comment> lineCommentIndex = buildLineCommentIndex(cu);
        for (AbstractTypeDeclaration type : (List<AbstractTypeDeclaration>)cu.types()) {
            collectFromType(cu, lineCommentIndex, type, source, changes);
        }
        return changes;
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Comment> buildLineCommentIndex(CompilationUnit cu) {
        Map<Integer, Comment> index = new HashMap<>();
        for (Comment comment : (List<Comment>)cu.getCommentList()) {
            if (comment instanceof LineComment) {
                index.put(cu.getLineNumber(comment.getStartPosition()), comment);
            }
        }
        return index;
    }

    @SuppressWarnings("unchecked")
    private void collectFromType(CompilationUnit cu,
            Map<Integer, Comment> lineCommentIndex,
            AbstractTypeDeclaration type,
            String source,
            List<ChangePoint> changes) {
        if (type instanceof EnumDeclaration enumDecl) {
            for (EnumConstantDeclaration ec : (List<EnumConstantDeclaration>)enumDecl.enumConstants()) {
                var javadocText = javadocText(ec, source);
                if (isGenerated(javadocText)) {
                    addInsertionPointForEnumConstant(cu, lineCommentIndex, ec, source, changes);
                } else if (isNonGenerated(javadocText)) {
                    addRemovalPointsForEnumConstant(cu, lineCommentIndex, ec, source, changes);
                }
            }
        }
        for (BodyDeclaration decl : (List<BodyDeclaration>)type.bodyDeclarations()) {
            if (decl instanceof AbstractTypeDeclaration nested) {
                collectFromType(cu, lineCommentIndex, nested, source, changes);
            } else if (decl instanceof MethodDeclaration || decl instanceof FieldDeclaration) {
                var javadocText = javadocText(decl, source);
                if (isGenerated(javadocText)) {
                    addInsertionPoint(cu, lineCommentIndex, decl, source, changes);
                } else if (isNonGenerated(javadocText)) {
                    addRemovalPoints(cu, lineCommentIndex, decl, source, changes);
                }
            }
        }
    }

    private String javadocText(BodyDeclaration node, String source) {
        Javadoc javadoc = node.getJavadoc();
        if (javadoc == null) {
            return null;
        }
        int start = javadoc.getStartPosition();
        if (start < 0) {
            return null;
        }
        return source.substring(start, start + javadoc.getLength());
    }

    private boolean isGenerated(String javadocText) {
        return javadocText != null && GENERATED_PATTERN.matcher(javadocText).find();
    }

    private boolean isNonGenerated(String javadocText) {
        return javadocText != null
                && ANY_GENERATED_PATTERN.matcher(javadocText).find()
                && !GENERATED_PATTERN.matcher(javadocText).find();
    }

    private void addInsertionPoint(CompilationUnit cu,
            Map<Integer, Comment> lineCommentIndex,
            BodyDeclaration node,
            String source,
            List<ChangePoint> changes) {
        int baseStart = resolveBaseStart(node);
        if (baseStart < 0) {
            return;
        }
        int lineStart = findLineStart(source, baseStart);
        String indent = extractIndent(source, lineStart, baseStart);
        int endInsert = node.getStartPosition() + node.getLength();

        int memberStartLine = cu.getLineNumber(baseStart);
        int memberEndLine = cu.getLineNumber(endInsert - 1);
        int maxLine = source.isEmpty() ? 1 : cu.getLineNumber(source.length() - 1);

        boolean needsStart = findTagBefore(lineCommentIndex, source, memberStartLine - 1, startTag) == null;
        boolean needsEnd = findTagAfter(lineCommentIndex, source, memberEndLine + 1, endTag, maxLine) == null;
        if (needsStart || needsEnd) {
            changes.add(new InsertionPoint(lineStart, endInsert, indent, needsStart, needsEnd));
        }
    }

    private void addInsertionPointForEnumConstant(CompilationUnit cu,
            Map<Integer, Comment> lineCommentIndex,
            EnumConstantDeclaration node,
            String source,
            List<ChangePoint> changes) {
        int baseStart = resolveBaseStart(node);
        if (baseStart < 0) {
            return;
        }
        int lineStart = findLineStart(source, baseStart);
        String indent = extractIndent(source, lineStart, baseStart);
        int pos = findEnumConstantEndPos(node, source);

        int memberStartLine = cu.getLineNumber(baseStart);
        // end tag goes after the line containing the comma/semicolon
        int separatorLine = cu.getLineNumber(pos - 1);
        int maxLine = source.isEmpty() ? 1 : cu.getLineNumber(source.length() - 1);

        boolean needsStart = findTagBefore(lineCommentIndex, source, memberStartLine - 1, startTag) == null;
        boolean needsEnd = findTagAfter(lineCommentIndex, source, separatorLine + 1, endTag, maxLine) == null;
        if (needsStart || needsEnd) {
            changes.add(new InsertionPoint(lineStart, pos, indent, needsStart, needsEnd));
        }
    }

    private void addRemovalPoints(CompilationUnit cu,
            Map<Integer, Comment> lineCommentIndex,
            BodyDeclaration node,
            String source,
            List<ChangePoint> changes) {
        int baseStart = resolveBaseStart(node);
        if (baseStart < 0) {
            return;
        }
        int endInsert = node.getStartPosition() + node.getLength();

        int memberStartLine = cu.getLineNumber(baseStart);
        int memberEndLine = cu.getLineNumber(endInsert - 1);
        int maxLine = source.isEmpty() ? 1 : cu.getLineNumber(source.length() - 1);

        addCommentRemovalIfPresent(findTagBefore(lineCommentIndex, source, memberStartLine - 1, startTag), source,
                changes);
        addCommentRemovalIfPresent(findTagAfter(lineCommentIndex, source, memberEndLine + 1, endTag, maxLine), source,
                changes);
    }

    private void addRemovalPointsForEnumConstant(CompilationUnit cu,
            Map<Integer, Comment> lineCommentIndex,
            EnumConstantDeclaration node,
            String source,
            List<ChangePoint> changes) {
        int baseStart = resolveBaseStart(node);
        if (baseStart < 0) {
            return;
        }
        int pos = findEnumConstantEndPos(node, source);

        int memberStartLine = cu.getLineNumber(baseStart);
        int separatorLine = cu.getLineNumber(pos - 1);
        int maxLine = source.isEmpty() ? 1 : cu.getLineNumber(source.length() - 1);

        addCommentRemovalIfPresent(findTagBefore(lineCommentIndex, source, memberStartLine - 1, startTag), source,
                changes);
        addCommentRemovalIfPresent(findTagAfter(lineCommentIndex, source, separatorLine + 1, endTag, maxLine), source,
                changes);
    }

    private static int resolveBaseStart(BodyDeclaration node) {
        Javadoc javadoc = node.getJavadoc();
        return javadoc != null ? javadoc.getStartPosition() : node.getStartPosition();
    }

    private static int findEnumConstantEndPos(EnumConstantDeclaration node, String source) {
        int pos = node.getStartPosition() + node.getLength();
        while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
            pos++;
        }
        if (pos < source.length() && (source.charAt(pos) == ',' || source.charAt(pos) == ';')) {
            pos++;
        }
        return pos;
    }

    private static void addCommentRemovalIfPresent(Comment comment, String source, List<ChangePoint> changes) {
        if (comment != null) {
            int lineStart = findLineStart(source, comment.getStartPosition());
            int lineEnd = findLineEnd(source, comment.getStartPosition() + comment.getLength());
            changes.add(new RemovalPoint(lineStart, lineEnd));
        }
    }

    /**
     * Searches backwards from {@code lineNumber}, skipping blank lines, for a line comment whose
     * content matches {@code tag}. Returns the comment if found, {@code null} if a non-blank line
     * without the tag is encountered first.
     */
    private static Comment findTagBefore(Map<Integer, Comment> lineCommentIndex,
            String source,
            int lineNumber,
            String tag) {
        String tagContent = commentContent(tag);
        for (int line = lineNumber; line >= 1; line--) {
            Comment found = findCommentOnLine(lineCommentIndex, source, line, tagContent);
            if (found != null) {
                return found;
            }
            if (!getLineContent(source, line).isBlank()) {
                break;
            }
        }
        return null;
    }

    /**
     * Searches forwards from {@code lineNumber}, skipping blank lines, for a line comment whose
     * content matches {@code tag}. Returns the comment if found, {@code null} if a non-blank line
     * without the tag is encountered first.
     */
    private static Comment findTagAfter(Map<Integer, Comment> lineCommentIndex,
            String source,
            int lineNumber,
            String tag,
            int maxLine) {
        String tagContent = commentContent(tag);
        for (int line = lineNumber; line <= maxLine; line++) {
            Comment found = findCommentOnLine(lineCommentIndex, source, line, tagContent);
            if (found != null) {
                return found;
            }
            if (!getLineContent(source, line).isBlank()) {
                break;
            }
        }
        return null;
    }

    private static Comment findCommentOnLine(Map<Integer, Comment> lineCommentIndex,
            String source,
            int lineNumber,
            String tagContent) {
        Comment comment = lineCommentIndex.get(lineNumber);
        if (comment != null) {
            String text = source.substring(comment.getStartPosition(),
                    comment.getStartPosition() + comment.getLength());
            if (commentContent(text).equals(tagContent)) {
                return comment;
            }
        }
        return null;
    }

    private static String getLineContent(String source, int lineNumber) {
        int count = 1;
        int i = 0;
        while (i < source.length() && count < lineNumber) {
            char c = source.charAt(i++);
            if (c == '\n') {
                count++;
            } else if (c == '\r') {
                count++;
                if (i < source.length() && source.charAt(i) == '\n') {
                    i++;
                }
            }
        }
        if (count < lineNumber) {
            return ""; //$NON-NLS-1$
        }
        int end = i;
        while (end < source.length() && source.charAt(end) != '\n' && source.charAt(end) != '\r') {
            end++;
        }
        return source.substring(i, end);
    }

    private static String commentContent(String text) {
        String stripped = text.strip();
        if (stripped.startsWith("//")) { //$NON-NLS-1$
            return stripped.substring(2).strip();
        }
        return stripped;
    }

    private static int findLineStart(String source, int position) {
        int idxN = source.lastIndexOf('\n', position - 1);
        // also handle CR-only line endings (classic Mac OS, pre-OS X)
        int idxR = source.lastIndexOf('\r', position - 1);
        return Math.max(idxN, idxR) + 1;
    }

    private static int findLineEnd(String source, int position) {
        int end = position;
        while (end < source.length() && source.charAt(end) != '\n' && source.charAt(end) != '\r') {
            end++;
        }
        if (end < source.length()) {
            if (source.charAt(end) == '\r') {
                end++;
                if (end < source.length() && source.charAt(end) == '\n') {
                    end++;
                }
            } else {
                end++;
            }
        }
        return end;
    }

    private static String extractIndent(String source, int lineStart, int contentStart) {
        StringBuilder indent = new StringBuilder();
        for (int i = lineStart; i < contentStart && i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == ' ' || c == '\t') {
                indent.append(c);
            } else {
                break;
            }
        }
        return indent.toString();
    }

    private static String detectLineSeparator(String source) {
        int crIdx = source.indexOf('\r');
        if (crIdx >= 0) {
            if (crIdx + 1 < source.length() && source.charAt(crIdx + 1) == '\n') {
                return "\r\n"; //$NON-NLS-1$
            }
            return "\r"; //$NON-NLS-1$
        }
        return "\n"; //$NON-NLS-1$
    }

    private sealed interface ChangePoint permits InsertionPoint, RemovalPoint {
        int sortKey();
    }

    private record InsertionPoint(int startInsert, int endInsert, String indent, boolean needsStart,
            boolean needsEnd) implements ChangePoint {
        @Override
        public int sortKey() {
            return startInsert;
        }
    }

    private record RemovalPoint(int start, int end) implements ChangePoint {
        @Override
        public int sortKey() {
            return start;
        }
    }
}
