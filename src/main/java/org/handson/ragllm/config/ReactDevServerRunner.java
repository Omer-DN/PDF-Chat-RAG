package org.handson.ragllm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * מרים את שרת הפיתוח של React (Vite) עם עליית האפליקציה, כשמופעל במצב dev.
 */
@Component
@ConditionalOnProperty(name = "rag.start-react-dev", havingValue = "true")
public class ReactDevServerRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ReactDevServerRunner.class);

    @Value("${rag.react-ui-path:rag-ui}")
    private String reactUiPath;

    private Process reactProcess;

    @Override
    public void run(ApplicationArguments args) {
        File dir = resolveReactUiDir();
        if (dir == null || !dir.isDirectory()) {
            log.warn("rag-ui directory not found at {} – skipping React dev server start. Set rag.react-ui-path if needed.", reactUiPath);
            return;
        }

        ProcessBuilder pb = buildProcess(dir);
        pb.directory(dir);
        pb.inheritIO();

        try {
            reactProcess = pb.start();
            log.info("React (Vite) dev server starting in {} – PID {}", dir.getAbsolutePath(), reactProcess.pid());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (reactProcess != null && reactProcess.isAlive()) {
                    reactProcess.destroyForcibly();
                    log.info("React dev server stopped.");
                }
            }, "react-dev-shutdown"));
        } catch (IOException e) {
            log.error("Failed to start React dev server: {}", e.getMessage());
        }
    }

    private File resolveReactUiDir() {
        File base = new File(System.getProperty("user.dir"));
        File candidate = new File(base, reactUiPath);
        if (candidate.isDirectory()) return candidate;
        candidate = new File(base.getParentFile(), reactUiPath);
        return candidate.isDirectory() ? candidate : null;
    }

    private ProcessBuilder buildProcess(File dir) {
        String npm = isWindows() ? "npm.cmd" : "npm";
        List<String> command = new ArrayList<>();
        command.add(npm);
        command.add("run");
        command.add("dev");
        return new ProcessBuilder(command);
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase().startsWith("windows");
    }
}
