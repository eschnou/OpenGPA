package org.opengpa.frontend.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.opengpa.frontend.components.HeaderComponent;
import org.opengpa.rag.service.RagChunk;
import org.opengpa.rag.service.RagDocument;
import org.opengpa.rag.service.RagService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route("documents")
@SpringComponent
@UIScope
@PermitAll
public class DocumentsView extends VerticalLayout {

    private final RagService ragService;
    private final TransactionTemplate transactionTemplate;
    private final Grid<RagDocument> grid;
    private final TextField searchField;
    private final Authentication user;

    public DocumentsView(RagService ragService, TransactionTemplate transactionTemplate) {
        this.ragService = ragService;
        this.transactionTemplate = transactionTemplate;
        this.user = SecurityContextHolder.getContext().getAuthentication();

        addClassName("document-management-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        Component headerComponent = new HeaderComponent();
        add(headerComponent);

        VerticalLayout content = new VerticalLayout();
        content.addClassName("content");
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);

        H2 title = new H2("Document Management");
        title.addClassName("view-title");

        searchField = new TextField("Search documents");
        searchField.addClassName("search-field");
        searchField.setPlaceholder("Enter search term...");
        searchField.addValueChangeListener(e -> updateList());

        Button uploadButton = new Button("Upload", e -> showUploadDialog());
        uploadButton.addClassName("upload-button");

        HorizontalLayout toolbar = new HorizontalLayout(searchField, uploadButton);
        toolbar.addClassName("toolbar");
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        grid = new Grid<>(RagDocument.class);
        grid.addClassName("document-grid");
        grid.setColumns();
        grid.setHeightFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Add columns with specific configurations
        grid.addColumn(RagDocument::getDocumentId)
                .setHeader("Document Id")
                .setKey("documentId")
                .setFlexGrow(0)
                .setWidth("200px");

        grid.addColumn(RagDocument::getFilename)
                .setHeader("Filename")
                .setFlexGrow(1)
                .setWidth("100px");

        grid.addColumn(RagDocument::getTitle)
                .setHeader("Title")
                .setFlexGrow(1)
                .setWidth("100px");

        grid.addColumn(RagDocument::getDescription)
                .setHeader("Description")
                .setFlexGrow(1)
                .setWidth("100px");

        grid.addColumn(RagDocument::getContentType)
                .setHeader("Content Type")
                .setFlexGrow(1)
                .setWidth("100px");

        grid.addColumn(new ComponentRenderer<>(this::createProgressBar))
                .setHeader("Ingested")
                .setFlexGrow(0)
                .setWidth("150px");

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Actions")
                .setFlexGrow(0)
                .setWidth("180px");

        // Add row styling based on progress
        grid.setClassNameGenerator(document -> document.getProgress() < 1.0 ? "incomplete-row" : null);

        content.add(title, toolbar, grid);
        add(content);
    }

    @PostConstruct
    private void init() {
        updateList();
    }

    private void updateList() {
        List<RagDocument> documents = transactionTemplate.execute(status ->
                ragService.listDocuments(user.getName())
        );
        grid.setItems(documents);
    }

    private HorizontalLayout createActionButtons(RagDocument document) {
        Button viewButton = new Button("View", new Icon(VaadinIcon.EYE), e -> showDocumentDetails(document.getDocumentId()));
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH), e -> {
            ConfirmDialog dialog = new ConfirmDialog(
                    "Confirm deletion",
                    "Are you sure you want to delete the document '" + document.getTitle() + "'?",
                    "Delete", this::deleteDocument,
                    "Cancel", this::cancelDelete);

            dialog.setConfirmButtonTheme("error primary");
            dialog.open();

            dialog.getElement().setProperty("documentId", document.getDocumentId());
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(viewButton, deleteButton);
        actions.addClassName("action-buttons");
        actions.setSpacing(true);

        // Disable buttons if progress is less than 1.0
        boolean isComplete = document.getProgress() >= 1.0;
        viewButton.setEnabled(isComplete);
        deleteButton.setEnabled(isComplete);

        return actions;
    }

    private void deleteDocument(ConfirmDialog.ConfirmEvent event) {
        String documentId = event.getSource().getElement().getProperty("documentId");
        transactionTemplate.execute(status -> {
            ragService.deleteDocument(documentId);
            return null;
        });
        updateList();
        Notification.show("Document deleted", 3000, Notification.Position.BOTTOM_START);
    }

