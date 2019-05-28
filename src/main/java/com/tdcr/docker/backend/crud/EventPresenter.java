package com.tdcr.docker.backend.crud;

import com.tdcr.docker.app.security.CurrentUser;
import com.tdcr.docker.backend.data.entity.Event;
import com.tdcr.docker.backend.repositories.EventsRepository;
import com.tdcr.docker.ui.components.EventsCardHeader;
import com.tdcr.docker.ui.components.EventsCardHeaderGenerator;
import com.tdcr.docker.ui.views.events.EventsView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.List;

@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EventPresenter {

    private EventsCardHeaderGenerator headersGenerator = new EventsCardHeaderGenerator();
    private EventsView view;
    private CurrentUser currentUser;
    private EventsRepository repository;
    private DataProvider dataProvider;

    private EntityPresenter<Event, EventsView> entityPresenter;

    @Autowired
    EventPresenter(EntityPresenter<Event, EventsView> entityPresenter, CurrentUser currentUser, EventsRepository repository){
        this.entityPresenter = entityPresenter;
        this.currentUser =currentUser;
        this.repository = repository;
        headersGenerator.resetHeaderChain(true);
        List<Event> lst = repository.findAll();
        headersGenerator.ordersRead(lst);
    }

    public EventsCardHeader getHeaderByEventId(Long id) {
        return headersGenerator.get(id);
    }


    public void init(EventsView view) {
        this.entityPresenter.setView(view);
        this.view = view;
        updateDataGrid();
    }

    public void updateDataGrid() {
        this.dataProvider = DataProvider.ofCollection(repository.findAll());
        view.getGrid().setDataProvider(dataProvider);
    }
}
