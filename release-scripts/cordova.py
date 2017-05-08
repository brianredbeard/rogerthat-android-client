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

from contextlib import closing
import os
import shutil
import subprocess
import sys
import tempfile


CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
REPOS_DIR = os.path.realpath(os.path.join(CURRENT_DIR, '..','..'))
ANDROID_REPO = os.path.join(REPOS_DIR, 'rogerthat-android-client')

ANDROID_SRC_DIR = os.path.join(CURRENT_DIR, '..', 'rogerthat', 'src')
SRC_JAVA_DIR = os.path.join(ANDROID_SRC_DIR, 'main', 'java')

sys.path.append(os.path.join(REPOS_DIR, 'rogerthat-build', 'src')); import app_utils

LICENSE = app_utils.get_license_header()

PLUGINS = {
    "cordova-plugin-compat": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-compat#1.1.0",
        "dependencies": []
    },
    "cordova-plugin-whitelist": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-whitelist#1.3.2",
        "dependencies": []
    },
    "cordova-rogerthat-plugin": {
        "url": "https://github.com/rogerthat-platform/cordova-rogerthat-plugin#master",
        "dependencies": []
    },
    "cordova-plugin-media": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-media#rt/3.0.1",
        "dependencies": [
            "https://github.com/rogerthat-platform/cordova-plugin-file#rt/4.3.3"
        ]
    },
    "cordova-plugin-contacts": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-contacts#2.3.1",
        "dependencies": []
    },
    "cordova-call-number-plugin": {
        "url": "https://github.com/rogerthat-platform/cordova-call-number-plugin#1.0.1",
        "dependencies": []
    },
    "cordova-sms-plugin": {
        "url": "https://github.com/rogerthat-platform/cordova-sms-plugin#v0.1.11",
        "dependencies": []
    },
    "cordova-plugin-videoplayer": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-videoplayer#rt/1.0.1",
        "dependencies": []
    },
    "cordova-plugin-globeconnect": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-globeconnect#master:/cordova-plugin-globeconnect",
        "dependencies": [
            "https://github.com/rogerthat-platform/cordova-plugin-add-swift-support#1.6.2",
            "https://github.com/rogerthat-platform/cordova-plugin-inappbrowser#1.7.1"
        ]
    }
}

def _get_plugin_urls(plugin):
    urls = []
    urls.extend(PLUGINS[plugin]["dependencies"])
    urls.append(PLUGINS[plugin]["url"])
    return urls


def install_cordova_plugins(app_id, cordova_plugins):

    if subprocess.call(['which', 'cordova'], stdout=subprocess.PIPE, stderr=subprocess.PIPE) != 0:
        raise Exception('The `cordova` command is not found. Install it using `npm install -g cordova`.')

    plugins = ['cordova-plugin-compat', 'cordova-plugin-whitelist', 'cordova-rogerthat-plugin']
    for cordova_plugin in cordova_plugins:
        if cordova_plugin in plugins:
            continue
        if cordova_plugin not in PLUGINS:
            raise Exception('The `%s` plugin is not found.' % cordova_plugin)
        plugins.append(cordova_plugin)

    output = u'''%(LICENSE)s

package com.mobicage.rogerthat.cordova;

public class CordovaPlugins {
    public static final String[] PLUGINS = new String[] { %(plugins)s };
}
''' % dict(LICENSE=LICENSE,
           plugins=','.join(['"%s"' % s for s in plugins]))

    path = os.path.join(SRC_JAVA_DIR, "com", "mobicage", "rogerthat", "cordova")
    if not os.path.exists(path):
        os.makedirs(path)

    with open(os.path.join(path, "CordovaPlugins.java"), 'w+') as f:
        f.write(output.encode('utf-8'))

    urls = []
    for plugin in plugins:
        urls.extend(_get_plugin_urls(plugin))

    tmp_dir = tempfile.mkdtemp()
    try:
        with app_utils.pushd(tmp_dir):
            _fake_cordova_android_project()
            _update_cordova_android()
            _install_plugins(urls)

            if app_id != 'rogerthat':
                app_id_with_dots = app_id.replace('-', '.')
                for f in (os.path.join(ANDROID_REPO, 'android.json'),
                          os.path.join(ANDROID_REPO, 'rogerthat', 'src', 'main', 'res', 'xml', 'config.xml')):
                    _replace_in_file(f, 'com.mobicage.rogerth.at', 'com.mobicage.rogerthat.%s' % app_id_with_dots)
    finally:
        shutil.rmtree(tmp_dir)


