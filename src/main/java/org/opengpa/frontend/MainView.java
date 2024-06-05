package org.opengpa.frontend;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.server.VaadinService;
import org.opengpa.frontend.components.AgentMessage;
import org.opengpa.server.dto.Step;
import org.opengpa.server.dto.Task;
import org.opengpa.server.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Route("")
public class MainView extends VerticalLayout {

    public static final int HISTORY_LABEL_SIZE = 35;

    public static final int MAX_LOOPS = 3;

    private final TaskService taskService;

    private Task currentTask;

    private TextArea userInput;

    private Button sendButton, continueButton;

    private VerticalLayout chatMessages;

    private VerticalLayout conversationHistory;

    private ProgressBar progressBar = new ProgressBar();

    private int loopCounter = 0;

    public MainView(TaskService taskService) {
        this.taskService = taskService;
        initView();
        loadTaskHistory();
    }

    private void initView() {
        setSizeFull(); // Make the view take the full screen
        getStyle().set("overflow", "hidden");
        getStyle().set("padding", "0");

        // Progress bar
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
        HorizontalLayout sidebarButtons = new HorizontalLayout();
        sidebarButtons.setWidthFull();
        sidebarButtons.setJustifyContentMode(JustifyContentMode.BETWEEN);
        Button collapseButton = new Button(new Icon(VaadinIcon.MENU));
        collapseButton.addClickListener(e -> splitLayout.setSplitterPosition(0));
        Button newConversationButton = new Button(new Icon(VaadinIcon.PLUS));
        newConversationButton.addClickListener(e -> onNewConversation());
        collapseButton.addClassName("icon-button");
        newConversationButton.addClassName("icon-button");
        sidebarButtons.add(collapseButton, newConversationButton);
        sidebarButtons.setClassName("sidebar-buttons");

        // Placeholder for conversation history
        conversationHistory = new VerticalLayout();
        conversationHistory.setClassName("conversation-history");

        // Company logo at the bottom
        Image projectLogo = new Image("/images/opengpa.png", "OpenGPA");
        projectLogo.addClassName("project-logo");

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

        // Wrapper for user input area to center it
        HorizontalLayout userInputWrapper = new HorizontalLayout();
        userInputWrapper.setWidthFull();
        userInputWrapper.setJustifyContentMode(JustifyContentMode.CENTER);

        // User input area
        HorizontalLayout userInputArea = new HorizontalLayout();
        userInputArea.setClassName("user-input-area");
        userInputArea.setWidth("100%");

        userInput = new TextArea();
        userInput.setWidthFull();
        userInput.setPlaceholder("Message your Assistant");
        userInput.addClassName("user-input");

        sendButton = new Button(VaadinIcon.ARROW_CIRCLE_UP_O.create());
        sendButton.addClassName("send-button");
        sendButton.addClickListener(click -> {
            onSendButtonClick(click);
        });
        userInputArea.add(userInput, sendButton);
        userInputArea.expand(userInput);

        userInputWrapper.add(userInputArea);

        continueButton = new Button("Continue search?", VaadinIcon.REFRESH.create());
        continueButton.addClassName("continue-button");
        continueButton.setVisible(false);
        continueButton.addClickListener(click -> {
            onSendButtonClick(click);
        });

        chatArea.add(chatMessages, continueButton, progressBar, userInputArea);
        chatArea.expand(chatMessages);

        // Add chat area to the wrapper
        chatWrapper.add(chatArea);
        chatWrapper.setAlignItems(Alignment.CENTER);

        // Add sidebar and chat wrapper to the split layout
        splitLayout.addToPrimary(sidebar);
        splitLayout.addToSecondary(chatWrapper);

        // Add split layout to the main view
        add(splitLayout);
    }

    private void onNewConversation() {
        currentTask = null;
        chatMessages.removeAll();
        userInput.clear();
        userInput.focus();
        loadTaskHistory();
    }

    private void onSendButtonClick(ClickEvent<Button> click) {
        String chatMessage = userInput.getValue();
        sendButton.setEnabled(false);
        continueButton.setVisible(false);
        progressBar.setVisible(true);
        userInput.clear();
        loopCounter = 0;

        if (chatMessage != null && StringUtils.hasText(chatMessage)) {
            Paragraph userMessage = new Paragraph(chatMessage);
            userMessage.getElement().getClassList().add("user-message");
            chatMessages.add(userMessage);
        }

        if (currentTask == null) {
            currentTask = taskService.plan(chatMessage, new HashMap<>());
        }

        UI ui = click.getSource().getUI().orElseThrow();
        executeNextStep(ui, chatMessage);
    }

    private void executeNextStep(UI ui, String chatMessage) {
        ListenableFuture<Step> future = taskService.asyncNextStep(currentTask.getTaskId(), chatMessage, new HashMap<>());
        future.addCallback(
                successResult -> onNextStepSuccess(ui, successResult),
                failureException -> onNextStepError(ui, failureException.getMessage())
        );
    }

    private void onNextStepSuccess(UI ui, Step successResult) {
        ui.access(() -> {
            renderStepOutput(successResult);
        });

        if (successResult.getIsLast()) {
            ui.access(() -> {
                resetLoop();
            });
        } else {
            // Not done and no output from the model, we check if max reached
            if (!StringUtils.hasText(successResult.getOutput())) {
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
                ui.access(() -> {
                    resetLoop();
                });
            }
        }
    }

    private void resetLoop() {
        sendButton.setEnabled(true);
        progressBar.setVisible(false);
        loadTaskHistory();
        loopCounter = 0;
    }

    private void onNextStepError(UI ui, String message) {
        ui.access(() -> {
            Notification.show("Execution error: " + message);
            resetLoop();
        });
    }


    private void loadTaskHistory() {
        conversationHistory.removeAll();
        List<Task> tasks = taskService.getTasks().reversed();
        for (Task task : tasks) {
            Button taskButton = new Button(shortLabel(task.getInput()));
            taskButton.addClassName("task-item");
            taskButton.setWidth("100%");
            taskButton.addClickListener(e -> onTaskClick(task.getTaskId()));
            conversationHistory.add(taskButton);
        }
    }

    private void onTaskClick(String taskId) {
        userInput.clear();
        chatMessages.removeAll();
        currentTask = taskService.getTask(taskId);
        taskService.getSteps(taskId).forEach(step -> {
            addStep(step);
        });
    }

    private void addStep(Step step) {
        if (step.getInput() != null && StringUtils.hasText(step.getInput())) {
            Paragraph userMessage = new Paragraph(step.getInput());
            userMessage.getElement().getClassList().add("user-message");
            chatMessages.add(userMessage);
        }

        renderStepOutput(step);
    }

    private void renderStepOutput(Step step) {
        if (StringUtils.hasText(step.getAction())) {
            chatMessages.add(new AgentMessage(step.getAction(), step.getReasoning(), AgentMessage.Type.ACTION));
        }
        if (StringUtils.hasText(step.getOutput())) {
            chatMessages.add(new AgentMessage(step.getOutput(), step.getReasoning(), AgentMessage.Type.OUTPUT));
        }
    }

    private String shortLabel(String input) {
        if (input == null || input.isEmpty() || input.length() < HISTORY_LABEL_SIZE) return input;
        return input.substring(0, HISTORY_LABEL_SIZE) + "...";
    }
}

