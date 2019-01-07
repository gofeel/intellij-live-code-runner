package net.zzid.backendai;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.SearchScopeProvider;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BARunProfileState implements RunProfileState {
    private final ExecutionEnvironment env;
    private final BARunConfiguration runConfiguration;
    private final TextConsoleBuilder consoleBuilder;

    BARunProfileState(BARunConfiguration configuration, ExecutionEnvironment executionEnvironment) {
        env = executionEnvironment;
        runConfiguration = configuration;
        final GlobalSearchScope searchScope = SearchScopeProvider.createSearchScope(env.getProject(), env.getRunProfile());
        consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(env.getProject(), searchScope);
    }

    @Nullable
    @Override
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner programRunner) throws ExecutionException {
        final BAProcessHandler processHandler = startProcess();
        final ConsoleView console = createConsole();

        if (console != null) {
            console.attachToProcess(processHandler);
        }
        processHandler.start(env.getProject());
        return new DefaultExecutionResult(console, processHandler);
    }

    private BAProcessHandler startProcess() throws ExecutionException {
        String endPoint = runConfiguration.getEndPoint();
        String accessKey = runConfiguration.getAccessKey();
        String secretKey = runConfiguration.getSecretKey();

        String kernelType = runConfiguration.getKernelType();
        String buildCmd = runConfiguration.getBuildCmd();
        String execCmd = runConfiguration.getExecCmd();
        String sessionId = runConfiguration.getSessionId();

        return new BAProcessHandler(accessKey, secretKey, kernelType, buildCmd, execCmd, endPoint, sessionId);
    }

    private ConsoleView createConsole() {
        return consoleBuilder != null ? consoleBuilder.getConsole() : null;
    }
}