package net.zzid.backendai;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class BARunConfigurationProducer extends RunConfigurationProducer<BARunConfiguration> {
    protected BARunConfigurationProducer(ConfigurationType configurationType) {
        super(configurationType);
    }
    public BARunConfigurationProducer() {
        super(new BAConfigurationType());
    }

    @Override
    protected boolean setupConfigurationFromContext(BARunConfiguration runConfiguration, ConfigurationContext configurationContext, Ref ref) {
        PsiElement elem = configurationContext.getPsiLocation();

        PsiFile file = elem != null ? elem.getContainingFile() : null;
        if (file == null) return false;
        VirtualFile vFile = file.getVirtualFile();
        String scriptPath = vFile != null ? file.getVirtualFile().getPath() : null;
        if (scriptPath != null) {
            runConfiguration.setScriptPath(scriptPath);
            String[] parts = scriptPath.split("/");
            if (parts.length > 0) {
                runConfiguration.setName(parts[parts.length - 1]);
            }
        }
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(BARunConfiguration runConfiguration, ConfigurationContext configurationContext) {
        PsiElement elem = configurationContext.getPsiLocation();
        PsiFile file = elem != null ? elem.getContainingFile() : null;
        if (file == null) return false;
        VirtualFile currentFile = file.getVirtualFile();

        boolean isSameFile = false;
        if (currentFile != null) {
            isSameFile = currentFile.getPath().equals(runConfiguration.getScriptPath());
        }
        return isSameFile;
    }
}
