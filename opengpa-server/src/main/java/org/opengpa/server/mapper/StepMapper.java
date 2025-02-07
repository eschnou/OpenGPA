package org.opengpa.server.mapper;

import org.opengpa.core.agent.AgentStep;
import org.opengpa.server.dto.ActionDTO;
import org.opengpa.server.dto.ResultDTO;
import org.opengpa.server.dto.StepDTO;

public class StepMapper {

    public static StepDTO toDTO(AgentStep step) {
        StepDTO stepDTO = new StepDTO();

        // Here are ActionInvocation -> ActionDTO conversion
        ActionDTO actionDTO = new ActionDTO();
        actionDTO.setName(step.getAction().getName());
        actionDTO.setParameters(step.getAction().getParameters());
        actionDTO.setFinal(step.isFinal());
        actionDTO.setReasoning(step.getReasoning());

        // Here are ActionResult -> ResultDTO conversion
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setStatus(step.getResult().getStatus().toString());
        resultDTO.setError(step.getResult().getError());
        resultDTO.setSummary(step.getResult().getSummary());
        resultDTO.setDetails(step.getResult().getResult());

        // List all artifcats created in this step
        stepDTO.setDocuments(step.getResult().getDocuments().stream().map(DocumentMapper::toDocumentDTO).toList());

        // Setting ActionDTO for StepDTO
        stepDTO.setInput(step.getInput());
        stepDTO.setAction(actionDTO);
        stepDTO.setResult(resultDTO);

        return stepDTO;
    }
}
