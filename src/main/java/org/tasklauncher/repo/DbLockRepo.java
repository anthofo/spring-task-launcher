package org.tasklauncher.repo;

import org.tasklauncher.domain.Task;
import org.tasklauncher.domain.DbLock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DbLockRepo extends JpaRepository<DbLock, Long> {

    DbLock findByEntity(Task entity);
}
