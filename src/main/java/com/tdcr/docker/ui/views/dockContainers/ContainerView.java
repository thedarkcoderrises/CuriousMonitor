package com.tdcr.docker.ui.views.dockContainers;

import com.github.dockerjava.api.model.Statistics;
import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.data.entity.DockImage;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.ComputeStats;
import com.tdcr.docker.backend.utils.DataUtil;
import com.tdcr.docker.ui.components.Message;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.EntityView;
import com.tdcr.docker.ui.views.MainView;
import com.tdcr.docker.ui.views.dashboard.DashboardCounterLabel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.ui.renderers.HtmlRenderer;
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

    private ComboBox<String> dockerComboBox;

    @Id("refresh")
    private Button refreshBtn;

    @Id("logs")
    private Button containerStats;

    @Id("updateStatus")
    private Button updateContainerStatus;

    /**
     * Creates a new Container
     */
    @PostConstruct
    void init() {
        initButtonListeners();
        setupGrid();
        setupSearchBar();
        initStatDialog();
    }

    private void initButtonListeners() {
        refreshBtn.setIcon(VaadinIcon.REFRESH.create());
        refreshBtn.addClickListener(e -> {
            initDataProvider();
            grid.setDataProvider(dataProvider);
        });

        containerStats.setIcon(VaadinIcon.PIE_CHART.create());
        containerStats.addClickListener(e ->{
            containerStatsubWindow();
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
                refreshBtn.click();
            }
        });
        dockerComboBox.setPlaceholder(AppConst.SELECT_PH);
    }

    private void setupSearchBar() {
        searchBar.getActionButton().setIcon(VaadinIcon.REFRESH.create());
        searchBar.getActionButton().addClickListener(e ->{
            initDataProvider();
            grid.setDataProvider(dataProvider);
        });
        searchBar.addSearchValueChangeListener(e ->
                dataProvider.setFilter(DockContainer::getContainerName,
                        s -> DataUtil.caseInsensitiveContains(s, searchBar.getFilter())));
        this.dockerComboBox =searchBar.getComboBox();
        dockerComboBox.setItems(dockerService.getDockerDeamons());
        dockerComboBox.setPlaceholder(AppConst.DOCKER_DEAMON_STR);
        dockerComboBox.addValueChangeListener(e -> {
            if (e.getSource().isEmpty()) {
                Notification.show("Inavlid action!");
            } else {
                dockerService.updateDockerClient(e.getValue());
                searchBar.getActionButton().click();
            }
        });
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
                .setWidth("270px").setHeader("ContainerName").setFlexGrow(3).setSortable(true);
        grid.addColumn(dc -> dc.getImageName())
                .setHeader("ImageName").setWidth("200px").setFlexGrow(3).setSortable(true);
        grid.addColumn(new ComponentRenderer<>(container -> {
            if (container.getStatus().equalsIgnoreCase(AppConst.CONTAINER_UP)) {
                Icon up = VaadinIcon.ARROW_UP.create();
                up.setColor("#7eec65");
                return up;
            } else {
                Icon down = VaadinIcon.ARROW_DOWN.create();
                down.setColor("#f51f06");
                return down;
            }
        })).setHeader("Status").setWidth("150px");
        grid.addColumn(DockContainer::getPort).setHeader("ExposedPort").setWidth("150px");

        grid.addColumn(TemplateRenderer.<DockContainer> of("<a href=\"[[item.link]]\">logs</a>\n")
                .withProperty("link", DockContainer::getURL));
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
    }


    private void initStatDialog() {
        dialog.addDialogCloseActionListener(e ->{
            dialog.setOpened(!dialog.isOpened());
            dialog.removeAll();
        });
    }
    private void containerStatsubWindow() {
        DockContainer container = getSelectedRow();
        if(container == null || !AppConst.CONTAINER_UP.equalsIgnoreCase(container.getStatus())) return;
        dialog.setOpened(true);
        dialog.add(getBoard(container));
    }

    private Component[] getBoard(DockContainer container) {
        final Chart memGauge = new Chart(ChartType.SOLIDGAUGE);
        Statistics stats = dockerService.getContainerRawStats(container.getContainerId());
        Number memUsage = ((Number)stats.getMemoryStats().get("max_usage"));
        Number memLimit = ((Number) stats.getMemoryStats().get("limit"));
        Configuration configuration = memGauge.getConfiguration();
        Pane pane = configuration.getPane();
        pane.setCenter(new String[] {"50%", "50%"});
        pane.setStartAngle(-90);
        pane.setEndAngle(90);

        Background paneBackground = new Background();
        paneBackground.setInnerRadius("60%");
        paneBackground.setOuterRadius("100%");
        paneBackground.setShape(BackgroundShape.ARC);
        pane.setBackground(paneBackground);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTickAmount(2);
        yAxis.setTitle("Memory");
        yAxis.setMinorTickInterval("null");
        yAxis.getTitle().setY(-50);
        yAxis.getLabels().setY(16);
        yAxis.setMin(0);
        PlotOptionsSolidgauge plotOptionsSolidgauge = new PlotOptionsSolidgauge();

        DataLabels dataLabels = plotOptionsSolidgauge.getDataLabels();
        dataLabels.setY(5);
        dataLabels.setUseHTML(true);
        configuration.setPlotOptions(plotOptionsSolidgauge);

        DataSeries series = new DataSeries("Memory Usage");
        DataSeriesItem item = new DataSeriesItem();
        item.setClassName("myClassName");
        yAxis.setMax(memLimit);
        item.setY(memUsage);
        DataLabels dataLabelsSeries = new DataLabels();
        final String dataLabelsStr= "<br><div style=\"text-align:center\"><span style=\"font-size:25px;"
                + "color:black' + '\">%s</span><br/>"
                + "<span style=\"font-size:12px;color:silver\">r/w</span></div>";
        dataLabelsSeries.setFormat(String.format(dataLabelsStr,ComputeStats.calculateSize(memUsage.longValue())));
        item.setDataLabels(dataLabelsSeries);
        series.add(item);
        configuration.addSeries(series);
        DashboardCounterLabel networkIO = new DashboardCounterLabel().init();
        networkIO.setData("NET I/O","",ComputeStats.calcNetworkStats(stats));

        DashboardCounterLabel memusageInPercentile = new DashboardCounterLabel().init();
        memusageInPercentile.setData("Current Usage%","", ComputeStats.computeMemoryInPercent(stats));
        return new Component[]{memGauge,networkIO.getTitle(),networkIO.getValue(),memusageInPercentile.getTitle(),memusageInPercentile.getValue()};
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
        boolean status = !container.getStatus().equalsIgnoreCase(AppConst.CONTAINER_UP);
        String statussMsg = status == true? "Run" :"Stop";
        String oppStatussMsg =status != true? "Run" :"Stop";

        Message containerMsg = new Message(
                statussMsg+" "+container.getContainerName().toUpperCase(),
                "Update Status","Discard",
                String.format("Container %s current status = %s. Continue %s?",
                        container.getContainerName().toUpperCase(),oppStatussMsg,statussMsg));

        confirmIfNecessaryAndExecute(
                isDirty(),
                containerMsg,
                () ->{
                   String containerID = dockerService.updateContainerStatus(container.getContainerId(),status);
                   if(container.getContainerId().equals(containerID)){
                       Notification.show("Saved status");
                       refreshBtn.click();
                   }
                },() -> clear());
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
