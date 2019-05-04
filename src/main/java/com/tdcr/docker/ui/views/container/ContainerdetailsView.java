package com.tdcr.docker.ui.views.container;

import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.utils.AppConst;
import com.tdcr.docker.ui.views.MainView;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A Designer generated component for the containerdetails-view template.
 *
 * Designer will add and remove fields with @Id mappings but
 * does not overwrite or otherwise change this file.
 */
@Tag("containerdetails-view")
@HtmlImport("src/views/container/containerdetails-view.html")
@Route(value = AppConst.PAGE_CONTAINER_DTL, layout = MainView.class)
@PageTitle(AppConst.TITLE_CONTAINER)
public class ContainerdetailsView extends PolymerTemplate<TemplateModel>
        implements HasLogger, HasUrlParameter<Long> {


    @Id("search")
    private SearchBar searchBar;

    /**
     * Creates a new ContainerdetailsView.
     */
    @Autowired
    public ContainerdetailsView() {
        // You can initialise any data required for the connected UI components here.
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long aLong) {
        searchBar.setActionText("New Container");
        searchBar.setCheckboxText("Show stopped conatianers");
        searchBar.setPlaceHolder("Search");
    }
}
