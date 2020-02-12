package com.wk.file.controller;

import com.wk.file.FastDFSFile;
import com.wk.file.utils.FastDFSUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("upload")
@CrossOrigin
public class FileUploadController {

    /**
     * 文件上传
     * @RequestParam：接收前端传入的参数
     */
    @PostMapping
    public Result fileUpload(@RequestParam(value = "file")MultipartFile file) throws Exception {
        //封装文件信息
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),     //文件名
                file.getBytes(),                //文件字节数组
                StringUtils.getFilenameExtension(file.getOriginalFilename())    //获取文件扩展名
        );

        //调用FastDFSUtil工具类将文件传入到FastDFS中
        String[] fileUpload = FastDFSUtil.fileUpload(fastDFSFile);

        //拼接访问地址http://192.168.211.132:8080/group1/M00/00/00/wKjThF1aW9CAOUJGAAClQrJOYvs424.jpg
        String url = FastDFSUtil.getTrackerInfo()+"/"+fileUpload[0]+"/"+fileUpload[1];
        return new Result(true, StatusCode.OK,"文件上传成功！",url);
    }
}
