package org.tasklauncher.controller;

import org.tasklauncher.domain.Task;
import org.tasklauncher.service.TaskLauncherService;
import org.tasklauncher.service.DbLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;

@RestController()
@RequestMapping(value="/v1/tasks", produces = {MediaType.APPLICATION_JSON_VALUE})
public class TaskController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private DbLockService dbLockService;

    @Autowired
    private TaskLauncherService taskLauncherService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${exec.enabled}")
    private Boolean defaultTaskEnabled;

    @Value("${exec.jar-path}")
    private String defaultTaskJarUrl;

    @PostMapping(value = "/DEFAULT/launch", produces = MediaType.TEXT_PLAIN_VALUE)
    public void execTask() throws IOException {
        Resource resource = resourceLoader.getResource(defaultTaskJarUrl);
        taskLauncherService.launch(Task.DEFAULT, resource);
    }

    // You can replace the previous method by this one to have a more generic codes, the previous method only launch the task named "DEFAULT" with the hardCoded jar path in the application.yml
//    @PostMapping(value = "/{entityType}/launch", produces = MediaType.TEXT_PLAIN_VALUE)
//    public void execTask(@PathVariable EntityType entityType) throws IOException {
//        Resource resource = resourceLoader.getResource(defaultTaskJarUrl); // You have to define which resource to use (url or path) depending on the entity type
//        taskLauncherService.launch(entityType, resource);
//    }

    @GetMapping("/{task}/last-exec")
    public Instant getLastExecDate(@PathVariable Task task) {
        return dbLockService.getLastExec(task).lastExec;
    }

    @GetMapping("/{task}/status")
    public boolean taskStatus(@PathVariable Task task) {
        return taskLauncherService.isAlive(task);
    }

    @PostMapping("/{task}/cancel")
    public boolean cancelTask(@PathVariable Task task) {
        return taskLauncherService.cancel(task);
    }

    @PutMapping("/{task}/in-progress/{value}")
    public void updateInProgressTo (@PathVariable boolean value, @PathVariable Task task) {
        dbLockService.changeInProgressTo(task, value);
    }

}
