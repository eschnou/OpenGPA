package org.opengpa.frontend.renderer;

import com.vaadin.flow.component.Component;
import org.opengpa.core.action.ActionResult;
import org.opengpa.core.agent.ActionInvocation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActionRendererService {

    private final List<ActionRenderer> rendererList;

    private final ActionRenderer defaultActionRenderer = new DefaultActionRenderer();

    public ActionRendererService(List<ActionRenderer> rendererList) {
        this.rendererList = rendererList;
    }

    public Optional<Component> render(ActionInvocation action, ActionResult result) {
        Optional<ActionRenderer> renderer = rendererList.stream().filter(actionRenderer ->
                        actionRenderer.applies(action.getName()))
                .findFirst();

        if (renderer.isPresent()) {
            return renderer.get().render(result);
        } else {
            return defaultActionRenderer.render(result);
        }
    }
}
