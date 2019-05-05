package com.tdcr.docker.ui.views.dockContainers;

import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.DataUtil;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.MainView;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

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
public class ContainerView extends PolymerTemplate<TemplateModel>{

    @Id("search")
    private SearchBar searchBar;

    @Id("grid")
    private Grid<DockContainer> grid;

    @Id("dialog")
    private Dialog dialog;

    @Autowired
    private DockerService dockerService;

    private ListDataProvider<DockContainer> dataProvider;

    /**
     * Creates a new ContainerView.
     */
    @PostConstruct
    void init() {
        initDockerClient();
        initDataProvider();
        setupGrid();
        setupSearchBar();
    }

    private void initDataProvider() {
       this.dataProvider= DataProvider.ofCollection(dockerService.listAllContainers(null));
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

    private void initDockerClient() {
        this.dockerService.updateDockerClient("LOCAL_DD");
    }

    private void setupGrid() {
        setGridDataProvider();
        addGridColumns();
    }

    private void setGridDataProvider() {
        grid.setDataProvider(dataProvider);
    }

    private void addGridColumns() {
        grid.addColumn(DockContainer::getContainerName)
                .setWidth("270px").setHeader("ContainerName").setFlexGrow(5).setSortable(true);
        grid.addColumn(dc -> dc.getImageName())
                .setHeader("ImageName").setWidth("200px").setFlexGrow(5).setSortable(true);
        grid.addColumn(DockContainer::getStatus).setHeader("Status").setWidth("150px");
        grid.setSelectionMode(Grid.SelectionMode.NONE);
    }
}
