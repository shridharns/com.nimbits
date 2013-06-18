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

package com.nimbits.user;

import com.nimbits.cloudplatform.client.model.email.*;

/**
 * Created by bsautner
 * User: benjamin
 * Date: 3/13/11
 * Time: 12:28 PM
 */
public class GoogleUser {
    private EmailAddress googleEmailAddress;
    private String googlePassword;

    public EmailAddress getGoogleEmailAddress() {
        return googleEmailAddress;
    }

    public String getGooglePassword() {
        return this.googlePassword;
    }

    private GoogleUser() {
    }

    public GoogleUser(final EmailAddress googleEmailAddress, final String googlePassword) {
        this.googleEmailAddress = googleEmailAddress;
        this.googlePassword = googlePassword;
    }


}