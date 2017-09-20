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

import git
import logging
import os
import shutil
import subprocess
import sys
import tempfile
from contextlib import closing
from lxml import etree

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
REPOS_DIR = os.path.realpath(os.path.join(CURRENT_DIR, '..', '..'))
ANDROID_REPO = os.path.join(REPOS_DIR, 'rogerthat-android-client')

ANDROID_SRC_DIR = os.path.join(CURRENT_DIR, '..', 'rogerthat', 'src')
SRC_JAVA_DIR = os.path.join(ANDROID_SRC_DIR, 'main', 'java')

sys.path.append(os.path.join(REPOS_DIR, 'rogerthat-build', 'src')); import app_utils

LICENSE = app_utils.get_license_header()

PLUGINS = {
    "cordova-plugin-compat": {
        "url": "cordova-plugin-compat@1.1.0"
    },
    "cordova-plugin-whitelist": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-whitelist#1.3.2"
    },
    "rogerthat-plugin": {
        "url": "https://github.com/rogerthat-platform/cordova-rogerthat-plugin#master"
    },
    "rogerthat-payments-plugin": {
        "url": "https://github.com/rogerthat-platform/cordova-rogerthat-payments-plugin#master"
    },
    "cordova-plugin-file": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-file#rt/4.3.3"
    },
    "cordova-plugin-media": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-media#rt/3.0.1",
        "dependencies": [
            "cordova-plugin-file"
        ]
    },
    "cordova-plugin-contacts": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-contacts#2.3.1"
    },
    "cordova-call-number-plugin": {
        "url": "https://github.com/rogerthat-platform/cordova-call-number-plugin#1.0.1"
    },
    "cordova-sms-plugin": {
        "url": "https://github.com/rogerthat-platform/cordova-sms-plugin#v0.1.11"
    },
    "cordova-plugin-videoplayer": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-videoplayer#rt/1.0.1"
    },
    "cordova-plugin-globeconnect": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-globeconnect#master:/cordova-plugin-globeconnect",
        "dependencies": [
            "cordova-plugin-add-swift-support",
            "cordova-plugin-inappbrowser"
        ]
    },
    "cordova-plugin-add-swift-support": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-add-swift-support#1.6.2"
    },
    "cordova-plugin-statusbar": {
        "url": "cordova-plugin-statusbar@2.2.3"
    },
    "cordova-plugin-splashscreen": {
        "url": "https://github.com/rogerthat-platform/cordova-plugin-splashscreen#custom"
    },
    'cordova-plugin-inappbrowser': {
        'url': 'cordova-plugin-inappbrowser@1.7.1'
    },
    'cordova-plugin-browsertab': {
        'url': 'cordova-plugin-browsertab@0.2.0'
    },
    'cordova-plugin-app-version': {
        'url': 'cordova-plugin-app-version@0.1.9'
    }
}

APPS = {
    'rogerthat-payment': {
        'url': 'https://github.com/rogerthat-platform/rogerthat-payment#master'
    }
}

# The order of these is important
DEFAULT_PLUGINS = ['cordova-plugin-compat', 'cordova-plugin-whitelist', 'rogerthat-plugin',
                   'cordova-plugin-splashscreen', 'cordova-plugin-statusbar', 'cordova-plugin-inappbrowser',
                   'cordova-plugin-browsertab']

def install_cordova_plugins(app_id, cordova_plugins, cordova_apps, colors):
    _check_cordova_command()

    install_cordova_apps(app_id, cordova_apps, colors)

    plugins = _get_plugins(cordova_plugins)
    _generate_cordova_plugins_file(plugins, cordova_apps)

    tmp_dir = tempfile.mkdtemp()
    try:
        with app_utils.pushd(tmp_dir):
            _fake_cordova_android_project()
            _update_cordova_android()
            _install_plugins(app_id, plugins, 'cordova_config')
    finally:
        shutil.rmtree(tmp_dir)


def install_cordova_apps(app_id, cordova_apps, colors):
    if not cordova_apps:
        return

    _check_cordova_command()

    unknown_cordova_apps = filter(lambda a: a not in APPS, cordova_apps)
    if unknown_cordova_apps:
        raise Exception('The following cordova apps are not defined: %s' % unknown_cordova_apps)

    for cordova_app in cordova_apps:
        _install_cordova_app(app_id, cordova_app, colors)


