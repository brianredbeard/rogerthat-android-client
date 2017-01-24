# Copyright 2016 Mobicage NV
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# @@license_version:1.1@@

import os
import yaml

CURRENT_PATH = os.path.dirname(os.path.realpath(__file__))


def main():
    app_config = None
    try:
        with open(os.path.join(CURRENT_PATH, '..', 'apps', 'rogerthat', 'build.yaml'), 'r') as f:
            app_config = yaml.load(f.read())
    except Exception as e:
        # File not found
        if e.errno == 2:
            pass
        else:
            raise

    with open(os.path.join(CURRENT_PATH, '.bootstrap_data','CustomCloudConstants.java.start'), 'r') as start_file:
        with open(os.path.join(CURRENT_PATH, 'rogerthat','src','main','java','com','mobicage','rpc','config','CustomCloudConstants.java'), 'w') as cloud_constants:
            file_contents = start_file.read()
            if app_config:
                file_contents = file_contents.replace('dummy_dashboard_email', app_config['APP_CONSTANTS']['DASHBOARD_EMAIL'])
                file_contents = file_contents.replace('dummy_email_encryption_key', app_config['APP_CONSTANTS']['EMAIL_HASH_ENCRYPTION_KEY'])
            cloud_constants.write(file_contents)

if __name__ == "__main__":
    main()
