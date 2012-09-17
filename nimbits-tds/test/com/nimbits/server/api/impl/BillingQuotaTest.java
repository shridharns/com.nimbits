/*
 * Copyright (c) 2012 Nimbits Inc.
 *
 *    http://www.nimbits.com
 *
 *
 * Licensed under the GNU GENERAL PUBLIC LICENSE, Version 3.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the license is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, eitherexpress or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nimbits.server.api.impl;


import com.nimbits.client.constants.Const;
import com.nimbits.client.enums.EntityType;
import com.nimbits.client.enums.SettingType;
import com.nimbits.client.exception.NimbitsException;
import com.nimbits.client.model.common.CommonFactory;
import com.nimbits.client.model.entity.Entity;
import com.nimbits.client.model.entity.EntityName;
import com.nimbits.client.model.point.Point;
import com.nimbits.client.model.user.User;
import com.nimbits.client.model.value.Value;
import com.nimbits.client.service.settings.SettingsService;
import com.nimbits.client.service.value.ValueService;
import com.nimbits.server.NimbitsServletTest;
import com.nimbits.server.admin.quota.QuotaFactory;
import com.nimbits.server.transactions.service.user.UserServerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created with IntelliJ IDEA.
 * User: benjamin
 * Date: 8/14/12
 * Time: 10:56 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:META-INF/applicationContext.xml"
})
public class BillingQuotaTest  extends NimbitsServletTest {

    @Resource(name = "valueApi")
    ValueServletImpl valueServlet;

    @Resource(name = "commonFactory")
    CommonFactory commonFactory;

    @Resource(name="valueService")
    ValueService valueService;

    @Resource(name="settingService")
    SettingsService settingsService;

    @Resource(name="userService")
    UserServerService userService;


    @Test(expected = NimbitsException.class)
    public void outOfMoneyTest() throws NimbitsException, IOException {




        settingsService.updateSetting(SettingType.billingEnabled, Const.TRUE);

        user.setBillingEnabled(true);


        entityService.addUpdateEntity(user, user);
        EntityName name = commonFactory.createName(Const.ACCOUNT_BALANCE, EntityType.point);
        List<Entity> list = entityService.getEntityByName(user,name, EntityType.point );
        assertFalse(list.isEmpty());
        Point accountBalance = (Point) list.get(0);
        accountBalance.setDeltaAlarm(1.50);
        accountBalance.setDeltaAlarmOn(true);
        entityService.addUpdateEntity(user, accountBalance);



        double calls = (0.06 /  QuotaFactory.getInstance(emailAddress).getCostPerApiCall());
        for (int i = 0; i < QuotaFactory.getInstance(emailAddress).getMaxDailyQuota()+calls; i++) {
            valueServlet.processGet(req, resp);
        }
        User u = (User) entityService.getEntityByKey(userService.getAnonUser(), user.getKey(), EntityType.user).get(0);
       // System.out.println(u.getBilling().getAccountBalance());
        List<Value> currentValueSample = valueService.getCurrentValue(accountBalance);
        assertFalse(currentValueSample.isEmpty());
        Value currentValue = currentValueSample.get(0);
        Assert.assertEquals(0.99,currentValue.getDoubleValue(), .001);

    }

    @Test(expected = NimbitsException.class)
    public void overDailyBudgetTest() throws NimbitsException, IOException {

        settingsService.updateSetting(SettingType.billingEnabled, Const.TRUE);

        user.setBillingEnabled(true);

//
//        user.getBilling().setAccountBalance(0.05);
//        user.getBilling().setBillingEnabled(true);
//        user.getBilling().setMaxDailyAllowance(1.50);

        double startingBalance = 5.00;

        entityService.addUpdateEntity(user, user);
        EntityName name = commonFactory.createName(Const.ACCOUNT_BALANCE, EntityType.point);
        List<Entity> list = entityService.getEntityByName(user,name, EntityType.point );
        assertFalse(list.isEmpty());
        Point accountBalance = (Point) list.get(0);
        accountBalance.setDeltaAlarm(0.01);
        accountBalance.setDeltaAlarmOn(true);

        entityService.addUpdateEntity(user, accountBalance);

        userService.fundAccount(user, BigDecimal.valueOf(startingBalance));

        List<Value> sample = valueService.getCurrentValue(accountBalance);
        assertFalse(sample.isEmpty());
        Value balance = sample.get(0);


        assertEquals(startingBalance, balance.getDoubleValue(), 0.0001);
        double nickle = 0.05;   //try to write a nickles worth on a 2 cent budget


        double calls = (nickle /  QuotaFactory.getInstance(emailAddress).getCostPerApiCall());
        for (int i = 0; i < QuotaFactory.getInstance(emailAddress).getMaxDailyQuota()+calls; i++) {
            valueServlet.processGet(req, resp);
        }






    }

    @Test
    public void fundAccountTest() throws NimbitsException, IOException {

        settingsService.updateSetting(SettingType.billingEnabled, Const.TRUE);

        user.setBillingEnabled(true);

//
//        user.getBilling().setAccountBalance(0.05);
//        user.getBilling().setBillingEnabled(true);
//        user.getBilling().setMaxDailyAllowance(1.50);

        double startingBalance = 5.00;

        entityService.addUpdateEntity(user, user);
        EntityName name = commonFactory.createName(Const.ACCOUNT_BALANCE, EntityType.point);
        List<Entity> list = entityService.getEntityByName(user,name, EntityType.point );
        assertFalse(list.isEmpty());
        Point accountBalance = (Point) list.get(0);
        accountBalance.setDeltaAlarm(0.01);
        accountBalance.setDeltaAlarmOn(true);

        entityService.addUpdateEntity(user, accountBalance);

       userService.fundAccount(user, BigDecimal.valueOf(startingBalance));



        for (int i = 0; i < 10; i++) {
            List<Value> sample = valueService.getCurrentValue(accountBalance);
            assertFalse(sample.isEmpty());
            Value balance = sample.get(0);
            assertEquals(startingBalance, balance.getDoubleValue(), 0.0001);

        }





    }




    @Test
    public void businessAsUsual() throws NimbitsException, IOException {

       settingsService.updateSetting(SettingType.billingEnabled, Const.TRUE);

        user.setBillingEnabled(true);

//
//        user.getBilling().setAccountBalance(0.05);
//        user.getBilling().setBillingEnabled(true);
//        user.getBilling().setMaxDailyAllowance(1.50);

        double startingBalance = 5.00;

        entityService.addUpdateEntity(user, user);
        EntityName name = commonFactory.createName(Const.ACCOUNT_BALANCE, EntityType.point);
        List<Entity> list = entityService.getEntityByName(user,name, EntityType.point );
        assertFalse(list.isEmpty());
        Point accountBalance = (Point) list.get(0);
        accountBalance.setDeltaAlarm(1.50);
        accountBalance.setDeltaAlarmOn(true);

        entityService.addUpdateEntity(user, accountBalance);

        userService.fundAccount(user, BigDecimal.valueOf(startingBalance));

        List<Value> sample = valueService.getCurrentValue(accountBalance);
        assertFalse(sample.isEmpty());
        Value balance = sample.get(0);


        assertEquals(startingBalance, balance.getDoubleValue(), 0.0001);
        double penny = 0.01;


        double calls = (penny /  QuotaFactory.getInstance(emailAddress).getCostPerApiCall());
        for (int i = 0; i < QuotaFactory.getInstance(emailAddress).getMaxDailyQuota()+calls; i++) {
            valueServlet.processGet(req, resp);
        }

        User u = (User) entityService.getEntityByKey(user, user.getKey(), EntityType.user).get(0);
        // System.out.println(u.getBilling().getAccountBalance());
        List<Value> currentValueSample =valueService.getCurrentValue(accountBalance);
        assertFalse(currentValueSample.isEmpty());
        Value currentValue = currentValueSample.get(0);
        Assert.assertEquals(startingBalance - penny,currentValue.getDoubleValue(), .001);



    }



}
