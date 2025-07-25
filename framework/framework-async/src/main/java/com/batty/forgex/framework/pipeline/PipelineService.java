package com.batty.forgex.framework.pipeline;

import com.batty.forgex.framework.tasks.Task;
import com.batty.forgex.framework.tasks.TasksList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PipelineService<T,U> {

    protected Logger log = LoggerFactory.getLogger(PipelineService.class);
    @Autowired
    public TasksList<T> tasksList;

    private Object obj;          // Store the object of any type

    public void executeTasks(U obj, Class<U> type,String id) {
        List<Task<T>> tasks = tasksList.getTasks();

        // Execute all tasks asynchronously
        List<CompletableFuture<?>> futures = tasks.parallelStream()
                .map(task -> CompletableFuture.runAsync(() -> {
                    task.setObject(obj,type);
                    task.setParentId(id);
                    task.execute(); })) // Call execute asynchronously
                .collect(Collectors.toList());

        // Combine all futures and wait for their completion
        CompletableFuture<Void> allDone = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]));

        // Add a callback to handle completion
        allDone.whenComplete((result, exception) -> {
            if (exception == null) {
                log.error("All tasks completed successfully!");
            } else {
                log.error("Exception occurred: {}" , exception.getMessage());
            }
        });
    }
}
