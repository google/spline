/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.example.spline.util;

/**
 * Static utility methods for sharing common File operations across components.
 */
public class FileUtils {

    public static String getPrettyFilename(String filename) {
        if (filename == null) {
            return null;
        }

        int i = filename.indexOf(".");
        if (i > -1) {
            return filename.substring(0, i);
        } else {
            return filename;
        }
    }
}
