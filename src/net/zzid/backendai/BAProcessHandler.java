package net.zzid.backendai;

import ai.backend.client.Config;
import ai.backend.client.Kernel;
import ai.backend.client.RunResult;
import ai.backend.client.exceptions.AuthorizationFailException;
import ai.backend.client.exceptions.ConfigurationException;
import ai.backend.client.exceptions.NetworkFailException;
import ai.backend.client.values.RunStatus;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class BAProcessHandler extends ProcessHandler {
    private final String secretKey;
    private String code;
    final PipedOutputStream outputStream;
    PipedInputStream  inputStream;
    private Kernel kernel;
    private String accessKey;
    private final String kernelType;

    public BAProcessHandler(String code, String accessKey, String secretKey, String kernelType) {
        super();
        this.code = code;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.kernelType = kernelType;
        outputStream = new PipedOutputStream();

        try {
            inputStream  = new PipedInputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void destroyProcessImpl() {
        if(kernel != null) {
            interuptKernel();
        }
    }

    @Override
    protected void detachProcessImpl() {
        if(kernel != null) {
            this.notifyTextAvailable(String.format("\nStopped.", kernel.getId()), ProcessOutputTypes.SYSTEM);
            stopKernel();
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

    public void start() {
        Config config;
        if(accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            this.notifyTextAvailable("Backend AI Error : Configuration failed. Check your keys.", ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        }
        try {
            config = new Config.Builder().accessKey(accessKey).secretKey(secretKey).build();
        } catch (ConfigurationException e) {
            this.notifyTextAvailable(String.format("\nBackend AI Error : Configuration failed. Check your keys."), ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        }

        if(this.kernelType.equals("unknown")) {
            this.notifyTextAvailable(String.format("\nBackend AI Error : Can't find a suitable kernel."), ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        }
        try {
            kernel = Kernel.getOrCreateInstance(null, this.kernelType, config);
        } catch (NetworkFailException e) {
            this.notifyTextAvailable(String.format("\nBackend AI Error : Network error"), ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        } catch (AuthorizationFailException e) {
            this.notifyTextAvailable(String.format("\nBackend AI Error : Authorization failed. Check your keys."), ProcessOutputTypes.SYSTEM);
            terminateProcess();
            return;
        }
        this.notifyTextAvailable(String.format("Backend AI Info : %s Kernel is ready : %s\n", kernelType, kernel.getId()), ProcessOutputTypes.SYSTEM);
        runCode(code);
    }


    public void runCode(String code) {
        if(kernel == null) {
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));

        RunResult result = kernel.runCode(code);
        this.notifyTextAvailable(result.getStdout(), ProcessOutputTypes.STDOUT);
        this.notifyTextAvailable(result.getStderr(), ProcessOutputTypes.STDERR);

        if(result.isFinished()) {
            this.notifyTextAvailable(String.format("\nProcess Finished", kernel.getId()), ProcessOutputTypes.SYSTEM);
            stopKernel();
        } else {
            final String input;
            String tmp_input;
            if(result.getStatus() == RunStatus.WAITING_INPUT) {
                try {
                    tmp_input = reader.readLine();
                } catch (IOException e) {
                    tmp_input = "";
                }
            } else {
                tmp_input = "";
            }

            input = tmp_input;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            runCode(input);
                        }
                    },
                    0
            );
        }
    }

    private void interuptKernel() {
        kernel.interrupt();
        //runCode("");
    }


    private void stopKernel() {
//        kernel.destroy();
        terminateProcess();
    }

    private void terminateProcess() {
        this.notifyProcessTerminated(0);
        kernel = null;
    }
}