def _install_cordova_app(app_id, cordova_app_name, colors):
    logging.info('* Installing %s', cordova_app_name)

    # Clone the repository if it doesn't exist yet
    url = APPS[cordova_app_name]['url']
    splitted_url = url.split('#')
    repo_dir = os.path.join(os.path.expanduser('~'), 'tmp', 'cordova', 'build', cordova_app_name)

    def checkout(repo):
        if len(splitted_url) > 1:
            logging.info('  * Switching to branch: %s', splitted_url[1])
            repo.git.checkout(splitted_url[1])

    if os.path.exists(os.path.join(repo_dir, '.git')):
        logging.info('  * Updating local repository: %s', repo_dir)
        repo = git.Repo(repo_dir)
        repo.git.clean('-df')
        repo.git.reset('--hard')
        checkout(repo)
        repo.git.pull()
    else:
        logging.info('  * Cloning repository: %s', splitted_url[0])
        repo = git.Repo.clone_from(splitted_url[0], repo_dir)
        checkout(repo)

    # get dependencies
    tree = etree.parse(os.path.join(repo_dir, "config.xml"))
    dependencies = []
    for p in tree.findall("{http://www.w3.org/ns/widgets}plugin"):
        for k, v in zip(p.keys(), p.values()):
            if k == "name":
                dependencies.append(v)

    tmp_dir = tempfile.mkdtemp()
    try:
        with app_utils.pushd(tmp_dir):
            # Create a new cordova-like directory structure.
            _fake_cordova_android_project()

            # Recreate the ./platforms/android/assets/www symlink to assets/cordova-apps/<cordova_app_name>/
            assets_dir = os.path.join(ANDROID_REPO, 'rogerthat', 'src', 'main', 'assets')
            dest_app_dir = os.path.join(assets_dir, 'cordova-apps', cordova_app_name)
            shutil.rmtree(dest_app_dir, ignore_errors=True)
            os.makedirs(dest_app_dir)
            cdv_www_dir = os.path.join('platforms', 'android', 'assets', 'www')
            os.remove(cdv_www_dir)
            os.symlink(dest_app_dir, cdv_www_dir)

            # Install cordova and the dependencies of <cordova_app_name>
            _update_cordova_android()
            _install_plugins(app_id,
                             _get_plugins(dependencies),
                             'cordova_%s_config' % cordova_app_name.replace('-', '_'))
    finally:
        shutil.rmtree(tmp_dir)

    # Check if there's something changed since the last time we built
    last_commit = repo.head.object.hexsha
    last_build_file = os.path.join(repo_dir, '.last_build')
    can_use_cached_build = False
    if os.path.exists(last_build_file):
        with open(last_build_file, 'r') as f:
            can_use_cached_build = last_commit == f.read()

    # Build the cordova app
    www_dir = os.path.join(repo_dir, 'www')
    # todo: Disabling cache until  https://github.com/rogerthat-platform/rogerthat-payment/issues/14 is solved
    can_use_cached_build = False
    logging.info('  * Building %s', cordova_app_name)
    shutil.rmtree(www_dir, ignore_errors=True)
    with app_utils.pushd(repo_dir):
        if not can_use_cached_build:
            # Not needed if commit hash hasn't changed.
            subprocess.check_call(['npm', 'install'])
        args = '--primary_color=%s --secondary_color=%s' % (colors['primary_color'], colors['app_tint_color'])
        subprocess.check_call(['npm', 'run', 'build:prepare', args])
        # If we can use cache, we only regenerate stylesheets (for colors). Else we completely rebuild the app.
        if not can_use_cached_build:
            subprocess.check_call(['npm', 'run', 'build:prod'])
        else:
            subprocess.check_call(['npm', 'run', 'build:style'])

    with open(last_build_file, 'w+') as f:
        f.write(last_commit)

    # Copy the build result to assets/cordova-apps/<cordova_app_name>
    logging.info('  * Copying %s to %s', cordova_app_name, dest_app_dir)
    app_utils.copytree(www_dir, dest_app_dir)


def _check_cordova_command():
    if subprocess.call(['which', 'cordova'], stdout=subprocess.PIPE, stderr=subprocess.PIPE) != 0:
        raise Exception('The `cordova` command is not found. Install it using `npm install -g cordova`.')


def _get_plugins(cordova_plugins):
    plugins = list(DEFAULT_PLUGINS)

    for cordova_plugin in cordova_plugins:
        if cordova_plugin in plugins:
            continue
        if cordova_plugin not in PLUGINS:
            raise Exception('The `%s` plugin is not found.' % cordova_plugin)
        plugins.append(cordova_plugin)

    return plugins


