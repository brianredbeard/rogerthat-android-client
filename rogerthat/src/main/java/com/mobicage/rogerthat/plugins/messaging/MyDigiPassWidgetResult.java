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

package com.mobicage.rogerthat.plugins.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.mobicage.models.properties.forms.MyDigiPassAddress;
import com.mobicage.models.properties.forms.MyDigiPassEidAddress;
import com.mobicage.models.properties.forms.MyDigiPassEidProfile;
import com.mobicage.models.properties.forms.MyDigiPassProfile;
import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rpc.IncompleteMessageException;
import com.mobicage.to.messaging.forms.MyDigiPassWidgetResultTO;

public class MyDigiPassWidgetResult extends MyDigiPassWidgetResultTO {

    public static class EidProfile extends MyDigiPassEidProfile {

        public EidProfile(Map<String, Object> json) throws IncompleteMessageException {
            super(json);
        }

        public String getDisplayName() {
            final List<String> parts = new ArrayList<String>();
            if (!TextUtils.isEmptyOrWhitespace(first_name))
                parts.add(first_name);
            if (!TextUtils.isEmptyOrWhitespace(first_name_3))
                parts.add(first_name_3);
            if (!TextUtils.isEmptyOrWhitespace(last_name))
                parts.add(last_name);
            return android.text.TextUtils.join(" ", parts);
        }

        public String getDisplayGender(Context ctx) {
            if ("M".equals(gender)) {
                return ctx.getString(R.string.male);
            }
            if ("F".equals(gender)) {
                return ctx.getString(R.string.female);
            }
            return gender;
        }

        public String getDisplayCardInfo(Context ctx) {
            return String.format("%s: %s\n%s: %s", ctx.getString(R.string.card_number), card_number,
                ctx.getString(R.string.chip_number), chip_number);
        }

        public String getDisplayValue() {
            return getDisplayName();
        }
    }

    public static class EidAddress extends MyDigiPassEidAddress {

        public EidAddress(Map<String, Object> json) throws IncompleteMessageException {
            super(json);
        }

        public String getDisplayValue() {
            final List<String> lines = new ArrayList<String>();
            if (!TextUtils.isEmptyOrWhitespace(street_and_number))
                lines.add(street_and_number);

            final List<String> line2 = new ArrayList<String>();
            if (!TextUtils.isEmptyOrWhitespace(zip_code))
                line2.add(zip_code);
            if (!TextUtils.isEmptyOrWhitespace(municipality))
                line2.add(municipality);
            if (line2.size() > 0)
                lines.add(android.text.TextUtils.join(" ", line2));

            return android.text.TextUtils.join("\n", lines);
        }
    }

    public static class Profile extends MyDigiPassProfile {

        public Profile(Map<String, Object> json) throws IncompleteMessageException {
            super(json);
        }

        public String getDisplayName() {
            final List<String> parts = new ArrayList<String>();
            if (!TextUtils.isEmptyOrWhitespace(first_name))
                parts.add(first_name);
            if (!TextUtils.isEmptyOrWhitespace(last_name))
                parts.add(last_name);
            return android.text.TextUtils.join(" ", parts);
        }

        public String getDisplayLanguage() {
            if (TextUtils.isEmptyOrWhitespace(preferred_locale))
                return "";
            final Locale locale = new Locale(preferred_locale);
            return locale.getDisplayLanguage(Locale.getDefault());
        }

        public String getDisplayValue() {
            return getDisplayName();
        }
    }

    public static class Address extends MyDigiPassAddress {

        public Address(Map<String, Object> json) throws IncompleteMessageException {
            super(json);
        }

        public String getDisplayValue() {
            final List<String> lines = new ArrayList<String>();
            if (!TextUtils.isEmptyOrWhitespace(address_1))
                lines.add(address_1);
            if (!TextUtils.isEmptyOrWhitespace(address_2))
                lines.add(address_2);

            final List<String> line2 = new ArrayList<String>();
            if (!TextUtils.isEmptyOrWhitespace(zip))
                line2.add(zip);
            if (!TextUtils.isEmptyOrWhitespace(city))
                line2.add(city);
            if (line2.size() > 0)
                lines.add(android.text.TextUtils.join(" ", line2));

            final List<String> line3 = new ArrayList<String>();
            if (!TextUtils.isEmptyOrWhitespace(state))
                line3.add(state);
            if (!TextUtils.isEmptyOrWhitespace(country))
                line3.add(country);
            if (line3.size() > 0)
                lines.add(android.text.TextUtils.join(", ", line3));

            return android.text.TextUtils.join("\n", lines);
        }
    }

    public EidProfile eid_profile;
    public EidAddress eid_address;
    public Profile profile;
    public Address address;

    @SuppressWarnings("unchecked")
    public MyDigiPassWidgetResult(Map<String, Object> json) throws IncompleteMessageException {
        super(json);
        Object val1 = json.get("address");
        this.address = val1 == null ? null : new Address((Map<String, Object>) val1);

        Object val2 = json.get("eid_address");
        this.eid_address = val2 == null ? null : new EidAddress((Map<String, Object>) val2);

        Object val3 = json.get("eid_profile");
        this.eid_profile = val3 == null ? null : new EidProfile((Map<String, Object>) val3);

        Object val4 = json.get("profile");
        this.profile = val4 == null ? null : new Profile((Map<String, Object>) val4);
    }

}