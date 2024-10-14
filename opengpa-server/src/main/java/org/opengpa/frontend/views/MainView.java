package org.opengpa.frontend.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.jetbrains.annotations.NotNull;
import org.opengpa.core.action.OutputMessageAction;
import org.opengpa.core.agent.AgentStep;
import org.opengpa.core.workspace.Workspace;
import org.opengpa.frontend.components.HeaderComponent;
import org.opengpa.frontend.components.StepComponent;
import org.opengpa.frontend.components.UserInputComponent;
import org.opengpa.frontend.renderer.ActionRendererService;
import org.opengpa.server.model.Task;
import org.opengpa.server.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Route("")
@PermitAll
public class MainView extends VerticalLayout {

    public static final int HISTORY_LABEL_SIZE = 35;
    public static final int MAX_LOOPS = 3;

    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    private final TaskService taskService;
    private final Workspace workspace;
    private final ActionRendererService actionRendererService;

    private final Authentication user;

    private ProgressBar progressBar;
    private Task currentTask;
    private Button continueButton;
    private VerticalLayout chatMessages;
    private VerticalLayout conversationHistory;
    private UserInputComponent userInputComponent;

    private int loopCounter = 0;

    public MainView(TaskService taskService, Workspace workspace, ActionRendererService actionRendererService) {
        this.taskService = taskService;
        this.workspace = workspace;
        this.actionRendererService = actionRendererService;

        this.user = SecurityContextHolder.getContext().getAuthentication();

        initView();
        loadTaskHistory();
    }

    private void initView() {
        setSizeFull(); // Make the view take the full screen
        getStyle().set("overflow", "hidden");
        getStyle().set("padding", "0");

        // Progress bar
        progressBar = new ProgressBar();
        progressBar.setWidth("100%");
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.addThemeVariants(ProgressBarVariant.LUMO_CONTRAST);

        // Split layout to divide the sidebar and the main chat area
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        // Sidebar with history and buttons
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth("300px");
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.setClassName("sidebar");

        // Buttons at the top of the sidebar
        HorizontalLayout sidebarButtons = getSidebarButtons(splitLayout);

        // Placeholder for conversation history
        conversationHistory = new VerticalLayout();
        conversationHistory.setClassName("conversation-history");

        // Company logo at the bottom
        Image projectLogo = new Image("/images/opengpa.png", "OpenGPA");
        projectLogo.addClassName("project-logo");
        projectLogo.addClickListener(imageClickEvent -> {
            UI.getCurrent().getPage().open("https://github.com/eschnou/opengpa");
        });

        sidebar.add(sidebarButtons, conversationHistory, projectLogo);

        // Wrapper for the entire chat area
        VerticalLayout chatWrapper = new VerticalLayout();
        chatWrapper.setSizeFull();
        chatWrapper.setClassName("chat-wrapper");

        // Main chat area
        VerticalLayout chatArea = new VerticalLayout();
        chatArea.setSizeFull();
        chatArea.setClassName("chat-area");

        // Placeholder for chat messages
        chatMessages = new VerticalLayout();
        chatMessages.setClassName("chat-messages");
        chatMessages.getStyle().set("flex-grow", "1").set("overflow", "auto");

        // Continue search button
        continueButton = new Button("Continue search?", VaadinIcon.REFRESH.create());
        continueButton.addClassName("continue-button");
        continueButton.setVisible(false);
        continueButton.addClickListener(this::onUserInputSubmit);

        // Wrapper for user input area to center it
        HorizontalLayout userInputWrapper = new HorizontalLayout();
        userInputWrapper.setWidthFull();
        userInputWrapper.setJustifyContentMode(JustifyContentMode.CENTER);

        userInputComponent = new UserInputComponent();
        userInputComponent.setSubmitListener(this::onUserInputSubmit);

        chatArea.add(chatMessages, continueButton, progressBar, userInputComponent);
        chatArea.expand(chatMessages);

        // Add chat area to the wrapper
        chatWrapper.add(chatArea);
        chatWrapper.setAlignItems(Alignment.CENTER);

        // Create the secondary panel
        VerticalLayout secondary = new VerticalLayout();
        Component headerComponent = new HeaderComponent();
        secondary.add(headerComponent, chatWrapper);

        // Add sidebar and chat wrapper to the split layout
        splitLayout.addToPrimary(sidebar);
        splitLayout.addToSecondary(secondary);

        // Add split layout to the main view
        add(splitLayout);
    }

