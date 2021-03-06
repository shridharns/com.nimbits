/*
 * Copyright (c) 2010 Nimbits Inc.
 *
 * http://www.nimbits.com
 *
 *
 * Licensed under the GNU GENERAL PUBLIC LICENSE, Version 3.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the license is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, eitherexpress or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nimbits.cloudplatform.server.io.storage;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.GSFileOptions;
import com.nimbits.cloudplatform.server.transactions.user.UserTransaction;

import java.io.IOException;

/**
 * Created by Benjamin Sautner
 * User: bsautner
 * Date: 3/19/12
 * Time: 2:02 PM
 */
public class StorageServiceImpl {

    private UserTransaction userService;

    public void test() throws IOException {
        FileService fileService = FileServiceFactory.getFileService();
        GSFileOptions.GSFileOptionsBuilder optionsBuilder = new GSFileOptions.GSFileOptionsBuilder()
                .setBucket("com/nimbits")
                .setKey("my_object")
                .setAcl("public-read");
        AppEngineFile writableFile = fileService.createNewGSFile(optionsBuilder.build());


    }


    public void setUserService(UserTransaction userService) {
        this.userService = userService;
    }

    public UserTransaction getUserService() {
        return userService;
    }
}
