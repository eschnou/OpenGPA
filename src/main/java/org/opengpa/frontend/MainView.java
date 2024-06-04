package org.opengpa.frontend;

import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import jdk.jfr.Threshold;
import org.opengpa.server.dto.Step;
import org.opengpa.server.dto.Task;
import org.opengpa.server.service.TaskService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.model.Label;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

    public static final int HISTORY_LABEL_SIZE = 20;
    private final TaskService taskService;

    private Task currentTask;

    private TextArea userInput;

    private VerticalLayout chatMessages;

    private VerticalLayout conversationHistory;

    public MainView(TaskService taskService) {
        this.taskService = taskService;
        initView();
        loadTaskHistory();
    }

    private void initView() {
        setSizeFull(); // Make the view take the full screen
        getStyle().set("overflow", "hidden");
        getStyle().set("padding", "0");

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

        sidebar.add(sidebarButtons, conversationHistory);

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
        userInput.addKeyUpListener(Key.ENTER, event -> onUserInput());

        Button sendButton = new Button(VaadinIcon.ARROW_CIRCLE_UP_O.create());
        sendButton.addClassName("send-button");
        sendButton.addClickListener(click -> {
            onUserInput();
        });
        userInputArea.add(userInput, sendButton);
        userInputArea.expand(userInput);

        userInputWrapper.add(userInputArea);

        chatArea.add(chatMessages, userInputWrapper);
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

    private void onUserInput() {
        String chatMessage = userInput.getValue();
        userInput.clear();

        if (chatMessage != null && StringUtils.hasText(chatMessage)) {
            Paragraph userMessage = new Paragraph(chatMessage);
            userMessage.getElement().getClassList().add("user-message");
            chatMessages.add(userMessage);
        }

        if (currentTask == null) {
            currentTask = taskService.plan(chatMessage, new HashMap<>());
        }

        Step step = taskService.nextStep(currentTask.getTaskId(), chatMessage, new HashMap<>());
        if (step != null) {
            Paragraph botMessage = new Paragraph(step.getOutput());
            botMessage.getElement().getClassList().add("bot-message");
            chatMessages.add(botMessage);
        }
    }

    private void onNewConversation() {
        currentTask = null;
        chatMessages.removeAll();
        userInput.clear();
        loadTaskHistory();
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

        Paragraph botMessage = new Paragraph(step.getOutput());
        botMessage.getElement().getClassList().add("bot-message");
        chatMessages.add(botMessage);
    }

    private String shortLabel(String input) {
        if (input == null || input.isEmpty() || input.length() < HISTORY_LABEL_SIZE) return input;
        return input.substring(0, HISTORY_LABEL_SIZE) + "...";
    }
}

