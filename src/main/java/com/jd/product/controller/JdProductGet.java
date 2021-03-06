package com.jd.product.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jd.product.utils.StringUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.List;

/**
 * @Author chenYongJin
 * @Date 2021-05-14
 */
@RestController
@RequestMapping("/jdProductGet")
public class JdProductGet {
    String imgUrl = "http://img12.360buyimg.com/n12/";
    String baseUrl = "https://item.jd.com/";
    String dirRoot = System.getProperty("user.dir")+"/config";

    @RequestMapping("/getDir")
    public String getDir() throws Exception {
        File file = new File(dirRoot+"/config.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String dir = reader.readLine();
        reader.close();
        return dir;
    }

    @RequestMapping("/setDir")
    public String setDir(String dir) throws IOException {
        File file = new File(dirRoot+"/config.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(dir);
        writer.flush();
        writer.close();
        return "";
    }

    @RequestMapping("/getCookie")
    public String getCookie() throws Exception {
        File file = new File(dirRoot+"/cookie.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String dir = reader.readLine();
        reader.close();
        return dir;
    }

    @RequestMapping("/setCookie")
    public String setCookie(String cookie) throws IOException {
        File file = new File(dirRoot+"/cookie.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(cookie);
        writer.flush();
        writer.close();
        return "";
    }

    @RequestMapping("/getLog")
    public String getLog() throws Exception {
        File file = new File(dirRoot+"/log.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int c = -1;
        String log = "";
        while ((c = reader.read()) != -1) {
            log+=reader.readLine();
        }
        reader.close();
        return log;
    }

    public void setLog(String log) throws IOException {
        File file = new File(dirRoot+"/log.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
        writer.write("<p>"+log+"</p>");
        writer.flush();
        writer.close();
    }

    /**
     * ????????????
     * @throws IOException
     */
    public void remLog() throws IOException {
        File file = new File(dirRoot+"/log.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file,false));
        writer.write("");
        writer.flush();
        writer.close();
    }

    @RequestMapping("/get")
    public String get(String url){
        try {
            //????????????
            String item = HttpRequest.get(url).cookie(getCookie()).execute().body();

            //????????????skuid
            String skuid = StringUtil.getSkuid(url);
            setLog("??????id???"+skuid);

            String mainSkuid = StringUtil.getSubString(item,"mainSkuId:'","'");

            //???????????????
            String path = getDir()+"/jd_"+StringUtil.removeStrTeShu(StringUtil.getSubString(item,"name: '","'"));
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            setLog("???????????????"+path);
            String imageListText = "{\"data\":"+StringUtil.getSubString(item, "imageList: ", "]") + "]}";
            JSONArray imgData = JSONUtil.parseObj(imageListText).getJSONArray("data");
            imgData.stream().forEach(it -> {
                String img = imgUrl +it;
                //???????????????
                long size = HttpUtil.downloadFile(img, FileUtil.file(path + "/skiud_"+skuid+"/??????/"+StringUtil.getFileName(img)));
                try {
                    setLog("?????????????????????"+img);
                    setLog("?????????????????????"+size);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            //????????????
            this.saveDesc(path,skuid,mainSkuid);

            //????????????
            String vid = StringUtil.getSubString(item, "imageAndVideoJson: ", "},")+"}";
            if (!vid.equals("{}")){
                vid = JSONUtil.parseObj(vid).getStr("mainVideoId");
                this.saveVideo(path,vid);
            }

            //??????????????????
            this.saveOffice(path, skuid);

            //??????????????????????????????
            String colorSizeText = "{\"data\":"+StringUtil.getSubString(item, "colorSize: ", "]") + "]}";
            JSONArray colorSizeData = JSONUtil.parseObj(colorSizeText).getJSONArray("data");
            colorSizeData.stream().forEach(it -> {
                JSONObject ite = JSONUtil.parseObj(it);
                String skuId = ite.getStr("skuId");
                if (!skuId.equals(skuid)) {
                    System.out.println(skuId);

                    //????????????
                    //????????????
                    String res = null;
                    try {
                        res = HttpRequest.get(baseUrl+skuId+".html").cookie(getCookie()).execute().body();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String imageList = "{\"data\":"+StringUtil.getSubString(res, "imageList: ", "]") + "]}";
                    JSONArray imgArrayData = JSONUtil.parseObj(imageList).getJSONArray("data");
                    imgArrayData.stream().forEach(iteme -> {
                        String img = imgUrl +iteme;
                        //????????????
                        long size = HttpUtil.downloadFile(img, FileUtil.file(path + "/skiud_"+skuId+"/??????/"+StringUtil.getFileName(img)));
                        try {
                            setLog("?????????????????????"+img);
                            setLog("?????????????????????"+size);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    String mainSkuidt = StringUtil.getSubString(res,"mainSkuId:'","'");

                    //??????
                    //????????????
                    try {
                        this.saveDesc(path,skuId,mainSkuidt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            remLog();
        }catch (Exception e){
            e.printStackTrace();
            return "filed";
        }
        return "ok";

    }

    public void saveDesc(String path,String skuId,String mainSkuId) throws Exception {
        //???????????????
        String res = HttpRequest.get("https://cd.jd.com/description/channel?skuId="+skuId+"&mainSkuId="+mainSkuId).cookie(getCookie()).execute().body();
        List<String> subStrArray = StringUtil.getSubStrArray(res, "/jfs/", ".jpg");
        for (String str:subStrArray) {
            String img = imgUrl+"jfs/"+str+".jpg";
            //????????????
            long size = HttpUtil.downloadFile(img, FileUtil.file(path + "/skiud_"+skuId+"/?????????/"+StringUtil.getFileName(img)));
            try {
                setLog("?????????????????????"+img);
                setLog("?????????????????????"+size);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveVideo(String path,String vid) {
        if (StrUtil.isEmpty(vid)) {
            return;
        }
        String res = HttpUtil.get("https://c.3.cn/tencent/video_v3?vid="+vid);
        if (StrUtil.isEmpty(res)) {
            return;
        }
        JSONObject obj = JSONUtil.parseObj(res);
        String payUrl = obj.getStr("playUrl");
        if (StrUtil.isEmpty(payUrl)) {
            return;
        }
        //????????????
        String fileName = StringUtil.getFileName(payUrl);
        //???????????????
        fileName = fileName.substring(fileName.lastIndexOf("."));
        long size = HttpUtil.downloadFile(payUrl, FileUtil.file(path + "/??????/"+ System.currentTimeMillis() +fileName));
        try {
            setLog("?????????????????????"+payUrl);
            setLog("?????????????????????"+size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveOffice(String path,String skuId) {
        String res = HttpUtil.get("https://club.jd.com/comment/productCommentSummaries.action?referenceIds=" + skuId);
        JSONArray commentsCount = JSONUtil.parseObj(res).getJSONArray("CommentsCount");
        commentsCount.stream().forEach(it -> {
            JSONObject obj = JSONUtil.parseObj(it);
            //????????????
            String commentCountStr = obj.getStr("CommentCountStr");
            //????????????
            String DefaultGoodCountStr = obj.getStr("DefaultGoodCountStr");
            //??????
            String GoodCountStr = obj.getStr("GoodCountStr");
            //??????
            String AfterCountStr = obj.getStr("AfterCountStr");
            //????????????
            String VideoCountStr = obj.getStr("VideoCountStr");
            //?????????
            String GoodRateShow = obj.getStr("GoodRateShow")+"%";
            //??????
            String GeneralCountStr = obj.getStr("GeneralCountStr");
            //??????
            String PoorCountStr = obj.getStr("PoorCountStr");

            String dir = path + "/????????????.xls";
            HSSFWorkbook sheets = new HSSFWorkbook();
            HSSFSheet sheet = sheets.createSheet("??????");
            sheet.setDefaultColumnWidth(10);
            sheet.setDefaultRowHeight((short) 500);
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("???????????????");
            row.createCell(1).setCellValue(commentCountStr);
            row.createCell(3).setCellValue("???????????????");
            row.createCell(4).setCellValue(DefaultGoodCountStr);
            row.createCell(6).setCellValue("?????????");
            row.createCell(7).setCellValue(GoodCountStr);
            row = sheet.createRow(1);
            row.createCell(0).setCellValue("?????????");
            row.createCell(1).setCellValue(AfterCountStr);
            row.createCell(3).setCellValue("???????????????");
            row.createCell(4).setCellValue(VideoCountStr);
            row.createCell(6).setCellValue("????????????");
            row.createCell(7).setCellValue(GoodRateShow);
            row = sheet.createRow(2);
            row.createCell(0).setCellValue("?????????");
            row.createCell(1).setCellValue(GeneralCountStr);
            row.createCell(3).setCellValue("?????????");
            row.createCell(4).setCellValue(PoorCountStr);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(dir);
                sheets.write(outputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }

}
