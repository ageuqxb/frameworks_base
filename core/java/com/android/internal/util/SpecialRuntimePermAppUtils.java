/*
 * Copyright (C) 2022 GrapheneOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util;

import android.Manifest;
import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.os.RemoteException;

public class SpecialRuntimePermAppUtils {
    private static final int FLAG_INITED = 1;
    public static final int FLAG_REQUESTS_INTERNET_PERMISSION = 1 << 1;
    public static final int FLAG_AWARE_OF_RUNTIME_INTERNET_PERMISSION = 1 << 2;

    private static volatile int cachedFlags;

    public static boolean hasInternetPermission() {
        Context ctx = AppGlobals.getInitialApplication();
        return ctx.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestsInternetPermission() {
        return (getFlags() & FLAG_REQUESTS_INTERNET_PERMISSION) != 0;
    }

    public static boolean awareOfRuntimeInternetPermission() {
        return (getFlags() & FLAG_AWARE_OF_RUNTIME_INTERNET_PERMISSION) != 0;
    }

    public static boolean isInternetCompatEnabled() {
        return !hasInternetPermission() && requestsInternetPermission() && !awareOfRuntimeInternetPermission();
    }

    private static int getFlags() {
        int cache = cachedFlags;
        if (cache != 0) {
            return cache;
        }

        IPackageManager pm = AppGlobals.getPackageManager();
        String pkgName = AppGlobals.getInitialPackage();
        try {
            return (cachedFlags = pm.getSpecialRuntimePermissionFlags(pkgName) | FLAG_INITED);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
