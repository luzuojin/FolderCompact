package dev.xframe.fc;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.AbstractModuleNode;
import com.intellij.ide.projectView.impl.nodes.LibraryGroupNode;
import com.intellij.ide.projectView.impl.nodes.NamedLibraryElement;
import com.intellij.ide.projectView.impl.nodes.NamedLibraryElementNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JdkOrderEntry;
import com.intellij.openapi.roots.LibraryOrSdkOrderEntry;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleSourceOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ModuleLibrariesNode extends ProjectViewNode<String> {
    private final Module module;
    public ModuleLibrariesNode(PsiDirectoryNode moduleRoot, ViewSettings viewSettings) {
        this(ProjectRootManager.getInstance(moduleRoot.getProject()).getFileIndex().getModuleForFile(moduleRoot.getVirtualFile()), viewSettings);
    }
    public ModuleLibrariesNode(Module module, ViewSettings viewSettings) {
        super(module.getProject(), UUID.randomUUID().toString(), viewSettings);
        this.module = module;
    }
    @Override
    public boolean contains(@NotNull VirtualFile file) {
        return someChildContainsFile(file, false);
    }
    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode<?>> getChildren() {
        return createModuleLibraries(module, getSettings(), false);
    }
    @Override
    protected void update(PresentationData presentation) {
        presentation.setPresentableText("Libraries");
        presentation.setIcon(PlatformIcons.LIBRARY_ICON);
    }
    @Override
    public boolean equals(Object object) {
        return this == object;
    }
    @Override
    public boolean canRepresent(Object element) {
        return false;
    }

    //module libraries
    public static Collection<? extends AbstractTreeNode<?>> createModuleLibraries(Module module, ViewSettings settings, boolean onlyExternal) {
        List<AbstractTreeNode<?>> children = new ArrayList<>();
        Project project = module.getProject();
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        final OrderEntry[] orderEntries = ModuleRootManager.getInstance(module).getOrderEntries();
        for (OrderEntry entry : orderEntries) {
            if (entry instanceof LibraryOrderEntry) {
                LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) entry;
                final Library library = libraryOrderEntry.getLibrary();
                if (library == null) {
                    continue;
                }
                if (onlyExternal && !hasExternalEntries(fileIndex, libraryOrderEntry)) {
                    continue;
                }
                final String libraryName = library.getName();
                if (libraryName == null || libraryName.length() == 0) {
                    addLibraryChildren(libraryOrderEntry, children, project, settings);
                } else {
                    children.add(new NamedLibraryElementNode(project, new NamedLibraryElement(module, libraryOrderEntry), settings));
                }
            } else if (entry instanceof JdkOrderEntry) {
                JdkOrderEntry jdkOrderEntry = (JdkOrderEntry) entry;
                if (jdkOrderEntry.getJdk() != null) {
                    children.add(new NamedLibraryElementNode(project, new NamedLibraryElement(module, jdkOrderEntry), settings));
                }
            }
        }
        for (Module dependency : ModuleRootManager.getInstance(module).getDependencies()) {
            children.add(new ModuleDependenceNode(project, dependency, settings));
        }
        return children;
    }
    static boolean hasExternalEntries(ProjectFileIndex index, LibraryOrderEntry orderEntry) {
        VirtualFile[] files = orderEntry.getRootFiles(OrderRootType.CLASSES);
        for (VirtualFile file : files) {
            if (!index.isInContent(VfsUtil.getLocalFile(file))) return true;
        }
        return false;
    }
    public static void addLibraryChildren(final LibraryOrderEntry entry, final List<AbstractTreeNode<?>> children, Project project, ViewSettings settings) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        final VirtualFile[] files = entry.getRootFiles(OrderRootType.CLASSES);
        for (final VirtualFile file : files) {
            final PsiDirectory psiDir = psiManager.findDirectory(file);
            if (psiDir == null) {
                continue;
            }
            children.add(new FixedPsiDirectoryNode(project, psiDir, settings));
        }
    }

    public static class FixedPsiDirectoryNode extends PsiDirectoryNode {
        public FixedPsiDirectoryNode(Project project, PsiDirectory value, ViewSettings viewSettings) {
            super(project, value, viewSettings);
        }
        @Override
        public Collection<AbstractTreeNode<?>> getChildrenImpl() {
            VirtualFile virtualFile = getVirtualFile();
            if (virtualFile != null && !virtualFile.isInLocalFileSystem() && getParent() instanceof PsiDirectoryNode) {
                PsiDirectoryNode parent = (PsiDirectoryNode) getParent();
                VirtualFile file = parent.getVirtualFile();
                if (file == null || !file.isInLocalFileSystem()) {
                    return Collections.emptyList();
                }
            }
            Collection<AbstractTreeNode<?>> children = super.getChildrenImpl();
            if (children == null) {
                return Collections.emptyList();
            }
            List<AbstractTreeNode<?>> wrappedNodes = new ArrayList<>();
            for (AbstractTreeNode<?> node : children) {
                wrappedNodes.add(wrapNode(node));
            }
            return wrappedNodes;
        }
        private AbstractTreeNode<?> wrapNode(AbstractTreeNode<?> node) {
            if (node instanceof PsiDirectoryNode) {
                return new FixedPsiDirectoryNode(node.getProject(), ((PsiDirectoryNode) node).getValue(), ((PsiDirectoryNode) node).getSettings());
            }
            if (node instanceof PsiFileNode) {
                return new FixedPsiFileNode(node.getProject(), ((PsiFileNode) node).getValue(), ((PsiFileNode) node).getSettings());
            }
            return node;
        }
        @Override
        public boolean contains(@NotNull VirtualFile file) {
            return file.isInLocalFileSystem() && super.contains(file);
        }
        @Override
        public boolean canRepresent(Object element) {
            return false;
        }
        @Override
        public boolean equals(Object object) {
            if (getVirtualFile() != null && getVirtualFile().isInLocalFileSystem()) {
                return super.equals(object);
            }
            return this == object;
        }
    }
    /**
     * A psi file node which is only equal to itself.
     * This avoids the selection jumping in the tree while opening and closing packages of library dependencies.
     */
    public static class FixedPsiFileNode extends PsiFileNode {
        public FixedPsiFileNode(Project project, PsiFile value, ViewSettings viewSettings) {
            super(project, value, viewSettings);
        }
        @Override
        public Collection<AbstractTreeNode<?>> getChildrenImpl() {
            return Collections.emptyList();
        }
        @Override
        public boolean equals(Object object) {
            return super.equals(object);
        }
        @Override
        public boolean contains(@NotNull VirtualFile file) {
            return false;
        }
        @Override
        public boolean canRepresent(Object element) {
            return false; //this == element;
        }
    }

    public static class ModuleDependenceNode extends AbstractModuleNode {
        protected ModuleDependenceNode(Project project, @NotNull Module module, ViewSettings viewSettings) {
            super(project, module, viewSettings);
        }
        @Override
        public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
            return Collections.emptyList();
        }
        @Override
        protected boolean showModuleNameInBold() {
            return false;
        }
    }
}
