/*
 * Copyright 2018 GIG Technology NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.util.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.App;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.news.NewsPlugin;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeDialogClick;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.rpc.config.LookAndFeelConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class UIUtils {

    public static final String ERROR_MESSAGE = "error_message";
    public static final String ERROR_TITLE = "error_title";
    public static final String ERROR_CAPTION = "error_caption";
    public static final String ERROR_ACTION = "error_action";
    private final static float UNINITIALIZED_PIXEL_SCALE = -1;
    private final static int UNINITIALIZED_PIXEL_WIDTH = -1;

    private static boolean sHardKeyboardInitialized = false;
    private static boolean sHasHardKeyboard;
    private static float sPixelScale = UNINITIALIZED_PIXEL_SCALE;
    private static int sPixelWith = UNINITIALIZED_PIXEL_WIDTH;


    private static List<Activity> sActivities = new ArrayList<Activity>();

    public static int getNumberOfActivities() {
        if (T.getThreadType() != T.UI) {
            throw new RuntimeException("This method must be called from T.UI, got " + T.getThreadName());
        }
        return sActivities.size();
    }

    public static void onActivityStart(final Activity activity, final MainService mainService) {
        if (T.getThreadType() != T.UI) {
            throw new RuntimeException("This method must be called from T.UI, got " + T.getThreadName());
        }
        sActivities.add(activity);
        L.d(activity.getClass().getSimpleName() + " starting. Visible activities: " + sActivities);
        connectToNewsChannel(mainService);
        checkAppDidEnterForeGround(mainService);
    }

    public static void onActivityBound(final MainService mainService) {
        if (T.getThreadType() != T.UI) {
            throw new RuntimeException("This method must be called from T.UI, got " + T.getThreadName());
        }
        connectToNewsChannel(mainService);
        checkAppDidEnterForeGround(mainService);
    }

    private static void connectToNewsChannel(final MainService mainService) {
        if (mainService != null && AppConstants.NEWS_ENABLED) {
            NewsPlugin newsPlugin = mainService.getPlugin(NewsPlugin.class);
            if (newsPlugin != null) {
                newsPlugin.connectToChannel();
            }
        }
    }

    private static void checkAppDidEnterForeGround(MainService mainService) {
        if (mainService != null && sActivities.size() == 1) {
            mainService.kickHttpCommunication(true, "App did enter foreground");
        }
    }

    public static void onActivityStop(final Activity activity, final MainService mainService) {
        if (T.getThreadType() != T.UI) {
            throw new RuntimeException("This method must be called from T.UI, got " + T.getThreadName());
        }
        sActivities.remove(activity);
        L.d(activity.getClass().getSimpleName() + " stopping. Visible activities: " + sActivities);
        if (mainService != null && AppConstants.NEWS_ENABLED && sActivities.size() == 0) {
            NewsPlugin newsPlugin = mainService.getPlugin(NewsPlugin.class);
            if (newsPlugin != null) {
                newsPlugin.disconnectChannel();
            }
        }
    }

    public static Toast showLongToast(final Context context, final String text) {
        T.UI();
        final Toast t = Toast.makeText(context, text, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
        return t;
    }

    public static Toast showLongToast(final Context context, final int resource) {
        T.UI();
        return showLongToast(context, context.getString(resource));
    }

    public static boolean hasHardKeyboard(Context pContext) {
        T.UI();
        if (!sHardKeyboardInitialized) {
            sHasHardKeyboard = (pContext.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS);
            sHardKeyboardInitialized = true;
            L.d("Hard keyboard detection = " + sHasHardKeyboard);
        }
        return sHasHardKeyboard;
    }

    public static void hideKeyboard(Context context, View view) {
        T.UI();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null)
            L.d("No input method manager");
        else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showKeyboard(Context context) {
        if (hasHardKeyboard(context))
            return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null)
            L.d("No input method manager");
        else
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    // Based on
    // http://stackoverflow.com/questions/991764/hiding-title-in-a-fullscreen-mode
    public static void updateFullscreenStatus(Activity pActivity, View pContentView, boolean pUseFullscreen) {
        T.UI();
        if (pActivity.getWindow() != null) {
            if (pUseFullscreen) {
                pActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                pActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            } else {
                pActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                pActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            pContentView.requestLayout();
        }
    }

    public static int convertDipToPixels(Context context, int size) {
        if (sPixelScale == UNINITIALIZED_PIXEL_SCALE)
            sPixelScale = context.getResources().getDisplayMetrics().density;
        return (int) (size * sPixelScale);
    }

    public static int getAbsoluteWidthInPixels(Context context) {
        if (sPixelWith == UNINITIALIZED_PIXEL_WIDTH)
            sPixelWith = context.getResources().getDisplayMetrics().widthPixels;
        return sPixelWith;
    }

    public static boolean showHint(final Activity activity, final MainService mainService, final String hintCode,
        final int hintResource, final Object... args) {

        return UIUtils.showHint(activity, mainService, null, hintCode, hintResource, args);
    }

    public static boolean showHint(final Activity activity, final MainService mainService,
                                   final SafeRunnable onDismissHandler, final String hintCode,
                                   final int hintResource, final Object... args) {
        return UIUtils.showHintWithImage(activity, mainService, null, hintCode, null, hintResource, args);
    }

    public static boolean showHintWithImage(final Activity activity, final MainService mainService,
                                            final String hintCode, final FontAwesome.Icon hintIcon,
                                            final int hintResource, final Object... args) {

        return UIUtils.showHintWithImage(activity, mainService, null, hintCode, hintIcon, hintResource, args);
    }

    public static boolean showHintWithImage(final Activity activity, final MainService mainService,
                                            final SafeRunnable onDismissHandler, final String hintCode,
                                            final FontAwesome.Icon hintIcon, final int hintResource, final Object... args) {
        final ConfigurationProvider configurationProvider = mainService.getConfigurationProvider();
        final String configkey = "HINT_REPOSITORY";
        final com.mobicage.rogerthat.config.Configuration config = configurationProvider.getConfiguration(configkey);
        if (config.get(hintCode, false))
            return false;

        LayoutInflater inflater = activity.getLayoutInflater();
        final View checkboxLayout = inflater.inflate(R.layout.hint, null);
        final TextView message = (TextView) checkboxLayout.findViewById(R.id.message);
        final CheckBox checkBox = (CheckBox) checkboxLayout.findViewById(R.id.checkBox);
        if (hintIcon != null) {
            final ImageView icon = (ImageView) checkboxLayout.findViewById(R.id.icon);
            icon.setImageDrawable(new IconicsDrawable(activity, hintIcon).color(Color.DKGRAY).sizeDp(30));
            icon.setVisibility(View.VISIBLE);
        }
        Resources resources = activity.getResources();
        message.setText(resources.getString(hintResource, args));
        AlertDialog dialog = new AlertDialog.Builder(activity)
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (onDismissHandler != null) {
                        onDismissHandler.run();
                    }
                }
            })
            .setTitle(R.string.activity_hint)
            .setView(checkboxLayout)
            .setPositiveButton(resources.getString(R.string.activity_close_hint),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkBox.isChecked()) {
                            config.put(hintCode, true);
                            configurationProvider.updateConfigurationNow(configkey, config);
                        }
                        if (onDismissHandler != null) {
                            onDismissHandler.run();
                        }
                    }
                }).create();
        dialog.show();
        int buttonId = mainService.getResources().getIdentifier("android:id/button1", null, null);
        ((Button) dialog.findViewById(buttonId)).setTextColor(LookAndFeelConstants.getPrimaryColor(activity));
        return true;
    }

    public static boolean isRogerthatTopActivity() {
        final Activity topActivity = UIUtils.getTopActivity();
        if (topActivity == null)
            return false;
        return App.getContext().getPackageName().equals(topActivity.getPackageName());
    }

    public static Activity getTopActivity() {
        if (sActivities.size() == 0)
            return null;

        return sActivities.get(sActivities.size() - 1);
    }

    // Lots of overloads for convenience

    public static AlertDialog showDialog(Context ctx, final int titleResource, final int messageResource) {
        return UIUtils.showDialog(ctx, ctx.getString(titleResource), ctx.getString(messageResource));
    }

    public static AlertDialog showDialog(Context ctx, final String title, final int messageResource) {
        return UIUtils.showDialog(ctx, title, ctx.getString(messageResource));
    }

    public static AlertDialog showDialog(Context ctx, final String title, final String messageResource) {
        return UIUtils.showDialog(ctx, title, messageResource, null, null, null);
    }


    public static AlertDialog showDialog(Context ctx, final String title, final String message,
                                         SafeDialogClick onPositiveButtonClickListener) {
        return UIUtils.showDialog(ctx, title, message, onPositiveButtonClickListener, null, null);
    }

    public static AlertDialog showDialog(Context ctx, String title, String message,
                                         SafeDialogClick onPositiveButtonClick, String errorCaption,
                                         SafeDialogClick onNegativeButtonClick) {
        String positiveButtonText = ctx.getString(R.string.rogerthat);
        return UIUtils.showDialog(ctx, title, message, positiveButtonText, onPositiveButtonClick, errorCaption,
                onNegativeButtonClick);
    }


    public static AlertDialog showDialog(Context ctx, String title, String message, String[] items, SafeDialogClick itemsOnClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        builder.setItems(items, itemsOnClickListener);
        AlertDialog dialog = builder.create();
        dialog.show();
        UIUtils.setDialogButtonColors(ctx, dialog);
        return dialog;
    }

    public static AlertDialog showDialog(Context ctx, String title, String message,int positiveButtonCaption,
                                         SafeDialogClick onPositiveClickListener, int negativeButtonCaption,
                                         SafeDialogClick onNegativeClickListener) {
        String positiveCaption = ctx.getString(positiveButtonCaption);
        String negativeCaption = ctx.getString(negativeButtonCaption);
        return UIUtils.showDialog(ctx, title, message, positiveCaption, onPositiveClickListener, negativeCaption,
                onNegativeClickListener);
    }

    public static AlertDialog showDialog(Context ctx, String title, String message, int positiveButtonCaption,
                                         SafeDialogClick onPositiveClickListener, int negativeButtonCaption,
                                         SafeDialogClick onNegativeClickListener, View view, boolean show) {
        String positiveCaption = ctx.getString(positiveButtonCaption);
        String negativeCaption = ctx.getString(negativeButtonCaption);
        return UIUtils.showDialog(ctx, title, message, positiveCaption, onPositiveClickListener, negativeCaption,
                onNegativeClickListener, view, show);
    }

    public static AlertDialog showDialog(Context ctx, String title, String message, String positiveCaption,
                                         SafeDialogClick onPositiveButtonClick, String negativeCaption,
                                         SafeDialogClick onNegativeButtonClick) {
        return UIUtils.showDialog(ctx, title, message, positiveCaption, onPositiveButtonClick, negativeCaption,
                onNegativeButtonClick, null, true);
    }

    public static AlertDialog showDialog(Context ctx, String title, String message, int positiveButtonCaption,
                                         SafeDialogClick onPositiveButtonClick, int negativeButtonCaption,
                                         SafeDialogClick onNegativeButtonClick, View view) {
        String positiveCaption = ctx.getString(positiveButtonCaption);
        String negativeCaption = ctx.getString(negativeButtonCaption);
        return UIUtils.showDialog(ctx, title, message, positiveCaption, onPositiveButtonClick, negativeCaption,
                onNegativeButtonClick, view, true);
    }

    public static AlertDialog showDialog(Context ctx, String title, String message, String positiveCaption,
                                         SafeDialogClick onPositiveButtonClick, String negativeCaption,
                                         SafeDialogClick onNegativeButtonClick, View view) {
        return UIUtils.showDialog(ctx, title, message, positiveCaption, onPositiveButtonClick, negativeCaption,
                onNegativeButtonClick, view, true);
    }

    public static AlertDialog showDialog(final Context ctx, String title, String message, String positiveCaption,
                                         SafeDialogClick onPositiveButtonClick, String negativeCaption,
                                         SafeDialogClick onNegativeButtonClick, View view, boolean show) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        builder.setPositiveButton(positiveCaption, onPositiveButtonClick);
        if (negativeCaption != null) {
            builder.setNegativeButton(negativeCaption, onNegativeButtonClick);
        }
        if (view != null) {
            builder.setView(view);
        }
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
             @Override
             public void onShow(DialogInterface dialogInterface) {
                 UIUtils.setDialogButtonColors(ctx, dialog);
             }
        });
        if (show) {
            dialog.show();
        }
        return dialog;
    }

    public static void showErrorDialog(final Activity activity, Intent intent) {
        final String errorMessage = intent.getStringExtra(ERROR_MESSAGE);
        if (TextUtils.isEmptyOrWhitespace(errorMessage)) {
            activity.finish();
            UIUtils.showErrorToast(activity, activity.getString(R.string.scanner_communication_failure));
        } else {
            final String errorCaption = intent.getStringExtra(ERROR_CAPTION);
            final String errorAction = intent.getStringExtra(ERROR_ACTION);
            final String errorTitle = intent.getStringExtra(ERROR_TITLE);

            SafeDialogClick positiveButtonListener = new SafeDialogClick() {
                @Override
                public void safeOnClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    activity.finish();
                }
            };
            SafeDialogClick negativeButtonListener = null;
            if (!TextUtils.isEmptyOrWhitespace(errorCaption) && !TextUtils.isEmptyOrWhitespace(errorAction)) {
                negativeButtonListener = new SafeDialogClick() {
                    @Override
                    public void safeOnClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(errorAction));
                        dialog.dismiss();
                        activity.finish();
                        activity.startActivity(intent);
                    }
                };
            }
            UIUtils.showDialog(activity, errorTitle, errorMessage, positiveButtonListener, errorCaption,
                    negativeButtonListener);
        }
    }


    public static void showErrorToast(Context context, String message) {
        UIUtils.showLongToast(context, message);
    }

    public static void setDialogButtonColors(Context ctx, AlertDialog dialog) {
        int primaryColor = LookAndFeelConstants.getPrimaryColor(ctx);
        // This may or may not change in different SDK versions
        int buttonId1 = ctx.getResources().getIdentifier("android:id/button1", null, null);
        ((Button) dialog.findViewById(buttonId1)).setTextColor(primaryColor);
        int buttonId2 = ctx.getResources().getIdentifier("android:id/button2", null, null);
        ((Button) dialog.findViewById(buttonId2)).setTextColor(primaryColor);
        int buttonId3 = ctx.getResources().getIdentifier("android:id/button3", null, null);
        ((Button) dialog.findViewById(buttonId3)).setTextColor(primaryColor);
    }

    public static AlertDialog showNotConnectedToFriendDialog(final Context context, final FriendsPlugin friendsPlugin,
                                                             final String friendEmail) {
        String message = context.getString(R.string.invite_as_friend, friendEmail);
        SafeDialogClick positiveClick = new SafeDialogClick() {
            @Override
            public void safeOnClick(DialogInterface dialog, int id) {
                friendsPlugin.inviteFriend(friendEmail, null, null, true);
            }
        };
        return UIUtils.showDialog(context, null, message, R.string.yes, positiveClick, R.string.no, null);
    }

    public static int getDisplayWidth(Context ctx) {
        final Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getWidth();
    }

    public static Point getDisplaySize(Context ctx) {
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        size.x = display.getWidth();
        size.y = display.getHeight();
        return size;
    }

    public static ColorFilter imageColorFilter(int color) {
        int red = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue = color & 0xFF;

        float[] matrix = { 0, 0, 0, 0, red, 0, 0, 0, 0, green, 0, 0, 0, 0, blue, 0, 0, 0, 1, 0 };

        return new ColorMatrixColorFilter(matrix);
    }

    public static Bitmap createThumbnail(Bitmap source, int maxWidthOrHeight) {
        if (source == null) {
            return null;
        }
        final int w;
        final int h;
        if (source.getWidth() > source.getHeight()) {
            w = maxWidthOrHeight;
            h = w * source.getHeight() / source.getWidth();
        } else {
            h = maxWidthOrHeight;
            w = h * source.getWidth() / source.getHeight();
        }
        return ThumbnailUtils.extractThumbnail(source, w, h);
    }

    public static Bitmap createVideoThumbnail(Context ctx, String path, int maxWidthOrHeight) {
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MINI_KIND);
        if (thumbnail != null) {
            // Shrink the thumbnail further to maxWidthOrHeight
            thumbnail = UIUtils.createThumbnail(thumbnail, maxWidthOrHeight);
            Bitmap overlay = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.video_overlay);
            overlay = createThumbnail(overlay, maxWidthOrHeight);

            Canvas canvas = new Canvas(thumbnail);
            int tWidth = thumbnail.getWidth();
            int tHeight = thumbnail.getHeight();

            int left = 0;
            int top = 0;
            if (tWidth > tHeight) {
                top = (tHeight - maxWidthOrHeight) / 2;
            } else if (tWidth < tHeight) {
                left = (tWidth - maxWidthOrHeight) / 2;
            }

            canvas.drawBitmap(overlay, left, top, null);
        }
        return thumbnail;
    }

    public static boolean setListViewHeightBasedOnItems(ListView listView) {
        return setListViewHeightBasedOnItems(listView, 0);
    }

    public static boolean setListViewHeightBasedOnItems(ListView listView, int maxHeight) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return false;
        }
        int numberOfItems = listAdapter.getCount();

        // Get total height of all items.
        int totalItemsHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
        for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
            View item = listAdapter.getView(itemPos, null, listView);
            item.measure(desiredWidth, 0);
            totalItemsHeight += item.getMeasuredHeight();
        }

        // Get total height of all item dividers.
        int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);

        // Set list height.
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight;
        if (maxHeight > 0 && params.height > maxHeight) {
            params.height = maxHeight;
        }
        listView.setLayoutParams(params);
        listView.requestLayout();

        return true;
    }

    public static void showNoNetworkDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.registration_screen_instructions_check_network_not_available);
        builder.setPositiveButton(R.string.rogerthat, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showErrorPleaseRetryDialog(Context context) {
        showDialog(context, null, R.string.error_please_try_again);
    }

    public static boolean isSupportedFontawesomeIcon(String iconName) {
        return iconName != null && getIcon(iconName) != null;
    }

    public static FontAwesome.Icon getIcon(String iconName) {
        iconName = iconName.replace("fa-", "faw_").replace("-", "_");
        try {
            return FontAwesome.Icon.valueOf(iconName);
        } catch (IllegalArgumentException exception) {
            L.d("Unknown icon: " + iconName);
            return null;
        }
    }

    public static IconicsDrawable getIconFromString(Context context, String iconName) {
        FontAwesome.Icon icon = getIcon(iconName);
        if (icon == null) {
            icon = FontAwesome.Icon.faw_question;
        }
        return new IconicsDrawable(context, icon);
    }

    public static void setBackgroundColor(View view, int backgroundColor) {
        Drawable background = view.getBackground();
        if (background instanceof ShapeDrawable) {
            ShapeDrawable shapeDrawable = (ShapeDrawable) background;
            shapeDrawable.getPaint().setColor(backgroundColor);
        } else if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(backgroundColor);
        } else if (background instanceof ColorDrawable) {
            // alpha value may need to be set again after this call
            ColorDrawable colorDrawable = (ColorDrawable) background;
            colorDrawable.setColor(backgroundColor);
        } else {
            L.e("Unknown background type to set color on: " + view.getClass());
        }
    }

    public static ProgressDialog showProgressDialog(Context context, CharSequence title, CharSequence message) {
        return UIUtils.showProgressDialog(context, title, message, true, true, null);
    }

    public static ProgressDialog showProgressDialog(Context context, CharSequence title, CharSequence message,
                                                    boolean indeterminate, boolean cancelable) {
        return UIUtils.showProgressDialog(context, title, message, indeterminate, cancelable, null);
    }

    public static ProgressDialog showProgressDialog(Context context, CharSequence title, CharSequence message,
                                                    boolean indeterminate, boolean cancelable,
                                                    DialogInterface.OnCancelListener onCancelListener) {
        return UIUtils.showProgressDialog(context, title, message, indeterminate, cancelable, onCancelListener,
                ProgressDialog.STYLE_SPINNER, true);
    }

    public static ProgressDialog showProgressDialog(final Context context, CharSequence title, CharSequence message,
                                                    boolean indeterminate, boolean cancelable,
                                                    DialogInterface.OnCancelListener onCancelListener,
                                                    int progressStyle, boolean show) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setProgressNumberFormat(null);
        dialog.setProgressPercentFormat(null);
        dialog.setOnCancelListener(onCancelListener);
        dialog.setProgressStyle(progressStyle);
        dialog.setOnShowListener(new ProgressDialog.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ProgressDialog progressDialog = (ProgressDialog) dialog;
                ProgressBar progressBar = (ProgressBar) progressDialog.findViewById(android.R.id.progress);
                UIUtils.setColors(context, progressBar);
            }
        });
        if (show) {
            dialog.show();
        }
        return dialog;
    }

    private static void colorEditText(EditText view, String fieldNameRes, String fieldNameDrawable, int color,
                                      boolean isArray) {
        try {
            // Get the cursor resource id
            final Field field = TextView.class.getDeclaredField(fieldNameRes);
            field.setAccessible(true);
            int drawableCursorResId = field.getInt(view);

            // Get the editor
            final Field editor = TextView.class.getDeclaredField("mEditor");
            editor.setAccessible(true);
            Object cursorEditor = editor.get(view);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(view.getContext(), drawableCursorResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            // Set the drawables
            final Field drawableField = cursorEditor.getClass().getDeclaredField(fieldNameDrawable);
            drawableField.setAccessible(true);
            if (isArray) {
                Drawable[] drawables = {drawable, drawable};
                drawableField.set(cursorEditor, drawables);
            } else {
                drawableField.set(cursorEditor, drawable);
            }
        } catch (IllegalAccessException e) {
            L.bug(e);
        } catch (NoSuchFieldException e) {
            L.e("NoSuchFieldException for " + fieldNameRes, e);
        }
    }

    /**
     * Use reflection to change cursor color since there is no build-in method to set this
     *
     * @param view  EditText to set the color on
     * @param color AARRGGBB
     */
    private static void setCursorColor(EditText view, int color) {
        UIUtils.colorEditText(view, "mCursorDrawableRes", "mCursorDrawable", color, true);
        UIUtils.colorEditText(view, "mTextSelectHandleLeftRes", "mSelectHandleLeft", color, false);
        UIUtils.colorEditText(view, "mTextSelectHandleRightRes", "mSelectHandleRight", color, false);
        UIUtils.colorEditText(view, "mTextSelectHandleRes", "mSelectHandleCenter", color, false);
    }

    private static void colorTextInputLayout(TextInputLayout textInputLayout, int color) {
        try {
            ColorStateList colorStateList = new ColorStateList(new int[][]{{0}}, new int[]{color});
            Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
            fDefaultTextColor.setAccessible(true);
            fDefaultTextColor.set(textInputLayout, colorStateList);

            Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
            fFocusedTextColor.setAccessible(true);
            fFocusedTextColor.set(textInputLayout, colorStateList);
        } catch (IllegalAccessException e) {
            L.bug(e);
        } catch (NoSuchFieldException e) {
            L.bug(e);
        }
    }

    public static void setColors(Context context, View... views) {
        int primaryColor = LookAndFeelConstants.getPrimaryColor(context);
        setColors(primaryColor, views);
    }

    public static void setColors(int color, View... views) {
        if (Build.VERSION.SDK_INT >= 27) {
            return;
        }
        for (View view : views) {
            if (view instanceof CheckBox) {
                CheckBox checkbox = (CheckBox) view;
                CompoundButtonCompat.setButtonTintList(checkbox, ColorStateList.valueOf(color));
            } else if (view instanceof FloatingActionButton) {
                //noinspection RedundantCast
                ((FloatingActionButton) view).setBackgroundTintList(ColorStateList.valueOf(color));
            } else if (view instanceof Button || view instanceof ImageButton) {
                Drawable background = view.getBackground();
                if (background != null) {
                    background.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            } else if (view instanceof TextInputLayout) {
                UIUtils.colorTextInputLayout((TextInputLayout) view, color);
                // TODO: EditText's who are a child of TextInputLayout their line isn't colored correctly
            } else if (view instanceof EditText) {
                EditText editText = (EditText) view;
                editText.setHighlightColor(color); // When selecting text
                editText.setHintTextColor(color); // Text for the  hint message
                // Line under the textfield
                Drawable background = editText.getBackground();
                if (background != null) {
                    DrawableCompat.setTint(background, color);
                    editText.setBackground(background);
                }
                UIUtils.setCursorColor(editText, color);
            } else if (view instanceof CheckedTextView) {
                CheckedTextView ctv = (CheckedTextView) view;
                Drawable d = ctv.getCheckMarkDrawable();
                d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            } else if (view instanceof TextView) {
                ((TextView) view).setLinkTextColor(color);
            } else if (view instanceof SeekBar) {
                SeekBar sb = (SeekBar) view;
                Drawable progress = sb.getProgressDrawable();
                if (progress != null) {
                    progress.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
                Drawable thumb = sb.getThumb();
                if (thumb != null) {
                    thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            } else if (view instanceof ProgressBar) {
                ((ProgressBar) view).getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            } else {
                L.d("Not coloring view: " + view.toString());
            }
        }
    }

    public static void setColorsRecursive(Context context, ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            final View view = viewGroup.getChildAt(i);
            if (!(view instanceof FrameLayout) && !(view instanceof LinearLayout) && !(view instanceof
                    RelativeLayout)) {
                UIUtils.setColors(context, view);
            } else if (view instanceof ViewGroup) {
                UIUtils.setColorsRecursive(context, (ViewGroup) view);
            }
        }
    }
}
