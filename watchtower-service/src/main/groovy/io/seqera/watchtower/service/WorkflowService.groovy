package io.seqera.watchtower.service

import io.seqera.watchtower.domain.Workflow

interface WorkflowService {

    Workflow get(Serializable id)

    List<Workflow> list()

    Workflow processWorkflowJsonTrace(Map workflowJson)

}