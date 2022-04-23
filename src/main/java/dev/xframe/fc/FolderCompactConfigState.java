package dev.xframe.fc;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "dev.xframe.fc.FolderCompactConfigState",
        storages = @Storage("FolderCompactPlugin.xml")
)
public class FolderCompactConfigState implements PersistentStateComponent<FolderCompactConfigState> {

    public static FolderCompactConfigState getInstance(Project project) {
        return project.getService(FolderCompactConfigState.class);
    }
    public static FolderCompactConfigState getDefaultInstance() {
        return getInstance(ProjectManager.getInstance().getDefaultProject());
    }

    private boolean compactSourceFolder = true;
    private boolean showModuleLibraries = false;

    public boolean getCompactSourceFolder() {
        return compactSourceFolder;
    }
    public void setCompactSourceFolder(boolean compactSourceFolder) {
        this.compactSourceFolder = compactSourceFolder;
    }
    public boolean getShowModuleLibraries() {
        return showModuleLibraries;
    }
    public void setShowModuleLibraries(boolean showModuleLibraries) {
        this.showModuleLibraries = showModuleLibraries;
    }

    @Nullable
    @Override
    public FolderCompactConfigState getState() {
        return this;
    }
    @Override
    public void loadState(@NotNull FolderCompactConfigState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
