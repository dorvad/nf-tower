package io.seqera.tower.service

import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowTag

interface WorkflowTagService {

    WorkflowTag create(WorkflowTag newWorkflowTag, Workflow associatedWorkflow)

    WorkflowTag update(WorkflowTag existingTag, WorkflowTag updatedTag)

}