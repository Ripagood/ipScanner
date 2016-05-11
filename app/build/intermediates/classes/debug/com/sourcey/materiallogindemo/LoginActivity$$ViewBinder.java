// Generated code from Butter Knife. Do not modify!
package com.sourcey.materiallogindemo;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class LoginActivity$$ViewBinder<T extends com.sourcey.materiallogindemo.LoginActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624039, "field '_emailText'");
    target._emailText = finder.castView(view, 2131624039, "field '_emailText'");
    view = finder.findRequiredView(source, 2131624040, "field '_passwordText'");
    target._passwordText = finder.castView(view, 2131624040, "field '_passwordText'");
    view = finder.findRequiredView(source, 2131624041, "field '_loginButton'");
    target._loginButton = finder.castView(view, 2131624041, "field '_loginButton'");
    view = finder.findRequiredView(source, 2131624042, "field '_SkiplLoginButton'");
    target._SkiplLoginButton = finder.castView(view, 2131624042, "field '_SkiplLoginButton'");
    view = finder.findRequiredView(source, 2131624043, "field '_signupLink'");
    target._signupLink = finder.castView(view, 2131624043, "field '_signupLink'");
  }

  @Override public void unbind(T target) {
    target._emailText = null;
    target._passwordText = null;
    target._loginButton = null;
    target._SkiplLoginButton = null;
    target._signupLink = null;
  }
}
