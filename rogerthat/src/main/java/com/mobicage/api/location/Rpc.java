/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */

package com.mobicage.api.location;

public class Rpc {

    public static void beaconDiscovered(com.mobicage.rpc.IResponseHandler<com.mobicage.to.location.BeaconDiscoveredResponseTO> responseHandler,
            com.mobicage.to.location.BeaconDiscoveredRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.location.beaconDiscovered", arguments, responseHandler);
    }

    public static void beaconInReach(com.mobicage.rpc.IResponseHandler<com.mobicage.to.location.BeaconInReachResponseTO> responseHandler,
            com.mobicage.to.location.BeaconInReachRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.location.beaconInReach", arguments, responseHandler);
    }

    public static void beaconOutOfReach(com.mobicage.rpc.IResponseHandler<com.mobicage.to.location.BeaconOutOfReachResponseTO> responseHandler,
            com.mobicage.to.location.BeaconOutOfReachRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.location.beaconOutOfReach", arguments, responseHandler);
    }

    public static void getBeaconRegions(com.mobicage.rpc.IResponseHandler<com.mobicage.to.beacon.GetBeaconRegionsResponseTO> responseHandler,
            com.mobicage.to.beacon.GetBeaconRegionsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.location.getBeaconRegions", arguments, responseHandler);
    }

    public static void getFriendLocation(com.mobicage.rpc.IResponseHandler<com.mobicage.to.location.GetFriendLocationResponseTO> responseHandler,
            com.mobicage.to.location.GetFriendLocationRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.location.getFriendLocation", arguments, responseHandler);
    }

    public static void getFriendLocations(com.mobicage.rpc.IResponseHandler<com.mobicage.to.location.GetFriendsLocationResponseTO> responseHandler,
            com.mobicage.to.location.GetFriendsLocationRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.location.getFriendLocations", arguments, responseHandler);
    }

}