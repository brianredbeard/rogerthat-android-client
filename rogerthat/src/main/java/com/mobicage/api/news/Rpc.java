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

package com.mobicage.api.news;

import com.mobicage.to.news.SaveNewsStatisticsRequestTO;
import com.mobicage.to.news.SaveNewsStatisticsResponseTO;

public class Rpc {

    public static void getNews(com.mobicage.rpc.IResponseHandler<com.mobicage.to.news.GetNewsResponseTO> responseHandler,
            com.mobicage.to.news.GetNewsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.news.getNews", arguments, responseHandler);
    }

    public static void getNewsItems(com.mobicage.rpc.IResponseHandler<com.mobicage.to.news.GetNewsItemsResponseTO> responseHandler,
            com.mobicage.to.news.GetNewsItemsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.news.getNewsItems", arguments, responseHandler);
    }

    public static void saveNewsStatistic(com.mobicage.rpc.IResponseHandler<SaveNewsStatisticsResponseTO> responseHandler,
                                         SaveNewsStatisticsRequestTO request) throws Exception {
        java.util.Map<java.lang.String, java.lang.Object> arguments = new java.util.LinkedHashMap<java.lang.String, java.lang.Object>();
        if (request == null) {
            arguments.put("request", null);
        } else {
            arguments.put("request", request.toJSONMap());
        }
        com.mobicage.rpc.Rpc.call("com.mobicage.api.news.saveNewsStatistic", arguments, responseHandler);
    }

}