    private void cancelDelete(ConfirmDialog.CancelEvent event) {
        // This method is called when the user cancels the delete operation
        // You can leave it empty or add any desired behavior
    }

    private void showDocumentDetails(String documentId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Document Details");
        dialog.setWidth("600px"); // Increased width

        VerticalLayout content = new VerticalLayout();
        content.addClassName("document-details-content");
        content.setSpacing(false);
        content.setPadding(false);

        transactionTemplate.execute(status -> {
            RagDocument document = ragService.getDocument(documentId).orElse(null);
            if (document != null) {
                H3 title = new H3(document.getTitle());
                title.addClassName("document-title");

                content.add(
                        title,
                        createDetailItem("Filename", document.getFilename()),
                        createDetailItem("Content Type", document.getContentType()),
                        createDetailItem("Description", document.getDescription()),
                        createDetailItem("Chunks count", String.valueOf(document.getChunks().size()))
                );

                H4 contentTitle = new H4("Document Content");
                contentTitle.addClassName("content-title");
                content.add(contentTitle);

                // Create a scrollable container for chunks
                VerticalLayout chunksContainer = new VerticalLayout();
                chunksContainer.addClassName("chunks-container");
                chunksContainer.setSpacing(false);
                chunksContainer.setPadding(false);
                chunksContainer.setSizeFull();

                // Sort chunks by chunkIndex and add them to the container
                List<RagChunk> sortedChunks = document.getChunks().stream()
                        .sorted(Comparator.comparingInt(RagChunk::getIndex))
                        .collect(Collectors.toList());

                // Add chunks with alternating backgrounds
                for (int i = 0; i < sortedChunks.size(); i++) {
                    RagChunk chunk = sortedChunks.get(i);
                    Paragraph chunkParagraph = new Paragraph(chunk.getContent());
                    chunkParagraph.addClassName("chunk");
                    chunkParagraph.addClassName(i % 2 == 0 ? "chunk-even" : "chunk-odd");
                    chunksContainer.add(chunkParagraph);
                }

                content.add(chunksContainer);
                content.setFlexGrow(1, chunksContainer);
            } else {
                content.add(new Paragraph("Document not found"));
            }
            return null;
        });

        dialog.add(content);

        Button closeButton = new Button("Close", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(closeButton);

        dialog.open();
    }

    private Div createDetailItem(String label, String value) {
        Div item = new Div();
        item.addClassName("detail-item");

        Span labelSpan = new Span(label + ":");
        labelSpan.addClassName("detail-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("detail-value");

        item.add(labelSpan, valueSpan);
        return item;
    }
    private void showUploadDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Upload Document");
        dialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidth("100%");

        TextField titleField = new TextField("Title");
        titleField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("100px");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setWidthFull();
        upload.setAcceptedFileTypes("application/pdf", "text/plain", "text/html", "text/csv");
        upload.setMaxFileSize(10 * 1024 * 1024); // 10 MB in bytes

        // Add a label to inform users about the file size limit
        Span infoText = new Span("Maximum file size: 10 MB");
        infoText.getStyle().set("color", "var(--lumo-secondary-text-color)");
        dialog.add(infoText);

        Button uploadButton = new Button("Upload", e -> {
            String fileName = buffer.getFileName();
            String mimeType = buffer.getFileData().getMimeType();
            InputStream inputStream = buffer.getInputStream();
            try {
                byte[] bytes = inputStream.readAllBytes();
                transactionTemplate.execute(status -> {
                    ragService.ingestDocument(user.getName(), fileName, mimeType, bytes,
                            titleField.getValue(), descriptionField.getValue());
                    return null;
                });
                updateList();
                dialog.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                // Handle error (show notification, etc.)
            }
        });
        uploadButton.setEnabled(false);
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        uploadButton.setWidthFull();

        upload.addSucceededListener(event -> uploadButton.setEnabled(true));

        // Add listener for file rejection (e.g., file too large)
        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();
            Notification.show(errorMessage, 3000, Notification.Position.MIDDLE);
        });

        content.add(titleField, descriptionField, upload, uploadButton);
        dialog.add(content);

        dialog.open();
    }

    private Component createProgressBar(RagDocument document) {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(document.getProgress());
        progressBar.setWidth("100px");

        Span progressLabel = new Span(String.format("%.0f%%", document.getProgress() * 100));
        progressLabel.getStyle().set("margin-left", "8px");

        HorizontalLayout progressLayout = new HorizontalLayout(progressBar, progressLabel);
        progressLayout.setAlignItems(Alignment.CENTER);

        return progressLayout;
    }

}