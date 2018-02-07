package net.zzid.backendai;

import com.intellij.execution.*;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.SearchScopeProvider;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BARunProfileState implements RunProfileState {
    private final ExecutionEnvironment env;
    private final BARunConfiguration runConfiguration;
    private TextConsoleBuilder myConsoleBuilder;

    public BARunProfileState(BARunConfiguration runConfiguration, ExecutionEnvironment executionEnvironment) {
        this.env = executionEnvironment;
        final GlobalSearchScope searchScope = SearchScopeProvider.createSearchScope(env.getProject(), env.getRunProfile());
        myConsoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(env.getProject(), searchScope);
        this.runConfiguration = runConfiguration;

    }

    @Nullable
    @Override
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner programRunner) throws ExecutionException {
        final BAProcessHandler processHandler = startProcess();
        final ConsoleView console = createConsole(executor);

        if (console != null) {
            console.attachToProcess(processHandler);
        }
        Project p = this.env.getProject();

        processHandler.start(p);
        return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler, executor));
    }

    protected BAProcessHandler startProcess() throws ExecutionException {
        String accessKey = runConfiguration.getAccessKey();
        String secretKey = runConfiguration.getSecretKey();
        String kernelType = runConfiguration.getKernelType();
        String buildCmd = runConfiguration.getBuildCmd();
        String execCmd = runConfiguration.getExecCmd();

        BAProcessHandler baProcessHandler = new BAProcessHandler(accessKey, secretKey, kernelType, buildCmd, execCmd);
        return baProcessHandler;
    }

    protected ConsoleView createConsole(@NotNull final Executor executor) throws ExecutionException {
        TextConsoleBuilder builder = getConsoleBuilder();
        return builder != null ? builder.getConsole() : null;
    }

    public TextConsoleBuilder getConsoleBuilder() {
        return myConsoleBuilder;
    }

    @NotNull
    protected AnAction[] createActions(final ConsoleView console, final ProcessHandler processHandler) {
        return createActions(console, processHandler, null);
    }

    @NotNull
    protected AnAction[] createActions(final ConsoleView console, final ProcessHandler processHandler, Executor executor) {
        if (console == null || !console.canPause() || (executor != null && !DefaultRunExecutor.EXECUTOR_ID.equals(executor.getId()))) {
            return AnAction.EMPTY_ARRAY;
        }
        return new AnAction[]{new PauseOutputAction(console, processHandler)};
    }

    protected static class PauseOutputAction extends ToggleAction implements DumbAware {
        private final ConsoleView myConsole;
        private final ProcessHandler myProcessHandler;

        public PauseOutputAction(final ConsoleView console, final ProcessHandler processHandler) {
            super(ExecutionBundle.message("run.configuration.pause.output.action.name"), null, AllIcons.Actions.Pause);
            myConsole = console;
            myProcessHandler = processHandler;
        }

        @Override
        public boolean isSelected(final AnActionEvent event) {
            return myConsole.isOutputPaused();
        }

        @Override
        public void setSelected(final AnActionEvent event, final boolean flag) {
            myConsole.setOutputPaused(flag);
            ApplicationManager.getApplication().invokeLater(() -> update(event));
        }

        @Override
        public void update(@NotNull final AnActionEvent event) {
            super.update(event);
            final Presentation presentation = event.getPresentation();
            final boolean isRunning = myProcessHandler != null && !myProcessHandler.isProcessTerminated();
            if (isRunning) {
                presentation.setEnabled(true);
            }
            else {
                if (!myConsole.canPause()) {
                    presentation.setEnabled(false);
                    return;
                }
                if (!myConsole.hasDeferredOutput()) {
                    presentation.setEnabled(false);
                }
                else {
                    presentation.setEnabled(true);
                    myConsole.performWhenNoDeferredOutput(() -> update(event));
                }
            }
        }
    }
}