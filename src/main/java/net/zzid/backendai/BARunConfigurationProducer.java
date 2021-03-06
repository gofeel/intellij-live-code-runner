package net.zzid.backendai;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.util.Ref;
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
        return file != null;
    }

    @Override
    public boolean isConfigurationFromContext(BARunConfiguration runConfiguration, ConfigurationContext configurationContext) {
        return false;
    }
}
