/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.latin.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.inputmethod.compat.BuildCompatUtils;
import com.android.inputmethod.latin.AudioAndHapticFeedbackManager;
import com.android.inputmethod.latin.InputAttributes;
import com.android.inputmethod.latin.R;
import com.android.inputmethod.latin.common.StringUtils;
import com.android.inputmethod.latin.utils.AdditionalSubtypeUtils;
import com.android.inputmethod.latin.utils.JniUtils;
import com.android.inputmethod.latin.utils.ResourceUtils;
import com.android.inputmethod.latin.utils.RunInLocale;
import com.android.inputmethod.latin.utils.StatsUtils;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

public final class Settings implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = Settings.class.getSimpleName();
    // Settings screens
    public static final String SCREEN_ACCOUNTS = "screen_accounts";
    public static final String SCREEN_THEME = "screen_theme";
    public static final String SCREEN_DEBUG = "screen_debug";
    public static final String SCREEN_GESTURE = "screen_gesture";
    // In the same order as xml/prefs.xml
    public static final String PREF_AUTO_CAP = "auto_cap";
    public static final String PREF_VIBRATE_ON = "vibrate_on";
    public static final String PREF_SOUND_ON = "sound_on";
    public static final String PREF_POPUP_ON = "popup_on";
    // PREF_VOICE_MODE_OBSOLETE is obsolete. Use PREF_VOICE_INPUT_KEY instead.
    public static final String PREF_VOICE_MODE_OBSOLETE = "voice_mode";
    public static final String PREF_VOICE_INPUT_KEY = "pref_voice_input_key";
    public static final String PREF_EDIT_PERSONAL_DICTIONARY = "edit_personal_dictionary";
    public static final String PREF_CONFIGURE_DICTIONARIES_KEY = "configure_dictionaries_key";
    // PREF_AUTO_CORRECTION_THRESHOLD_OBSOLETE is obsolete. Use PREF_AUTO_CORRECTION instead.
    public static final String PREF_AUTO_CORRECTION_THRESHOLD_OBSOLETE =
            "auto_correction_threshold";
    public static final String PREF_AUTO_CORRECTION = "pref_key_auto_correction";
    // PREF_SHOW_SUGGESTIONS_SETTING_OBSOLETE is obsolete. Use PREF_SHOW_SUGGESTIONS instead.
    public static final String PREF_SHOW_SUGGESTIONS_SETTING_OBSOLETE = "show_suggestions_setting";
    public static final String PREF_SHOW_SUGGESTIONS = "show_suggestions";
    public static final String PREF_KEY_USE_CONTACTS_DICT = "pref_key_use_contacts_dict";
    public static final String PREF_KEY_USE_PERSONALIZED_DICTS = "pref_key_use_personalized_dicts";
    public static final String PREF_KEY_USE_DOUBLE_SPACE_PERIOD =
            "pref_key_use_double_space_period";
    public static final String PREF_BLOCK_POTENTIALLY_OFFENSIVE =
            "pref_key_block_potentially_offensive";
    public static final boolean ENABLE_SHOW_LANGUAGE_SWITCH_KEY_SETTINGS =
            BuildCompatUtils.EFFECTIVE_SDK_INT <= Build.VERSION_CODES.KITKAT;
    public static final boolean SHOULD_SHOW_LXX_SUGGESTION_UI =
            BuildCompatUtils.EFFECTIVE_SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    public static final String PREF_SHOW_LANGUAGE_SWITCH_KEY =
            "pref_show_language_switch_key";
    public static final String PREF_INCLUDE_OTHER_IMES_IN_LANGUAGE_SWITCH_LIST =
            "pref_include_other_imes_in_language_switch_list";
    public static final String PREF_CUSTOM_INPUT_STYLES = "custom_input_styles";
    public static final String PREF_ENABLE_SPLIT_KEYBOARD = "pref_split_keyboard";
    // TODO: consolidate key preview dismiss delay with the key preview animation parameters.
    public static final String PREF_KEY_PREVIEW_POPUP_DISMISS_DELAY =
            "pref_key_preview_popup_dismiss_delay";
    public static final String PREF_BIGRAM_PREDICTIONS = "next_word_prediction";
    public static final String PREF_GESTURE_INPUT = "gesture_input";
    public static final String PREF_VIBRATION_DURATION_SETTINGS =
            "pref_vibration_duration_settings";
    public static final String PREF_KEYPRESS_SOUND_VOLUME = "pref_keypress_sound_volume";
    public static final String PREF_KEY_LONGPRESS_TIMEOUT = "pref_key_longpress_timeout";
    public static final String PREF_ENABLE_EMOJI_ALT_PHYSICAL_KEY =
            "pref_enable_emoji_alt_physical_key";
    public static final String PREF_GESTURE_PREVIEW_TRAIL = "pref_gesture_preview_trail";
    public static final String PREF_GESTURE_FLOATING_PREVIEW_TEXT =
            "pref_gesture_floating_preview_text";
    public static final String PREF_SHOW_SETUP_WIZARD_ICON = "pref_show_setup_wizard_icon";

    public static final String PREF_KEY_IS_INTERNAL = "pref_key_is_internal";

    public static final String PREF_ENABLE_METRICS_LOGGING = "pref_enable_metrics_logging";
    // This preference key is deprecated. Use {@link #PREF_SHOW_LANGUAGE_SWITCH_KEY} instead.
    // This is being used only for the backward compatibility.
    private static final String PREF_SUPPRESS_LANGUAGE_SWITCH_KEY =
            "pref_suppress_language_switch_key";

    private static final String PREF_LAST_USED_PERSONALIZATION_TOKEN =
            "pref_last_used_personalization_token";
    private static final String PREF_LAST_PERSONALIZATION_DICT_WIPED_TIME =
            "pref_last_used_personalization_dict_wiped_time";
    private static final String PREF_CORPUS_HANDLES_FOR_PERSONALIZATION =
            "pref_corpus_handles_for_personalization";

    // Keyboard height (moved from debug settings)
    public static final String PREF_KEYBOARD_HEIGHT_SCALE = "pref_keyboard_height_scale";

    // Emoji
    public static final String PREF_EMOJI_RECENT_KEYS = "emoji_recent_keys";
    public static final String PREF_EMOJI_CATEGORY_LAST_TYPED_ID = "emoji_category_last_typed_id";
    public static final String PREF_LAST_SHOWN_EMOJI_CATEGORY_ID = "last_shown_emoji_category_id";

    private static final float UNDEFINED_PREFERENCE_VALUE_FLOAT = -1.0f;
    private static final int UNDEFINED_PREFERENCE_VALUE_INT = -1;

    private Context mContext;
    private Resources mRes;
    private SharedPreferences mPrefs;
    private SettingsValues mSettingsValues;
    private final ReentrantLock mSettingsValuesLock = new ReentrantLock();

    private static final Settings sInstance = new Settings();

    public static Settings getInstance() {
        return sInstance;
    }

    public static void init(final Context context) {
        sInstance.onCreate(context);
    }

    private Settings() {
        // Intentional empty constructor for singleton.
    }

    private void onCreate(final Context context) {
        mContext = context;
        mRes = context.getResources();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        upgradeAutocorrectionSettings(mPrefs, mRes);
    }

    public void onDestroy() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
        mSettingsValuesLock.lock();
        try {
            if (mSettingsValues == null) {
                // TODO: Introduce a static function to register this class and ensure that
                // loadSettings must be called before "onSharedPreferenceChanged" is called.
                Log.w(TAG, "onSharedPreferenceChanged called before loadSettings.");
                return;
            }
            loadSettings(mContext, mSettingsValues.mLocale, mSettingsValues.mInputAttributes);
            StatsUtils.onLoadSettings(mSettingsValues);
        } finally {
            mSettingsValuesLock.unlock();
        }
    }

    public void loadSettings(final Context context, final Locale locale,
            @Nonnull final InputAttributes inputAttributes) {
        mSettingsValuesLock.lock();
        mContext = context;
        try {
            final SharedPreferences prefs = mPrefs;
            final RunInLocale<SettingsValues> job = new RunInLocale<SettingsValues>() {
                @Override
                protected SettingsValues job(final Resources res) {
                    return new SettingsValues(context, prefs, res, inputAttributes);
                }
            };
            mSettingsValues = job.runInLocale(mRes, locale);
        } finally {
            mSettingsValuesLock.unlock();
        }
    }

    // TODO: Remove this method and add proxy method to SettingsValues.
    public SettingsValues getCurrent() {
        return mSettingsValues;
    }

    public boolean isInternal() {
        return mSettingsValues.mIsInternal;
    }

    public static int readScreenMetrics(final Resources res) {
        return res.getInteger(R.integer.config_screen_metrics);
    }

    // Accessed from the settings interface, hence public
    public static boolean readKeypressSoundEnabled(final SharedPreferences prefs,
            final Resources res) {
        return prefs.getBoolean(PREF_SOUND_ON,
                res.getBoolean(R.bool.config_default_sound_enabled));
    }

    public static boolean readVibrationEnabled(final SharedPreferences prefs,
            final Resources res) {
        final boolean hasVibrator = AudioAndHapticFeedbackManager.getInstance().hasVibrator();
        return hasVibrator && prefs.getBoolean(PREF_VIBRATE_ON,
                res.getBoolean(R.bool.config_default_vibration_enabled));
    }

    public static boolean readAutoCorrectEnabled(final SharedPreferences prefs,
            final Resources res) {
        return prefs.getBoolean(PREF_AUTO_CORRECTION, false);
    }

    public static float readPlausibilityThreshold(final Resources res) {
        return Float.parseFloat(res.getString(R.string.plausibility_threshold));
    }

    public static boolean readBlockPotentiallyOffensive(final SharedPreferences prefs,
            final Resources res) {
        return prefs.getBoolean(PREF_BLOCK_POTENTIALLY_OFFENSIVE,
                res.getBoolean(R.bool.config_block_potentially_offensive));
    }

    public static boolean readFromBuildConfigIfGestureInputEnabled(final Resources res) {
        if (!JniUtils.sHaveGestureLib) {
            return false;
        }
        return res.getBoolean(R.bool.config_gesture_input_enabled_by_build_config);
    }

    public static boolean readGestureInputEnabled(final SharedPreferences prefs,
            final Resources res) {
        return readFromBuildConfigIfGestureInputEnabled(res)
                && prefs.getBoolean(PREF_GESTURE_INPUT, true);
    }

    public static boolean readFromBuildConfigIfToShowKeyPreviewPopupOption(final Resources res) {
        return res.getBoolean(R.bool.config_enable_show_key_preview_popup_option);
    }

    public static boolean readKeyPreviewPopupEnabled(final SharedPreferences prefs,
            final Resources res) {
        final boolean defaultKeyPreviewPopup = res.getBoolean(
                R.bool.config_default_key_preview_popup);
        if (!readFromBuildConfigIfToShowKeyPreviewPopupOption(res)) {
            return defaultKeyPreviewPopup;
        }
        return prefs.getBoolean(PREF_POPUP_ON, defaultKeyPreviewPopup);
    }

    public static int readKeyPreviewPopupDismissDelay(final SharedPreferences prefs,
            final Resources res) {
        return Integer.parseInt(prefs.getString(PREF_KEY_PREVIEW_POPUP_DISMISS_DELAY,
                Integer.toString(res.getInteger(
                        R.integer.config_key_preview_linger_timeout))));
    }

    public static boolean readShowsLanguageSwitchKey(final SharedPreferences prefs) {
        if (prefs.contains(PREF_SUPPRESS_LANGUAGE_SWITCH_KEY)) {
            final boolean suppressLanguageSwitchKey = prefs.getBoolean(
                    PREF_SUPPRESS_LANGUAGE_SWITCH_KEY, false);
            final SharedPreferences.Editor editor = prefs.edit();
            editor.remove(PREF_SUPPRESS_LANGUAGE_SWITCH_KEY);
            editor.putBoolean(PREF_SHOW_LANGUAGE_SWITCH_KEY, !suppressLanguageSwitchKey);
            editor.apply();
        }
        return prefs.getBoolean(PREF_SHOW_LANGUAGE_SWITCH_KEY, true);
    }

    public static String readPrefAdditionalSubtypes(final SharedPreferences prefs,
            final Resources res) {
        final String predefinedPrefSubtypes = AdditionalSubtypeUtils.createPrefSubtypes(
                res.getStringArray(R.array.predefined_subtypes));
        return prefs.getString(PREF_CUSTOM_INPUT_STYLES, predefinedPrefSubtypes);
    }

    public static void writePrefAdditionalSubtypes(final SharedPreferences prefs,
            final String prefSubtypes) {
        prefs.edit().putString(PREF_CUSTOM_INPUT_STYLES, prefSubtypes).apply();
    }

    public static float readKeypressSoundVolume(final SharedPreferences prefs,
            final Resources res) {
        final float volume = prefs.getFloat(
                PREF_KEYPRESS_SOUND_VOLUME, UNDEFINED_PREFERENCE_VALUE_FLOAT);
        return (volume != UNDEFINED_PREFERENCE_VALUE_FLOAT) ? volume
                : readDefaultKeypressSoundVolume(res);
    }

    // Default keypress sound volume for unknown devices.
    // The negative value means system default.
    private static final String DEFAULT_KEYPRESS_SOUND_VOLUME = Float.toString(-1.0f);

    public static float readDefaultKeypressSoundVolume(final Resources res) {
        return Float.parseFloat(ResourceUtils.getDeviceOverrideValue(res,
                R.array.keypress_volumes, DEFAULT_KEYPRESS_SOUND_VOLUME));
    }

    public static int readKeyLongpressTimeout(final SharedPreferences prefs,
            final Resources res) {
        final int milliseconds = prefs.getInt(
                PREF_KEY_LONGPRESS_TIMEOUT, UNDEFINED_PREFERENCE_VALUE_INT);
        return (milliseconds != UNDEFINED_PREFERENCE_VALUE_INT) ? milliseconds
                : readDefaultKeyLongpressTimeout(res);
    }

    public static int readDefaultKeyLongpressTimeout(final Resources res) {
        return res.getInteger(R.integer.config_default_longpress_key_timeout);
    }

    public static int readKeypressVibrationDuration(final SharedPreferences prefs,
            final Resources res) {
        final int milliseconds = prefs.getInt(
                PREF_VIBRATION_DURATION_SETTINGS, UNDEFINED_PREFERENCE_VALUE_INT);
        return (milliseconds != UNDEFINED_PREFERENCE_VALUE_INT) ? milliseconds
                : readDefaultKeypressVibrationDuration(res);
    }

    // Default keypress vibration duration for unknown devices.
    // The negative value means system default.
    private static final String DEFAULT_KEYPRESS_VIBRATION_DURATION = Integer.toString(-1);

    public static int readDefaultKeypressVibrationDuration(final Resources res) {
        return Integer.parseInt(ResourceUtils.getDeviceOverrideValue(res,
                R.array.keypress_vibration_durations, DEFAULT_KEYPRESS_VIBRATION_DURATION));
    }

    public static float readKeyPreviewAnimationScale(final SharedPreferences prefs,
            final String prefKey, final float defaultValue) {
        final float fraction = prefs.getFloat(prefKey, UNDEFINED_PREFERENCE_VALUE_FLOAT);
        return (fraction != UNDEFINED_PREFERENCE_VALUE_FLOAT) ? fraction : defaultValue;
    }

    public static int readKeyPreviewAnimationDuration(final SharedPreferences prefs,
            final String prefKey, final int defaultValue) {
        final int milliseconds = prefs.getInt(prefKey, UNDEFINED_PREFERENCE_VALUE_INT);
        return (milliseconds != UNDEFINED_PREFERENCE_VALUE_INT) ? milliseconds : defaultValue;
    }

    public static float readKeyboardHeight(final SharedPreferences prefs,
            final float defaultValue) {
        final float percentage = prefs.getFloat(
                PREF_KEYBOARD_HEIGHT_SCALE, UNDEFINED_PREFERENCE_VALUE_FLOAT);
        return (percentage != UNDEFINED_PREFERENCE_VALUE_FLOAT) ? percentage : defaultValue;
    }

    public static boolean readUseFullscreenMode(final Resources res) {
        return res.getBoolean(R.bool.config_use_fullscreen_mode);
    }

    public static boolean readShowSetupWizardIcon(final SharedPreferences prefs,
            final Context context) {
        if (!prefs.contains(PREF_SHOW_SETUP_WIZARD_ICON)) {
            final ApplicationInfo appInfo = context.getApplicationInfo();
            final boolean isApplicationInSystemImage =
                    (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            // Default value
            return !isApplicationInSystemImage;
        }
        return prefs.getBoolean(PREF_SHOW_SETUP_WIZARD_ICON, false);
    }

    public static boolean readHasHardwareKeyboard(final Configuration conf) {
        // The standard way of finding out whether we have a hardware keyboard. This code is taken
        // from InputMethodService#onEvaluateInputShown, which canonically determines this.
        // In a nutshell, we have a keyboard if the configuration says the type of hardware keyboard
        // is NOKEYS and if it's not hidden (e.g. folded inside the device).
        return conf.keyboard != Configuration.KEYBOARD_NOKEYS
                && conf.hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES;
    }

    public static boolean isInternal(final SharedPreferences prefs) {
        return prefs.getBoolean(PREF_KEY_IS_INTERNAL, false);
    }

    public void writeLastUsedPersonalizationToken(byte[] token) {
        if (token == null) {
            mPrefs.edit().remove(PREF_LAST_USED_PERSONALIZATION_TOKEN).apply();
        } else {
            final String tokenStr = StringUtils.byteArrayToHexString(token);
            mPrefs.edit().putString(PREF_LAST_USED_PERSONALIZATION_TOKEN, tokenStr).apply();
        }
    }

    public byte[] readLastUsedPersonalizationToken() {
        final String tokenStr = mPrefs.getString(PREF_LAST_USED_PERSONALIZATION_TOKEN, null);
        return StringUtils.hexStringToByteArray(tokenStr);
    }

    public void writeLastPersonalizationDictWipedTime(final long timestamp) {
        mPrefs.edit().putLong(PREF_LAST_PERSONALIZATION_DICT_WIPED_TIME, timestamp).apply();
    }

    public long readLastPersonalizationDictGeneratedTime() {
        return mPrefs.getLong(PREF_LAST_PERSONALIZATION_DICT_WIPED_TIME, 0);
    }

    public void writeCorpusHandlesForPersonalization(final Set<String> corpusHandles) {
        mPrefs.edit().putStringSet(PREF_CORPUS_HANDLES_FOR_PERSONALIZATION, corpusHandles).apply();
    }

    public Set<String> readCorpusHandlesForPersonalization() {
        final Set<String> emptySet = Collections.emptySet();
        return mPrefs.getStringSet(PREF_CORPUS_HANDLES_FOR_PERSONALIZATION, emptySet);
    }

    public static void writeEmojiRecentKeys(final SharedPreferences prefs, String str) {
        prefs.edit().putString(PREF_EMOJI_RECENT_KEYS, str).apply();
    }

    public static String readEmojiRecentKeys(final SharedPreferences prefs) {
        return prefs.getString(PREF_EMOJI_RECENT_KEYS, "");
    }

    public static void writeLastTypedEmojiCategoryPageId(
            final SharedPreferences prefs, final int categoryId, final int categoryPageId) {
        final String key = PREF_EMOJI_CATEGORY_LAST_TYPED_ID + categoryId;
        prefs.edit().putInt(key, categoryPageId).apply();
    }

    public static int readLastTypedEmojiCategoryPageId(
            final SharedPreferences prefs, final int categoryId) {
        final String key = PREF_EMOJI_CATEGORY_LAST_TYPED_ID + categoryId;
        return prefs.getInt(key, 0);
    }

    public static void writeLastShownEmojiCategoryId(
            final SharedPreferences prefs, final int categoryId) {
        prefs.edit().putInt(PREF_LAST_SHOWN_EMOJI_CATEGORY_ID, categoryId).apply();
    }

    public static int readLastShownEmojiCategoryId(
            final SharedPreferences prefs, final int defValue) {
        return prefs.getInt(PREF_LAST_SHOWN_EMOJI_CATEGORY_ID, defValue);
    }

    private void upgradeAutocorrectionSettings(final SharedPreferences prefs, final Resources res) {
        final String thresholdSetting =
                prefs.getString(PREF_AUTO_CORRECTION_THRESHOLD_OBSOLETE, null);
        if (thresholdSetting != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(PREF_AUTO_CORRECTION_THRESHOLD_OBSOLETE);
            final String autoCorrectionOff =
                    res.getString(R.string.auto_correction_threshold_mode_index_off);
            if (thresholdSetting.equals(autoCorrectionOff)) {
                editor.putBoolean(PREF_AUTO_CORRECTION, false);
            } else {
                editor.putBoolean(PREF_AUTO_CORRECTION, true);
            }
            editor.commit();
        }
    }
}
