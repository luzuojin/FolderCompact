package dev.xframe.fc;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleOptionAction;
import com.intellij.openapi.actionSystem.ToggleOptionAction.Option;
import com.intellij.openapi.project.Project;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FolderCompactActions {

    static class FolderCompactOptionSupplier implements Function<AnActionEvent, Option> {
        final Function<FolderCompactConfigState, Boolean> getter;
        final BiConsumer<FolderCompactConfigState, Boolean> setter;
        public FolderCompactOptionSupplier(Function<FolderCompactConfigState, Boolean> getter, BiConsumer<FolderCompactConfigState, Boolean> setter) {
            this.getter = getter;
            this.setter = setter;
        }
        @Override
        public Option apply(AnActionEvent o) {
            Project project = o.getProject();
            FolderCompactConfigState state = FolderCompactConfigState.getInstance(project);
            return new Option() {
                @Override
                public boolean isSelected() {
                    return getter.apply(state);
                }
                @Override
                public void setSelected(boolean selected) {
                    boolean update = isSelected() != selected;
                    setter.accept(state, selected);
                    if(update) ProjectView.getInstance(project).refresh();
                }
            };
        }
    }
    public static class CompactSourceFolder extends ToggleOptionAction {
        public CompactSourceFolder() {
            super(new FolderCompactOptionSupplier(
                    FolderCompactConfigState::getCompactSourceFolder,
                    FolderCompactConfigState::setCompactSourceFolder));
        }
    }
    public static class ShowModuleLibraries extends ToggleOptionAction {
        public ShowModuleLibraries() {
            super(new FolderCompactOptionSupplier(
                    FolderCompactConfigState::getShowModuleLibraries,
                    FolderCompactConfigState::setShowModuleLibraries));
        }
    }
}
