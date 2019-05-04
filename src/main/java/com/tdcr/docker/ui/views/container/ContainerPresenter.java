package com.tdcr.docker.ui.views.container;

import com.tdcr.docker.backend.data.entity.Container;
import com.tdcr.docker.ui.crud.EntityPresenter;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ContainerPresenter {

    private final EntityPresenter<Container, ContainerView> entityPresenter;
    private ContainerView view;

    @Autowired
    ContainerPresenter(EntityPresenter<Container, ContainerView> entityPresenter){
        this.entityPresenter = entityPresenter;
    }

    void init(ContainerView view){
        this.entityPresenter.setView(view);
        this.view = view;
    }

    public void cancel() {
        entityPresenter.cancel(() -> close(), () -> view.setOpened(true));
    }

    private void close() {
        view.setOpened(false);
        view.navigateToMainView();
        entityPresenter.close();
    }
}
