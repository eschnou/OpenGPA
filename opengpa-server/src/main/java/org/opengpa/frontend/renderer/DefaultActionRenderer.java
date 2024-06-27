package org.opengpa.frontend.renderer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import org.opengpa.core.action.Action;
import org.opengpa.core.action.ActionResult;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class DefaultActionRenderer implements ActionRenderer {

    @Override
    public boolean applies(String action) {
        return true;
    }

    @Override
    public Optional<Component> render(ActionResult result) {
        if (!StringUtils.hasText(result.getSummary())) return Optional.empty();

        var component = new Div();
        component.setText(result.getSummary());
        return Optional.of(component);
    }
}
