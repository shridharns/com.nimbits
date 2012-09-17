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

package com.nimbits.server.api.impl;

import com.nimbits.client.common.Utils;
import com.nimbits.client.constants.Const;
import com.nimbits.client.constants.Path;
import com.nimbits.client.constants.Words;
import com.nimbits.client.enums.EntityType;
import com.nimbits.client.enums.ExportType;
import com.nimbits.client.enums.Parameters;
import com.nimbits.client.enums.ProtectionLevel;
import com.nimbits.client.exception.NimbitsException;
import com.nimbits.client.model.common.CommonFactoryLocator;
import com.nimbits.client.model.entity.Entity;
import com.nimbits.client.model.entity.EntityName;
import com.nimbits.client.model.timespan.Timespan;
import com.nimbits.client.model.user.User;
import com.nimbits.client.model.value.Value;
import com.nimbits.server.admin.logging.LogHelper;
import com.nimbits.server.api.ApiServlet;
import com.nimbits.server.time.TimespanServiceFactory;
import com.nimbits.server.transactions.service.entity.EntityServiceImpl;
import com.nimbits.server.transactions.service.value.ValueServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Deprecated
@Transactional
@Service("chartApi")
public class ChartApiServletImpl extends ApiServlet {
    private static final Logger log = Logger.getLogger(ChartApiServletImpl.class.getName());
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String autoscaleCode = "&chds=a";
    private static final String chartDateCode = "&chd=t:";
    private static final int INT = 512;
    private static final Pattern COMPILE = Pattern.compile(",");
    private EntityServiceImpl entityService;
    private ValueServiceImpl valueService;

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        processGet(req, resp);
    }

    protected void processGet(final HttpServletRequest req, final HttpServletResponse resp) {
        final String formatParam = req.getParameter(Parameters.format.getText());


        try {
            super.doInit(req, resp, getContentType(formatParam));
            final Timespan timespan = getTimestamp(req);


            final ExportType type = getContentType(formatParam);
            log.info(req.getQueryString());
            log.info(req.getParameter(Parameters.email.getText()));

            final boolean doScale = !Utils.isEmptyString(getParam(Parameters.autoscale)) && getParam(Parameters.autoscale).equals(Words.WORD_TRUE);

            if (user==null)  {
                log.severe("Null user in chart api");
            }
            else {
                final List<EntityName> pointList = createPointList(getParam(Parameters.points), getParam(Parameters.point));
                final int count = Utils.isEmptyString(getParam(Parameters.count)) ? 10 : Integer.valueOf(getParam(Parameters.count));

                if (type == ExportType.png) {
                    final String params = generateImageChartParams(req, timespan, count, doScale, user, pointList);
                    sendChartImage(resp, params);
                }


            }
        } catch (IOException e) {
            LogHelper.logException(ChartApiServletImpl.class, e);

        } catch (NimbitsException e) {
            log.warning(e.getMessage());

        }
    }

    private static ExportType getContentType(final String formatParam) {

        return Utils.isEmptyString(formatParam)
                ? ExportType.png : formatParam.equals("image")
                ? ExportType.png : formatParam.equals("table")
                ? ExportType.table : ExportType.plain;
    }



    private String generateImageChartParams(final HttpServletRequest req,
                                                   final Timespan timespan,
                                                   final int valueCount,
                                                   final boolean doScale,
                                                   final User u,
                                                   final Iterable<EntityName> pointList) throws NimbitsException {

        final StringBuilder params = new StringBuilder(INT);
        params.append(req.getQueryString());
        params.append(chartDateCode);

        if (u != null) {
            for (final EntityName pointName : pointList) {


                final List<Entity> list = entityService.getEntityByName(u, pointName, EntityType.point);
                if (list.isEmpty()) {
                    log.info("Couldn't find a point in the chart request.");
                } else {
                    final Entity p = list.get(0);

                    if (p.getProtectionLevel().equals(ProtectionLevel.everyone) || !u.isRestricted()) {


                        final List<Value> values = timespan != null ?

                                valueService.getDataSegment(p, timespan) :
                                valueService.getTopDataSeries(p, valueCount);


                        for (final Value v : values) {
                            params.append(v.getDoubleValue()).append(Const.DELIMITER_COMMA);

                        }
                        if (params.lastIndexOf(Const.DELIMITER_COMMA) > 0) {
                            params.deleteCharAt(params.lastIndexOf(Const.DELIMITER_COMMA));

                        }
                        params.append(Const.DELIMITER_BAR);

                    }


                }
                if (params.lastIndexOf(Const.DELIMITER_BAR) > 0) {
                    params.deleteCharAt(params.lastIndexOf(Const.DELIMITER_BAR));
                }

                if (doScale) {
                    params.append(autoscaleCode);
                }
            }
        }
        return params.toString();
    }

    private static void sendChartImage(final ServletResponse resp, final String params) throws IOException {
        final URL url = new URL(Path.PATH_GOOGLE_CHART_API);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod(Const.METHOD_POST);
        connection.setReadTimeout(Const.DEFAULT_HTTP_TIMEOUT);
        final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(params);
        writer.close();
        final InputStream is = connection.getInputStream();
        resp.setContentType(ExportType.png.getCode());
        final int length = connection.getContentLength();
        log.info(params);
        final OutputStream out = resp.getOutputStream();
        resp.setContentLength(length);
        final byte[] buffer = new byte[length];
        int i;
        while ((i = is.read(buffer)) >= 0) {
            out.write(buffer, 0, i);
        }
        out.close();
    }


    private static List<EntityName> createPointList(final String pointsListParam, final String pointParamName) throws NimbitsException {
        final List<EntityName> pointList = new ArrayList<EntityName>(10);
        if (!Utils.isEmptyString(pointParamName)) {
            pointList.add(CommonFactoryLocator.getInstance().createName(pointParamName, EntityType.point));
        } else if (!Utils.isEmptyString(pointsListParam)) {
            final String[] p1 = COMPILE.split(pointsListParam);
            final List<String> pointsParams = Arrays.asList(p1);
            for (final String pn : pointsParams) {
                pointList.add(CommonFactoryLocator.getInstance().createName(pn, EntityType.point));
            }
        }
        return pointList;
    }

    private static Timespan getTimestamp(final ServletRequest req) throws NimbitsException {
        String startDate = req.getParameter(Parameters.sd.getText());
        String endDate = req.getParameter(Parameters.ed.getText());
        //support for legacy st param
        if (startDate == null) {
            startDate = req.getParameter("st");
        }
        //support for legacy et param
        if (endDate == null) {
            endDate = req.getParameter("et");
        }

        Timespan timespan = null;
        if (startDate != null && endDate != null) {

            timespan = TimespanServiceFactory.getInstance().createTimespan(startDate, endDate);

        }
        return timespan;
    }

    public void setEntityService(EntityServiceImpl entityService) {
        this.entityService = entityService;
    }

    public EntityServiceImpl getEntityService() {
        return entityService;
    }

    public void setValueService(ValueServiceImpl valueService) {
        this.valueService = valueService;
    }

    public ValueServiceImpl getValueService() {
        return valueService;
    }
}
