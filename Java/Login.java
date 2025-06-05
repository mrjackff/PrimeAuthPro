package com.chad;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import java.io.File;
import java.util.List;

public class Login {

    private static final String APP_ID = "c379560000";
    private static final String SECRET_KEY = "8ad92d0a650a178cb642";
    private static final String VERSION = "2.0";

    private static final String[] ALLOWED_PACKAGES = {
            "Admin",
            "Premium",
            "VIP",
            "Moderator"
    };

    private Context context;
    private Utils utils;
    private ImageString imageString;
    private SharedPreferences sharedPreferences;
    private Switch suToggle;
    private TextView rootBypassStatus;

    // UI Components
    private LinearLayout card;
    private Button loginBtn;
    private Button forceStopBtn;
    private Button mainMenuBtn;
    private ProgressBar loadingSpinner;
    private TextView loadingText;
    private LinearLayout loadingContainer;
    private EditText userInput, passInput;
    private LinearLayout rootBypassSection;
    private LinearLayout actionButtonsContainer;

    // Theme colors
    private boolean isDarkMode;
    private ThemeColors colors;

    public Login(Context context) {
        this.context = context;
        this.utils = new Utils(context);
        this.imageString = new ImageString();
        this.sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        // Detect system theme
        detectSystemTheme();

        buildUI();
    }

