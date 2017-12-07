package net.zzid.backendai;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BARunConfiguration extends LocatableConfigurationBase {
    private static final String SCRIPT_PATH_URL = "scriptUrl";
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final String KERNEL_TYPE_KEY = "kernelType";

    private String scriptPath;
    private String accessKey;
    private String secretKey;
    private String kernelType = "auto";

    protected BARunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }
    @NotNull
    @Override
    public SettingsEditor<BARunConfiguration> getConfigurationEditor() {
        return new BARunConfigurationEditor(getProject(), this);
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return new BARunProfileState(this, executionEnvironment);
    }


    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        String scriptUrl = element.getAttributeValue(SCRIPT_PATH_URL);
        String accessKey = element.getAttributeValue(ACCESS_KEY);
        String secretKey = element.getAttributeValue(SECRET_KEY);
        String kernelType = element.getAttributeValue(KERNEL_TYPE_KEY);

        if (!StringUtil.isEmpty(scriptUrl)) {
            scriptPath = scriptUrl;
        }
        if (!StringUtil.isEmpty(accessKey)) {
            this.accessKey = accessKey;
        }
        if (!StringUtil.isEmpty(secretKey)) {
            this.secretKey = secretKey;
        }
        if (!StringUtil.isEmpty(kernelType)) {
            this.kernelType = kernelType;
        }
    }
    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        if (!StringUtil.isEmpty(scriptPath)) {
            element.setAttribute(SCRIPT_PATH_URL, scriptPath);
        }
        if (!StringUtil.isEmpty(accessKey)) {
            element.setAttribute(ACCESS_KEY, accessKey);
        }
        if (!StringUtil.isEmpty(secretKey)) {
            element.setAttribute(SECRET_KEY, secretKey);
        }
        if (!StringUtil.isEmpty(kernelType)) {
            element.setAttribute(KERNEL_TYPE_KEY, kernelType);
        }
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getKernelType() {
        return kernelType;
    }

    public void setKernelType(String kernelType) {
        this.kernelType = kernelType;
    }
}