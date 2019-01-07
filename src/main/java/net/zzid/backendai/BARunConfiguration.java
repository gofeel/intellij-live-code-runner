package net.zzid.backendai;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
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
    private static final String ENDPOINT_KEY = "endPoint";
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final String KERNEL_TYPE_KEY = "kernelType";
    private static final String BUILD_CMD_KEY = "buildCmd";
    private static final String EXEC_CMD_KEY = "execCmd";

    private String endPoint = "https://api.backend.ai";
    private String accessKey;
    private String secretKey;
    private String kernelType = "auto";
    private String buildCmd = "*";
    private String execCmd = "*";
    private String sessionId = null;

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
        String endPoint = element.getAttributeValue(ENDPOINT_KEY);
        String accessKey = element.getAttributeValue(ACCESS_KEY);
        String secretKey = element.getAttributeValue(SECRET_KEY);
        String kernelType = element.getAttributeValue(KERNEL_TYPE_KEY);
        String execCmd = element.getAttributeValue(EXEC_CMD_KEY);
        String buildCmd = element.getAttributeValue(BUILD_CMD_KEY);
        if (!StringUtil.isEmpty(endPoint)) {
            this.endPoint = endPoint;
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
        if (!StringUtil.isEmpty(endPoint)) {
            element.setAttribute(ENDPOINT_KEY, endPoint);
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
        if (!StringUtil.isEmpty(buildCmd)) {
            element.setAttribute(BUILD_CMD_KEY, buildCmd);
        }
        if (!StringUtil.isEmpty(execCmd)) {
            element.setAttribute(EXEC_CMD_KEY, execCmd);
        }
    }
    String getEndPoint() {
        return endPoint;
    }

    void setEndPoint(String endPoint) {
        updateSessionId();

        this.endPoint = endPoint;
    }

    String getAccessKey() {
        return accessKey;
    }

    void setAccessKey(String accessKey) {
        updateSessionId();
        this.accessKey = accessKey;
    }

    String getSecretKey() {
        return secretKey;
    }

    void setSecretKey(String secretKey) {
        updateSessionId();
        this.secretKey = secretKey;
    }

    String getKernelType() {
        return kernelType;
    }

    void setKernelType(String kernelType) {
        updateSessionId();
        this.kernelType = kernelType;
    }

    String getBuildCmd() {
        return buildCmd;
    }

    String getExecCmd() {
        return execCmd;
    }

    void setBuildCmd(String buildCmd) {
        this.buildCmd = buildCmd;
    }

    void setExecCmd(String execCmd) {
        this.execCmd = execCmd;
    }

    void updateSessionId() {
        sessionId = java.util.UUID.randomUUID().toString();

    }
    String getSessionId() {
        if(sessionId == null) {
            updateSessionId();
        }
        return sessionId;
    }
}
