package net.zzid.backendai;

import ai.backend.client.ClientConfig;
import ai.backend.client.Kernel;
import ai.backend.client.exceptions.AuthorizationFailureException;
import ai.backend.client.exceptions.ConfigurationException;
import ai.backend.client.exceptions.NetworkFailureException;
import ai.backend.client.exceptions.ResourceLimitException;
import ai.backend.client.values.ExecutionMode;
import ai.backend.client.values.ExecutionResult;
import ai.backend.client.values.RunStatus;
import com.google.gson.JsonObject;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class BAProcessHandler extends ProcessHandler {

    private String sessionId;
    private PipedInputStream  inputStream;
    private final PipedOutputStream outputStream;

    private final String accessKey;
    private final String secretKey;
    private final String endPoint;

    private final String buildCmd;
    private final String execCmd;
    private final String kernelType;

    private Kernel kernel;


    public BAProcessHandler(String accessKey, String secretKey, String kernelType, String buildCmd, String execCmd, String endPoint) {
        super();
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endPoint = endPoint;

        this.buildCmd = buildCmd;
        this.execCmd = execCmd;

        this.kernelType = kernelType;
        outputStream = new PipedOutputStream();
        this.sessionId = null;
        try {
            inputStream  = new PipedInputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void destroyProcessImpl() {
        if(kernel != null) {
            interruptKernel();
        }
    }

    @Override
    protected void detachProcessImpl() {
        if(kernel != null) {
            this.notifyTextAvailable(String.format("\nStopped.", kernel.getId()), ProcessOutputTypes.SYSTEM);
            terminateProcess();
        }
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Nullable
    @Override
    public OutputStream getProcessInput() {
        return outputStream;
    }

    @Override
    public boolean isProcessTerminated() {
        return super.isProcessTerminated();
    }

    public void start(Project project) {
        ClientConfig config;
        final Map<String, String> files;
        try {
            files = scanFiles(project);
        } catch (IOException e) {
            this.notifyTextAvailable("\nFile Error : Check path of your files.", ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        }

        if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            this.notifyTextAvailable("\nBackend AI Error : Configuration failed. Check your keys.", ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        }
        try {
            config = new ClientConfig.Builder().accessKey(accessKey).secretKey(secretKey).endPoint(endPoint).build();
        } catch (ConfigurationException e) {
            this.notifyTextAvailable(String.format("\nBackend AI Error : Configuration failed. Check your keys."), ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        }

        if (this.kernelType.equals("unknown")) {
            this.notifyTextAvailable(String.format("\nBackend AI Error : Can't find a suitable kernel."), ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        }

        try {
            kernel = Kernel.getOrCreateInstance(this.sessionId, this.kernelType, config);
            this.sessionId = kernel.getId();
        } catch (NetworkFailureException e) {
            this.notifyTextAvailable(String.format("\nBackend AI Error : Network error"), ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        } catch (AuthorizationFailureException e) {
            this.notifyTextAvailable(String.format("\nBackend AI Error : Authorization failed. Check your keys."), ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        } catch (ResourceLimitException e) {
            this.notifyTextAvailable(String.format("\nBackend AI Error : Resource is full. Check your limit for backend ai service."), ProcessOutputTypes.SYSTEM);
            terminateProcess();
        }
        this.notifyTextAvailable(String.format("Backend AI Info : %s Kernel is ready : %s\n", kernelType, kernel.getId()), ProcessOutputTypes.SYSTEM);
        this.notifyTextAvailable(String.format("Backend AI Info : Target Files: %d\n", files.size()), ProcessOutputTypes.SYSTEM);
        Runnable task = () -> {
            kernel.upload(files);
            this.notifyTextAvailable(String.format("Backend AI Info : Build Script - %s\n", this.buildCmd), ProcessOutputTypes.SYSTEM);
            this.notifyTextAvailable(String.format("Backend AI Info : Execution Script - %s\n", this.execCmd), ProcessOutputTypes.SYSTEM);
            runCode(kernel, this.buildCmd, this.execCmd);
        };
        new Thread(task).start();
    }

    private Map<String, String> scanFiles(Project project) throws IOException {
        List<VirtualFile> files = new ArrayList<>();
        VirtualFile[] vFiles = ProjectRootManager.getInstance(project).getContentRoots();
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        Queue<VirtualFile> queue = new LinkedList<>(Arrays.asList(vFiles));
        HashMap<String, String> result = new HashMap<>();
        String basepath = FilenameUtils.normalize(project.getBasePath() + File.separatorChar);

        while (!queue.isEmpty()) {
            VirtualFile file = queue.remove();
            for (VirtualFile f : file.getChildren()) {

                if (projectFileIndex.isUnderIgnored(f) ||
                        projectFileIndex.isExcluded(f) ||
                        f.getFileType().getName().startsWith("IDEA_") ||
                        f.isDirectory() && f.getName().equals(".idea")) {
                    continue;
                }

                if (f.isDirectory() && !projectFileIndex.isUnderIgnored(f) && !projectFileIndex.isExcluded(f)) {
                    queue.add(f);
                    continue;
                }
                files.add(f);
            }
        }
        for (VirtualFile file : files) {
            result.put(Utils.getUnixRelativePath(basepath, file.getPath()), file.getPath());
        }
        return result;
    }

    public void runCode(Kernel kernel, String buildCmd, String execCmd) {
        if(this.kernel == null) {
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));

        ExecutionMode mode = ExecutionMode.BATCH;
        String runId = Kernel.generateRunId();
        String code = "";
        while (true) {
            JsonObject opts = new JsonObject();
            opts.addProperty("build", buildCmd);
            opts.addProperty("exec", execCmd);

            ExecutionResult result = kernel.execute(mode, runId, code, opts);

            this.notifyTextAvailable(result.getStdout(), ProcessOutputTypes.STDOUT);
            this.notifyTextAvailable(result.getStderr(), ProcessOutputTypes.STDERR);

            if (result.isFinished()) {
                this.notifyTextAvailable(String.format("\nProcess Finished"), ProcessOutputTypes.SYSTEM);
                terminateProcess();
                break;
            }
            if (result.getStatus() == RunStatus.WAITING_INPUT) {
                try {
                    code = reader.readLine();
                } catch (IOException e) {
                    code = "<user-input error>";
                }
            } else {
                code = "";
            }
        }
    }

    private void interruptKernel() {
        Runnable task = () -> {
            kernel.interrupt();
            terminateProcess();
        };
        new Thread(task).start();
    }

    private void terminateProcess() {
        this.notifyProcessTerminated(0);
        kernel = null;
    }
}
