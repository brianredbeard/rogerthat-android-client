/*
 * Copyright 2016 Mobicage NV
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
 * @@license_version:1.1@@
 */

package com.mobicage.rogerthat.util.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v7.app.NotificationCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.MainActivity;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.SafeRunnable;
import com.mobicage.rogerthat.util.system.T;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class UIUtils {

    private final static float UNINITIALIZED_PIXEL_SCALE = -1;

    // Owned by UI thread
    private final static Set<Integer> sNotificationIDs = new HashSet<Integer>();
    private static boolean sHardKeyboardInitialized = false;
    private static boolean sHasHardKeyboard;
    private static float sPixelScale = UNINITIALIZED_PIXEL_SCALE;

    public static final String NOTIFICATION_TIMESTAMP = "NOTIFICATION_TIMESTAMP";

    private static List<Activity> sActivities = new ArrayList<Activity>();
    private static Map<String, Integer> notificationIds = new HashMap<>();

    public static void onActivityStart(final Activity activity) {
        if (T.getThreadType() != T.UI) {
            throw new RuntimeException("This method must be called from T.UI, got " + T.getThreadName());
        }
        sActivities.add(activity);
        L.d(activity.getClass().getSimpleName() + " starting. Visible activities: " + sActivities);
    }

    public static void onActivityStop(final Activity activity) {
        if (T.getThreadType() != T.UI) {
            throw new RuntimeException("This method must be called from T.UI, got " + T.getThreadName());
        }
        sActivities.remove(activity);
        L.d(activity.getClass().getSimpleName() + " stopping. Visible activities: " + sActivities);
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

    public static void doNotification(Context pContext, String title, String message, int notificationId,
                                      String action, boolean withSound, boolean withVibration, boolean withLight,
                                      boolean autoCancel, int icon, int notificationNumber, String extra,
                                      String extraData, String tickerText, long timestamp, int priority,
                                      List<NotificationCompat.Action> actionButtons,
                                      String longNotificationText, Bitmap largeIcon, String category) {
        Bundle b = null;
        if (extra != null) {
            b = new Bundle();
            b.putString(extra, extraData);
        }
        doNotification(pContext, title, message, notificationId, action, withSound, withVibration, withLight,
                autoCancel, icon, notificationNumber, b, tickerText, timestamp, priority, actionButtons,
                longNotificationText, largeIcon, category);
    }

    public static void doNotification(Context pContext, String title, String message, int notificationId, String action,
                                      boolean withSound, boolean withVibration, boolean withLight, boolean autoCancel,
                                      int icon, int notificationNumber, Bundle extras, String tickerText,
                                      long timestamp, int priority, List<NotificationCompat.Action> actionButtons,
                                      String longNotificationText, Bitmap largeIcon, String category) {
        T.dontCare();
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(pContext);
        int defaults = 0;
        if (withSound) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (withVibration) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        builder.setDefaults(defaults);

        if (withLight) {
            builder.setLights(0xff00ff00, 300, 3000);
        }

        builder.setAutoCancel(autoCancel);
        builder.setOngoing(!autoCancel);
        builder.setCategory(category);

        if (message != null) {
            builder.setSmallIcon(icon);
            if (largeIcon != null) {
                builder.setLargeIcon(largeIcon);
            }
            if (tickerText != null) {
                builder.setTicker(tickerText);
            }
            builder.setWhen(System.currentTimeMillis());
            final Intent intent = new Intent(action, null, pContext, MainActivity.class);
            intent.addFlags(MainActivity.FLAG_CLEAR_STACK_SINGLE_TOP);
            if (extras != null)
                intent.putExtras(extras);
            intent.putExtra(NOTIFICATION_TIMESTAMP, timestamp);

            final PendingIntent pi = PendingIntent.getActivity(pContext, 1000, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pi);
            builder.setContentText(message);
            builder.setContentTitle(title);
            if (!TextUtils.isEmptyOrWhitespace(longNotificationText)) {
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(longNotificationText));
            }
            builder.setPriority(priority);
            if (actionButtons != null) {
                for (NotificationCompat.Action actionButton : actionButtons) {
                    builder.addAction(actionButton);
                }
            }
        }

        builder.setNumber(notificationNumber);

        final NotificationManager nm = (NotificationManager) pContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificationId, builder.build());
        sNotificationIDs.add(notificationId);
    }

    public static int getNotificationId(final String notificationKey, boolean create) {
        for (Map.Entry<String, Integer> notification : notificationIds.entrySet()) {
            if (notification.getKey().equals(notificationKey)) {
                return notification.getValue();
            }
        }
        if (!create) {
            return -1;
        }
        int notificationID = NotificationID.next();
        notificationIds.put(notificationKey, notificationID);
        return notificationID;
    }

    public static void cancelNotification(final Context pContext, final int pNotificationId) {
        T.dontCare();
        final NotificationManager nm = (NotificationManager) pContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(pNotificationId);
    }

    public static void cancelNotification(final Context context, final String notificationString) {
        int notificationId = getNotificationId(notificationString, false);
        if (notificationId != -1) {
            cancelNotification(context, notificationId);
        }
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
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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

    public static int convertPixelsToDip(Context context, int size) {
        if (sPixelScale == UNINITIALIZED_PIXEL_SCALE)
            sPixelScale = context.getResources().getDisplayMetrics().density;
        return (int) (size / sPixelScale);
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

        new AlertDialog.Builder(activity)
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
                }).create().show();
        return true;
    }

    public static Activity getTopActivity(final MainService mainService) {
        if (sActivities.size() == 0)
            return null;

        return sActivities.get(sActivities.size() - 1);
    }

    public static AlertDialog showAlertDialog(Context ctx, final int titleResource, final int messageResource) {
        return UIUtils.showAlertDialog(ctx, ctx.getString(titleResource), ctx.getString(messageResource));
    }

    public static AlertDialog showAlertDialog(Context ctx, final String title, final int messageResource) {
        return UIUtils.showAlertDialog(ctx, title, ctx.getString(messageResource));
    }

    public static AlertDialog showAlertDialog(Context ctx, final String title, final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setPositiveButton(R.string.rogerthat, null);
        builder.setMessage(message);
        builder.setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
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
        showAlertDialog(context, null, R.string.error_please_try_again);
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

    public static void setIconBackground(ImageView imageView, int backgroundColor) {
        GradientDrawable background = (GradientDrawable) imageView.getBackground();
        background.setColor(backgroundColor);
    }
}

class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(1000);

    public static int next() {
        return c.incrementAndGet();
    }
}