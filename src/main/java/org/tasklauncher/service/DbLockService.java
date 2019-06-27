package org.tasklauncher.service;

import org.tasklauncher.domain.Task;
import org.tasklauncher.domain.DbLock;
import org.tasklauncher.repo.DbLockRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DbLockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbLockService.class);

    @Autowired
    private DbLockRepo dbLockRepo;

    public DbLock updateExecDate(Task task, Instant execDate) {
        LOGGER.info("Persist exec date for entity {} - date : {}", task, execDate);
        DbLock exec = dbLockRepo.findByEntity(task);
        exec.lastExec = execDate;
        return dbLockRepo.saveAndFlush(exec);
    }

    public DbLock getLastExec(Task task) {
        LOGGER.info("Fetch last exec date for entity {}", task);
        DbLock lastexec = dbLockRepo.findByEntity(task);
        if (lastexec != null) LOGGER.debug("Exec date found - date : {}", lastexec);
        else {
            LOGGER.debug("New exec lock created");
            lastexec = new DbLock();
            lastexec.inProgress = false;
            lastexec.entity = task;
            lastexec.lastExec = Instant.ofEpochMilli(0); // 1970-01-01
            lastexec = dbLockRepo.save(lastexec);
        }
        return lastexec;
    }

    public void changeInProgressTo(Task task, Boolean inProgress) {
        LOGGER.info("Updating exec in progress for entity {} to {}", task, inProgress);
        DbLock lastExec = dbLockRepo.findByEntity(task);
        lastExec.inProgress = inProgress;
        dbLockRepo.saveAndFlush(lastExec);
        LOGGER.info("Updated exec in progress for entity {} to {}", task, inProgress);
    }

}
