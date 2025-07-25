// Generated by view binder compiler. Do not edit!
package com.app.smart.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.app.smart.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySettingsBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final MaterialButton btnClearHistory;

  @NonNull
  public final Slider sliderAcceleration;

  @NonNull
  public final Slider sliderRotation;

  @NonNull
  public final Spinner spinnerSensitivity;

  @NonNull
  public final SwitchMaterial switchAutoCall;

  @NonNull
  public final SwitchMaterial switchAutoSms;

  @NonNull
  public final SwitchMaterial switchDarkMode;

  @NonNull
  public final Toolbar toolbar;

  private ActivitySettingsBinding(@NonNull CoordinatorLayout rootView,
      @NonNull MaterialButton btnClearHistory, @NonNull Slider sliderAcceleration,
      @NonNull Slider sliderRotation, @NonNull Spinner spinnerSensitivity,
      @NonNull SwitchMaterial switchAutoCall, @NonNull SwitchMaterial switchAutoSms,
      @NonNull SwitchMaterial switchDarkMode, @NonNull Toolbar toolbar) {
    this.rootView = rootView;
    this.btnClearHistory = btnClearHistory;
    this.sliderAcceleration = sliderAcceleration;
    this.sliderRotation = sliderRotation;
    this.spinnerSensitivity = spinnerSensitivity;
    this.switchAutoCall = switchAutoCall;
    this.switchAutoSms = switchAutoSms;
    this.switchDarkMode = switchDarkMode;
    this.toolbar = toolbar;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySettingsBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySettingsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_settings, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySettingsBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btn_clear_history;
      MaterialButton btnClearHistory = ViewBindings.findChildViewById(rootView, id);
      if (btnClearHistory == null) {
        break missingId;
      }

      id = R.id.slider_acceleration;
      Slider sliderAcceleration = ViewBindings.findChildViewById(rootView, id);
      if (sliderAcceleration == null) {
        break missingId;
      }

      id = R.id.slider_rotation;
      Slider sliderRotation = ViewBindings.findChildViewById(rootView, id);
      if (sliderRotation == null) {
        break missingId;
      }

      id = R.id.spinner_sensitivity;
      Spinner spinnerSensitivity = ViewBindings.findChildViewById(rootView, id);
      if (spinnerSensitivity == null) {
        break missingId;
      }

      id = R.id.switch_auto_call;
      SwitchMaterial switchAutoCall = ViewBindings.findChildViewById(rootView, id);
      if (switchAutoCall == null) {
        break missingId;
      }

      id = R.id.switch_auto_sms;
      SwitchMaterial switchAutoSms = ViewBindings.findChildViewById(rootView, id);
      if (switchAutoSms == null) {
        break missingId;
      }

      id = R.id.switch_dark_mode;
      SwitchMaterial switchDarkMode = ViewBindings.findChildViewById(rootView, id);
      if (switchDarkMode == null) {
        break missingId;
      }

      id = R.id.toolbar;
      Toolbar toolbar = ViewBindings.findChildViewById(rootView, id);
      if (toolbar == null) {
        break missingId;
      }

      return new ActivitySettingsBinding((CoordinatorLayout) rootView, btnClearHistory,
          sliderAcceleration, sliderRotation, spinnerSensitivity, switchAutoCall, switchAutoSms,
          switchDarkMode, toolbar);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
