package com.tdcr.docker.ui.views.dockContainers;

import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.DataUtil;
import com.tdcr.docker.ui.components.Message;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.EntityView;
import com.tdcr.docker.ui.views.MainView;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * A Designer generated component for the container-view template.
 *
 * Designer will add and remove fields with @Id mappings but
 * does not overwrite or otherwise change this file.
 */
@Tag("container-view")
@HtmlImport("src/views/dock-container/container-view.html")
@Route(value = AppConst.PAGE_CONTAINERS, layout = MainView.class)
@PageTitle(AppConst.TITLE_CONTAINER)
public class ContainerView extends PolymerTemplate<TemplateModel> implements EntityView<DockContainer> {

    @Id("search")
    private SearchBar searchBar;

    @Id("grid")
    private Grid<DockContainer> grid;

    @Id("dialog")
    private Dialog dialog;

    private ConfirmDialog confirmation;

    @Autowired
    private DockerService dockerService;

    private ListDataProvider<DockContainer> dataProvider;

    @Id("comboBox")
    private ComboBox<String> dockerComboBox;

    @Id("refresh")
    private Button refreshBtn;

    @Id("logs")
    private Button logs;

    @Id("updateStatus")
    private Button updateContainerStatus;

    /**
     * Creates a new Container
     */
    @PostConstruct
    void init() {
        initDockerCombobox();
        initButtonListeners();
        setupGrid();
        setupSearchBar();
    }

    private void initButtonListeners() {


        refreshBtn.setIcon(VaadinIcon.REFRESH.create());
        refreshBtn.addClickListener(e -> {
            grid.setDataProvider(dataProvider);
        });

        logs.setIcon(VaadinIcon.PIE_CHART.create());
        logs.addClickListener(e ->{
            logSubWindow();
        });

        updateContainerStatus.setIcon(VaadinIcon.POWER_OFF.create());
        updateContainerStatus.addClickListener(e ->{
            updateStatus();
        });
    }

    private void initDockerCombobox() {
        dockerComboBox.setItems(dockerService.getDockerDeamons());
        dockerComboBox.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                Notification.show("Inavlid action!");
            } else {
                dockerService.updateDockerClient(event.getValue());
                initDataProvider();
                refreshBtn.click();
            }
        });
    }

    private void setupSearchBar() {
        searchBar.setActionText("New Container");
        searchBar.setCheckboxText("Exclude stopped containers");
        searchBar.addSearchValueChangeListener(e ->
                dataProvider.setFilter(DockContainer::getContainerName,
                        s -> DataUtil.caseInsensitiveContains(s, searchBar.getFilter())));
//        searchBar.addFilterChangeListener(e ->
//                dataProvider.setFilter(DockContainer::getStatus,
//                        s -> DataUtil.caseInsensitiveContains(s, ((SearchBar)e.getSource())
//                                .isCheckboxChecked()? "Running": s)));
    }

    private void setupGrid() {
        dockerService.updateDockerClient(dockerService.getDockerDeamons().iterator().next());
        initDataProvider();
        addGridColumns();
    }

    private void initDataProvider() {
        this.dataProvider= DataProvider.ofCollection(dockerService.listAllContainers(null));
    }

    private void addGridColumns() {
        grid.addColumn(DockContainer::getContainerName)
                .setWidth("270px").setHeader("ContainerName").setFlexGrow(5).setSortable(true);
        grid.addColumn(dc -> dc.getImageName())
                .setHeader("ImageName").setWidth("200px").setFlexGrow(5).setSortable(true);
        grid.addColumn(DockContainer::getStatus).setHeader("Status").setWidth("150px");
        grid.addColumn(DockContainer::getPort).setHeader("ExposedPort").setWidth("150px");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
    }

    private void logSubWindow() {
        DockContainer selectedRow = getSelectedRow();
        if(selectedRow == null) return;
    }

    private DockContainer getSelectedRow() {
        Set selectedItems = grid.getSelectedItems();
        if(selectedItems.isEmpty()){
            Notification.show("Please select a container!");
            return null;
        }
        return (DockContainer) selectedItems.toArray()[0];
    }

    private void updateStatus() {
        DockContainer container = getSelectedRow();
        if(container == null) return;

        if(container.getContainerName().contains("socat") || container.getContainerName().contains("dw") ){
            Notification.show("Inavlid action!");
            return;
        }
        boolean status = !container.getStatus().equalsIgnoreCase("running");
        String statussMsg = status == true? "Start" :"Stop";
        String oppStatussMsg =status != true? "Start" :"Stop";

        Message containerMsg = new Message(
                statussMsg+" "+container.getContainerName(), "Update Status","Discard",
                String.format("Container %s with status %s. Continue %s?",
                        container.getContainerName(),oppStatussMsg,statussMsg));

        confirmIfNecessaryAndExecute(
                isDirty(),
                containerMsg,
                () ->setOpened(true),() -> clear());
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public void clear() {
    }

    @Override
    public void write(DockContainer entity) throws ValidationException {

    }

    private void close() {
        navigateToMainView();
    }

    void navigateToMainView() {
        getUI().ifPresent(ui -> ui.navigate(AppConst.PAGE_CONTAINERS));
    }

    @Override
    public String getEntityName() {
        return AppConst.DOCKER_CONTAINER_NAME;
    }

    @Override
    public void setConfirmDialog(ConfirmDialog confirmDialog) {
        this.confirmation = confirmDialog;
    }

    @Override
    public ConfirmDialog getConfirmDialog() {
        return this.confirmation;
    }

    private void setOpened(boolean opened) {
        dialog.setOpened(opened);
    }

    private void confirmIfNecessaryAndExecute(
            boolean needsConfirmation, Message message, Runnable onConfirmed,
            Runnable onCancelled) {
        if (needsConfirmation) {
            showConfirmationRequest(message, onConfirmed, onCancelled);
        }
        else {
            onConfirmed.run();
        }
    }

    private void showConfirmationRequest(
            Message message, Runnable onOk, Runnable onCancel) {
        getConfirmDialog().setText(message.getMessage());
        getConfirmDialog().setHeader(message.getCaption());
        getConfirmDialog().setCancelText(message.getCancelText());
        getConfirmDialog().setConfirmText(message.getOkText());
        getConfirmDialog().setOpened(true);

        final Registration okRegistration =
                getConfirmDialog().addConfirmListener(e -> onOk.run());
        final Registration cancelRegistration =
                getConfirmDialog().addCancelListener(e -> onCancel.run());
    }

}
