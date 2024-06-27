package org.opengpa.frontend.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserInputComponent extends VerticalLayout {

    private final TextArea userInput;

    private final HorizontalLayout uploadContainer;

    private final Button uploadButton;

    private final Button sendButton;

    private final List<UploadedFile> uploadedFiles = new ArrayList<>();

    private ComponentEventListener submitListener;

    public UserInputComponent() {
        setClassName("user-input-component");
        setWidth("100%");

        uploadContainer = new HorizontalLayout();
        uploadContainer.setClassName("upload-container");
        uploadContainer.setWidth("100%");

        HorizontalLayout inputContainer = new HorizontalLayout();
        inputContainer.setClassName("input-container");
        inputContainer.setWidth("100%");

        uploadButton = new Button(VaadinIcon.PAPERCLIP.create());
        uploadButton.setClassName("upload-button");
        uploadButton.addClickListener(this::onUploadButtonClick);

        userInput = new TextArea();
        userInput.setWidthFull();
        userInput.setPlaceholder("Message your Assistant");
        userInput.addClassName("user-input");
        userInput.setValueChangeMode(ValueChangeMode.ON_CHANGE);

        sendButton = new Button(VaadinIcon.ARROW_CIRCLE_UP_O.create());
        sendButton.addClassName("send-button");
        sendButton.addClickListener(this::onSendButtonClick);

        inputContainer.add(uploadButton, userInput, sendButton);
        inputContainer.expand(userInput);

        add(uploadContainer, inputContainer);
    }

    public void setSubmitListener(ComponentEventListener listener) {
        this.submitListener = listener;
    }

    public void enable() {
        uploadButton.setEnabled(true);
        sendButton.setEnabled(true);
        userInput.setEnabled(true);
    }

    public void disable() {
        uploadButton.setEnabled(false);
        sendButton.setEnabled(false);
        userInput.setEnabled(false);
    }

    public String getUserInput() {
        return userInput.getValue();
    }

    public List<UploadedFile> getUploadedFiles() {
        return uploadedFiles;
    }

    public void clear() {
        uploadedFiles.clear();
        userInput.clear();
        uploadContainer.removeAll();
    }

    private void onSendButtonClick(ClickEvent<Button> buttonClickEvent) {
        if (submitListener == null) return;

        submitListener.onComponentEvent(buttonClickEvent);
    }

    private void onUploadButtonClick(ClickEvent<Button> buttonClickEvent) {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        Dialog dialog = new Dialog(upload);

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            byte[] fileData = null;
            try {
                fileData = buffer.getInputStream(fileName).readAllBytes();
                UploadedFile file = new UploadedFile(fileName, fileData);
                uploadedFiles.add(file);
                refreshUploadedFilesArea();
            } catch (IOException e) {
                onError(event.getSource().getUI().orElseThrow(), e.getMessage());
            }
            dialog.close();
        });

        dialog.open();
    }

    private void refreshUploadedFilesArea() {
        uploadContainer.removeAll();

        for (UploadedFile file : uploadedFiles) {
            HorizontalLayout fileContainer = new HorizontalLayout();
            fileContainer.addClassName("file-component");

            Icon fileIcon = new Icon(VaadinIcon.FILE);
            fileIcon.getStyle().set("margin-right", "10px");

            Button removeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            removeButton.addClassName("remove-button");
            removeButton.addClickListener(event -> {
                uploadedFiles.remove(file);
                refreshUploadedFilesArea();
            });

            fileContainer.add(fileIcon, new Div(file.getFileName()), removeButton);

            uploadContainer.add(fileContainer);
        }
    }

    private void onError(UI ui, String message) {
        ui.access(() -> {
            Notification.show("Execution error: " + message);
            log.warn("Execution error {}", message);
            clear();
        });
    }

    public static class UploadedFile {
        private final String fileName;
        private final byte[] data;

        public UploadedFile(String fileName, byte[] data) {
            this.fileName = fileName;
            this.data = data;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getData() {
            return data;
        }
    }
}
