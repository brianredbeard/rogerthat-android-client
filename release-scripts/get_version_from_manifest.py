#!/usr/bin/env python
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

if __name__ == '__main__':
    manifest_path = os.path.join(os.path.dirname(__file__), '..', 'rogerthat', 'src', 'main', 'AndroidManifest.xml')
    with open(manifest_path, 'r+') as f:
        manifest = f.read()
        # version should be "major.minor"
        version = re.findall('android:versionName="(\d+\.\d+).*"', manifest)[0]
        sys.stdout.write(version)