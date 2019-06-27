package org.tasklauncher.service;

import org.tasklauncher.domain.Task;
import org.tasklauncher.domain.DbLock;
import org.tasklauncher.util.ProcessHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Inspired by the spring cloud data flow local server deployer TaskLauncher.java
 */
@Service
public class TaskLauncherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLauncherService.class);

    @Autowired
    private DbLockService dbLockService;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private Map<Task, Process> running = new HashMap<>();

    public boolean launch(Task entity, Resource resource) throws IOException {
        LOGGER.info("New launch request for {} - {}", entity, resource);

        // Check if there is a db lock or not
        DbLock exec = dbLockService.getLastExec(entity);

        if (!exec.inProgress) {
            // Do not put the lock acquirement in the try-catch. If the server fails to acquire a lock while it was suppose to be free
            // it means another server acquired it concurrently and we must not free it in the catch exception
            try{
                dbLockService.changeInProgressTo(entity, true);
            } catch (Exception e) {
                LOGGER.info("{} failed to acquire lock", entity);
                throw e;
            }

            try {
                LOGGER.info("{} lock acquired", entity);
                Process process = ProcessHelper.runJarProcess(resource, "-Dspring.profiles.active="+this.activeProfile);
                running.put(entity, process);

                dbLockService.updateExecDate(entity, Instant.now());
                ProcessHelper   .waitForProcessEnds(process)
                                .thenRun(() -> dbLockService.changeInProgressTo(entity, false));
                return true;
            } catch (Exception e) {
                dbLockService.changeInProgressTo(entity, false);
                throw e;
            }
        } else throw new RuntimeException("Task already in progress");
    }

    public boolean cancel(Task entity) {
        Process runningProcess = running.get(entity);
        if (runningProcess != null && runningProcess.isAlive()) {
            runningProcess.destroy();
            return true;
        }
        return false;
    }

    public boolean isAlive(Task entity) {
        Process runningProcess = running.get(entity);
        return runningProcess != null && runningProcess.isAlive();
    }

    @PreDestroy
    private void shutdown() {
        for (Task p : running.keySet()) {
            cancel(p);
        }
    }
}
