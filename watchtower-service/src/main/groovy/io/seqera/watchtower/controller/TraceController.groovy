package io.seqera.watchtower.controller


import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TraceProcessingStatus
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse
import io.seqera.watchtower.service.TraceService

import javax.inject.Inject

/**
 * Implements the `trace` API
 *
 */
@Controller("/trace")
@Slf4j
class TraceController {

    TraceService traceService

    @Inject
    TraceController(TraceService traceService) {
        this.traceService = traceService
    }


    @Post("/workflow")
    HttpResponse<TraceWorkflowResponse> workflow(@Body TraceWorkflowRequest trace) {
        HttpResponse<TraceWorkflowResponse> response
        try {
            log.info("Receiving workflow trace: ${trace.inspect()}")
            Workflow workflow = traceService.processWorkflowTrace(trace)
            log.info("Processed workflow trace: ${trace.inspect()}")

            response = HttpResponse.created(new TraceWorkflowResponse(status: TraceProcessingStatus.OK, workflowId: workflow.id.toString()))
        } catch (Exception e) {
            response = HttpResponse.badRequest(new TraceWorkflowResponse(status: TraceProcessingStatus.KO, message: e.message))
        }

        response
    }

    @Post("/task")
    HttpResponse<TraceTaskResponse> task(@Body TraceTaskRequest trace) {
        HttpResponse<TraceTaskResponse> response
        try {
            log.info("Receiving task trace: ${trace.inspect()}")
            Task task = traceService.processTaskTrace(trace)
            log.info("Processed task trace: ${trace.inspect()}")

            response = HttpResponse.created(new TraceTaskResponse(status: TraceProcessingStatus.OK, workflowId: task.workflowId.toString()))
        } catch (Exception e) {
            response = HttpResponse.badRequest(new TraceTaskResponse(status: TraceProcessingStatus.KO, message: e.message))
        }

        response
    }

}