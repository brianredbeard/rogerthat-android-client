# -*- coding: utf-8 -*-
# Copyright 2016 Mobicage NV
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# @@license_version:1.1@@

import os

res_dir = os.path.join(os.path.dirname(__file__), '..', 'rogerthat', 'src', 'main', 'res')
for parent_dir, sub_dirs, files in os.walk(res_dir):
    for filename in files:
        if os.path.splitext(filename)[1] == '.xml':
            full_filename = os.path.join(parent_dir, filename)
            with open(full_filename, 'r+') as f:
                s = f.read()
                if 'google_maps_debug_api_key' in s:
                    print "Replacing Google Maps debug key with release key in %s" % full_filename
                    s = s.replace('google_maps_debug_api_key', 'google_maps_release_api_key')
                    f.seek(0)
                    f.write(s)
                    f.truncate()
