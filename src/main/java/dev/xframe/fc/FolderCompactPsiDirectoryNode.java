package dev.xframe.fc;

import com.intellij.history.core.Paths;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import org.jetbrains.annotations.NotNull;

public class FolderCompactPsiDirectoryNode extends PsiDirectoryNode  {

    final PsiDirectoryNode root;

    public FolderCompactPsiDirectoryNode(PsiDirectoryNode root, PsiDirectoryNode origin) {
        super(origin.getProject(), origin.getValue(), origin.getSettings(), origin.getFilter());
        this.root = root;
    }

    @Override
    protected void updateImpl(@NotNull PresentationData data) {
        super.updateImpl(data);
        //set text like src/main/java...etc
        data.setPresentableText(Paths.relativeIfUnder(getVirtualFile().getPath(), root.getVirtualFile().getPath()));
    }

}
