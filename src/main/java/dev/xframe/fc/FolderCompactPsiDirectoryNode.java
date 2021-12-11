package dev.xframe.fc;

import com.intellij.history.core.Paths;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class FolderCompactPsiDirectoryNode extends PsiDirectoryNode  {

    public FolderCompactPsiDirectoryNode(PsiDirectoryNode origin) {
        super(origin.getProject(), origin.getValue(), origin.getSettings(), origin.getFilter());
    }

    @Override
    protected void updateImpl(@NotNull PresentationData data) {
        super.updateImpl(data);
        //set text like src/main/java...etc
        data.setPresentableText(Paths.relativeIfUnder(getVirtualFile().getPath(), ProjectFileIndex.getInstance(getValue().getProject()).getContentRootForFile(getVirtualFile()).getPath()));
    }

}
