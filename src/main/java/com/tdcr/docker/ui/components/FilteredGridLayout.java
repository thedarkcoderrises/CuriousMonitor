package com.tdcr.docker.ui.components;


import com.tdcr.docker.backend.data.entity.DockContainer;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.springframework.util.StringUtils;

import java.util.Set;
@Deprecated
public class FilteredGridLayout extends HorizontalLayout {

    private Grid grid;
    private TextField textFiler;
    private ComboBox<String> comboBox;


    public FilteredGridLayout(Grid grid) {
        this.grid = grid;
    }

    public void addTextFilterToColumnHeader(String placeHolder,String heightInPercentile){
        textFiler = new TextField();
        textFiler.setPlaceholder(placeHolder);
        textFiler.addValueChangeListener(this::onTextFilterTextChange);
        textFiler.setHeight(heightInPercentile);
    }

    private void onTextFilterTextChange(HasValue.ValueChangeEvent<String> event) {
        ListDataProvider<DockContainer> dataProvider = (ListDataProvider<DockContainer>) grid.getDataProvider();
        dataProvider.setFilter(DockContainer::getImageName, s -> caseInsensitiveContains(s, event.getValue()));
    }

    public static boolean caseInsensitiveContains(String where, String what) {
        if(StringUtils.isEmpty(what)) return false;
        return (where.toLowerCase().contains(what.toLowerCase()) || what.toLowerCase().contains(where.toLowerCase()));
    }


    public TextField getTextFiler() {
        return textFiler;
    }

    public ComboBox<String> getComboBox(Set<String> itemSet, String place, String width) {
        comboBox = new ComboBox<>();
        comboBox.setItems(itemSet);
        comboBox.setWidth(width);
        return comboBox;
    }
}