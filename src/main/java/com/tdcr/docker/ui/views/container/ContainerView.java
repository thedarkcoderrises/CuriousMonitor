package com.tdcr.docker.ui.views.container;

import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.backend.data.entity.Container;
import com.tdcr.docker.backend.data.entity.util.EntityUtil;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.ui.views.EntityView;
import com.tdcr.docker.ui.views.MainView;
import com.tdcr.docker.utils.AppConst;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

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
public class ContainerView extends PolymerTemplate<TemplateModel>
        implements HasLogger, HasUrlParameter<Long>, EntityView<Container> {

    @Id("search")
    private SearchBar searchBar;

    @Id("grid")
    private Grid<Container> grid;

    @Id("dialog")
    private Dialog dialog;

    private ConfirmDialog confirmation;

    private ContainerPresenter presenter;

    /**
     * Creates a new ContainerDtlView.
     */
    @Autowired
    public ContainerView(ContainerPresenter presenter) {
        this.presenter = presenter;
        searchBar.setActionText("New "+ EntityUtil.getName(Container.class));
        searchBar.setCheckboxText("Show stop containers");
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        presenter.init(this);
        dialog.addDialogCloseActionListener(e -> presenter.cancel());
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long aLong) {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public void write(Container entity) throws ValidationException {

    }

    @Override
    public String getEntityName() {
        return null;
    }

    @Override
    public void setConfirmDialog(ConfirmDialog confirmDialog) {
        this.confirmation = confirmDialog;
    }

    @Override
    public ConfirmDialog getConfirmDialog() {
        return confirmation;
    }
    void setOpened(boolean opened) {
        dialog.setOpened(opened);
    }

    void navigateToMainView() {
        getUI().ifPresent(ui -> ui.navigate(AppConst.PAGE_CONTAINERS));
    }
}
