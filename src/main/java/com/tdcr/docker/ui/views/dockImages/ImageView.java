package com.tdcr.docker.ui.views.dockImages;

import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.data.entity.DockImage;
import com.tdcr.docker.backend.utils.EntityUtil;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.MainView;
import com.tdcr.docker.backend.utils.AppConst;
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
 * A Designer generated component for the container-view template.
 *
 * Designer will add and remove fields with @Id mappings but
 * does not overwrite or otherwise change this file.
 */
@Tag("image-view")
@HtmlImport("src/views/dock-images/image-view.html")
@Route(value = AppConst.PAGE_IMAGES, layout = MainView.class)
@PageTitle(AppConst.TITLE_IMAGES)
public class ImageView extends PolymerTemplate<TemplateModel>{

    @Id("search")
    private SearchBar searchBar;

    @Id("grid")
    private Grid<DockContainer> grid;

    @Id("dialog")
    private Dialog dialog;

    /**
     * Creates a new ContainerView.
     */
    public ImageView() {
        searchBar.setActionText("New "+ EntityUtil.getName(DockImage.class));
        searchBar.setCheckboxText("Show stop containers");
        grid.setSelectionMode(Grid.SelectionMode.NONE);
    }

}
