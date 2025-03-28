package com.batty.forgex.framework.pipeline;

import com.batty.forgex.framework.tasks.Task;
import com.batty.forgex.framework.tasks.TasksList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PipelineService<T> {

    @Autowired
    public TasksList<T> tasksList;

    public void executeTasks() {
        List<Task<T>> tasks = tasksList.getTasks();

        // Execute all tasks asynchronously
        List<CompletableFuture<?>> futures = tasks.parallelStream()
                .map(Task::execute)  // Call execute asynchronously
                .collect(Collectors.toList());

        // Combine all futures and wait for their completion
        CompletableFuture<Void> allDone = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]));

        // Add a callback to handle completion
        allDone.whenComplete((result, exception) -> {
            if (exception == null) {
                System.out.println("All tasks completed successfully!");
            } else {
                System.err.println("Exception occurred: " + exception.getMessage());
            }
        });
    }
}
