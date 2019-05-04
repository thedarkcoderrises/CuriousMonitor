package com.tdcr.docker.ui.views.login;

import com.tdcr.docker.app.security.SecurityUtils;
import com.tdcr.docker.ui.components.AppCookieConsent;
import com.tdcr.docker.ui.utils.AppConst;
import com.tdcr.docker.ui.views.MainView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.*;

@Route
@PageTitle(AppConst.PAGE_TITLE)
@HtmlImport("styles/shared-styles.html")
@Viewport(AppConst.VIEWPORT)
public class LoginView extends VerticalLayout
        implements AfterNavigationObserver, BeforeEnterObserver {
    private LoginOverlay login = new LoginOverlay();

    public LoginView() {
        getElement().appendChild(
                new AppCookieConsent().getElement(), login.getElement());

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("curious");
        i18n.getHeader().setDescription(
                "curious@tdcr.com + admin");
        i18n.setAdditionalInformation(null);
        i18n.setForm(new LoginI18n.Form());
        i18n.getForm().setSubmit("Sign in");
        i18n.getForm().setTitle("Sign in");
        i18n.getForm().setUsername("Email");
        i18n.getForm().setPassword("Password");
        login.setI18n(i18n);
        login.setForgotPasswordButtonVisible(false);
        login.setAction("login");
        login.setOpened(true);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        login.setError(
                afterNavigationEvent.getLocation().getQueryParameters().getParameters().containsKey(
                        "error"));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (SecurityUtils.isUserLoggedIn()) {
            // Needed manually to change the URL because of https://github.com/vaadin/flow/issues/4189
            UI.getCurrent().getPage().getHistory().replaceState(null, "");
            beforeEnterEvent.rerouteTo(String.valueOf(MainView.class));
        }
    }
}
