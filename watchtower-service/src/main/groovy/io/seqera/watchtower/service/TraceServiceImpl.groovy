package io.seqera.watchtower.service

import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import org.springframework.validation.FieldError

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException

@Singleton
class TraceServiceImpl implements TraceService {

    WorkflowService workflowService

    TaskService taskService

    @Inject
    TraceServiceImpl(WorkflowService workflowService, TaskService taskService) {
        this.workflowService = workflowService
        this.taskService = taskService
    }


    Workflow processWorkflowTrace(TraceWorkflowRequest traceJson) {
        Workflow workflow = workflowService.processWorkflowJsonTrace(traceJson)
        checkWorkflowSaveErrors(workflow)

        workflow
    }

    Task processTaskTrace(TraceTaskRequest trace) {
        Task task = taskService.processTaskJsonTrace(trace)
        checkTaskSaveErrors(task)

        task
    }

    private void checkWorkflowSaveErrors(Workflow workflow) {
        if (!workflow.hasErrors()) {
            return
        }

        List<FieldError> fieldErrors = workflow.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            throw new ValidationException("Can't save a workflow without ${nullableError.field}")
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            throw new ValidationException("Can't save a workflow with the same ${uniqueError.field} of another")
        }
    }

    private void checkTaskSaveErrors(Task task) {
        if (!task.hasErrors()) {
            return
        }

        List<FieldError> fieldErrors = task.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            throw new ValidationException("Can't save a task without ${nullableError.field}")
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            throw new ValidationException("Can't save a task with the same ${uniqueError.field} of another")
        }
    }

}
