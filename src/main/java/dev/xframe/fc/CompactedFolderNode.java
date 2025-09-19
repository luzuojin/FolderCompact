package dev.xframe.fc;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

public class CompactedFolderNode extends PsiDirectoryNode {
    final PsiDirectoryNode root;
    public CompactedFolderNode(PsiDirectoryNode root, PsiDirectoryNode origin) {
        super(origin.getProject(), origin.getValue(), origin.getSettings(), origin.getFilter());
        this.root = root;
    }
    @Override
    protected void updateImpl(@NotNull PresentationData data) {
        super.updateImpl(data);
        //set text like src/main/java...etc
        data.setPresentableText(relativeIfUnder(getVirtualFile().getPath(), root.getVirtualFile().getPath()));
    }

    /* copy from com.intellij.history.core.Paths */
    private static boolean myIsCaseSensitive = SystemInfo.isFileSystemCaseSensitive;
    public static String relativeIfUnder(String path, String root) {
        if (!isParent(root, path)) {
            return null;
        } else {
            path = path.substring(root.length());
            if (path.length() == 0) {
                return "";
            } else {
                return path.charAt(0) != '/' ? null : path.substring(1);
            }
        }
    }
    public static boolean isParent(String parent, String path) {
        if (equals(parent, path)) {
            return true;
        } else {
            parent = appendParent(parent);
            return myIsCaseSensitive ? path.startsWith(parent) : StringUtil.startsWithIgnoreCase(path, parent);
        }
    }
    public static boolean equals(String p1, String p2) {
        return myIsCaseSensitive ? p1.equals(p2) : p1.equalsIgnoreCase(p2);
    }
    private static String appendParent(String parent) {
        if (parent.isEmpty()) {
            return parent;
        } else {
            if (parent.charAt(parent.length() - 1) != '/') {
                parent = parent + "/";
            }

            return parent;
        }
    }
}
