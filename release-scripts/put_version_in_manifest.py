# -*- coding: utf-8 -*-
# Copyright 2017 GIG Technology NV
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
# @@license_version:1.3@@

import os
import re
import sys

manifest = os.path.join(os.path.dirname(__file__), '..', 'rogerthat', 'src', 'main', 'AndroidManifest.xml')


if len(sys.argv) != 3:
    raise Exception, "VERSION_NAME, VERSION_CODE is a required argument"
else:
    VERSION_NAME = sys.argv[1]
    VERSION_CODE = sys.argv[2]
    with open(manifest, 'r+') as f:
        s = f.read()
        version_code = int(re.findall('android:versionCode="(.*)"', s)[0])
        version_name = re.findall('android:versionName="(.*)"', s)[0]

        print 'Bumping versionCode from %s to %s' % (version_code, VERSION_CODE)
        s = re.sub('android:versionCode=".*"', 'android:versionCode="%s"' % VERSION_CODE, s)
        print 'Bumping versionName from %s to %s' % (version_name, VERSION_NAME)
        s = re.sub('android:versionName=".*"', 'android:versionName="%s"' % VERSION_NAME, s)
        f.seek(0)
        f.write(s)
        f.truncate()
