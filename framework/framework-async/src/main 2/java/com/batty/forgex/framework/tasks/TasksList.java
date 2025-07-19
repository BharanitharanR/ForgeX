package com.batty.forgex.framework.tasks;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component("TaskList")
public final class TasksList<T> {

    // Use thread-safe collection for concurrent access
    private final List<Task<T>> tasks = new CopyOnWriteArrayList<>();

    // Register task
    public void registerTask(Task<T> task) {
        tasks.add(task);
    }

    // Get list of tasks
    public List<Task<T>> getTasks() {
        return tasks;
    }
}