def _fake_cordova_android_project():
    '''
    Creating the following directory structure, with symlinks to directories in the rogerthat-android-client folder.
    This way we can use the `cordova plugin add` command to install plugins.

    ./config.xml                            -> rogerthat-cordova-plugins/config.xml
    ./cordova/
    ./platforms/android/android.json        -> rogerthat-android-client/android.json
    ./platforms/android/AndroidManifest.xml -> rogerthat-android-client/rogerthat/src/main/AndroidManifest.xml
    ./platforms/android/assets/www          -> rogerthat-android-client/rogerthat/src/main/assets/cordova
    ./platforms/android/cordova             -> rogerthat-android-client/cordova
    ./platforms/android/CordovaLib          -> rogerthat-android-client/CordovaLib
    ./platforms/android/libs                -> rogerthat-android-client/libs
    ./platforms/android/platform_www
    ./platforms/android/plugins
    ./platforms/android/res                 -> rogerthat-android-client/rogerthat/src/main/res
    ./platforms/android/src                 -> rogerthat-android-client/rogerthat/src/main/java
    ./plugins/
    ./www/
    '''

    # Copy config.xml to the tmp project
    shutil.copy2(os.path.join(CURRENT_DIR, 'config.xml'), '.')
    android_file = os.path.join(ANDROID_REPO, 'android.json')
    if os.path.exists(android_file):
        os.remove(android_file)

    # Create the necessary cordova dirs
    for d in ('cordova', 'plugins', 'www'):
        _mkdir(d)

    # Create the platform specific dirs
    for d in ('platform_www', 'plugins'):
        _mkdir(os.path.join('platforms', 'android', d))

    with app_utils.pushd(os.path.join('platforms', 'android')):
        # Create the assets/cordova folder in rogerthat-android-client
        _mkdir(os.path.join(ANDROID_REPO, 'rogerthat', 'src', 'main', 'assets', 'cordova'))

        # Create the assets folder in the tmp project
        _mkdir('assets')

        # Create the symlinks which are in the root folder in rogerthat-android-client and the platform/android dir
        for src in ('android.json', 'cordova', 'cordovaLib'):
            os.symlink(os.path.join(ANDROID_REPO, src), src)

        os.symlink(os.path.join(ANDROID_REPO, 'rogerthat', 'libs'), 'libs')

        # Create symlinks from rogerthat-android-client/src/main/ to the platform/android dir
        for src, dst in (('AndroidManifest.xml', 'AndroidManifest.xml'),
                         ('assets/cordova', 'assets/www'),
                         ('res', 'res'),
                         ('java', 'src')):
            os.symlink(os.path.join(ANDROID_REPO, 'rogerthat', 'src', 'main', src), dst)


def _update_cordova_android():
    cmd = ['cordova', 'platform', 'update', 'android']
    print '* Executing `%s` in %s' % (' '.join(cmd), os.path.abspath(os.path.curdir))
    subprocess.call(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    # For some reason, the first time always fails. The second time too, but it's OK, plugins can be installed
    subprocess.call(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)


def _install_plugins(urls):
    for url in urls:
        cmd = ['cordova', 'plugin', 'add', url, '--save']
        print '* Executing `%s`' % ' '.join(cmd)
        subprocess.check_call(cmd)


def _replace_in_file(path, old, new):
    with closing(open(path, 'r+')) as f:
        s = f.read()
        f.seek(0)
        f.write(s.replace(old, new))
        f.truncate()


def _mkdir(path):
    if not os.path.exists(path):
        os.makedirs(path)
