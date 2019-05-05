package com.tdcr.docker.ui.components;


import com.tdcr.docker.backend.data.entity.DockContainer;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;

@Deprecated
public class FilteredGridLayout extends HorizontalLayout {

    private Grid grid;
    private final TextField containerNameFilter;
    private final TextField imageNameFilter;


    public FilteredGridLayout(Grid grid) {
        containerNameFilter = new TextField();
        containerNameFilter.setPlaceholder("Container...");
        containerNameFilter.addValueChangeListener(this::onContainerNameFilterTextChange);
        containerNameFilter.setHeight("50%");
        imageNameFilter = new TextField();
        imageNameFilter.setPlaceholder("Image...");
        imageNameFilter.addValueChangeListener(this::onImageNameFilterTextChange);
        imageNameFilter.setHeight("50%");
        this.grid = grid;
    }

    private void onContainerNameFilterTextChange(HasValue.ValueChangeEvent<String> event) {
        ListDataProvider<DockContainer> dataProvider = (ListDataProvider<DockContainer>) grid.getDataProvider();
        dataProvider.setFilter(DockContainer::getContainerName, s -> caseInsensitiveContains(s, event.getValue()));
    }


    private void onImageNameFilterTextChange(HasValue.ValueChangeEvent<String> event) {
        ListDataProvider<DockContainer> dataProvider = (ListDataProvider<DockContainer>) grid.getDataProvider();
        dataProvider.setFilter(DockContainer::getImageName, s -> caseInsensitiveContains(s, event.getValue()));
    }

    private Boolean caseInsensitiveContains(String where, String what) {
        return where.toLowerCase().contains(what.toLowerCase());
    }

    public TextField getContainerNameFilter() {
        return containerNameFilter;
    }

    public TextField getImageNameFilter() {
        return imageNameFilter;
    }
}