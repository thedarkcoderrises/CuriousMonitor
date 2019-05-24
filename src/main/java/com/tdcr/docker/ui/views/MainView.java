package com.tdcr.docker.ui.views;


import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.app.security.SecurityUtils;
import com.tdcr.docker.backend.data.entity.DockContainer;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.ui.components.AppCookieConsent;
import com.tdcr.docker.ui.views.dashboard.DashboardView;
import com.tdcr.docker.ui.views.dockContainers.ContainerView;
import com.tdcr.docker.ui.views.users.UserView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AbstractAppRouterLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayoutMenu;
import com.vaadin.flow.component.applayout.AppLayoutMenuItem;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.PWA;
import com.vaadin.navigator.PushStateNavigation;

import java.util.concurrent.Future;

import static com.tdcr.docker.backend.utils.AppConst.VIEWPORT;

@Viewport(VIEWPORT)
@Push
public class MainView extends AbstractAppRouterLayout implements HasLogger {


    private final ConfirmDialog confirmDialog;

    public MainView() {
        this.confirmDialog = new ConfirmDialog();
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmButtonTheme("raised tertiary error");
        confirmDialog.setCancelButtonTheme("raised tertiary");
        getElement().appendChild(confirmDialog.getElement());
        getElement().appendChild(new AppCookieConsent().getElement());
    }
    @Override
    protected void configure(AppLayout appLayout, AppLayoutMenu appLayoutMenu) {
        appLayout.setBranding(new Span(AppConst.APP_NAME));

        if (SecurityUtils.isUserLoggedIn()) {
            if (SecurityUtils.isAccessGranted(ContainerView.class)) {
                setMenuItem(appLayoutMenu,
                        new AppLayoutMenuItem(VaadinIcon.CUBES.create(),
                                AppConst.TITLE_CONTAINER, AppConst.PAGE_CONTAINERS));
            }

            setMenuItem(appLayoutMenu, new AppLayoutMenuItem(VaadinIcon.LINE_BAR_CHART.create(),
                        AppConst.TITLE_DASHBOARD, AppConst.PAGE_DASHBOARD));

            setMenuItem(appLayoutMenu,new AppLayoutMenuItem(VaadinIcon.FLAG.create(),
                    AppConst.TITLE_EVENTS, AppConst.PAGE_EVENTS));
            if (SecurityUtils.isAccessGranted(UserView.class)) {
                setMenuItem(appLayoutMenu, new AppLayoutMenuItem(VaadinIcon.USER.create(),
                        AppConst.TITLE_USERS, AppConst.PAGE_USERS));
            }

            setMenuItem(appLayoutMenu, new AppLayoutMenuItem(VaadinIcon.ARROW_RIGHT.create(), AppConst.TITLE_LOGOUT, e ->
                    UI.getCurrent().getPage().executeJavaScript("location.assign('logout')")));

        }
        getElement().addEventListener("search-focus", e -> {
            appLayout.getElement().getClassList().add("hide-navbar");
        });

        getElement().addEventListener("search-blur", e -> {
            appLayout.getElement().getClassList().remove("hide-navbar");
        });
    }

    private void setMenuItem(AppLayoutMenu menu, AppLayoutMenuItem menuItem) {
        menuItem.getElement().setAttribute("theme", "icon-on-top");
        menu.addMenuItem(menuItem);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        super.showRouterLayoutContent(content);
        this.confirmDialog.setOpened(false);
        if (content instanceof HasConfirmation) {
            ((HasConfirmation) content).setConfirmDialog(this.confirmDialog);
        }
    }
}
