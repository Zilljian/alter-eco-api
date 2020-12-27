package org.alter.eco.api.model;

import org.alter.eco.api.jooq.tables.Task;
import org.jooq.SortField;

public enum Sort {

    REWARD_DESC(Task.TASK.REWARD.desc()),
    CREATED_DESC(Task.TASK.CREATED.desc()),
    DUE_DATE_DESC(Task.TASK.DUE_DATE.desc()),
    REWARD(Task.TASK.REWARD.asc()),
    CREATED(Task.TASK.CREATED.asc()),
    DUE_DATE(Task.TASK.DUE_DATE.asc());

    private final SortField<?> value;

    Sort(SortField<?> value) {
        this.value = value;
    }

    public SortField<?> value() {
        return value;
    }
}
