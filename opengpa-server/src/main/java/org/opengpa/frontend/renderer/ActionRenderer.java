package org.opengpa.frontend.renderer;

import com.vaadin.flow.component.Component;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionResult;

import java.util.Optional;

public interface ActionRenderer {

    boolean applies(String action);

    Optional<Component> render(ActionResult result);

}
