package org.tasklauncher.domain;

public enum Task {

    ANOTHER_TASK("anotherTask"),
    DEFAULT("defaultTask");

    private String dbValue;

    Task(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }
}
