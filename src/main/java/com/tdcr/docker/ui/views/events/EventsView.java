package com.tdcr.docker.ui.views.events;

import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.backend.crud.EventPresenter;
import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.data.entity.Event;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.backend.utils.DataUtil;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.EntityView;
import com.tdcr.docker.ui.views.MainView;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("events-view")
@HtmlImport("src/views/events-view/events-view.html")
@Route(value = AppConst.PAGE_EVENTS, layout = MainView.class)
@PageTitle(AppConst.TITLE_EVENTS)
public class EventsView extends PolymerTemplate<TemplateModel>
		implements HasLogger, EntityView<Event> {

    @Id("search")
    private SearchBar searchBar;

    @Id("grid")
    private Grid<Event> grid;


    EventPresenter presenter;

    @Autowired
    public EventsView (EventPresenter presenter){
        this.presenter = presenter;
        searchBar.setPlaceHolder("Search");

        grid.setSelectionMode(Grid.SelectionMode.NONE);

        grid.addColumn(EventsCard.getTemplate()
                .withProperty("eventsCard", EventsCard::create)
                .withProperty("header", event -> presenter.getHeaderByEventId(event .getId())));
        searchBar.getComboBox().addClassName("hide-drop-down");
        searchBar.getActionButton().setIcon(VaadinIcon.REFRESH.create());
        searchBar.getActionButton().addClickListener(e ->{
            presenter.updateDataGrid();
        });

        searchBar.addSearchValueChangeListener(e ->
                ((ListDataProvider<Event>)grid.getDataProvider()).setFilter(Event::getShortDesc,
                        s -> DataUtil.caseInsensitiveContains(s, searchBar.getFilter())));

        presenter.init(this);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public void write(Event entity) throws ValidationException {

    }

    @Override
    public String getEntityName() {
        return null;
    }

    @Override
    public void setConfirmDialog(ConfirmDialog confirmDialog) {

    }

    @Override
    public ConfirmDialog getConfirmDialog() {
        return null;
    }


    public SearchBar getSearchBar() {
        return searchBar;
    }

    public Grid<Event> getGrid() {
        return grid;
    }
}
