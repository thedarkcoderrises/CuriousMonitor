package com.tdcr.docker.ui.views.containers;

import com.tdcr.docker.backend.data.entity.Container;
import com.tdcr.docker.backend.data.entity.util.EntityUtil;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.MainView;
import com.tdcr.docker.utils.AppConst;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

/**
 * A Designer generated component for the container-dtl-view template.
 *
 * Designer will add and remove fields with @Id mappings but
 * does not overwrite or otherwise change this file.
 */
@Tag("container-view")
@HtmlImport("src/views/container/container-view.html")
@Route(value = AppConst.PAGE_CONTAINERS, layout = MainView.class)
@PageTitle(AppConst.TITLE_CONTAINER)
public class ContainerView extends PolymerTemplate<TemplateModel>{

    @Id("search")
    private SearchBar searchBar;

    @Id("grid")
    private Grid<Container> grid;

    @Id("dialog")
    private Dialog dialog;
    @Id("dialog")
    private Dialog vaadinDialog;

    /**
     * Creates a new ContainerView.
     */
    public ContainerView() {
        searchBar.setActionText("New "+ EntityUtil.getName(Container.class));
        searchBar.setCheckboxText("Show stop containers");
        grid.setSelectionMode(Grid.SelectionMode.NONE);
    }

}
