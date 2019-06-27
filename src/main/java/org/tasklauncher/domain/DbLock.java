package org.tasklauncher.domain;

import com.google.common.base.Objects;
import org.tasklauncher.util.EntityTypeEnumConverter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
public class DbLock {

    @Id
    @GeneratedValue
    public Long id;

    public Instant lastExec;

    @NotNull
    @Convert(converter = EntityTypeEnumConverter.class)
    public Task entity;

    @Version
    public Long version;

    public Boolean inProgress;

    @Override
    public String toString() {
        return "DbLock{" +
                "id=" + id +
                ", lastExec=" + lastExec +
                ", entity=" + entity +
                ", version=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbLock that = (DbLock) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(lastExec, that.lastExec) &&
                entity == that.entity &&
                Objects.equal(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(lastExec);
    }
}
