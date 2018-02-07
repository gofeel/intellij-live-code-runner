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
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final String KERNEL_TYPE_KEY = "kernelType";
    private static final String BUILD_CMD_KEY = "buildCmd";
    private static final String EXEC_CMD_KEY = "execCmd";

    private String accessKey;
    private String secretKey;
    private String kernelType = "auto";
    private String buildCmd = "*";
    private String execCmd = "*";

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
        String accessKey = element.getAttributeValue(ACCESS_KEY);
        String secretKey = element.getAttributeValue(SECRET_KEY);
        String kernelType = element.getAttributeValue(KERNEL_TYPE_KEY);
        String execCmd = element.getAttributeValue(BUILD_CMD_KEY);
        String buildCmd = element.getAttributeValue(EXEC_CMD_KEY);

        if (!StringUtil.isEmpty(accessKey)) {
            this.accessKey = accessKey;
        }
        if (!StringUtil.isEmpty(secretKey)) {
            this.secretKey = secretKey;
        }
        if (!StringUtil.isEmpty(kernelType)) {
            this.kernelType = kernelType;
        }
        if (!StringUtil.isEmpty(execCmd)) {
            this.execCmd = execCmd;
        }
        if (!StringUtil.isEmpty(buildCmd)) {
            this.buildCmd = buildCmd;
        }
    }
    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        if (!StringUtil.isEmpty(accessKey)) {
            element.setAttribute(ACCESS_KEY, accessKey);
        }
        if (!StringUtil.isEmpty(secretKey)) {
            element.setAttribute(SECRET_KEY, secretKey);
        }
        if (!StringUtil.isEmpty(kernelType)) {
            element.setAttribute(KERNEL_TYPE_KEY, kernelType);
        }
        if (!StringUtil.isEmpty(buildCmd)) {
            element.setAttribute(BUILD_CMD_KEY, buildCmd);
        }
        if (!StringUtil.isEmpty(execCmd)) {
            element.setAttribute(EXEC_CMD_KEY, execCmd);
        }
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

    public String getBuildCmd() {
        return buildCmd;
    }

    public String getExecCmd() {
        return execCmd;
    }

    public void setBuildCmd(String buildCmd) {
        this.buildCmd = buildCmd;
    }

    public void setExecCmd(String execCmd) {
        this.execCmd = execCmd;
    }
}