    private void detectSystemTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 11+ (API 30+)
            int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29)
            int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        } else {
            // Fallback for older versions - default to dark mode
            isDarkMode = true;
        }

        colors = new ThemeColors(isDarkMode);
    }

    private static class ThemeColors {
        final String background1, background2;
        final String cardBackground;
        final String inputBackground;
        final String textPrimary, textSecondary, textHint;
        final String buttonBackground, buttonText;
        final String accentColor;
        final String successColor, errorColor;
        final String dividerColor;
        final String warningColor;

        ThemeColors(boolean isDark) {
            if (isDark) {
                // Dark theme colors
                background1 = "#0F0F23";
                background2 = "#1A1A2E";
                cardBackground = "#1A1A2E";
                inputBackground = "#262640";
                textPrimary = "#FFFFFF";
                textSecondary = "#8A8A9A";
                textHint = "#8A8A9A";
                buttonBackground = "#9406C7";
                buttonText = "#FFFFFF";
                accentColor = "#0066FF";
                successColor = "#00CC66";
                errorColor = "#FF4444";
                dividerColor = "#333344";
                warningColor = "#FF8C00";
            } else {
                // Light theme colors
                background1 = "#F5F7FA";
                background2 = "#E8EBF0";
                cardBackground = "#FFFFFF";
                inputBackground = "#F8F9FA";
                textPrimary = "#1A1A2E";
                textSecondary = "#666677";
                textHint = "#999AAA";
                buttonBackground = "#9406C7";
                buttonText = "#FFFFFF";
                accentColor = "#0066FF";
                successColor = "#00AA44";
                errorColor = "#DD3333";
                dividerColor = "#E0E0E8";
                warningColor = "#FF6600";
            }
        }
    }

    private void buildUI() {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Background with theme-aware gradient
        FrameLayout parent = new FrameLayout(context);
        parent.setBackground(createThemeGradientBackground());

        // Main card
        card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dpToPx(15), dpToPx(10), dpToPx(15), dpToPx(10));
        card.setBackground(Utils.createRoundedDrawable(24, Color.parseColor(colors.cardBackground)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(dpToPx(isDarkMode ? 12 : 8));
        }

        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                dpToPx(400), ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        card.setLayoutParams(cardParams);

        // Logo
        ImageBase64 logo = new ImageBase64(context);
        logo.setImageBase64(imageString.icon_image);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dpToPx(80), dpToPx(80));
        logoParams.gravity = Gravity.CENTER_HORIZONTAL;
        logoParams.bottomMargin = dpToPx(16);
        logo.setLayoutParams(logoParams);
        card.addView(logo);

        // Title
        TextView title = new TextView(context);
        title.setText("SUKUNA REGEDIT");
        title.setTextColor(Color.parseColor(colors.textPrimary));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setPadding(0, 0, 0, dpToPx(8));
        card.addView(title);

        // Subtitle
        TextView subtitle = new TextView(context);
        subtitle.setText("Best Panel Ever");
        subtitle.setTextColor(Color.parseColor(colors.textSecondary));
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        subtitle.setPadding(0, 0, 0, dpToPx(32));
        card.addView(subtitle);

        // Input fields
        userInput = createInput("Username");
        passInput = createInput("Password");
        passInput.setTransformationMethod(PasswordTransformationMethod.getInstance());

        userInput.setText(sharedPreferences.getString("username", ""));
        passInput.setText(sharedPreferences.getString("password", ""));

        card.addView(userInput);
        card.addView(passInput);

        // Login button
        loginBtn = createLoginButton();
        card.addView(loginBtn);

        // Loading container (hidden initially)
        loadingContainer = createLoadingContainer();
        card.addView(loadingContainer);

        // Action buttons container (hidden initially)
        actionButtonsContainer = createActionButtonsContainer();
        card.addView(actionButtonsContainer);

        // Root bypass section (hidden initially)
        rootBypassSection = createRootBypassSection();
        card.addView(rootBypassSection);

        // Footer
        TextView footer = new TextView(context);
        footer.setText("© 2025 PrimeJack");
        footer.setTextColor(Color.parseColor(colors.textSecondary));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dpToPx(32), 0, 0);
        card.addView(footer);

        parent.addView(card);
        ((Activity) context).setContentView(parent);

        // Setup click listeners
        setupClickListeners(androidId);

        // Start entrance animation
        startEntranceAnimation();
    }

    private android.graphics.drawable.Drawable createThemeGradientBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            android.graphics.drawable.GradientDrawable gradient = new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            Color.parseColor(colors.background1),
                            Color.parseColor(colors.background2)
                    }
            );
            return gradient;
        } else {
            // Fallback for older Android versions
            android.graphics.drawable.ColorDrawable solid = new android.graphics.drawable.ColorDrawable(
                    Color.parseColor(colors.background1)
            );
            return solid;
        }
    }

    private EditText createInput(String hint) {
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setBackground(Utils.createRoundedDrawable(16, Color.parseColor(colors.inputBackground)));
        et.setTextColor(Color.parseColor(colors.textPrimary));
        et.setHintTextColor(Color.parseColor(colors.textHint));
        et.setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(18));
        et.setTextSize(16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dpToPx(12), 0, 0);
        et.setLayoutParams(params);

        return et;
    }

    private Button createLoginButton() {
        Button btn = new Button(context);
        btn.setText("LOGIN");
        btn.setAllCaps(true);
        btn.setTextColor(Color.parseColor(colors.buttonText));
        btn.setTextSize(16);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setBackground(Utils.createRoundedDrawable(20, Color.parseColor(colors.buttonBackground)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(56));
        params.setMargins(0, dpToPx(32), 0, 0);
        btn.setLayoutParams(params);

        return btn;
    }

    private LinearLayout createLoadingContainer() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        container.setVisibility(View.GONE);
        container.setPadding(0, dpToPx(24), 0, 0);

        loadingSpinner = new ProgressBar(context);
        loadingSpinner.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadingSpinner.getIndeterminateDrawable().setColorFilter(
                    Color.parseColor(colors.accentColor), PorterDuff.Mode.SRC_IN);
        }

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                dpToPx(24), dpToPx(24));
        spinnerParams.setMargins(0, 0, dpToPx(12), 0);
        loadingSpinner.setLayoutParams(spinnerParams);

        loadingText = new TextView(context);
        loadingText.setText("Authenticating...");
        loadingText.setTextColor(Color.parseColor(colors.accentColor));
        loadingText.setTextSize(14);

        container.addView(loadingSpinner);
        container.addView(loadingText);

        return container;
    }

    private LinearLayout createActionButtonsContainer() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setVisibility(View.GONE);
        container.setPadding(0, dpToPx(16), 0, 0);

        // Force Stop button
        forceStopBtn = new Button(context);
        forceStopBtn.setText("FORCE STOP");
        forceStopBtn.setAllCaps(true);
        forceStopBtn.setTextColor(Color.parseColor(colors.buttonText));
        forceStopBtn.setTextSize(14);
        forceStopBtn.setTypeface(null, Typeface.BOLD);
        forceStopBtn.setBackground(Utils.createRoundedDrawable(18, Color.parseColor(colors.warningColor)));

        LinearLayout.LayoutParams forceStopParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(48));
        forceStopParams.setMargins(0, dpToPx(12), 0, 0);
        forceStopBtn.setLayoutParams(forceStopParams);

        // Main Menu button
        mainMenuBtn = new Button(context);
        mainMenuBtn.setText("MAIN MENU");
        mainMenuBtn.setAllCaps(true);
        mainMenuBtn.setTextColor(Color.parseColor(colors.buttonText));
        mainMenuBtn.setTextSize(14);
        mainMenuBtn.setTypeface(null, Typeface.BOLD);
        mainMenuBtn.setBackground(Utils.createRoundedDrawable(18, Color.parseColor(colors.accentColor)));

        LinearLayout.LayoutParams mainMenuParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(48));
        mainMenuParams.setMargins(0, dpToPx(12), 0, 0);
        mainMenuBtn.setLayoutParams(mainMenuParams);

        container.addView(forceStopBtn);
        container.addView(mainMenuBtn);

        return container;
    }

    private LinearLayout createRootBypassSection() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16));
        container.setBackground(Utils.createRoundedDrawable(16, Color.parseColor(colors.inputBackground)));
        container.setVisibility(View.GONE); // Hidden initially

        ImageView icon = new ImageView(context);
        icon.setImageResource(android.R.drawable.ic_lock_power_off);
        // Tint icon based on theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon.setColorFilter(Color.parseColor(colors.textSecondary), PorterDuff.Mode.SRC_IN);
        }
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(20), dpToPx(20));
        iconParams.setMargins(0, 0, dpToPx(12), 0);
        icon.setLayoutParams(iconParams);
        container.addView(icon);

        TextView label = new TextView(context);
        label.setText("Root Bypass");
        label.setTextColor(Color.parseColor(colors.textPrimary));
        label.setTextSize(14);
        container.addView(label);

        Space space = new Space(context);
        LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(0, 0, 1f);
        space.setLayoutParams(spaceParams);
        container.addView(space);

        suToggle = new Switch(context);
        suToggle.setChecked(isSuRenamed());
        // Theme the switch if possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                suToggle.getThumbDrawable().setColorFilter(
                        Color.parseColor(colors.accentColor), PorterDuff.Mode.SRC_IN);
            } catch (Exception e) {
                // Ignore if theming fails
            }
        }
        container.addView(suToggle);

        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sectionParams.setMargins(0, dpToPx(24), 0, 0);
        container.setLayoutParams(sectionParams);

        return container;
    }

    private void startEntranceAnimation() {
        // Simple fade in animation
        card.setAlpha(0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.start();
    }

    private void startAuthenticationAnimation() {
        // Hide login button, show loading
        ObjectAnimator btnFade = ObjectAnimator.ofFloat(loginBtn, "alpha", 1f, 0f);
        btnFade.setDuration(300);
        btnFade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginBtn.setVisibility(View.GONE);
                loadingContainer.setVisibility(View.VISIBLE);
                loadingContainer.setAlpha(0f);
                ObjectAnimator.ofFloat(loadingContainer, "alpha", 0f, 1f).setDuration(300).start();
            }
        });
        btnFade.start();
    }

    private void showSuccessAnimation(Runnable onComplete) {
        // Hide loading, show success message
        ObjectAnimator loadingFade = ObjectAnimator.ofFloat(loadingContainer, "alpha", 1f, 0f);
        loadingFade.setDuration(300);
        loadingFade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loadingContainer.setVisibility(View.GONE);

                // Show success message
                TextView successText = new TextView(context);
                successText.setText("✓ Login Successful!");
                successText.setTextColor(Color.parseColor(colors.successColor));
                successText.setTextSize(16);
                successText.setGravity(Gravity.CENTER);
                successText.setAlpha(0f);

                LinearLayout.LayoutParams successParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                successParams.setMargins(0, dpToPx(24), 0, 0);
                successText.setLayoutParams(successParams);

                // Add success text before footer
                card.addView(successText, card.getChildCount() - 1);

                // Animate success message
                ObjectAnimator successFade = ObjectAnimator.ofFloat(successText, "alpha", 0f, 1f);
                successFade.setDuration(500);
                successFade.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Hide input boxes and show action buttons and root bypass section
                        hideInputsAndShowActionElements();

                        // Complete after delay
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            onComplete.run();
                        }, 2000);
                    }
                });
                successFade.start();
            }
        });
        loadingFade.start();
    }

    private void hideInputsAndShowActionElements() {
        // Hide input boxes with fade out animation
        ObjectAnimator userFade = ObjectAnimator.ofFloat(userInput, "alpha", 1f, 0f);
        ObjectAnimator passFade = ObjectAnimator.ofFloat(passInput, "alpha", 1f, 0f);

        userFade.setDuration(400);
        passFade.setDuration(400);

        userFade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                userInput.setVisibility(View.GONE);
            }
        });

        passFade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                passInput.setVisibility(View.GONE);

                // Show action buttons and root bypass section
                showActionElements();
            }
        });

        userFade.start();
        passFade.start();
    }

    private void showActionElements() {
        // Show action buttons container
        actionButtonsContainer.setVisibility(View.VISIBLE);
        actionButtonsContainer.setAlpha(0f);
        ObjectAnimator actionFade = ObjectAnimator.ofFloat(actionButtonsContainer, "alpha", 0f, 1f);
        actionFade.setDuration(500);
        actionFade.setStartDelay(200);
        actionFade.start();

        // Show root bypass section
        rootBypassSection.setVisibility(View.VISIBLE);
        rootBypassSection.setAlpha(0f);
        ObjectAnimator sectionFade = ObjectAnimator.ofFloat(rootBypassSection, "alpha", 0f, 1f);
        sectionFade.setDuration(500);
        sectionFade.setStartDelay(500);
        sectionFade.start();
    }

    private void showErrorAnimation(String errorMessage) {
        // Hide loading, show login button
        ObjectAnimator loadingFade = ObjectAnimator.ofFloat(loadingContainer, "alpha", 1f, 0f);
        loadingFade.setDuration(300);
        loadingFade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loadingContainer.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);
                loginBtn.setAlpha(0f);
                ObjectAnimator.ofFloat(loginBtn, "alpha", 0f, 1f).setDuration(400).start();

                // Shake inputs
                ObjectAnimator.ofFloat(userInput, "translationX", 0, -10, 10, -10, 10, 0).setDuration(500).start();
                ObjectAnimator.ofFloat(passInput, "translationX", 0, -10, 10, -10, 10, 0).setDuration(500).start();

                toast(errorMessage);
            }
        });
        loadingFade.start();
    }

    private void forceStopFreeFireAndApp() {
        try {
            // Method 1: Using ActivityManager to kill processes
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                // Kill Free Fire processes
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    List<ActivityManager.AppTask> tasks = am.getAppTasks();
                    for (ActivityManager.AppTask task : tasks) {
                        try {
                            ActivityManager.RecentTaskInfo info = task.getTaskInfo();
                            if (info.baseIntent != null && info.baseIntent.getComponent() != null) {
                                String packageName = info.baseIntent.getComponent().getPackageName();
                                if (packageName.contains("com.dts.freefireth") ||
                                        packageName.contains("com.dts.freefiremax") ||
                                        packageName.contains("garena")) {
                                    task.finishAndRemoveTask();
                                }
                            }
                        } catch (Exception e) {
                            // Continue with other methods
                        }
                    }
                }

                // Kill background processes if possible
                try {
                    am.killBackgroundProcesses("com.dts.freefireth");
                    am.killBackgroundProcesses("com.dts.freefiremax");
                } catch (Exception e) {
                    // May not work on newer Android versions
                }
            }

            // Method 2: Using shell commands (requires root)
            try {
                Process process = Runtime.getRuntime().exec("su");
                process.getOutputStream().write("pkill -f com.dts.freefireth\n".getBytes());
                process.getOutputStream().write("pkill -f com.dts.freefiremax\n".getBytes());
                process.getOutputStream().write("pkill -f garena\n".getBytes());
                process.getOutputStream().write("am force-stop com.dts.freefireth\n".getBytes());
                process.getOutputStream().write("am force-stop com.dts.freefiremax\n".getBytes());
                process.getOutputStream().write("exit\n".getBytes());
                process.getOutputStream().flush();
                process.waitFor();
            } catch (Exception e) {
                // Root commands failed, try non-root approach
            }

            // Method 3: Send broadcast to close Free Fire (if it supports it)
            try {
                Intent closeIntent = new Intent();
                closeIntent.setAction("com.dts.freefireth.CLOSE_APP");
                closeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.sendBroadcast(closeIntent);
            } catch (Exception e) {
                // Broadcast failed
            }

            toast("Force stop attempted for Free Fire");

            // Wait a moment then close current app
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    // Close current application
                    ((Activity) context).finishAffinity();
                    System.exit(0);
                } catch (Exception e) {
                    // Fallback methods
                    try {
                        ((Activity) context).finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    } catch (Exception ex) {
                        System.exit(0);
                    }
                }
            }, 1000);

        } catch (Exception e) {
            toast("Force stop failed: " + e.getMessage());
        }
    }

    private void setupClickListeners(String androidId) {
        // Toggle logic
        suToggle.setOnCheckedChangeListener((btn, isChecked) -> {
            String from = isChecked ? "/system/xbin/su" : "/system/xbin/su1";
            String to = isChecked ? "/system/xbin/su1" : "/system/xbin/su";
            try {
                Process process = Runtime.getRuntime().exec(isChecked ? "su" : "su1");
                process.getOutputStream().write(("mount -o remount,rw /system\n").getBytes());
                process.getOutputStream().write(("mv " + from + " " + to + "\n").getBytes());
                process.getOutputStream().write("exit\n".getBytes());
                process.getOutputStream().flush();
                process.waitFor();
                String msg = "Bypass " + (isChecked ? "enabled" : "disabled");
                toast(msg);
            } catch (Exception e) {
                toast("Failed: " + e.getMessage());
            }
        });

        // Force Stop button click listener
        forceStopBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Force Stop Confirmation");
            builder.setMessage("This will force stop Free Fire and close this application. Continue?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                forceStopFreeFireAndApp();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        // Main Menu button click listener
        mainMenuBtn.setOnClickListener(view -> {
            try {
                int injectType = 1;
                new Menu(context, injectType);
                toast("Opening Main Menu...");
            } catch (Exception e) {
                toast("Failed to open main menu: " + e.getMessage());
            }
        });

        // Login logic
        loginBtn.setOnClickListener(view -> {
            String user = userInput.getText().toString().trim();
            String pass = passInput.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                toast("Please fill all fields");
                return;
            }

            sharedPreferences.edit()
                    .putString("username", user)
                    .putString("password", pass)
                    .apply();

            startAuthenticationAnimation();

            showHelpDialog(() -> {
                AuthHelper.loginWithPackageValidation(context, APP_ID, SECRET_KEY, user, pass, VERSION,
                        ALLOWED_PACKAGES, new AuthHelper.AuthCallback() {
                            @Override
                            public void onSuccess(AuthHelper.LoginResult result) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    showSuccessAnimation(() -> {
                                        toast("Login successful!");
                                    });
                                });
                            }

                            @Override
                            public void onFailure(String reason) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    showErrorAnimation(reason);
                                });
                            }
                        });
            });
        });
    }

    private void showHelpDialog(Runnable onContinue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Need Help?");
        builder.setMessage("Join our Discord server for support and updates.");
        builder.setPositiveButton("Continue", (d, i) -> onContinue.run());
        builder.setNegativeButton("Join Discord", (d, i) -> {
            Intent discord = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://discord.gg/BYCYX3wrpM"));
            context.startActivity(discord);
        });
        builder.show();
    }

    private void toast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private boolean isSuRenamed() {
        return new File("/system/xbin/su1").exists();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}