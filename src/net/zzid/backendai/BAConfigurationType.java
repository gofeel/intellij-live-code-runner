package net.zzid.backendai;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class BAConfigurationType extends ConfigurationTypeBase {
    public BAConfigurationType(){
        super("BackendAIConfigurationType", "BackendAI", "Remote Backend AI run configuration", IconLoader.getIcon("/icons/icon.png"));
        addFactory(new ConfigurationFactory(this) {
            @Override
            public boolean isConfigurationSingletonByDefault() {
                return true;
            }

            @Override
            public boolean canConfigurationBeSingleton() {
                return false;
            }

            @NotNull
            @Override
            public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new BARunConfiguration(project, this, " Template config");
            }
        });
    }
}
