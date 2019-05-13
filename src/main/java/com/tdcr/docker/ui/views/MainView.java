package com.tdcr.docker.ui.views;


import com.tdcr.docker.app.security.SecurityUtils;
import com.tdcr.docker.backend.utils.AppConst;
import com.tdcr.docker.ui.components.AppCookieConsent;
import com.tdcr.docker.ui.views.users.UserView;
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
import com.vaadin.navigator.PushStateNavigation;

import static com.tdcr.docker.backend.utils.AppConst.VIEWPORT;

@Viewport(VIEWPORT)
//@PWA(name = "Curious App Monitor", shortName = "curious",
//        startPath = "login",
//        backgroundColor = "#227aef", themeColor = "#227aef",
//        offlinePath = "offline-page.html",
//        offlineResources = {"images/offline-login-banner.jpg"})
@Push
public class MainView extends AbstractAppRouterLayout {


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
            //setMenuItem(appLayoutMenu, new AppLayoutMenuItem(VaadinIcon.CUBE.create(), AppConst.TITLE_IMAGES, AppConst.PAGE_IMAGES));
            setMenuItem(appLayoutMenu,
                    new AppLayoutMenuItem(VaadinIcon.CUBES.create(),
                            AppConst.TITLE_CONTAINER, AppConst.PAGE_CONTAINERS));
            setMenuItem(appLayoutMenu, new AppLayoutMenuItem(VaadinIcon.LINE_BAR_CHART.create(), AppConst.TITLE_DASHBOARD, AppConst.PAGE_DASHBOARD));
            if (SecurityUtils.isAccessGranted(UserView.class)) {
                setMenuItem(appLayoutMenu, new AppLayoutMenuItem(VaadinIcon.USER.create(), AppConst.TITLE_USERS, AppConst.PAGE_USERS));
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