def _generate_cordova_plugins_file(plugins, apps):
    output = u'''%(LICENSE)s

package com.mobicage.rogerthat.cordova;

import java.util.Arrays;
import java.util.List;

public class CordovaSettings {
    public static final List<String> PLUGINS = Arrays.asList(%(plugins)s);
    public static final List<String> APPS = Arrays.asList(%(apps)s);
}
''' % dict(LICENSE=LICENSE,
           plugins=','.join(['"%s"' % s for s in plugins]),
           apps=','.join(['"%s"' % s for s in apps]))

    path = os.path.join(SRC_JAVA_DIR, "com", "mobicage", "rogerthat", "cordova")
    _mkdir(path)

    with open(os.path.join(path, "CordovaSettings.java"), 'w+') as f:
        f.write(output.encode('utf-8'))


def _get_plugin_urls(plugin):
    urls = []
    for dependency in PLUGINS[plugin].get('dependencies', []):
        urls.extend(_get_plugin_urls(dependency))
    urls.append(PLUGINS[plugin]["url"])
    return urls


def _fake_cordova_android_project():
    '''
    Creating the following directory structure, with symlinks to directories in the rogerthat-android-client folder.
    This way we can use the `cordova plugin add` command to install plugins.

    ./config.xml                            -> rogerthat-android-client/release-script/config.xml
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
    ./platforms/android/src                 -> rogerthat-android-client/rogerthat/src/main/java (copied, no symlink)
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

        # Create the src and assets folders in the tmp project
        _mkdir('assets')
        _mkdir('src')

        # Create the symlinks which are in the root folder in rogerthat-android-client and the platform/android dir
        for src in ('android.json', 'cordova', 'cordovaLib'):
            os.symlink(os.path.join(ANDROID_REPO, src), src)

        os.symlink(os.path.join(ANDROID_REPO, 'rogerthat', 'libs'), 'libs')

        # Create symlinks from rogerthat-android-client/src/main/ to the platform/android dir
        for src, dst in (('AndroidManifest.xml', 'AndroidManifest.xml'),
                         ('assets/cordova', 'assets/www'),
                         ('res', 'res')):
            os.symlink(os.path.join(ANDROID_REPO, 'rogerthat', 'src', 'main', src), dst)


def _update_cordova_android():
    cmd = ['cordova', 'platform', 'update', 'android']
    logging.info('* Executing `%s` in %s', ' '.join(cmd), os.path.abspath(os.path.curdir))
    subprocess.call(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    # For some reason, the first time always fails. The second time too, but it's OK, plugins can be installed
    subprocess.call(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)


def _install_plugins(app_id, plugins, config_filename):
    # List plugin URLs with all their dependencies
    urls  = list()
    for plugin in plugins:
        for url in _get_plugin_urls(plugin):
            if url not in urls:
                urls.append(url)

    # Install plugins using the `cordova` command
    for url in urls:
        cmd = ['cordova', 'plugin', 'add', url, '--save']
        logging.info('* Executing `%s`', ' '.join(cmd))
        subprocess.check_call(cmd)

    # Copy java files. Not working with a symlinks here because cordova doesn't like it when files already exist.
    app_utils.copytree(os.path.join('platforms', 'android', 'src'),
                       os.path.join(ANDROID_REPO, 'rogerthat', 'src', 'main', 'java'))

    # Correct the package name
    xml_dir = os.path.join(ANDROID_REPO, 'rogerthat', 'src', 'main', 'res', 'xml')
    if app_id != 'rogerthat':
        app_id_with_dots = app_id.replace('-', '.')
        for f in (os.path.join(ANDROID_REPO, 'android.json'),
                  os.path.join(xml_dir, 'config.xml')):
            _replace_in_file(f, 'com.mobicage.rogerth.at', 'com.mobicage.rogerthat.%s' % app_id_with_dots)

    # Rename rogerthat/src/main/res/config.xml to rogerthat/src/main/res/<cordova_app_name>_config.xml
    os.rename(os.path.join(xml_dir, 'config.xml'),
              os.path.join(xml_dir, '%s.xml' % config_filename))



def _replace_in_file(path, old, new):
    with closing(open(path, 'r+')) as f:
        s = f.read()
        f.seek(0)
        f.write(s.replace(old, new))
        f.truncate()


def _mkdir(path):
    if not os.path.exists(path):
        os.makedirs(path)