    private @NotNull HorizontalLayout getSidebarButtons(SplitLayout splitLayout) {
        HorizontalLayout sidebarButtons = new HorizontalLayout();
        sidebarButtons.setWidthFull();
        sidebarButtons.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Button collapseButton = new Button(new Icon(VaadinIcon.MENU));
        collapseButton.addClickListener(e -> splitLayout.setSplitterPosition(0));
        Button newConversationButton = new Button(new Icon(VaadinIcon.PLUS));
        newConversationButton.addClickListener(e -> onNewConversation());
        collapseButton.addClassName("icon-button");
        newConversationButton.addClassName("icon-button");
        Div title = new Div("Tasks");
        title.setClassName("panel-title");

        sidebarButtons.add(collapseButton, title, newConversationButton);
        sidebarButtons.setClassName("sidebar-buttons");
        return sidebarButtons;
    }

    private void onNewConversation() {
        currentTask = null;
        chatMessages.removeAll();
        loadTaskHistory();
        userInputComponent.clear();
        userInputComponent.enable();
    }

    private void onUserInputSubmit(ComponentEvent<?> componentEvent) {
        String chatMessage = userInputComponent.getUserInput();
        userInputComponent.disable();
        userInputComponent.clear();

        continueButton.setVisible(false);
        progressBar.setVisible(true);
        loopCounter = 0;

        if (StringUtils.hasText(chatMessage)) {
            Paragraph userMessage = new Paragraph(chatMessage);
            userMessage.getElement().getClassList().add("user-message");
            chatMessages.add(userMessage);
        }

        List<UserInputComponent.UploadedFile> uploadedFiles = userInputComponent.getUploadedFiles();
        if (!uploadedFiles.isEmpty()) {
            for (UserInputComponent.UploadedFile uploadedFile : uploadedFiles) {
                workspace.addDocument(currentTask.getTaskId(), uploadedFile.getFileName(), uploadedFile.getData());
            }
        }

        if (currentTask == null) {
            currentTask = taskService.plan(user.getName(), chatMessage, new HashMap<>());
        }

        UI ui = componentEvent.getSource().getUI().orElseThrow();
        executeNextStep(ui, chatMessage);
    }

    private void executeNextStep(UI ui, String chatMessage) {
        ListenableFuture<AgentStep> future = taskService.asyncNextStep(user.getName(), currentTask.getTaskId(), chatMessage, new HashMap<>());
        future.addCallback(
                successResult -> onNextStepSuccess(ui, successResult),
                failureException -> onNextStepError(ui, failureException.getMessage())
        );
    }

    private void onNextStepSuccess(UI ui, AgentStep successResult) {
        ui.access(() -> renderStepOutput(successResult));

        if (successResult.isFinal()) {
            ui.access(this::resetLoop);
        } else {
            // Not done and no output from the model, we check if max reached
            if (!successResult.getAction().getName().equals(OutputMessageAction.NAME)) {
                if (loopCounter < MAX_LOOPS) {
                    executeNextStep(ui, "");
                    loopCounter++;
                } else {
                    ui.access(() -> {
                        continueButton.setVisible(true);
                        resetLoop();
                    });
                }
            } else {
                ui.access(this::resetLoop);
            }
        }
    }

    private void resetLoop() {
        userInputComponent.enable();
        progressBar.setVisible(false);
        loadTaskHistory();
        loopCounter = 0;
    }

    private void onNextStepError(UI ui, String message) {
        ui.access(() -> {
            Notification.show("Execution error: " + message);
            log.warn("Execution error {}", message);
            resetLoop();
        });
    }

    private void loadTaskHistory() {
        conversationHistory.removeAll();
        List<Task> tasks = taskService.getTasks(user.getName()).reversed();
        for (Task task : tasks) {
            Button taskButton = new Button(shortLabel(task.getTitle()));
            taskButton.addClassName("task-item");
            taskButton.setWidth("100%");
            taskButton.addClickListener(e -> onTaskClick(task.getTaskId()));
            conversationHistory.add(taskButton);
        }
    }

    private void onTaskClick(String taskId) {
        userInputComponent.clear();
        userInputComponent.enable();
        chatMessages.removeAll();
        currentTask = taskService.getTask(user.getName(), taskId);
        taskService.getSteps(user.getName(), taskId).forEach(this::addStep);
    }

    private void addStep(AgentStep step) {
        if (step.getInput() != null && StringUtils.hasText(step.getInput())) {
            Paragraph userMessage = new Paragraph(step.getInput());
            userMessage.getElement().getClassList().add("user-message");
            chatMessages.add(userMessage);
        }

        renderStepOutput(step);
    }

    private void renderStepOutput(AgentStep step) {
        StepComponent stepComponent = new StepComponent(step);
        if (!step.getAction().getName().equals(OutputMessageAction.NAME)) {
            Optional<Component> actionComponent = actionRendererService.render(step.getAction(), step.getResult());
            if (actionComponent.isPresent()) {
                stepComponent.setActionComponent(actionComponent.get());
            }
        }

        chatMessages.add(stepComponent);
    }

    private String shortLabel(String input) {
        if (input == null || input.length() < HISTORY_LABEL_SIZE) return input;
        return input.substring(0, HISTORY_LABEL_SIZE) + "...";
    }
}

