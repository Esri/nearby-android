# Nearby Places for Android

<!-- MDTOC maxdepth:6 firsth1:0 numbering:0 flatten:0 bullets:1 updateOnSave:1 -->

- [Features](#features)   
- [Detailed Documentation](#detailed-documentation)   
- [Development Instructions](#development-instructions)   
   - [Fork the Repo](#fork-the-repo)   
   - [Clone the Repo](#clone-the-repo)   
      - [Command Line Git](#command-line-git)   
   - [Configuring a Remote for a Fork](#configuring-a-remote-for-a-fork)   
- [Requirements](#requirements)   
- [Resources](#resources)   
- [Issues](#issues)   
- [Contributing](#contributing)   
- [MDTOC](#mdtoc)   
- [Licensing](#licensing)   

<!-- /MDTOC -->
---

This repo provides an example app called Nearby Places for Android devices built with the [ArcGIS Runtime SDK for Android](https://developers.arcgis.com/android/).  With some minor customization, you can change the types of places displayed or simply use the app as is.

Visit Esri's [**open source app site**](https://developers.arcgis.com/example-apps/nearby-android/?utm_source=github&utm_medium=web&utm_campaign=example_apps_nearby_android) to read more about this app and how the Runtime features are implemented.
Join our [**GeoNet community**](https://community.esri.com/community/developers/native-app-developers/arcgis-runtime-sdk-for-android) and keep up to date on the latest discussions about open source apps!

## Features

 * Geocoding with categories
 * Displaying device location
 * Calculating bearing and distance

## Detailed Documentation

Read the [docs](./docs/README.md) for a detailed explanation of the application, including its architecture and how it leverages the ArcGIS platform, as well as how you can begin using the app right away.

## Development Instructions

This Nearby Places repo is an Android Studio Project and App Module that can be directly cloned and imported into Android Studio.

### Fork the Repo

**Fork** the [Nearby Places Android](https://github.com/Esri/nearby-android/fork) repo.

### Clone the Repo

Once you have forked the repo, you can make a clone.

#### Command Line Git

1. [Clone](https://help.github.com/articles/fork-a-repo#step-2-clone-your-fork) the Nearby Places repo.
2. `cd` into the `nearby-android` folder.
3. Make your changes and create a [pull request](https://help.github.com/articles/creating-a-pull-request).

### Configuring a Remote for a Fork

If you make changes in the fork and would like to [sync](https://help.github.com/articles/syncing-a-fork/) those changes with the upstream repository, you must first [configure the remote](https://help.github.com/articles/configuring-a-remote-for-a-fork/). This will be required when you have created local branches and would like to make a [pull request](https://help.github.com/articles/creating-a-pull-request) to your upstream branch.

1. In the Terminal (for Mac users) or command prompt (for Windows and Linux users) type `git remote -v` to list the current configured remote repo for your fork.
2. `git remote add upstream https://github.com/Esri/nearby-android.git` to specify new remote upstream repository that will be synced with the fork. You can type `git remote -v` to verify the new upstream.

If there are changes made in the Original repository, you can sync the fork to keep it updated with upstream repository.

1. In the terminal, change the current working directory to your local project.
2. Type `git fetch upstream` to fetch the commits from the upstream repository.
3. `git checkout master` to checkout your fork's local master branch.
4. `git merge upstream/master` to sync your local `master` branch with `upstream/master`. **Note**: Your local changes will be retained and your fork's master branch will be in sync with the upstream repository.

## Requirements

* [JDK 6 or higher](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Android Studio](http://developer.android.com/sdk/index.html)

## Resources

* [Nearby Places](nearby-app/README.md)
* [ArcGIS Runtime SDK for Android Developers Site](https://developers.arcgis.com/android/)
* [ArcGIS Mobile Blog](http://blogs.esri.com/esri/arcgis/category/mobile/)
* [ArcGIS Developer Blog](http://blogs.esri.com/esri/arcgis/category/developer/)
* [Google+](https://plus.google.com/+esri/posts)
* [twitter@ArcGISRuntime](https://twitter.com/ArcGISRuntime)
* [twitter@esri](http://twitter.com/esri)

## Issues

Find a bug or want to request a new feature enhancement?  Let us know by submitting an issue.

## Contributing

Anyone and everyone is welcome to [contribute](CONTRIBUTING.md). We do accept pull requests.

1. Get involved
2. Report issues
3. Contribute code
4. Improve documentation

## MDTOC

Generating table of contents for documents in this repository was performed using the [MDTOC package for Atom](https://atom.io/packages/atom-mdtoc).

## Licensing

Copyright 2017 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [LICENSE](LICENSE) file.

For information about licensing your deployed app, see [License your app](https://developers.arcgis.com/android/guide/license-your-app.htm).

[](Esri Tags: ArcGIS Android Mobile)
[](Esri Language: Java)​
