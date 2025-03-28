package com.batty.forgex.framework.tasks;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskRegistrationService<T> {

    protected Logger log = LoggerFactory.getLogger(TaskRegistrationService.class);
        @Autowired
        private TasksList<T> tasksList;

        public void registerEntityBuilderActor(Task<T> task) {
            log.info("Registring {}",task.getName());
            tasksList.registerTask(task);
        }


}
