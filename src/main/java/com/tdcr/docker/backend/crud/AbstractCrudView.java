package com.tdcr.docker.backend.crud;

import com.tdcr.docker.app.security.CurrentUser;
import com.tdcr.docker.backend.data.entity.AbstractEntity;
import com.tdcr.docker.backend.utils.EntityUtil;
import com.tdcr.docker.backend.service.FilterableCrudService;
import com.tdcr.docker.ui.components.SearchBar;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.ui.views.HasNotifications;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudI18n;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import elemental.json.Json;

import java.util.function.Consumer;

public abstract class AbstractCrudView<E extends AbstractEntity> extends Crud<E>
        implements HasUrlParameter<Long>, HasNotifications {


    private final Grid<E> grid;

    private final CrudEntityPresenter<E> entityPresenter;

    protected abstract String getBasePage();

    protected abstract void setupGrid(Grid<E> grid);

    public AbstractCrudView(Class<E> beanType, FilterableCrudService<E> service,
                                  Grid<E> grid, CrudEditor<E> editor, CurrentUser currentUser) {
        super(beanType, grid, editor);
        this.grid = grid;
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        CrudI18n crudI18n = CrudI18n.createDefault();
        String entityName = EntityUtil.getName(beanType);
        crudI18n.setNewItem("New " + entityName);
        crudI18n.setEditItem("Edit " + entityName);
        crudI18n.setEditLabel("Edit " + entityName);
        crudI18n.getConfirm().getCancel().setContent(String.format(AppConst.DISCARD_MESSAGE, entityName));
        crudI18n.getConfirm().getDelete().setContent(String.format(AppConst.DELETE_MESSAGE, entityName));
        crudI18n.getConfirm().getDelete().setTitle("Delete "+entityName);
        crudI18n.setDeleteItem("Delete");
        setI18n(crudI18n);

        CrudEntityDataProvider<E> dataProvider = new CrudEntityDataProvider<>(service);
        grid.setDataProvider(dataProvider);
        setupGrid(grid);
        Crud.addEditColumn(grid);//grid row edit

        entityPresenter = new CrudEntityPresenter<>(service, currentUser, this);

        SearchBar searchBar = new SearchBar();
        searchBar.setActionText("New " + entityName);
        searchBar.setPlaceHolder("Search");
        searchBar.addFilterChangeListener(e -> dataProvider.setFilter(searchBar.getFilter()));
        searchBar.getActionButton().getElement().setAttribute("new-button", true);
        setToolbar(searchBar);
        setupCrudEventListeners(entityPresenter);
    }

    private void setupCrudEventListeners(CrudEntityPresenter<E> entityPresenter) {
        Consumer<E> onSuccess = entity -> navigateToEntity(null);
        Consumer<E> onFail = entity -> {
            // TODO: https://github.com/vaadin/vaadin-crud-flow/issues/76
            // Throw an exception whenever it is supported by component
        };

        addEditListener(e ->
                entityPresenter.loadEntity(e.getItem().getId(),
                        entity -> navigateToEntity(entity.getId().toString())));

        addCancelListener(e -> navigateToEntity(null));

        addSaveListener(e ->
                entityPresenter.save(e.getItem(), onSuccess, onFail));

        addDeleteListener(e ->
                entityPresenter.delete(e.getItem(), onSuccess, onFail));
    }

    protected void navigateToEntity(String id) {
        getUI().ifPresent(ui -> ui.navigate(EntityUtil.generateLocation(getBasePage(), id)));
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        if (id != null) {
            E item = getEditor().getItem();
            if (item != null && id.equals(item.getId())) {
                return;
            }
            entityPresenter.loadEntity(id, this::edit);
        }
    }

    private void edit(E entity) {
        // TODO: Use component API after https://github.com/vaadin/vaadin-crud-flow/issues/68
        getElement().callFunction("__edit", Json.instance().parse("{\"key\":\""
                + grid.getDataCommunicator().getKeyMapper().key(entity) + "\"}"));
    }
}
