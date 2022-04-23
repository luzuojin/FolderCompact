package dev.xframe.fc;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FolderCompactTreeStructureProvider implements TreeStructureProvider {

    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        if(parent instanceof PsiDirectoryNode &&
                ProjectRootsUtil.isModuleContentRoot(((PsiDirectoryNode) parent).getValue())) {
            FolderCompactConfigState fcState = FolderCompactConfigState.getInstance(parent.getProject());
            if(fcState.getShowModuleLibraries()) {
                children.add(new ModuleLibrariesNode((PsiDirectoryNode) parent, settings));
            }
            if(fcState.getCompactSourceFolder()) {
                return children.stream().map(child->compact((PsiDirectoryNode) parent, child)).flatMap(List::stream).collect(Collectors.toList());
            }
        }
        return children;
    }

    private List<AbstractTreeNode<?>> compact(PsiDirectoryNode root, AbstractTreeNode<?> node) {
        List<AbstractTreeNode<?>> found = findSourceRootChild(root, node, new ArrayList<>());
        return found.isEmpty() ? Arrays.asList(node) : found;
    }

    private List<AbstractTreeNode<?>> findSourceRootChild(PsiDirectoryNode root, AbstractTreeNode<?> node, List<AbstractTreeNode<?>> out) {
        if((node instanceof PsiDirectoryNode)) {
            PsiDirectoryNode dir = (PsiDirectoryNode) node;
            if(ProjectRootsUtil.isModuleSourceRoot(dir.getVirtualFile(), dir.getProject())) {
                out.add(new CompactedFolderNode(root, dir));
                return out;//不再继续往下找
            }
            for (AbstractTreeNode<?> child : node.getChildren()) {
                findSourceRootChild(root, child, out);
            }
        }
        return out;
    }

}
