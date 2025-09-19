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
        return found.isEmpty() ? Arrays.asList(node) : findNotSourceRootChild(root, node, found.stream().map(e -> ((CompactedFolderNode) e).getVirtualFile().getPath()).collect(Collectors.toList()), found);
    }

    private List<AbstractTreeNode<?>> findSourceRootChild(PsiDirectoryNode root, AbstractTreeNode<?> node, List<AbstractTreeNode<?>> out) {
        if((node instanceof PsiDirectoryNode)) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            if(ProjectRootsUtil.isModuleContentRoot(dirNode.getVirtualFile(), dirNode.getProject())) {
//                out.add(new CompactedFolderNode(root, dirNode));
                //无法确保该TreeStructureProvider一定在最后执行. 暂时不处理
                return out;
            }
            if(ProjectRootsUtil.isModuleSourceRoot(dirNode.getVirtualFile(), dirNode.getProject())) {
                out.add(new CompactedFolderNode(root, dirNode));
                return out;//不再继续往下找
            }
            for (AbstractTreeNode<?> child : node.getChildren()) {
                findSourceRootChild(root, child, out);
            }
        }
        return out;
    }

    //node:src  compacted:src/main/java --> out src/main/sql...etc
    private static List<AbstractTreeNode<?>> findNotSourceRootChild(PsiDirectoryNode root, AbstractTreeNode<?> node, List<String> compactedPaths, List<AbstractTreeNode<?>> out) {
        for (AbstractTreeNode<?> child : node.getChildren()) {
            if((child instanceof PsiDirectoryNode)) {
                PsiDirectoryNode dirNode = (PsiDirectoryNode) child;
                if(compactedPaths.stream().anyMatch(e -> dirNode.getVirtualFile().getPath().equals(e))) {
                    //skip compacted source folders
                } else if(compactedPaths.stream().anyMatch(e -> CompactedFolderNode.isParent(dirNode.getVirtualFile().getPath(), e))) {
                    findNotSourceRootChild(root, child, compactedPaths, out);//parentOf compacted source folders, compact child similar as source folders
                } else {
                    out.add(new CompactedFolderNode(root, dirNode));
                }
            }
        }
        return out;
    }
}
